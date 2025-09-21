package com.mycompany;

import com.mycompany.currency.*;
import com.mycompany.entity.User;
import com.mycompany.currency.CurrencyQuotes;
import com.mycompany.my.MyTimer;
import com.mycompany.service.UserService;
import com.mycompany.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.mycompany.currency.BybitCryptocurrencyPair.*;
import static com.mycompany.currency.CalculatedQuoteCurrencyPair.*;
import static com.mycompany.currency.MoexCurrencyPair.*;

/**
 * Класс, описывающий Telegram-бота.
 */
@Component
public class TelegramBot extends TelegramLongPollingBot {

  private final String username;
  private final String token;
  private final UserService userService;

  @Autowired
  public TelegramBot(
      @Value("${bot.username}") String username,
      @Value("${bot.token}") String token,
      UserService userService) {
    this.username = username;
    this.token = token;
    this.userService = userService;
  }

  @Override
  public String getBotUsername() {
    return username; // этот параметр можно получить у телеграм-бота @BotFather https://t.me/BotFather
  }

  @Override
  public String getBotToken() {
    return token; // этот параметр можно получить у телеграм-бота @BotFather https://t.me/BotFather
  }


  // котировки валютных пар
  private CurrencyQuotes quotes = new CurrencyQuotes();

  // Коллекция для хранения множества chatId пользователей бота
  private Set<Long> userChatIds = ConcurrentHashMap.newKeySet(); // так мы получаем потокобезопасный HashSet


  private User user = readUserFromDataBase();
  private User temp = new User(user);
  private User tempBefore;

  // имитируем чтение инфы из базы
  private User readUserFromDataBase() {
    Map<String, Boolean> settings = new LinkedHashMap<>();
    settings.put(USD_RUB.name(), false);
    settings.put(EUR_RUB.name(), false);
    settings.put(CNY_RUB.name(), false);
    settings.put(TRY_RUB.name(), false);
    settings.put(EUR_USD.name(), false);
    settings.put(USD_KZT.name(), false);
    settings.put(RUB_KZT.name(), false);
    settings.put(USD_BYN.name(), false);
    settings.put(BTC_USDT.name(), true);
    settings.put(ETH_USDT.name(), true);
    settings.put(SOL_USDT.name(), false);
    settings.put(WLKN_USDT.name(), true);

    return new User(3, "den", "chik", "pyramid", settings);
  }

  // текстовые константы для callback data кнопок меню "настройки"
  private static final String FIAT_CURRENCIES_SETTINGS = "fiatSettings";
  private static final String CRYPTO_CURRENCIES_SETTINGS = "cryptoSettings";
  private static final String SAVE = "save";
  private static final String BACK = "back";
  private static final String EXIT = "exit";
  private static final String SHOW_ALL_FIAT = "show_all_fiat";
  private static final String SHOW_ALL_CRYPTO = "show_all_crypto";
  private static final String DONT_SHOW_ALL_FIAT = "don't_show_all_fiat";
  private static final String DONT_SHOW_ALL_CRYPTO = "don't_show_all_crypto";
  private static final String FIAT_SETTINGS_MESSAGE_TEXT = getCurrencySettingsMessageText(
      "валютных");
  private static final String CRYPTO_SETTINGS_MESSAGE_TEXT = getCurrencySettingsMessageText(
      "криптовалютных");

  private static String getCurrencySettingsMessageText(String currenciesType) {
    return "С помощью кнопок ниже настройте отображение " + currenciesType +
        """
             пар в сообщениях бота, затем нажмите "Сохранить".
            
            ✅ - данная валютная пара будет отображаться в сообщениях бота
            ❌ - данная валютная пара не будет отображаться в сообщениях бота
            """;
  }

  //    Ссылка на сообщение с клавиатурой, которое нужно будет редактировать или удалять.
//
//    Эта ссылка должна быть именно полем класса, а не локальной переменной метода onUpdateReceived(), т.к. каждое
//    новое нажатие любой из кнопок - это новый вызов этого метода. Если эта переменная будет локальной переменной
//    метода, ее значение будет обнуляться после каждого нажатия кнопок. А нам нужно, чтобы состояние этой переменной
//    сохранялось от одного нажатия кнопки к другому.
  private Message messageWithKeyboard;

  // TODO https://javarush.com/groups/posts/3219-java-proekt-ot-a-do-ja-realizuem-command-pattern-dlja-rabotih-s-botom-chastjh-1
  // вызывается автоматически всякий раз при получении сообщения (update) от юзера
  @Override
  public void onUpdateReceived(Update update) {
    // Проверяем, содержит ли update сообщение и содержится ли в сообщении текст
    if (update.hasMessage() && update.getMessage().hasText()) {
      String messageText = update.getMessage().getText(); // получаем текст сообщения
      long chatId = update.getMessage().getChatId();

      // обработка нажатий пунктов меню
      switch (messageText) {
        case "/start" -> {
          if (!userExists(chatId)) {
            addUser(chatId); // ???????? нужна ли эта строка ?

            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            String userName = update.getMessage().getFrom().getUserName();
            userService.add(new User(chatId, firstName, lastName, userName));
          }

          // отправляем сообщения с котировками фиатных валют и крипты пользователю
          quotes.getRelevantQuotes();
          sendMessage(chatId, quotes.getFiatCurrenciesQuotesMessage());
          sendMessage(chatId, quotes.getCryptocurrenciesQuotesMessage());
        }

        case "/settings" -> {
          // этот кусок кода при вызове настроек пользователем удаляет предыдущее сообщение с настройками
          // таким образом мы не допускаем, чтобы в боте было одновременно два сообщения с настройками
          // иначе - ошибки
          // удаляем предыдущее сообщение с настройками, если оно есть
          // сообщений с настройками должно быть только в единственном виде во избежание ошибок, связанных с этим дублированием
          if (messageWithKeyboard != null) {
            deletePreviousMessageWithKeyboard(chatId);
            temp = new User(user); // также сбрасываем temp в исходное состояние
          }

          var keyboard = createSettingsKeyboard();
          messageWithKeyboard = sendMessage(chatId, "Что необходимо настроить ?", keyboard);
        }

        case "/exit" -> {
          sendMessage(chatId, "Бот остановлен !");
          deleteUser(chatId);
          userService.delete(new User(chatId));
        }
      }
    } else if (update.hasCallbackQuery()) {
      var callbackQuery = update.getCallbackQuery();
      var callbackQueryData = callbackQuery.getData();
      long chatId = callbackQuery.getMessage().getChatId();

      // обработка нажатия кнопок
      switch (callbackQueryData) {
        case FIAT_CURRENCIES_SETTINGS -> { // кнопка "отображение фиатных валют"
          // удалить предыдущую клавиатуру
          deletePreviousMessageWithKeyboard(chatId);
          // не надо ли обнулять здесь messageWithKeyboard
          // конкретно здесь необязательно, так как спустя пару строчек мы присваеваем в эту переменную новое значение

          ///////////////////////////////////////////////////////////////////////////
          // сначала надо считать настройки из базы и применить их настройках бота //
          ///////////////////////////////////////////////////////////////////////////

          var keyboard = createFiatCurrenciesKeyboard(user); // user или temp???
          messageWithKeyboard = sendMessage(chatId, FIAT_SETTINGS_MESSAGE_TEXT, keyboard);
        }

        case CRYPTO_CURRENCIES_SETTINGS -> { // кнопка "отображение криптовалют"
          deletePreviousMessageWithKeyboard(chatId);

          ///////////////////////////////////////////////////////////////////////////
          // сначала надо считать настройки из базы и применить их настройках бота //
          ///////////////////////////////////////////////////////////////////////////

          var keyboard = createCryptoCurrenciesKeyboard(user);
          messageWithKeyboard = sendMessage(chatId, CRYPTO_SETTINGS_MESSAGE_TEXT, keyboard);
        }

        case EXIT -> { // кнопка "Выход"
          deletePreviousMessageWithKeyboard(chatId);
        }

        case SHOW_ALL_FIAT -> { // кнопка "показывать все" в меню "отображение фиатных валют"
          temp.enableFiat(); //user.enableFiat();
          InlineKeyboardMarkup keyboard = createFiatCurrenciesKeyboard(temp); // читаем из user
          editMessage(chatId, messageWithKeyboard.getMessageId(), keyboard);
        }

        case
            DONT_SHOW_ALL_FIAT -> { // кнопка "не показывать все" в меню "отображение фиатных валют"
          temp.disableFiat(); //user.disableFiat();
          InlineKeyboardMarkup keyboard = createFiatCurrenciesKeyboard(temp);
          editMessage(chatId, messageWithKeyboard.getMessageId(),
              keyboard); // error: message is not modified
        }

        case SHOW_ALL_CRYPTO -> { // кнопка "показывать все" в меню "отображение криптовалют"
          temp.enableCrypto(); //user.enableCrypto();
          InlineKeyboardMarkup keyboard = createCryptoCurrenciesKeyboard(temp);
          editMessage(chatId, messageWithKeyboard.getMessageId(),
              keyboard);  // error: message is not modified
        }

        case
            DONT_SHOW_ALL_CRYPTO -> { // кнопка "не показывать все" в меню "отображение криптовалют"
          temp.disableCrypto(); //user.disableCrypto();
          InlineKeyboardMarkup keyboard = createCryptoCurrenciesKeyboard(temp);
          editMessage(chatId, messageWithKeyboard.getMessageId(), keyboard);
        }

        case SAVE -> { // кнопка "Сохранить"
          deletePreviousMessageWithKeyboard(chatId);

          ///////////////////////////////////////////////
          // здесь д.б. код сохранения настроек в базе //
          ///////////////////////////////////////////////

          user = new User(temp);
          sendMessage(chatId, "Настройки сохранены!");
        }

        case BACK -> { // кнопка "< Назад"
          temp = new User(user); //////////////////////////////////////////////
          deletePreviousMessageWithKeyboard(chatId);
          var keyboard = createSettingsKeyboard();
          messageWithKeyboard = sendMessage(chatId, "Что необходимо настроить ?", keyboard);
        }

        default -> { // эта ветка обрабатывает нажатия кнопок с валютными парами
          if (temp.getUserSettings().containsKey(callbackQueryData)) { // если это валютная пара
            //if (user.getUserSettings().containsKey(callbackQueryData)) { // если это валютная пара
            //user.toggleShow(callbackQueryData); // инвертируем настройку показа этой валютной пары
            temp.toggleShow(callbackQueryData); // инвертируем настройку показа этой валютной пары
            InlineKeyboardMarkup keyboard = null;
            // здесь я при нажатии на кнопку создаю новую клавиатуру и вставляю ее в editMessage. Мб имеет
            // смысл редактировать только одну кнопку, а остальную клаву не менять ?
            if (CurrencyPair.valueOf(callbackQueryData).isFiat()) {
              keyboard = createFiatCurrenciesKeyboard(temp);
            }

            if (CurrencyPair.valueOf(callbackQueryData).isCrypto()) {
              keyboard = createCryptoCurrenciesKeyboard(temp);
            }
            editMessage(chatId, messageWithKeyboard.getMessageId(), keyboard);
          }
        }
      }
    }
  }

  // добавляет chatId нового юзера
  public void addUser(long userChatId) {
    userChatIds.add(userChatId);
  }

  // удаляет chatId юзера
  public void deleteUser(long userChatId) {
    userChatIds.remove(userChatId);
  }

  private boolean userExists(long chatId) {
    return userService.getByChatId(chatId) != null;
  }

  /**
   * Создает и устанавливает таймер для периодической отправки биржевой информации всем юзерам
   * бота.
   * <p>
   * Timer и TimerTask - это служебные классы Java, используемые для планирования задач в фоновом
   * потоке. В двух словах: TimerTask is the task to perform and Timer is the scheduler.
   *
   * @param startTimeStr - время и часовой пояс первого запуска задачи (task) в виде текста в
   *                     формате "11:00:00 Europe/Moscow"
   * @param period       - период повтора задачи (task)
   * @param unit         - единица измерения времени для period
   */
  public void sendExchangeInfoToAllUsersAt(String startTimeStr, long period, TimeUnit unit) {
    String[] timeData = startTimeStr.split(" "); // timeData[0] - время, timeData[1] - часовой пояс
    var parsedTime = LocalTime.parse(timeData[0]);
    var zoneId = ZoneId.of(timeData[1]);

    // Дата запуска - сегодня или завтра (см. коммент ниже)
    var startTime = ZonedDateTime.now(zoneId).with(parsedTime);
    // Если полученное время запуска сегодня уже прошло, то увеличиваем дату запуска на 1 день, таким образом
    // первый запуск состоится завтра в это же время.
    // Если еще не прошло, то оставляем без изменения, и запуск состоится сегодня в это время.
    if (startTime.isBefore(ZonedDateTime.now(zoneId))) {
      startTime = startTime.plusDays(1);
    }

    new MyTimer().schedule(sendQuotesInfoMessageDailyTask, startTime, period, unit);
  }

  // Задача (task) для выполнения по таймеру MyTimer.
  // Суть задачи: получаем котировки фиатных валют и крипты и рассылаем сообщение с ними всем юзерам
  private MyTimer.MyTimerTask sendQuotesInfoMessageDailyTask = () -> {
    quotes.getRelevantQuotes();
    sendToAll(quotes.getFiatCurrenciesQuotesMessage());
    sendToAll(quotes.getCryptocurrenciesQuotesMessage());
  };

  /**
   * Отправляет сообщение всем зарегистрированным пользователям. Если к моменту выполнения этого
   * метода список пользователям будет пустой, цикл просто не запустится и все, исключений никаких
   * не будет, т.к. foreach при компиляции заменяется на while(iterator.hasNext()) { }
   *
   * @param text текст сообщения
   */
  private void sendToAll(String text) {
    SendMessage sendMessage = new SendMessage();
    try {
      for (long chatId : userChatIds) {
        sendMessage.setChatId(chatId); // почему я здесь не юзаю метод send() ????
        sendMessage.setText(text);
        execute(sendMessage); // отправляем сообщение
      }
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  /**
   * Отправляет сообщение одному пользователю.
   *
   * @param chatId id чата юзера, которому будет отправлено сообщение
   * @param text   текст сообщения
   */
  private void sendMessage(long chatId, String text) {
    try {
      execute(new SendMessage("" + chatId, text));
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  /**
   *
   */
  private Message sendMessage(long userChatId, String text, InlineKeyboardMarkup keyboard) {
    try {
      return execute(
          SendMessage.builder().chatId(userChatId).text(text).replyMarkup(keyboard).build());
    } catch (TelegramApiException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  /**
   *
   */
  private void editMessage(long chatId, int messageId, InlineKeyboardMarkup keyboard) {
    var tempBeforeTemp = new User(temp);
    try {
      // сделать проверку: сообщение изменено или то же самое // мб сравнивать user и temp???????????????
      execute(EditMessageReplyMarkup
          .builder()
          .chatId(chatId)
          .messageId(messageId)
          .replyMarkup(keyboard)
          .build());
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  /**
   * только для дебага ///////////////////////////////////////////////////////////////////////////
   */
  private void editMessage(long chatId, int messageId, InlineKeyboardMarkup keyboard, String text) {
    try {
      execute(EditMessageText
          .builder()
          .chatId(chatId)
          .messageId(messageId)
          .replyMarkup(keyboard)
          .text(text)
          .build());
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  /**
   *
   */
  private void deleteMessage(long chatId, Message message) {
    try {
      execute(DeleteMessage.builder().chatId(chatId).messageId(message.getMessageId()).build());
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  /**
   *
   */
  private void deletePreviousMessageWithKeyboard(long chatId) {
    deleteMessage(chatId, messageWithKeyboard);
    messageWithKeyboard = null;
  }

  // создаем клавиатуру для настройки валют
  private InlineKeyboardMarkup createSettingsKeyboard() {
    List<InlineKeyboardButton> firstRow = List.of(
        createButton("отображение фиатных валют", FIAT_CURRENCIES_SETTINGS));
    var secondRow = List.of(createButton("отображение криптовалют", CRYPTO_CURRENCIES_SETTINGS));
    var thirdRow = List.of(createButton("◀ Выход", EXIT));
    List<List<InlineKeyboardButton>> rows = List.of(firstRow, secondRow, thirdRow);
    return new InlineKeyboardMarkup(rows);
  }

  private InlineKeyboardMarkup createFiatCurrenciesKeyboard(User user) {
    return createCurrenciesKeyboard(CurrencyQuotes.FIAT_CURRENCY_PAIRS, user);
  }

  // клавиатура для настройки отображения крипты
  private InlineKeyboardMarkup createCryptoCurrenciesKeyboard(User user) {
    return createCurrenciesKeyboard(Utilities.CRYPTO_CURRENCY_PAIRS, user);
  }

  // клавиатура для настройки отображения валютных пар
  private InlineKeyboardMarkup createCurrenciesKeyboard(List<CurrencyPair> currencyPairs,
      User user) {
    // из списка валютных пар создаем строки с кнопками, в каждой по две кнопки
    List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
    for (int i = 0; i < currencyPairs.size(); i += 2) {
      keyboardRows.add(
          createRowWithButtonsFor(currencyPairs.get(i), currencyPairs.get(i + 1), user));
    }

    String callbackDataShow = getCallbackDataForShowButton(currencyPairs);
    String callbackDataDontShow = "don't_" + callbackDataShow;

    keyboardRows.add(List.of(createButton("показывать все", callbackDataShow),
        createButton("не показывать все", callbackDataDontShow)));

    keyboardRows.add(List.of(createButton("\uD83C\uDD97 Сохранить", SAVE),
        createButton("◀ Назад", BACK)));
    return new InlineKeyboardMarkup(keyboardRows);
  }

  private String getCallbackDataForShowButton(List<CurrencyPair> currencyPairs) {
    if (currencyPairs.equals(Utilities.CRYPTO_CURRENCY_PAIRS)) {
      return SHOW_ALL_CRYPTO;
    }
    if (currencyPairs.equals(CurrencyQuotes.FIAT_CURRENCY_PAIRS)) {
      return SHOW_ALL_FIAT;
    }
    throw new IllegalArgumentException();
  }

  // создает кнопку с текстом и callbackData
  // callbackData - это инфа, которая будет отправлена на сервер при нажатии кнопки
  private InlineKeyboardButton createCurrencyPairButton(CurrencyPair currencyPair, User user) {
    String textOnButton = currencyPair.getShortName();
    if (user.isShow(currencyPair)) {
      textOnButton += ": ✅";
    } else {
      textOnButton += ": ❌";
    }

    String callbackData = currencyPair.name(); // Что будет отсылаться серверу при нажатии на кнопку
    return createButton(textOnButton, callbackData);
  }

  // создает кнопку с текстом и callbackData
  // callbackData - это инфа, которая будет отправлена на сервер при нажатии кнопки
  private InlineKeyboardButton createButton(String text, String callbackData) {
    return InlineKeyboardButton.builder().text(text).callbackData(callbackData).build();
  }

  private List<InlineKeyboardButton> createRowWithButtonsFor(CurrencyPair first,
      CurrencyPair second, User user) {
    return List.of(
        createCurrencyPairButton(first, user),
        createCurrencyPairButton(second, user)
    );
  }
}
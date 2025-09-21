package com.mycompany.entity;

import com.mycompany.Utilities;
import com.mycompany.currency.CurrencyPair;
import com.mycompany.currency.CurrencyQuotes;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mycompany.currency.BybitCryptocurrencyPair.*;
import static com.mycompany.currency.CalculatedQuoteCurrencyPair.*;
import static com.mycompany.currency.MoexCurrencyPair.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // должен ли этот конструктор быть public ???
@ToString
@EqualsAndHashCode
public class User {

  @Id
  @Column(name = "telegram_user_id")
  private long chatId;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "telegram_username")
  private String userName;

  @Column(name = "registered_at")
  private Instant registeredAt;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "users_display_currencies",
      joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "users_display_currencies_fk"))
  )
  @Column(name = "currency_pair", nullable = false)
  private Set<String> displayableCurrencies = new LinkedHashSet<>();

    /*@ElementCollection(fetch = FetchType.EAGER)
    @Cascade({ org.hibernate.annotations.CascadeType.ALL }) // обозначает, какие из методов интерфейса Session будут распространяться каскадно к ассоциированным сущностям
    @CollectionTable(
            name = "user_settings", // имя вспомогательной таблицы
            joinColumns = @JoinColumn( // для задания ссылки на внешний ключ
                                        name="user_chat_id", // название внешнего ключа
                                        foreignKey = @ForeignKey(
                                                name = "user_settings_fk"*//*, // название внешнего ключа
                                                value = ConstraintMode.CONSTRAINT,
                                                foreignKeyDefinition = "foreign key (user_chat_id) references" +
                                                        "telegram_users ON DELETE CASCADE"*//*)))
    @MapKeyColumn(name = "currency_pair") // название столбца для ключей
    @Column(name = "is_show") // название столбца для значений
    private Map<String, Boolean> displayableCurrencies = new LinkedHashMap<>();*/

  private static final Set<String> DEFAULT_SETTINGS;

  static {
    DEFAULT_SETTINGS = new LinkedHashSet<>();

    DEFAULT_SETTINGS.add(USD_RUB.name());
    DEFAULT_SETTINGS.add(EUR_RUB.name());
    DEFAULT_SETTINGS.add(CNY_RUB.name());
    DEFAULT_SETTINGS.add(TRY_RUB.name());
    DEFAULT_SETTINGS.add(EUR_USD.name());
    DEFAULT_SETTINGS.add(USD_KZT.name());
    DEFAULT_SETTINGS.add(RUB_KZT.name());
    DEFAULT_SETTINGS.add(USD_BYN.name());

    DEFAULT_SETTINGS.add(BTC_USDT.name());
    DEFAULT_SETTINGS.add(ETH_USDT.name());
    DEFAULT_SETTINGS.add(SOL_USDT.name());
    DEFAULT_SETTINGS.add(LINEA_USDT.name());
  }

  public User(long chatId) {
    this.chatId = chatId;
  }

  public User(long chatId, String firstName, String lastName, String userName) {
    this(chatId, firstName, lastName, userName, Instant.now(), DEFAULT_SETTINGS);
  }

  // конструктор клонирования
  public User(User userToClone) {
    this.chatId = userToClone.chatId;
    this.firstName = userToClone.firstName;
    this.lastName = userToClone.lastName;
    this.userName = userToClone.userName;
    this.registeredAt = userToClone.registeredAt;
    this.displayableCurrencies = new LinkedHashSet<>(userToClone.displayableCurrencies);
  }

  public User(long chatId, String firstName, String lastName, String userName,
      Set<String> settings) {
    this(chatId, firstName, lastName, userName, Instant.now(), settings);
  }

  public void toggleShow(String currencyPairName) {
    boolean isShow = displayableCurrencies.contains(currencyPairName);
    displayableCurrencies.add(currencyPairName);
  }

  public boolean isShow(CurrencyPair currencyPair) {
    return displayableCurrencies.contains(currencyPair.name());
  }

  public void enableFiat() {
    show(CurrencyQuotes.FIAT_CURRENCY_PAIRS, true);
  }

  public void disableFiat() {
    show(CurrencyQuotes.FIAT_CURRENCY_PAIRS, false);
  }

  public void enableCrypto() {
    show(Utilities.CRYPTO_CURRENCY_PAIRS, true);
  }

  public void disableCrypto() {
    show(Utilities.CRYPTO_CURRENCY_PAIRS, false);
  }

  private void show(List<CurrencyPair> currencyPairs, boolean show) {
    for (CurrencyPair pair : currencyPairs) {
      if (show) {
        displayableCurrencies.add(pair.name());
      } else {
        displayableCurrencies.remove(pair.name());
      }
    }
  }

}
package com.mycompany.model;

import com.mycompany.Utilities;
import com.mycompany.currency.CurrencyPair;
import com.mycompany.currency.CurrencyQuotes;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    @Column(name = "user_id")
    private long chatId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "user_name")
    private String userName;

    @ElementCollection(fetch = FetchType.EAGER) // если не указывать параметр в скобках, будет исключение
    @CollectionTable(name = "user_settings", joinColumns = @JoinColumn(name="user_chat_id")) // задаем имя вспомогательной таблицы
    @MapKeyColumn(name = "currency_pair") // название столбца для ключей
    @Column(name = "is_show") // название столбца для значений
    private Map<String, Boolean> userSettings = new LinkedHashMap<>();

    private static final Map<String, Boolean> DEFAULT_SETTINGS;

    static {
        DEFAULT_SETTINGS = new LinkedHashMap<>();

        DEFAULT_SETTINGS.put(USD_RUB.name(), true);
        DEFAULT_SETTINGS.put(EUR_RUB.name(), true);
        DEFAULT_SETTINGS.put(CNY_RUB.name(), true);
        DEFAULT_SETTINGS.put(TRY_RUB.name(), true);
        DEFAULT_SETTINGS.put(EUR_USD.name(), true);
        DEFAULT_SETTINGS.put(USD_KZT.name(), true);
        DEFAULT_SETTINGS.put(RUB_KZT.name(), true);
        DEFAULT_SETTINGS.put(USD_BYN.name(), true);

        DEFAULT_SETTINGS.put(BTC_USDT.name(), true);
        DEFAULT_SETTINGS.put(ETH_USDT.name(), true);
        DEFAULT_SETTINGS.put(SOL_USDT.name(), true);
        DEFAULT_SETTINGS.put(WLKN_USDT.name(), false);
    }

    public User(long chatId) {
        this.chatId = chatId;
    }

    public User(long chatId, String firstName, String lastName, String userName) {
        this(chatId, firstName, lastName, userName, DEFAULT_SETTINGS);
    }

    // конструктор клонирования
    public User(User userToClone) {
        this.chatId = userToClone.chatId;
        this.firstName = userToClone.firstName;
        this.lastName = userToClone.lastName;
        this.userName = userToClone.userName;
        this.userSettings = new LinkedHashMap<>(userToClone.userSettings);
    }

    public void toggleShow(String currencyPairName) {
        boolean isShow = userSettings.get(currencyPairName);
        userSettings.put(currencyPairName, !isShow);
    }

    public boolean isShow(CurrencyPair currencyPair) {
        return userSettings.get(currencyPair.name());
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
            userSettings.put(pair.name(), show);
        }
    }
}
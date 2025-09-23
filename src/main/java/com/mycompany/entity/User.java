package com.mycompany.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.LinkedHashSet;
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
  private long id;

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

  public User(long id) {
    this.id = id;
  }

  public User(long id, String firstName, String lastName, String userName) {
    this(id, firstName, lastName, userName, Instant.now(), DEFAULT_SETTINGS);
  }

}
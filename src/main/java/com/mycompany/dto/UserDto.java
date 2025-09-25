package com.mycompany.dto;

import com.mycompany.Utilities;
import com.mycompany.currency.CurrencyPair;
import com.mycompany.currency.CurrencyQuotes;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static com.mycompany.currency.CurrencyQuotes.CRYPTO_CURRENCY_PAIRS;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class UserDto {

  private long chatId;
  private String firstName;
  private String lastName;
  private String userName;
  private Instant registeredAt;

  // если нужно передавать список валют
  private Set<String> displayableCurrencies;

  // меняет состояние валюты (вкл/выкл)
  public void toggleShow(String currencyPairName) {
    if (displayableCurrencies.contains(currencyPairName)) {
      displayableCurrencies.remove(currencyPairName);
    } else {
      displayableCurrencies.add(currencyPairName);
    }
    /*boolean isShow = displayableCurrencies.contains(currencyPairName);
    displayableCurrencies.add(currencyPairName);*/
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
    show(CRYPTO_CURRENCY_PAIRS, true);
  }

  public void disableCrypto() {
    show(CRYPTO_CURRENCY_PAIRS, false);
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

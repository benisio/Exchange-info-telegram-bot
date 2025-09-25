package com.mycompany.service;

import com.mycompany.currency.KucoinCryptocurrencyPair;

public interface KucoinMarketService {

  double getQuote(KucoinCryptocurrencyPair currencyPair);
}

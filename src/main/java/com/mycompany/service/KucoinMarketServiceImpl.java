package com.mycompany.service;

import com.mycompany.HttpRequestFactory;
import com.mycompany.currency.KucoinCryptocurrencyPair;
import com.mycompany.dto.KucoinApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KucoinMarketServiceImpl implements KucoinMarketService {

  private final HttpRequestFactory httpRequestFactory;

  @Override
  public double getQuote(KucoinCryptocurrencyPair currencyPair) {
    KucoinApiResponse response = httpRequestFactory.getKucoinMarketData(currencyPair);
    return Double.parseDouble(response.getMarketData().getPrice());
  }
}

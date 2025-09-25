package com.mycompany.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KucoinApiResponse {

  private String code;

  @JsonProperty("data")
  private MarketData marketData;

  // геттеры и сеттеры
  @Getter
  @Setter
  public static class MarketData {
    private long time;
    private String sequence;
    private String price;
    private String size;
    private String bestBid;
    private String bestBidSize;
    private String bestAsk;
    private String bestAskSize;
  }
}
package org.knowm.xchange.vega.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class VegaMarketData {
    @JsonProperty("markPrice")
    public String markPrice;
    @JsonProperty("bestBidPrice")
    public String bestBidPrice;
    @JsonProperty("bestBidVolume")
    public String bestBidVolume;
    @JsonProperty("bestOfferPrice")
    public String bestOfferPrice;
    @JsonProperty("bestOfferVolume")
    public String bestOfferVolume;
    @JsonProperty("bestStaticBidPrice")
    public String bestStaticBidPrice;
    @JsonProperty("bestStaticBidVolume")
    public String bestStaticBidVolume;
    @JsonProperty("bestStaticOfferPrice")
    public String bestStaticOfferPrice;
    @JsonProperty("bestStaticOfferVolume")
    public String bestStaticOfferVolume;
    @JsonProperty("midPrice")
    public String midPrice;
    @JsonProperty("staticMidPrice")
    public String staticMidPrice;
    @JsonProperty("market")
    public String market;
    @JsonProperty("timestamp")
    public String timestamp;
    @JsonProperty("openInterest")
    public String openInterest;
    @JsonProperty("auctionEnd")
    public String auctionEnd;
    @JsonProperty("auctionStart")
    public String auctionStart;
    @JsonProperty("indicativePrice")
    public String indicativePrice;
    @JsonProperty("indicativeVolume")
    public String indicativeVolume;
    @JsonProperty("marketTradingMode")
    public String marketTradingMode;
    @JsonProperty("trigger")
    public String trigger;
    @JsonProperty("targetStake")
    public String targetStake;
    @JsonProperty("suppliedStake")
    public String suppliedStake;
    @JsonProperty("marketValueProxy")
    public String marketValueProxy;

    public String getMarkPrice() {
        return markPrice;
    }

    public String getBestBidPrice() {
        return bestBidPrice;
    }

    public String getBestBidVolume() {
        return bestBidVolume;
    }

    public String getBestOfferPrice() {
        return bestOfferPrice;
    }

    public String getBestOfferVolume() {
        return bestOfferVolume;
    }

    public String getBestStaticBidPrice() {
        return bestStaticBidPrice;
    }

    public String getBestStaticBidVolume() {
        return bestStaticBidVolume;
    }

    public String getBestStaticOfferPrice() {
        return bestStaticOfferPrice;
    }

    public String getBestStaticOfferVolume() {
        return bestStaticOfferVolume;
    }

    public String getMidPrice() {
        return midPrice;
    }

    public String getStaticMidPrice() {
        return staticMidPrice;
    }

    public String getMarket() {
        return market;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getOpenInterest() {
        return openInterest;
    }

    public String getAuctionEnd() {
        return auctionEnd;
    }

    public String getAuctionStart() {
        return auctionStart;
    }

    public String getIndicativePrice() {
        return indicativePrice;
    }

    public String getIndicativeVolume() {
        return indicativeVolume;
    }

    public String getMarketTradingMode() {
        return marketTradingMode;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getTargetStake() {
        return targetStake;
    }

    public String getSuppliedStake() {
        return suppliedStake;
    }

    public String getMarketValueProxy() {
        return marketValueProxy;
    }

}

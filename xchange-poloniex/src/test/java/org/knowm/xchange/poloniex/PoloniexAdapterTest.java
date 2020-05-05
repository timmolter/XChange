package org.knowm.xchange.poloniex;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.Assert;
import org.junit.Test;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.account.FundingRecord.Type;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.poloniex.dto.marketdata.PoloniexLoansDataTest;
import org.knowm.xchange.poloniex.dto.trade.PoloniexDepositsWithdrawalsResponse;
import org.knowm.xchange.poloniex.dto.trade.PoloniexTrade;

public class PoloniexAdapterTest {

  @Test
  public void testTradeHistory() throws IOException {

    final InputStream is =
        PoloniexTrade.class.getResourceAsStream(
            "/org/knowm/xchange/poloniex/dto/trade/adapter-trade-history.json");

    final ObjectMapper mapper = new ObjectMapper();
    final JavaType tradeArray = mapper.getTypeFactory().constructArrayType(PoloniexTrade.class);

    PoloniexTrade[] tradeHistory = mapper.readValue(is, tradeArray);

    LimitOrder result = PoloniexAdapters.adaptUserTradesToOrderStatus("102", tradeHistory);

    Assert.assertEquals(new BigDecimal("0.0102693100000000"), result.getAveragePrice());
    Assert.assertEquals(new BigDecimal("0.03000000"), result.getCumulativeAmount());
    Assert.assertEquals(null, result.getOriginalAmount());
    Assert.assertEquals("102", result.getId());
  }

  @Test
  public void testFundingHistory() throws JsonParseException, JsonMappingException, IOException {

    final InputStream is =
        PoloniexLoansDataTest.class.getResourceAsStream(
            "/org/knowm/xchange/poloniex/dto/account/deposits-withdrawals.json");

    final ObjectMapper mapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    final PoloniexDepositsWithdrawalsResponse response =
        mapper.readValue(is, PoloniexDepositsWithdrawalsResponse.class);

    final List<FundingRecord> fundingRecords = PoloniexAdapters.adaptFundingRecords(response);

    assertThat(fundingRecords).hasSize(3);
    final FundingRecord adjustment =
        fundingRecords.stream()
            .filter(record -> record.getType() == Type.AIRDROP)
            .findFirst()
            .orElseThrow(NullPointerException::new);

    assertThat(adjustment.getAddress()).isNull();
    assertThat(adjustment.getDate()).isEqualTo(new Date(1564782686000L));
    assertThat(adjustment.getCurrency()).isEqualTo(Currency.USDT);
    assertThat(adjustment.getAmount())
        .isCloseTo(new BigDecimal("0.01752476"), Offset.offset(BigDecimal.ZERO));
    assertThat(adjustment.getInternalId()).isNull();
    assertThat(adjustment.getBlockchainTransactionHash()).isNull();
    assertThat(adjustment.getType()).isEqualTo(Type.AIRDROP);
    assertThat(adjustment.getStatus()).isEqualTo(FundingRecord.Status.COMPLETE);
    assertThat(adjustment.getBalance()).isNull();
    assertThat(adjustment.getFee()).isNull();
    assertThat(adjustment.getDescription())
        .isEqualTo(
            "adjustment:USDT_AIDROP\nUSDT-TRON Airdrop\nYour airdrop for Aug 2, 2019.\nhttps://support.poloniex.circle.com/hc/en-us/articles/360029433451-USDT-TRON-Airdrop-and-Other-Frequently-Asked-Questions");

    final FundingRecord deposit =
        fundingRecords.stream()
            .filter(record -> record.getType() == FundingRecord.Type.DEPOSIT)
            .findFirst()
            .orElseThrow(NullPointerException::new);

    assertThat(deposit.getAddress()).isEqualTo("0xf015a632ac1d13aeca3513c8c76c96334c5861a3");
    assertThat(deposit.getDate()).isEqualTo(new Date(1556178200000L));
    assertThat(deposit.getCurrency()).isEqualTo(Currency.ETH);
    assertThat(deposit.getAmount())
        .isCloseTo(new BigDecimal("8.995"), Offset.offset(BigDecimal.ZERO));
    assertThat(deposit.getInternalId()).isEqualTo("152426993");
    assertThat(deposit.getBlockchainTransactionHash())
        .isEqualTo("0xa7bc2c38e97c1ed9cee7436da5912ba8c7d721dce9884ca8f6ab3e35da31dd44");
    assertThat(deposit.getType()).isEqualTo(FundingRecord.Type.DEPOSIT);
    assertThat(deposit.getStatus()).isEqualTo(FundingRecord.Status.COMPLETE);
    assertThat(deposit.getBalance()).isNull();
    assertThat(deposit.getFee()).isNull();
    assertThat(deposit.getDescription()).isEqualTo("COMPLETE");

    final FundingRecord withdrawal =
        fundingRecords.stream()
            .filter(record -> record.getType() == FundingRecord.Type.WITHDRAWAL)
            .findFirst()
            .orElseThrow(NullPointerException::new);

    assertThat(withdrawal.getAddress()).isEqualTo("0x4120F5b5A6adA3B543da0535e7a9844689f5016C");
    assertThat(withdrawal.getDate()).isEqualTo(new Date(1556435569000L));
    assertThat(withdrawal.getCurrency()).isEqualTo(Currency.ETH);
    assertThat(withdrawal.getAmount())
        .isCloseTo(new BigDecimal("2.0"), Offset.offset(BigDecimal.ZERO));
    assertThat(withdrawal.getInternalId()).isEqualTo("19322067");
    assertThat(withdrawal.getBlockchainTransactionHash())
        .isEqualTo("0xf85d3870a4c9a077cd333f59ad49db7f2b21e4a67326961c09d629f6a7bb2e9d");
    assertThat(withdrawal.getType()).isEqualTo(FundingRecord.Type.WITHDRAWAL);
    assertThat(withdrawal.getStatus()).isEqualTo(FundingRecord.Status.COMPLETE);
    assertThat(withdrawal.getBalance()).isNull();
    assertThat(withdrawal.getFee())
        .isCloseTo(new BigDecimal("0.01"), Offset.offset(BigDecimal.ZERO));
    ;
    assertThat(withdrawal.getDescription())
        .isEqualTo("COMPLETE: 0xf85d3870a4c9a077cd333f59ad49db7f2b21e4a67326961c09d629f6a7bb2e9d");
  }
}

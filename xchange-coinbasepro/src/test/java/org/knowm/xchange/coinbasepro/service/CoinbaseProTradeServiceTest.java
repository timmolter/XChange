package org.knowm.xchange.coinbasepro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CoinbaseProTradeServiceTest extends BaseWiremockTest {

    private CoinbaseProTradeService classUnderTest;

    public static final String WIREMOCK_FILES_PATH = "__files";
    private static final String ORDERS_FILE_NAME = "orders.json";
    private static final String OPENORDERS_FILE_NAME = "openOrders.json";

    @Before
    public void setup(){
        classUnderTest = (CoinbaseProTradeService) createExchange().getTradeService();
    }

    @Test
    public void ordersTest() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonRoot =
                mapper.readTree(
                        this.getClass().getResource("/" + WIREMOCK_FILES_PATH + "/" + ORDERS_FILE_NAME));

        stubFor(
                get(urlPathEqualTo("/orders"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("orders.json")));

        OpenOrders openOrders = classUnderTest.getOpenOrders();

        assertThat(openOrders).isNotNull();
        assertThat(openOrders.getOpenOrders()).hasSize(jsonRoot.size());
        LimitOrder firstOrder = openOrders.getOpenOrders().get(0);
        assertThat(firstOrder).isNotNull();
        assertThat(firstOrder.getOriginalAmount()).isNotNull().isPositive();
        assertThat(firstOrder.getId()).isNotBlank();
        assertThat(firstOrder.getInstrument()).isEqualTo(CurrencyPair.ETH_BTC);

        LimitOrder secondOrder = openOrders.getOpenOrders().get(1);
        assertThat(secondOrder).isNotNull();
        assertThat(secondOrder.getOriginalAmount()).isNotNull().isPositive();
        assertThat(secondOrder.getId()).isNotBlank();
        assertThat(secondOrder.getInstrument()).isEqualTo(CurrencyPair.LTC_BTC);

    }

    @Test
    public void openOrdersByProductTest() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonRoot =
                mapper.readTree(
                        this.getClass().getResource("/" + WIREMOCK_FILES_PATH + "/" + OPENORDERS_FILE_NAME));
        stubFor(
                get(urlPathEqualTo("/orders"))
                        .withQueryParam("status", equalTo("open"))
                        .withQueryParam("product_id", equalTo("BTC-USD"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("openOrders.json")));

        DefaultOpenOrdersParamCurrencyPair defaultOpenOrdersParamCurrencyPair =
                new DefaultOpenOrdersParamCurrencyPair(CurrencyPair.BTC_USD);

        OpenOrders openOrders = classUnderTest.getOpenOrders(defaultOpenOrdersParamCurrencyPair);
        assertThat(openOrders).isNotNull();
        assertThat(openOrders.getOpenOrders()).hasSize(jsonRoot.size());
        LimitOrder firstOrder = openOrders.getOpenOrders().get(0);
        assertThat(firstOrder).isNotNull();
        assertThat(firstOrder.getOriginalAmount()).isNotNull().isPositive();
        assertThat(firstOrder.getId()).isNotBlank();
        assertThat(firstOrder.getInstrument()).isEqualTo(CurrencyPair.BTC_USD);
    }
}
package info.bitrich.xchangestream.poloniex2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.poloniex2.dto.PoloniexWebSocketAccountNotificationsTransaction;
import info.bitrich.xchangestream.poloniex2.dto.PoloniexWebSocketEvent;
import info.bitrich.xchangestream.poloniex2.dto.PoloniexWebSocketPrivateTradeEvent;
import info.bitrich.xchangestream.poloniex2.dto.PrivateTradeEvent;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.Observable;
import org.knowm.xchange.dto.trade.UserTrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.SynchronizedValueFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

/** Created by Marcin Rabiej 22.05.2019 */
public class PoloniexStreamingUserDataService {
  private static final Logger LOG = LoggerFactory.getLogger(PoloniexStreamingUserDataService.class);

  private final PoloniexStreamingService service;
  private Observable<JsonNode> accountNotificationsSubscription;
  private final SimpleDateFormat sdf;
  private final ObjectMapper objectMapper = StreamingObjectMapperHelper.getObjectMapper();

  public PoloniexStreamingUserDataService(PoloniexStreamingService service) {
    this.service = service;
    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public Observable<UserTrade> getUserTrades() {
    return getStreamEvents()
        .filter(s -> "t".equals(s.getEventType()))
        .map(s -> ((PoloniexWebSocketPrivateTradeEvent) s).getPrivateTradeEvent())
        .share()
        .map(this::adaptTrade);
  }

  public Observable<PoloniexWebSocketEvent> getStreamEvents() {
    return accountNotificationsSubscription
        .flatMapIterable(
            s -> {
              PoloniexWebSocketAccountNotificationsTransaction transaction =
                  objectMapper.treeToValue(
                      s, PoloniexWebSocketAccountNotificationsTransaction.class);
              return Arrays.asList(transaction.getEvents());
            })
        .share();
  }

  public Observable<JsonNode> getRawStreamEvents() {
    return accountNotificationsSubscription.share();
  }

  private UserTrade adaptTrade(PrivateTradeEvent event) {
    Date date = getDate(event);
    return new UserTrade.Builder()
        .orderId(event.getOrderNumber())
        .originalAmount(event.getAmount())
        .price(event.getPrice())
        .timestamp(date)
        .feeAmount(event.getTotalFee())
        .id(event.getTradeId())
        .build();
  }

  private Date getDate(PrivateTradeEvent event) {
    try {
      return sdf.parse(event.getDate());
    } catch (ParseException e) {
      return null;
    }
  }

  public void init(String apiKey, String secretKey, SynchronizedValueFactory<Long> nonceFactory) {
    accountNotificationsSubscription =
        service.subscribeAccountNotificationsChannel(apiKey, secretKey, nonceFactory);
  }
}

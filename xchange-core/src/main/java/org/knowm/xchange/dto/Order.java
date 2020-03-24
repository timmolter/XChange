package org.knowm.xchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.instrument.Instrument;

/** Data object representing an order */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "trigger")
@JsonSubTypes({
  @JsonSubTypes.Type(value = LimitOrder.class, name = "limit"),
  @JsonSubTypes.Type(value = StopOrder.class, name = "stop"),
  @JsonSubTypes.Type(value = MarketOrder.class, name = "market")
})
public abstract class Order implements Serializable {

  private static final long serialVersionUID = -8132103343647993249L;

  /** Order type i.e. bid or ask */
  private final OrderType type;
  /** Amount to be ordered / amount that was ordered */
  private final BigDecimal originalAmount;
  /** The instrument could be a currency pair of derivative */
  private final Instrument instrument;
  /** An identifier set by the exchange that uniquely identifies the order */
  private final String id;
  /** An identifier provided by the user on placement that uniquely identifies the order */
  private final String userReference;
  /** The timestamp on the order according to the exchange's server, null if not provided */
  private final Date timestamp;
  /** Any applicable order flags */
  private final Set<IOrderFlags> orderFlags = new HashSet<>();
  /** Status of order during it lifecycle */
  private OrderStatus status;
  /** Amount to be ordered / amount that has been matched against order on the order book/filled */
  private BigDecimal cumulativeAmount;
  /** Weighted Average price of the fills in the order */
  private BigDecimal averagePrice;
  /** The total of the fees incurred for all transactions related to this order */
  private BigDecimal fee;
  /** The leverage to use for margin related to this order */
  private String leverage = null;

  /**
   * @param type Either BID (buying) or ASK (selling)
   * @param originalAmount The amount to trade
   * @param currencyPair currencyPair The identifier (e.g. BTC/USD)
   * @param id An id (usually provided by the exchange)
   * @param timestamp the absolute time for this order according to the exchange's server, null if
   *     not provided
   */
  public Order(
      OrderType type, BigDecimal originalAmount, Instrument instrument, String id, Date timestamp) {
    this(type, originalAmount, instrument, id, timestamp, null, null, null, null);
  }

  /**
   * @param type Either BID (buying) or ASK (selling)
   * @param originalAmount The amount to trade
   * @param instrument The identifier (e.g. BTC/USD)
   * @param id An id (usually provided by the exchange)
   * @param timestamp the absolute time for this order according to the exchange's server, null if
   *     not provided
   * @param averagePrice the averagePrice of fill belonging to the order
   * @param cumulativeAmount the amount that has been filled
   * @param fee the fee associated with this order
   * @param status the status of the order at the exchange
   */
  public Order(
      OrderType type,
      BigDecimal originalAmount,
      Instrument instrument,
      String id,
      Date timestamp,
      BigDecimal averagePrice,
      BigDecimal cumulativeAmount,
      BigDecimal fee,
      OrderStatus status) {

    this(
        type,
        originalAmount,
        instrument,
        id,
        timestamp,
        averagePrice,
        cumulativeAmount,
        fee,
        status,
        100000000 + new Random().nextInt(100000000) + "");
  }

  /**
   * @param type Either BID (buying) or ASK (selling)
   * @param originalAmount The amount to trade
   * @param instrument The identifier (e.g. BTC/USD)
   * @param id An id (usually provided by the exchange)
   * @param timestamp the absolute time for this order according to the exchange's server, null if
   *     not provided
   * @param averagePrice the averagePrice of fill belonging to the order
   * @param cumulativeAmount the amount that has been filled
   * @param fee the fee associated with this order
   * @param status the status of the order at the exchange
   * @param userReference a reference provided by the user to identify the order
   */
  public Order(
      OrderType type,
      BigDecimal originalAmount,
      Instrument instrument,
      String id,
      Date timestamp,
      BigDecimal averagePrice,
      BigDecimal cumulativeAmount,
      BigDecimal fee,
      OrderStatus status,
      String userReference) {

    this.type = type;
    this.originalAmount = originalAmount;
    this.instrument = instrument;
    this.id = id;
    this.timestamp = timestamp;
    this.averagePrice = averagePrice;
    this.cumulativeAmount = cumulativeAmount;
    this.fee = fee;
    this.status = status;
    this.userReference = userReference;
  }

  private static String print(BigDecimal value) {
    return value == null ? null : value.toPlainString();
  }

  /**
   * The total of the fees incurred for all transactions related to this order
   *
   * @return null if this information is not available on the order level on the given exchange in
   *     which case you will have to navigate trades which filled this order to calculate it
   */
  public BigDecimal getFee() {
    return fee;
  }

  public void setFee(BigDecimal fee) {
    this.fee = fee;
  }

  /** @return The type (BID or ASK) */
  public OrderType getType() {

    return type;
  }

  /**
   * @return The type (PENDING_NEW, NEW, PARTIALLY_FILLED, FILLED, PENDING_CANCEL, CANCELED,
   *     PENDING_REPLACE, REPLACED, STOPPED, REJECTED or EXPIRED)
   */
  public OrderStatus getStatus() {

    return status;
  }

  /** The amount to trade */
  public BigDecimal getOriginalAmount() {

    return originalAmount;
  }

  /** The amount that has been filled */
  public BigDecimal getCumulativeAmount() {

    return cumulativeAmount;
  }

  public void setCumulativeAmount(BigDecimal cumulativeAmount) {

    this.cumulativeAmount = cumulativeAmount;
  }

  @JsonIgnore
  public BigDecimal getCumulativeCounterAmount() {
    if (cumulativeAmount != null
        && averagePrice != null
        && averagePrice.compareTo(BigDecimal.ZERO) > 0) {
      return cumulativeAmount.multiply(averagePrice);
    }
    return null;
  }

  /** @return The remaining order amount */
  public BigDecimal getRemainingAmount() {
    if (cumulativeAmount != null && originalAmount != null) {
      return originalAmount.subtract(cumulativeAmount);
    }
    return originalAmount;
  }

  /**
   * The average price of the fills in the order.
   *
   * @return null if this information is not available on the order level on the given exchange in
   *     which case you will have to navigate trades which filled this order to calculate it
   */
  public BigDecimal getAveragePrice() {

    return averagePrice;
  }

  public void setAveragePrice(BigDecimal averagePrice) {

    this.averagePrice = averagePrice;
  }
  /**
   * @deprecated CurrencyPair is a subtype of instrument <br>
   *     use {@link #getInstrument()} instead like this:
   *     <blockquote>
   *     <pre>
   * getInstrument()
   * </pre>
   *     </blockquote>
   */
  @Deprecated
  public CurrencyPair getCurrencyPair() {
    if (!(instrument instanceof CurrencyPair)) {
      throw new IllegalStateException(
          "The instrument of this order is not a currency pair: " + instrument);
    }
    return (CurrencyPair) instrument;
  }

  /** @return The instrument to be bought or sold */
  public Instrument getInstrument() {
    return instrument;
  }

  /** @return A unique identifier (normally provided by the exchange) */
  public String getId() {

    return id;
  }

  /** @return A unique identifier provided by the user on placement */
  public String getUserReference() {

    return userReference;
  }

  public Date getTimestamp() {

    return timestamp;
  }

  public Set<IOrderFlags> getOrderFlags() {

    return orderFlags;
  }

  public void setOrderFlags(Set<IOrderFlags> flags) {

    this.orderFlags.clear();
    if (flags != null) {
      this.orderFlags.addAll(flags);
    }
  }

  public boolean hasFlag(IOrderFlags flag) {

    return orderFlags.contains(flag);
  }

  public void addOrderFlag(IOrderFlags flag) {

    orderFlags.add(flag);
  }

  public void setOrderStatus(OrderStatus status) {

    this.status = status;
  }

  public String getLeverage() {
    return leverage;
  }

  public void setLeverage(String leverage) {
    this.leverage = leverage;
  }

  @Override
  public String toString() {

    return "Order [type="
        + type
        + ", originalAmount="
        + print(originalAmount)
        + ", cumulativeAmount="
        + print(cumulativeAmount)
        + ", averagePrice="
        + print(averagePrice)
        + ", fee="
        + print(fee)
        + ", instrument="
        + instrument
        + ", id="
        + id
        + ", timestamp="
        + timestamp
        + ", status="
        + status
        + ", flags="
        + orderFlags
        + ", userReference="
        + userReference
        + "]";
  }

  @Override
  public int hashCode() {

    int hash = 7;
    hash = 83 * hash + (this.type != null ? this.type.hashCode() : 0);
    hash = 83 * hash + (this.originalAmount != null ? this.originalAmount.hashCode() : 0);
    hash = 83 * hash + (this.instrument != null ? this.instrument.hashCode() : 0);
    hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 83 * hash + (this.timestamp != null ? this.timestamp.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {

    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Order other = (Order) obj;
    if (this.type != other.type) {
      return false;
    }
    if ((this.originalAmount == null)
        ? (other.originalAmount != null)
        : this.originalAmount.compareTo(other.originalAmount) != 0) {
      return false;
    }
    if ((this.instrument == null)
        ? (other.instrument != null)
        : !this.instrument.equals(other.instrument)) {
      return false;
    }
    if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
      return false;
    }
    if (this.timestamp != other.timestamp
        && (this.timestamp == null || !this.timestamp.equals(other.timestamp))) {
      return false;
    }
    return true;
  }

  public enum OrderType {

    /** Buying order (the trader is providing the counter currency) */
    BID,
    /** Selling order (the trader is providing the base currency) */
    ASK,
    /**
     * This is to close a short position when trading crypto currency derivatives such as swaps,
     * futures for CFD's.
     */
    EXIT_ASK,
    /**
     * This is to close a long position when trading crypto currency derivatives such as swaps,
     * futures for CFD's.
     */
    EXIT_BID;

    public OrderType getOpposite() {
      switch (this) {
        case BID:
          return ASK;
        case ASK:
          return BID;
        case EXIT_ASK:
          return EXIT_BID;
        case EXIT_BID:
          return EXIT_ASK;
        default:
          return null;
      }
    }
  }

  public enum OrderStatus {

    /** Initial order when instantiated */
    PENDING_NEW,
    /** Initial order when placed on the order book at exchange */
    NEW,
    /** Partially match against opposite order on order book at exchange */
    PARTIALLY_FILLED,
    /** Fully match against opposite order on order book at exchange */
    FILLED,
    /** Waiting to be removed from order book at exchange */
    PENDING_CANCEL,
    /** Order was partially canceled at exchange */
    PARTIALLY_CANCELED,
    /** Removed from order book at exchange */
    CANCELED,
    /** Waiting to be replaced by another order on order book at exchange */
    PENDING_REPLACE,
    /** Order has been replace by another order on order book at exchange */
    REPLACED,
    /** Order has been triggered at stop price */
    STOPPED,
    /** Order has been rejected by exchange and not place on order book */
    REJECTED,
    /** Order has expired it's time to live or trading session and been removed from order book */
    EXPIRED,
    /**
     * The exchange returned a state which is not in the exchange's API documentation. The state of
     * the order cannot be confirmed.
     */
    UNKNOWN;

    /** Returns true for final {@link OrderStatus} */
    public boolean isFinal() {
      switch (this) {
        case FILLED:
        case PARTIALLY_CANCELED: // Cancelled, partially-executed order is final status.
        case CANCELED:
        case REPLACED:
        case STOPPED:
        case REJECTED:
        case EXPIRED:
          return true;
        default:
          return false;
      }
    }

    /** Returns true when open {@link OrderStatus} */
    public boolean isOpen() {
      switch (this) {
        case PENDING_NEW:
        case NEW:
        case PARTIALLY_FILLED:
          return true;
        default:
          return false;
      }
    }
  }

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
  public interface IOrderFlags {}

  public abstract static class Builder {

    protected final Set<IOrderFlags> flags = new HashSet<>();
    protected OrderType orderType;
    protected BigDecimal originalAmount;
    protected BigDecimal cumulativeAmount;
    protected BigDecimal remainingAmount;
    protected Instrument instrument;
    protected String id;
    protected String userReference;
    protected Date timestamp;
    protected BigDecimal averagePrice;
    protected OrderStatus status;
    protected BigDecimal fee;
    protected String leverage;

    protected Builder(OrderType orderType, Instrument instrument) {

      this.orderType = orderType;
      this.instrument = instrument;
    }

    @JsonProperty("type")
    public Builder orderType(OrderType orderType) {

      this.orderType = orderType;
      return this;
    }

    @JsonProperty("status")
    public Builder orderStatus(OrderStatus status) {

      this.status = status;
      return this;
    }

    public Builder originalAmount(BigDecimal originalAmount) {

      this.originalAmount = originalAmount;
      return this;
    }

    public Builder cumulativeAmount(BigDecimal cumulativeAmount) {

      this.cumulativeAmount = cumulativeAmount;
      return this;
    }

    public Builder fee(BigDecimal fee) {

      this.fee = fee;
      return this;
    }

    public Builder remainingAmount(BigDecimal remainingAmount) {

      this.remainingAmount = remainingAmount;
      return this;
    }

    public Builder averagePrice(BigDecimal averagePrice) {

      this.averagePrice = averagePrice;
      return this;
    }

    @Deprecated
    public Builder currencyPair(CurrencyPair currencyPair) {

      this.instrument = currencyPair;
      return this;
    }

    public Builder instrument(Instrument instrument) {
      this.instrument = instrument;
      return this;
    }

    public Builder id(String id) {

      this.id = id;
      return this;
    }

    public Builder userReference(String userReference) {

      this.userReference = userReference;
      return this;
    }

    public Builder timestamp(Date timestamp) {

      this.timestamp = timestamp;
      return this;
    }

    public Builder leverage(String leverage) {

      this.leverage = leverage;
      return this;
    }

    @JsonProperty("orderFlags")
    public Builder flags(Set<IOrderFlags> flags) {

      this.flags.addAll(flags);
      return this;
    }

    public Builder flag(IOrderFlags flag) {

      this.flags.add(flag);
      return this;
    }

    public abstract Order build();
  }
}

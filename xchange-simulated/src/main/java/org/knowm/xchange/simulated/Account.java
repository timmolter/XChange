package org.knowm.xchange.simulated;

import static java.math.BigDecimal.ZERO;
import static org.knowm.xchange.dto.Order.OrderType.ASK;

import com.google.common.collect.Collections2;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.FundsExceededException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;

class Account {

  private final ConcurrentMap<Currency, AtomicReference<Balance>> balances =
      new ConcurrentHashMap<>();

  void initialize(Iterable<Currency> currencies) {
    currencies.forEach(
        currency -> balances.put(currency, new AtomicReference<>(new Balance(currency, ZERO))));
  }

  public Collection<Balance> balances() {
    return Collections2.transform(balances.values(), AtomicReference::get);
  }

  public void checkBalance(LimitOrder order) {
    checkBalance(order, order.getOriginalAmount().multiply(order.getLimitPrice()));
  }

  public void checkBalance(Order order, BigDecimal bidAmount) {
    switch (order.getType()) {
      case ASK:
        BigDecimal askAmount = order.getRemainingAmount();
        Balance askBalance =
            balances.computeIfAbsent(order.getCurrencyPair().base, this::defaultBalance).get();
        checkBalance(order, askAmount, askBalance);
        break;
      case BID:
        Balance bidBalance =
            balances.computeIfAbsent(order.getCurrencyPair().counter, this::defaultBalance).get();
        checkBalance(order, bidAmount, bidBalance);
        break;
      default:
        throw new NotAvailableFromExchangeException(
            "Order type " + order.getType() + " not supported");
    }
  }

  private void checkBalance(Order order, BigDecimal amount, Balance balance) {
    if (balance.getAvailable().compareTo(amount) < 0) {
      throw new FundsExceededException(
          "Insufficient balance: "
              + amount.toPlainString()
              + (order.getType() == ASK
                  ? order.getCurrencyPair().base
                  : order.getCurrencyPair().counter)
              + " required but only "
              + balance.getAvailable()
              + " available");
    }
  }

  public void reserve(LimitOrder order) {
    reserve(order, false);
  }

  public void release(LimitOrder order) {
    reserve(order, true);
  }

  private AtomicReference<Balance> defaultBalance(Currency currency) {
    return new AtomicReference<>(new Balance(currency, ZERO));
  }

  private void reserve(LimitOrder order, boolean negate) {
    switch (order.getType()) {
      case ASK:
        BigDecimal askAmount =
            negate ? order.getRemainingAmount().negate() : order.getRemainingAmount();
        balance(order.getCurrencyPair().base)
            .updateAndGet(
                b -> {
                  if (b.getAvailable().compareTo(askAmount) < 0) {
                    throw new ExchangeException(
                        "Insufficient balance: "
                            + askAmount.toPlainString()
                            + order.getCurrencyPair().base
                            + " required but only "
                            + b.getAvailable()
                            + " available");
                  }
                  return Balance.Builder.from(b)
                      .available(b.getAvailable().subtract(askAmount))
                      .frozen(b.getFrozen().add(askAmount))
                      .build();
                });
        break;
      case BID:
        BigDecimal bid = order.getRemainingAmount().multiply(order.getLimitPrice());
        BigDecimal bidAmount = negate ? bid.negate() : bid;
        balance(order.getCurrencyPair().counter)
            .updateAndGet(
                b -> {
                  if (b.getAvailable().compareTo(bidAmount) < 0) {
                    throw new ExchangeException(
                        "Insufficient balance: "
                            + bidAmount.toPlainString()
                            + order.getCurrencyPair().counter
                            + " required but only "
                            + b.getAvailable()
                            + " available");
                  }
                  return Balance.Builder.from(b)
                      .available(b.getAvailable().subtract(bidAmount))
                      .frozen(b.getFrozen().add(bidAmount))
                      .build();
                });
        break;
      default:
        throw new NotAvailableFromExchangeException(
            "Order type " + order.getType() + " not supported");
    }
  }

  public void fill(UserTrade userTrade, boolean reserved) {
    BigDecimal counterAmount = userTrade.getOriginalAmount().multiply(userTrade.getPrice());
    switch (userTrade.getType()) {
      case ASK:
        balance(userTrade.getCurrencyPair().base)
            .updateAndGet(
                b ->
                    Balance.Builder.from(b)
                        .available(
                            reserved
                                ? b.getAvailable()
                                : b.getAvailable().subtract(userTrade.getOriginalAmount()))
                        .frozen(
                            reserved
                                ? b.getFrozen().subtract(userTrade.getOriginalAmount())
                                : b.getFrozen())
                        .total(b.getTotal().subtract(userTrade.getOriginalAmount()))
                        .build());
        balance(userTrade.getCurrencyPair().counter)
            .updateAndGet(
                b ->
                    Balance.Builder.from(b)
                        .total(b.getTotal().add(counterAmount))
                        .available(b.getAvailable().add(counterAmount))
                        .build());
        break;
      case BID:
        balance(userTrade.getCurrencyPair().base)
            .updateAndGet(
                b ->
                    Balance.Builder.from(b)
                        .total(b.getTotal().add(userTrade.getOriginalAmount()))
                        .available(b.getAvailable().add(userTrade.getOriginalAmount()))
                        .build());
        balance(userTrade.getCurrencyPair().counter)
            .updateAndGet(
                b ->
                    Balance.Builder.from(b)
                        .available(
                            reserved ? b.getAvailable() : b.getAvailable().subtract(counterAmount))
                        .frozen(reserved ? b.getFrozen().subtract(counterAmount) : b.getFrozen())
                        .total(b.getTotal().subtract(counterAmount))
                        .build());
        break;
      default:
        throw new NotAvailableFromExchangeException(
            "Order type " + userTrade.getType() + " not supported");
    }
  }

  private AtomicReference<Balance> balance(Currency currency) {
    return balances.computeIfAbsent(currency, this::defaultBalance);
  }

  public void deposit(Currency currency, BigDecimal amount) {
    balance(currency)
        .updateAndGet(
            b ->
                Balance.Builder.from(b)
                    .total(b.getTotal().add(amount))
                    .available(b.getAvailable().add(amount))
                    .build());
  }
}

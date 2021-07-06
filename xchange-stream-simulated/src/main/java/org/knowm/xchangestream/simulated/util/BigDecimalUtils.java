package org.knowm.xchangestream.simulated.util;

import java.math.BigDecimal;
import org.apache.commons.lang3.math.NumberUtils;

/** @author mrmx */
public final class BigDecimalUtils {

  public static BigDecimal toBigDecimal(String value) {
    if (!NumberUtils.isCreatable(value)) {
      illegalNumberArgument(value);
    }
    return NumberUtils.createBigDecimal(value);
  }

  public static BigDecimal toBigDecimal(Number value) {
    if (value == null) {
      illegalNumberArgument(value);
    }
    if (value instanceof BigDecimal) {
      return (BigDecimal) value;
    }
    return BigDecimal.valueOf(value.doubleValue());
  }

  private static void illegalNumberArgument(Object value) {
    throw new IllegalArgumentException("Not a number: " + value);
  }
}

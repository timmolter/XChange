package org.knowm.xchange.poloniex;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/** @author Zach Holmes */
public class PoloniexUtils {

  public static String toPairString(org.knowm.xchange.currency.CurrencyPair currencyPair) {

    return currencyPair.getCounter().getCurrencyCode().toUpperCase()
        + "_"
        + currencyPair.getBase().getCurrencyCode().toUpperCase();
  }

  public static org.knowm.xchange.currency.CurrencyPair toCurrencyPair(String pair) {

    String[] currencies = pair.split("_");
    return org.knowm.xchange.currency.CurrencyPair.build(currencies[1], currencies[0]);
  }

  public static Date stringToDate(String dateString) {

    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
      return sdf.parse(dateString);
    } catch (ParseException e) {
      return new Date(0);
    }
  }

  public static class UnixTimestampDeserializer extends JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      final String dateTimeInUnixFormat = p.getText();
      try {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(Long.parseLong(dateTimeInUnixFormat + "000"));
        return calendar.getTime();
      } catch (Exception e) {
        return new Date(0);
      }
    }
  }
}

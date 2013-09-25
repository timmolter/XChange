/**
 * Copyright (C) 2012 - 2013 Xeiam LLC http://xeiam.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xeiam.xchange.examples.kraken.account;

import org.xchange.kraken.KrakenExchange;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.dto.account.AccountInfo;

/**
 * <p>
 * Example showing the following:
 * </p>
 * <ul>
 * <li>Connect to Kraken exchange with authentication</li>
 * <li>View account balance</li>
 * <li>Get the bitcoin deposit address</li>
 * </ul>
 */
public class KrakenAccountDemo {

  public static void main(String[] args) {
    
    Exchange kraken = ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class.getName());
    ExchangeSpecification specification = kraken.getDefaultExchangeSpecification();
    specification.setApiKey("rOkckzK+auTaBmwjbN1NPkLr6W0RcCo0ckpdylNavNyR+ZRv/RyDil4K");
    specification.setSecretKey("GTU3yVFB22zeWsN/sAUfmN3PgKU2lyces2IVuc7Ay0o1Qb9imFycboXYMwhzsq7YICJO5O9UkyZyUBkye4g5sA==");
    specification.setUserName("XChange");
    kraken.applySpecification(specification);
    AccountInfo accountInfo =kraken.getPollingAccountService().getAccountInfo();
    System.out.println("AccountInfo as String: " + accountInfo.toString());

  }
}

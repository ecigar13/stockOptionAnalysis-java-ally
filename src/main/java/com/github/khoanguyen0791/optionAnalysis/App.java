package com.github.khoanguyen0791.optionAnalysis;

import java.util.ArrayList;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.fixprotocol.fixml.QuoteRequestMessageT;
import org.fixprotocol.fixml.RestructuringTypeEnumT;

import com.celexus.conniption.api.TradeKing;
import com.celexus.conniption.foreman.TradeKingForeman;
import com.celexus.conniption.foreman.stream.StreamHandler;
import com.celexus.conniption.model.accounts.AccountsResponse;
import com.celexus.conniption.model.clock.ClockResponse;
import com.celexus.conniption.model.quotes.Quote;
import com.celexus.conniption.model.quotes.QuotesResponse;

import io.netty.handler.codec.http.HttpContentEncoder.Result;

/**
 * Hello world!
 *
 */
public class App {
  public static void main(String[] args) throws InterruptedException, IOException {
    String[] symbols = { "gnw" };
    Arrays.sort(symbols);
    String optionSymbol = "gnw";
    List<String[]> queries = new ArrayList<String[]>();
    queries.add(new String[] { "strikeprice", "<", "130" });
    queries.add(new String[] { "put_call", "eq", "call" });
    // String[] fids = { "symbol", "ask", "bid", "xdate", "xday", "xmonth", "xyear"
    // };
    String[] fids = null;
    TradeKing tk = new TradeKing(new TradeKingForeman());

    // get the market clock
    // ClockResponse c = tk.clock();

    // options search is more complex because of query

    // option search use case
    QuotesResponse quotesResponse = tk.options(optionSymbol, queries, fids);

    // while (true) {
    QuotesResponse quotes = tk.quotes(symbols);
    List<Quote> q = quotes.getQuotes().getQuote();
    for (Quote qt : q) {
      System.out.println(qt.getSymbol() + "\t" + qt.getBid() + "\t" + qt.getAsk());
    }

    List<Quote> list = quotesResponse.getQuotes().getQuote();
    list.sort(new Comparator<Quote>() {

      public int compare(Quote o1, Quote o2) {
        int result = 0;
        result = Double.valueOf(o1.getXyear()).compareTo(Double.valueOf(o2.getXyear()));
        if (result != 0)
          return result;
        result = Double.valueOf(o1.getXmonth()).compareTo(Double.valueOf(o2.getXmonth()));
        if (result != 0)
          return result;

        result = Double.valueOf(o1.getXday()).compareTo(Double.valueOf(o2.getXday()));
        if (result != 0)
          return result;

        result = Double.valueOf(o1.getStrikeprice()).compareTo(Double.valueOf(o2.getStrikeprice()));
        return result;
      }

    });
    // double endPrice = 127.5;
    double endPrice = 5.41; // 5.43 but price never gets there.
    for (Quote quote : list) {
      double breakeven = quote.getStrikeprice() + quote.getAsk();
      double profitMultiple = (endPrice - breakeven) / quote.getAsk() * 100;
      if (profitMultiple > 0.0) {

        System.out.println(quote.getAsk() + "\t" + quote.getStrikeprice() + "\t" + profitMultiple + "\t" + breakeven
            + "\t" + "\t" + quote.getXday() + "\t" + quote.getXmonth() + "\t" + quote.getXyear());
      }
    }

    System.out.println("NXPI: ");
    optionSymbol = "nxpi";
    quotesResponse = tk.options(optionSymbol, queries, fids);
    list = quotesResponse.getQuotes().getQuote();
    endPrice = 127.5;
    for (Quote quote : list) {
      double breakeven = quote.getStrikeprice() + quote.getAsk();
      double profitMultiple = (endPrice - breakeven) / quote.getAsk() * 100;
      if (profitMultiple > 0.0) {

        System.out.println(quote.getAsk() + "\t" + quote.getStrikeprice() + "\t" + profitMultiple + "\t" + breakeven
            + "\t" + "\t" + quote.getXday() + "\t" + quote.getXmonth() + "\t" + quote.getXyear());
      }
    }

    System.out.println("\n\nUVXY: ");
    optionSymbol = "UVXY";
    queries.remove(queries.size() - 1);
    queries.add(new String[] { "put_call", "eq", "put" });
    quotesResponse = tk.options(optionSymbol, queries, fids);
    list = quotesResponse.getQuotes().getQuote();
    endPrice = 9.86;
    for (Quote quote : list) {
      double breakeven = quote.getStrikeprice() - quote.getAsk();
      double profitMultiple = Math.abs((endPrice - breakeven)) / quote.getAsk() * 100;
      if (breakeven > 2.5) {

        System.out.println(
            quote.getAsk() + "\t" + String.format("%.2f", breakeven) +"\t"+ quote.getStrikeprice()+ "\t" + String.format("%.2f", profitMultiple)
                + "\t" + quote.getXday() + " " + quote.getXmonth() + " " + quote.getXyear());
      }
    }
    String[] sym = { "T" };
    quotes = tk.quotes(sym);
    q = quotes.getQuotes().getQuote();
    for (Quote qt : q) {
      System.out.println(qt.getSymbol() + "\t" + qt.getAsk());
      if (qt.getAsk() < 37.411) {

        System.out.println(53.75 + 1.437 * qt.getAsk());
      } else if (qt.getAsk() > 41.349) {
        System.out.println(53.75 + 1.3 * qt.getAsk());
      } else
        System.out.println(53.75 * 2);
    }

    // TimeUnit.SECONDS.sleep(15);
    // }

    // get your account
    // AccountsResponse a = tk.accounts();
    // stream market quotes
    //
    // Future f = tk.streamQuotes(new StreamHandler<Quote>() {
    // public void handle(Quote quote) {
    // System.out.println(quote.toString());
    // }
    // }, "TWTR", "FB");

  }
}

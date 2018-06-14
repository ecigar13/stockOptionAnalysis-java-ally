package com.github.khoanguyen0791.optionAnalysis;

import java.util.ArrayList;
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
  public static void main(String[] args) {
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
    QuotesResponse quotes = tk.quotes(symbols);
    List<Quote> q = quotes.getQuotes().getQuote();
    for(Quote qt: q) {
      System.out.println(qt.getSymbol() + "\t"+qt.getAsk());
    }

    // get the market clock
    // ClockResponse c = tk.clock();

    // options search is more complex because of query

    // option search use case
    QuotesResponse quotesResponse = tk.options(optionSymbol, queries, fids);
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
    double endPrice = 5.43;
    for (Quote quote : list) {
      double breakeven = quote.getStrikeprice() + quote.getAsk();
      double profitPercent = (endPrice - breakeven) / quote.getAsk() * 100;
      if (profitPercent > 0.0 && quote.getXmonth() == 7.0 && quote.getXday() == 27.0) {

        System.out.println(quote.getAsk() + "\t" + quote.getStrikeprice() + "\t" + profitPercent + "\t" + quote.getBid()
            + "\t" + quote.getAsk() + "\t" + quote.getXday() + "\t" + quote.getXmonth() + "\t" + quote.getXyear());
      }
    }

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

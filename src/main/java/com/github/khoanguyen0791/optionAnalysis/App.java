package com.github.khoanguyen0791.optionAnalysis;

import java.util.ArrayList;
import java.io.IOException;
import java.sql.Time;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.celexus.conniption.api.TradeKing;
import com.celexus.conniption.foreman.TradeKingForeman;
import com.celexus.conniption.foreman.stream.StreamHandler;
import com.celexus.conniption.model.accounts.AccountsResponse;
import com.celexus.conniption.model.clock.ClockResponse;
import com.celexus.conniption.model.quotes.Quote;
import com.celexus.conniption.model.quotes.QuotesResponse;
import com.github.scribejava.apis.TheThingsNetworkV1StagingApi;

import io.netty.handler.codec.http.HttpContentEncoder.Result;

/**
 * Hello world!
 *
 */
public class App {
  public static void main(String[] args) throws InterruptedException, IOException {

    // }

    // get your account
    // AccountsResponse a = tk.accounts();
    // stream market quotes

    TradeKing tk = new TradeKing(new TradeKingForeman());
    Future f = tk.streamQuotes(new StreamHandler<Quote>() {
      public void handle(Quote quote) {
        if (quote.getBid() != 0.0) {
          System.out.println(quote.getSymbol() + "\t" + quote.getBid() + "\t" + quote.getAsk());
        }
      }
    }, "F", "GM");

    String[] quoteSymbols = { "F", "GM" };
    String optionSymbol = "MON";
    List<String[]> queries = new ArrayList<String[]>();
    // queries.add(new String[] { "strikeprice", "<", "130" });
    queries.add(new String[] { "put_call", "eq", "call" });
    while (true) {
      Comparator<Quote> comparator = new resultComparator();
      // getPutOption(optionSymbol, queries, null, 12, comparator);
      // getCallOption(optionSymbol, queries, null, 125, comparator);
      getQuote(quoteSymbols, null, comparator);
      System.out.println(Instant.now());
      TimeUnit.SECONDS.sleep(15);
    }

  }

  /**
   * Implement abstraction of getQuote so I can pass any comparator, symbols and
   * fids.
   * 
   * @param symbols
   *          String array of symbols
   * @param fids
   *          string arrays of fids to get (not used very often)
   * @param comparator
   *          a customized comparator.
   */
  public static void getQuote(String[] symbols, String[] fids, Comparator<Quote> comparator) {
    Arrays.sort(symbols);
    TradeKing tk = new TradeKing(new TradeKingForeman());
    QuotesResponse quotesResponse = tk.quotes(symbols, fids);
    List<Quote> list = quotesResponse.getQuotes().getQuote();

    for (Quote qt : list) {
      System.out.println(qt.getSymbol() + "\t" + qt.getBid() + "\t" + qt.getAsk() + "\t" + qt.getVl());
    }
  }

  /**
   * Implement abstraction of getOption so I can pass any comparator, query,
   * symbol and fidsand calculate profit.
   * 
   * @param symbol
   *          one single symbol.
   * @param queries
   *          List of string array of queries.
   * @param fids
   *          fields to return
   * @param targetPrice
   *          price to calculate profit. Using the wrong price will give the wrong
   *          profit multiple.
   * @param comparator
   *          a customized comparator to sort results.
   */
  public static void getCallOption(String symbol, List<String[]> queries, String[] fids, double targetPrice,
      Comparator<Quote> comparator) {
    TradeKing tk = new TradeKing(new TradeKingForeman());
    QuotesResponse optionResponse = tk.options(symbol, queries, fids);
    List<Quote> list = optionResponse.getQuotes().getQuote();
    list.sort(comparator);

    int curDay = 0;
    double maxMultiple = 0.0;
    for (Quote qt : list) {
      if (curDay != qt.getXday()) {
        System.out.println("");
      }
      curDay = qt.getXday();

      double profit = targetPrice - qt.getStrikeprice() - qt.getAsk();
      double profitMultiple = profit / qt.getAsk();

      if (maxMultiple < profitMultiple) {
        maxMultiple = profitMultiple;
      }
      if (profitMultiple > 0.0) {

        System.out.println(String.format("%6.2f", profitMultiple) + "\t" + String.format("%6.2f", qt.getAsk()) + "\t"
            + qt.getBid() + "\t" + qt.getXday() + " " + qt.getXmonth() + " " + qt.getXyear() + "  "
            + String.format("%6.2f", qt.getStrikeprice()) + "  " + String.format("%8.2f", qt.getOpeninterest())
            + String.format("%4d", qt.getAsksz()) + String.format("%4d", qt.getBidsz())
            + String.format("%3d", qt.getIncrVl()) + String.format("%10d", qt.getVl()));
      }

    }
    System.out.println("Max call multiple: " + String.format("%6.2f", maxMultiple));

  }

  public static void getPutOption(String symbol, List<String[]> queries, String[] fids, double targetPrice,
      Comparator<Quote> comparator) {
    TradeKing tk = new TradeKing(new TradeKingForeman());
    QuotesResponse optionResponse = tk.options(symbol, queries, fids);
    List<Quote> list = optionResponse.getQuotes().getQuote();
    list.sort(comparator);

    int curDay = 0;
    double maxMultiple = 0.0;
    for (Quote qt : list) {
      if (curDay != qt.getXday())
        System.out.println("");
      curDay = qt.getXday();

      double profit = qt.getStrikeprice() - targetPrice - qt.getAsk();
      double profitMultiple = profit / qt.getAsk();

      if (maxMultiple < profitMultiple) {
        maxMultiple = profitMultiple;
      }
      if (profitMultiple > 5.0) {

        System.out.println(String.format("%6.2f", profitMultiple) + "\t" + String.format("%6.2f", qt.getAsk()) + "\t"
            + qt.getBid() + "\t" + qt.getXday() + " " + qt.getXmonth() + " " + qt.getXyear() + "  "
            + String.format("%6.2f", qt.getStrikeprice()) + "  " + String.format("%8.2f", qt.getOpeninterest())
            + String.format("%4d", qt.getAsksz()) + String.format("%4d", qt.getBidsz())
            + String.format("%3d", qt.getIncrVl()) + String.format("%10d", qt.getVl()));
      }

    }
    System.out.println("Max put multiple: " + String.format("%6.2f", maxMultiple));

  }

  /**
   * Comparator class to sort result of all options and quotes responses.
   * 
   * @author khoa
   *
   */
  static class resultComparator implements Comparator<Quote> {
    @Override
    public int compare(Quote o1, Quote o2) {
      int result = 0;
      result = Integer.valueOf(o2.getXyear()).compareTo(Integer.valueOf(o1.getXyear()));
      if (result != 0)
        return result;
      result = Integer.valueOf(o2.getXmonth()).compareTo(Integer.valueOf(o1.getXmonth()));
      if (result != 0)
        return result;

      result = Integer.valueOf(o2.getXday()).compareTo(Integer.valueOf(o1.getXday()));
      if (result != 0)
        return result;

      result = Double.valueOf(o2.getStrikeprice()).compareTo(Double.valueOf(o1.getStrikeprice()));
      return result;
    }

  }

  /**
   * Format the array of objects so it looks better when printed.
   * 
   * @param objectList
   * @return List of strings already formatted.
   */
  public static List<String> toStringArray(List<Object> objectList) {
    List<String> stringList = new ArrayList<>();
    for (Object q : objectList) {
      stringList.add(String.format("%6.2f", q));
    }
    return stringList;
  }
}

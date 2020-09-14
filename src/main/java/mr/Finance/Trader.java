package mr.Finance;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

//Papers 

// https://jpm.pm-research.com/content/38/1/110.short
// https://www.tandfonline.com/doi/abs/10.2469/faj.v57.n3.2449
//https://www.sciencedirect.com/science/article/abs/pii/S1568494610000621
//https://www.sciencedirect.com/science/article/abs/pii/S1386418199000129

public class Trader {

//Account Manager Instance
	AccountManager am = null;

//Trader Name
	String trader = "mrfinance";
	List<HistoricalData> data = new ArrayList<>();

//Constructor		
	public Trader(AccountManager am) {

		this.am = am;
	}

	// Class Ticker with Prices

	class HistoricalData {
		String ticker;
		List<Double> adjustedPrices = new ArrayList<>();

		public HistoricalData(String ticker) {

			DownloadData d = new DownloadData();
			this.ticker = ticker;
			this.adjustedPrices = d.stock.getHistorical(ticker);

		}

		double getTotalReturn() {

			return adjustedPrices.get(adjustedPrices.size() - 1) / adjustedPrices.get(0) - 1;

		}

		double[] getReturns() {

			List<Double> returns = new ArrayList<>();

			Double previous = adjustedPrices.get(0);

			for (int i = 1; i < adjustedPrices.size(); i++) {

				returns.add(Math.log(previous / adjustedPrices.get(i)));
			}

			Double[] data = returns.toArray(new Double[returns.size()]);

			return ArrayUtils.toPrimitive(data);
		}

	}

	// Data List

	// Open stocks from List

	List<HistoricalData> openStocks() {
		List<HistoricalData> data = new ArrayList<>();
		try {
			FileInputStream fstream = new FileInputStream("./trade.lst");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			while ((strLine = br.readLine()) != null) {

				data.add(new HistoricalData(strLine));
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return data;
	}

	// Co-Variance Analysis get top most uncorrelated stocks with the SP500

	List<HistoricalData> marketCorrelation(List<HistoricalData> data, Double threshold) {

		List<HistoricalData> newData = new ArrayList<>();
		PearsonsCorrelation p = new PearsonsCorrelation();
		HistoricalData SP = new HistoricalData("spy");

		for (HistoricalData stock : data) {

			try {
				Double corr = p.correlation(SP.getReturns(), stock.getReturns());

				System.out.println("Correlation: " + stock.ticker + " : " + corr);

				if (corr < threshold) {

					newData.add(stock);

				}
			} catch (Exception e) {

				e.printStackTrace();
			}

		}

		return newData;

	}

	// Get total Return per period

	List<HistoricalData> getTotalReturn(List<HistoricalData> data, Double threshold) {

		List<HistoricalData> newData = new ArrayList<>();

		for (HistoricalData stock : data) {

			Double totalReturn = stock.getTotalReturn();

			if (totalReturn > threshold) {
				System.out.println("Total Return: " + stock.ticker + " : " + totalReturn);
				newData.add(stock);

			}
		}

		return newData;

	}

	// Search Trades
	void searchTrades() {
		
		System.out.println("Start Search");

		data = getTotalReturn(openStocks(), 0.015);

	}
	
	

	// Open top 10 picks

	void openTrades() {

		// Instance of Download Data

		DownloadData d = new DownloadData();

		
	
		// Allocate Balance
		Double allocation = am.getCashBalance(trader) / data.size();
		System.out.println("System Allocation : " + allocation);
		// Go thru list and open trades.
		for (HistoricalData stock : data) {
			try {
				Double price = (Double) d.stock.getPrice((String) stock.ticker)[0];

				Integer number = (int) Math.round(allocation / price);

				am.openPosition(trader, number, (String) stock.ticker, "equity", "long");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// Close all Trades
	void closeTrades() {

		// Get List Of Open Trades
		List<ImmutablePair<String, Integer>> pairs = am.getListOfOpenTrades(trader);

		// Print List of Pairs To Close

		for (ImmutablePair<String, Integer> pair : pairs) {

			System.out.println("Closing " + (int) pair.getRight() + " " + (String) pair.getLeft() + "....");

		}

		// Close all trades one by one

		for (ImmutablePair<String, Integer> pair : pairs) {

			am.closePosition(trader, (int) pair.getRight(), (String) pair.getLeft(), "equity", "long");

		}

	}

	// Boot Strap
	/*
	 * void bootstrap() { am.addNewAccount(trader, 100000.0); am.openBond(trader,
	 * 1000);
	 * 
	 * }
	 */

	// Set Timer

	void startTradeTimers() {

		// Create timer Deamons for open and close trades.
		
		

		TimeDeamon search = new TimeDeamon();
		TimeDeamon open = new TimeDeamon();
		TimeDeamon close = new TimeDeamon();

		// Set Timers
		Calendar searchTime = Calendar.getInstance();
		Calendar openTime = Calendar.getInstance();
		Calendar closeTime = Calendar.getInstance();

		// Search Time

		String searchTimeString = "2000-09-01 05:40:00";

		// Open Time

		String openTimeString = "2000-09-01 09:00:00";

		// Close time

		String closeTimeString = "2000-09-01 15:15:00";

		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		try {

			searchTime.setTime(format1.parse(searchTimeString));
			openTime.setTime(format1.parse(openTimeString));
			closeTime.setTime(format1.parse(closeTimeString));

		} catch (Exception e) {

			e.printStackTrace();
		}

		// Set Search Trade Timer

		search.DailyRunnerDaemon(searchTime, new Runnable() {

			@Override
			public void run() {
				
				searchTrades();
			}

		}, "Searching in the Morning");
		// Set Open Trade Timer

		open.DailyRunnerDaemon(openTime, new Runnable() {

			@Override
			public void run() {
				openTrades();
			}

		}, "Opening in the Morning");

		// Set Close Trade Timer

		close.DailyRunnerDaemon(closeTime, new Runnable() {

			@Override
			public void run() {
				closeTrades();
			}

		}, "Closing for the day");

		// Start timers.
		search.start();
		open.start();
		close.start();

	}

}

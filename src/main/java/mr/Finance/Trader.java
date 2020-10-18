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

public class Trader extends Thread {

//Account Manager Instance
	AccountManager am = null;

//Trader Name
	String trader = "mrfinance";

	List<HistoricalData> data = new ArrayList<>();
	List<HistoricalData> tradeList = new ArrayList<>();

	Long milliseconds = 0L;

//Constructor		
	public Trader(AccountManager am, Long milliseconds) {
		this.milliseconds = milliseconds;
		this.am = am;
	}

	// Class Ticker with Prices

	class HistoricalData {

		String ticker;
		List<Double> adjustedPrices = new ArrayList<>();
		DownloadData d = new DownloadData();
		Double wins = 0.0;
		Double plays = 0.0;

		public HistoricalData(String ticker) {

			this.ticker = ticker;
			Double price = (Double) d.stock.getPrice(ticker)[0];
			this.adjustedPrices.add(price);
			this.adjustedPrices.add(price);

		}

		void increaseWins() {

			wins += 1.0;
		}

		void increasePlays() {

			plays += 1.0;

		}
		
		Double getWinRate() {
			
			return wins/plays;
			
		}

		void pushData() {
			// Copy Last
			
			Double price = adjustedPrices.get(adjustedPrices.size() - 1);
			adjustedPrices.clear();
			
			adjustedPrices.add(price);
			adjustedPrices.add((Double) d.stock.getPrice(ticker)[0]);
			
		}

		double getTotalReturn() {

			System.out.println(adjustedPrices.get(adjustedPrices.size() - 1) + " " + adjustedPrices.get(0) + " "
					+ adjustedPrices.size());

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

	public void updateStocks() {

		for (HistoricalData dat : data) {

			dat.pushData();

		}

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
				stock.increasePlays();
				newData.add(stock);

			}
		}

		return newData;

	}

	// Search Trades
	void searchTrades() {

		System.out.println("Start Search");

		updateStocks();
		tradeList.clear();
		tradeList = getTotalReturn(data, 0.000);
		System.out.println(data.size());

	}

	// Open top 10 picks

	void openTrades() {

		// Instance of Download Data

		DownloadData d = new DownloadData();

		if (tradeList.size() > 0) {
			// Allocate Balance
			Double allocation = am.getCashBalance(trader) / tradeList.size();
			System.out.println("System Allocation : " + allocation);
			// Go thru list and open trades.
			for (HistoricalData stock : tradeList) {
				try {
					Double price = (Double) d.stock.getPrice((String) stock.ticker)[0];

					Integer number = (int) (Math.round(allocation / price) * .8);

					am.openPosition(trader, number, (String) stock.ticker, "equity", "long");
				} catch (Exception e) {
					e.printStackTrace();
				}
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
		
		//Add to List
		for(HistoricalData stock: data){
			
			DownloadData d = new DownloadData();
			
			for (ImmutablePair<String, Integer> pair : pairs) {
			
				if(((String) pair.getLeft()).equals(stock.ticker)) {
					
					if(stock.adjustedPrices.get(1)<(Double) d.stock.getPrice(stock.ticker)[0]) {
						
						stock.increaseWins();
					}
				}
				
				
				
			}
			
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

		data.clear();
		data = openStocks();

		this.start();

	}

	public void run() {
		int i = 0;
		while (true) {
			if (i != 0) {
				searchTrades();
				openTrades();
			} else {
				searchTrades();
				closeTrades();
				i++;

			}

			System.out.println("Trading...");
			try {
				Thread.sleep(1000 * 60 * 15);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			closeTrades();
			
			
			for(HistoricalData stock: data) {
				
				System.out.println(stock.ticker+" wins:  "+stock.wins+"  played:"+stock.plays+" winrate:"+ stock.getWinRate());
				
			}

		}

	}

}

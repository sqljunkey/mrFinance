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
//
// None as of Yet


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


	// Open stocks from List

	List<HistoricalData> loadStockList() {

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




	// Search Trades
	void searchTrades() {

		System.out.println("Start Search");

		Filters f = new Filters();
		
		updateStocks();
		tradeList = f.sortByReturn(data);
		

	}	
	
	public void updateStocks() {

		for (HistoricalData dat : data) {

			dat.pushData();

		}

	}


	// Open top 10 picks

	void openTrades() {

		// Instance of Download Data

		DownloadData d = new DownloadData();
		
		Double money = am.getCashBalance(trader);
		int topValue = 2;
		Double allocationQuantity = money/topValue*.8;

		
		
		//Open top 10
		
		
		if(tradeList.size()> topValue)
		for(int i = 0; i < topValue; i++) {
			System.out.println(tradeList.get(i).getPercentageChange());
			Double quantity = allocationQuantity/ (Double) d.stock.getPrice(tradeList.get(i).ticker)[0];

			am.openPosition(trader, quantity.intValue(), tradeList.get(i).ticker, "equity", "short");
		}
	
		
		
		

	}

	// Close all Trades
	void closeTrades() {
		List<String> portfolio = am.getPortfolio(trader);
	

		// Get List Of Open Trades
		List<Record> records = am.getListOfOpenTrades(trader);

		// Print List of Pairs To Close

		for (Record record : records) {

			System.out.println("Closing " + record.getUnits() + " " + record.getTicker() + "....");

		}

		// Add to List
		for (HistoricalData stock : data) {

			DownloadData d = new DownloadData();

			if (stock.getStatus()) {

				if (stock.adjustedPrices.get(1) < (Double) d.stock.getPrice(stock.ticker)[0]) {

					stock.addScore(1.0);
				} else {
					stock.addScore(0.0);
				}

				stock.decativate();
			}

		}

		// Close all trades one by one

		for (Record record : records) {

			am.closePosition(trader, record.getUnits().intValue(), record.getTicker(), "equity", record.getType());

		}

	}


	void startTradeTimers() {

		data.clear();
		data = loadStockList();

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
				Thread.sleep(1000 * 60 * 60);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			closeTrades();

			for (HistoricalData stock : data) {

				Double score = stock.scores.stream().mapToDouble(f -> f.doubleValue()).sum();

				System.out.println(stock.ticker + " wins:  " + score + "  played:" + stock.scores.size() + " winrate:"
						+ stock.getWinRate());

			}

		}

	}

}

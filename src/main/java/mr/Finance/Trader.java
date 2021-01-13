package mr.Finance;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;
import java.util.TimeZone;



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



	// Open top 10 picks

	void openTrades() {

		// Instance of Download Data
		WinRate winRates = new WinRate();
		List<WinrateRecord> records = winRates.getWinRates();
		int topValue = 0;

		for (WinrateRecord record : records) {

			
				if (record.getWinrate() > .69) {
					topValue++;
				}
		

		}

		DownloadData d = new DownloadData();

		Double money = am.getCashBalance(trader);

		Double allocationQuantity = money / topValue * .8;

		// Open top 10

		for (WinrateRecord record : records) {

			if (record.getWinrate() > .69) {

				Double quantity = allocationQuantity / (Double) d.stock.getPrice(record.getTickerName())[0];

				am.openPosition(trader, quantity.intValue(), record.getTickerName(), "equity", "long");
			}
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

	

		this.start();

	}

	public void run() {
		closeTrades();
		Boolean trading = false; 
		
		while (true) {

			
			try {

				// Check every 15 minutes
				Thread.sleep(1000 * 60 * 15);
				Calendar date = Calendar.getInstance(TimeZone.getTimeZone("America/Chicago"));
				System.out.println("Hour of Day "+ date.get(Calendar.HOUR_OF_DAY) );
				
				if (date.get(Calendar.HOUR_OF_DAY) == 15 && !trading) {
					closeTrades();
					openTrades();
					trading =true;
					System.out.println("Trading...");

				}else if(date.get(Calendar.HOUR_OF_DAY) != 15 && trading){
					
					trading = false;
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}

package mr.Finance;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Trader extends Thread {

	AccountManager am = null;

	String trader = "mrfinance";
	Boolean tradingLock = false;
	String list = "";
	Double threshold = 0.0;
	Clock clock = new Clock();
    String directory = "/home/junkey/website/";
    int sleepTime= 1000 * 60 * 20;
    
	DecimalFormat twoDecimal = new DecimalFormat("###,###,##0.00");

	public void setDirectory(String directory) {
		
		this.directory = directory; 
	}
	
	public void setSleepTime(int sleepTime) {
		
		this.sleepTime=sleepTime;
	}
	public void setInformation(Double threshold, String list) {
		this.threshold = threshold;
		this.list = list;

	}

	public Trader(AccountManager am) {

		this.am = am;
		setInformation(10.0, "nasdaqShort.lst");
	}

	public void setClock(int begin, int closing, int workWeek) {
		
		clock.setBeginHour(begin);
		clock.setClosingHour(closing);
		clock.setWorkWeek(workWeek);
	} 
	
	
public List<String> createMessageList(List<Record> trades) {

		List<String> result = new ArrayList<>();

		for (Record r : trades) {

			Double returnValue = (r.getDifference() - 1.0);

			result.add(r.getTicker() + "  " + twoDecimal.format(returnValue * 100.0) + "% ");

		}

		return result;

	}

	public String getAverageReturn(List<Record> trades) {

		String result = "";
		Double average = 0.0;

		for (Record r : trades) {

			Double returnValue = (r.getDifference() - 1.0);

			average += returnValue;

		}

		average /= trades.size();

		if (average.isNaN()) {
			average = 0.0;
		}

		result = "Average return: " + twoDecimal.format((average * 100)) + "%";

		return result;

	}

	public Double getTotalValue(List<Record> trades) {

		Double result = 0.0;

		for (Record r : trades) {

			result += r.cost * (r.getDifference());

		}

		return result;

	}

	public void updateSite(String actionTitle) {

		Index index = new Index();
		String link = "bot.html";
		Double money = am.getCashBalance(trader);

		List<Record> trades = am.getListOfOpenTrades(trader);

		if (trades.isEmpty()) {

			List<Record> records = loadSignals();

			for (Record record : records) {

				trades.add(record);
			}

			records.clear();

		}

		List<String> message = createMessageList(trades);
		String secondMessage = getAverageReturn(trades);
		Double value = getTotalValue(trades);

		index.store((value + money), message, link, actionTitle, secondMessage);

	}

	public void resetOrders() {

		closeAllTrades();
		updateSite("Reloading...");

		openTrades(getSignals());

		updateSite("Out of battery");

	}

	public void closeAllTrades() {

		List<Record> records = am.getListOfOpenTrades(trader);
       
		for (Record record : records) {

			am.closePosition(trader, record.units.intValue(), record.getTicker(), record.exchange, record.type);

		}

	}

	public Double getAllocation(int size) {

		return am.getCashBalance(trader) * (1.0 / (double) size) * .95;
	}

	public void openTrades(List<Record> records) {

		DownloadData d = new DownloadData();

		if (records.isEmpty()) {

			return;
		}

		for (Record record : records) {

			Double price = (Double) d.stock.getPrice(record.getTicker())[0];

			Double ratio = (Double) (getAllocation(records.size())) / price;

			if (price != 0.0) {

				am.openPosition(trader, ratio.intValue(), (String) record.getTicker(), "equity", "short",
						record.elapsedTime, 0.0);

			}

		}

	}

	public List<Record> getSignals() {
		Signal s = new Signal();
		List<Record> result = s.getPositiveVolatility(list, threshold);

		return result;

	}

	public void deleteStoredSignals() {

		Util.deleteFile("trade.lst");

	}

	public void storeSignals(List<Record> records) {

		for (Record record : records) {
			
			Util.writeToFile("trade.lst", record.getTicker());
		}

	}

	public List<Record> loadSignals() {

		List<String> tickers = Util.getStockListFromFile("trade.lst");
		List<Record> result = new ArrayList<>();

		for (String ticker : tickers) {

			Record record = new Record();
			record.setTicker(ticker);

			result.add(record);
		}

		return result;
	}

	public void startTradeTimers() {

		this.start();

	}

	public Boolean isTradingLocked() {

		return tradingLock;

	}

	public void setTradingLock() {

		tradingLock = true;
	}

	public void unlockTradingLock() {

		tradingLock = false;
	}

	public void sleepForTwentyMinutes() {
		long twentyMinutes = sleepTime;

		try {

			sleep(twentyMinutes);

		} catch (Exception e) {

		}

	}

	public void trade() {
		
		try {
		
				clock.printTime();

				if (clock.isUpdateTime()) {

					updateSite("In battery");

				}

				if (clock.isUnlockTime() && isTradingLocked()) {

					unlockTradingLock();

				}

				if (clock.isClosingTime()&&!isTradingLocked()) {

					
					closeAllTrades();
					updateSite("Reloading...");

					openTrades(getSignals());
					

					updateSite("Out of battery");
					setTradingLock();

				}

				sleepForTwentyMinutes();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {

		while (true) {

			try {

				trade();

			} catch (Exception e) {

				e.printStackTrace();
			}

		}

	}

}

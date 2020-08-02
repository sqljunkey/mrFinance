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

import org.apache.commons.lang3.tuple.ImmutablePair;

//Papers 

// https://jpm.pm-research.com/content/38/1/110.short
// https://www.tandfonline.com/doi/abs/10.2469/faj.v57.n3.2449


public class Trader {

//Account Manager Instance
	AccountManager am = null;

//Trader Name
	String trader = "mrfinance";

//Constructor		
	public Trader(AccountManager am) {

		this.am = am;
	}

//// Load List of stocks and check all of them against RSS Feed for news and
	//// return a list from most RSS Feeds to last.

	private List<ImmutablePair<Integer, String>> getNews() {

		// Create Data Downloader
		DownloadData d = new DownloadData();

		// Load List of stocks from file

		List<String> ticker = new ArrayList<>();
		try {
			FileInputStream fstream = new FileInputStream("./sp500.lst");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			while ((strLine = br.readLine()) != null) {

				ticker.add(strLine);
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();

		}

		// Get all head lines for each ticket
		List<ImmutablePair<Integer, String>> pairs = new ArrayList<ImmutablePair<Integer, String>>();
		for (String tick : ticker) {
			
			System.out.print("Getting News "+tick+ ".....");
		
			Integer size = d.stock.getHeadline(tick).size();


			System.out.println(size+ " articles found.");
			
			pairs.add(new ImmutablePair<>(size, tick));

		}

		// Sort Pair

		pairs.sort(new Comparator<ImmutablePair>() {
			@Override
			public int compare(ImmutablePair p1, ImmutablePair p2) {

				if ((int) p1.getLeft() > (int) p2.getLeft()) {

					return -1;
				} else if ((int) p2.getLeft() > (int) p1.getLeft()) {

					return 1;
				}

				return 0;

			}
		});

		// Print Pair

		for (ImmutablePair pair : pairs) {

			System.out.println((int) pair.getLeft() + " " + (String) pair.getRight());

		}

		// Return Pair

		return pairs;

	}

	// Open top 10 picks

	void openTrades() {

		// Instance of Download Data

		DownloadData d = new DownloadData();

		// Open List
		List<ImmutablePair<Integer, String>> pairs = getNews();

		// Allocate Balance
		Double allocation = am.getCashBalance(trader) / 20;
		// Go thru list and open trades.
		for (int i = 0; i < 20; i++) {

			ImmutablePair<Integer, String> pair = pairs.get(i);

			Double price = (Double) d.stock.getPrice((String) pair.getRight())[0];

			Integer number = (int) Math.round(allocation / price);

			am.openPosition(trader, number, (String) pair.getRight(), "equity", "short");

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

			am.closePosition(trader, (int) pair.getRight(), (String) pair.getLeft(), "equity", "short");

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

		TimeDeamon open = new TimeDeamon();
		TimeDeamon close = new TimeDeamon();

		// Set Timers
		Calendar openTime = Calendar.getInstance();
		Calendar closeTime = Calendar.getInstance();

		// Open Time

		String openTimeString = "2000-09-01 09:30:00";

		// Close time

		String closeTimeString = "2000-09-01 14:45:00";

		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		try {

			openTime.setTime(format1.parse(openTimeString));
			closeTime.setTime(format1.parse(closeTimeString));

		} catch (Exception e) {

			e.printStackTrace();
		}

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
		
		
		//Start timers.
		
		open.start();
		close.start();

	}

}

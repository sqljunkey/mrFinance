package mr.Finance;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;

public class WinRate {

	List<String> stockTickers = new ArrayList<>();

	public void loadStockTickers() {
		Scanner scanner;
		try {
			scanner = new Scanner(new FileReader("stock.lst"));

			while (scanner.hasNext()) {
				String str = scanner.next();

				stockTickers.add(str);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	WinrateRecord getWinRate(String ticker) {

		Calendar to = new GregorianCalendar();
		Calendar from = Calendar.getInstance(TimeZone.getTimeZone("America/Chicago"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		dateFormat.setTimeZone(to.getTimeZone());

		//System.out.println(from.toString());
		//System.out.println(to.toString());
		try {
			to.add(Calendar.MONTH, -1);
			//to.setTime(dateFormat.parse("01-12-2020"));
			//from.setTime(dateFormat.parse("02-01-2021"));
		} catch (Exception e) {

			e.printStackTrace();

		}

		DownloadedData data = new DownloadedData(ticker, from, to);

		double logReturns[] = data.getLogReturns();
		Double wins = 0.0;
		for (int i = 0; i < logReturns.length; i++) {

			if (logReturns[i] < 0.0) {
				wins++;
			}
		}

		if (Double.isNaN(wins / logReturns.length)) {
			return new WinrateRecord(ticker, 0.0, 0.0);

		} else {
			return new WinrateRecord(ticker, wins / logReturns.length, (double) logReturns.length);
		}
	}

	List<WinrateRecord> getWinRates() {

		loadStockTickers();

		List<WinrateRecord> records = new ArrayList<>();

		for (String ticker : stockTickers) {

			System.out.println("Loading....." + ticker);
			records.add(getWinRate(ticker));

		}

		Collections.sort(records, new Comparator<WinrateRecord>() {
			@Override
			public int compare(WinrateRecord lhs, WinrateRecord rhs) {
				// -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
				return lhs.getWinrate() > rhs.getWinrate() ? -1 : (lhs.getWinrate() < rhs.getWinrate()) ? 1 : 0;
			}
		});

		// Write CSV file Called Records
		String csv = "";
		for (WinrateRecord record : records) {

			csv += (record.getTickerName() + "," + record.getWinrate() + "," + record.getSampleSize() + "\n");
		}

		try {
			FileWriter myWriter = new FileWriter("report.csv");
			
			myWriter.write(csv);
			myWriter.close();
			
		} catch (Exception e) {

			e.printStackTrace();

		}
		
		return records;

	}

}

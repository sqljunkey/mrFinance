package mr.Finance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public class Signal {

	public HashMap<String, String> findSignal() {

		List<String> tickers = Util.getStockListFromFile("liquid.lst");
		DownloadData d = new DownloadData();

		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		to.add(Calendar.DATE, -6);

		HashMap<String, String> signal = new HashMap<>();

		for (String tick : tickers) {

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			List<Double> prices = d.stock.getHistorical(tick, from, to);
			SimpleRegression r = new SimpleRegression();
			int counter = 0;

			if (prices.size() > 2) {

				Double returns = Math.log(prices.get(prices.size() - 1) / prices.get(prices.size() - 2));

				if (returns < 0 /* && Math.abs(returns) > 0.05 */) {

					System.out.println(tick);
					signal.put(tick, "long");
				}

			}

		}
		return signal;

	}
	
	public List<Record> getAllDividendList(){
		DownloadData d = new DownloadData();

		Pattern p = Pattern.compile(
				"(\\d+\\/\\d+\\/\\d+)\\s+(\\w+)\\s+((\\w+)?\\s+)+(\\d+|Special)\\s+(\\$\\d+\\.\\d+)\\s+(\\d+\\.\\d+|0)\\%?\\s+(\\d+\\/\\d+\\/\\d+)\\s+(\\d+\\/\\d+\\/\\d+)\\s+(\\d+\\/\\d+\\/\\d+)\\s+");

		Pattern p2 = Pattern.compile("(\\w+),");

		List<Record> signals = new ArrayList<>();

		Calendar future1 = Calendar.getInstance();
		Calendar future2 = Calendar.getInstance();
		Calendar exDiv = Calendar.getInstance();

		future1.add(Calendar.DAY_OF_YEAR, +20);
		future2.add(Calendar.DAY_OF_YEAR, +12);
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

		String dividend = Util.getFileInString("dividend.lst");
		String exclude = Util.getFileInString("exclude.lst");
		Matcher m = p.matcher(dividend);
		Matcher m2 = p2.matcher(exclude);

		List<String> exclusions = new ArrayList<>();

		while (m2.find()) {

			exclusions.add(m2.group(1));
		}

		while (m.find()) {
			
			try {
				Thread.sleep(1000);

			} catch (Exception e) {

			}

			System.out.println(m.group(1)); // Date
			System.out.println(m.group(2)); // Ticker
			System.out.println(m.group(5)); // Frequency | Special
			System.out.println(m.group(6)); // Payment
			System.out.println(m.group(7)); // Yield

			System.out.println("==========");
			Double payment = Double.parseDouble(m.group(6).replace("$", ""));
			Object[] data = d.stock.getPrice(m.group(2));
			Double yield = payment / (Double) data[0];
		
			Record record = new Record();
			record.setTicker(m.group(2));
			record.setSignal(yield);
			record.setElapsedTime(0);
			signals.add(record);
		
		}
		
		
		
		
		signals.sort(Comparator.comparing(Record::getSignal).reversed());
		signals.forEach(System.out::println);
		
		
		return signals;
		
	}
	
	public List<Record> getPositiveVolatility(String list,Double threshold){
		
		
		List<Record> result = new ArrayList<>();
		
		List<Record> records = getVolatility(list,threshold);
		for(Record record: records) {
			
			if(record.getSignal()>0) {
				
				result.add(record);
			}
		}
		
		
		return result;
	}
	
	
	public List<Record> getNegativeVolatility(String list,Double threshold){
		
		List<Record> result = new ArrayList<>();
		
		List<Record> records = getVolatility(list,threshold);
		for(Record record: records) {
			
			if(record.getSignal()<0) {
				
				result.add(record);
			}
		}
		
		
		return result;
	}
	
	
	public List<Record> getVolatility(String list, Double threshold){
		DownloadData d = new DownloadData();
		List<Record> signals = new ArrayList<>();
		List<String> tickers= Util.getStockListFromFile(list);
		
		
		for(String tick: tickers) {
			try {
			Thread.sleep(2000);
			}catch(Exception e) {
				
			}
			
			Object[] data = d.stock.getPrice(tick);
			Double marketCap = Util.parseNumberWithLetter((String) data[5]);
			Double avgVolume  = Util.parseNumberWithLetter((String) data[8]);
			Double averageDollarTraded= avgVolume*(Double) data[0];
			
			if(  averageDollarTraded > 1E6  && Math.abs((Double)data[1]) >threshold && (Double)data[0] > 2.0) {
				
			Record record = new Record();
			record.setTicker(tick);
			record.setSignal((Double)data[1]);
			record.setCost((Double) data[0]);
			record.setUnits(1.0);
			record.setElapsedTime(0);
			signals.add(record);}
			
		}
		
		signals.sort(Comparator.comparing(Record::getSignal).reversed());
		signals.forEach(System.out::println);
		
		
		return signals;
		
	}
	
	public List<Record> getReturnField(){
		DownloadData d = new DownloadData();
		List<Record> signals = new ArrayList<>();
		Double counter = 0.0;
		Double totalChange =0.0;
		List<String> tickers= Util.getStockListFromFile("sp500ticker.lst");
		
		for(String tick: tickers) {
			
			try {
			Thread.sleep(2000);
			}catch(Exception e) {
				
			}
			Object[] data = d.stock.getPrice(tick);
			counter++;
			totalChange+=(Double)data[1];
			
			
			Record record = new Record();
			record.setTicker(tick);
			record.setSignal((Double)data[1]);
			record.setCost((Double) data[0]);
			record.setUnits(1.0);
			record.setElapsedTime(0);
			signals.add(record);
			
		}
		
		
		if((totalChange/counter)>0.0) {
			
			signals.sort(Comparator.comparing(Record::getSignal).reversed());
			signals = signals.subList(0, Math.min(45, signals.size()));
			signals.forEach(System.out::println);
			
		}else {
			
			signals.sort(Comparator.comparing(Record::getSignal));
			signals = signals.subList(0, Math.min(45, signals.size()));
			signals.forEach(System.out::println);
		}
		
		
		return signals;
	}

	public List<Record> getDividendList() {

		DownloadData d = new DownloadData();

		Pattern p = Pattern.compile(
				"(\\d+\\/\\d+\\/\\d+)\\s+(\\w+)\\s+((\\w+)?\\s+)+(\\d+|Special)\\s+(\\$\\d+\\.\\d+)\\s+(\\d+\\.\\d+|0)\\%?\\s+(\\d+\\/\\d+\\/\\d+)\\s+(\\d+\\/\\d+\\/\\d+)\\s+(\\d+\\/\\d+\\/\\d+)\\s+");

		Pattern p2 = Pattern.compile("(\\w+),");

		List<Record> signals = new ArrayList<>();

		Calendar future1 = Calendar.getInstance();
		Calendar future2 = Calendar.getInstance();
		Calendar exDiv = Calendar.getInstance();

		future1.add(Calendar.DAY_OF_YEAR, +20);
		future2.add(Calendar.DAY_OF_YEAR, +12);
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

		String dividend = Util.getFileInString("dividend.lst");
		String exclude = Util.getFileInString("exclude.lst");
		Matcher m = p.matcher(dividend);
		Matcher m2 = p2.matcher(exclude);

		List<String> exclusions = new ArrayList<>();

		while (m2.find()) {

			exclusions.add(m2.group(1));
		}

		while (m.find()) {

			System.out.println(m.group(1)); // Date
			System.out.println(m.group(2)); // Ticker
			System.out.println(m.group(5)); // Frequency | Special
			System.out.println(m.group(6)); // Payment
			System.out.println(m.group(7)); // Yield

			System.out.println("==========");

			if (!exclusions.contains(m.group(2))) {
				try {
					formatter.parse(m.group(1));

					exDiv = formatter.getCalendar();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (exDiv.compareTo(future1) <= 0 && exDiv.compareTo(future2) > 0) {

					Double payment = Double.parseDouble(m.group(6).replace("$", ""));
					try {
						Thread.sleep(1000);

					} catch (Exception e) {

					}
					Object[] data = d.stock.getPrice(m.group(2));
					Double yield = payment / (Double) data[0];
					Double marketCap = Util.parseNumberWithLetter((String) data[5]);
					Double avgVolume  = Util.parseNumberWithLetter((String) data[8]);
					
					Double averageDollarTraded= avgVolume*(Double) data[0];
					System.out.println(m.group(2) + " AverageVolume $"+ avgVolume*(Double) data[0]);
					
					if ( /*averageDollarTraded > 1E5 &&*/ yield > 0.028 && !yield.isInfinite()
							 && !yield.isNaN()) {
						
						

						Record record = new Record();
						record.setTicker(m.group(2));
						record.setSignal(yield);
						record.setCost((Double) data[0]);
						record.setUnits(1.0);
						record.setElapsedTime(0);
						signals.add(record);

					}

				}
			}

		}

		signals.sort(Comparator.comparing(Record::getSignal).reversed());
		signals.forEach(System.out::println);

		return signals;
	}

}

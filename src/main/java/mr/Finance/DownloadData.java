package mr.Finance;

import java.net.URL;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;


public class DownloadData extends Thread {

	public Stock stock = new Stock();

	public DownloadData() {
		super();
		
	}

	public static class Stock {
		List<Data> cache = new CopyOnWriteArrayList();
		

		public Object[] getFCF(String ticker) {
			Object[] prices = { "n/a", "n/a", "n/a", "n/a", "n/a" };
			//String url = "https://www.marketwatch.com/investing/stock/" + ticker + "/financials/cash-flow";

			try {
			//	driver.get(url);
				//Document doc = Jsoup.parse(driver.getPageSource());
				
				URL url = new URL( "https://www.marketwatch.com/investing/stock/" + ticker + "/financials/cash-flow");
				Document doc = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				
				Elements info = doc.select("tr");
				String freecashflow = "";
				for (Element i : info) {

					if (i.text().contains("Free Cash Flow")) {
						freecashflow = i.text();
						break;
					}
				}

				String quotes[] = freecashflow.replaceAll("Free Cash Flow ", "").split("\\s");

				int f = 0;
				for (int i = quotes.length - 1; i >= 0; i--) {
					prices[f] = quotes[i];
					f++;

				}

			} catch (Exception e) {

			}

			return prices;

		}

		public Object[] getOpCash(String ticker) {
			Object[] prices = { "n/a", "n/a", "n/a", "n/a", "n/a" };
			//String url = "https://www.marketwatch.com/investing/stock/" + ticker + "/financials/cash-flow";

			try {
				URL url = new URL( "https://www.marketwatch.com/investing/stock/" + ticker + "/financials/cash-flow");
				Document doc = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				Elements info = doc.select("tr");
				String freecashflow = "";
				for (Element i : info) {

					if (i.text().contains("Net Operating Cash Flow")) {
						freecashflow = i.text();
						break;
					}
				}

				String quotes[] = freecashflow.replaceAll("Net Operating Cash Flow ", "").split("\\s");

				int f = 0;
				for (int i = quotes.length - 1; i >= 0; i--) {
					prices[f] = quotes[i];
					f++;

				}

			} catch (Exception e) {

			}

			return prices;

		}

		public Object[] getIncome(String ticker) {
			Object[] prices = { "n/a", "n/a", "n/a", "n/a", "n/a" };
			//String url = "https://www.marketwatch.com/investing/stock/" + ticker + "/financials/";

			try {
				URL url = new URL( "https://www.marketwatch.com/investing/stock/" + ticker + "/financials/cash-flow");
				Document doc = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				Elements info = doc.select("tr");
				String freecashflow = "";
				for (Element i : info) {

					if (i.text().contains("Net Income")) {
						freecashflow = i.text();
						break;
					}
				}

				String quotes[] = freecashflow.replaceAll("Net Income ", "").split("\\s");

				int f = 0;
				for (int i = quotes.length - 1; i >= 0; i--) {
					prices[f] = quotes[i];
					f++;

				}

			} catch (Exception e) {

			}

			return prices;

		}

		public Object[] getRev(String ticker) {
			Object[] prices = { "n/a", "n/a", "n/a", "n/a", "n/a" };
			//String url = "https://www.marketwatch.com/investing/stock/" + ticker + "/financials/";

			try {
				URL url = new URL( "https://www.marketwatch.com/investing/stock/" + ticker + "/financials/");
				Document doc = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				Elements info = doc.select("tr");
				String freecashflow = "";
				for (Element i : info) {

					if (i.text().contains("Sales/Revenue") || i.text().contains("Interest Income")) {
						freecashflow = i.text();
						break;
					}
				}

				String quotes[] = freecashflow.replaceAll("Sales/Revenue ", "").split("\\s");

				int f = 0;
				for (int i = quotes.length - 1; i >= 0; i--) {
					prices[f] = quotes[i];
					f++;

				}

			} catch (Exception e) {

			}

			return prices;

		}

		
		public Object[] getCrypto(String ticker) {
			Document document = null;
			Object price[] = { 0.0, 0.0, "" };
			try {
				document = Jsoup.connect("https://coinmarketcap.com/all/views/all/")
						.header("Accept-Encoding", "gzip, deflate")
						.userAgent(
								"Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
						.referrer("http://www.google.com").maxBodySize(1200000).timeout(600000).followRedirects(true)
						.get();

				Elements info = document.select("table");
				for (Element row : info.select("tr")) {
					if (row.text().toLowerCase().contains(ticker)) {
						System.out.println(row.text());
						List<String> data = Arrays.asList(row.text().split(" "));
						System.out.println(row.select("td").get(4).text());
						System.out.println(row.select("td").get(9).text());
						System.out.println(row.select("td").get(1).text());
						price[0] = Double.parseDouble(row.select("td").get(4).text().replaceAll("[^0-9.-]", ""));
						price[1] = Double.parseDouble(row.select("td").get(8).text().replaceAll("[^0-9.-]", ""));
						price[2] = row.select("td").get(1).text();
						break;
					}
				}
				// price = Double.parseDouble(info.text().replaceAll("[^0-9.]", ""));
				// if(!document.select("div.qwidget-percent.qwidget-Red").isEmpty()){
				// price*=-1.0;
				// }

			} catch (Exception e) {
				e.printStackTrace();
			}
			return price;

		}

		public Object[] getCryptoAPI(String ticker) {
			Object price[] = { 0.0 };

			Document document = null;

			try {

				URL url = new URL(
						"https://min-api.cryptocompare.com/data/price?fsym=" + ticker.toUpperCase() + "&tsyms=USD");
				document = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));

				String s = document.text();

				if (!document.text().toLowerCase().contains("error")) {

					price[0] = Double.parseDouble(document.text().replaceAll("[^0-9.]", ""));
				}

				// System.out.println(obj.getString("price"));
				System.out.println("API : " + document.text());

			} catch (Exception e) {
				e.printStackTrace();

			}
			return price;
		}

		public Object[] getMadeUpIndex(List<String> file) {

			Object quote[] = { 0.0, 0.0 };
			List<Double> prices = new ArrayList<>();
			List<Double> changes = new ArrayList<>();
			for (String tick : file) {

				Object chg[] = getPrice(tick);

				if ((Double) chg[1] != 0.0) {

					prices.add((Double) chg[0]);
					changes.add((Double) chg[1]);
					System.out.println(tick + (Double) chg[1]);
				}

			}
			Double totalPrice = 0.0;
			for (Double price : prices) {

				totalPrice += price;
			}

			Double totalChange = 0.0;
			for (Double chg : changes) {

				totalChange += chg;
			}

			quote[1] = totalChange / changes.size();
			quote[0] = totalPrice / prices.size();
			return quote;

		}

		
		public Object[] getCap(String ticker) {

			Object quote[] = { 0.0, 0.0 };
			Document document = null;
			try {
				URL url = new URL("https://www.nasdaq.com/symbol/" + ticker);
				document = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				Elements info = document.select("div.table-row");

				for (Element data : info) {

					if (data.text().contains("Market Cap")) {

						quote[0] = Double.parseDouble(data.text().replaceAll("[^0-9.]", ""));
						break;
					}

				}

			} catch (Exception e) {
			}

			return quote;

		}

		public Object[] getYield(String ticker) {

			Object price[] = { 0.0, 0.0, "" };
			for (Data f : cache) {
				if (f.getFunctionType().contentEquals("getYield") && f.getTicker().contentEquals(ticker))
					return f.getData();
			}
			try {
				//String url = "http://quotes.wsj.com/bond/BX/TMUBMUSD" + ticker;
				//driver.get(url);
				//Document doc = Jsoup.parse(driver.getPageSource());
				
				URL url = new URL( "http://quotes.wsj.com/bond/BX/TMUBMUSD" + ticker);
				Document doc = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				Elements info = doc.select("span#quote_val");
				Elements chg = doc.select("span#quote_change");

				System.out.println(info.text());
				System.out.println(chg.text());
				price[0] = Double.parseDouble(info.text().replaceAll("[^0-9.-]", ""));
				price[1] = Double.parseDouble(chg.text().replaceAll("[^0-9.-]", ""));
			} catch (Exception e) {

				e.printStackTrace();
			}

			cache.add(new Data(ticker, "getYield", price));

			return price;
		}

		public Object[] getShiller() {
			Object quote[] = { 0.0, 0.0, 0.0, 0.0 };
			List<Object> quotes = new ArrayList<>();
			Document document = null;

			String urls[] = { "https://www.multpl.com/shiller-pe/", "https://www.multpl.com/s-p-500-pe-ratio" };
			for (int i = 0; i < urls.length; i++) {
				try {

					URL url = new URL(urls[i]);

					document = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
					Elements info = document.select("div#current");

					List<String> line = Arrays.asList(info.text().split(" "));
					int number = 1;
					Double value = 0.0;
					Double percent = 0.0;

					if (info.text().contains("-")) {
						number = -1;
					}
					for (String piece : line) {

						try {
							System.out.println(piece);
							if (!piece.contains("500")) {
								if (piece.contains("%")) {
									quotes.add(Double.parseDouble(piece.replaceAll("[^0-9.]", "")) * number);
									// System.out.println("Percent: "+number+" "+Double.parseDouble(
									// piece.replaceAll("[^0-9.]", "")));
									break;
								} else if (piece.contains("+") || piece.contains("-")) {
								} else {
									quotes.add(Double.parseDouble(piece.replaceAll("[^0-9.]", "")));
									// System.out.println(Double.parseDouble(piece.replaceAll("[^0-9.]", "")));
								}
							}
						} catch (Exception e) {
							// e.printStackTrace();
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			quote = quotes.toArray();
			return quote;
		}

		public String getInfo(String ticker) {
			Document document = null;
			String info = "";
			
			try {
				
				
				URL url = new URL( "https://quotes.wsj.com/"+ticker.toUpperCase());
				document = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				//System.out.println(document.text());
				
				
				Pattern p = Pattern.compile(
						"(\\$\\d+\\.\\d+)"
						+ "\\s+"
						+ "(USD)?"
						+ "\\s+"
						+ "(-?\\d+.\\d+)"
						+ "\\s+"
						+ "(-?\\d+\\.\\d+%)"
						);
				Matcher m = p.matcher(document.text());
				if (m.find()) {
					
					System.out.println(m.group(0));
					
					info+= m.group(1)+" "+m.group(3)+" "+m.group(4);
				}
				
				Pattern company = Pattern.compile(
						"Stock\\s+Price\\s+&\\s+News\\s+-(.+?)\\s+-"
						
						);
				 m = company.matcher(document.text());
				if (m.find()) {
					
					System.out.println(m.group(1));
					info+=m.group(1);
				}
				
				
			
				
				Pattern pe = Pattern.compile(
						"P/E\\s+Ratio\\s+\\(TTM\\)\\s+(\\d+.\\d+)(.+?)EPS"
						
						);
				 m = pe.matcher(document.text());
				if (m.find()) {
					
					System.out.println(m.group(1));
					info+=" P/E: "+m.group(1);
				}
				
				Pattern eps = Pattern.compile(
						"EPS\\s+\\(TTM\\)\\s+(-?\\$\\d+.\\d+)(.+?)Market\\s+Cap"
						
						);
				 m = eps.matcher(document.text());
				if (m.find()) {
					
					System.out.println(m.group(1));
					info+=" EPS: "+m.group(1);
				}
				
				Pattern cap = Pattern.compile(
						"EPS\\s+\\(TTM\\)(.+?)Market\\s+Cap\\s+(.+?)\\s+Shares"
						
						);
				 m = cap.matcher(document.text());
				if (m.find()) {
					
					System.out.println(m.group(2));
					info+=" MktCp: "+m.group(2);
				}
				
				Pattern yield = Pattern.compile(
						"Yield\\s+(\\d+\\.\\d+%)"
						
						);
				 m = yield.matcher(document.text());
				if (m.find()) {
					
					System.out.println(m.group(1));
					info+= " Yield: "+ m.group(1);
				}
				
					Pattern sector = Pattern.compile(
						"Sector\\s+(.+?)\\s+Sales\\s+or\\s+Revenue"
						
						);
				 m = sector.matcher(document.text());
				if (m.find()) {
					
					System.out.println(m.group(1));
					info+=" Sector: "+m.group(1);
				}
				Pattern after = Pattern.compile(
						"AFTER\\s+HOURS(.+?)(\\$\\d+\\.\\d+)\\s+(-?\\d+\\.\\d+)\\s+(-?\\d+\\.\\d+%)"
						
						);
				 m = after.matcher(document.text());
				if (m.find()) {
					
					info+=" AH: "+m.group(2)+" "+m.group(3)+" "+m.group(4);
					System.out.println(m.group(2));
				}
				
				System.out.println(info);
				
			}
			catch(Exception e) {
				
				e.printStackTrace();
			}
			
			return info;
			
		} 
		
		public Object[] getPrice(String ticker) {
			Document document = null;
			Object price[] = {

					0.0, // 0. Price
					0.0, // 1. Percentage
					"" // 2. Company Name

			};

			try {
				System.out.println("Downloading...");

				URL url = new URL("https://finance.yahoo.com/quote/" + ticker.toUpperCase());
				document = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));

				// System.out.println(document.text());

				// Get Price
				// Get Change
				// Get Percentage Change
				// Matching pattern 310,720.00 -445.00 (-0.14%)

				Pattern p = Pattern.compile(
						"(\\d+,)?(\\d+)?\\.\\d+\\s+?(\\+|-)?(\\d+,)?(\\d+)?\\.\\d+\\s+?\\((\\+|-)?(\\d+,)?(\\d+)?\\.\\d+%\\)");
				Matcher m = p.matcher(document.text());
				if (m.find()) {

					System.out.println(m.group(0));
					String[] splited = m.group(0).split("\\s+");

					price[0] = Double.parseDouble(splited[0].replaceAll("[^0-9.-]", ""));
					price[1] = Double.parseDouble(splited[2].replaceAll("[^0-9.-]", ""));

				}

				// Get Company Name
				// Matching pattern (ticker)

				Pattern name = Pattern.compile(".+?(\\(" + Pattern.quote(ticker.toUpperCase()) + "\\))");

				m = name.matcher(document.text());
				if (m.find()) {

					price[2] = m.group(0).replaceAll("\\(\\w+\\)", "");
					System.out.println(m.group(0).replaceAll("\\(\\w+\\)", ""));

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return price;
		}

		public List<String> getHeadline( String ticker) {

			List<String> headlines = new ArrayList<>();

			DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MMM-yyyy");
			try {

				//String url = "https://news.google.com/rss/search?q=" + ticker + "%20stock";
				//driver.get(url);
				//Document doc = Jsoup.parse(driver.getPageSource());
				
				URL url = new URL( "https://news.google.com/rss/search?q=" + ticker + "%20stock");
				Document doc = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				Elements info = doc.select("item");

				for (Element f : info) {

					if (!f.select("title").text().contains("Google News") && headlines.size() < 3) {

						DateTime nowTime = new DateTime();
						String date = f.select("pubDate").text();
						String[] datePieces = date.split(" ");

						String dater = datePieces[1] + "-" + datePieces[2] + "-" + datePieces[3];
						DateTime pubDate = dtf.parseDateTime(dater);

						LocalDate now = nowTime.toLocalDate();
						LocalDate printed = pubDate.toLocalDate();

						// System.out.println(now +" "+ printed+" "+dater);

						if (now.compareTo(printed) == 0)
							headlines.add(dater + " :" + f.select("title").text());

					}
				}

			} catch (Exception e) {
			}

			return headlines;
		}

		/*
		  Depreciated
		 
		   public Object[] getTradeView(WebDriver driver, String ticker) {
		  
		  Object price[] = { 0.0, 0.0, "" };
		 * 
		 * try { String url = "https://www.tradingview.com/symbols/" + ticker;
		 * driver.get(url); Document doc = Jsoup.parse(driver.getPageSource()); Elements
		 * info = doc.select(
		 * "span[class$=tv-symbol-header-quote__value tv-symbol-header-quote__value--large js-symbol-last]"
		 * ); Elements directionDown = doc.select(
		 * "span[class$=tv-symbol-header-quote__trend-arrow tv-symbol-header-quote__trend-arrow--small js-symbol-change-direction tv-symbol-header-quote__trend-arrow--falling]"
		 * ); Elements directionUp = doc.select(
		 * "span[class$=tv-symbol-header-quote__trend-arrow tv-symbol-header-quote__trend-arrow--small js-symbol-change-direction tv-symbol-header-quote__trend-arrow--growing]"
		 * ); Elements chg = doc.select(
		 * "span[class$=tv-symbol-header-quote__value tv-symbol-header-quote__value--small js-symbol-change-pt]"
		 * );
		 * 
		 * Double direction = 1.0;
		 * 
		 * if (!directionDown.isEmpty()) { System.out.println("Down"); direction *=
		 * -1.0; } else { System.out.println("up");
		 * 
		 * } System.out.println(info.text()); System.out.println(chg.text()); price[0] =
		 * Double.parseDouble(info.text().replaceAll("[^0-9.-]", "")); price[1] =
		 * direction * Double.parseDouble(chg.text().replaceAll("[^0-9.-]", "")); }
		 * catch (Exception e) {
		 * 
		 * e.printStackTrace(); }
		 * 
		 * return price; } public Object[] getJsoupPrice(String ticker, String id) {
		 * 
		 * Document document = null; Object price[] = { 0.0, 0.0 }; try {
		 * System.out.println("Downloading...");
		 * 
		 * URL url = new URL("https://old.nasdaq.com/symbol/" + ticker);
		 * 
		 * // URL url = new URL("https://finance.yahoo.com/chart/AAPL");
		 * 
		 * // URL url = new URL("https://finance.yahoo.com/chart/"+ticker); document =
		 * Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
		 * 
		 * // System.out.println(document.text());
		 * 
		 * Elements info = document.select("div#qwidget_lastsale");
		 * System.out.println(document.select("div#qwidget_lastsale").text()); if
		 * (info.text().replaceAll("[^0-9.]", "").isEmpty()) { return price; } price[0]
		 * = Double.parseDouble(info.text().replaceAll("[^0-9.]", "")); Elements data =
		 * document.select("div#qwidget_percent");
		 * System.out.println(document.select("div#qwidget_percent").text()); Double
		 * percent = Double.parseDouble(data.text().replaceAll("[^0-9.]", "")); if
		 * (!document.select("div.qwidget-percent.qwidget-Red").isEmpty()) { percent *=
		 * -1.0; } price[1] = percent; } catch (Exception e) { e.printStackTrace(); }
		 * 
		 * return price;
		 * 
		 * } public Object[] getAH(String ticker) {
		 * 
		 * Document document = null; Object price[] = { 0.0, 0.0 }; try {
		 * System.out.println("Downloading AH...");
		 * 
		 * URL url = new URL("https://www.nasdaq.com/symbol/" + ticker +
		 * "/after-hours");
		 * 
		 * document = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
		 * 
		 * Elements info = document.select("div#qwidget_lastsale");
		 * System.out.println(document.select("div#qwidget_lastsale").text()); price[0]
		 * = Double.parseDouble(info.text().replaceAll("[^0-9.]", "")); Elements data =
		 * document.select("div#qwidget_percent");
		 * System.out.println(document.select("div#qwidget_percent").text()); Double
		 * percent = Double.parseDouble(data.text().replaceAll("[^0-9.]", "")); if
		 * (!document.select("div.qwidget-percent.qwidget-Red").isEmpty()) { percent *=
		 * -1.0; } price[1] = percent; } catch (Exception e) { e.printStackTrace(); }
		 * return price;
		 * 
		 * } public Object[] getCompanyName(WebDriver driver, String ticker) { Object[]
		 * companyName = { "" }; for (Data f : cache) { if
		 * (f.getFunctionType().contentEquals("getCompanyName") &&
		 * f.getTicker().contentEquals(ticker)) return f.getData(); }
		 * 
		 * try { String url = "https://finance.yahoo.com/quote/" + ticker;
		 * driver.get(url); Document doc = Jsoup.parse(driver.getPageSource()); Elements
		 * info = doc.select("h1"); for (Element f : info) {
		 * System.out.println(f.text());
		 * 
		 * if (f.text().contains("(" + ticker.toUpperCase() + ")")) { companyName[0] =
		 * f.text().replaceAll("(" + ticker.toUpperCase() + ")", "").replaceAll("[()]",
		 * "");
		 * 
		 * System.out.println(companyName[0]); } } } catch (Exception e) {
		 * 
		 * }
		 * 
		 * cache.add(new Data(ticker, "getCompanyName", companyName));
		 * 
		 * return companyName; }
		 */

	}

	public void run() {

		while (true) {

			try {
				this.sleep(300000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			System.out.println("Running...");

		}

	}
}

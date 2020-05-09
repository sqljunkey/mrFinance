package mr.Finance;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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
import org.jsoup.Connection;
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

		public Object[] getFCF(String ticker,String quarterly) {
			Object[] prices = { "n/a", "n/a", "n/a", "n/a", "n/a" };
			// String url = "https://www.marketwatch.com/investing/stock/" + ticker +
			// "/financials/cash-flow";

			try {
				// driver.get(url);
				// Document doc = Jsoup.parse(driver.getPageSource());

				URL url = new URL("https://www.marketwatch.com/investing/stock/" + ticker + "/financials/cash-flow"+quarterly);
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

		public Object[] getOpCash(String ticker, String quarterly) {
			Object[] prices = { "n/a", "n/a", "n/a", "n/a", "n/a" };
			// String url = "https://www.marketwatch.com/investing/stock/" + ticker +
			// "/financials/cash-flow";

			try {
				URL url = new URL("https://www.marketwatch.com/investing/stock/" + ticker + "/financials/cash-flow"+quarterly);
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

		public Object[] getIncome(String ticker,String quarterly) {
			Object[] prices = { "n/a", "n/a", "n/a", "n/a", "n/a" };
			// String url = "https://www.marketwatch.com/investing/stock/" + ticker +
			// "/financials/";

			try {
				URL url = new URL("https://www.marketwatch.com/investing/stock/" + ticker + "/financials/cash-flow"+quarterly);
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

		public Object[] getRev(String ticker, String quarterly) {
			Object[] prices = { "n/a", "n/a", "n/a", "n/a", "n/a" };
			// String url = "https://www.marketwatch.com/investing/stock/" + ticker +
			// "/financials/";

			try {
				URL url = new URL("https://www.marketwatch.com/investing/stock/" + ticker + "/financials/"+quarterly);
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
		
		//Get Libor Rate Data
		public Double[] getLibor() {
			Double price[] = { 0.0, //1 month libor rate
					           0.0, //3 month libor rate
					           0.0, //6 month libor rate
					           0.0 //1 year  libor rate
					             };
			
				
			
			try {
				

				URL url = new URL("https://www.bankrate.com/rates/interest-rates/libor.aspx");
				Document doc = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				
				
				
				Pattern oneMonth = Pattern.compile("1\\s+Month\\s+LIBOR\\s+Rate\\s+(\\d+\\.\\d+)");
				Pattern threeMonth = Pattern.compile("3\\s+Month\\s+LIBOR\\s+Rate\\s+(\\d+\\.\\d+)");
				Pattern sixMonth = Pattern.compile("6\\s+Month\\s+LIBOR\\s+Rate\\s+(\\d+\\.\\d+)");
				Pattern oneYear = Pattern.compile("1\\s+Year\\s+LIBOR\\s+Rate\\s+(\\d+\\.\\d+)");
				
				//Check For one Month Libor Rate
				Matcher m = oneMonth.matcher(doc.text());
				
				if(m.find()) {
					
					price[0] = Double.parseDouble(m.group(1));
					
					
					
					}
				
				//Check for three Month Libor Rate
			     m = threeMonth.matcher(doc.text());
				
				if(m.find()) {
					
					price[1] = Double.parseDouble(m.group(1));
					
					}
				
				//Check for six Month Libor Rate
				 m = sixMonth.matcher(doc.text());
				 
				if(m.find()) {
					
					price[2] = Double.parseDouble(m.group(1));
					}
				
				//Check for one Year Libor Rate
				m = oneYear.matcher(doc.text());
				
				if(m.find()) {
					price[3] = Double.parseDouble(m.group(1));
					
					}
				
				
			}catch(Exception e) {
				
				e.printStackTrace();
				
			}
			
			
			
			
			
			
			
			
			
			return price;
					
			
		}
		public Object[] getYield(String ticker) {

			Object price[] = { 0.0, 0.0 };
			Pattern p = Pattern.compile("Yield(\\s+\\d+\\.\\d+\\%)\\s+(-?\\d+\\.\\d+)\\s+Price");
			try {
				

				URL url = new URL("https://quotes.wsj.com/bond/BX/TMUBMUSD" + ticker);
				Document doc = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				

				Matcher m = p.matcher(doc.text());
				//System.out.println(doc.text());
				if(m.find()) {
					
					System.out.println(m.group(0));
					System.out.println(m.group(1));
					System.out.println(m.group(2));
					price[0]=Double.parseDouble(m.group(1).replaceAll("[^0-9.]",""));
					price[1]=Double.parseDouble(m.group(2).replaceAll("[^0-9.]",""));
					
				}
				

			} catch (Exception e) {

				e.printStackTrace();
			}

			

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

		// Coronavirus Cases
		public String getCases() {
			Document document = null;
			String info = "";
			try {

				Connection connection = Jsoup.connect("https://www.worldometers.info/coronavirus/");

				// specify user agent
				connection.userAgent("Mozilla/5.0");

				// get the HTML document
				document = connection.get();

				Pattern cases = Pattern.compile("(Coronavirus\\s+Cases:)(\\s+\\d+,\\d+,\\d+)");
				Pattern death = Pattern.compile("(Deaths:)(\\s+(\\d+,)+\\d+)");
				Pattern recovred = Pattern.compile("(Recovered:)(\\s+(\\d+,)+\\d+)");

				Matcher m = cases.matcher(document.text());
				if (m.find()) {
					info += m.group(0) + " ";

				}

				m = death.matcher(document.text());

				if (m.find()) {
					info += m.group(0) + " ";

				}

				m = recovred.matcher(document.text());

				if (m.find()) {
					info += m.group(0) + " ";

				}

				System.out.println(document.text());
				System.out.println(info);

			} catch (Exception e) {
				e.printStackTrace();
			}

			return info;

		}

		// Depreciated-Maybe
		public String getInfo(String ticker) {
			Document document = null;
			String info = "";

			try {

				URL url = new URL("https://quotes.wsj.com/" + ticker.toUpperCase());
				document = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				// System.out.println(document.text());

				Pattern p = Pattern.compile("(\\$\\d+\\.\\d+)" + "\\s+" + "(USD)?" + "\\s+" + "(-?\\d+.\\d+)" + "\\s+"
						+ "(-?\\d+\\.\\d+%)");
				Matcher m = p.matcher(document.text());
				if (m.find()) {

					System.out.println(m.group(0));

					info += m.group(1) + " " + m.group(3) + " " + m.group(4);
				}

				Pattern company = Pattern.compile("Stock\\s+Price\\s+&\\s+News\\s+-(.+?)\\s+-"

				);
				m = company.matcher(document.text());
				if (m.find()) {

					System.out.println(m.group(1));
					info += m.group(1);
				}

				Pattern pe = Pattern.compile("P/E\\s+Ratio\\s+\\(TTM\\)\\s+(\\d+.\\d+)(.+?)EPS"

				);
				m = pe.matcher(document.text());
				if (m.find()) {

					System.out.println(m.group(1));
					info += " P/E: " + m.group(1);
				}

				Pattern eps = Pattern.compile("EPS\\s+\\(TTM\\)\\s+(-?\\$\\d+.\\d+)(.+?)Market\\s+Cap"

				);
				m = eps.matcher(document.text());
				if (m.find()) {

					System.out.println(m.group(1));
					info += " EPS: " + m.group(1);
				}

				Pattern cap = Pattern.compile("EPS\\s+\\(TTM\\)(.+?)Market\\s+Cap\\s+(.+?)\\s+Shares"

				);
				m = cap.matcher(document.text());
				if (m.find()) {

					System.out.println(m.group(2));
					info += " MktCp: " + m.group(2);
				}

				Pattern yield = Pattern.compile("Yield\\s+(\\d+\\.\\d+%)"

				);
				m = yield.matcher(document.text());
				if (m.find()) {

					System.out.println(m.group(1));
					info += " Yield: " + m.group(1);
				}

				Pattern sector = Pattern.compile("Sector\\s+(.+?)\\s+Sales\\s+or\\s+Revenue"

				);
				m = sector.matcher(document.text());
				if (m.find()) {

					System.out.println(m.group(1));
					info += " Sector: " + m.group(1);
				}
				Pattern after = Pattern
						.compile("AFTER\\s+HOURS(.+?)(\\$\\d+\\.\\d+)\\s+(-?\\d+\\.\\d+)\\s+(-?\\d+\\.\\d+%)"

						);
				m = after.matcher(document.text());
				if (m.find()) {

					info += " AH: " + m.group(2) + " " + m.group(3) + " " + m.group(4);
					System.out.println(m.group(2));
				}

				System.out.println(info);

			} catch (Exception e) {

				e.printStackTrace();
			}

			return info;

		}

		public Object[] getPrice(String ticker) {
			Document document = null;
			Object price[] = {

					0.0, // 0. Price
					0.0, // 1. Percentage
					"", // 2. Company Name
					"", //Div Yield
					"" //Pe Ratio
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
				
				
				
				//Get Divident Yield
				Pattern dividendYield = Pattern.compile("Dividend\\s+\\&\\s+Yield\\s+\\d+\\.\\d+\\s+\\((\\d+\\.\\d+\\%)\\)");
				
				m= dividendYield.matcher(document.text());
				
				if(m.find()) {
					price[3]=m.group(1);
					
				}
				
				//Get Divident Yield
				Pattern peRatio = Pattern.compile("PE\\s+Ratio\\s+\\(TTM\\)\\s+(\\d+\\.\\d+)");
				
				m= peRatio.matcher(document.text());
				
				if(m.find()) {
					price[4]=m.group(1);
					
				}
				

			} catch (Exception e) {
				e.printStackTrace();
			}

			return price;
		}

		public List<String> getHeadline(String ticker) {

			List<String> headlines = new ArrayList<>();

			DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MMM-yyyy");
			try {

				// String url = "https://news.google.com/rss/search?q=" + ticker + "%20stock";
				// driver.get(url);
				// Document doc = Jsoup.parse(driver.getPageSource());

				URL url = new URL("https://news.google.com/rss/search?q=" + ticker + "%20stock");
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
		 * This function assumes a regular person who earns 2,000 dollars a month and
		 * has to buy items out of a list The items in the link list are grouped under
		 * several categories. This person will allocate an equal portion of his
		 * earnings to each group item in the list. So for example if a #bag_of_apples
		 * group costs on average 5 dollars and there are 10 groups in the list of items
		 * he will allocate 2,000 / 10 to buying apples. Which is 200 dollars.
		 *
		 * With that 200 dollars he will be able to buy 40 bags of apples. Now for
		 * example the #automobile group costs on average 20,000 dollars, which is more
		 * than he earns a month. Again with a 10 group list he will allocate 200
		 * dollars towards a car in his monthly budgeting, 2,000 divided by 10 = 200.
		 * Dividing the average cost of a automobile gives me 200/20,000= 0.01. Or he is
		 * able to buy 1% of an #automobile.
		 *
		 * These two numbers are used to show the buying power of an individual earning
		 * 2,000 dollars a month. I use these numbers to normalize the average prices of
		 * each group and I add them all up to create a consumer price index.
		 *
		 */

		public Object[] getInflation() {

			// Final Object sent to caller
			Object[] data = {0.0, 0.0};

			// List of website prices
			List<List<String>> urlList = new ArrayList<>();

			// Total Price Index Tracker
			Double consumerPriceIndex = 0.0;
			
			//Old Consumer Data
			Double oldConsumerPriceIndexData = 0.0;

			// All price finder regex
			Pattern p = Pattern.compile("(\\$\\d+\\.\\d{2}?)|(\\$\\d+,\\d+)");

			// Load list of links if an error has occured fuction will return a null object
			// array
			// Which means it will not print anything
			
			try {
				FileInputStream fstream = new FileInputStream("./consumerpriceindextracker.lst");
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

				String strLine;

				while ((strLine = br.readLine()) != null) {

					oldConsumerPriceIndexData = Double.parseDouble(strLine);
					break;
				}

				br.close();
			} catch (Exception e) {

				e.printStackTrace();
			}

			try {
				FileInputStream fstream = new FileInputStream("./inflation.lst");
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

				String strLine;

				while ((strLine = br.readLine()) != null) {

					// USE # to create new price groups
					if (strLine.contains("#")) {

						urlList.add(new ArrayList<>());
					}

					if (strLine.contains("http"))
						urlList.get(urlList.size() - 1).add(strLine);
				}

				br.close();
			} catch (Exception e) {

				e.printStackTrace();
			}

			// Load Group of list group by group
			for (int i = 0; i < urlList.size(); i++) {

				Double priceTotal = 0.0;
				Double count = 0.0;
				// Load all links one by one
				for (String link : urlList.get(i)) {

					try {

						Connection connection = Jsoup.connect(link);

						// specify user agent
						connection.userAgent("Mozilla/5.0");

						// get the HTML document
						Document document = connection.get();

						Matcher m = p.matcher(document.text());

						while (m.find()) {

							
							
								priceTotal += Double.parseDouble(m.group(0).replaceAll("[^0-9.]", ""));
								count++;
							

						}
					} catch (Exception e) {

						e.printStackTrace();

					}

				}

				Double groupAveragePrice = (priceTotal / count);

				consumerPriceIndex += (2000 / urlList.size()) / groupAveragePrice;
			
				System.out.println("Running Power: " + consumerPriceIndex);

			}
			
			data[0] = consumerPriceIndex; 
			data[1] = Math.log(consumerPriceIndex/oldConsumerPriceIndexData)*100;
			return data;
		}

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

package mr.Finance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DownloadData {

	public Stock stock = new Stock();

	public DownloadData() {
		super();

	}

	public static class Stock {

		public boolean isTheMarketOpen() {
			boolean isOpen = false;

			try {

				File myObj = new File("isOpen.lst");
				if (myObj.createNewFile()) {
					System.out.println("File created: " + myObj.getName());
				} else {
					System.out.println("File already exists.");
				}
				List<String> isOpenList = Util.getStockListFromFile("isOpen.lst");

				Pattern data = Pattern.compile("(\\w+)\\s+(\\d+.\\d+)");

				for (String line : isOpenList) {
					Matcher m = data.matcher(line);

					if (m.find()) {
						String ticker = m.group(1);
						Double price = Double.parseDouble(m.group(2));

						Double newPrice = (Double) getPrice(ticker)[0];

						System.out.println(newPrice+" "+price);
						if (!newPrice.equals(price) && newPrice != 0.0) {
							isOpen = true;
							break;
						}

					}

				}

				FileWriter myWriter = new FileWriter("isOpen.lst");

				List<String> files = Util.getStockListFromFile("sp500ticker.lst");
				Random random = new Random();
				for (int i = 0; i < 3;) {
					int var = random.nextInt((files.size() - 1) - 0) + 0;

					String ticker = files.get(var);
					Double newPrice = (Double) getPrice(ticker)[0];
					if (newPrice != 0.0) {
						i++;
						myWriter.write(ticker + " " + newPrice + "\r\n");
					}
				}

				myWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return isOpen;
		}

		
		
		public void getDividendList() {

			Pattern p = Pattern.compile(
					"(\\d+\\/\\d+\\/\\d+)\\s+(\\w+)\\s+((\\w+)?\\s+)+(\\d+|Special)\\s+(\\$\\d+\\.\\d+)\\s+(\\d+\\.\\d+|0)\\%?\\s+(\\d+\\/\\d+\\/\\d+)\\s+(\\d+\\/\\d+\\/\\d+)\\s+(\\d+\\/\\d+\\/\\d+)\\s+(Get\\s+Alert)");

			Calendar future1 = Calendar.getInstance();
			Calendar future2 = Calendar.getInstance();
			Calendar exDiv = Calendar.getInstance();

			future1.add(Calendar.DAY_OF_YEAR, +13);
			future2.add(Calendar.DAY_OF_YEAR, +9);
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

			Double signal = 0.0;
			String ticker = "";
			Double pay = 0.0;
			String day = "";

			String dividend = Util.getFileInString("dividend.lst");
			Matcher m = p.matcher(dividend);

			while (m.find()) {

				System.out.println(m.group(1)); // Date
				System.out.println(m.group(2)); // Ticker
				System.out.println(m.group(5)); // Frequency | Special
				System.out.println(m.group(6)); // Payment
				System.out.println(m.group(7)); // Yield

				System.out.println("==========");

				if (!m.group(5).contains("12")) {
					try {
						formatter.parse(m.group(1));

						exDiv = formatter.getCalendar();
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (exDiv.compareTo(future1) <= 0 && exDiv.compareTo(future2) > 0) {

						Double payment = Double.parseDouble(m.group(6).replace("$", ""));
						Double yield = payment / (Double) getPrice(m.group(2))[0];

						if (yield > signal) {

							signal = yield;
							ticker = m.group(2);
							pay = payment;
							day = m.group(1);
						}

					}
				}

			}

			System.out.println(ticker + " : " + signal);
			System.out.println(pay);
			System.out.println(day);

		}

		public Object[] getFCF(String ticker, String quarterly) {
			Object[] prices = { "n/a", "n/a", "n/a", "n/a", "n/a" };
			// String url = "https://www.marketwatch.com/investing/stock/" + ticker +
			// "/financials/cash-flow";

			try {
				// driver.get(url);
				// Document doc = Jsoup.parse(driver.getPageSource());

				URL url = new URL(
						"https://www.marketwatch.com/investing/stock/" + ticker + "/financials/cash-flow" + quarterly);
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
				URL url = new URL(
						"https://www.marketwatch.com/investing/stock/" + ticker + "/financials/cash-flow" + quarterly);
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

		public Object[] getIncome(String ticker, String quarterly) {
			Object[] prices = { "n/a", "n/a", "n/a", "n/a", "n/a" };
			// String url = "https://www.marketwatch.com/investing/stock/" + ticker +
			// "/financials/";

			try {
				URL url = new URL(
						"https://www.marketwatch.com/investing/stock/" + ticker + "/financials/cash-flow" + quarterly);
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

		// https://finance.yahoo.com/quote/ETH-USD/history?p=ETH-USD
		public List<Double> getHistorical(String ticker, Calendar from, Calendar to) {

			Long startTime = from.getTimeInMillis() / 1000;
			Long endTime = to.getTimeInMillis() / 1000;

			System.out.println(startTime);
			System.out.println(endTime);
			List<Double> adjustedClose = new ArrayList<>();

			try {

				// https://query1.finance.yahoo.com/v7/finance/download/ETH-USD?period1=1566568826&period2=1598191226&interval=1d&events=history
				FileUtils.copyURLToFile(new URL("https://query1.finance.yahoo.com/v7/finance/download/" + ticker
						+ "?period1=" + endTime + "&period2=" + startTime + "&interval=1d&events=history")

						, new File("./" + ticker + ".cvs"), 2000, 2000);

			} catch (Exception e) {

				e.printStackTrace();

			}

			// Read Downloaded File

			BufferedReader csvReader = null;
			try {

				csvReader = new BufferedReader(new FileReader("./" + ticker + ".cvs"));
				String row;
				while ((row = csvReader.readLine()) != null) {
					String[] data = row.split(",");

					// Copy Adjusted

					try {

						adjustedClose.add(Double.parseDouble(data[5]));

					} catch (Exception e) {

						// e.printStackTrace();
					}

				}
			} catch (Exception e) {

				e.printStackTrace();
			} finally {
				try {
					csvReader.close();
				} catch (Exception e) {
					e.printStackTrace();

				}
			}

			// Delete Downloaded File

			File myObj = new File("./" + ticker + ".cvs");
			if (myObj.delete()) {
				// System.out.println("Deleted the file: " + myObj.getName());
			} else {
				System.out.println("Failed to delete the file.");
			}

			for (Double price : adjustedClose) {

				// System.out.println(price);
			}

			return adjustedClose;

		}

		public Object[] getGross(String ticker, String quarterly) {
			Object[] prices = { "n/a", "n/a", "n/a", "n/a", "n/a" };
			// String url = "https://www.marketwatch.com/investing/stock/" + ticker +
			// "/financials/";

			try {
				// URL url = new
				// URL("https://www.marketwatch.com/investing/stock/aapl/financials/income/quarter");
				URL url = new URL("https://www.marketwatch.com/investing/stock/" + ticker + "/financials/" + quarterly);
				Document doc = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				Elements info = doc.select("tr");
				String freecashflow = "";
				String quotes[];
				for (Element i : info) {

					if (i.text().contains("Gross Income")) {
						freecashflow = i.text();
						quotes = freecashflow.replaceAll("Gross Income", "").split("\\s");
						int f = 0;
						for (int d = quotes.length - 1; d >= 0; d--) {
							prices[f] = quotes[d];
							f++;

						}
						break;
					}

					if (i.text().contains("Net Interest Income")) {
						freecashflow = i.text();
						quotes = freecashflow.replaceAll("Net Interest Income", "").split("\\s");
						int f = 0;
						for (int d = quotes.length - 1; d >= 0; d--) {
							prices[f] = quotes[d];
							f++;

						}
						break;
					}

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
				// URL url = new
				// URL("https://www.marketwatch.com/investing/stock/aapl/financials/income/quarter");
				URL url = new URL("https://www.marketwatch.com/investing/stock/" + ticker + "/financials/" + quarterly);
				Document doc = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				Elements info = doc.select("tr");
				String freecashflow = "";
				for (Element i : info) {

					if (i.text().contains("Sales/Revenue")) {
						freecashflow = i.text();
						String quotes[] = freecashflow.replaceAll("Sales/Revenue", "").split("\\s");

						int f = 0;
						for (int d = quotes.length - 1; d >= 0; d--) {
							prices[f] = quotes[d];
							f++;

						}
						break;
					}

					if (i.text().contains("Interest Income")) {
						freecashflow = i.text();
						String quotes[] = freecashflow.replaceAll("Interest Income", "").split("\\s");

						int f = 0;
						for (int d = quotes.length - 1; d >= 0; d--) {
							prices[f] = quotes[d];
							f++;

						}
						break;
					}

				}

			} catch (Exception e) {

			}

			return prices;

		}

		public Object[] getDebt(String ticker, String quarterly) {
			Object[] prices = { "n/a", "n/a", "n/a", "n/a", "n/a" };
			// String url = "https://www.marketwatch.com/investing/stock/" + ticker +
			// "/financials/balance-sheet";

			try {
				URL url = new URL("https://www.marketwatch.com/investing/stock/" + ticker + "/financials/balance-sheet/"
						+ quarterly);
				Document doc = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				Elements info = doc.select("tr");
				String freecashflow = "";
				for (Element i : info) {

					if (i.text().startsWith("Total Liabilities")) {
						freecashflow = i.text();
						System.out.println(i.text());
						break;
					}
				}

				String quotes[] = freecashflow.replaceAll("Total Liabilities ", "").split("\\s");

				int f = 0;
				for (int i = quotes.length - 1; i >= 0; i--) {
					prices[f] = quotes[i];
					f++;

				}

			} catch (Exception e) {

			}

			return prices;

		}

		public Object[] getAsset(String ticker, String quarterly) {
			Object[] prices = { "n/a", "n/a", "n/a", "n/a", "n/a" };
			// String url = "https://www.marketwatch.com/investing/stock/" + ticker +
			// "/financials/balance-sheet";

			try {
				URL url = new URL("https://www.marketwatch.com/investing/stock/" + ticker + "/financials/balance-sheet/"
						+ quarterly);
				Document doc = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				Elements info = doc.select("tr");
				String freecashflow = "";
				for (Element i : info) {

					if (i.text().startsWith("Total Assets")) {
						freecashflow = i.text();
						System.out.println(i.text());
						break;
					}
				}

				String quotes[] = freecashflow.replaceAll("Total Assets ", "").split("\\s");

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
						System.out.println(row.select("td").get(1).text().replaceFirst(ticker.toUpperCase(), ""));
						price[0] = Double.parseDouble(row.select("td").get(4).text().replaceAll("[^0-9.-]", ""));
						price[1] = Double.parseDouble(row.select("td").get(8).text().replaceAll("[^0-9.-]", ""));
						price[2] = row.select("td").get(1).text().replaceFirst(ticker.toUpperCase(), "");
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

		public Object[] getBond() {

			Object quote[] = { 0.0, 0.0 };
			Document document = null;
			try {
				URL url = new URL(
						"https://finra-markets.morningstar.com/BondCenter/BondTradeActivitySearchResult.jsp?ticker=C908317&startdate=11%2F23%2F2021&enddate=11%2F23%2F2022");
				document = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));

				System.out.println(document.text());

			} catch (Exception e) {
			}

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
		// Get Title

		public String getTitle(String link) {

			String title = "";
			Document document = null;
			try {
				URL url = new URL(link);
				document = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));
				Elements info = document.select("title");
				title = info.text();
				// System.out.println(title);

				if (title.length() > 80) {
					title = title.substring(0, 79);
				}

			} catch (Exception e) {
			}

			return title;
		}

		// Get Libor Rate Data
		public String getEPSEstimate(String ticker) {

			String data = "";
			Document doc = null;
			try {

				doc = Jsoup.connect("https://www.wsj.com/market-data/quotes/" + ticker.toUpperCase()).get();

				// System.out.println(doc.wholeText());

				Pattern p = Pattern.compile(
						"will\\s+report\\s+(Q\\d+)\\s+earnings\\s+on\\s+(\\d+\\/\\d+\\/\\d+).+?Estimate TrendsCurrent:\\$(\\d+\\.\\d+)");
				Matcher m = p.matcher(doc.wholeText());

				if (m.find()) {

					data = ticker.toUpperCase() + " will announce " + m.group(1) + " earnings on " + m.group(2)
							+ ". Forecasted EPS: $" + m.group(3);
					System.out.println(data);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return data;
		}

		// Get Libor Rate Data
		public Double[] getLibor() {
			Double price[] = { 0.0, // 1 month libor rate
					0.0, // 3 month libor rate
					0.0, // 6 month libor rate
					0.0 // 1 year libor rate
			};

			try {

				URL url = new URL("https://www.bankrate.com/rates/interest-rates/libor.aspx");
				Document doc = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));

				Pattern oneMonth = Pattern.compile("1\\s+Month\\s+LIBOR\\s+Rate\\s+(\\d+\\.\\d+)");
				Pattern threeMonth = Pattern.compile("3\\s+Month\\s+LIBOR\\s+Rate\\s+(\\d+\\.\\d+)");
				Pattern sixMonth = Pattern.compile("6\\s+Month\\s+LIBOR\\s+Rate\\s+(\\d+\\.\\d+)");
				Pattern oneYear = Pattern.compile("1\\s+Year\\s+LIBOR\\s+Rate\\s+(\\d+\\.\\d+)");

				// Check For one Month Libor Rate
				Matcher m = oneMonth.matcher(doc.text());

				if (m.find()) {

					price[0] = Double.parseDouble(m.group(1));

				}

				// Check for three Month Libor Rate
				m = threeMonth.matcher(doc.text());

				if (m.find()) {

					price[1] = Double.parseDouble(m.group(1));

				}

				// Check for six Month Libor Rate
				m = sixMonth.matcher(doc.text());

				if (m.find()) {

					price[2] = Double.parseDouble(m.group(1));
				}

				// Check for one Year Libor Rate
				m = oneYear.matcher(doc.text());

				if (m.find()) {
					price[3] = Double.parseDouble(m.group(1));

				}

			} catch (Exception e) {

				e.printStackTrace();

			}

			return price;

		}

		public Object[] getYield(String ticker) {

			Object price[] = { 0.0, 0.0 };
			Pattern p = Pattern.compile("Yield(\\s+\\d+\\.\\d+\\%)\\s+(-?\\d+\\.\\d+)\\s+Price");
			try {

				// Patch By JustAnotherUser
				URL url = new URL("https://quotes.wsj.com/bond/BX/TMUBMUSD" + ticker);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestProperty("user-agent", "Mozilla/5.0");
				con.connect();
				StringBuffer sb = new StringBuffer();
				Scanner sc = new Scanner(con.getInputStream());
				while (sc.hasNext()) {
					sb.append(sc.nextLine());
				}

				// End Patch
				Document doc = Jsoup.parse(sb.toString());

				Matcher m = p.matcher(doc.text());
				// System.out.println(doc.text());
				if (m.find()) {

					System.out.println(m.group(0));
					System.out.println(m.group(1));
					System.out.println(m.group(2));
					price[0] = Double.parseDouble(m.group(1).replaceAll("[^0-9.]", ""));
					price[1] = Double.parseDouble(m.group(2).replaceAll("[^0-9.]", ""));

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

		public Double getDividend(String ticker) {
			Double divRate = 0.0;

			try {

				String url = new String("https://finance.yahoo.com/quote/" + ticker.toUpperCase() + "?p="
						+ ticker.toUpperCase() + "&.tsrc=fin-srch");
				Document doc = Jsoup.connect(url).userAgent(
						"Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
						.referrer("http://www.google.com").get();

				Pattern p = Pattern.compile("Dividend\\s+&\\s+Yield\\s+(\\d+.\\d+)");
				Matcher m = p.matcher(doc.text());

				if (m.find()) {

					divRate = Double.parseDouble(m.group(1));

				}
				// System.out.println(doc.text());
			} catch (Exception e) {

				e.printStackTrace();
			}

			return divRate;
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

		public Object[] getStrength() {

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Object[] priceInfo = { 0.0, // Strength
					0.0, // Current Price
					0.0 // Target Price
			};
			Date dateNow = new Date();
			Date dateBeginning = null;
			try {
				dateBeginning = format.parse("2021-01-01");

			} catch (ParseException e) {

			}

			Double startprice = 380.0;
			Double currentPrice = (Double) getPrice("spy")[0];

			Long timeElapsed = dateNow.getTime() - dateBeginning.getTime();

			Double growthRate = 0.208 / (365.0 * 24.0 * 60.0 * 60.0 * 1000.0);

			System.out.println(growthRate);
			System.out.println("Return:" + (1 + (timeElapsed * growthRate)));
			System.out.println("Start Price: " + startprice);
			System.out.println("Current Stock: " + currentPrice);
			System.out.println("Time Elapsed: " + timeElapsed);

			System.out.println("Calculated Stock: " + (startprice * (1 + (timeElapsed * growthRate))));

			Double distance = ((startprice * (1 + (timeElapsed * growthRate))) - currentPrice);

			Double strength = Math.pow(distance, 2.0);
			if (distance < 0) {

				strength *= -1.0;
			}

			priceInfo[0] = strength;
			priceInfo[1] = currentPrice;
			priceInfo[2] = (startprice * (1 + (timeElapsed * growthRate)));

			return priceInfo;
		}

		public Object[] getKeyStatistics(String ticker) {
			Document document = null;
			Object price[] = {

					"", // 0. 52 week change
					"", // 1. beta

			};

			try {
				System.out.println("Downloading...");

				URL url = new URL("https://finance.yahoo.com/quote/" + ticker.toUpperCase() + "/key-statistics?p="
						+ ticker.toUpperCase());
				document = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));

				System.out.println(document.text());

				// Get 52 week
				// Get Beta

				Pattern patternBeta = Pattern.compile("Beta\\s+\\(5Y Monthly\\)\\s+(\\d+.\\d+)");
				Matcher m = patternBeta.matcher(document.text());

				m = patternBeta.matcher(document.text());
				if (m.find()) {
					price[1] = m.group(1);
				}
				Pattern pattern52Week = Pattern.compile("\\d+\\.\\d+\\s+52-Week\\s+Change\\s+3\\s+(-?\\d+.\\d+\\%)");
				m = pattern52Week.matcher(document.text());
				if (m.find()) {
					price[0] = m.group(1);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println(price[0] + " " + price[1]);
			return price;
		}
		
		
		public void shorterStockList(){
					
			List<String> tickers= Util.getStockListFromFile("nasdaq.lst");
			
			
			for(String tick: tickers) {
				try {
				Thread.sleep(2000);
				}catch(Exception e) {
					
				}
				
				Object[] data = getPrice(tick);
				Double marketCap = Util.parseNumberWithLetter((String) data[5]);
				Double avgVolume  = Util.parseNumberWithLetter((String) data[8]);
				Double averageDollarTraded= avgVolume*(Double) data[0];
				
				if(  averageDollarTraded > 1E6) {
					Util.writeToFile("nasdaqShort.lst", tick);
                }
				
			}
			

			
		}
		

		public Object[] getPrice(String ticker) {
			Document document = null;
			Object price[] = {

					0.0, // 0. Price
					0.0, // 1. Percentage
					"", // 2. Company Name
					"", // 3. Div Yield
					"", // 4. Pe Ratio
					"", // 5. MarketCap
					"", // 6. After Hours
					"", // 7. volume
					"", // 8. avg volume
					0.0, //9. bid
					0.0 //10. ask
			};

			try {
				System.out.println("Downloading...");

				URL url = new URL("https://finance.yahoo.com/quote/" + ticker.toUpperCase()+"/");
				document = Jsoup.parse(IOUtils.toString(url, Charset.forName("UTF-8")));

				// System.out.println(document.text());

				// Get Price
				// Get Change
				// Get Percentage Change
				// Matching pattern 310,720.00 -445.00 (-0.14%)
				// System.out.println(document.text());

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

				// Get Divident Yield
				Pattern dividendYield = Pattern
						.compile("Dividend\\s+\\&\\s+Yield\\s+\\d+\\.\\d+\\s+\\((\\d+\\.\\d+\\%)\\)");

				m = dividendYield.matcher(document.text());

				if (m.find()) {
					price[3] = m.group(1);

				}

				// Get Divident Yield
				Pattern peRatio = Pattern.compile("PE\\s+Ratio\\s+\\(TTM\\)\\s+(\\d+\\.\\d+)");

				m = peRatio.matcher(document.text());

				if (m.find()) {
					price[4] = m.group(1);

				}

				// MarketCap

				Pattern marketCap = Pattern.compile("Market Cap (\\d+.\\d+(B|M|T))");

				m = marketCap.matcher(document.text());

				if (m.find()) {
					price[5] = m.group(1);
				}

				// Get After Hour

				Pattern afterHour = Pattern.compile("\\(((-|\\+)\\d+.\\d+\\%)\\)\\s+After\\s+hours:");

				m = afterHour.matcher(document.text());
				if (m.find()) {
					price[6] = m.group(1);
				}
				
				Pattern volume = Pattern.compile("Volume\\s+((\\d+,|\\d+)+)");

				m = volume.matcher(document.text());
				if (m.find()) {
					price[7] = m.group(1);
				}
				
				Pattern avgVolume = Pattern.compile("Avg.\\s+Volume\\s+((\\d+,|\\d+)+)");

				m = avgVolume.matcher(document.text());
				if (m.find()) {
					price[8] = m.group(1);
				}
				
				Pattern bid = Pattern.compile("Bid\\s+(\\d+.\\d+)x\\s+\\d+");

				m = bid.matcher(document.text());
				if (m.find()) {
					price[9] = m.group(1);
				}
				
				
				Pattern ask = Pattern.compile("Ask\\s+(\\d+.\\d+)x\\s+\\d+");

				m = ask.matcher(document.text());
				if (m.find()) {
					price[10] = m.group(1);
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

					if (!f.select("title").text().contains("Google News")) {

						DateTime nowTime = new DateTime();
						String date = f.select("pubDate").text();
						String[] datePieces = date.split(" ");

						String dater = datePieces[1] + "-" + datePieces[2] + "-" + datePieces[3];
						DateTime pubDate = dtf.parseDateTime(dater);

						LocalDate now = nowTime.toLocalDate();
						LocalDate printed = pubDate.toLocalDate();

						// System.out.println(now +" "+ printed+" "+dater);

						// if (now.compareTo(printed) == 0)
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
			Object[] data = { 0.0, 0.0 };

			// List of website prices
			List<List<String>> urlList = new ArrayList<>();

			// Total Price Index Tracker
			Double consumerPriceIndex = 0.0;

			// Old Consumer Data
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
			data[1] = Math.log(consumerPriceIndex / oldConsumerPriceIndexData) * 100;
			return data;
		}

	}

}

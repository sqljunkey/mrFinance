package mr.Finance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * 
 */
public class Communicator extends Thread {

	DownloadData d;
	AccountManager am = null;
	List<String> channels = new ArrayList<>();
	String NickName = "mrfinance";
	String Password = "";
	Trader t = null;
	
	public void addChannel(String channel) {

		channels.add(channel);
	}

	public class MyBot extends PircBot {

		public MyBot() {
			this.setName(NickName);
		}

		public void onPrivateMessage(String sender, String login, String hostname, String message) {

			onMessage("", sender, login, hostname, message);

		}

		@Override
		protected void onDisconnect() {
			super.onDisconnect();

			for (int i = 0; i < 10000; i++) {
				try {

					System.out.println("Reconnecting....");
					Thread.sleep(10000);
					reconnect();
					bot.sendMessage("nickserv", " id " + Password);
					for (String channel : channels) {
						Thread.sleep(4000);
						this.joinChannel(channel);

					}

				} catch (Exception e) {
					if (bot.isConnected()) {
						break;
					}
					e.printStackTrace();
				}
			}

		}

		public void onMessage(String channel, String sender, String login, String hostname, String message) {
			char c = 0x03;

			System.out.println("Channel: " + channel);
			System.out.println("Sender: " + sender);

			if (channel.isEmpty()) {

				channel = sender;

			}
			if (message.contentEquals(".clearCache") && sender.contains("hammond")) {
				d.stock.cache.clear();
				sendMessage(channel,
						sender + ":" + "There are " + d.stock.cache.size() + " currently stored in the cached system.");

			}
			if (message.contentEquals(".getCache")) {

				sendMessage(channel,
						sender + ":" + "There are " + d.stock.cache.size() + " currently stored in the cached system.");

			}

			if (message.contains(".coronavirus")) {

				sendMessage(channel, sender + ": " + d.stock.getCases());

			}

			// FIX
			if (message.contains(".uptime")) {
				RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
				long uptime = rb.getUptime();
				int seconds = (int) (uptime / 1000) % 60;
				int minutes = (int) ((uptime / (1000 * 60)) % 60);
				int hours = (int) ((uptime / (1000 * 60 * 60)) % 24);
				sendMessage(channel, " Uptime:" + hours + " hours, " + minutes + " minutes, " + seconds + " seconds.");
			}

			// Begin Papertrade commands

			if (message.contains(".openaccount") && message.charAt(0) == '.' && message.charAt(1) == 'o') {
				if (am.addNewAccount(sender, 100000.0)) {

					sendMessage(sender, " New Account created.");
				} else {

					sendMessage(sender, " Account already exists.");
				}

			}

			if (message.contains(".getbalance") && message.charAt(0) == '.' && message.charAt(1) == 'g') {
				DecimalFormat fte = new DecimalFormat("###,###,###.00");

				sendMessage(sender, sender + ":You have $" + fte.format(am.getCashBalance(sender)) + " left.");

			}

			if (message.contentEquals(".restart") && sender.contentEquals("hammond")) {

				am.restart();

			}
			if (message.contains(".getportfolio") && message.charAt(0) == '.' && message.charAt(1) == 'g') {

				String[] parts = message.split("\\s");
				List<String> portfolio = am.getPortfolio(parts[1]);

				for (int i = 0; i < portfolio.size() - 1; i++) {
					sendMessage(sender, portfolio.get(i));
				}

			}
			if (message.contains(".limits") && message.charAt(0) == '.' && message.charAt(1) == 'l') {

				List<String> list = am.getLimit(sender);

				for (String line : list) {

					sendMessage(sender, line);

				}

			}

			if (message.contains(".set") && message.charAt(0) == '.' && message.charAt(1) == 's') {

				Pattern command = Pattern.compile(
						"\\.set\\s+(long|short)\\s+(.+?)\\s+sl\\s+(-\\d+\\.\\d+\\%)\\s+tp\\s+(\\d+\\.\\d+\\%)");
				Matcher m = command.matcher(message);

				if (m.find()) {

					String type = m.group(1);
					String ticker = m.group(2);
					Double stopLoss = Double.parseDouble(m.group(3).replaceAll("[^0-9.-]", ""));
					Double takeProfit = Double.parseDouble(m.group(4).replaceAll("[^0-9.-]", ""));

					System.out.println(type + " " + ticker + " " + stopLoss + " " + takeProfit);
					String info = am.setLimit(sender, ticker, type, stopLoss, takeProfit);

					sendMessage(sender, info);
					System.out.println(info);

				}

				else {

					sendMessage(sender,
							"Syntax to set limit: <.set long btc sl -9.0% tp 0.1%> / <.set long aapl sl -1.0% tp 10.0% >");
				}

			}

			if (message.contains(".unset") && message.charAt(0) == '.' && message.charAt(1) == 'u') {

				Pattern command = Pattern.compile("\\.unset\\s+(long|short)\\s+(.+?)($|\\s+)");
				Matcher m = command.matcher(message);

				if (m.find()) {

					String type = m.group(1);
					String ticker = m.group(2);

					System.out.println(type + " " + ticker + " ");

					String info = am.unSetLimit(sender, ticker, type);

					sendMessage(sender, info);
					System.out.println(info);

				}

				else {

					sendMessage(sender, "Syntax to unset limit: <.unset long btc / <.unset long aapl >");
				}

			}
			if (message.contains(".close") && message.charAt(0) == '.' && message.charAt(1) == 'c') {
				String[] parts = message.split("\\s");
				Pattern p = Pattern.compile("\\.close\\s+(\\d+)\\s+bond");
				Matcher m = p.matcher(message);
				Integer number = 0;
				if (parts.length == 5) {
					try {
						number = Integer.parseInt(parts[2]);
					} catch (Exception e) {
						e.printStackTrace();
					}
					String type = "";

					if (parts[1].contentEquals("long") || parts[1].contentEquals("short")) {
						type = parts[1];

					}
					String exchange = "";
					if (parts[4].contentEquals("equity") || parts[4].contentEquals("crypto")) {
						exchange = parts[4];

					}

					if (number != 0 && !type.isEmpty() && !exchange.isEmpty()) {
						String reply = am.closePosition(sender, number, parts[3].toLowerCase(), exchange, type);
						sendMessage(channel, sender + ": " + reply);
					} else {
						sendMessage(sender, "format <.open long 3 btc crypto> / <.open short 3 aapl equity>");
					}
				}  else if(m.find()) {
					
					
					sendMessage(sender, am.closeBond(sender, Integer.parseInt(m.group(1))).get(0));
					
				}else {

					System.out.println("arg number");
					sendMessage(sender, "format <.close long 3 btc crypto> / <.close short 3 aapl equity> / <.close 3 bond>");
				}

			}
			if (message.contains(".open") && !message.contains("openaccount") && message.charAt(0) == '.'
					&& message.charAt(1) == 'o') {
				
				Pattern p = Pattern.compile("\\.open\\s+(\\d+)\\s+bond");
				Matcher m = p.matcher(message);
				String[] parts = message.split("\\s");
				Integer number = 0;
				if (parts.length == 5) {
					try {
						number = Integer.parseInt(parts[2]);
					} catch (Exception e) {
						e.printStackTrace();
					}

					String type = "";

					if (parts[1].contentEquals("long") || parts[1].contentEquals("short")) {
						type = parts[1];

					}
					String exchange = "";
					if (parts[4].contentEquals("equity") || parts[4].contentEquals("crypto")) {
						exchange = parts[4];

					}

					if (number != 0 && !type.isEmpty() && !exchange.isEmpty()) {
						String reply = am.openPosition(sender, number, parts[3].toLowerCase(), exchange, type);
						sendMessage(sender, reply);
					} else {
						sendMessage(sender, "format <.open long 3 btc crypto> / <.open short 3 aapl equity>");
					}
				} else if(m.find()) {
					
					
					sendMessage(sender, am.openBond(sender, Integer.parseInt(m.group(1))).get(0));
					
				}
				else {
					System.out.println("arg number");
					sendMessage(sender, "format <.open long 3 btc crypto> / <.open short 3 aapl equity>/<.open 3 bond");
				}

			}

			if (message.contains(".darkpool-price") && message.charAt(0) == '.' && message.charAt(1) == 'd') {
				DecimalFormat fte = new DecimalFormat("###,###,###.0000");
				String[] parts = message.split("\\s");
				if (parts.length == 3) {

					if (message.contains("crypto")) {
						Object data[] = am.getPositionValue(parts[1], "crypto");

						if ((Double) data[1] != 0.0) {

							sendMessage(sender,
									parts[1] + " Bid: " + fte.format(data[0]) + "  Ask:" + fte.format(data[1]));
						}

						else {

							sendMessage(sender, "ticker not found.");
						}

					}
					if (message.contains("equity")) {
						Object data[] = am.getPositionValue(parts[1], "equity");

						if ((Double) data[1] != 0.0) {

							sendMessage(sender,
									parts[1] + " Bid: " + fte.format(data[0]) + "  Ask:" + fte.format(data[1]));
						}

						else {

							sendMessage(sender, "ticker not found.");
						}
					}

				} else {

					sendMessage(sender, ".format <.darkpool-price btc crypto> / <.darkpool-price btc equity>");

				}

			}

			if (message.contains(".scores") && message.charAt(0) == '.' && message.charAt(1) == 's') {
				List<String> lines = am.getScore();
				sendMessage(sender, "" + "=============================================");
				sendMessage(sender, "" + " The game will run until the server crashes  ");
				sendMessage(sender, "" + "          Since Dec 01 2019                  ");
				sendMessage(sender, "" + "=============================================");
				for (String line : lines) {
					sendMessage(sender, "" + line);

				}

			}

			// End of Papertrade Commands

			if (message.equals(".futures")) {
				DecimalFormat fte = new DecimalFormat("#0.00");

				String parts[] = { "ES=F", "NQ=F" };
				String partz[] = { "E-Mini S&P 500", "E-Mini Dow", };
				Double price = 0.0;
				Double change = 0.0;
				String reply = "";

				for (int i = 0; i < parts.length; i++) {

					Object quote[] = { 0.0, 0.0 };

					quote = d.stock.getPrice(parts[i]);

					price = (Double) quote[0];
					change = (Double) quote[1];
					if (price > 0.0) {
						if (change < 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price);

							reply += " " + fte.format(change) + "%" + c + " ,";

						} else if (change > 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "03" + fte.format(price);

							reply += " +" + fte.format(change) + "%" + c + " ,";
						} else {
							reply += " " + partz[i] + ": " + fte.format(price);
							if (!change.isNaN()) {
								reply += " " + fte.format(change) + "% ,";
							}
						}

					}

				}

				reply = reply.substring(0, reply.lastIndexOf(","));
				String a[] = reply.split(" ");

				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

			}
			//New Function Testor
			if (message.equals(".test")) {
				
			//am.fix();
				
			}
			if (message.equals(".hammond")) {
				DecimalFormat fte = new DecimalFormat("#0.00");
				DecimalFormat fte2 = new DecimalFormat("#0.000");

				List<String> ticker = new ArrayList<>();
				try {
					FileInputStream fstream = new FileInputStream("./recession.lst");
					BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

					String strLine;

					while ((strLine = br.readLine()) != null) {

						ticker.add(strLine);
					}

					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				Object quote[] = d.stock.getMadeUpIndex(ticker);
				String reply = "";
				Double price = (Double) quote[0];
				Double change = (Double) quote[1];
				if (price > 0.0) {
					if (change < 0 && !change.isNaN()) {
						reply += " " + "Hammond Recession Proof Index. " + ": " + c + "04" + fte.format(price);

						reply += " " + fte2.format(change) + "%" + c + " ,";

					} else if (change > 0 && !change.isNaN()) {
						reply += " " + "Hammond Recession Proof Index. " + ": " + c + "03" + fte.format(price);

						reply += " +" + fte2.format(change) + "%" + c + " ,";
					} else {
						reply += " " + "Hammond Recession Proof Index. " + ": " + fte.format(price);
						if (!change.isNaN()) {
							reply += " " + fte2.format(change) + "% ,";
						}
					}

				}

				sendMessage(channel, sender + ":" + reply);

			}

			if (message.equals(".help")) {

				sendMessage(sender, "               PRESET COMMANDS:         ");
				sendMessage(sender, "(these commands don't require arguments)");
				sendMessage(sender, "----------------------------------------");
				sendMessage(sender, "   ");

				sendMessage(sender, ".fundamentals .fundamentalsq .head .futures, .vol, .grain  .asia, .coronavirus");
				sendMessage(sender, ".europe, .currency, .shiller, .metal, .soft , .livestock,");
				sendMessage(sender, ".oil, .crypto, .help, .market, .yield, .other, .hammond, .cpi");
				sendMessage(sender, "   ");

				sendMessage(sender, "             ARGUMENT COMMANDS:        ");
				sendMessage(sender, "---------------------------------------");
				sendMessage(sender, ".q aapl noc lmt    |  .q cad=x");
				sendMessage(sender, ".i aapl ");
				sendMessage(sender, "   ");

				sendMessage(sender, "                 GAMES:                ");
				sendMessage(sender, "---------------------------------------");
				sendMessage(sender, ".papertrade  ");

			}

			if (message.contains(".papertrade")) {


				sendMessage(sender, "Check commands at: https://github.com/sqljunkey/mrFinance/blob/master/papertradeHelp.txt");

			}

			if (message.equals(".grain")) {
				DecimalFormat fte = new DecimalFormat("#0.00");

				String parts[] = { "C=F", "S=F", "O=F" };
				String partz[] = { "Corn", "Soybean", "OATS" };
				Double price = 0.0;
				Double change = 0.0;
				String reply = "";

				for (int i = 0; i < parts.length; i++) {

					Object quote[] = { 0.0, 0.0 };

					quote = d.stock.getPrice(parts[i]);

					price = (Double) quote[0];
					change = (Double) quote[1];
					if (price > 0.0) {
						if (change < 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price);

							reply += " " + fte.format(change) + "%" + c + " ,";

						} else if (change > 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "03" + fte.format(price);

							reply += " +" + fte.format(change) + "%" + c + " ,";
						} else {
							reply += " " + partz[i] + ": " + fte.format(price);
							if (!change.isNaN()) {
								reply += " " + fte.format(change) + "% ,";
							}
						}

					}

				}

				reply = reply.substring(0, reply.lastIndexOf(","));
				String a[] = reply.split(" ");

				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

			}
			if (message.equals(".soft")) {
				DecimalFormat fte = new DecimalFormat("#0.00");

				String parts[] = { "CC=F", "KC=F", "LB=F", "OJ=F", "SB=F" };
				String partz[] = { "Cocoa", "Coffee", "Lumber", "OJ", "Sugar" };
				Double price = 0.0;
				Double change = 0.0;
				String reply = "";

				for (int i = 0; i < parts.length; i++) {

					Object quote[] = { 0.0, 0.0 };

					quote = d.stock.getPrice(parts[i]);

					price = (Double) quote[0];
					change = (Double) quote[1];
					if (price > 0.0) {
						if (change < 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price);

							reply += " " + fte.format(change) + "%" + c + " ,";

						} else if (change > 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "03" + fte.format(price);

							reply += " +" + fte.format(change) + "%" + c + " ,";
						} else {
							reply += " " + partz[i] + ": " + fte.format(price);
							if (!change.isNaN()) {
								reply += " " + fte.format(change) + "% ,";
							}
						}

					}

				}

				reply = reply.substring(0, reply.lastIndexOf(","));
				String a[] = reply.split(" ");

				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

			}

			if (message.equals(".animal") || message.equals(".livestock")) {
				DecimalFormat fte = new DecimalFormat("#0.00");

				String parts[] = { "FC=F", "LH=F", "LC=F" };
				String partz[] = { "Feeder Cattle", "Lean Hog", "Live Cattle" };
				Double price = 0.0;
				Double change = 0.0;
				String reply = "";

				for (int i = 0; i < parts.length; i++) {

					Object quote[] = { 0.0, 0.0 };

					quote = d.stock.getPrice(parts[i]);

					price = (Double) quote[0];
					change = (Double) quote[1];
					if (price > 0.0) {
						if (change < 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price);

							reply += " " + fte.format(change) + "%" + c + " ,";

						} else if (change > 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "03" + fte.format(price);

							reply += " +" + fte.format(change) + "%" + c + " ,";
						} else {
							reply += " " + partz[i] + ": " + fte.format(price);
							if (!change.isNaN()) {
								reply += " " + fte.format(change) + "% ,";
							}
						}

					}

				}

				reply = reply.substring(0, reply.lastIndexOf(","));
				String a[] = reply.split(" ");

				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

			}
			if (message.equals(".oil")) {
				DecimalFormat fte = new DecimalFormat("#0.00");

				String parts[] = { "CL=F", "BZ=F", "NG=F", "RB=F" };
				String partz[] = { "WTI Oil", "Brent Oil", "Nat Gas", "RBOB Gasoline" };
				Double price = 0.0;
				Double change = 0.0;
				String reply = "";

				for (int i = 0; i < parts.length; i++) {

					Object quote[] = { 0.0, 0.0 };

					quote = d.stock.getPrice(parts[i]);

					price = (Double) quote[0];
					change = (Double) quote[1];
					if (price > 0.0) {
						if (change < 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price);

							reply += " " + fte.format(change) + "%" + c + " ,";

						} else if (change > 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "03" + fte.format(price);

							reply += " +" + fte.format(change) + "%" + c + " ,";
						} else {
							reply += " " + partz[i] + ": " + fte.format(price);
							if (!change.isNaN()) {
								reply += " " + fte.format(change) + "% ,";
							}
						}

					}

				}

				reply = reply.substring(0, reply.lastIndexOf(","));

				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

			}

			if (message.equals(".cpi")) {

				DecimalFormat fte = new DecimalFormat("#0.00");
				Object data[]=d.stock.getInflation();
				
				String reply =" (Test Mode)Daily Consumer Price Index: ";
				Double price = (Double) data[0];
				Double change= (Double) data[1];
				
				if (price > 0.0) {
					if (change < 0 && !change.isNaN()) {
						reply +=   c + "04" + fte.format(price);

						reply += " " + fte.format(change) + "%" + c + " ,";

					} else if (change > 0 && !change.isNaN()) {
						reply += c + "03" + fte.format(price);

						reply += " +" + fte.format(change) + "%" + c + " ,";
					} else {
						reply +=  fte.format(price);
						if (!change.isNaN()) {
							reply += " " + fte.format(change) + "% ,";
						}
					}

				}
				
				
				sendMessage(channel, sender + ":" + reply);
			}

			if (message.equals(".yields") || message.equals(".yield") || message.equals(".bonds")
					|| message.equals(".bond")) {
				DecimalFormat fte = new DecimalFormat("#0.00");

				System.out.println("Getting yields");

				String parts[] = { "02Y", "05Y", "10Y", "30Y" };
				String partz[] = { "2 year", "5 year", "10 year", "30 year" };
				Double price = 0.0;
				Double change = 0.0;
				String reply = "";

				for (int i = 0; i < parts.length; i++) {

					Object quote[] = { 0.0, 0.0 };

					quote = d.stock.getYield(parts[i]);

					price = (Double) quote[0];
					change = (Double) quote[1];
					if (price > 0.0) {
						if (change > 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price) + "%" + c + " ,";
							;

						} else if (change < 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "03" + fte.format(price) + "%" + c + " ,";
							;

						} else {
							reply += " " + partz[i] + ": " + fte.format(price) + "%" + c + " ,";
							;

						}

					}

				}

				reply = reply.substring(0, reply.lastIndexOf(","));

				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

			}

			if (message.equals(".vol") || message.equals(".vix")) {
				DecimalFormat fte = new DecimalFormat("#0.00");

				String parts[] = { "^VIX", "^VIX9D", "^VIX3M", "^VIX6M" };
				String partz[] = { "CBOE Volatility Index", "9 Days", "3 Months", "6 Months" };
				Double price = 0.0;
				Double change = 0.0;
				String reply = "";

				for (int i = 0; i < parts.length; i++) {

					Object quote[] = { 0.0, 0.0 };
					try {
						quote = d.stock.getPrice(parts[i]);
					} catch (Exception ex) {
						Logger.getLogger(Communicator.class.getName()).log(Level.SEVERE, null, ex);
					}
					price = (Double) quote[0];
					change = (Double) quote[1];
					if (price > 0.0) {
						if (change < 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price);

							reply += " " + fte.format(change) + "%" + c + " ,";

						} else if (change > 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "03" + fte.format(price);

							reply += " +" + fte.format(change) + "%" + c + " ,";
						} else {
							reply += " " + partz[i] + ": " + fte.format(price);
							if (!change.isNaN()) {
								reply += " " + fte.format(change) + "% ,";
							}
						}

					}

				}

				reply = reply.substring(0, reply.lastIndexOf(","));

				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

			}

			if (message.equals(".metal") || message.equals(".metals")) {
				DecimalFormat fte = new DecimalFormat("#0.00");

				String parts[] = { "GC=F", "SI=F", "PL=F", "HG=F" };
				String partz[] = { "Gold", "Silver", "Platinum", "Copper" };
				Double price = 0.0;
				Double change = 0.0;
				String reply = "";

				for (int i = 0; i < parts.length; i++) {

					Object quote[] = { 0.0, 0.0 };

					quote = d.stock.getPrice(parts[i]);

					price = (Double) quote[0];
					change = (Double) quote[1];
					if (price > 0.0) {
						if (change < 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price);

							reply += " " + fte.format(change) + "%" + c + " ,";

						} else if (change > 0 && !change.isNaN()) {
							reply += " " + partz[i] + ": " + c + "03" + fte.format(price);

							reply += " +" + fte.format(change) + "%" + c + " ,";
						} else {
							reply += " " + partz[i] + ": " + fte.format(price);
							if (!change.isNaN()) {
								reply += " " + fte.format(change) + "% ,";
							}
						}

					}

				}

				reply = reply.substring(0, reply.lastIndexOf(","));

				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

			}

			if (message.equals(".currency") || message.equals(".fx") || message.equals(".currencies")) {
				try {
					DecimalFormat fte = new DecimalFormat("#0.00");

					String parts[] = { "GBPUSD=X", "JPY=X", "EURUSD=X", "USDCNY", "CAD=X", "MXN=X", "GC=F", "BTC-USD" };
					String partz[] = { "GBPUSD", "USDJPY", "EURUSD", "USDCNY", "USDCAD", "USDMXN", "GOLD", "BTC-USD" };

					Double price = 0.0;
					Double change = 0.0;

					String reply = "";
					for (int i = 0; i < parts.length; i++) {
						Object quote[] = null;

						quote = d.stock.getPrice(parts[i]);

						price = (Double) quote[0];
						change = (Double) quote[1];

						if (price != 0.0) {
							if (change < 0) {
								reply += " " + partz[i] + ": " + c + "04" + fte.format(price);

								reply += " " + fte.format(change) + "%" + c + " ,";

							} else if (change > 0) {
								reply += " " + partz[i] + ": " + c + "03" + fte.format(price);

								reply += " +" + fte.format(change) + "%" + c + " ,";
							} else {
								reply += " " + partz[i] + ": " + fte.format(price);
								if (!change.isNaN()) {
									reply += " " + fte.format(change) + "% ,";
								}

							}

						}
					}

					reply = reply.substring(0, reply.lastIndexOf(","));

					System.out.println(channel + ":" + sender + ":" + reply);
					sendMessage(channel, sender + ":" + reply);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			if (message.contains(".other")) {

				DecimalFormat fte = new DecimalFormat("#,##0.00");

				String parts[] = { "^KLSE", "^BVSP", "^JKSE", "^IMOEX.ME", "^MXX" };
				String partz[] = { "Malaysia", "Brasil BOVESPA", "JAKARTA", "MOEX Russia Index", "IPC Mexico" };

				Double price = 0.0;
				Double change = 0.0;

				String reply = "";
				for (int i = 0; i < parts.length; i++) {
					Object quote[] = { 0.0, 0.0 };

					quote = d.stock.getPrice(parts[i]);

					price = (Double) quote[0];
					change = (Double) quote[1];

					if (price != 0.0) {
						if (change < 0) {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price);

							reply += " " + fte.format(change) + "%" + c + " ,";

						} else if (change > 0) {
							reply += " " + partz[i] + ": " + c + "03" + fte.format(price);

							reply += " +" + fte.format(change) + "%" + c + " ,";
						} else {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price) + c + "03" + ",";

						}

					}
				}

				reply = reply.substring(0, reply.lastIndexOf(","));

				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

			}
			if (message.equals(".asia")) {

				DecimalFormat fte = new DecimalFormat("#,##0.00");

				String parts[] = { "^N225", "^HSI", "^STI", "^AXJO" };
				String partz[] = { "Nikkei", "Hang Seng", "STI Index", "Aus. ASX 200" };

				Double price = 0.0;
				Double change = 0.0;

				String reply = "";
				for (int i = 0; i < parts.length; i++) {

					Object quote[] = d.stock.getPrice(parts[i]);
					price = (Double) quote[0];
					change = (Double) quote[1];

					if (price != 0.0) {
						if (change < 0) {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price);

							reply += " " + fte.format(change) + "%" + c + " ,";

						} else if (change > 0) {
							reply += " " + partz[i] + ": " + c + "03" + fte.format(price);

							reply += " +" + fte.format(change) + "%" + c + " ,";
						} else {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price) + c + "03" + ",";

						}

					}
				}

				reply = reply.substring(0, reply.lastIndexOf(","));

				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

			}

			if (message.equals(".europe")) {

				DecimalFormat fte = new DecimalFormat("#,##0.00");

				String parts[] = { "^FTSE", "^GDAXI", "^FCHI", "^BFX" };
				String partz[] = { "FTSE 100", "DAX", "Cac 40", "Bel 20" };

				Double price = 0.0;
				Double change = 0.0;

				String reply = "";
				for (int i = 0; i < parts.length; i++) {

					Object quote[] = d.stock.getPrice(parts[i]);
					price = (Double) quote[0];
					change = (Double) quote[1];

					if (price != 0.0) {
						if (change < 0) {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price);

							reply += " " + fte.format(change) + "%" + c + " ,";

						} else if (change > 0) {
							reply += " " + partz[i] + ": " + c + "03" + fte.format(price);

							reply += " +" + fte.format(change) + "%" + c + " ,";
						} else {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price) + c + "03" + ",";

						}

					}
				}

				reply = reply.substring(0, reply.lastIndexOf(","));

				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

			}

			if (message.equals(".market") || message.equals(".markets")) {

				DecimalFormat fte = new DecimalFormat("#,##0.00");

				String parts[] = { "^DJI", "^GSPC", "^RUT", "^IXIC", "^VIX" };
				String partz[] = { "Dow", "S&P 500", "Russell 2000", "NASDAQ", "VIX" };

				Double price = 0.0;
				Double change = 0.0;

				String reply = "";
				for (int i = 0; i < parts.length; i++) {

					Object quote[] = d.stock.getPrice(parts[i]);
					price = (Double) quote[0];
					change = (Double) quote[1];

					if (price != 0.0) {
						if (change < 0) {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price);

							reply += " " + fte.format(change) + "%" + c + " ,";

						} else if (change > 0) {
							reply += " " + partz[i] + ": " + c + "03" + fte.format(price);

							reply += " +" + fte.format(change) + "%" + c + " ,";
						} else {
							reply += " " + partz[i] + ": " + c + "04" + fte.format(price) + c + "03" + ",";

						}

					}
				}

				reply = reply.substring(0, reply.lastIndexOf(","));

				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

			}

			if (message.contains(".head")) {

				String[] parts = message.split(" ");

				List<String> headline = d.stock.getHeadline(parts[1]);
				String reply = "";

				int i = 0;
				for (String head : headline) {
					if(i>2) {break;}
					i++;
					sendMessage(channel, head);
				}

			}

			if (message.contains(".shiller")) {

				DecimalFormat fte = new DecimalFormat("#0.00");
				Object quote[] = d.stock.getShiller();
				String reply = " Shiller PE Ratio";
				Double price = (Double) quote[0];
				Double change = (Double) quote[1];
				if (price > 0.0) {
					if (change < 0 && !change.isNaN()) {
						reply += " " + ": " + c + "04" + fte.format(price);

						reply += " " + fte.format(change) + "%" + c;

					} else if (change > 0 && !change.isNaN()) {
						reply += " " + ": " + c + "03" + fte.format(price);

						reply += " +" + fte.format(change) + "%" + c;
					} else {
						reply += " " + ": " + fte.format(price);
						if (!change.isNaN()) {
							reply += " " + fte.format(change);
						}
					}

				}

				reply += " S&P 500 PE Ratio";

				price = (Double) quote[2];
				change = (Double) quote[3];
				if (price > 0.0) {
					if (change < 0 && !change.isNaN()) {
						reply += " " + ": " + c + "04" + fte.format(price);

						reply += " " + fte.format(change) + "%" + c;

					} else if (change > 0 && !change.isNaN()) {
						reply += " " + ": " + c + "03" + fte.format(price);

						reply += " +" + fte.format(change) + "%" + c;
					} else {
						reply += " " + ": " + fte.format(price);
						if (!change.isNaN()) {
							reply += " " + fte.format(change);
						}
					}

				}

				sendMessage(channel, sender + ":" + reply);
			}
			if (message.contains(".fundamental") && message.charAt(0) == '.' && message.charAt(1) == 'f') {

				String parts[] = message.split("\\s");
				String reply = "";
				String quarter = "";
				
				if(message.contains(".fundamentalsq")){
					quarter ="/income/quarter";
				}
				Object quotes[] = d.stock.getRev(parts[1],quarter);

				for (int i = 0; i < quotes.length; i++) {
					reply += " " + quotes[i] + ",";
				}
				reply = reply.substring(0, reply.lastIndexOf(","));
				sendMessage(channel, sender + ":" + parts[1] + " Revenue: " + reply);
				reply = "";
				
				if(message.contains(".fundamentalsq")){
					quarter ="/quarter";
				}
				
				Object quotes1[] = d.stock.getIncome(parts[1],quarter);

				for (int i = 0; i < quotes1.length; i++) {
					reply += " " + quotes1[i] + ",";
				}
				reply = reply.substring(0, reply.lastIndexOf(","));
				sendMessage(channel, sender + ":" + parts[1] + " Net Income: " + reply);

				reply = "";
				if(message.contains(".fundamentalsq")){
					quarter ="/quarter";
				}
				Object quotes2[] = d.stock.getOpCash(parts[1],quarter);

				for (int i = 0; i < quotes2.length; i++) {
					reply += " " + quotes2[i] + ",";
				}
				reply = reply.substring(0, reply.lastIndexOf(","));
				sendMessage(channel, sender + ":" + parts[1] + " Operating Cashflow: " + reply);
				reply = "";
				
				if(message.contains(".fundamentalsq")){
					quarter ="/quarter";
				}
				Object quotes3[] = d.stock.getFCF(parts[1],quarter);

				for (int i = 0; i < quotes3.length; i++) {
					reply += " " + quotes3[i] + ",";
				}
				reply = reply.substring(0, reply.lastIndexOf(","));
				sendMessage(channel, sender + ":" + parts[1] + " Free Cash Flow: " + reply);
				reply = "";
				
				if(message.contains(".fundamentalsq")){
					quarter ="/quarter";
				}
				Object quotes4[] = d.stock.getAsset(parts[1],quarter);

				for (int i = 0; i < quotes4.length; i++) {
					reply += " " + quotes4[i] + ",";
				}
				reply = reply.substring(0, reply.lastIndexOf(","));
				sendMessage(channel, sender + ":" + parts[1] + " Total Asset: " + reply);


			}

			if (message.contains(".i") && message.charAt(0) == '.' && message.charAt(1) == 'i') {
				DecimalFormat fte = new DecimalFormat("###,###,##0.00");
				String[] parts = message.split("\\s");

				sendMessage(channel, sender + ": " + d.stock.getInfo(parts[1]));

			}

			if (message.contains(".q") && message.charAt(0) == '.' && message.charAt(1) == 'q') {

				DecimalFormat fte = new DecimalFormat("###,###,##0.00");

				System.out.println("Quoted");
				String reply = "";
				Double price = 0.0;
				Double change = 0.0;
				Object quote[] = null;

				String[] parts = message.split("\\s");

				for (int i = 1; i < parts.length; i++) {
					d.stock.getPrice(parts[i]);

					quote = d.stock.getPrice(parts[i]);
					price = (Double) quote[0];
					change = (Double) quote[1];

					if (price > 0.0) {
						if (change < 0 && !change.isNaN()) {
							reply += " " + parts[i] + ": " + c + "04" + fte.format(price);

							reply += " " + fte.format(change) + "%" + c + " ,";

						} else if (change > 0 && !change.isNaN()) {
							reply += " " + parts[i] + ": " + c + "03" + fte.format(price);

							reply += " +" + fte.format(change) + "%" + c + " ,";
						} else {
							reply += " " + parts[i] + ": " + fte.format(price);
							if (!change.isNaN()) {
								reply += " " + fte.format(change) + "% ,";
							}
						}

					} else {

					}

				}

				reply = reply.substring(0, reply.lastIndexOf(","));
				String a[] = reply.split(" ");

				if (a.length == 4) {

					reply += quote[2];
					
					String div =(String) quote[3];
					if(!div.isEmpty()) {
						reply+=" Div: " + div;
					}
					String pe =(String) quote[4];
					if(!pe.isEmpty()) {
						reply+=" P/E: " + pe;
					}
					

				}
				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

				// End Third Try
			} else if (message.contains(".crypto")) {
				DecimalFormat fte = new DecimalFormat("#0.00000");
				DecimalFormat per = new DecimalFormat("#0.00");

				System.out.println("QuotedCrypto");
				String reply = "";
				Double price = 0.0;
				Double change = 0.0;
				Object data[];
				String[] parts = message.toLowerCase().split(" ");
				for (int i = 1; i < parts.length; i++) {
					data = d.stock.getCrypto(parts[i]);
					price = (Double) data[0];
					change = (Double) data[1];
					System.out.println(price);

					if (price > 0.0) {
						if (change < 0 && !change.isNaN()) {
							reply += " " + data[2] + ": " + c + "04" + fte.format(price);

							reply += " " + per.format(change) + "%" + c + " ,";

						} else if (change > 0 && !change.isNaN()) {
							reply += " " + data[2] + ": " + c + "03" + fte.format(price);

							reply += " +" + per.format(change) + "%" + c + " ,";
						} else {
							reply += " " + data[2] + ": " + fte.format(price);
							if (!change.isNaN()) {
								reply += " " + per.format(change) + "% ,";
							}
						}

					}

				}

				reply = reply.substring(0, reply.lastIndexOf(","));
				String a[] = reply.split(" ");

				if (a.length == 4) {
					// reply += d.stock.Name(a[1].replace(":", ""));

				}
				System.out.println(channel + ":" + sender + ":" + reply);
				sendMessage(channel, sender + ":" + reply);

			}
		}

	}

	public MyBot bot = new MyBot();

	public Communicator() throws IOException, IrcException {

		d = new DownloadData();
		d.start();
		
		
		//Start Account Manager
		
		am = new AccountManager(d);
		am.start();
		
		//Start Trader
		
		t = new Trader(am);

		// Load Password
		try {
			FileInputStream fstream = new FileInputStream("./passwrd.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			while ((strLine = br.readLine()) != null) {

				Password = strLine;
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void run() {
		try {
			// Connect to the IRC server.
			bot.connect("irc.freenode.net");
			bot.sendMessage("nickserv", " id " + Password);

			for (String channel : channels) {
				Thread.sleep(4000);

				bot.joinChannel(channel);

			}

			// Update Trackers every 6 hours
			while (true) {

				try {

					Boolean isUpdated = false;
					LocalDate date = LocalDate.now();
					System.out.println(date.toDate().toString());

					try {
						FileInputStream fstream = new FileInputStream("./consumerpriceindextracker.lst");
						BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

						String strLine;

						while ((strLine = br.readLine()) != null) {

							// if date is the same set true
							if (strLine.contentEquals(date.toDate().toString())) {
								isUpdated = true;

							}

						}

						br.close();
					} catch (Exception e) {

						e.printStackTrace();
					}

					if (!isUpdated) {
						Double data = (Double)d.stock.getInflation()[0];
						try {
							
							FileWriter myWriter = new FileWriter("./consumerpriceindextracker.lst");
							
							myWriter.write(""+data+"\n");
							myWriter.write(date.toDate().toString());
							
							myWriter.close();
						} catch (Exception e) {

							e.printStackTrace();
						}

					}

					this.sleep(21000000);
				} catch (Exception e) {

					e.printStackTrace();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			bot.onDisconnect();
		}

	}

	public void sendinformation(List<String> r) {
		for (String report : r) {
			bot.sendMessage("", report);
		}

	}
}

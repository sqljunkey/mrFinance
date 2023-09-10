package mr.Finance;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class AccountManager extends Thread {

	
	String passwd = "";
	String user = "";
	Double startAmount = 100000.0;
	DecimalFormat formatter = new DecimalFormat("###,###,##0.0000");
	DecimalFormat twoDecimal = new DecimalFormat("###,###,##0.00");
	List<Account> accounts = new ArrayList<>();

	AccountManager() {
		

		try {
			FileInputStream fstream = new FileInputStream("./sqlpasswrd.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			while ((strLine = br.readLine()) != null) {

				passwd = strLine;
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			FileInputStream fstream = new FileInputStream("./user.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			while ((strLine = br.readLine()) != null) {

				user = strLine;
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void run() {
		
		HashMap<String, Double> map = new HashMap<>();
		Boolean alreadyPaid = false;

		while (true) {

			
			try {
				
				
				System.out.println("Sleeping");
				this.sleep(1000*60*60*6);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Calendar cal = Calendar.getInstance();
			int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
			if(dayOfMonth!=1) {
				
				alreadyPaid =false;
			}

			Connection conn = null;
			Properties connectionProps = new Properties();
			connectionProps.put("user", user);
			connectionProps.put("password", passwd);
			if(!alreadyPaid&&dayOfMonth==1) {
				DownloadData d = new DownloadData();
			try {
				conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
				if (!conn.isClosed()) {
					    Statement smt = conn.createStatement();
						String query = "select orders.ticker  from orders where exchange!='bond' and exchange!='crypto' group by ticker";
						ResultSet rs = smt.executeQuery(query);
						while(rs.next()) {
							
							
							String ticker = rs.getString("TICKER");
							
							Double dividend =0.0;
							if(!ticker.contains("=")&&!ticker.contains("^")) {
							dividend = d.stock.getDividend(ticker);
							//System.out.println(ticker+" "+ dividend);
							}
							
							if(dividend!=0.0) {
								
								map.put(ticker, dividend/12);
								
							}
							
							
						}
						
						
						query ="select orders.user_id, orders.units, orders.ticker from orders";
						
						rs = smt.executeQuery(query);
						HashMap <Integer, Double> paymentSheet = new HashMap<>();
						while(rs.next()) {
							
							Integer user = rs.getInt("USER_ID");
							paymentSheet.put(user, 0.0);
						
						}
						
						rs= smt.executeQuery(query);
						
						while(rs.next()) {
							
							Integer user = rs.getInt("USER_ID");
							Integer units = rs.getInt("UNITS");
							String ticker = rs.getString("TICKER");
							
							if(map.containsKey(ticker)) {
								
								Double amount = map.get(ticker)*units;
								
								paymentSheet.put(user, paymentSheet.get(user)+amount);
								
							}
							
							
						}
						
						query = "select * from user_accounts";
						
						
						rs= smt.executeQuery(query);
						
						while(rs.next()) {
							
							Integer user = rs.getInt("USER_ID");
							Double cashBalance = rs.getDouble("CASH_BALANCE");
							if(paymentSheet.containsKey(user)) {
								
								
								
								paymentSheet.put(user, paymentSheet.get(user)+cashBalance);
								
							}
							
						}
						
						
						
						for(Map.Entry<Integer, Double> g: paymentSheet.entrySet()) {
							
							
							query = "UPDATE user_accounts SET cash_balance='"+g.getValue()+
									"' where user_id='"+g.getKey()+"'";
							smt.executeUpdate(query);
							System.out.println(g.getKey()+" "+g.getValue());
							
							
						}
						
					alreadyPaid=true;	

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {

				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			}

		}

	}

	// Calculates the value of bonds, all Bonds are of one type, 1,000 face value,
	// and have a monthly interest rate calculated by the
	// Monthly Libor Rate.

	public Double getBondValue(String ticker, Long startTime, Date date) {
		Double value = -1000.0;

		Double monthlyInterestRate = Double.parseDouble(ticker.replaceAll("[^0-9.-]", ""));
		Double millisecondInterestRate = (monthlyInterestRate / 100) / 2.592E9;
		;

		Long endTime = date.getTime();

		value *= (1 + ((endTime - startTime) * millisecondInterestRate));

		return value;
	}

	// This function will close the bond, starting with the oldest and thus most
	// expensive bond. If you don't have
	// enough to repay the bond principal plus accrued interest, it will not sell
	// the bond off. Selling the bonds will reduce
	// the user cash.

	public List<String> closeBond(String nickName, Integer number) {

		// List of text output
		List<String> list = new ArrayList<>();

		// Open New connection
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);

		try {

			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
			if (!conn.isClosed()) {

				Statement smt = conn.createStatement();
				Integer userId = getUserId(nickName);
				Double cashBalance = 0.0;
				Double bondTotalValue = 0.0;
				
				if(number<=0) {
					
					list.add("That's an odd lot.");
					return list;
				}
				
				if (userId == 0) {

					list.add("No such user, make a new account by using .openaccount");
					return list;
				}
				// Create the queries
				String query = "";

				// get Cash on Hand Balance

				query = "select cash_balance from user_accounts where user_id= '" + userId + "'";

				ResultSet rs = smt.executeQuery(query);

				while (rs.next()) {

					cashBalance = rs.getDouble("CASH_BALANCE");

				}

				// Calculate bond total value.

				query = "select ticker, starttime, order_id from orders where user_id='" + userId
						+ "' and exchange='BOND' order by starttime asc";

				rs = smt.executeQuery(query);

				// Count up to the number of desired sold items.
				int i = 0;

				List<Integer> orderIdList = new ArrayList<>();

				Date date = new Date();

				while (rs.next() && i < number) {

					String ticker = rs.getString("TICKER");
					Timestamp ts = rs.getTimestamp("starttime");
					Integer orderId = rs.getInt("ORDER_ID");
					// add total of bonds

					bondTotalValue += getBondValue(ticker, ts.getTime(), date);

					// Keep a list of order ids for later transaction
					orderIdList.add(orderId);
					// increase increment of bonds
					i++;

				}

				if (orderIdList.size() == 0) {

					list.add("You do not hold any bonds at the moment.");
					return list;
				}

				if ((cashBalance + bondTotalValue) < 0) {

					list.add("You have insufficent funds to pay bond(s) principal $" + twoDecimal.format(1000 * number)
							+ " with accumulated interest $" + twoDecimal.format(bondTotalValue - (1000 * number))
							+ ". Try selling some assets or increasing your equity. ");
					return list;
				}

				// If suffecient funds are aviablable repay principal
				query = "";

				for (Integer orderId : orderIdList) {

					query += "delete from orders where order_id='" + orderId + "' and user_id='" + userId
							+ "' and exchange='BOND';";

				}
				query += "update user_accounts set cash_balance='" + (cashBalance + bondTotalValue)
						+ "' where user_id='" + userId + "'";

				smt.executeUpdate(query);

				list.add("Paid principal and interest on bonds totalling: $"
						+ twoDecimal.format(Math.abs(bondTotalValue)));

			}

		} catch (Exception e) {

			e.printStackTrace();

		} finally {

			try {
				conn.close();
			} catch (SQLException e) {

				e.printStackTrace();
			}
		}

		return list;

	}

	/// FIXOR
	public void fix() {

		// Open connection
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);

		try {

			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
			if (!conn.isClosed()) {

				Statement smt = conn.createStatement();

				String query = "";
				for (int i = 0; i < 500; i++) {

					query += "insert into orders(USER_ID, TICKER, EXCHANGE, HOLD_METHOD, UNITS, COST,STARTTIME ) values ('"
							+ 10 + "'," // user id
							+ "'LIBOR" + 0.18 + "'," // ticker name
							+ "'BOND'," // Exchange
							+ "'SHORT'," // Hold method
							+ "'1'," // Number
							+ "'-1000'," // Cost, negative 1000 dollars in this case because it's debt
							+ "now()" // timestamp
							+ ");";

				}

				smt.executeUpdate(query);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	// This function opens a bond position with par value of -1,000 dollars, and
	// deposits +1,000 dollars into the user account.
	// I made a separate function for opening a short bond position because this is
	// an internal product and the way you call it
	// is different from the way you call other instruments, namely (.open 3 bond )
	// to open 3 new bonds or borrow 3,000 dollars.
	// This function has a lot of stylized features, and a lot of assumptions have
	// been made, such has never having to pay the principle back.
	// The bare bones are there however.

	public List<String> openBond(String nickName, Integer number) {

		// List of text output
		List<String> list = new ArrayList<>();

		// Open connection
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);

		try {

			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
			if (!conn.isClosed()) {

				Statement smt = conn.createStatement();
				Integer userId = getUserId(nickName);
				Double cashBalance = 0.0;
				DownloadData d = new DownloadData();
				Double libor = d.stock.getLibor()[0];

				// If we didn't get the libor set the libor to something else.
				if (libor == 0.0) {

					System.out.println("Was unable to get libor. ");

					libor = .25;
				}

				if (number >=0) {

					list.add("That's an odd lot.");
					return list;

				}

				if (userId == 0) {

					list.add("User does not exist, you have to create a new account with .open account.");

					return list;
				}

				// Create the queries
				String query = "";

				// Count bond amount to see if it exceeds the borrowing limit.

				query = "select ticker from orders where user_id='" + userId + "' and exchange='BOND';";

				ResultSet rs = smt.executeQuery(query);

				Integer bondCount = 0;
				while (rs.next()) {
					bondCount++;

				}

				if ((bondCount + number) > 1000) {

					list.add("You cannot sell bond amount exceeding $1,000,000. Currently you have $"
							+ twoDecimal.format(number * 1000) + " sold.");
					return list;
				}

				// get Cash on Hand Balance

				query = "select cash_balance from user_accounts where user_id= '" + userId + "'";

				rs = smt.executeQuery(query);

				while (rs.next()) {

					cashBalance = rs.getDouble("CASH_BALANCE");

				}

				// Add bonds individually because each will have it's own maturity dates.
				query = "";
				for (int i = 0; i < number; i++) {

					query += "insert into orders(USER_ID, TICKER, EXCHANGE, HOLD_METHOD, UNITS, COST,STARTTIME ) values ('"
							+ userId + "'," // user id
							+ "'LIBOR" + libor + "'," // ticker name
							+ "'BOND'," // Exchange
							+ "'SHORT'," // Hold method
							+ "'1'," // Number
							+ "'-1000'," // Cost, negative 1000 dollars in this case because it's
							+ "now()" // timestamp
							+ ");";

				}

				// Add a cash to the cash account from number of sold Bonds.

				cashBalance += (number * 1000.0);

				query += "update user_accounts set cash_balance='" + cashBalance + "' where user_id ='" + userId + "';";

				// Execute order
				smt.executeUpdate(query);

				list.add("You sold: " + number + " $1000 par value bonds at (variable) monthly LIBOR rate of: " + libor
						+ ". Cash Proceedings totaled: $" + twoDecimal.format(number * 1000) + ".");

				System.out.println(list.get(0));

			}
		} catch (Exception e) {
			// Print any errors
			e.printStackTrace();
		} finally {

			try {
				conn.close();
			} catch (SQLException e) {

				e.printStackTrace();
			}
		}
		return list;

	}

	public List<String> getLimit(String nickName) {

		List<String> list = new ArrayList<>();

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);

		try {

			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
			if (!conn.isClosed()) {

				Statement smt = conn.createStatement();
				Integer userId = getUserId(nickName);
				if (userId == 0) {

					return list;
				}

				String query = "select " + "orders.ticker, " + "orders.units, " + "orders.cost, " + "orders.stop_loss, "
						+ "orders.take_profit, " + "orders.exchange, " + "orders.hold_method" + "  " + "from orders "
						+ "inner join user_accounts  " + "on orders.user_id=user_accounts.user_id "
						+ "where user_nickname='" + nickName + "' AND " + "units!='0.0' AND  "
						+ "stop_loss!='0.0' OR take_profit!='0.0'";

				ResultSet rs = smt.executeQuery(query);
				String ticker = "";
				Double units = 0.0;
				Double cost = 0.0;
				Double stopLoss = 0.0;
				Double takeProfit = 0.0;
				String exchange = "";
				String type = "";

				list.add(" Ticker  Value  Percentage  Stop-Loss  Take-Profit");
				list.add("====================================================");
				while (rs.next()) {

					ticker = rs.getString("TICKER");
					units = rs.getDouble("UNITS");
					cost = rs.getDouble("COST");
					takeProfit = rs.getDouble("TAKE_PROFIT");
					stopLoss = rs.getDouble("STOP_LOSS");
					type = rs.getString("HOLD_METHOD");
					exchange = rs.getString("EXCHANGE");

					Double percentage = 0.0;
					Object value[] = null;
					if (type.contentEquals("long")) {
						Double costBasis = cost / units;
						value = getPositionValue(ticker, exchange);
						percentage = (((Double) value[0] / costBasis) - 1) * 100;

					}
					if (type.contentEquals("short")) {

						Double costBasis = cost / units;
						value = getPositionValue(ticker, exchange);
						percentage = ((costBasis / (Double) value[0]) - 1) * 100;

					}

					list.add(ticker.toUpperCase() + "   " + formatter.format((Double) value[0]) + "  "
							+ formatter.format(percentage) + "%   " + formatter.format(stopLoss) + "%   "
							+ formatter.format(takeProfit) + "%  ");
				}

			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		return list;

	}

	public String unSetLimit(String nickName, String tickerName, String type) {

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);

		try {

			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
			if (!conn.isClosed()) {

				Statement smt = conn.createStatement();
				Integer userId = getUserId(nickName);
				Double stopLoss = 0.0;
				Double takeProfit = 0.0;
				Integer orderId = 0;
				if (userId == 0) {

					return "User not found. Open a New Account.";
				}

				String query = "select orders.order_id, orders.take_profit, orders.stop_loss from orders"
						+ " inner join user_accounts on " + "user_accounts.user_id = orders.user_id "
						+ "where orders.ticker='" + tickerName + "' and orders.hold_method ='" + type
						+ "' and user_accounts.user_nickname='" + nickName + "'";

				ResultSet rs = smt.executeQuery(query);

				while (rs.next()) {

					takeProfit = rs.getDouble("TAKE_PROFIT");
					stopLoss = rs.getDouble("STOP_LOSS");
					orderId = rs.getInt("ORDER_ID");

				}
				if (takeProfit == 0.0 && stopLoss == 0.0) {

					return "There are no limits set for this position.";
				}

				query = "update orders set stop_loss ='" + 0.0 + "', take_profit ='" + 0.0 + "' where order_id='"
						+ orderId + "'";

				smt.executeUpdate(query);

			}
		} catch (Exception e) {

			e.printStackTrace();

		}

		return "Limits unset on: " + tickerName;
	}

	public String setLimit(String nickName, String tickerName, String type, Double stopLoss, Double takeProfit) {
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);

		try {

			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
			if (!conn.isClosed()) {

				Statement smt = conn.createStatement();

				Integer userId = getUserId(nickName);
				Double units = 0.0;
				Integer orderId = 0;

				if (userId == 0) {

					return "User not found. Open a New Account.";
				}

				String query = "select orders.order_id, orders.units from orders" + " inner join user_accounts on "
						+ "user_accounts.user_id = orders.user_id " + "where orders.ticker='" + tickerName
						+ "' and orders.hold_method ='" + type + "' and user_accounts.user_nickname='" + nickName + "'";

				ResultSet rs = smt.executeQuery(query);

				while (rs.next()) {

					units = rs.getDouble("UNITS");
					orderId = rs.getInt("ORDER_ID");
				}

				if (units == 0.0) {

					return "You currently don't hold this position. Check .getportfolio <nickname> to see your positions.";

				}

				query = "update orders set stop_loss ='" + stopLoss + "', take_profit ='" + takeProfit
						+ "' where order_id='" + orderId + "'";

				smt.executeUpdate(query);

			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return "Limit Order Added for " + tickerName;
	}

	public boolean addNewAccount(String nickname, Double cash) {

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);

		try {
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
			if (!conn.isClosed()) {

				String checkQuery = "select * from user_accounts where user_nickname ='" + nickname + "'";
				Statement smt = conn.createStatement();
				ResultSet rs = smt.executeQuery(checkQuery);
				if (rs.next()) {

					return false;
				}

				String insertQuery = "insert into user_accounts(user_nickname, cash_balance )values ('" + nickname
						+ "','" + cash + "');";

				smt.executeUpdate(insertQuery);

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				conn.close();
			} catch (SQLException e) {

				e.printStackTrace();
			}
		}

		return true;

	}

	public Connection restart() {
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);

		try {
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
			if (!conn.isClosed()) {
				System.out.println("Connected to database");

				try {

					String comm = "drop table user_accounts;";
					comm += "drop table orders;";

					Statement smt = conn.createStatement();
					smt.executeUpdate(comm);

					System.out.println("Dropping user_accounts");
					System.out.println("Dropping positions");

				} catch (Exception e) {

					e.printStackTrace();
				}
				try {

					String comm = "create table user_accounts (" + " USER_ID serial primary key, "
							+ "USER_NICKNAME varchar(40) not null," + " CASH_BALANCE decimal not null" + " );";

					comm += "create table orders (" + " ORDER_ID serial primary key," + " USER_ID integer not null,"
							+ " TICKER varchar(40) not null," + " EXCHANGE varchar(40) not null,"
							+ " HOLD_METHOD varchar(40) not null," + " UNITS decimal not null,"
							+ " COST decimal not null," + " STOP_LOSS decimal null," + " TAKE_PROFIT decimal null"
							+ ");";

					Statement smt = conn.createStatement();
					smt.executeUpdate(comm);

					System.out.println("Creating user_accounts");
					System.out.println("Creating orders");

				} catch (Exception e) {
					e.printStackTrace();

				}

				finally {

					conn.close();
				}

			}
		} catch (Exception e) {

			e.printStackTrace();

		} finally {

			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return conn;

	}

	public String openPosition(String nickName, Integer number, String tickerName, String exchange, String type,
			Integer elapsedTime, Double cost) {
		Connection conn = null;
		Statement smt = null;
		ResultSet rs = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);
		String message = "";
		try {

			Double value = cost;
			System.out.println("");
			System.out.println("Test Coot 2 "+value);
			System.out.println("");
			try {
				Thread.sleep(2000);
			}catch(Exception e) {
				
			}
			if(value==0.0) {
				value =(Double) getPositionValue(tickerName, exchange)[1]; 
				System.out.println("");
				System.out.println("Test Coot 3 "+value);
				System.out.println("");
				try {
					Thread.sleep(2000);
				}catch(Exception e) {
					
				}
				}
			

			String query = "";
			if (value != 0.0) {

				int userId = getUserId(nickName);
				Double balance = getCashBalance(nickName);
				if (userId == 0) {
					message = "User not in the system, create new account by typing .openaccount";

				}

				if(number <0) {
					
					message = "Negative Number Entered.";
				}
				else if ((value * number) > balance) {

					System.out.println(value * number + " " + getCashBalance(nickName));
					message = "Not enough money in bank to process this order.";

				}

				else {

					conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
					if (!conn.isClosed()) {

						// see if order exists

						query = "select orders.units, orders.cost, orders.order_id from orders inner join user_accounts on user_accounts.user_id = orders.user_id where orders.ticker ='"
								+ tickerName + "' AND orders.hold_method='" + type
								+ "' and user_accounts.user_nickname='" + nickName + "'";

						smt = conn.createStatement();
						rs = smt.executeQuery(query);

						Double orderAmount = 0.0;
						Double orderCost = 0.0;
						Integer orderId = 0;
						while (rs.next()) {

							orderAmount = rs.getDouble("UNITS");
							orderCost = rs.getDouble("COST");
							orderId = rs.getInt("ORDER_ID");
						}

						if (orderId == 0) {
							System.out.println("New Order");
							orderAmount = (double) number;
							orderCost = value * number;
							query = "insert into orders(USER_ID, TICKER, EXCHANGE, HOLD_METHOD, UNITS, COST, ELAPSED_TIME,STARTTIME ) values ('"
									+ userId + "','" + tickerName + "','" + exchange + "','" + type + "', '"
									+ orderAmount + "', '" + orderCost + "','"+elapsedTime+"','now()') ;";

						} else {

							System.out.println("Order Already Exists");
							orderAmount += number;
							orderCost += (value * number);

							query = "UPDATE orders SET COST ='" + orderCost + "', UNITS ='" + orderAmount
									+ "' WHERE order_id = '" + orderId + "' ;";

						}

						query += "update user_accounts set cash_balance='" + (balance - (value * number))
								+ "' where user_nickname='" + nickName + "';";

						smt.executeUpdate(query);
						message = "Order processed. " + number + " positions of " + tickerName + " opened at " + value
								+ ".";

					} else {

						message = "Connection very surial.";

					}
				}

			} else {

				message = "Ticker not found at this moment.";

			}
		} catch (Exception e) {

			e.printStackTrace();
			message = "Something went wrong.";
		} finally {

			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(smt);
			DbUtils.closeQuietly(conn);

		}

		return message;

	}

	public String closePosition(String nickName, Integer number, String tickerName, String exchange, String type) {
		int positionNum = 0;
		
		if(number<0) {
			
			return "Invalid negative number of shares/crypto entered.";
		}

		Double value = (Double) getPositionValue(tickerName, exchange)[0];

		if (value == 0.0) {

			return "Order failed try again later.";
		}

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);
		try {
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
			if (!conn.isClosed()) {

				String query = "select orders.order_id, orders.cost, orders.units from orders"
						+ " inner join user_accounts on user_accounts.user_id = orders.user_id"
						+ " where  orders.ticker='" + tickerName + "' and user_accounts.user_nickname='" + nickName
						+ "' and orders.hold_method= '" + type + "' and orders.exchange ='" + exchange + "';";
				Statement smt = conn.createStatement();
				ResultSet rs = smt.executeQuery(query);
				Double returnOnInvestment = 0.0;
				query = "";

				Integer orderId = 0;
				Double orderCost = 0.0;
				Double orderUnits = 0.0;
				while (rs.next()) {

					orderId = rs.getInt("ORDER_ID");
					orderCost = rs.getDouble("COST");
					orderUnits = rs.getDouble("UNITS");
				}

				if (orderUnits < number) {

					return "You only have " + orderUnits + " " + tickerName + ".";
				} else if (orderUnits == 0.0) {

					return "You don't own this position.";
				}

				Double costBasis = (orderCost / orderUnits) * number;

				if((orderUnits - number)!=0.0) {
				query = "UPDATE orders SET units ='" + (orderUnits - number) + "',cost='" + (orderCost - costBasis)
						+ "' WHERE order_id ='" + orderId + "' ;";

				}else {
					query="DELETE FROM orders WHERE order_id='"+orderId+"';";
				}
				if (type.contentEquals("long")) {

					returnOnInvestment = value * number;

				} else if (type.contentEquals("short")) {
					returnOnInvestment = costBasis + (costBasis - (value * number));

				}

				System.out.println(getCashBalance(nickName) + "  " + returnOnInvestment);
				query += "UPDATE user_accounts SET cash_balance ='" + (getCashBalance(nickName) + returnOnInvestment)
						+ "' WHERE user_nickname ='" + nickName + "';";
				smt.executeUpdate(query);

			}
		} catch (Exception e) {

			e.printStackTrace();

			return "Something went wrong.";
		} finally {

			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return "Order processed. " + number + " positions closed at $" + formatter.format(value) + ".";

	}

	public Integer getUserId(String nickName) {

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);
		Integer userId = 0;
		try {
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);

			if (!conn.isClosed()) {
				String comm = "select user_id from user_accounts where user_nickname='" + nickName + "'";
				Statement smt = conn.createStatement();
				ResultSet rs = smt.executeQuery(comm);

				while (rs.next()) {
					userId = rs.getInt("USER_ID");

				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		} finally {

			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return userId;
	}

	public Double getCashBalance(String nickName) {

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);
		Double cash = 0.0;
		try {
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);

			if (!conn.isClosed()) {
				String comm = "select cash_balance from user_accounts where user_nickname='" + nickName + "'";
				Statement smt = conn.createStatement();
				ResultSet rs = smt.executeQuery(comm);

				while (rs.next()) {
					cash = rs.getDouble("CASH_BALANCE");

				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		} finally {

			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return cash;
	}

	public Object[] getPositionValue(String ticker, String exchange) {

		
			DownloadData d = new DownloadData();
		
		Object value[] = { 0.0, 0.0 };
		System.out.println(ticker);
		System.out.println(exchange);
		Random r = new Random();
		Double random = r.nextGaussian();
try{
		if (exchange.contentEquals("equity")) {

			Object data[] = d.stock.getPrice(ticker.toUpperCase());

			 if ((Double)data[0]!=0.0) {
				value[0] = (Double) data[0] * Math.exp(((random * 0.001) - 0.0001));//bid
				value[1] = (Double) data[0] * Math.exp(((random * 0.001) + 0.0001));//ask
				
			}

		} else if (exchange.contentEquals("crypto")) {

			Object data[] = d.stock.getCryptoAPI(ticker);
			if ((Double) data[0] != 0.0) {
				value[0] = (Double) data[0] * Math.exp(((random * 0.001) - 0.0001));
				value[1] = (Double) data[0] * Math.exp(((random * 0.001) + 0.0001));
			}
		}

		}catch(Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	public List<Record> getListOfOpenTrades(String nickName) {

		List<Record>records = new ArrayList<>();

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);

		int userId = getUserId(nickName);
		if (userId == 0) {

		

			return records;
		}

		try {
			
			

			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
			if (!conn.isClosed()) {
				System.out.println("Connected to database");


				try {

					String query = "select  units, cost, ticker, hold_method, exchange, elapsed_time from orders where user_id='"+userId+"'";

					Statement smt = conn.createStatement();
					ResultSet rs = smt.executeQuery(query);
                   
					while (rs.next()) {

						if (rs.getDouble("UNITS") != 0.0 && !rs.getString("TICKER").contains("LIBOR")) {

							Double units =  rs.getDouble("UNITS");
							String ticker = rs.getString("TICKER");
							String type = rs.getString("HOLD_METHOD");
							String exchange = rs.getString("EXCHANGE");
							int elapsed    = rs.getInt("ELAPSED_TIME");
							Double cost = rs.getDouble("COST");
							Double currentPrice =(Double) getPositionValue(ticker,exchange)[0];
									
									
							Record record = new Record();
							
							record.setCost(cost);
							record.setCurrentPrice(currentPrice);
							record.setElapsedTime(elapsed);
							record.setUnits(units);
							record.setTicker(ticker);
							record.setType(type);
							record.setExchange(exchange);
							
							records.add(record);

						}

					}

				} catch (Exception e) {
					e.printStackTrace();

				}
				
			}
		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		

		return records;

	}

	public List<String> getPortfolio(String nickName) {

		HashMap<String, Double> tickerPrice = new HashMap<String, Double>();
		Double marketValue = 0.0;
		List<String> port = new ArrayList<>();
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);

		int userId = getUserId(nickName);
		if (userId == 0) {

			List<String> reply = new ArrayList<>();
			reply.add("User not found.");

			return reply;
		}

		try {

			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
			if (!conn.isClosed()) {
				System.out.println("Connected to database");

				try {
					System.out.println(nickName);
					String query = "select orders.ticker , orders.exchange, orders.units " + "from orders "
							+ "inner join user_accounts " + "on user_accounts.user_id = orders.user_id  "
							+ "where user_accounts.user_nickname='" + nickName + "'";

					Statement smt = conn.createStatement();

					ResultSet rs = smt.executeQuery(query);

					while (rs.next()) {

						String ticker = rs.getString("TICKER");
						if (tickerPrice.get(ticker) == null && rs.getDouble("UNITS") != 0.0) {

							tickerPrice.put(ticker, (Double) getPositionValue(ticker, rs.getString("EXCHANGE"))[0]);
						}

					}
					port.add("Ticker   Qty   Cost Basis   Cur Price   Percentage  ");// % of Portfolio");
					port.add("====================================================");
					port.add(" ");
					port.add("long                           ");
					port.add("----------------------------------------------------");

					for (Map.Entry<String, Double> entry : tickerPrice.entrySet()) {

						query = "select orders.units, orders.cost " + "from orders " + "inner join user_accounts "
								+ "on user_accounts.user_id = orders.user_id  " + "where user_accounts.user_nickname='"
								+ nickName
								+ "' and orders.hold_method ='long' and orders.units != '0.0' and orders.ticker='"
								+ entry.getKey() + "'";

						rs = smt.executeQuery(query);
						Double orderCost = 0.0;
						Double quantity = 0.0;
						while (rs.next()) {

							quantity = rs.getDouble("UNITS");
							orderCost = rs.getDouble("COST");
						}

						Double costBasis = (orderCost / quantity);
						if (quantity != 0.0) {
							port.add(entry.getKey().toUpperCase() + "   " + quantity + "   "
									+ formatter.format(costBasis) + "   " + formatter.format(entry.getValue()) + "    "
									+ formatter.format(((entry.getValue() / costBasis) - 1) * 100) + "%"
							// + " (TOTAL VALUE:"+(orderCost + (orderCost - (entry.getValue() *
							// quantity)))+")"
							);
							marketValue += (entry.getValue() * quantity);
						}
					}

					port.add(" ");
					port.add("short                          ");
					port.add("----------------------------------------------------");

					for (Map.Entry<String, Double> entry : tickerPrice.entrySet()) {

						query = "select orders.units, orders.cost " + "from orders " + "inner join user_accounts "
								+ "on user_accounts.user_id = orders.user_id  " + "where user_accounts.user_nickname='"
								+ nickName
								+ "' and orders.hold_method ='short' and orders.units != '0.0' and orders.ticker='"
								+ entry.getKey() + "'";

						rs = smt.executeQuery(query);
						Double orderCost = 0.0;
						Double quantity = 0.0;
						while (rs.next()) {

							quantity = rs.getDouble("UNITS");
							orderCost = rs.getDouble("COST");
						}

						Double costBasis = (orderCost / quantity);
						if (quantity != 0.0) {

							port.add(entry.getKey().toUpperCase() + "   " + quantity + "   "
									+ formatter.format(costBasis) + "   " + formatter.format(entry.getValue()) + "    "
									+ formatter.format(((costBasis / entry.getValue()) - 1) * 100) + "%"
							// + " (TOTAL VALUE:"+(orderCost + (orderCost - (entry.getValue() *
							// quantity)))+")"
							);

							marketValue += orderCost + (orderCost - (entry.getValue() * quantity));
						}
					}

					query = "select ticker, starttime from orders where user_id ='" + userId + "' and exchange='BOND'";

					rs = smt.executeQuery(query);
					Double bondTotalValue = 0.0;
					List<String> tickers = new ArrayList<>();
					Date date = new Date();
					while (rs.next()) {

						String ticker = rs.getString("TICKER");
						Timestamp ts = rs.getTimestamp("STARTTIME");
						bondTotalValue += getBondValue(ticker, ts.getTime(), date);
						tickers.add(ticker);

					}

					if (tickers.size() != 0) {

						port.add(" ");
						port.add("debt                          ");
						port.add("----------------------------------------------------");

						port.add("LIBOR BOND " + tickers.size() + "   -1000.0   "
								+ twoDecimal.format(bondTotalValue / tickers.size()) + "  "
								+ formatter.format(1 - (tickers.size() * -1000) / bondTotalValue) + "%");

					}

					query = "select user_accounts.cash_balance  from user_accounts where user_nickname ='" + nickName
							+ "'";
					rs = smt.executeQuery(query);
					Double cash = 0.0;
					while (rs.next()) {

						cash = rs.getDouble("CASH_BALANCE");

					}
					port.add(" ");
					port.add("-----------------------------------------------------");

					port.add("Cash Balance: $ " + twoDecimal.format(cash) + "            ");// (TOTAL VALUE:"+cash+")");
					port.add("=====================================================");
					port.add("Total Debt: $(" + twoDecimal.format(bondTotalValue) + ")");
					port.add("Total Asset: $" + twoDecimal.format(cash + marketValue));
					port.add("=====================================================");
					port.add("Total Equity: $ " + twoDecimal.format(cash + marketValue + bondTotalValue));
					port.add(Double.toString(cash + marketValue + bondTotalValue));

				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return port;
	}

	public static HashMap<String, Double> sortByValue(HashMap<String, Double> hm) {

		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(hm.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, Double> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}
		return temp;
	}
	
	public void updateElapsedTime(String nickName) {
		
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);
		
		
		int userId = getUserId(nickName);
		if (userId == 0) {

			List<String> reply = new ArrayList<>();
			reply.add("User not found.");

			return;
		}
		

		try {
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
			if (!conn.isClosed()) {
				
				String sql="update orders set elapsed_time=elapsed_time+1 where user_id='"+userId+"'";
				Statement smt = conn.createStatement();
				smt.executeUpdate(sql);
				
				
			}}catch (Exception e) {

				e.printStackTrace();

			} finally {

				try {
					conn.close();
				} catch (SQLException e) {

					e.printStackTrace();
				}
			}
		
	}

	public List<String> getScore() {

		List<String> scores = new ArrayList<>();
		HashMap<String, Double> userValue = new HashMap<String, Double>();

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", user);
		connectionProps.put("password", passwd);

		try {
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", connectionProps);
			if (!conn.isClosed()) {

				String query = "select user_nickname from user_accounts";

				Statement smt = conn.createStatement();

				ResultSet rs = smt.executeQuery(query);

				while (rs.next()) {

					String nickname = rs.getString("USER_NICKNAME");
					List<String> portfolio = getPortfolio(nickname);
					userValue.put(nickname, Double.parseDouble(portfolio.get(portfolio.size() - 1)));

				}
				HashMap<String, Double> sort = sortByValue(userValue);

				Integer rank = 0;
				for (Map.Entry<String, Double> entry : sort.entrySet()) {
					rank++;

					String line = rank + ".";
					for (int i = 0; i < (3 - Integer.toString(rank).length()); i++) {

						line += " ";
					}

					line += entry.getKey();

					for (int i = 0; i < (16 - entry.getKey().length()); i++) {

						line += " ";
					}
					line += "$" + twoDecimal.format(entry.getValue());
					scores.add(line);

				}

			}

		} catch (Exception e) {

			e.printStackTrace();

		} finally {

			try {
				conn.close();
			} catch (SQLException e) {

				e.printStackTrace();
			}
		}

		return scores;
	}

}

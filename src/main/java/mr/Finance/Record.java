package mr.Finance;

import java.sql.Timestamp;
import java.util.Calendar;
import java.time.temporal.ChronoUnit;

public class Record {

	String ticker;
	String nickname;
	Double units;
	Double cost;
	Double currentPrice;
	Double takeProfit;
	Double stopLoss;
	String type;
	String exchange;
	Timestamp timestamp;
	int elapsedTime;
	Double signal; 

	public Record() {
		this.ticker = "";
		this.nickname = "";
		this.units = 0.0;
		this.cost = 0.0;
		this.takeProfit = 0.0;
		this.stopLoss = 0.0;
		this.type = "";
		this.exchange = "";
		this.currentPrice=0.0;
		elapsedTime=0;

	}

	public Record(String ticker, String nickname, Double units, Double cost, Double takeProfit, Double stopLoss,
			String type, String exchange) {
		super();
		this.ticker = ticker;
		this.nickname = nickname;
		this.units = units;
		this.cost = cost;
		this.takeProfit = takeProfit;
		this.stopLoss = stopLoss;
		this.type = type;
		this.exchange = exchange;
	}
	
	public void setSignal(Double signal) {
		this.signal = signal;
	}

	public void setElapsedTime(int time) {
		this.elapsedTime = time;
	}

	public void setTimeStamp(Timestamp ts) {
		this.timestamp = ts;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Double getUnits() {
		return units;
	}

	public void setUnits(Double units) {
		this.units = units;
	}



	public void setCost(Double cost) {
		this.cost = cost;
	}

	public Double getTakeProfit() {
		return takeProfit;
	}

	public void setTakeProfit(Double takeProfit) {
		this.takeProfit = takeProfit;
	}

	public Double getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(Double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public void setCurrentPrice(Double currentPrice) {
		this.currentPrice = currentPrice;
	}

	public Double getDifference() {
		
		
		
		if(type.equals("long")) {
		  return currentPrice / (cost / units);
		}
		else {
		  return (cost / units)/currentPrice  ;
		}
	}
	
	public Double getCost() {
		
		Double unitCost=0.0;
		Double testCost=(cost/units);
		
		
		if(!testCost.isNaN()&&!testCost.isInfinite()) {
			System.out.println("");
			System.out.println("Test Coot 1 "+testCost);
			System.out.println("");
			try {
				Thread.sleep(2000);
			}catch(Exception e) {
				
			}
			unitCost=testCost;
		}
		
		return unitCost; 
	}

	public long getElapsedDays() {

		return elapsedTime;

	}
	
	public Double getSignal() {
		
		return signal;
	}
	
    @Override
    public String toString() {
        return " [name=" + ticker + ", signal=" + signal + "]";
    }

}

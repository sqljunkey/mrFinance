package mr.Finance;

public class Record {
	
	String ticker;
	String nickname;
	Double units;
	Double cost;
	Double takeProfit;
	Double stopLoss;
	String type;
	String exchange;
	
	public Record() {
		this.ticker = "";
		this.nickname = "";
		this.units = 0.0;
		this.cost = 0.0;
		this.takeProfit = 0.0;
		this.stopLoss = 0.0;
		this.type = "";
		this.exchange ="";
		
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
	public Double getCost() {
		return cost;
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
	
	
	
	
	
	
}

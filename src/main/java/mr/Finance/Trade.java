package mr.Finance;

import java.util.Calendar;

public class Trade {
	
	String ticker = "";
	Double signal=0.0;
	Double openPrice = 0.0;
	Double currentPrice =0.0;
	Double quantity =0.0;
	Calendar startDate = Calendar.getInstance();

	
	public Trade(String ticker, Double signal) {
		this.ticker = ticker;
		this.signal=signal;
		
	}	
	
	public void setTime() {
		
	}
	
	public Double getQuantity() {
		return quantity;
	}
	
	public void setQuantity(Double quantity) {
		this.quantity=quantity;
	}
	
	public void setOpenPrice(Double price) {
		this.openPrice=price;
	}
	
	public void setCurrentPrice(Double price) {
		this.currentPrice=price;
	}
	
	public Double getDifference() {
		
		return openPrice/currentPrice;
	}
	
	String getTicker() {
		return ticker;
	}
	
	Double getSignal() {
		return signal;
	}
	
    @Override
    public String toString() {
        return "Date [name=" + ticker + ", signal=" + signal + "]";
    }
	
	
	
	

}

package mr.Finance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Account {
	
	String nickname;
	Double cash; 
	List<Stock> portfolio = new ArrayList<>();
	
	
	public Account(String nickname, Double cash){
		
		this.nickname = nickname;
		this.cash = cash;
	}
	
	
	public Account(String nickname, Double cash, List<Stock> portfolio){
		
		this.nickname = nickname;
		this.cash = cash;
		this.portfolio = portfolio;
	}
	
	
	public String getNickname() {
		return nickname;
	}



	public Double getCashLeft() {
		return cash;
	}


	public void addCash(Double amount) {
		this.cash += amount;
	}

	public List<Stock> getPortfolio() {
		
		return portfolio;
	}
	public Double getTotalValue() {
		
		Double totalCash = cash;
		
		for(Stock s: portfolio) {
			
			totalCash+=  s.getNowPrice();
		}
		
		
		return totalCash;
	}



//.buy 4 aapl
	public void addStock(Integer number,Stock stock) {
		
		for(int i = 0; i< number; i++) {
			if(cash>=stock.getNowPrice()) {
			portfolio.add(stock);
			}
		}
	}
		
	
	
//.sell 5 aapl
	public void removeStock(Integer number, Stock stock) {
		
		Iterator iter = portfolio.listIterator();
		
		while(iter.hasNext()) {
			
			Stock s = (Stock)iter.next();
			
			if(s.getName().equals(stock.getName()) && (number-1)>0) {
				
				cash+= stock.getNowPrice();
				iter.remove();
				number --;
			}
			
			
			
		}
		
		
	}

}

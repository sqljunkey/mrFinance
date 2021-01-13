package mr.Finance;

public class WinrateRecord {
	Double winrate = 0.0;
	Double sampleSize = 0.0;
	String tickerName = "";
	
	
	public WinrateRecord(String tickerName,Double winrate, Double sampleSize ) {
		super();
		this.winrate = winrate;
		this.sampleSize = sampleSize;
		this.tickerName = tickerName;
	}


	public Double getWinrate() {
		return winrate;
	}


	public Double getSampleSize() {
		return sampleSize;
	}


	public String getTickerName() {
		return tickerName;
	}
	
	
	
	
	
	
	
}

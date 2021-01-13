package mr.Finance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class DownloadedData {

	String ticker;
	List<Double> adjustedPrices = new ArrayList<>();

	public DownloadedData(String ticker, Calendar from, Calendar to) {
		this.ticker = ticker;

		DownloadData d = new DownloadData();

		try {

			adjustedPrices = d.stock.getHistorical(ticker, from, to);

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	public Integer getDataSize() {

		return adjustedPrices.size();

	}
	
	public String getName() {
		
		return ticker;
	}

	public double[] getLogReturns() {

		List<Double> returns = new ArrayList<>();
		
		
		for (int i = 1; i < adjustedPrices.size(); i++) {
			
			returns.add(Math.log(adjustedPrices.get(i - 1) / adjustedPrices.get(i)));
			
		}
		
		Double[] ds = returns.toArray(new Double[returns.size()]);
		
		double[] d = ArrayUtils.toPrimitive(ds);
		
		
		return d;

	}

}

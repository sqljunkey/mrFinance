package mr.Finance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

public class HistoricalData {

	DownloadData d = new DownloadData();

	String ticker;
	List<Double> adjustedPrices = new ArrayList<>();

	List<Double> scores = new ArrayList<>();
	Boolean active = false;
	Double boughtPrice = 0.0;
	Double change = 0.0;

	public HistoricalData(String ticker) {

		this.ticker = ticker;
		Double price = (Double) d.stock.getPrice(ticker)[0];
		if (price.equals(0.0)) {

			price = 1.0;
		}

		// Random r = new Random();

		// this.adjustedPrices.add(r.nextGaussian());
		// this.adjustedPrices.add(r.nextGaussian());
		this.adjustedPrices.add(price);
		this.adjustedPrices.add(price);

	}

	void addScore(Double score) {

		scores.add(score);
		if (scores.size() > 20) {

			scores.remove(0);
		}
	}

	Double getWinRate() {

		Double wins = scores.stream().mapToDouble(f -> f.doubleValue()).sum();

		return wins / scores.size();

	}

	void pushData() {
		// Copy Last
		// Random r = new Random();
		Double price = adjustedPrices.get(adjustedPrices.size() - 1);
		adjustedPrices.clear();
		// this.adjustedPrices.add(r.nextGaussian());
		adjustedPrices.add(price);
		Double newPrice = (Double) d.stock.getPrice(ticker)[0];
		if (newPrice.equals(0.0)) {

			newPrice = price;
		}
		// this.adjustedPrices.add(r.nextGaussian());
		adjustedPrices.add(newPrice);

	}

	double getTotalReturn() {

		return adjustedPrices.get(adjustedPrices.size() - 1) / adjustedPrices.get(0) - 1;

	}

	double[] getReturns() {

		List<Double> returns = new ArrayList<>();

		Double previous = adjustedPrices.get(0);

		for (int i = 1; i < adjustedPrices.size(); i++) {

			returns.add(Math.log(previous / adjustedPrices.get(i)));
		}

		Double[] data = returns.toArray(new Double[returns.size()]);

		return ArrayUtils.toPrimitive(data);
	}

	public boolean getStatus() {

		return active;

	}

	public void decativate() {

		active = false;

	}

	public void activate() {

		active = true;
	}

	public Double getPercentageChange() {

		Double percentage = 0.0;

		percentage = Math.log(adjustedPrices.get(0) / adjustedPrices.get(1));

		if (percentage.isNaN() || percentage.isInfinite()) {

			percentage = 0.0;
		}

		return percentage;
	}

	public Double getPercentageAbsoluteChange() {

		Double percentage = 0.0;

		percentage = Math.abs(Math.log(adjustedPrices.get(0) / adjustedPrices.get(1)));

		if (percentage.isNaN() || percentage.isInfinite()) {

			percentage = 0.0;
		}

		return percentage;
	}

}

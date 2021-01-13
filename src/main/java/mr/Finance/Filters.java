package mr.Finance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class Filters {
	// Co-Variance Analysis get top most uncorrelated stocks with the SP500

	List<HistoricalData> sortByReturn(List<HistoricalData> data) {
		List<HistoricalData> newData = data;

		Collections.sort(newData, new Comparator<HistoricalData>() {
			@Override
			public int compare(HistoricalData lhs, HistoricalData rhs) {
				// -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
				return lhs.getPercentageChange() > rhs.getPercentageChange() ? 1
						: (lhs.getPercentageChange() < rhs.getPercentageChange()) ? -1 : 0;
			}
		});

		return newData;

	}

	List<HistoricalData> sortByAbsoluteReturn(List<HistoricalData> data) {
		List<HistoricalData> newData = data;

		Collections.sort(newData, new Comparator<HistoricalData>() {
			@Override
			public int compare(HistoricalData lhs, HistoricalData rhs) {
				// -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
				return lhs.getPercentageAbsoluteChange() > rhs.getPercentageAbsoluteChange() ? -1
						: (lhs.getPercentageAbsoluteChange() < rhs.getPercentageAbsoluteChange()) ? 1 : 0;
			}
		});

		return newData;

	}

	List<HistoricalData> marketCorrelation(List<HistoricalData> data, Double threshold) {

		List<HistoricalData> newData = new ArrayList<>();
		PearsonsCorrelation p = new PearsonsCorrelation();
		HistoricalData SP = new HistoricalData("spy");

		for (HistoricalData stock : data) {

			try {
				Double corr = p.correlation(SP.getReturns(), stock.getReturns());

				System.out.println("Correlation: " + stock.ticker + " : " + corr);

				if (corr < threshold) {

					newData.add(stock);

				}
			} catch (Exception e) {

				e.printStackTrace();
			}

		}

		return newData;

	}

	// Get Change

	List<HistoricalData> getChange(List<HistoricalData> data, Double threshold) {
		List<HistoricalData> newData = new ArrayList<>();

		for (HistoricalData stock : data) {

			Double totalPercentageChange = stock.getPercentageChange();

			if (totalPercentageChange > threshold) {

				System.out.println("Total Change: " + stock.ticker + " : " + totalPercentageChange);

				newData.add(stock);

			}
		}

		return newData;

	}

	// Get total Return per period

	List<HistoricalData> getTotalReturn(List<HistoricalData> data, Double threshold) {

		List<HistoricalData> newData = new ArrayList<>();

		for (HistoricalData stock : data) {

			Double totalReturn = stock.getTotalReturn();

			if (totalReturn > threshold) {

				System.out.println("Total Return: " + stock.ticker + " : " + totalReturn);

				newData.add(stock);

			}

		}

		return newData;

	}

}

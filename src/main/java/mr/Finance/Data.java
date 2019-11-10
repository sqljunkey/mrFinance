package mr.Finance;

public class Data {

	String ticker;
	String functionType;
	Object[] data;
	public Data(String ticker, String functionType, Object[] data) {
		super();
		this.ticker = ticker;
		this.functionType = functionType;
		this.data = data;
	}
	public String getTicker() {
		return ticker;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	public String getFunctionType() {
		return functionType;
	}
	public void setFunctionType(String functionType) {
		this.functionType = functionType;
	}
	public Object[] getData() {
		return data;
	}
	public void setData(Object[] data) {
		this.data = data;
	}
	
	

}

package mr.Finance;

public class App {

	public static void main(String[] args) throws Exception {

		Communicator c = new Communicator();

		c.addChannel("#materia");
		c.addChannel("##economics");
		c.addChannel("##investments");
		c.addChannel("#bitcoin-pricetalk");
		c.addChannel("##econometrix");

		c.start();

		for (Trader t : c.t) {
			t.startTradeTimers();
		}
	}

}
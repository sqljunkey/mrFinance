package mr.Finance;



public class App {

	public static void main(String[] args) throws Exception {

		Communicator c = new Communicator();

		c.addServerName("irc.libera.chat");
		c.addChannel("#materia");

		// pscp mrFinance.jar mrfinance@45.79.31.219:/home/mrfinance/mrF/mrFinance.jar
		c.start();

		for (Trader t : c.t) {
			t.startTradeTimers();
		}
	}

}
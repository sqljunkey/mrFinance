package mr.Finance;

public class App {

	public static void main(String[] args) throws Exception {

		Communicator c = new Communicator();

		c.addChannel("#materia");
		c.addChannel("##economics");

		c.addChannel("#bitcoin-pricetalk");
		c.addChannel("##austrians");
		c.addChannel("##econometrix");
		c.addChannel("##bitcoin");

		c.start();

	}

}
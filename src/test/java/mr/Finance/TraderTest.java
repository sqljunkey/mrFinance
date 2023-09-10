package mr.Finance;

import static org.junit.Assert.*;

import java.util.List;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

public class TraderTest {
	
	@Test
	void testTraderDeleteSavedSignals() {
		
		Trader trader = createDefaultTestTrader();
		
		trader.deleteStoredSignals();
		
		List<Record> record = trader.loadSignals(); 
		
		assertTrue(record.isEmpty());
		
			
		
		
	}
	
	
	@Test
	void testTraderSaveSignals() {
		
		
		Trader trader = createDefaultTestTrader();

		trader.storeSignals(trader.getSignals());
		
		List<Record> record = trader.loadSignals(); 
		
		
		assertTrue(!record.isEmpty());
		
				
		
	}
	
	@Test
	void testCloseTradeExecution() {
		
		Trader trader = createDefaultTestTrader();
		
		trader.trade();
		trader.closeAllTrades();
		
		assertTrue(trader.am.getListOfOpenTrades("mrfinance").isEmpty());
		
		
	}
	
	
	@Test
	void testOpenTradeExecution() {
		
		Trader trader = createDefaultTestTrader();
		
		trader.trade();
		
		assertTrue(!trader.am.getListOfOpenTrades("mrfinance").isEmpty());
		
		
	}
	
	@Test
	
	void testTradingLock() {
		
		Trader trader = createDefaultTestTrader();
		trader.trade();
		
		assertTrue(trader.isTradingLocked());
		
		
		
	}

	private Trader createDefaultTestTrader() {
		AccountManager am = new AccountManager();
		Trader trader = new Trader(am);
		trader.setSleepTime(0);
	    trader.setDirectory("");
	    trader.setInformation(0.0, "bad.lst"); 
	    
	    trader.setClock(trader.clock.getTime().getHour(),
	    		trader.clock.getTime().getHour(),
	    		trader.clock.getTime().getDayOfWeek().getValue());
		return trader;
	}
	

	
	

	

}

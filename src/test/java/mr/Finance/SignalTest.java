package mr.Finance;
import static org.junit.Assert.*;


import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SignalTest {
	
	@Test
	
	void testPositiveVolatility() {
		
		Signal s = new Signal();
		
		List<Record> records = s.getPositiveVolatility("bad.lst",0.0);
		
		for(Record record : records) {
			
			assertTrue(record.getSignal()>0);
			
		}
		
	}
	
	@Test
	
	void testNegativeVolatility() {
		
		Signal s = new Signal();
		
		List<Record> records = s.getNegativeVolatility("bad.lst",0.0);
		
		for(Record record : records) {
			
			assertTrue(record.getSignal()<0);
			
		}
		
	}
	
	
	@Test
	
	void testListVolatilityNotEmpty() {
		
		Signal s = new Signal();
		
		List<Record> records = s.getNegativeVolatility("bad.lst",0.0);
		
		
			
			assertTrue(!records.isEmpty());
			
		
		
	}

}

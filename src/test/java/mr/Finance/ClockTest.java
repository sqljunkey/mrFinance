package mr.Finance;

import static org.junit.Assert.*;


import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

public class ClockTest {
	
	@Test
	void testUpdateTime() {
		
		Clock clock = new Clock();
		ZonedDateTime zdt=clock.getTime();
		
		int start = zdt.getHour();
		int end=start+7;
		int workWeek = zdt.getDayOfWeek().getValue();
		
		clock.setBeginHour(start);
		clock.setClosingHour(end);
		clock.setWorkWeek(workWeek);
		
		assertTrue(clock.isUpdateTime());
		
	}
	
	@Test
	void testWeekend() {
		
		Clock clock = new Clock();
			
		assertFalse(clock.isDuringWeek());
		
	}
	
	@Test
	void isNotWeekend() {
		
		Clock clock = new Clock();
		ZonedDateTime zdt=clock.getTime();
		
		int workWeek = zdt.getDayOfWeek().getValue();
		clock.setWorkWeek(workWeek);
		assertTrue(clock.isDuringWeek());
		
	}
	
	
	

}

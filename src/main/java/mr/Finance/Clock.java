package mr.Finance;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Clock {
	
	
	int beginHour =7;
	int closingHour=15;
	int workWeek=5;
	
	
	void setClosingHour(int hour) {
		
		this.closingHour = hour;
	}
	
	void setBeginHour(int hour) {
		
		this.beginHour = hour;
	}
	
	void setWorkWeek(int day) {
		
		this.workWeek=day;
	}
	
	  ZonedDateTime getTime() {

		ZoneId zoneId = ZoneId.of("America/Chicago");
		ZonedDateTime time = ZonedDateTime.now(zoneId);

		
		return time;
	}

	 Boolean isUpdateTime() {

       
		return (getTime().getHour() >= beginHour && getTime().getHour() < closingHour &&  isDuringWeek());

	}

	 Boolean isOpenTime() {

		
		return (getTime().getHour() == beginHour  &&  isDuringWeek());

	}

	 Boolean isClosingTime() {

	
		
		return (getTime().getHour() == closingHour  &&  isDuringWeek());

	}
	
	 Boolean isDuringWeek() {
		
		
		return (getTime().getDayOfWeek().getValue() <= workWeek);
	}

	 Boolean isUnlockTime() {


		return !isClosingTime() && !isOpenTime() && isDuringWeek();

	}
	
	 public void printTime() {


		
	
		System.out.println("Time inside runner: " 
		+  getTime().getHour() + ":" 
		+ getTime().getMinute() + " day: " 
		+getTime().getDayOfWeek().getValue());

	}
	

	
	
	

}

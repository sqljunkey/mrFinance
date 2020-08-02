package mr.Finance;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimeDeamon
{
   private  Runnable dailyTask;
   private int hour;
   private int minute;
   private int second;
   private String runThreadName;

   public void DailyRunnerDaemon(Calendar timeOfDay, Runnable dailyTask, String runThreadName)
   {
      this.dailyTask = dailyTask;
      this.hour = timeOfDay.get(Calendar.HOUR_OF_DAY);
      
      this.minute = timeOfDay.get(Calendar.MINUTE);
      this.second = timeOfDay.get(Calendar.SECOND);
      this.runThreadName = runThreadName;
   }

   public void start()
   {
      startTimer();
   }
   private Date getNextRunTime()
   {
      Calendar startTime = Calendar.getInstance();
      Calendar now = Calendar.getInstance();
      startTime.set(Calendar.HOUR_OF_DAY, hour);
      startTime.set(Calendar.MINUTE, minute);
      startTime.set(Calendar.SECOND, second);
      startTime.set(Calendar.MILLISECOND, 0);

      if(startTime.before(now) || startTime.equals(now))
      {
         startTime.add(Calendar.DATE, 1);
      }

      return startTime.getTime();
   }

 private void startTimer()
   {
      new Timer(runThreadName, true).schedule(new TimerTask()
      {
         @Override
         public void run()
         {
        	System.out.println("Starting "+ runThreadName+ " TimeDeamon.");
            
        	dailyTask.run();
            startTimer();
         }
      }, getNextRunTime());
   }



}
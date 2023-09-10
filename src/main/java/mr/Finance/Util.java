package mr.Finance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Util {
	
	  public static List<String> getStockListFromFile(String list) {
	        FileInputStream fstream = null;

	        List<String> resetList = new ArrayList<>();
	        try {  
	            fstream = new FileInputStream(list);
	            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

	            String strLine;


	            while ((strLine = br.readLine()) != null) {

	                resetList.add(strLine);
	            }
	            fstream.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return resetList;
	    }
	 
	  public static Double parseNumberWithLetter(String number){

	        Double value = 0.0;

	        try{
	        if(number.contains("T")){

	            value=Double.parseDouble(number.replaceAll("[^0-9.-]", ""))*1E12;
	        }
	       else if(number.contains("B")){

	            value=Double.parseDouble(number.replaceAll("[^0-9.-]", ""))*1E9;
	        }
	        else if(number.contains("M")){

	            value=Double.parseDouble(number.replaceAll("[^0-9.-]", ""))*1E6;
	        }
	       else if(number.contains("K")){

	            value=Double.parseDouble(number.replaceAll("[^0-9.-]", ""))*1E3;
	        }else{

	            value=Double.parseDouble(number.replaceAll("[^0-9.-]", ""));
	        }}catch(Exception e){
	            e.printStackTrace();
	        }


	        return value;
	    }
	  
	  
	  
	  public static boolean deleteFile(String filename) {

		  boolean result = false;
		  File file = new File(filename);
		  try {
			result = Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		  return result;
	  }
	  
	  
	  public static void writeToFile(String file, String line) {
		  File myObj = new File(file);
		  try {
          if (myObj.createNewFile()) {
	            System.out.println("File created: " + myObj.getName());
	          } else {
	            System.out.println("File already exists.");
	          }
          
          
		  
		  }catch(Exception e) {
			  e.printStackTrace();
		  }
		  
		  try {

			  FileWriter myWriter = new FileWriter(file,true);
			  myWriter.write(line+"\n");
			  myWriter.close();
			  
			  }catch(Exception e) {
				  e.printStackTrace();
			  }
		  
		  
		  
	  }
	  
	  public static String getFileInString(String file) {
	        FileInputStream fstream = null;

	       String resetList = "";
	        try {  // Open the file
	            fstream = new FileInputStream(file);
	            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

	            String strLine;


	            while ((strLine = br.readLine()) != null) {

	                resetList+=" "+strLine;
	            }
	            fstream.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return resetList;
	    }

}

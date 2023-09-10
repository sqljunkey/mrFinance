package mr.Finance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class Index {
	
	
	
	String first="<!DOCTYPE html>\r\n" + 
			"<html lang=\"en\">\r\n"
			+ "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.9.4/Chart.js\"></script>" + 
			"<head>\r\n" + 
			"<meta charset=\"UTF-8\">\r\n" + 
			"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + 
			"<meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\r\n" + 
			"<title>Finance Research Data</title>\r\n" + 
			"<style type=\"text/css\">\r\n" + 
			"    body {\r\n" + 
			"        background-color: #f5f5f5;\r\n" + 
			"        margin-top: 8%;\r\n" + 
			"        color: #5d5d5d;\r\n" + 
			"        font-family: -apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,\"Helvetica Neue\",Arial,\"Noto Sans\",sans-serif,\"Apple Color Emoji\",\"Segoe UI Emoji\",\"Segoe UI Symbol\",\"Noto Color Emoji\";\r\n" + 
			"        text-shadow: 0px 1px 1px rgba(255,255,255,0.75);\r\n" + 
			"        text-align: center !important;\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    h1 {\r\n" + 
			"        font-size: 2.45em;\r\n" + 
			"        font-weight: 700;\r\n" + 
			"        color: #5d5d5d;\r\n" + 
			"        letter-spacing: -0.02em;\r\n" + 
			"        margin-bottom: 30px;\r\n" + 
			"        margin-top: 30px;\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    .container {\r\n" + 
			"        width: 100%;\r\n" + 
			"        margin-right: auto;\r\n" + 
			"        margin-left: auto;\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    .animated {\r\n" + 
			"        -webkit-animation-duration: 1s;\r\n" + 
			"        animation-duration: 1s;\r\n" + 
			"        -webkit-animation-fill-mode: both;\r\n" + 
			"        animation-fill-mode: both;\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    .fadeIn {\r\n" + 
			"        -webkit-animation-name: fadeIn;\r\n" + 
			"        animation-name: fadeIn;\r\n" + 
			"    }\r\n" + 
			"    \r\n" + 
			"    .info {\r\n" + 
			"        color:#5594cf;\r\n" + 
			"        fill:#5594cf;\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    .error {\r\n" + 
			"        color:#c92127;\r\n" + 
			"        fill:#c92127;\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    .warning {\r\n" + 
			"        color:#ffcc33;\r\n" + 
			"        fill:#ffcc33;\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    .success {\r\n" + 
			"        color:#5aba47;\r\n" + 
			"        fill:#5aba47;\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    .icon-large {\r\n" + 
			"        height: 132px;\r\n" + 
			"        width: 132px;\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    .description-text {\r\n" + 
			"        color: #707070;\r\n" + 
			"        letter-spacing: -0.01em;\r\n" + 
			"        font-size: 1.25em;\r\n" + 
			"        line-height: 20px;\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    .footer {\r\n" + 
			"        margin-top: 40px;\r\n" + 
			"        font-size: 0.7em;\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    .delay-1s {\r\n" + 
			"        -webkit-animation-delay: 1s;\r\n" + 
			"        animation-delay: 1s;\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    @keyframes fadeIn {\r\n" + 
			"        from { opacity: 0; }\r\n" + 
			"        to   { opacity: 1; }\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"</style>\r\n" + 
			"</head>\r\n" + 
			"<body>\r\n" + 
			"<div class=\"container text-center\">\r\n" + 
			"    <div class=\"row\">\r\n" + 
			"        <div class=\"col\">\r\n" + 
			"            <div class=\"animated fadeIn\">\r\n" + 
			"               <h1>Launsdorf</h1> "+
			"            </div>\r\n" ; 
			 ; 
	
	String second="            <div class=\"description-text animated fadeIn delay-1s\">\r\n";

			
	String third=		"<p align=\"center\"><canvas id=\"myChart\" style=\"width:100%;max-width:700px align=\"right\"\"></canvas>\r\n</p>";
			
	String forth=		 "               <section class=\"footer\"><strong>Domain:</strong> launsdorf.com</section>\r\n" + 
			"            </div>\r\n" + 
			"        </div>\r\n" + 
			"    </div>\r\n" + 
			"</div>\r\n" + 
			"</body>\r\n" + 
			"</html>\r\n" + 
			"";;
			
	
	public String chart() {
		
		
		
	List<String> prices = Util.getStockListFromFile("data.file");
	
	String script="";

	if(!prices.isEmpty()){
	String yValues="[";
	String xValues="[";
	int i=0;
	
	Double max=0.0;
	Double min=Double.MAX_VALUE;
	
	for(String price: prices) {
	   i++;
		xValues+=i+",";
		yValues+=price+",";
		
		Double value = Double.parseDouble(price);
		
		if(value>max) {
			max = value;
		}
		if(value<min) {
			min = value;
		}
		
		
	}	
	
	xValues.subSequence(0, xValues.length()-1);
	yValues.subSequence(0, yValues.length()-1);
	
	yValues+="]";
	xValues+="]";
	
	script+="<script>\r\n" + 
			"var xValues = "+xValues+";\r\n" + 
			"var yValues = "+yValues+";\r\n" + 
			"\r\n" + 
			"new Chart(\"myChart\", {\r\n" + 
			"  type: \"line\",\r\n" + 
			"  data: {\r\n" + 
			"    labels: xValues,\r\n" + 
			"    datasets: [{\r\n" + 
			"      fill: false,\r\n" + 
			"      lineTension: 0,\r\n" + 
			"      backgroundColor: \"rgba(0,0,255,1.0)\",\r\n" + 
			"      borderColor: \"rgba(0,0,255,0.1)\",\r\n" + 
			"      data: yValues\r\n" + 
			"    }]\r\n" + 
			"  },\r\n" + 
			"  options: {\r\n" + 
			"    legend: {display: false},\r\n" + 
			"    scales: {\r\n" + 
			"      yAxes: [{ticks: {min: "+min+", max:"+max+"}}],\r\n" + 
			"    }\r\n" + 
			"  }\r\n" + 
			"});\r\n" + 
			"</script>";
		
	}
		
		return script;
		
	}

	
	
	public void store(Double main,List<String> list, String file,String s,String a) {
		
		DecimalFormat fte = new DecimalFormat("###,###,##0.00");
		
	      
	      try {
	    	  
	          File myObj = new File(file);
	          File myObj2 = new File("data.file");
	          if (myObj.createNewFile()) {
	            System.out.println("File created: " + myObj.getName());
	          } else {
	            System.out.println("File already exists.");
	          }
	          
	          if (myObj2.createNewFile()) {
		            System.out.println("File created: " + myObj2.getName());
		          } else {
		            System.out.println("File already exists.");
		          }
	        
	        
	        List<String> prices = Util.getStockListFromFile("data.file");
	        prices = prices.subList(Math.max(0, prices.size()-200),  prices.size());
	        prices.add(main+"");
	        
	        
	    	FileWriter myOtherWriter = new FileWriter("data.file");
			for(String price: prices) {
			  myOtherWriter.write(price+"\n");
			}
			myOtherWriter.close();
			
			
	    	FileWriter myWriter = new FileWriter(file);
			myWriter.write(first+"\r\n");
			myWriter.write("<p></p>");
			myWriter.write(second);
			myWriter.write("<p1> Simulated Balance: $"+fte.format(main)+"</p>");
			myWriter.write("<p> "+s+"</p1>\r\n");
			myWriter.write("<p> "+a+"</p1>\r\n");
			myWriter.write("<p></p1>\r\n");
			myWriter.write("<p></p1>\r\n");
			myWriter.write("<p>_________________________________________________</p1>\r\n");
			
			for(String line: list) {
				myWriter.write("<p>"+line+"</p>");
			}
			
			
			
			myWriter.write(third);
			myWriter.write(chart());
			myWriter.write(forth);
			myWriter.close();
			
			
			

			
			
			
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
		
		
		
		
		
		
		
		
	}
	

}

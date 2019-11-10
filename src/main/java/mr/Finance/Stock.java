package mr.Finance;

public class Stock {
	String name;
	String type;
	
	DownloadData d;
	
	Stock (DownloadData d, String name, String type){
		
		this.d = d;
		this.name = name;
		this.type = type;
		
		
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	

	public Double getNowPrice() {
		
		

		return 0.0;
	}

}

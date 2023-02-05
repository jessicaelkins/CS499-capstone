package application;

public abstract class Sensor {
	private String name;
	private String status;
	
	Sensor(String name, String status) {
		this.name = name;
		this.status = status;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
}

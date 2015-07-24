package it.paololatella.aws.demo.apigateway;

public class Devices {

	public Devices (String a)
	{
		address = a;
		type = "null";
		model = "null";
	}
	
	public Devices (String a, String t, String m)
	{
		address = a;
		type = t;
		model = m;
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}
	
	public String toString(char delimiter)
	{
		return(address+delimiter+type+delimiter+model);
	}

	private String address;
	private String type;
	private String model;

}

package application;

import javafx.beans.property.SimpleStringProperty;

public class Visit
{
	private SimpleStringProperty hour;
	private SimpleStringProperty bookedStatus;
	private SimpleStringProperty name;
	
	public Visit(String hour, String bookedStatus, String name)
	{
		this.hour = new SimpleStringProperty(hour);
		this.bookedStatus = new SimpleStringProperty(bookedStatus);
		this.name = new SimpleStringProperty(name);
	}
	
	public String getHour()
	{
		return hour.get();
	}
	
	public String getBookedStatus()
	{
		return bookedStatus.get();
	}
	
	public String getName()
	{
		return name.get();
	}
	
	
	public void bookVisit(String bookedStatus)
	{
		this.bookedStatus = new SimpleStringProperty(bookedStatus);
	}
}

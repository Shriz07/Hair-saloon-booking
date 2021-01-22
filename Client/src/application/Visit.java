package application;

import javafx.beans.property.SimpleStringProperty;

public class Visit
{
	private SimpleStringProperty hour;
	private SimpleStringProperty bookedStatus;
	
	public Visit(String hour, String bookedStatus)
	{
		this.hour = new SimpleStringProperty(hour);
		this.bookedStatus = new SimpleStringProperty(bookedStatus);
	}
	
	public String getHour()
	{
		return hour.get();
	}
	
	public String getBookedStatus()
	{
		return bookedStatus.get();
	}
	
	public void setBookedStatus(String bookedStatus)
	{
		this.bookedStatus = new SimpleStringProperty(bookedStatus);
	}
}

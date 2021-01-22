package application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ClientAppController 
{
	@FXML
	private ListView<String> log;
	
	@FXML
	private Button book;
	
	@FXML
	private Button unbook;
	
	@FXML
	private TableView<Visit> table;
	
	@FXML
	private TableColumn<Visit, String> hourColumn;
	
	@FXML
	private TableColumn<Visit, String> bookedColumn;
	
	private ClientBackend clientBackend;
	
	private List<Visit> schedule = new ArrayList<Visit>();
	
	public void logAdd(String message)
	{
		GregorianCalendar time = new GregorianCalendar();
		String timeString = "[" + time.get(GregorianCalendar.HOUR) + ":" + time.get(GregorianCalendar.MINUTE) + ":" + time.get(GregorianCalendar.SECOND) + "] -";
		String result = timeString + " " + message;	
		log.getItems().add(0, result);
	}
	
	public void setData(ClientBackend clientBackend)
	{
		this.clientBackend = clientBackend;
		hourColumn.setCellValueFactory(new PropertyValueFactory<>("Hour"));
		bookedColumn.setCellValueFactory(new PropertyValueFactory<>("BookedStatus"));
	}
	
	public void bookVisit(ActionEvent event)
	{
		try
		{
			Visit visit = table.getSelectionModel().getSelectedItem();
			if(visit == null)
			{
				logAdd("Please select hour");
				return;
			}
			logAdd("Trying to book visit at " + visit.getHour());
			clientBackend.bookVisit(visit);
		}
		catch(IOException e)
		{
			logAdd("Failed to book visit");
		}
	}
	
	public void cancelVisit(ActionEvent event)
	{
		try
		{
			Visit visit = table.getSelectionModel().getSelectedItem();
			if(visit == null)
			{
				logAdd("Please select hour");
				return;
			}
			if(visit.getBookedStatus() == "No")
			{
				logAdd("Hour is available");
				return;
			}
			logAdd("Trying to cancel visit at " + visit.getHour());
			clientBackend.cancelVisit(visit);
		}
		catch(IOException e)
		{
			logAdd("Failed to cancel visit");
		}
	}
	
	public void refreshDisplayedVisits()
	{
		schedule = clientBackend.getSchedule();
		ObservableList<Visit> items = FXCollections.observableArrayList(schedule);
		table.setItems(items);
		table.refresh();
	}
}

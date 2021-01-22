package application;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ServerAppController 
{
	@FXML
	private ListView<String> log;
	
	@FXML 
	private TableView<Visit> table;
	
	@FXML
	private TableColumn<Visit, String> hourColumn;
	
	@FXML
	private TableColumn<Visit, String> bookedColumn;
	
	@FXML
	private TableColumn<Visit, String> nameColumn;
	
	private ServerBackend serverBackend;
	
	private List<Visit> schedule = new ArrayList<Visit>();
	
	public void setData(ServerBackend serverBackend)
	{
		this.serverBackend = serverBackend;
		this.schedule = serverBackend.getSchedule();
		hourColumn.setCellValueFactory(new PropertyValueFactory<>("Hour"));
		bookedColumn.setCellValueFactory(new PropertyValueFactory<>("BookedStatus"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));
	}
	
	public void logAdd(String message)
	{
		GregorianCalendar time = new GregorianCalendar();
		String timeString = "[" + time.get(GregorianCalendar.HOUR) + ":" + time.get(GregorianCalendar.MINUTE) + ":" + time.get(GregorianCalendar.SECOND) + "] -";
		String result = timeString + " " + message;
		log.getItems().add(0, result);
	}
	
	public void refreshDisplayedSchedule()
	{
		schedule = serverBackend.getSchedule();
		ObservableList<Visit> items = FXCollections.observableArrayList(schedule);
		table.setItems(items);
		table.refresh();
	}
}

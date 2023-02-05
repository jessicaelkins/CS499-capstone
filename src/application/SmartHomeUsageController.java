package application;

import java.sql.Statement;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class SmartHomeUsageController implements Initializable{
	
	private Scene firstScene;
	private Scene thirdScene;
	@FXML
	private Button homeButton;
	@FXML
	private Button diagnosticsButton;
	@FXML
	private ComboBox<String> monthComboBox;
	@FXML
	private Button selectMonthButton;
	@FXML
	private NumberAxis x;
	@FXML
	private NumberAxis y;
	@FXML
	private LineChart<?,?> usageChart;
	@FXML
	private Button refreshTableButton;
	@FXML
	private Button refreshGraphButton;

	
	@FXML
    private TableView<DatabaseTable> usageTable;
	@FXML
	private TableColumn<DatabaseTable, String> monthColumn;
	@FXML
	private TableColumn<DatabaseTable, Double> wattageColumn;
	@FXML
	private TableColumn<DatabaseTable, Double> gallonsColumn;
	@FXML
	private TableColumn<DatabaseTable, Double> costColumn;
	
	ObservableList<String> monthList = FXCollections.observableArrayList("February", "March", "April");
	
	// sets the home screen
	public void setHomeScene(Scene scene) {
		firstScene = scene;
	}

	// sets the diagnostics screen
	public void setDiagnosticsScene(Scene scene) {
		thirdScene = scene;
	}

	// event listener for home button that sets the scene to home page
	public void homeButtonPressed(ActionEvent actionEvent) {
		Stage primaryStage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
		primaryStage.setScene(firstScene);
	}
	
	// event listener for diagnostics button that sets the scene to diagnostics page
	public void diagnosticsButtonPressed(ActionEvent actionEvent) {
		Stage primaryStage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
		primaryStage.setScene(thirdScene);
	}
	
	// event listener for refresh button that updates the table values
	public void refreshTableButtonPressed(ActionEvent actionEvent) throws SQLException {
		usageTable.setItems(getData());
	}
	
	// event listener for refresh button that updates the graph
	public void refreshGraphButtonPressed(ActionEvent actionEvent) throws SQLException {
		String month = monthComboBox.getValue();
		
		if (month == null) {
			System.out.println("Please select a month");
		} else if (month == "April") {
			// generate graph for April
			createMonthGraph(4, 30);
		} else if (month == "March") {
			// generate graph for March
			createMonthGraph(3, 31);
		} else {
			// month would be February
			// generate graph for February
			createMonthGraph(2, 28);
		}
	}
	
	// create the line graph that shows monthly usage
	public void createMonthGraph(int monthNumber, int days) throws SQLException {
		usageChart.getData().clear();
		
		int i = 1;
		
		// getting the current day
		Calendar calendar = Calendar.getInstance();
		int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
		
		// arrays to store the cost from electricity and water
		long[] costArr1 = new long[days];
		long[] costArr2 = new long[days];
		
		// creating the series
		XYChart.Series electricity = new XYChart.Series();
		XYChart.Series water = new XYChart.Series();
		XYChart.Series waterEst = new XYChart.Series();
		XYChart.Series cost = new XYChart.Series();
		XYChart.Series costEst = new XYChart.Series();
		XYChart.Series electricityEst = new XYChart.Series();
		
		// creating the lists to store the data points in
		List<XYChart.Series> elecList = new ArrayList<>();
		List<XYChart.Series> waterList = new ArrayList<>();
		List<XYChart.Series> waterEstList = new ArrayList<>();
		List<XYChart.Series> costList = new ArrayList<>();
		List<XYChart.Series> costEstList = new ArrayList<>();
		List<XYChart.Series> elecEstList = new ArrayList<>();
		
		
		// naming the lines
		electricity.setName("Electricity");
		water.setName("Water");
		
		// adding x-axis constraints
		x.setAutoRanging(false);
		x.setLowerBound(1);
		x.setUpperBound(days);
		x.setTickUnit(1);
		x.setLabel("Day");
		
		// adding y-axis constraints
		y.setLabel("Dollars/Kilowatts/Gallons*");
		y.setAutoRanging(true);
		y.setTickUnit(5);
		
		// querying the database for electricity usage 
		String sqlQuery = String.format("SELECT * FROM electricity_bill WHERE CAST (start_date as CHAR) LIKE '%d%%' ORDER BY start_date", monthNumber);
		Statement s = Main.c.createStatement();
		ResultSet queryResult = s.executeQuery(sqlQuery);
		long kilowatts = 0;
		
		// loop that adds electricity to the graph up to the current day
		while(i != (currentDay + 1)) {
			queryResult.next();
			kilowatts = queryResult.getLong("kilowatts");
			costArr1[i - 1] = queryResult.getLong("total_amount");
			
			electricity.getData().add(new XYChart.Data(i,kilowatts));
			i++;
		} 

		// add the current day to estimated line so there is not a gap in the line
		electricityEst.getData().add(new XYChart.Data(i - 1, kilowatts));
		
		// adds electricity from tomorrows date until the end of month
		while(queryResult.next()) {
			kilowatts = queryResult.getLong("kilowatts");
			costArr1[i - 1] = queryResult.getLong("total_amount");
			electricityEst.getData().add(new XYChart.Data(i, kilowatts));
			i++;
		}
		
		// closing the query thread
		queryResult.close();
		
		// querying the database for water usage
		String sqlQuery2 = String.format("SELECT * FROM water_bill WHERE CAST (start_date as CHAR) LIKE '%d%%' ORDER BY start_date", monthNumber);
		Statement s2 = Main.c.createStatement();
		ResultSet queryResult2 = s2.executeQuery(sqlQuery2);
		i = 1;
		int gallons = 0;
		
		// going through each row that resulted from the query and adding the gallons to the graph up to the current day
		while(i != (currentDay + 1)) {
			queryResult2.next();
			gallons = (queryResult2.getInt("gallons"))/100;
			costArr2[i - 1] = queryResult2.getLong("amount");
			
			water.getData().add(new XYChart.Data(i,gallons));
			i++;
		} 

		// add the current day to estimated line so there is not a gap in the line	
		waterEst.getData().add(new XYChart.Data(i - 1, gallons));
		
		// adds water from tomorrows date until the end of month
		while(queryResult2.next()) {
			gallons = (queryResult2.getInt("gallons"))/100;
			costArr2[i - 1] = queryResult2.getLong("amount");
			waterEst.getData().add(new XYChart.Data(i, gallons));
			i++;
		}
		
		// closing the query thread
		queryResult2.close();
		
		
		long totalCost = costArr1[currentDay - 1] + costArr2[currentDay - 1];
		
		costEst.getData().add(new XYChart.Data(currentDay, totalCost));
		
		// loop that adds the elec and water cost together
		// and adds it to the graph
		for(int n = 0; n < days; n++) {
			// adding the electric and water cost
			totalCost = costArr1[n] + costArr2[n];
			
			// if the month is not April, dont need estimate data
			if(monthNumber != 4) {
				cost.getData().add(new XYChart.Data(n + 1, totalCost));
			} else {
				if(n > currentDay - 1) {
					// adding cost to estimated line if n is greater than yesterday's date
					costEst.getData().add(new XYChart.Data(n + 1, totalCost));
				} else {
					cost.getData().add(new XYChart.Data(n + 1, totalCost));
				}
			}
		}
		
		// adding the data points to the series
		elecList.add(electricity);
		waterList.add(water);
		waterEstList.add(waterEst);
		costList.add(cost);
		costEstList.add(costEst);
		elecEstList.add(electricityEst);
		
		
		// adding the series to the graph
		usageChart.getData().add(elecList.get(elecList.size() - 1));
		usageChart.getData().add(waterList.get(waterList.size() - 1));
		usageChart.getData().add(waterEstList.get(waterEstList.size() - 1));
		usageChart.getData().add(costList.get(costList.size() - 1));
		usageChart.getData().add(costEstList.get(costEstList.size() - 1));
		usageChart.getData().add(elecEstList.get(elecEstList.size() - 1));
		
		usageChart.setLegendVisible(false);

		if(monthNumber == 4) {
			// changing the electricity line to red
			Set<Node> elecNodes = usageChart.lookupAll(".series" + 0);
					
			// iterating through each node in the set
			for(Node node : elecNodes) {
				node.setStyle("-fx-stroke: #ff0000;\n" + "-fx-background-color: #ff0000, white;");
			}
			
			// changing the electricity estimate line
			Set<Node> elecEstNodes = usageChart.lookupAll(".series" + 5);
											
			// iterating through each node in the set
			for(Node node : elecEstNodes) {
				node.setStyle("-fx-stroke: #ff0000;\n" + "-fx-background-color: #ff0000, white;\n" + "-fx-stroke-dash-array: 1 1 2 10;");
			}
			
			// changing the water line to blue
			Set<Node> waterNodes = usageChart.lookupAll(".series" + 1);
					
			// iterating through each node in the set
			for(Node node : waterNodes) {
				node.setStyle("-fx-stroke: #1184e8;\n" + "-fx-background-color: #1184e8, white;\n");
			}
			
			// changing the water estimate line
			Set<Node> waterEstNodes = usageChart.lookupAll(".series" + 2);
								
			// iterating through each node in the set
			for(Node node : waterEstNodes) {
				node.setStyle("-fx-stroke: #1184e8;\n" + "-fx-background-color: #1184e8, white;\n" + "-fx-stroke-dash-array: 1 1 2 10;");
			}
			
			// changing the cost line to green
			Set<Node> costNodes = usageChart.lookupAll(".series" + 3);
								
			// iterating through each node in the set
			for(Node node : costNodes) {
				node.setStyle("-fx-stroke: #12d312;\n" + "-fx-background-color: #12d312, white;\n");
			}
			
			// changing the cost line to green
			Set<Node> costEstNodes = usageChart.lookupAll(".series" + 4);
											
			// iterating through each node in the set
			for(Node node : costEstNodes) {
				node.setStyle("-fx-stroke: #12d312;\n" + "-fx-background-color: #12d312, white;\n" + "-fx-stroke-dash-array: 1 1 2 10;");
			}
			
		}else {
			// changing the electricity line to red
			Set<Node> elecNodes = usageChart.lookupAll(".series" + 0);
		
			// iterating through each node in the set
			for(Node node : elecNodes) {
				node.setStyle("-fx-stroke: #ff0000;\n" + "-fx-background-color: #ff0000, white;");
			}
		
			// changing the water line to blue
			Set<Node> waterNodes = usageChart.lookupAll(".series" + 1);
		
			// iterating through each node in the set
			for(Node node : waterNodes) {
				node.setStyle("-fx-stroke: #1184e8;\n" + "-fx-background-color: #1184e8, white;\n");
			}
			
			// changing the water estimate line
			Set<Node> waterEstNodes = usageChart.lookupAll(".series" + 2);
											
			// iterating through each node in the set
			for(Node node : waterEstNodes) {
				node.setStyle("-fx-stroke: #1184e8;\n" + "-fx-background-color: #1184e8, white;\n");
			}
			
			// changing the cost line to green
			Set<Node> costNodes = usageChart.lookupAll(".series" + 3);
											
			// iterating through each node in the set
			for(Node node : costNodes) {
				node.setStyle("-fx-stroke: #12d312;\n" + "-fx-background-color: #12d312, white;\n");
			}
			
			// changing the cost line to green
			Set<Node> costEstNodes = usageChart.lookupAll(".series" + 4);
														
			// iterating through each node in the set
			for(Node node : costEstNodes) {
				node.setStyle("-fx-stroke: #12d312;\n" + "-fx-background-color: #12d312, white;\n" + "-fx-stroke-dash-array: 1 1 2 10;");
			}
			
			// changing the elec estimate line
			Set<Node> elecEstNodes = usageChart.lookupAll(".series" + 5);
														
			// iterating through each node in the set
			for(Node node : elecEstNodes) {
				node.setStyle("-fx-stroke: #ff0000;\n" + "-fx-background-color: #ff0000, white;\n");
			}
		}
	}
	
	// display the graph with the electricity and water usage
	public void selectMonthButtonPressed(ActionEvent actionEvent) throws SQLException {
		String month = monthComboBox.getValue();
		
		if (month == null) {
			System.out.println("Please select a month");
		} else if (month == "April") {
			// generate graph for April
			createMonthGraph(4, 30);
			System.out.println("April was selected");
		} else if (month == "March") {
			// generate graph for March
			createMonthGraph(3, 31);
			System.out.println("March was selected");
		} else {
			// month would be February
			// generate graph for February
			createMonthGraph(2, 28);
			System.out.println("February was selected");
		}
	}
	
	
	//Returns list of data that can be inserted in the table
	public ObservableList<DatabaseTable> getData() throws SQLException {
		// initializing the list that will get inserted into the table
		ObservableList<DatabaseTable> data = FXCollections.observableArrayList();
		
		// querying the database for each months water and electricity usage
		String FEBWsqlQuery = "SELECT * FROM electricity_bill WHERE CAST (start_date as CHAR) LIKE '2%' ORDER BY start_date";
		String FEBGsqlQuery = "SELECT * FROM water_bill WHERE CAST (start_date as CHAR) LIKE '2%' ORDER BY start_date";
		String MARWsqlQuery = "SELECT * FROM electricity_bill WHERE CAST (start_date as CHAR) LIKE '3%' ORDER BY start_date";
		String MARGsqlQuery = "SELECT * FROM water_bill WHERE CAST (start_date as CHAR) LIKE '3%' ORDER BY start_date";
		String APRWsqlQuery = "SELECT * FROM electricity_bill WHERE CAST (start_date as CHAR) LIKE '4%' ORDER BY start_date";
		String APRGsqlQuery = "SELECT * FROM water_bill WHERE CAST (start_date as CHAR) LIKE '4%' ORDER BY start_date";
		
		// obtain database contents for table
		List<Double> totals = TableQuery(FEBWsqlQuery, FEBGsqlQuery, true);
		totals = roundingData(totals);
		// adding the totals for February to the list for the "February" row in the table
		data.add(new DatabaseTable("February", totals.get(0), totals.get(1), totals.get(2)));
		
		totals = TableQuery(MARWsqlQuery, MARGsqlQuery, true);
		totals = roundingData(totals);
		// adding the totals for March to the list for the "March" row in the table
		data.add(new DatabaseTable("March", totals.get(0), totals.get(1), totals.get(2)));
		
		totals = TableQuery(APRWsqlQuery, APRGsqlQuery, false);
		totals = roundingData(totals);
		// adding the totals for April to the list for the "April" row in the table
		data.add(new DatabaseTable("April", totals.get(0), totals.get(1), totals.get(2)));
		
		totals = TableQuery(APRWsqlQuery, APRGsqlQuery, true);
		totals = roundingData(totals);
		// adding the totals for April estimate to the list for the "April (EST)" row in the table
		data.add(new DatabaseTable("April (EST)", totals.get(0), totals.get(1), totals.get(2)));
		
		// returns the list with the row values added
		return data;
	}
	
	// retrieve database contents
	public List<Double> TableQuery(String WsqlQuery, String GsqlQuery, boolean fullmonth) throws SQLException {
		List<Double> totals = Arrays.asList(0.0, 0.0, 0.0);
		double w = 0.0;
		double g = 0.0;
		double c = 0.0;	
		
		// obtains data for the incomplete month Ex. April
		if (fullmonth == false) {
			// obtain data from electricity_bill
			Statement tw = Main.c.createStatement();
			ResultSet queryResultw = tw.executeQuery(WsqlQuery);
			
			Calendar calendar = Calendar.getInstance();
			int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
			int d = 1;
			
			while(d < (currentDay + 1)) {
				queryResultw.next();
				Double kilowatts = queryResultw.getDouble("kilowatts");
				w = w + kilowatts;
				Double wcost = queryResultw.getDouble("total_amount");
				c = c + wcost;
				d++;
			} 
			totals.set(0, w);
			queryResultw.close();
		
			// obtain data from water_bill
			Statement tg = Main.c.createStatement();
			ResultSet queryResultg = tg.executeQuery(GsqlQuery);
			d = 1;
			
			while(d < (currentDay + 1)) {
				queryResultg.next();
				Double gallons = queryResultg.getDouble("gallons");
				g = g + gallons;
				Double gcost = queryResultg.getDouble("amount");
				c = c + gcost;
				d++;
			} 
			totals.set(1, g);
			totals.set(2, c);
			queryResultg.close();
		}
		// obtains data for the complete months including the prediction for April
		else {
			// obtain data from electricity_bill
			Statement tw = Main.c.createStatement();
			ResultSet queryResultw = tw.executeQuery(WsqlQuery);
			
			while(queryResultw.next()) {
				Double kilowatts = queryResultw.getDouble("kilowatts");
				w = w + kilowatts;
				Double wcost = queryResultw.getDouble("total_amount");
				c = c + wcost;
			} 
			totals.set(0, w);
			queryResultw.close();
		
			// obtain data from water_bill
			Statement tg = Main.c.createStatement();
			ResultSet queryResultg = tg.executeQuery(GsqlQuery);
			while(queryResultg.next()) {
				Double gallons = queryResultg.getDouble("gallons");
				g = g + gallons;
				Double gcost = queryResultg.getDouble("amount");
				c = c + gcost;
			} 
			// adding the total amount of gallons to the set
			totals.set(1, g);
			totals.set(2, c);
			queryResultg.close();
		}
		return totals;
	}
	
	// rounds the data in the table so it is more readable
	public List<Double> roundingData(List<Double> totals) {
		int z = 0;
		double setas;
		while (z <= 2) {
			setas = totals.get(z);
			setas = Math.round(setas*100.0)/100.0;
			totals.set(z, setas);
			z++;
		}
		return totals;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
		// setting default month to April
		this.monthComboBox.getItems().removeAll(monthComboBox.getItems());
		this.monthComboBox.getItems().addAll(monthList);
		this.monthComboBox.getSelectionModel().select("April");

		// initializing the graph to April
		try {
			createMonthGraph(4,30);
		} catch (SQLException e1) {
			System.out.println("Error creating graph.");
		}
		
		// initalizing the table columns
		monthColumn.setCellValueFactory(new PropertyValueFactory<DatabaseTable, String>("month"));
		wattageColumn.setCellValueFactory(new PropertyValueFactory<DatabaseTable, Double>("wattage"));
		gallonsColumn.setCellValueFactory(new PropertyValueFactory<DatabaseTable, Double>("gallons"));
		costColumn.setCellValueFactory(new PropertyValueFactory<DatabaseTable, Double>("cost"));
		
		// fill in data for columns
		try {
			usageTable.setItems(getData());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

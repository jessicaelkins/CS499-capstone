package application;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SmartHomeController implements Initializable {
	private int temperatureCurrent;
	private int temperatureSet;
	private int temperatureOutside;
	private Scene secondScene;
	private Scene thirdScene;
	private Timeline timeline = new Timeline();
	private Timeline timelineOutside = new Timeline();
	private Timeline timelineDoors = new Timeline();
	private Timeline timelineWindows = new Timeline();
	private String farenheight = "°F";
	@FXML
	private Button increaseTemperatureButton;
	@FXML
	private Button decreaseTemperatureButton;
	@FXML
	private Button homeButton;
	@FXML
	private Button diagnosticsButton;
	@FXML
	private Button usageButton;
	@FXML
	private Button allLightsButton;
	@FXML
	private Button allDoorsLockedButton;
	@FXML
	private Button garageDoorOpenButton;
	@FXML
	private Button entertainmentOnButton;
	@FXML
	private TextField temperatureTextField;
	@FXML
	private TextField temperatureSetTextField;
	@FXML
	private TextField temperatureOutsideTextField;
	@FXML
	private Pane pane;
	@FXML
	private StackPane temperaturePane;
	@FXML
	private StackPane currentPane;
	@FXML
	private StackPane setToPane;
	@FXML
	private ImageView imageView;
	@FXML
	private TextArea quickStatusField;
	@FXML
	public Pane lightingOverlay;
	@FXML
	private Rectangle door_toGarage;
	@FXML
	private Rectangle door_front;
	@FXML
	private Rectangle door_garage_1;
	@FXML
	private Rectangle door_garage_2;
	@FXML
	private Rectangle door_back;
	@FXML
	private Label insideLabel;
	@FXML
	private Label outsideLabel;
	@FXML
	private Label setToLabel;
	@FXML
	private Label setLabel;
	@FXML
	public Rectangle app_livingroom_TV;
	@FXML
	public Circle lamp_Livinga;
	@FXML
	public Circle overheadLight_LR;
	@FXML
	public Circle lamp_Livingb;
	@FXML
	public Rectangle app_hvac;

	private double dailyElectricUsage;
	private double dailyWaterUsage;
	private double dailyOverallCost;
	private int temperatureDifference;
	private boolean tempStable;
	private int seconds;
	private int hvacDuration;
	private int hvacOutsideDuration;
	private int hvacDoorsDuration;
	private int hvacWindowsDuration;

	private ArrayList<Rectangle> doors = new ArrayList<>();
	private ArrayList<Rectangle> garageDoors = new ArrayList<>();
	private ArrayList<Shape> entertainment = new ArrayList<>();

	public ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

	// sets the usage scene
	public void setUsageScene(Scene scene) {
		secondScene = scene;
	}

	// sets the diagnostics scene
	public void setDiagnosticsScene(Scene scene) {
		thirdScene = scene;
	}
	
	// sets the current temperature
	public void setCurrentTemperature(int temperature) {
		this.temperatureCurrent = temperature;
		temperatureTextField.setText(String.valueOf(temperatureCurrent) + farenheight);
	}
	
	// sets the temperature difference
	public void setTemperatureDifference(int temperature) {
		this.temperatureDifference = temperature;
	}
	
	// sets the set to temperature and updates label
	public void setSetTemperature(int temperature) {
		this.temperatureSet = temperature;
		setLabel.setText("Set to: " + String.valueOf(temperatureSet) + farenheight);
	}
	
	// sets the outside temperature and updates label
	public void setOutsideTemperature(int temperature) {
		temperatureOutside = temperature;
		temperatureOutsideTextField.setText(String.valueOf(temperatureOutside) + farenheight);
	}
	
	// gets the current temperature
	public int getCurrentTemperature() {
		return temperatureCurrent;
	}
	
	// gets the temperature difference
	public int getTemperatureDifference() {
		return temperatureDifference;
	}

	// gets the set temperature
	public int getSetTemperature() {
		return temperatureSet;
	}
	
	// gets the outside temperature
	public int getOutsideTemperature() {
		return temperatureOutside;
	}

	// gets the scheduled task (for closing properly on program close) see main
	public ScheduledExecutorService getScheduleTasks() {
		return ses;
	}

	@FXML
	// event listener for usage button that sets the scene to usage page
	public void usageButtonPressed(ActionEvent actionEvent) {
		Stage primaryStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
		primaryStage.setScene(secondScene);
	}

	@FXML
	// event listener for diagnostics button that sets the scene to diagnostics page
	public void diagnosticsButtonPressed(ActionEvent actionEvent) {
		Stage primaryStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
		primaryStage.setScene(thirdScene);
		actionEvent.consume();
	}

	// Creates an Image object from a String url
	public Image openImage(String url) {
		Image image = null;
		try {
			image = new Image(new FileInputStream(url));
		} catch (FileNotFoundException e) {
			System.out.println("Background image not found!");
			e.printStackTrace();
		}
		return image;
	}

	// sets the background image centered and scaled to the Pane
	public void setImageView(Image image) {
		imageView = new ImageView();
		imageView.setImage(image);
		imageView.setPreserveRatio(true);
		imageView.fitHeightProperty().bind(pane.heightProperty());
		imageView.fitWidthProperty().bind(pane.widthProperty());
	}
	
	// used for demonstration purposes, changes the timescale of the hvac automatic operations
	public void hvacTimeScale(int seconds) {
		hvacDuration = seconds;
		hvacOutsideDuration = seconds*60;
		hvacDoorsDuration = seconds*5;
		hvacWindowsDuration = seconds*5;
		
	}
	
	// used to turn on hvac and ensures that it is off before attempting to turn it on
	public void turnOnHVAC() throws SQLException {
		updateTempDiff();
		// if hvac is not on
		if (!getHvacStatus() && getTemperatureDifference() != 0) {
			toggleOn((Shape)app_hvac, false);
		}
		if(getTemperatureDifference()>0) {
			temperatureTextField.setStyle("-fx-background-color:" + "#92DFF3");
		}
		else {
			temperatureTextField.setStyle("-fx-background-color:" + "#EF7C24");
		}
	}
	
	// increments or decrements temperatureSet and displays "set to" temperature
	// for three seconds before returning to inside temperature
	@FXML
	public void temperatureButtonPressed(ActionEvent buttonEvent) throws SQLException {
		// handles increase temperature
		if (buttonEvent.getSource() == increaseTemperatureButton) {
			setSetTemperature(getSetTemperature() + 1);
			turnOnHVAC();
			timeline.playFromStart();
		}
		// handles decrease temperature
		else {
			setSetTemperature(getSetTemperature() - 1);
			turnOnHVAC();
			timeline.playFromStart();
		}
		// if hvac is not on already, turn on the hvac unit
		turnOnHVAC();
	}
	
	// uses timeline animations to handle temperature changes in the UI
	public void hvacEvent() throws SQLException {
		final Duration TEMP_CHANGE = Duration.seconds(hvacDuration);
		timeline = new Timeline(
				new KeyFrame(
						TEMP_CHANGE,
						new EventHandler<ActionEvent>() {
							@Override 
							public void handle(ActionEvent actionEvent) {
								// if hvac is not on already, turn on the hvac unit
								try {
									turnOnHVAC();
								} catch (SQLException e1) {
									e1.printStackTrace();
								}
								// current - set > 0, lower current temperature
								if(getTemperatureDifference()>0) {
									temperatureTextField.setStyle("-fx-background-color:" + "#92DFF3");
									setCurrentTemperature(getCurrentTemperature()-1);
									updateTempDiff();
								}
								// current - set < 0, raise current temperature
								if(getTemperatureDifference()<0) {
									temperatureTextField.setStyle("-fx-background-color:" + "#EF7C24");
									setCurrentTemperature(getCurrentTemperature()+1);
									updateTempDiff();
								}
								// temperature is stable, turn off the hvac
								if(stableTemperature()) {
									temperatureTextField.setStyle("-fx-background-color:" + "#FFFFFF");
									try {
										if(getHvacStatus()) {
											toggleOff((Shape)app_hvac, false);
										}
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
						}
					));
	    timeline.setCycleCount(Timeline.INDEFINITE);
	    timeline.play();
	}
	
	// uses timeline animations to handle outside temp balance changes in the UI
		public void hvacOutside() {
			// duration to check statuses and update temperatures
			final Duration TEMP_CHANGE = Duration.seconds(hvacOutsideDuration);
			timelineOutside = new Timeline(
					new KeyFrame(
							TEMP_CHANGE,
							new EventHandler<ActionEvent>() {
								@Override 
								public void handle(ActionEvent actionEvent) {
									int externalDifference = getCurrentTemperature() - getOutsideTemperature();
									// raise the house temperature if outside is warmer
									if (externalDifference < 0) {
										int tempChange = 2 * (Math.abs(externalDifference) / 10);
										setCurrentTemperature(getCurrentTemperature() + tempChange);
										updateTempDiff();
										timeline.playFromStart();
									}
									// lower the house temperature if outside is cooler
									else if (externalDifference > 0) {
										int tempChange = 2 * (Math.abs(externalDifference) / 10);
										setCurrentTemperature(getCurrentTemperature() - tempChange);
										updateTempDiff();
										timeline.playFromStart();
									}
								}
							}
						));
		    timelineOutside.setCycleCount(Timeline.INDEFINITE);
		    timelineOutside.play();
		}
		
	// uses timeline animations to handle door temp balance changes in the UI
	public void hvacDoors() {
		// duration to check statuses and update temperatures
		final Duration TEMP_CHANGE = Duration.seconds(hvacDoorsDuration);
		timelineDoors = new Timeline(
				new KeyFrame(
						TEMP_CHANGE,
						new EventHandler<ActionEvent>() {
							@Override 
							public void handle(ActionEvent actionEvent) {
								int doorsOpen = 0;
								int externalDifference = getCurrentTemperature() - getOutsideTemperature();
								// check if doors are open
								String sqlQuery = "SELECT COUNT( * ) as \"Number of Rows\"\r\n"
										+ "FROM public.live_events\r\n"
										+ "WHERE event_id LIKE '%Door%' and on_status = true;";
								Statement s;
								// query for status
								try {
									s = Main.c.createStatement();
									ResultSet queryResult = s.executeQuery(sqlQuery);
									queryResult.next();
									doorsOpen = queryResult.getInt(1);
									queryResult.close();
								} catch (SQLException e) {
									e.printStackTrace();
								}
								if (doorsOpen > 0) {
									// raise the house temperature if outside is warmer
									if (externalDifference < 0) {
										int tempChange = 2 * (Math.abs(externalDifference) / 10);
										setCurrentTemperature(getCurrentTemperature() + tempChange);
										updateTempDiff();
										timeline.playFromStart();
									}
									// lower the house temeprature if outside is cooler
									if (externalDifference > 0) {
										int tempChange = 2 * (Math.abs(externalDifference) / 10);
										setCurrentTemperature(getCurrentTemperature() - tempChange);
										updateTempDiff();
										timeline.playFromStart();
									}
								}
								// do nothing, no status change needed
								else {
									return;
								}
							}
						}
					));
	    timelineDoors.setCycleCount(Timeline.INDEFINITE);
	    timelineDoors.play();
	}
				
	// uses timeline animations to handle window temp balance changes in the UI
	public void hvacWindows() {
		// duration to check statuses and update temperatures
		final Duration TEMP_CHANGE = Duration.seconds(hvacWindowsDuration);
		timelineWindows = new Timeline(
				new KeyFrame(
						TEMP_CHANGE,
						new EventHandler<ActionEvent>() {
							@Override 
							public void handle(ActionEvent actionEvent) {
								int windowsOpen = 0;
								int externalDifference = getCurrentTemperature() - getOutsideTemperature();
								// check if windows are open
								String sqlQuery = "SELECT COUNT( * ) as \"Number of Rows\"\r\n"
										+ "FROM public.live_events\r\n"
										+ "WHERE event_id LIKE '%window%' and on_status = true;";
								Statement s;
								// query for status
								try {
									s = Main.c.createStatement();
									ResultSet queryResult = s.executeQuery(sqlQuery);
									queryResult.next();
									windowsOpen = queryResult.getInt(1);
									queryResult.close();
								} catch (SQLException e) {
									e.printStackTrace();
								}
								// if windows are open, perform actions
								if (windowsOpen > 0) {
									// raise the house temperature if outside is warmer
									if (externalDifference < 0) {
										int tempChange = 1 * (Math.abs(externalDifference) / 10);
										setCurrentTemperature(getCurrentTemperature() + tempChange);
										updateTempDiff();
										timeline.playFromStart();
									}
									// lower the house temperature if outside is cooler
									if (externalDifference > 0) {
										int tempChange = 1 * (Math.abs(externalDifference) / 10);
										setCurrentTemperature(getCurrentTemperature() - tempChange);
										updateTempDiff();
										timeline.playFromStart();
									}
								}
								// do nothing, no status change needed
								else {
									return;
								}
							}
						}
					));
	    timelineWindows.setCycleCount(Timeline.INDEFINITE);
	    timelineWindows.play();
	}
	
	// Gets the on_status of the hvac unit
	public Boolean getHvacStatus() throws SQLException {
		String sqlQuery = "SELECT on_status FROM public.live_events WHERE event_id = \'Appliance - HVAC\'";
		Statement s;
		s = Main.c.createStatement();
		ResultSet queryResult = s.executeQuery(sqlQuery);
		queryResult.next();
		Boolean status = queryResult.getBoolean("on_status");
		queryResult.close();
		return status;
	}

	// update the temperature difference
	public void updateTempDiff() {
		setTemperatureDifference(getCurrentTemperature() - getSetTemperature());
	}
	
	// checks for a stable temperature
	public Boolean stableTemperature() {
		if (getTemperatureDifference() == 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// every hour, update the outside temperature
	public void externalTempUpdater() throws SQLException {
		// schedules commands to run every hour			
		ses.scheduleAtFixedRate(new Runnable() {
			// query database for outdoor temp and set it
			@Override
			public void run() {
				String sqlQuery = "SELECT weather.datetime, weather.temp FROM weather ORDER BY weather.datetime DESC LIMIT 1;";
				Statement s;
				try {
					s = Main.c.createStatement();
					ResultSet queryResult = s.executeQuery(sqlQuery);
					queryResult.next();
					setOutsideTemperature(queryResult.getInt("temp"));
					queryResult.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}, 0, 1, TimeUnit.HOURS);
	}

	@FXML
	// turns on all the lights or turns off all the lights
	public void allLightsButtonPressed() throws SQLException {
		// If button is "All Lights On", turn all lights on and set button to "All
		// Lights off"
		if (allLightsButton.getText().equals("All Lights On")) {
			for (Node node : lightingOverlay.getChildren()) {
				if (node instanceof Circle && ((Circle) node).getFill() == Color.YELLOW) {
					toggleOn((Circle) node, false);
				}
			}
			allLightsButton.setText("All Lights Off");
			quickStatusField.appendText("\n" + "All Lights On");
		// button was already used once, turn off all the lights
		} else {
			for (Node node : lightingOverlay.getChildren()) {
				if (node instanceof Circle && ((Circle) node).getFill() == Color.RED) {
					toggleOff((Circle) node, false);
				}
			}
			allLightsButton.setText("All Lights On");
			quickStatusField.appendText("\n" + "All Lights Off");
		}
	}

	@FXML
	// locks or unlocks all doors
	public void allDoorsLockedButtonPressed() throws SQLException {
		// If button is "All Doors Open", open associated doors
		if (allDoorsLockedButton.getText().equals("All Doors Locked")) {
			for (Rectangle door : doors) {
				if (door.getFill() == Color.DARKGREEN) { 
					toggleOn(door, false);
				}
			}
			// Set button text to be "All Doors Locked" to setup for locking doors
			allDoorsLockedButton.setText("All Doors Unlocked");
			quickStatusField.appendText("\n" + "All Doors Locked");
		// Doors were already opened and need to be locked
		} else {
			for (Rectangle door : doors) {
				if (door.getFill() == Color.RED) {
					toggleOff(door, false);
				}
			}
			// Set button text to be "All Doors Open" to setup for opening doors
			quickStatusField.appendText("\n" + "All Doors Unlocked");
			allDoorsLockedButton.setText("All Doors Locked");
		}
	}

	@FXML
	// closes or opens all garage doors
	public void garageDoorOpenButtonPressed() throws SQLException {
		// If button is "Garage Door Open", open associated doors
		if (garageDoorOpenButton.getText().equals("Garage Door Open")) {
			for (Rectangle door : garageDoors) {
				if (door.getFill() == Color.DARKGREEN) { 
					toggleOn(door, false);
				}
			}
			// Set button text to be "Garage Door Close" to setup for closing doors
			garageDoorOpenButton.setText("Garage Door Close");
			quickStatusField.appendText("\n" + "Garage doors opened");
		// Doors were already opened and need to be closed
		} else {
			for (Rectangle door : garageDoors) {
				if (door.getFill() == Color.RED) {
					toggleOff(door, false);
				}
			}
			// Set button text to be "Garage Door Open" to setup for opening doors
			quickStatusField.appendText("\n" + "Garage doors closed");
			garageDoorOpenButton.setText("Garage Door Open");
		}
	}

	@FXML
	// turns on or off the entertainment setup 
	public void entertainmentOnButtonPressed() throws SQLException {
		// If button is "Entertainment On", turn on associated items
		if (entertainmentOnButton.getText().equals("Entertainment On")) {
			for (Shape event : entertainment) {
				if (event.getFill() == Color.YELLOW || event.getFill() == Color.DODGERBLUE) { 
					toggleOn(event, false);
				}
			}
			// Set button text to be "Entertainment Off" to setup for turning off items
			entertainmentOnButton.setText("Entertainment Off");
			quickStatusField.appendText("\n" + "Entertainment On");
		// Entertainment is already on and needs to be turned off
		} else {
			for (Shape event : entertainment) {
				if (event.getFill() == Color.RED) { 
					toggleOff(event, false);
				}
			}
			// Set button text to be "Entertainment On" to setup for turning on items
			quickStatusField.appendText("\n" + "Entertainment Off");
			entertainmentOnButton.setText("Entertainment On");
		}
	}

	// When an item is clicked on Home Screen
	public void itemClicked(MouseEvent event) throws SQLException {
		Shape itemClicked = (Shape) event.getSource();
		Paint currentColor = itemClicked.fillProperty().getValue();
		// item is on, turn it off
		if (currentColor == Paint.valueOf("RED")) {
			toggleOff(itemClicked, false);
		// item is off, turn it on
		} else {
			toggleOn(itemClicked, false);

		}
	}

	// used to toggle event nodes for diagnostic tests
	void diagnosticToggle(String id, int toggle) throws SQLException {
		Shape item = null;
		for (Node node : lightingOverlay.getChildren()) {
			if (node.getId().compareToIgnoreCase(id) == 0) {
				item = (Shape) node;
				if (toggle == 1) {
					toggleOn(item, true);
				} else if (toggle == 2) {
					toggleOff(item, true);
				}
			}
		}
	}

	// Toggles an item on
	public void toggleOn(Shape itemClicked, Boolean fromDiagToggle) throws SQLException {
		// start time, and item id
		String startTime = getEventTime(itemClicked);
		String id = itemClicked.getId();
		String sensorTest = "";
		
		// if only a sensor test, print (Sensor Test)
		if (fromDiagToggle == true) {
			sensorTest = " (Sensor Test)";
		}
		
		// Sensor Quick Status message
		System.out.println(itemClicked.getId() + " Activated" + sensorTest);
		if (itemClicked.getClass().getTypeName().endsWith("Circle")) {
			changeColorAndMessage(itemClicked, Color.RED, "on" + sensorTest);
		} else if (itemClicked.getClass().getTypeName().endsWith("Rectangle") && itemClicked.getId().contains("App")) {
			changeColorAndMessage(itemClicked, Color.RED, "on" + sensorTest);
		} else if (itemClicked.getClass().getTypeName().endsWith("Rectangle")
				&& itemClicked.getId().contains("Window")) {
			changeColorAndMessage(itemClicked, Color.RED, "open" + sensorTest);
		} else if (itemClicked.getClass().getTypeName().endsWith("Rectangle") && itemClicked.getId().contains("Door")) {
			changeColorAndMessage(itemClicked, Color.RED, "open" + sensorTest);
		} else if (itemClicked.getClass().getTypeName().endsWith("Circle")
				&& itemClicked.getId().contains("exhaustFan")) {
			changeColorAndMessage(itemClicked, Color.RED, "on" + sensorTest);
		} else if (itemClicked.getClass().getTypeName().endsWith("Polygon")) {
			changeColorAndMessage(itemClicked, Color.RED, "flowing" + sensorTest);
		} else {
			System.out.println("OOPS! No such indicator to toggle on.");
		}
		
		// if not a sensor test, update timestamp in the database and set on_status to true
		if (fromDiagToggle == false) {
			// sql statement to check if sensor is in database already
			String sqlStatement = String.format("SELECT * FROM public.live_events WHERE event_id =\'%s\';", id);
			Statement s2 = Main.c.createStatement();
			ResultSet query = s2.executeQuery(sqlStatement);
	
			// checking if sensor is already in the event database
			if (query.next()) {
				// the sensor was already in the database, so just update the timestamp
	
				// sqlQuery to insert timestamp for toggleOn event
				String sqlQuery = String.format(
						"UPDATE public.live_events " + "SET on_status = FALSE " + "WHERE event_id=\'%s\';"
								+ "UPDATE public.live_events " + "SET time_stamp = \'%s\' " + "WHERE event_id=\'%s\';"
								+ "UPDATE public.live_events " + "SET on_status = TRUE " + "WHERE event_id=\'%s\';",
						id, startTime, id, id);
				Statement s = Main.c.createStatement();
				s.executeUpdate(sqlQuery);
			} else {
				// the sensor was not in the database, so insert sensor id and timestamp
				String sqlInsert = String.format("INSERT INTO public.live_events " + "VALUES (\'%s\', \'%s\', TRUE);", id,
						startTime);
				s2.executeUpdate(sqlInsert);
			}
		}
	}

	// toggles an item off
	public void toggleOff(Shape itemClicked, Boolean fromDiagToggle) throws SQLException {
		
		
		// start times, end times, and id
		String endTimeString = getEventTime(itemClicked);
		String startTimeString = null;
		String id = itemClicked.getId();
		String sensorTest = "";
		
		
		// if only a sensor test, print (Sensor Test)
		if (fromDiagToggle == true) {
			sensorTest = " (Sensor Test)";
		}
		
		// Sensor Quick Status message
		System.out.println(itemClicked.getId() + " Deactivated" + sensorTest);
		if (itemClicked.getClass().getTypeName().endsWith("Circle")) {
			changeColorAndMessage(itemClicked, Color.YELLOW, "off" + sensorTest);
		} else if (itemClicked.getClass().getTypeName().endsWith("Rectangle") && itemClicked.getId().contains("App")) {
			changeColorAndMessage(itemClicked, Color.DODGERBLUE, "off");
		} else if (itemClicked.getClass().getTypeName().endsWith("Rectangle")
				&& itemClicked.getId().contains("Window")) {
			changeColorAndMessage(itemClicked, Color.BLUEVIOLET, "closed" + sensorTest);
		} else if (itemClicked.getClass().getTypeName().endsWith("Rectangle") && itemClicked.getId().contains("Door")) {
			changeColorAndMessage(itemClicked, Color.DARKGREEN, "closed" + sensorTest);
		} else if (itemClicked.getClass().getTypeName().endsWith("Circle")
				&& itemClicked.getId().contains("exhaustFan")) {
			changeColorAndMessage(itemClicked, Color.YELLOW, "off" + sensorTest);
		} else if (itemClicked.getClass().getTypeName().endsWith("Polygon")) {
			changeColorAndMessage(itemClicked, Color.AQUA, "not flowing" + sensorTest);
		} else {
			System.out.println("OOPS! No such indicator to toggle off.");
		}
		
		// if not a sensor test, update status on database and calculate the usage
		if (fromDiagToggle == false) {
			// sqlQuery to set status to off
			String statusOff = String
					.format("UPDATE public.live_events " + "SET on_status = FALSE " + "WHERE event_id=\'%s\';", id);
			// sqlQuery to get the timestamp
			String sqlQuery = String.format("SELECT time_stamp from public.live_events " + "WHERE event_id=\'%s\'", id);
			Statement statusUpdate = Main.c.createStatement();
			Statement timestamp = Main.c.createStatement();
			statusUpdate.executeUpdate(statusOff);
			ResultSet timestampResult = timestamp.executeQuery(sqlQuery);
	
			if (timestampResult.next()) {
				startTimeString = timestampResult.getString(1);
			}
			// calculate the usage
			long difference = timeDifference(strToTime(startTimeString), strToTime(endTimeString), ChronoUnit.SECONDS);
			calculateUsage(id, difference);
		}
	}
	
	// calculates the usage of the events
	public void calculateUsage(String id, long difference) throws SQLException {
		UsageCalculations UC = new UsageCalculations();
		double minutesOn = (double) difference / 60;
		double hoursOn = (double) minutesOn / 60;

		double kilowattsUsed = 0.0;
		double elecCost = 0.0;
		double gallonsUsed = 0.0;
		double waterCost = 0.0;
		
		// matching event name to usage calculations
		if (id.contains("Light") || id.contains("Lamp")) {
			// calculating light usage
			kilowattsUsed = UC.electricUsage(UC.lightWattage, hoursOn);
			elecCost = UC.electricCost(kilowattsUsed);

		} else if (id.contains("Exhaust Fan")) {
			// calculating exhaust fan usage
			kilowattsUsed = UC.electricUsage(UC.exhaustFanWattage, hoursOn);
			elecCost = UC.electricCost(kilowattsUsed);

		} else if (id.contains("Living Room TV")) {
			// calculating living room tv usage
			kilowattsUsed = UC.electricUsage(UC.livingRoomTVWattage, hoursOn);
			elecCost = UC.electricCost(kilowattsUsed);

		} else if (id.contains("Dishwasher")) {
			// Calculates only electric usage for this appliance with this toggle
			kilowattsUsed = UC.electricUsage(UC.dishwasherWattage, hoursOn);
			elecCost = UC.electricCost(kilowattsUsed);

		} else if (id.contains("Washer")) {
			// Calculates only electric usage for this appliance with this toggle
			kilowattsUsed = UC.electricUsage(UC.clothesWasherWattage, hoursOn);

		} else if (id.contains("Washing Machine Water")) {
			// Calculation based on 20 gallons per load, 30 minutes per load ==> .67
			// gallons/minute
			// Simulates only cold water usage
			gallonsUsed = UC.waterCubicFeetUsage(.67 * minutesOn);
			waterCost = UC.waterCost(gallonsUsed);

		} else if (id.contains("Dryer")) {
			// calculating dryer usage
			kilowattsUsed = UC.electricUsage(UC.clothesDryerWattage, hoursOn);
			elecCost = UC.electricCost(kilowattsUsed);

		} else if (id.contains("Refridgerator")) {
			// calculating fridge usage
			kilowattsUsed = UC.electricUsage(UC.refridgeratorWattage, hoursOn);
			elecCost = UC.electricCost(kilowattsUsed);

		} else if (id.contains("Stove")) {
			// calculating stove usage
			kilowattsUsed = UC.electricUsage(UC.stoveWattage, hoursOn);
			elecCost = UC.electricCost(kilowattsUsed);

		} else if (id.contains("Oven")) {
			// calculating oven usage
			kilowattsUsed = UC.electricUsage(UC.ovenWattage, hoursOn);
			elecCost = UC.electricCost(kilowattsUsed);

		} else if (id.contains("Microwave")) {
			// calculating microwave usage
			kilowattsUsed = UC.electricUsage(UC.microwaveWattage, hoursOn);
			elecCost = UC.electricCost(kilowattsUsed);

		} else if (id.contains("Bedroom TV")) {
			// calculating bedroom tv usage
			kilowattsUsed = UC.electricUsage(UC.bedroomTVWattage, hoursOn);
			elecCost = UC.electricCost(kilowattsUsed);

		} else if (id.contains("Window")) {
			// handled by hvacWindows()

		} else if (id.contains("Door")) {
			// handled by hvacDoors()

		} else if (id.contains("Bathroom") && (id.contains("Sink"))) {
			// Obtained avg. gpm of faucet from
			// https://www.hunker.com/13415104/the-average-sink-faucet-gallons-of-water-per-minute
			// assumes a cold water simulation
			gallonsUsed = UC.waterCubicFeetUsage(1.5 * minutesOn);
			waterCost = UC.waterCost(gallonsUsed);

		} else if (id.contains("Kitchen Sink")) {
			// Obtained avg. gpm of faucet from
			// https://www.hunker.com/13415104/the-average-sink-faucet-gallons-of-water-per-minute
			// assumes a cold water simulation
			gallonsUsed = UC.waterCubicFeetUsage(2.2 * minutesOn);
			waterCost = UC.waterCost(gallonsUsed);

		} else if (id.contains("Toilet Water")) {
			// Obtained avg. gpm of toilet from
			// https://drinking-water.extension.org/what-is-the-water-flow-rate-to-most-fixtures-in-my-house/
			gallonsUsed = UC.waterCubicFeetUsage(2.5 * minutesOn);
			waterCost = UC.waterCost(gallonsUsed);

		} else if (id.contains("Shower")) {
			// Obtained avg. gpm of toilet from
			// https://drinking-water.extension.org/what-is-the-water-flow-rate-to-most-fixtures-in-my-house/
			gallonsUsed = UC.waterCubicFeetUsage(2.25 * minutesOn);
			waterCost = UC.waterCost(gallonsUsed);

		} else if (id.contains("Outside Faucet")) {
			// Obtained avg. gpm of toilet from
			// https://www.swanhose.com/garden-hose-flow-rate-s/1952.htm
			gallonsUsed = UC.waterCubicFeetUsage(13 * minutesOn);
			waterCost = UC.waterCost(gallonsUsed);

		} else if (id.contains("Dishwashwer Water")) {
			// Used cost of 6 gallons per 45 minute load, totaling .13 gallons per minute
			// assumes a cold water simulation
			gallonsUsed = UC.waterCubicFeetUsage(0.13 * minutesOn);
			waterCost = UC.waterCost(gallonsUsed);

		} else if (id.contains("Water Heater")) {
			// Calculates only electric usage for this appliance with this toggle
			kilowattsUsed = UC.electricUsage(UC.waterHeaterWattage, hoursOn);
			elecCost = UC.electricCost(kilowattsUsed);

		} else if (id.contains("HVAC")) {
			// calculating hvac usage
			kilowattsUsed = UC.electricUsage(UC.HVACWattage, hoursOn);
			elecCost = UC.electricCost(kilowattsUsed);
		}

		// console output for testing
		System.out.println(id + " was on for " + difference + " secs " + minutesOn + " mins " + hoursOn + " hours" + " - Kilowatts: " + kilowattsUsed + " ElecCost: $" + elecCost + " Gallons: "
				+ gallonsUsed + " WaterCost: $" + waterCost);

		updateUsagePage(kilowattsUsed, elecCost, gallonsUsed, waterCost);
	}

	// updates the usage graph and table with the usage amounts from the live events
	public void updateUsagePage(double kilowattsUsed, double elecCost, double gallonsUsed, double waterCost)
			throws SQLException {
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;

		String sqlDate = String.format("%d%d21", month, day);

		String sqlQuery = String.format(
				"UPDATE electricity_bill " + "SET kilowatts = kilowatts + %f " + "WHERE start_date = \'%s\';"
						+ "UPDATE electricity_bill " + "SET total_amount = total_amount + %f "
						+ "WHERE start_date = \'%s\';" + "UPDATE water_bill " + "SET gallons = gallons + %f "
						+ "WHERE start_date = \'%s\';" + "UPDATE water_bill " + "SET amount = amount + %f "
						+ "WHERE start_date = \'%s\';",
				kilowattsUsed, sqlDate, elecCost, sqlDate, gallonsUsed, sqlDate, waterCost, sqlDate);
		Statement s = Main.c.createStatement();
		s.executeUpdate(sqlQuery);
		// long difference = timeDifference(strToTime(startTimeString),
		// strToTime(endTimeString), ChronoUnit.SECONDS);

	}

	// changes the color and the status message
	public void changeColorAndMessage(Shape itemClicked, Paint color, String text) {
		itemClicked.setFill(color);
		quickStatusField.appendText("\n" + String.valueOf(itemClicked.getId()) + " " + text);
	}

	// creates a timestamp when called
	public String getEventTime(Shape itemClicked) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return timestamp.toString();
	}
	
	// converts timestring into timestamp
	public Timestamp strToTime(String timestring) {
		Timestamp timestamp = Timestamp.valueOf(timestring);
		return timestamp;

	}

	// calculates time difference between two Timestamps, first convert to
	// LocalDateTime
	static long timeDifference(Timestamp d1, Timestamp d2, ChronoUnit unit) {
		LocalDateTime localDateTimeStart = d1.toLocalDateTime();
		LocalDateTime localDateTimeEnd = d2.toLocalDateTime();
		return unit.between(localDateTimeStart, localDateTimeEnd);
	}
	
	// sets the status for all events to off in database, ensures there are no mismatched states
	public void allStatusOff() throws SQLException {
		String sqlQuery = "UPDATE public.live_events SET on_status = false;";
		Statement s;
		s = Main.c.createStatement();
		s.executeUpdate(sqlQuery);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// setting up temperature values
		setCurrentTemperature(72);
		setSetTemperature(getCurrentTemperature());
		setTemperatureDifference(0);
		
		// setting up hvac timescale, set to 3 for demonstration, 60 for requirements
		hvacTimeScale(3);
		
		// ensure status for everything matches the database
		try {
			allStatusOff();
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		
		// starting timelines for automated hvac controls
		try {
			hvacEvent();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		hvacOutside();
		hvacWindows();
		hvacDoors();
		
		// setting up lists for home page door control buttons
		doors.add(door_toGarage);
		doors.add(door_front);
		doors.add(door_back);
		garageDoors.add(door_garage_1);
		garageDoors.add(door_garage_2);
		
		// setting up lists for home page entertainment control buttons
		entertainment.add(app_livingroom_TV);
		entertainment.add(lamp_Livinga);
		entertainment.add(overheadLight_LR);
		entertainment.add(lamp_Livingb);
		
		// starting service for automatic external temperature updates
		try {
			externalTempUpdater();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

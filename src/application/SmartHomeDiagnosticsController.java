package application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

public class SmartHomeDiagnosticsController implements Initializable{
	private Scene firstScene;
	private Scene secondScene;
	private SmartHomeController homeController;
	
	@FXML
	private Button HomeButton;
	@FXML
	private Button UsageButton;
	@FXML
    private ToggleButton simulatewashingButton;
    @FXML
    private ToggleButton simulateshowerButton;
	@FXML
    private TextArea simulationField;
	@FXML
	private TextField lengthOfSimulationField;
	@FXML
    private ToggleButton simulateclotheswashButton;
	@FXML
	private ToggleButton simulateLIVEclotheswashButton;
	@FXML
	private Label gallonsUsedLabel;
	@FXML
	private Label kilowattsUsedLabel;
	@FXML
	private Label overallCostLabel;
	
	public Double timeToSimulate = 0.0;
	
	// USAGE READOUT VARIABLES
	public Double overallCost = 0.0;
	public Double gallonsUsed = 0.0;
	public Double kilowattsUsed = 0.0;


	public void setHomeScene(Scene scene) {
		firstScene = scene;
	}

	public void setUsageScene(Scene scene) {
		secondScene = scene;		
	}

	// event listener for home button that sets the scene to home page
	public void homeButtonPressed(ActionEvent actionEvent) {
		Stage primaryStage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
		primaryStage.setScene(firstScene);
	}
	
	// event listener for usage button that sets the scene to usage page
	public void usageButtonPressed(ActionEvent actionEvent) {
		Stage primaryStage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
		primaryStage.setScene(secondScene);
	}
	
	// takes the value of the minutes textbox and converts to double
	@FXML
	public void simulationMinutesUpdate() {
		if (lengthOfSimulationField.getText().isEmpty() == true) {
			this.lengthOfSimulationField.setText("1");
		} else {
			this.timeToSimulate = Double.parseDouble(lengthOfSimulationField.getText());
		}
	}
	
	// pause function for waiting during simulations
	public static void pause(int milisec) {
		try {
			Thread.sleep(milisec);
		} catch (InterruptedException e) {
			System.out.println("Error sleeping");
		}
	}
	
	// Calculates the shower event and outputs the results
	@FXML
    public void simulateshowerButtonPressed(ActionEvent event) {
		new Thread() {
			public void run() {
				simulationField.clear();
		
				// disabling buttons while simulation is running
				simulateshowerButton.setDisable(true);
				simulatewashingButton.setDisable(true);
				simulateclotheswashButton.setDisable(true);
				simulateLIVEclotheswashButton.setDisable(true);
				
				// checking the current value of the editable minutes textbox
				simulationMinutesUpdate();
				ToggleButton buttonID = (ToggleButton) event.getSource();
		
				String simulateStart = "\n Calculating shower event for "+String.valueOf(timeToSimulate)+" minutes...";
				simulationField.appendText(simulateStart);
				
		
				// turning on shower, fan, and light
				try {
					homeController.diagnosticToggle("Master Bedroom Shower", 1);
					homeController.diagnosticToggle("Master Bedroom Exhaust Fan", 1);
					homeController.diagnosticToggle("Master Bath Overhead Light", 1);
					
					// waiting while person "takes" a shower
					pause(10000);
					
					// turning off shower, fan, and light
					homeController.diagnosticToggle("Master Bedroom Shower", 2);
					homeController.diagnosticToggle("Master Bedroom Exhaust Fan", 2);
					homeController.diagnosticToggle("Master Bath Overhead Light", 2);
					
					// turning on water heater
					homeController.diagnosticToggle("Appliance - Water Heater", 1);
					
					// waiting while water heater heats more water
					pause(10000);
					
					// turning off water heater
					homeController.diagnosticToggle("Appliance - Water Heater", 2);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
		
				// Shower (+ Water Heater) calculations
				List<Double> totals = simulationCalculation(0, (2.25*timeToSimulate), (timeToSimulate/60), 0.65);
				double tempw = totals.get(0); 
				double tempg = totals.get(1);
				double tempwc = totals.get(2);
				double tempgc = totals.get(3);
		
				// Exhaust fan calculations
				totals = simulationCalculation(30, 0, timeToSimulate/60, 0.0);
				totals.set(0, totals.get(0)+tempw);
				totals.set(1, totals.get(1)+tempg);
				totals.set(2, totals.get(2)+tempwc);
				totals.set(3, totals.get(3)+tempgc);
				tempw = totals.get(0); 
				tempg = totals.get(1);
				tempwc = totals.get(2);
				tempwc = totals.get(3);
		
				// Overhead light calculations
				totals = simulationCalculation(60, 0, timeToSimulate/60, 0.0);
				totals.set(0, totals.get(0)+tempw);
				totals.set(1, totals.get(1)+tempg);
				totals.set(2, totals.get(2)+tempwc);
				totals.set(3, totals.get(3)+tempgc);
				totals = roundingData(totals);
		
				// TextArea output
				String watts = "\n Calculated kilowatts used: ";
				String gallons = "\n Calculated gallons used: ";
				String cost = "\n Calculated overall cost: ";
				String addon = Double.toString(totals.get(0));
				simulationField.appendText(watts+addon);
				addon = Double.toString(totals.get(1));
				simulationField.appendText(gallons+addon);
				double fullcost = (totals.get(2) + totals.get(3));
    			addon = Double.toString(fullcost);
				simulationField.appendText(cost+addon);
				
				// re-enable the simulation buttons
				simulateshowerButton.setDisable(false);
				simulateclotheswashButton.setDisable(false);
				simulatewashingButton.setDisable(false);
				simulateshowerButton.setSelected(false);
				simulateLIVEclotheswashButton.setDisable(false);
		}
		}.start();
	}
	
	// Calculates the dishwasher event and outputs the results
    @FXML
    void simulatewashingButtonPressed(ActionEvent event) {
    	new Thread() {
    		public void run() {
    			simulationField.clear();
    			
    			// disabling buttons while simulation is running
    			simulateshowerButton.setDisable(true);
				simulatewashingButton.setDisable(true);
				simulateclotheswashButton.setDisable(true);
				simulateLIVEclotheswashButton.setDisable(true);
    			
				// checking the current value of the editable minutes textbox
    			simulationMinutesUpdate();
    			ToggleButton buttonID = (ToggleButton) event.getSource();
		
    			String simulateStart = "\n Calculating dish washing event for "+String.valueOf(timeToSimulate)+" minutes...";
    			simulationField.appendText(simulateStart);
    			
 			
    			// turning on dishwasher, dishwasher water, and kitchen light
    			try {
					homeController.diagnosticToggle("Appliance - Dishwasher", 1);
					homeController.diagnosticToggle("Dishwasher Water", 1);
					homeController.diagnosticToggle("Kitchen Overhead Light", 1);
					
					// waiting while dishes are washed
					pause(10000);
					
					// turning off dishwasher, dishwasher water, and kitchen light
					homeController.diagnosticToggle("Appliance - Dishwasher", 2);
					homeController.diagnosticToggle("Dishwasher Water", 2);
					homeController.diagnosticToggle("Kitchen Overhead Light", 2);
					
					// turning on water heater 
					homeController.diagnosticToggle("Appliance - Water Heater", 1);
					
					// waiting while water heater heats water
					pause(10000);
					
					// turning water heater off
					homeController.diagnosticToggle("Appliance - Water Heater", 2);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		
    			// Dishwasher (+ Water Heater) calculation
    			List<Double> totals = simulationCalculation(1800, (0.13*timeToSimulate), timeToSimulate/60, 1.0);
    			double tempw = totals.get(0); 
    			double tempg = totals.get(1);
    			double tempwc = totals.get(2);
    			double tempgc = totals.get(3);
		
		
    			// Kitchen-light calculation 
    			totals = simulationCalculation(60, 0, timeToSimulate/60, 0.0);
    			totals.set(0, totals.get(0)+tempw);
    			totals.set(1, totals.get(1)+tempg);
    			totals.set(2, totals.get(2)+tempwc);
				totals.set(3, totals.get(3)+tempgc);
    			totals = roundingData(totals);
		
    			// TextArea output
    			String watts = "\n Calculated kilowatts used: ";
    			String gallons = "\n Calculated gallons used: ";
    			String cost = "\n Calculated overall cost: ";
    			String addon = Double.toString(totals.get(0));
    			simulationField.appendText(watts+addon);
    			addon = Double.toString(totals.get(1));
    			simulationField.appendText(gallons+addon);
    			double fullcost = (totals.get(2) + totals.get(3));
    			addon = Double.toString(fullcost);
    			simulationField.appendText(cost+addon);
    			
    			// re-enable the simulation buttons
    			simulateshowerButton.setDisable(false);
    			simulateclotheswashButton.setDisable(false);
				simulatewashingButton.setDisable(false);
				simulatewashingButton.setSelected(false);
				simulateLIVEclotheswashButton.setDisable(false);
    	}
    	}.start();
    }
	
    // Calculates the clothes washing event and outputs the results
    @FXML
    void simulateclotheswashButtonPressed(ActionEvent event) {
    	new Thread() {
    		public void run() {
    			simulationField.clear();
    			
    			// disabling buttons while simulation is running
    			simulateshowerButton.setDisable(true);
				simulatewashingButton.setDisable(true);
				simulateclotheswashButton.setDisable(true);
				simulateLIVEclotheswashButton.setDisable(true);
    			
				// checking the current value of the editable minutes textbox
    			simulationMinutesUpdate();
    			ToggleButton buttonID = (ToggleButton) event.getSource();
		
    			String simulateStart = "\n Calculating clothes washing event for "+String.valueOf(timeToSimulate)+" minutes...";
    			simulationField.appendText(simulateStart);
    			
 			
    			// turning on clothes washer, clothes washer water, and dryer
    			try {
    				homeController.diagnosticToggle("Appliance - Washer", 1);
					homeController.diagnosticToggle("Washing Machine Water", 1);
					
					// waiting while clothes are washed
					pause(10000);
					
					// turning off clothes washer and clothes washer water
					homeController.diagnosticToggle("Appliance - Washer", 2);
					homeController.diagnosticToggle("Washing Machine Water", 2);
					
					// turning on water heater 
					homeController.diagnosticToggle("Appliance - Water Heater", 1);
					
					// waiting while water heater heats water
					pause(10000);
					
					// turning water heater off
					homeController.diagnosticToggle("Appliance - Water Heater", 2);
					
					// turning on dryer
					homeController.diagnosticToggle("Appliance - Dryer", 1);
					
					// waiting while clothes are dried
					pause(10000);
					
					// turning off dryer
					homeController.diagnosticToggle("Appliance - Dryer", 2);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		
    			// Clothes washer (+ Water Heater) calculation
    			List<Double> totals = simulationCalculation(500, (0.67*timeToSimulate), timeToSimulate/60, .85);
    			double tempw = totals.get(0); 
    			double tempg = totals.get(1);
    			double tempwc = totals.get(2);
    			double tempgc = totals.get(3);
		
		
    			// Clothes dryer calculation 
    			totals = simulationCalculation(3000, 0, timeToSimulate/60, 0.0);
    			totals.set(0, totals.get(0)+tempw);
    			totals.set(1, totals.get(1)+tempg);
    			totals.set(2, totals.get(2)+tempwc);
				totals.set(3, totals.get(3)+tempgc);
    			totals = roundingData(totals);
		
    			// TextArea output
    			String watts = "\n Calculated kilowatts used: ";
    			String gallons = "\n Calculated gallons used: ";
    			String cost = "\n Calculated overall cost: ";
    			String addon = Double.toString(totals.get(0));
    			simulationField.appendText(watts+addon);
    			addon = Double.toString(totals.get(1));
    			simulationField.appendText(gallons+addon);
    			double fullcost = (totals.get(2) + totals.get(3));
    			addon = Double.toString(fullcost);
    			simulationField.appendText(cost+addon);
    			
    			// re-enable the simulation buttons
    			simulateshowerButton.setDisable(false);
				simulatewashingButton.setDisable(false);
				simulateclotheswashButton.setDisable(false);
				simulateclotheswashButton.setSelected(false);
				simulateLIVEclotheswashButton.setDisable(false);
    	}
    	}.start();
    }    
    
 // Calculates the clothes washing event and outputs the results
    @FXML
    void simulateLIVEclotheswashButtonPressed(ActionEvent event) {
    	new Thread() {
    		public void run() {
    			simulationField.clear();
    			
    			// disabling buttons while simulation is running
    			simulateshowerButton.setDisable(true);
				simulatewashingButton.setDisable(true);
				simulateclotheswashButton.setDisable(true);
				simulateLIVEclotheswashButton.setDisable(true);
    			
				// checking the current value of the editable minutes textbox
    			simulationMinutesUpdate();
    			ToggleButton buttonID = (ToggleButton) event.getSource();
		
    			String simulateStart = "\n Calculating clothes washing event for "+String.valueOf(timeToSimulate)+" minutes...";
    			simulationField.appendText(simulateStart);
    			
 			
    			// turning on clothes washer, clothes washer water, and dryer
    			try {
    				homeController.diagnosticToggle("Appliance - Washer", 1);
					homeController.diagnosticToggle("Washing Machine Water", 1);
					
					// waiting while clothes are washed
					pause(10000);
					
					// turning off clothes washer and clothes washer water
					homeController.diagnosticToggle("Appliance - Washer", 2);
					homeController.diagnosticToggle("Washing Machine Water", 2);
					
					// turning on water heater 
					homeController.diagnosticToggle("Appliance - Water Heater", 1);
					
					// waiting while water heater heats water
					pause(10000);
					
					// turning water heater off
					homeController.diagnosticToggle("Appliance - Water Heater", 2);
					
					// turning on dryer
					homeController.diagnosticToggle("Appliance - Dryer", 1);
					
					// waiting while clothes are dried
					pause(10000);
					
					// turning off dryer
					homeController.diagnosticToggle("Appliance - Dryer", 2);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		
    			// Clothes washer (+ Water Heater) calculation
    			List<Double> totals = simulationCalculation(500, (0.67*timeToSimulate), timeToSimulate/60, .85);
    			double tempw = totals.get(0); 
    			double tempg = totals.get(1);
    			double tempwc = totals.get(2);
    			double tempgc = totals.get(3);
		
		
    			// Clothes dryer calculation 
    			totals = simulationCalculation(3000, 0, timeToSimulate/60, 0.0);
    			totals.set(0, totals.get(0)+tempw);
    			totals.set(1, totals.get(1)+tempg);
    			totals.set(2, totals.get(2)+tempwc);
    			totals.set(3, totals.get(3)+tempgc);
    			totals = roundingData(totals);
		
    			// TextArea output
    			String watts = "\n Calculated kilowatts used: ";
    			String gallons = "\n Calculated gallons used: ";
    			String cost = "\n Calculated overall cost: ";
    			String addon = Double.toString(totals.get(0));
    			simulationField.appendText(watts+addon);
    			addon = Double.toString(totals.get(1));
    			simulationField.appendText(gallons+addon);
    			double fullcost = (totals.get(2) + totals.get(3));
    			addon = Double.toString(fullcost);
    			simulationField.appendText(cost+addon);
    			
    			// update simulation to live data
    			try {
					homeController.updateUsagePage(totals.get(0), totals.get(2), totals.get(1), totals.get(3));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    			// re-enable the simulation buttons
    			simulateshowerButton.setDisable(false);
				simulatewashingButton.setDisable(false);
				simulateclotheswashButton.setDisable(false);
				simulateLIVEclotheswashButton.setSelected(false);
				simulateLIVEclotheswashButton.setDisable(false);
    	}
    	}.start();
    }
    
    // Calculates for any given simulation
    public List<Double> simulationCalculation(int watts, double gallons, double time, double hotpercent) {
		List<Double> totals = Arrays.asList(0.0, 0.0, 0.0, 0.0);
		double w = 0.0;
		double g = gallons;
		double cubicfeet = 0.0;
		double wc = 0.0;
		double gc = 0.0;
		
		// watts calculation
		if (watts != 0) {
			w = w + ((watts*time)/1000);
		}
		
		// hot water heater calculation
		if (hotpercent != 0) {
			w = w + (((4000*hotpercent) * (4*gallons)/60)/1000);
		}
		
		// gallons --> cubic feet conversion
		if (gallons != 0) {
			cubicfeet = (100 * gallons)/748;
		}
		
		// cost calculation combining the wattage and water cost
		wc = wc + (0.12 * w);
		gc = gc + (cubicfeet/100)*2.52;
		totals.set(0, w);
		totals.set(1, cubicfeet);
		totals.set(2, wc);
		totals.set(3, gc);
		
		
		return totals;
    }
	
    // rounds data from the full simulations
    public List<Double> roundingData(List<Double> totals) {
		int z = 0;
		double setas;
		while (z <= 3) {
			setas = totals.get(z);
			setas = Math.round(setas*100.0)/100.0;
			totals.set(z, setas);
			z++;
		}
		return totals;
	}
    
    // rounds data from the simulation toggles
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    @Override
	public void initialize(URL arg0, ResourceBundle arg1) {
    	simulationField.setEditable(false);
	}
	
    
    // Function occurs when a device is toggled
	public void toggleSimulation2 (ActionEvent event) throws SQLException {
		UsageCalculations UC = new UsageCalculations();
		simulationMinutesUpdate();
		ToggleButton buttonID = (ToggleButton) event.getSource();
		String toggleID = buttonID.getId().toString();
		
		if (buttonID.isSelected() == true) {
			homeController.diagnosticToggle(toggleID, 1);
			
			if (toggleID.contains("Light") || toggleID.contains("Lamp")) {
				costCalculationsOn(UC.lightWattage, 0.0);
			
			} else if (toggleID.contains("Exhaust Fan")) {
				costCalculationsOn(UC.exhaustFanWattage, 0.0);

			} else if (toggleID.contains("Living Room TV")) {
				costCalculationsOn(UC.livingRoomTVWattage, 0.0);
				
			} else if (toggleID.contains("Dishwasher")) {
				// Calculates only electric usage for this appliance with this toggle
				costCalculationsOn(UC.dishwasherWattage, 0.0);
				
			} else if (toggleID.contains("Washer")) {
				// Calculates only electric usage for this appliance with this toggle
				costCalculationsOn(UC.clothesWasherWattage, 0.0);
				
			} else if (toggleID.contains("Washing Machine Water")) {
				// Calculation based on 20 gallons per load, 30 minutes per load ==> .67 gallons/minute
				// Simulates only cold water usage
				costCalculationsOn(0.0, .67 * timeToSimulate);
				
			} else if (toggleID.contains("Dryer")) {
				costCalculationsOn(UC.clothesDryerWattage, 0.0);			
				
			} else if (toggleID.contains("Refridgerator")) {
				costCalculationsOn(UC.refridgeratorWattage, 0.0);
				
			} else if (toggleID.contains("Stove")) {
				costCalculationsOn(UC.stoveWattage, 0.0);
				
			} else if (toggleID.contains("Oven")) {
				costCalculationsOn(UC.ovenWattage, 0.0);
				
			} else if (toggleID.contains("Microwave")) {
				costCalculationsOn(UC.microwaveWattage, 0.0);
				
			} else if (toggleID.contains("HVAC")) {
				//Calculates only electric usage for this appliance with this toggle
				costCalculationsOn(UC.HVACWattage, 0.0);
				
			} else if (toggleID.contains("Window")) {
				//TODO: Uncertain what impact this action will have on house temp
				//For now, the parameters have no impact
				costCalculationsOn(0.0, 0.0);
				
			} else if (toggleID.contains("Door")) {
				//TODO: Uncertain what impact this action will have on house temp
				//For now, the parameters have no impact
				costCalculationsOn(0.0, 0.0);
				
			} else if (toggleID.contains("Bathroom") && (toggleID.contains("Sink"))) {
				//Obtained avg. gpm of faucet from https://www.hunker.com/13415104/the-average-sink-faucet-gallons-of-water-per-minute
				//assumes a cold water simulation
				costCalculationsOn(0.0, 1.5 * timeToSimulate);
				
			} else if (toggleID.contains("Kitchen Sink")) {
				//Obtained avg. gpm of faucet from https://www.hunker.com/13415104/the-average-sink-faucet-gallons-of-water-per-minute
				//assumes a cold water simulation
				costCalculationsOn(0.0, 2.2 * timeToSimulate);
				
			} else if (toggleID.contains("Toilet Water")) {
				//Obtained avg. gpm of toilet from https://drinking-water.extension.org/what-is-the-water-flow-rate-to-most-fixtures-in-my-house/
				costCalculationsOn(0.0, 2.5 * timeToSimulate);
				
			} else if (toggleID.contains("Shower")) {
				//Obtained avg. gpm of shower from https://drinking-water.extension.org/what-is-the-water-flow-rate-to-most-fixtures-in-my-house/
				costCalculationsOn(0.0, 2.25 * timeToSimulate);
				
			} else if (toggleID.contains("Outside Faucet")) {
				//Obtained avg. gpm of faucet from https://www.swanhose.com/garden-hose-flow-rate-s/1952.htm
				costCalculationsOn(0.0, 13 * timeToSimulate);
				
			} else if (toggleID.contains("Dishwashwer Water")) {
				//Used cost of 6 gallons per 45 minute load, totaling .13 gallons per minute
				//assumes a cold water simulation
				costCalculationsOn(0.0, 0.13 * timeToSimulate);
				
			} else if (toggleID.contains("Water Heater")) {
				//Calculates only electric usage for this appliance with this toggle
				costCalculationsOn(UC.waterHeaterWattage, 0.0);
			}
		
		} else if (buttonID.isSelected() == false) {
			homeController.diagnosticToggle(toggleID, 2);
		
			if (toggleID.contains("Light") || toggleID.contains("Lamp")) {
				costCalculationsOff(UC.lightWattage, 0.0);
			
			} else if (toggleID.contains("Exhaust Fan")) {
				costCalculationsOff(UC.exhaustFanWattage, 0.0);

			} else if (toggleID.contains("Living Room TV")) {
				costCalculationsOff(UC.livingRoomTVWattage, 0.0);
				
			} else if (toggleID.contains("Dishwasher")) {
				// Just calculates electric usage for the appliance itself, no water calculation here. That is done via event simulation.
				costCalculationsOff(UC.dishwasherWattage, 0.0);
				
			} else if (toggleID.contains("Washer")) {
				// Calculates only electric usage for this appliance with this toggle
				costCalculationsOff(UC.clothesWasherWattage, 0.0);
				
			} else if (toggleID.contains("Washing Machine Water")) {
				// Calculation based on 20 gallons per load, 30 minutes per load ==> .67 gallons/minute
				// Simulates only cold water usage
				costCalculationsOff(0.0, .67 * timeToSimulate);
				
			} else if (toggleID.contains("Dryer")) {
				costCalculationsOff(UC.clothesDryerWattage, 0.0);			
				
			} else if (toggleID.contains("Refridgerator")) {
				costCalculationsOff(UC.refridgeratorWattage, 0.0);
				
			} else if (toggleID.contains("Stove")) {
				costCalculationsOff(UC.stoveWattage, 0.0);
				
			} else if (toggleID.contains("Oven")) {
				costCalculationsOff(UC.ovenWattage, 0.0);
				
			} else if (toggleID.contains("Microwave")) {
				costCalculationsOff(UC.microwaveWattage, 0.0);
				
			} else if (toggleID.contains("Window")) {
				//TODO: Uncertain what impact this action will have on house temp
				//For now, the parameters have no impact
				costCalculationsOff(0.0, 0.0);
				
			} else if (toggleID.contains("HVAC")) {
				//Calculates only electric usage for this appliance with this toggle
				costCalculationsOff(UC.HVACWattage, 0.0);
				
			} else if (toggleID.contains("Door")) {
				//TODO: Uncertain what impact this action will have on house temp
				//For now, the parameters have no impact
				costCalculationsOff(0.0, 0.0);
				
			} else if (toggleID.contains("Bathroom") && (toggleID.contains("Sink"))) {
				//Obtained avg. gpm of faucet from https://www.hunker.com/13415104/the-average-sink-faucet-gallons-of-water-per-minute
				//assumes a cold water simulation
				costCalculationsOff(0.0, 1.5 * timeToSimulate);
				
			} else if (toggleID.contains("Kitchen Sink")) {
				//Obtained avg. gpm of faucet from https://www.hunker.com/13415104/the-average-sink-faucet-gallons-of-water-per-minute
				//assumes a cold water simulation
				costCalculationsOff(0.0, 2.2 * timeToSimulate);
				
			} else if (toggleID.contains("Toilet Water")) {
				//Obtained avg. gpm of toilet from https://drinking-water.extension.org/what-is-the-water-flow-rate-to-most-fixtures-in-my-house/
				costCalculationsOff(0.0, 2.5 * timeToSimulate);	
				
			} else if (toggleID.contains("Shower")) {
				//Obtained avg. gpm of shower from https://drinking-water.extension.org/what-is-the-water-flow-rate-to-most-fixtures-in-my-house/
				costCalculationsOff(0.0, 2.25 * timeToSimulate);
				
			} else if (toggleID.contains("Outside Faucet")) {
				//Obtained avg. gpm of faucet from https://www.swanhose.com/garden-hose-flow-rate-s/1952.htm
				costCalculationsOff(0.0, 13 * timeToSimulate);
				
			} else if (toggleID.contains("Dishwashwer Water")) {
				//Used cost of 6 gallons per 45 minute load, totaling .13 gallons per minute
				//assumes a cold water simulation
				costCalculationsOff(0.0, 0.13 * timeToSimulate);
				
			} else if (toggleID.contains("Water Heater")) {
				//Calculates only electric usage for this appliance with this toggle
				costCalculationsOff(UC.waterHeaterWattage, 0.0);
			}
		}		
	}
	
	// Calculates the Electric and Water Usage and the Overall Cost when toggled ON, adding it to the Usage Readout 
	public void costCalculationsOn (Double wattage, Double gallons) {
		UsageCalculations UC = new UsageCalculations();
		Double electricUsage = UC.electricUsage(wattage, (timeToSimulate/60));
		Double electricCost = UC.electricCost(electricUsage);
		Double waterUsage = UC.waterCubicFeetUsage(gallons);
		Double waterCost = UC.waterCost(waterUsage);
		Double totalCost = electricCost + waterCost;
		overallCost = round((overallCost + totalCost), 2);
		gallonsUsed = round((gallonsUsed + waterUsage), 2);
		kilowattsUsed = round((kilowattsUsed + electricUsage), 4);
		updateIndicators();
	}
	
	// Calculates the Electric and Water Usage and the Overall Cost when toggled OFF, subtracting it from the Usage Readout 
	public void costCalculationsOff (Double wattage, Double gallons) {
		UsageCalculations UC = new UsageCalculations();
		Double electricUsage = UC.electricUsage(wattage, (timeToSimulate/60));
		Double electricCost = UC.electricCost(electricUsage);
		Double waterUsage = UC.waterCubicFeetUsage(gallons);
		Double waterCost = UC.waterCost(waterUsage);
		Double totalCost = electricCost + waterCost;
		overallCost = round((overallCost - totalCost), 2);
		gallonsUsed = round((gallonsUsed - waterUsage), 2);
		kilowattsUsed = round((kilowattsUsed - electricUsage), 4);
		updateIndicators();
	}
	
	// updates the Usage Readout based on the given parameters
	public void updateIndicators () {
		String cost = "$ ";
		gallonsUsedLabel.setText(gallonsUsed.toString());
		kilowattsUsedLabel.setText(kilowattsUsed.toString());
		overallCostLabel.setText(cost+overallCost.toString());
	}
	
	// resets the Usage Readout ***deprecated****
	public void resetIndicators () {
		overallCost = 0.0;
		kilowattsUsed = 0.0000;
		overallCost = 0.00;
		updateIndicators();
	}
		
	public void setHomeController(SmartHomeController smartHomeController) {
		this.homeController = smartHomeController;
	}

}

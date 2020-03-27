import java.util.HashMap;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class ServerGUI extends Application {
	
	HashMap<String,Scene> sceneMap = new HashMap<String,Scene>();// to hold scenes for server
	Server serverConnection;
	
	/*Scene 1 variables*/
	Label portLabel;// label for port TextField
	TextField portTextField;//to enter port number
	Button onButton;//to turn on the server
	Integer portNumber;//to hold port number
	HBox startHBox;//HBox to hold above nodes
	Label warningLabel; //label to display warning(Ex. incorrect port number)
	BorderPane startBorderPane;//pane for the start scene
	
	/*Scene 2 variables*/
	ListView<String> svListView = new ListView<String>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		primaryStage.setTitle("Let's Play Tic Tac Toe!!!");
		
		//create scenes and add them to a hash map
		sceneMap.put("start", getStartScene());
		sceneMap.put("info", getInfoScene());
		
		//set scene
		primaryStage.setScene(sceneMap.get("start"));
		primaryStage.show();
		
		
		onButton.setOnAction(e->{
			try {
				if(!isValidPort()) {
					showError(warningLabel,"Please enter port from 4500-6500 !");
					return;
				}	
			}
			catch(Exception ex){
				showError(warningLabel,"Port only contains integers !");
				return;
			}
			
			primaryStage.setScene(sceneMap.get("info"));
			primaryStage.setTitle("This is the server");
			serverConnection = new Server(data -> {
				Platform.runLater(()->{
					svListView.getItems().add(data.toString()); 
				});
			}, portNumber);
											
		});
		
		
		//close the stage properly
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                //System.exit(0);
                
            }
        });
	}
	
	//function to create start scene
	public Scene getStartScene() {
		
		//initialize nodes
		portLabel = new Label("Enter port:");
		portTextField = new TextField();
		onButton = new Button("Turn On Server");
		onButton.setDefaultButton(true);//set this so we can hit ENTER instead of clicking mouse
		warningLabel = new Label();
		warningLabel.setStyle("-fx-font: 16 Arial");
		
		//add nodes to HBox
		this.startHBox = new HBox(10,portLabel, portTextField, onButton);
		
		//configure start pane
		startBorderPane = new BorderPane();
		startBorderPane.setPadding(new Insets(20));
		startBorderPane.setCenter(startHBox);
		startBorderPane.setBottom(warningLabel);//for displaying warnings
		startBorderPane.setStyle("-fx-background-color: #00CED1");
		
		//create new scene from pane
		return new Scene(startBorderPane,400,200);
	}
	
	//function creates scene 2: information board
	public Scene getInfoScene() {
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(20));
		pane.setStyle("-fx-background-color: #00CED1");
		pane.setCenter(svListView);
		return new Scene(pane, 500, 200);	
	}
	
	//Function to validate port number
	//For this program, we only use port 5555 for simplicity
	boolean isValidPort() throws Exception{
		portNumber = Integer.parseInt(portTextField.getText());
		return portNumber >= 4500 && portNumber <= 6500;//avoid reserved ports
	}
			
	//Function to display input error
	void showError(Label label,String msg) {
		/*Pause the program for certain duration*/
		//create an empty event handler to act as a place holder for Timeline
		EventHandler<ActionEvent> empty = e ->{};
		//create a Timeline object to pause the scene
		Timeline pause = new Timeline(new KeyFrame(Duration.millis(3000),empty));
						
		//add the message for 2 seconds then remove it
		label.setText(msg);
		pause.play();
		pause.setOnFinished(q->{
			label.setText("");
		});
	}
}

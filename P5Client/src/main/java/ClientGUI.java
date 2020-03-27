import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class ClientGUI extends Application {
	
	HashMap<String,Scene> sceneMap = new HashMap<String,Scene>();// to hold scenes for client
	Client clientConnection;
	Socket clientSocket;
	String difficulty; //selected difficulty
	Integer score = 0;//to hold score
	
	/*Scene 1 variables*/
	TextField ipTextField, portTextField;
	Label ipLabel,portLabel;
	HBox portBox, ipBox;
	Button onButton;
	VBox centerVBox;
	Label warningLabel; //label to display warning(Ex. incorrect port/ip number)
	BorderPane startBorderPane;
	
	/*Scene 2 variables*/
	Label promptLabel;
	Button easyButton, mediumButton, expertButton;
	HBox difficultyHBox;
	BorderPane difficultyBorderPane;

	/*Scene 3 variables*/
	Label msgLabel;
	ListView<String> msgListView;
	VBox msgVBox;
	Label topScoreLabel;
	ListView<String> topScoreListView;
	VBox topScoreVBox;
	GridPane gameGridPane;
	Label l1,l2,l3,l4,l5,l6,l7,l8,l9;//labels for gameGridPane
	Button playAgainButton;
	GridPane sceneGridPane;
	Label scoreTextLabel, scoreNumberLabel;
	HBox scoreHBox;
	VBox bottomLeftGridVBox;
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		sceneMap.put("start",getStartScene());
		sceneMap.put("difficulty", getDifficultyScene());
		sceneMap.put("play", getPlayScene());
		
		//The on Button
		this.onButton.setOnAction(e-> {
			try {
				clientSocket = new Socket(ipTextField.getText(),Integer.parseInt(portTextField.getText()));
				clientSocket.setTcpNoDelay(true);
			}
			catch(Exception ex) {
				showError(warningLabel,"IP is 127.0.0.1 for this program\nPort is from 4500 to 6500\n");
				return;
			}
			
			
			
			//start a new client thread
			clientConnection = new Client(data->{
				//update msg list view
				Platform.runLater(()->{
					if(data.toString().equals("refresh")
							|| data.toString().equals("sv_win")) {
						
						refreshBoard(clientConnection.getGameInfo());
					}
					else if(data.toString().equals("update_score")) {
						score++;
						scoreNumberLabel.setText(score.toString());
					}
					else if(data.toString().equals("disable")){
						disableLabels();
					}
					else {
						msgListView.getItems().add(data.toString());
					}
				});
			},data1->{
				//update top score list view
				Platform.runLater(()->{
					if(data1.toString().equals("clear")) {
						topScoreListView.getItems().clear();
					}
					else {
						topScoreListView.getItems().add(data1.toString());
					}
				});
			},clientSocket);
			
			//show scene 2
			primaryStage.setTitle("Difficulty : Client " + clientConnection.getID());
			primaryStage.setScene(sceneMap.get("difficulty"));
			primaryStage.show();
			
			clientConnection.start();
		});
		
		easyButton.setOnAction(e->{
			difficulty = "easy";
			msgListView.getItems().add("Difficulty: EASY");
			enableLabels();//turn on clikable labels
			
			//set the difficulty in the GameInfo object, then send it to server
			clientConnection.getGameInfo().difficulty = "easy";
			clientConnection.getGameInfo().signal = GameInfo.READ_DIFF;
			try {
				clientConnection.out.writeObject(clientConnection.getGameInfo());
				clientConnection.out.reset();
			} catch (IOException e1) {}
			
			primaryStage.setTitle("Game Scene : Client " + clientConnection.getID());
			primaryStage.setScene(sceneMap.get("play"));
			clientConnection.getGameInfo().resetBoard();//reset the board for a new game
			refreshBoard(clientConnection.getGameInfo());//show the new board
			primaryStage.show();
		});
		
		mediumButton.setOnAction(e->{
			difficulty = "medium";
			msgListView.getItems().add("Difficulty: MEDIUM");
			enableLabels();//turn on clikable labels
			
			//set the difficulty in the GameInfo object, then send it to server
			clientConnection.getGameInfo().difficulty = "medium";
			clientConnection.getGameInfo().signal = GameInfo.READ_DIFF;
			try {
				clientConnection.out.writeObject(clientConnection.getGameInfo());
				clientConnection.out.reset();
			} catch (IOException e1) {}
			
			primaryStage.setTitle("Game Scene : Client " + clientConnection.getID());
			primaryStage.setScene(sceneMap.get("play"));
			clientConnection.getGameInfo().resetBoard();//reset the board for a new game
			refreshBoard(clientConnection.getGameInfo());//show the new board
			primaryStage.show();
		});
		
		expertButton.setOnAction(e->{
			difficulty = "expert";
			msgListView.getItems().add("Difficulty: EXPERT");
			enableLabels();//turn on clikable labels
			
			//set the difficulty in the GameInfo object, then send it to server
			clientConnection.getGameInfo().difficulty = "expert";
			clientConnection.getGameInfo().signal = GameInfo.READ_DIFF;
			try {
				clientConnection.out.writeObject(clientConnection.getGameInfo());
				clientConnection.out.reset();
			} catch (IOException e1) {}
			
			primaryStage.setTitle("Game Scene : Client " + clientConnection.getID());
			primaryStage.setScene(sceneMap.get("play"));
			clientConnection.getGameInfo().resetBoard();//reset the board for a new game
			refreshBoard(clientConnection.getGameInfo());//show the new board
			primaryStage.show();
		});
		
		l1.setOnMouseClicked(e->{
			if(l1.getText().equals("O") || l1.getText().equals("X")) {
				msgListView.getItems().add("Please choose different square!!!");
			}
			else {
				clientConnection.getGameInfo().stringBoard[0][0] = "O";
				refreshBoard(clientConnection.getGameInfo());
				
				//send the signal to tell the server to make a move
				clientConnection.getGameInfo().signal = GameInfo.READ_MOVE;
				try {
					clientConnection.out.writeObject(clientConnection.getGameInfo());
					clientConnection.out.reset();
				} catch (IOException e1) {}
			}
		});
		l2.setOnMouseClicked(e->{
			if(l2.getText().equals("O") || l2.getText().equals("X")) {
				msgListView.getItems().add("Please choose different square!!!");
			}
			else {
				clientConnection.getGameInfo().stringBoard[0][1] = "O";
				refreshBoard(clientConnection.getGameInfo());
				
				//send the signal to tell the server to make a move
				clientConnection.getGameInfo().signal = GameInfo.READ_MOVE;
				try {
					clientConnection.out.writeObject(clientConnection.getGameInfo());
					clientConnection.out.reset();
				} catch (IOException e1) {}
			}
		});
		l3.setOnMouseClicked(e->{
			if(l3.getText().equals("O") || l3.getText().equals("X")) {
				msgListView.getItems().add("Please choose different square!!!");
			}
			else {
				clientConnection.getGameInfo().stringBoard[0][2] = "O";
				refreshBoard(clientConnection.getGameInfo());
				
				//send the signal to tell the server to make a move
				clientConnection.getGameInfo().signal = GameInfo.READ_MOVE;
				try {
					clientConnection.out.writeObject(clientConnection.getGameInfo());
					clientConnection.out.reset();
				} catch (IOException e1) {}
			}
		});
		l4.setOnMouseClicked(e->{
			if(l4.getText().equals("O") || l4.getText().equals("X")) {
				msgListView.getItems().add("Please choose different square!!!");
			}
			else {
				clientConnection.getGameInfo().stringBoard[1][0] = "O";
				refreshBoard(clientConnection.getGameInfo());
				
				//send the signal to tell the server to make a move
				clientConnection.getGameInfo().signal = GameInfo.READ_MOVE;
				try {
					clientConnection.out.writeObject(clientConnection.getGameInfo());
					clientConnection.out.reset();
				} catch (IOException e1) {}
			}
		});
		l5.setOnMouseClicked(e->{
			if(l5.getText().equals("O") || l5.getText().equals("X")) {
				msgListView.getItems().add("Please choose different square!!!");
			}
			else {
				clientConnection.getGameInfo().stringBoard[1][1] = "O";
				refreshBoard(clientConnection.getGameInfo());
					
				//send the signal to tell the server to make a move
				clientConnection.getGameInfo().signal = GameInfo.READ_MOVE;
				try {
					clientConnection.out.writeObject(clientConnection.getGameInfo());
					clientConnection.out.reset();
				} catch (IOException e1) {}
			}
		});
		l6.setOnMouseClicked(e->{
			if(l6.getText().equals("O") || l6.getText().equals("X")) {
				msgListView.getItems().add("Please choose different square!!!");
			}
			else {
				clientConnection.getGameInfo().stringBoard[1][2] = "O";
				refreshBoard(clientConnection.getGameInfo());
				
				//send the signal to tell the server to make a move
				clientConnection.getGameInfo().signal = GameInfo.READ_MOVE;
				try {
					clientConnection.out.writeObject(clientConnection.getGameInfo());
					clientConnection.out.reset();
				} catch (IOException e1) {}
			}
		});
		l7.setOnMouseClicked(e->{
			if(l7.getText().equals("O") || l7.getText().equals("X")) {
				msgListView.getItems().add("Please choose different square!!!");
			}
			else {
				clientConnection.getGameInfo().stringBoard[2][0] = "O";
				refreshBoard(clientConnection.getGameInfo());
				
				//send the signal to tell the server to make a move
				clientConnection.getGameInfo().signal = GameInfo.READ_MOVE;
				try {
					clientConnection.out.writeObject(clientConnection.getGameInfo());
					clientConnection.out.reset();
				} catch (IOException e1) {}
			}
		});
		l8.setOnMouseClicked(e->{
			if(l8.getText().equals("O") || l8.getText().equals("X")) {
				msgListView.getItems().add("Please choose different square!!!");
			}
			else {
				clientConnection.getGameInfo().stringBoard[2][1] = "O";
				refreshBoard(clientConnection.getGameInfo());
				
				//send the signal to tell the server to make a move
				clientConnection.getGameInfo().signal = GameInfo.READ_MOVE;
				try {
					clientConnection.out.writeObject(clientConnection.getGameInfo());
					clientConnection.out.reset();
				} catch (IOException e1) {}
			}
		});
		l9.setOnMouseClicked(e->{
			if(l9.getText().equals("O") || l9.getText().equals("X")) {
				msgListView.getItems().add("Please choose different square!!!");
			}
			else {
				clientConnection.getGameInfo().stringBoard[2][2] = "O";
				refreshBoard(clientConnection.getGameInfo());
				
				//send the signal to tell the server to make a move
				clientConnection.getGameInfo().signal = GameInfo.READ_MOVE;
				try {
					clientConnection.out.writeObject(clientConnection.getGameInfo());
					clientConnection.out.reset();
				} catch (IOException e1) {}
			}
		});
		
		playAgainButton.setOnAction(e->{
			difficulty = "";//reset difficulty
			msgListView.getItems().clear();//clear the msg list view
			
			primaryStage.setTitle("Difficulty");
			primaryStage.setScene(sceneMap.get("difficulty"));
			primaryStage.show();
		});
		
		primaryStage.setTitle("Let's Play Tic Tac Toe!!!");
		primaryStage.setScene(sceneMap.get("start"));
		primaryStage.show();
		
		//close client properly
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
	}
	
	
	//function to create start scene
	public Scene getStartScene() {
		//Making two labels for entering port label and IP address
		portLabel = new Label("Enter Port number: ");
		ipLabel = new Label("Enter IP number: ");
		
		//Making two text fields for each of these
		portTextField = new TextField();
		ipTextField = new TextField();
		
		//Making 2 HBoxes for each:
		portBox = new HBox(portLabel, portTextField);
		ipBox = new HBox(ipLabel, ipTextField);
		
		//Putting padding between the two Hboxes:
		portBox.setPadding(new Insets(10, 10, 10, 10));
		ipBox.setPadding(new Insets(10, 10, 10, 10));
		
		//Making the buttons take in port Number and IP Address
		this.onButton = new Button("ON");
		onButton.setDefaultButton(true);//set this so we can hit ENTER instead of clicking mouse
		
		//New VBox to stick them there
		centerVBox = new VBox();
		centerVBox.getChildren().addAll(portBox, ipBox, onButton);
		
		//border pane for the scene
		startBorderPane = new BorderPane();
		startBorderPane.setPadding(new Insets(20));
		startBorderPane.setCenter(centerVBox);
		startBorderPane.setStyle("-fx-background-color: pink");
		
		//label to display error
		warningLabel = new Label();
		warningLabel.setStyle("-fx-font: 16 Arial");
		startBorderPane.setBottom(warningLabel);//to display port of IP errors
		
		return new Scene(startBorderPane,400,200);
	}
	
	//function to create difficulty scene
	public Scene getDifficultyScene() {
		//initialize nodes
		promptLabel = new Label("Difficulty");
		easyButton = new Button("Easy");
		mediumButton = new Button("Medium");
		expertButton = new Button("Expert");
		difficultyHBox = new HBox(10);
		difficultyBorderPane = new BorderPane();
		
		//add all nodes together
		difficultyHBox.getChildren().addAll(easyButton,mediumButton,expertButton);
		difficultyBorderPane.setCenter(difficultyHBox);
		difficultyBorderPane.setTop(promptLabel);
		
		//configure styles
		promptLabel.setStyle("-fx-font: 16 Arial");
		difficultyHBox.setAlignment(Pos.TOP_CENTER);
		BorderPane.setAlignment(promptLabel, Pos.CENTER);
		BorderPane.setMargin(promptLabel, new Insets(20));
		difficultyBorderPane.setStyle("-fx-background-color: pink");
		
		return new Scene(difficultyBorderPane,400,200);
	}
	
	//function to create play scene
	public Scene getPlayScene() {
		//message VBox
		msgLabel = new Label("Messages from server");
		msgListView = new ListView<String>();
		msgVBox = new VBox(5,msgLabel,msgListView);
		msgVBox.setAlignment(Pos.CENTER);
		
		//top score VBox
		topScoreLabel = new Label("Top scores");
		topScoreListView = new ListView<String>();
		topScoreVBox = new VBox(5,topScoreLabel,topScoreListView);
		topScoreVBox.setAlignment(Pos.CENTER);
		
		//game grid pane
		l1 = new Label("X");
		l1.setStyle("-fx-font: 76 Arial");
		l2 = new Label("X");
		l2.setStyle("-fx-font: 76 Arial");
		l3 = new Label("X");
		l3.setStyle("-fx-font: 76 Arial");
		l4 = new Label("X");
		l4.setStyle("-fx-font: 76 Arial");
		l5 = new Label("X");
		l5.setStyle("-fx-font: 76 Arial");
		l6 = new Label("X");
		l6.setStyle("-fx-font: 76 Arial");
		l7 = new Label("X");
		l7.setStyle("-fx-font: 76 Arial");
		l8 = new Label("X");
		l8.setStyle("-fx-font: 76 Arial");
		l9 = new Label("X");
		l9.setStyle("-fx-font: 76 Arial");
		
		gameGridPane = new GridPane();
		gameGridPane.add(l1,0,0,1,1);
		gameGridPane.add(l2,1,0,1,1);
		gameGridPane.add(l3,2,0,1,1);
		gameGridPane.add(l4,0,1,1,1);
		gameGridPane.add(l5,1,1,1,1);
		gameGridPane.add(l6,2,1,1,1);
		gameGridPane.add(l7,0,2,1,1);
		gameGridPane.add(l8,1,2,1,1);
		gameGridPane.add(l9,2,2,1,1);
		gameGridPane.setAlignment(Pos.CENTER);
		gameGridPane.setGridLinesVisible(true);
		
		//play again button
		playAgainButton = new Button("Play Again");
		playAgainButton.setAlignment(Pos.CENTER);
		//score labels
		scoreTextLabel = new Label("Your Score: ");
		scoreNumberLabel = new Label("0");
		scoreHBox = new HBox(10,scoreTextLabel,scoreNumberLabel);
		bottomLeftGridVBox = new VBox(10,scoreHBox,playAgainButton);
		
		//scene grid pane
		sceneGridPane = new GridPane();
		sceneGridPane.add(gameGridPane, 0, 0,1,1);
		sceneGridPane.add(msgVBox, 1, 0,1,1);
		sceneGridPane.add(bottomLeftGridVBox, 0, 1,1,1);
		GridPane.setConstraints(bottomLeftGridVBox, 0, 1,1,1,HPos.CENTER,VPos.CENTER);
		sceneGridPane.add(topScoreVBox, 1, 1,1,1);
		
		//configure scene grid pane
		ColumnConstraints cc1 = new ColumnConstraints();
		cc1.setPercentWidth(40);
		ColumnConstraints cc2 = new ColumnConstraints();
		cc2.setPercentWidth(60);
		RowConstraints rc1 = new RowConstraints();
		rc1.setPercentHeight(70);
		RowConstraints rc2 = new RowConstraints();
		rc2.setPercentHeight(30);
		sceneGridPane.getColumnConstraints().addAll(cc1,cc2);
		sceneGridPane.getRowConstraints().addAll(rc1,rc2);
		sceneGridPane.setHgap(10);
		sceneGridPane.setVgap(20);
		sceneGridPane.setPadding(new Insets(20));
		sceneGridPane.setStyle("-fx-background-color: pink");
	
		return new Scene(sceneGridPane,600,600);
	}
	
	//function to refresh the board after each move
	void refreshBoard(GameInfo g) {
		l1.setText(g.stringBoard[0][0].equals("b") ? "   " : g.stringBoard[0][0]);
		l2.setText(g.stringBoard[0][1].equals("b") ? "   " : g.stringBoard[0][1]);
		l3.setText(g.stringBoard[0][2].equals("b") ? "   " : g.stringBoard[0][2]);
		l4.setText(g.stringBoard[1][0].equals("b") ? "   " : g.stringBoard[1][0]);
		l5.setText(g.stringBoard[1][1].equals("b") ? "   " : g.stringBoard[1][1]);
		l6.setText(g.stringBoard[1][2].equals("b") ? "   " : g.stringBoard[1][2]);
		l7.setText(g.stringBoard[2][0].equals("b") ? "   " : g.stringBoard[2][0]);
		l8.setText(g.stringBoard[2][1].equals("b") ? "   " : g.stringBoard[2][1]);
		l9.setText(g.stringBoard[2][2].equals("b") ? "   " : g.stringBoard[2][2]);
	}
	
	//function to enable labels
	void enableLabels() {
		l1.setDisable(false);
		l2.setDisable(false);
		l3.setDisable(false);
		l4.setDisable(false);
		l5.setDisable(false);
		l6.setDisable(false);
		l7.setDisable(false);
		l8.setDisable(false);
		l9.setDisable(false);
	}
	
	//function to disable labels
	void disableLabels() {
		l1.setDisable(true);
		l2.setDisable(true);
		l3.setDisable(true);
		l4.setDisable(true);
		l5.setDisable(true);
		l6.setDisable(true);
		l7.setDisable(true);
		l8.setDisable(true);
		l9.setDisable(true);
	}
	
	//Function to display input error
	void showError(Label label,String msg) {
		/*Pause the program for certain duration*/
		//create an empty event handler to act as a place holder for Timeline
		EventHandler<ActionEvent> empty = e ->{};
		//create a Timeline object to pause the scene
		Timeline pause = new Timeline(new KeyFrame(Duration.millis(2000),empty));
				
		//add the message for 2 seconds then remove it
		label.setText(msg);
		pause.play();
		pause.setOnFinished(q->{
			label.setText("");
		});
	}
		
	//Function to pause for given millis seconds
	void pause(int sec) {
		/*Pause the program for certain duration*/
		//create an empty event handler to act as a place holder for Timeline
		EventHandler<ActionEvent> empty = e ->{};
		//create a Timeline object to pause the scene
		Timeline pause = new Timeline(new KeyFrame(Duration.millis(sec),empty));
		pause.play();
		pause.setOnFinished(q->{});
	}
}

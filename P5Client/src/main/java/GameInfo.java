import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.util.Pair;

//Each client will have its own GameInfo object
//Not like in project 4 that we use only 1 GameInfo project
//throughout the program.
public class GameInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	public String[][] stringBoard = new String[3][3];//2-d array representation of the board
	public String stateString;//the string representation of the game board
	public String msg;//any messages back and forth between server and client
	public String difficulty;
	public String whoWin;//to hold who win the game
	public ArrayList<Pair<String,Integer>> highScores;//hold 3 highest score mapped by their client's name
	public int Id;//to hold ID of client
	
	public int signal;//server tells client what to do, or client tells server what to do
	//list of command signals
	final static int READ_MSG = 1;//tell the receiver(server or client) to read the msg
	final static int READ_MOVE = 2;//tell the receiver to make move
	final static int READ_DIFF = 3;//tell the receiver to read the difficulty
	final static int PLAY_AGAIN = 4;//client tells the server that she will play again
	final static int RESULT = 5; //notify client that someone wins or the result is tie
	final static int SCORE_LIST = 6;//tell clients to update top scores list
	final static int GET_ID = 7;//tell the client to get her Id number
	
	GameInfo(){
		//initialize board to all b's, b is blank
		for(int i=0; i<3;i++) {
			for(int j=0;j<3;j++) {
				stringBoard[i][j] = "b";
			}
		}
	}
	
	public void resetBoard() {
		//reset board to all b's, b is blank
		for(int i=0; i<3;i++) {
			for(int j=0;j<3;j++) {
				stringBoard[i][j] = "b";
			}
		}
	}
	
	//make stateString from a board
	public String boardToString() {
		stateString ="";
		for(int i=0; i<3;i++) {
			for(int j=0;j<3;j++) {
				stateString = stateString.concat(stringBoard[i][j] + " ");
			}
		}
		stateString = stateString.substring(0, stateString.length() - 1);//delete the last 2 spaces
		return stateString;
	}
}

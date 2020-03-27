import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Consumer;

import javafx.util.Pair;


public class Server{
	
	int count = 1;//to create unique id for each client
	Integer clientsNum = 0;//number of clients currently connected to server
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	Integer portNum;
	FindNextMove FNM = new FindNextMove();
	
	//enums for evaluate() function
	enum Result {SV_WIN,CL_WIN,TIE,NO_RESULT};
	
	Server(){}//empty default constructor, used as place holder for testing
	
	Server(Consumer<Serializable> call, Integer portNum){
	
		callback = call;
		this.portNum = portNum;
		server = new TheServer();
		server.start();
	}
	
	/*The class to find the next move with a given game's state */
	public class FindNextMove {
		
		AI_MinMax algorithm;
		ArrayList<Integer> result;
		
		//only 1 client use this function at a time
		public synchronized int getMove(String state, String diff) {

			fnmThread t = new fnmThread(state);
			t.start();
			
			while(t.isAlive());//wait until the thread finishes running
			
			if(result.size() == 0) {//no move can result a win or tie
				return 0;
			}
			else {
				
				//get the list of available spots from the state string
				ArrayList<Integer> avai = new ArrayList<Integer>();
				String delim = "[ ]+";
				String[] parsedString = state.split(delim);
				for(int i = 0; i < parsedString.length;i++) {
					if(parsedString[i].equals("b")) {
						avai.add(i+1);
					}
				}
				
				if(diff.equals("expert")) {//expert mode: always follow the algorithm
					//create a random int that is within index bound of the ArrayList
					int ranInt =( (int) (Math.random()*10) ) % result.size();
					
					return result.get(ranInt);
				}
				else if(diff.equals("medium")){//medium mode: follow the algorithm at 50$ chance
					
					//create a random number to only follow the algorithm at 50% chance
					Double randDouble = Math.random();
					
					if(randDouble < 0.5) {//follow the algorithm only if the random number is < 0.5 (imply 50% here)
						//create a random int that is within index bound of the ArrayList
						int ranInt =( (int) (Math.random()*10) ) % result.size();
						return result.get(ranInt);
					}
					else {//pick the move randomly
						
						//create a random int that is within index bound of the ArrayList
						int ranInt =( (int) (Math.random()*10) ) % avai.size();
						return avai.get(ranInt);
					}
						
				}//end else-if : medium
				
				else {//easy mode: always pick moves randomly
					
					//create a random number to only follow the algorithm at 20% chance
					Double randDouble = Math.random();
					
					if(randDouble < 0.2) {//follow the algorithm only if the random number is < 0.2 (imply 20% here)
						//create a random int that is within index bound of the ArrayList
						int ranInt =( (int) (Math.random()*10) ) % result.size();
						return result.get(ranInt);
					}
					else {//pick the move randomly
						
						//create a random int that is within index bound of the ArrayList
						int ranInt =( (int) (Math.random()*10) ) % avai.size();
						return avai.get(ranInt);
					}
					
				}//end easy mode
				
			}//end else: if(result.isEmpty())
			
		}//end getMove
		
		public class fnmThread extends Thread{
			String state;
			
			fnmThread(String s){
				state = s;
			}
			
			public void run() {
				algorithm = new AI_MinMax(state);

				result = algorithm.printBestMoves();
			}
		}
	}
	
	
	public class TheServer extends Thread{
		
		public void run() {
		
			try(ServerSocket mysocket = new ServerSocket(portNum);){
			    callback.accept("Server is waiting for a client!");
			    System.out.println("Server is waiting for a client!");
			    while(true) {
			    	//keep accepting clients
					ClientThread c = new ClientThread(mysocket.accept(), count);
					callback.accept("client has connected to server: " + "client #" + count);
					clients.add(c);
					c.start();
					count++;
					System.out.println("Count is "+ count);
					clientsNum++;	
				}//end of while
			    
			}//end of try
			
			catch(Exception e) {
					callback.accept("Server socket did not launch");
			}
		}//end of run
	}
	

	class ClientThread extends Thread{
			
		
		Socket connection;
		int id;
		int score = 0;//winning 1 game will earn 1 point
		ObjectInputStream in;
		ObjectOutputStream out;
		GameInfo gameInfo = new GameInfo();
		String difficulty;
		String state; // to store the game state
		int nextMove;
			
		ClientThread(Socket s, int count){
			this.connection = s;
			this.id = count;	
		}
		
		//function to tell all clients to update score list
		public void updateClientsScoreList() {
			
			ArrayList<Pair<String,Integer>> highest3 = getHighest3(clients);
			
			for(int i = 0; i < clients.size(); i++) {
				ClientThread t = clients.get(i);
				t.gameInfo.signal = GameInfo.SCORE_LIST;
				t.gameInfo.highScores = highest3;
				try {
				t.out.writeObject(t.gameInfo);
				t.out.reset();
				}
				catch(Exception e) {}
			}
		}
		
			
		public void run(){
					
			try {
				in = new ObjectInputStream(connection.getInputStream());
				out = new ObjectOutputStream(connection.getOutputStream());
				connection.setTcpNoDelay(true);	
			
				//send welcome string to client
				gameInfo.signal = GameInfo.READ_MSG;
				gameInfo.msg = "Welcome to Tik Tak To";
				out.writeObject(gameInfo);
				out.reset();
				
				//send Id number to client
				gameInfo.signal = GameInfo.GET_ID;
				gameInfo.Id = id;
				out.writeObject(gameInfo);
				out.reset();
				
				//tell clients to update score list
				updateClientsScoreList();
			}
			catch(Exception e) {
				System.out.println("Streams not open");
			}
				
					
			while(true) {
				try {
					gameInfo = (GameInfo)in.readObject();
					
					if(gameInfo.signal == GameInfo.READ_DIFF) {
						difficulty = gameInfo.difficulty;
						gameInfo.resetBoard();//reset the board
						callback.accept("New game: Client " + id + " sets the difficulty " + difficulty.toUpperCase());
					}
					
					if(gameInfo.signal == GameInfo.READ_MOVE) {
						state = gameInfo.boardToString();//get the state string from the board
						
						//game is not done
						if(evaluate(gameInfo) == Result.NO_RESULT ) {
							callback.accept("Client " + id + " made a move: No one wins yet.");
						
							nextMove = FNM.getMove(state,difficulty);//pass state string to algorithm to determine next move
							
							if(nextMove == 0) { //make a random move, because server will lose anyway
								
								nextMove = getRandMove(gameInfo);//get a random move
							}
							
							setSVMove(gameInfo, nextMove);//set the next move to the board
							
							//evaluate the board again, if no result, tell the client to make move
							if(evaluate(gameInfo) == Result.NO_RESULT) {
								//tell client to make move
								gameInfo.signal = GameInfo.READ_MOVE;
								out.writeObject(gameInfo);
								out.reset();
								callback.accept("Server made a move to client " + id + " : No one wins yet.");
							}
							else {
								if(evaluate(gameInfo) == Result.SV_WIN) {
									gameInfo.whoWin = "server";
									callback.accept("Server made a move to client " + id + " and wins.");
									
								}
								else if(evaluate(gameInfo) == Result.CL_WIN) {//should never run this code
									gameInfo.whoWin = "client";
									score++;//increase the score by 1
									
									//tell clients to update score list
									updateClientsScoreList();
								}
								else {
									gameInfo.whoWin = "tie";
									callback.accept("Server made a move to client " + id + ". Resulting tie.");
									
								}
								gameInfo.signal = GameInfo.RESULT;
								out.writeObject(gameInfo);//send result to client
								out.reset();
							}
						}
						else {
							if(evaluate(gameInfo) == Result.CL_WIN) {
								gameInfo.whoWin = "client";
								score++;//increase the score by 1
								callback.accept("Client " + id + " made a move, and client wins.");
								
								//tell clients to update score list
								updateClientsScoreList();
							}
							else if(evaluate(gameInfo) == Result.SV_WIN) {//should never run this code
								gameInfo.whoWin = "server";
								callback.accept("Server made a move to client " + id + " and wins.");
							
							}
							else {
								gameInfo.whoWin = "tie";
								callback.accept("Client " + id + " made a move. Resulting tie.");
								
							}
							gameInfo.signal = GameInfo.RESULT;
							out.writeObject(gameInfo);//send result to client
							out.reset();
						}
					
					}//end if: READ_MOVE
					    	
				}
				catch(Exception e) {
					e.printStackTrace();
					callback.accept("OOOOPPs...Something wrong with the socket from client: " + id + "....closing down!");
					clients.remove(this);
					break;
				}
			}//end of while
		}//end of run
			
		//function to set server's move on the board
		void setSVMove(GameInfo gi, int move) {
			
			switch (move) {
			case 1:
				gi.stringBoard[0][0] = "X";
				break;
			case 2:
				gi.stringBoard[0][1] = "X";
				break;
			case 3:
				gi.stringBoard[0][2] = "X";
				break;
			case 4:
				gi.stringBoard[1][0] = "X";
				break;
			case 5:
				gi.stringBoard[1][1] = "X";
				break;
			case 6:
				gi.stringBoard[1][2] = "X";
				break;
			case 7:
				gi.stringBoard[2][0] = "X";
				break;
			case 8:
				gi.stringBoard[2][1] = "X";
				break;
			case 9:
				gi.stringBoard[2][2] = "X";
				break;
			}
			//convert board to string
			gi.boardToString();
		}//end setSVMove
		
		//function to evaluate game result
		Result evaluate(GameInfo gi) {
			if ( (gi.stringBoard[0][0].equals("X") && gi.stringBoard[0][1].equals("X") && gi.stringBoard[0][2].equals("X"))//1st row
				|| (gi.stringBoard[1][0].equals("X") && gi.stringBoard[1][1].equals("X") && gi.stringBoard[1][2].equals("X"))//2nd row
				|| (gi.stringBoard[2][0].equals("X") && gi.stringBoard[2][1].equals("X") && gi.stringBoard[2][2].equals("X"))//3rd row
				|| (gi.stringBoard[0][0].equals("X") && gi.stringBoard[1][0].equals("X") && gi.stringBoard[2][0].equals("X"))//1st col
				|| (gi.stringBoard[0][1].equals("X") && gi.stringBoard[1][1].equals("X") && gi.stringBoard[2][1].equals("X"))//2nd col
				|| (gi.stringBoard[0][2].equals("X") && gi.stringBoard[1][2].equals("X") && gi.stringBoard[2][2].equals("X"))//3rd
				|| (gi.stringBoard[0][0].equals("X") && gi.stringBoard[1][1].equals("X") && gi.stringBoard[2][2].equals("X"))//1st diagonal
				|| (gi.stringBoard[0][2].equals("X") && gi.stringBoard[1][1].equals("X") && gi.stringBoard[2][0].equals("X"))//2nd diagonal
			)
			{	
				return Result.SV_WIN; // server wins
			}
			else if ( (gi.stringBoard[0][0].equals("O") && gi.stringBoard[0][1].equals("O") && gi.stringBoard[0][2].equals("O"))//1st row
					|| (gi.stringBoard[1][0].equals("O") && gi.stringBoard[1][1].equals("O") && gi.stringBoard[1][2].equals("O"))//2nd row
					|| (gi.stringBoard[2][0].equals("O") && gi.stringBoard[2][1].equals("O") && gi.stringBoard[2][2].equals("O"))//3rd row
					|| (gi.stringBoard[0][0].equals("O") && gi.stringBoard[1][0].equals("O") && gi.stringBoard[2][0].equals("O"))//1st col
					|| (gi.stringBoard[0][1].equals("O") && gi.stringBoard[1][1].equals("O") && gi.stringBoard[2][1].equals("O"))//2nd col
					|| (gi.stringBoard[0][2].equals("O") && gi.stringBoard[1][2].equals("O") && gi.stringBoard[2][2].equals("O"))//3rd
					|| (gi.stringBoard[0][0].equals("O") && gi.stringBoard[1][1].equals("O") && gi.stringBoard[2][2].equals("O"))//1st diagonal
					|| (gi.stringBoard[0][2].equals("O") && gi.stringBoard[1][1].equals("O") && gi.stringBoard[2][0].equals("O"))//2nd diagonal
			)
			{
				return Result.CL_WIN; //client wins
			}
			else if(gi.boardToString().contains("b")) {
				return Result.NO_RESULT;//game is not done
			}
			else {
				return Result.TIE; //it's tie
			}
		}//end of evaluate
		
		//function to get random move from any available spot
		int getRandMove(GameInfo gi) {
			ArrayList<Integer> avaiSpots = new ArrayList<Integer>();//hold available spots
			
			for(int i=0;i<3;i++) {
				for(int j=0;j<3;j++) {
					if(gi.stringBoard[i][j].equals("b"))
						avaiSpots.add(i*3 + j + 1);
				}
			}
			
			//create a random int that is within index bound of the ArrayList
			int randNum =( (int) (Math.random() * 10) ) % avaiSpots.size();
			
			if(randNum == 0)//if there no available spot 
				return 0;
			
			return avaiSpots.get(randNum);
		}
		
		/*function to get the 3 highest score and return a hash map
		 * that'd map the client's name to his/her score
		 */
		ArrayList<Pair<String,Integer>> getHighest3(ArrayList<ClientThread> list){
			
			@SuppressWarnings("unchecked")
			ArrayList<Pair<String,Integer>> result = new ArrayList<Pair<String,Integer>>();//to store the resulting hash map
			
			Collections.sort(list,new SortByScore());//sort the list of clients by their score
			
			if(list.size() < 4) {//list has 3 clients or less: everyone will be on the high score list
				for(ClientThread c : list) {
					result.add(new Pair<String,Integer>("client " + c.id,c.score));
				}
				return result;
			}
			else {
				result.add(new Pair<String,Integer>("client " + list.get(0).id, list.get(0).score));
				result.add(new Pair<String,Integer>("client " + list.get(1).id, list.get(1).score));
				result.add(new Pair<String,Integer>("client " + list.get(2).id, list.get(2).score));
				return result;
			}
		}
		
		//class to compare ClientThread objects by their score's
		class SortByScore implements Comparator<ClientThread>{
			@Override
			public int compare(ClientThread c1, ClientThread c2) {
				return c2.score - c1.score;//sort by descending order
			}
		}
		
	}//end of client thread
}


	
	

	

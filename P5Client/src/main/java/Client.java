import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

import javafx.util.Pair;



public class Client extends Thread{

	
	Socket socketClient;
	
	ObjectOutputStream out;
	ObjectInputStream in;
	GameInfo gameInfo;
	int score = 0;//winning 1 game will earn 1 point
	int id;
	
	private Consumer<Serializable> callback;//for msg list view
	private Consumer<Serializable> callback2;//for top scores list view
	
	Client(Consumer<Serializable> call, Consumer<Serializable> call2, Socket clientSckt){
	
		callback = call;
		callback2 = call2;
		socketClient = clientSckt;
		try {
			out = new ObjectOutputStream(socketClient.getOutputStream());
			in = new ObjectInputStream(socketClient.getInputStream());
		}catch(Exception e) {}
	}
	
	public void run() {
		
		while(true) {
			 
			try {
				gameInfo = (GameInfo)in.readObject();
				
				//update the highest scores list
				if(gameInfo.signal == GameInfo.SCORE_LIST) {
					callback2.accept("clear");//tell the GUI to clear the list
					
					for(Pair<String, Integer> p : gameInfo.highScores) {// keySet() will return a list of keys
						callback2.accept(p.getKey() + " score : " + p.getValue());
					}
				}
				
				if(gameInfo.signal == GameInfo.READ_MSG) {
					callback.accept(gameInfo.msg);
				}
				
				if(gameInfo.signal == GameInfo.GET_ID) {
					id = gameInfo.Id;
				}
				
				if(gameInfo.signal == GameInfo.READ_MOVE) {
					callback.accept("refresh");//tell the GUI to refresh the board
					callback.accept("Please make your move!!!");
				}
				
				if(gameInfo.signal == GameInfo.RESULT) {
					
					callback.accept("disable");//tell the GUI to disable labels
					
					if(gameInfo.whoWin.equals("server")) {
						callback.accept("sv_win");//to tell the GUI to update board
						callback.accept("Sorry, you lost. Try again or give up?");
					}
					else if(gameInfo.whoWin.equals("client")) {
						score++;//increase the score by 1
						callback.accept("update_score");
						callback.accept("Congrats, you won. Try again or quit?");
					}
					else {
						callback.accept("Phew! It's tie. Try again or quit?");
					}
					
				}//end if: RESULT
				
			}
			catch(Exception e) {}
		}
	
    }
	
	//ClientGUI uses this to communicate with Client
	public GameInfo getGameInfo() {
		return gameInfo;
	}
	
	//function to get client's ID
	public int getID() {
		return id;
	}
}

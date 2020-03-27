import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class is used to read in a state of a tic tac toe board. It creates a MinMax object and passes the state to it. What returns is a list 
 * of possible moves for the player X that have been given min/max values by the method findMoves. The moves that can result in a win or a 
 * tie for X are printed out with the method printBestMoves()
 * 
 * @author Mark Hallenbeck
 *
 * Copyright© 2014, Mark Hallenbeck, All Rights Reservered.
 *
 */
public class AI_MinMax{//changed from not extending Thread to extending Thread
	
	private String[] init_board;
	
	private ArrayList<Node> movesList;
	
	//newly added constructor
	AI_MinMax(String receivedState){
		init_board = getBoard(receivedState);
		
		if(init_board.length != 9)
		{
			System.out.println("You have entered an invalid state for tic tac toe, exiting......");
			System.exit(-1);
		}
		
		MinMax sendIn_InitState = new MinMax(init_board);
		
		movesList = sendIn_InitState.findMoves();
		
		//printBestMoves();//will be deleted
	}
	
	/**
	 * reads in a string from user and parses the individual letters into a string array
	 * @return String[]
	 */
	private String[] getBoard(String puzzle)// changed from no argument to 1 argument
	{
			//String puzzle; //this will be deleted
			String[] puzzleParsed;
			String delim = "[ ]+";
			
			/*
			//give input message
			System.out.println("Enter a string to represent the board state:");
			
			Scanner userInput = new Scanner(System.in);		//open scanner
			
			puzzle = userInput.nextLine();					//scan in string
			*///These will be deleted
			
			puzzleParsed = puzzle.split(delim);
			//userInput.close();   	  						//close scanner
			
			return puzzleParsed;
			
	}
	
	/**
	 * goes through a node list and prints out the moves with the best result for player X
	 * checks the min/max function of each state and only recomends a path that leads to a win or tie
	 */
	public ArrayList<Integer> printBestMoves()//this function is modified to return an ArrayList<Integer> that contains moves
	{
		//System.out.print("\n\nThe moves list is: < ");//will be deleted
		ArrayList<Integer> result = new ArrayList<Integer>();//to hold resulting moves
		
		for(int x = 0; x < movesList.size(); x++)
		{
			Node temp = movesList.get(x);
			
			if(temp.getMinMax() == 10 || temp.getMinMax() == 0)
			{
				//System.out.print(temp.getMovedTo() + " ");//will be deleted
				result.add(temp.getMovedTo());
			}
		}
		
		return result;
	}
}

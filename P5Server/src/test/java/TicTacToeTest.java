import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TicTacToeTest {

	@Test
	//test AI_MinMax class constructor
	void testClass_AI_MinMax() {
		String state = "b b b b b b b b b";//new blank state
		AI_MinMax ai = new AI_MinMax(state);
		assertEquals("AI_MinMax", ai.getClass().getName(), "AI_MinMax instance is not properly initialized");
	}
	
	
	@Test
	//test MinMax class constructor
	void testClass_MinMax() {
		String[] parsedState = { "b","b","b","X","b","b","X","b","O"};
		MinMax mm = new MinMax(parsedState);
		assertEquals("MinMax", mm.getClass().getName(), "MinMax instance is not properly initialized");
	}
	
	@Test
	//test Node class constructor
	void testClass_Node() {
		String[] parsedState = { "b","b","b","X","b","b","X","b","O"};
		Node n = new Node(parsedState, 0);
		assertEquals("Node", n.getClass().getName(), "Node instance is not properly initialized");
	}
	
	/*Test MinMax algorithm with different inputs*/
	 
	@Test
	//Test if the algorithm return the right number of available spots
	void testAlgorithm1() {
		String state = "X b b b b b b b b";
		Server s = new Server();
		Server.FindNextMove fnm = s.new FindNextMove();
	
		Server.FindNextMove.fnmThread t = fnm.new fnmThread(state);
		
		t.start();
		while(t.isAlive());//wait until the thread finishes running
		
		ArrayList<Integer> result = fnm.result;
		
		assertEquals(8,result.size(), "MinMax algorithm does not return the correct number of available spots");
	}
	
	@Test
	//Test if the algorithm return 0 when the server can't win or tie
	void testAlgorithm2() {
		String state = "O X b X X O b O O";//the game's state that the server cannot win
		Server s = new Server();
		Server.FindNextMove fnm = s.new FindNextMove();
	
		int move = fnm.getMove(state, "expert");//only in expert mode we fully follow the algorithm
		
		assertEquals(0,move, "MinMax algorithm does not return the correct next move");
	}
	
	@Test
	//Test if the algorithm return the right list of moves(based on Professor.Hallenbeck algorithm)
	void testAlgorithm3() {
		String state = "b b O b b X O b b";//the game's state that will result in move = 5;
		Server s = new Server();
		Server.FindNextMove fnm = s.new FindNextMove();
	
		int move = fnm.getMove(state, "expert");//only in expert mode we fully follow the algorithm
		
		assertEquals(5,move, "MinMax algorithm does not return the correct next move");
	}
	
	@Test
	//Test if the algorithm return the right list of moves(based on Professor.Hallenbeck algorithm)
	void testAlgorithm4() {
		String state = "X b X O O X O O b";//the game's state that will result in result = {2,9};
		Server s = new Server();
		Server.FindNextMove fnm = s.new FindNextMove();
	
		Server.FindNextMove.fnmThread t = fnm.new fnmThread(state);
		
		t.start();
		while(t.isAlive());//wait until the thread finishes running
		
		ArrayList<Integer> result = fnm.result;
		
		assertEquals(2,result.get(0), "MinMax algorithm does not return the correct list of moves");
		assertEquals(9,result.get(1), "MinMax algorithm does not return the correct list of moves");
	}
	
	@Test
	//Test if the algorithm return the right list of moves(based on Professor.Hallenbeck algorithm)
	void testAlgorithm5() {
		String state = "b O b b b b b b b";//the game's state that will result in result = {1,2,5,8};
		Server s = new Server();
		Server.FindNextMove fnm = s.new FindNextMove();
	
		Server.FindNextMove.fnmThread t = fnm.new fnmThread(state);
		
		t.start();
		while(t.isAlive());//wait until the thread finishes running
		
		ArrayList<Integer> result = fnm.result;
		
		assertEquals(1,result.get(0), "MinMax algorithm does not return the correct list of moves");
		assertEquals(3,result.get(1), "MinMax algorithm does not return the correct list of moves");
		assertEquals(5,result.get(2), "MinMax algorithm does not return the correct list of moves");
		assertEquals(8,result.get(3), "MinMax algorithm does not return the correct list of moves");
	}
}

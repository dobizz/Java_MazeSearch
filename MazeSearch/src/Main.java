

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;

public class Main {

	static boolean VERBOSE = false;

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		String fileName = "input";
		//open file stream
		Scanner inFile = new Scanner(new FileReader(fileName));
		char[][] input_map;
		String currentLine;

		int loops = inFile.nextInt();

		for(int maps = 0; maps < loops; maps++){
			int rows = inFile.nextInt();
			int cols = inFile.nextInt();
			input_map = new char[rows][cols];

			inFile.nextLine();

			for(int i = 0; i < rows; i++){
				currentLine = inFile.nextLine();
				input_map[i] = currentLine.toCharArray();
				if(VERBOSE) System.out.println(currentLine);
			}
			MapSearch map = new MapSearch(input_map);
			System.out.println("Case " + (maps+1) +":");
			//travelMap();
			if(VERBOSE) map.display();
			map.printStats();
		}
		//close file stream
		inFile.close();
	}

}

class MapSearch{
	private static boolean VERBOSE = false;
	private static int level = 0;
	private static int errCount = 0;
	char[][] map;
	int row = 0;
	int col = 0;
	int max_row;
	int max_col;

	Stack<Integer> row_stack = new Stack<Integer>();
	Stack<Integer> col_stack = new Stack<Integer>();
	Stack<Integer> row_log = new Stack<Integer>();
	Stack<Integer> col_log = new Stack<Integer>();
	Stack<Character> army_stack = new Stack<Character>();
	Scanner console = new Scanner(System.in);

	private static final char MOUNTAIN = '#';
	//private static final char LAND = '.';
	private static final char TRAIL = '+';
	private static final char CONTESTED = '~';

	enum Direction {
		NORTH, SOUTH, EAST, WEST
	}


	public MapSearch(char[][] input){
		map = input;
		max_row = map.length - 1;
		max_col = map[0]. length - 1;
		scanLetters();
	}

	public void display(){
		for(int row = 0; row <= max_row; row++){
			for(int col = 0; col <= max_col; col++){
				System.out.print(map[row][col]);
			}
			System.out.println();
		}
	}

	public void printStats(){
		Stack<Character> armyList = new Stack<Character>();
		Stack<Character> keyStack = new Stack<Character>();
		int items = 0;
		int keyCount = 0;
		char[] armyArray;

		char temp = '\0';
		try{
			while(!col_stack.empty() && !row_stack.empty()){
				explore(row_stack.pop(), col_stack.pop());
				if(!army_stack.empty()){
					temp = evalArmy();
					armyList.push(temp);
					keyStack.push(temp);
					items++;
					keyCount++;
				}

				if(!keyStack.empty()){
					temp = keyStack.pop();
					keyCount--;
					if(keyStack.search(temp) < 0){
						keyStack.push(temp);
						keyCount++;
					}
				}
			}
			char[] keys = new char[keyCount];
			armyArray = new char[items];
			//display(); //plot traversed map
			while(!keyStack.empty()){
				keys[--keyCount] = keyStack.pop();
			}

			while(!armyList.empty()){
				armyArray[--items] = armyList.pop();
			}
			Arrays.sort(keys);
			printFinalStats(armyArray, keys);

		}
		catch (StackOverflowError e) {
			if(VERBOSE) System.err.println("true recursion level was "+level);
			if(VERBOSE) System.err.println("reported recursion level was "+e.getStackTrace().length + " " +e.getMessage() );
			System.out.println("contested 0"); 	// Unable to get rid of recursion overflow error
												// Some maps just have too much branching for my current computer system to handle :(
												// Used to indicate an invalid output
			errCount++;
		}

		finally{

			if(VERBOSE) 
				System.out.println("errors found: " + errCount);
	
		}
	}

	private void printFinalStats(char[] items, char[] keys){
		int[] tally = new int[keys.length];

		for(int k = 0; k < keys.length; k++){
			for(int i = 0; i < items.length; i++){
				if(keys[k] == items[i])
					tally[k]++;
			}
		}

		for(int x = 0; x < keys.length; x++){
			if(keys[x] == CONTESTED)
				System.out.println("contested " + tally[x]);
			else
				System.out.println(keys[x] +" " + tally[x]);
		}
	}

	private char evalArmy(){
		char army = CONTESTED;
		boolean check = true;
		while(!army_stack.empty()){
			army = army_stack.pop();
			if(check){
				if(!army_stack.empty() && army_stack.search(army) < 1){
					army = CONTESTED;	
					check = false;
					while(!army_stack.empty()){
						army_stack.pop();
					}
				}
			}		
		}
		return army;
	}

	private void scanLetters(){
		char c;
		for(int row = 0; row <= max_row; row++){
			for(int col = 0; col <= max_col; col++){
				c = map[row][col];
				if(Character.isLetter(c)){
					col_stack.push(col);
					row_stack.push(row);
				}
			}
		}
	}

	private void explore(int row, int col){
		boolean N = false;
		boolean S = false;
		boolean E = false;
		boolean W = false;
		boolean stuck = false;
		char temp = '0';
		temp = map[row][col];
		if(Character.isLetter(map[row][col])){
			army_stack.push(temp);
		}
		map[row][col] = TRAIL;
		while(!stuck){
			N = S = E = W = false;

			if(move(Direction.NORTH, row , col)){
				N = true;
				if(VERBOSE) System.out.println("Move N");
				explore(row-1,col);
			}
			else if(move(Direction.EAST, row , col)){
				E = true;
				if(VERBOSE) System.out.println("Move E");
				explore(row,col+1);
			}
			else if(move(Direction.SOUTH, row , col)){
				S = true;
				if(VERBOSE) System.out.println("Move S");
				explore(row+1,col);
			}
			else if(move(Direction.WEST, row , col)){
				W = true;
				if(VERBOSE) System.out.println("Move W");
				explore(row,col-1);
			}
			else if(!(N || S || E || W))
				stuck = true;
		}
	}

	private boolean move(Direction dir,int row, int col){
		boolean moveOk = false;

		switch(dir){

		case NORTH :
			if(0 < row)
				if(map[row-1][col] != MOUNTAIN)
					if((map[row-1][col] != TRAIL)){
						if(VERBOSE) System.out.println("Move N OK");
						moveOk = true;
					}
			break;
		case SOUTH:
			if(row < max_row)
				if(map[row+1][col] != MOUNTAIN)
					if((map[row+1][col] != TRAIL)){
						if(VERBOSE) System.out.println("Move S OK");
						moveOk = true;
					}
			break;
		case EAST:
			if(col < max_col)
				if(map[row][col+1] != MOUNTAIN)
					if((map[row][col+1] != TRAIL)){
						if(VERBOSE) System.out.println("Move E OK");
						moveOk = true;
					}
			break;
		case WEST:
			if(0 < col)
				if(map[row][col-1] != MOUNTAIN)
					if((map[row][col-1] != TRAIL)){
						if(VERBOSE) System.out.println("Move W OK");
						moveOk = true;
					}
			break;
		}
		return moveOk;
	}

}

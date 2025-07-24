package slidinggame;

public class SlidingGameState  {

	/** The current board */
	int [][] board;

	/** String maintaining series of moves */
	private String sequence;

	/** Dimension of board */
	private int size;

	/** Row number of empty spot */
	private int emptyI;

	/** Column number of empty spot */
	private int emptyJ;

	/** Number of moves made */
	private int numberMoves;

	/**
	 * Method that returns true if the two boards are equal, false otherwise
	 * @param inSG The game to compare to
	 * @return True if they are equal, false otherwise
	 */
	public boolean equals(SlidingGameState inSG)
	{
		// If empty spots are not the same, return false
		if (emptyI != inSG.emptyI || emptyJ != inSG.emptyJ)
			return false;
		// If any of the tiles don't match, return false
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				if (board[i][j] != inSG.board[i][j])
					return false;
		// Otherwise return true: two boards are equal
		return true;
	}

	/**
	 * Check if move is valid
	 * @param iOffset The new row number
	 * @param jOffset The new column number
	 * @return True if the new position is valid, false otherwise
	 */
	public boolean isValid(int iOffset, int jOffset)
	{
		int startI = iOffset + emptyI;
		int startJ = jOffset + emptyJ;
		return startI >= 0 && startI < size && startJ >= 0 && startJ < size;
	}

	/**
	 * Constructor
	 * @param newSize Size of the board
	 * @param initialArray Initial configuration of tiles as single array
	 */
	public SlidingGameState(int newSize, int [] initialArray)
	{
		setSize(newSize);
		int index = 0;
		board = new int[size][size];
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
			{
				board[i][j] = initialArray[index];
				if (initialArray[index] == 0)
				{
					emptyI = i;
					emptyJ = j;
				}
				index++;
			}
		sequence = "";
		numberMoves = 0;
	}

	/**
	 * Setter for size
	 * @param newSize new size of game
	 */
	public void setSize(int newSize)
	{
		size = newSize;
	}

	/**
	 * Move the token in one direction
	 * @param upDownMove +1 indicates down, -1 indicates up
	 * @param leftRightMove +1 indicates right, -1 indicates left
	 * @throws Exception If the new location offset more than one or in both directions simultaneously
	 */
	public void moveToken(int upDownMove, int leftRightMove) throws Exception
	{
		if (upDownMove < -1 || upDownMove > 1 || leftRightMove < -1 || leftRightMove > 1)
			throw new Exception("Slide operation cannot be more than one space");
		if (upDownMove != 0 && leftRightMove != 0)
			throw new Exception("You can only move in one direction at a time");
		int newI = upDownMove + emptyI;
		int newJ = leftRightMove + emptyJ;
		board[emptyI][emptyJ] = board[newI][newJ];
		board[newI][newJ] = 0;
		numberMoves++;
		sequence += ("Move " + board[emptyI][emptyJ] + " at [" + newI + "," + 
				newJ + "] to [" + emptyI + "," + emptyJ + "]\n");
		sequence += toString();
		sequence += ("Number moves: " + numberMoves + "\n");
		emptyI = newI;
		emptyJ = newJ;

	}

	/**
	 * Copy constructor
	 * @param original The current state of the game to be copied
	 */
	public SlidingGameState(SlidingGameState original)
	{
		setSize(original.size);
		board = new int[size][size];
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				board[i][j] = original.board[i][j];
		emptyJ = original.emptyJ;
		emptyI = original.emptyI;
		sequence = original.sequence;
		numberMoves = original.numberMoves;
	}

	/**
	 * Checks to see if the game has been solved
	 * @return True if the game is solved, false otherwise
	 */
	public boolean isWin()
	{
		int tileNumber = 1;
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				// Last spot must be 0
				if (i == size - 1 && j == size - 1)
				{
					if (board[i][j] != 0)
						return false;
				}
				// All other tiles must be in order from 1 through N^2-1
				else if (board[i][j] != tileNumber)
					return false;
				tileNumber++;
			}
		}
		return true;
	}

	/**
	 * Return the string corresponding to the current sequence of moves
	 * @return The string of moves
	 */
	public String getSequence()
	{
		return sequence;

	}

	public String toString()
	{
		String s= "";
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
				s += (board[i][j] + "\t");
			s += "\n";
		}
		return s;
	}

}

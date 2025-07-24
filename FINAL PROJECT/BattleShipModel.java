package battleShip;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * This class holds all the game logic and internal state for battleship. It
 * manages players' boards, ship placement, fire selection, turn logic, and win
 * condition checking.
 * 
 * @author Matthew Washburn, Peyton Baker
 * @version Spring 2025
 * 
 */
public class BattleShipModel {

	// A helper object to handle observer pattern behavior
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/**
	 * Defines the possible states that a single grid cell on the board can be in.
	 * this helps the model and view determine what is visually shown and how game
	 * logic reacts.
	 */
	public enum gridState {
		// the possible states each grid space can be in
		Hit("Hit"), Miss("Miss"), Empty("Empty"), Ship("PlacingShip"), Sunk("SunkShip");

		private final String mGridState;

		gridState(String gridstate) {
			mGridState = gridstate;
		}

		public String toString() {
			return mGridState;
		}
	}

	// For placing ships
	private GridSpace[][] player1Board;
	private GridSpace[][] player2Board;
	// For playing the game
	private GridSpace[][] player1View;
	private GridSpace[][] player2View;
	// Global game state variables
	private int currentPlayer = 1;
	private boolean placingShips = true;
	private int currentShipIndex = 0;
	private int currentDirection = 1;
	private boolean singlePlayer = false;
	private int player1ShipsSunk = 0;
	private int player2ShipsSunk = 0;
	private int nextShipId = 1;
	private Map<Integer, Ship> ships = new HashMap<>();
	private boolean isGameOver = false;
	// All possible ship lengths and their corresponding names
	private static final int[] SHIP_SIZES = { 5, 4, 3, 3, 2 };
	private static final String[] SHIP_NAMES = { "Aircraft Carrier (Length: 5)", "Battleship (Length: 4)",
			"Submarine (Length: 3)", "Cruiser (Length: 3)", "Destroyer (Length: 2)" };

	/**
	 * 
	 */
	public BattleShipModel() {
		initializeBoards();
	}

	/**
	 * Initalizes both players exposed ship placing boards and hidden battle boards
	 */
	private void initializeBoards() {
		// initialize player 1's internal board
		player1Board = new GridSpace[10][10];
		// initialize player 2's internal board
		player2Board = new GridSpace[10][10];
		// initialize player 1's visible view of opponent's board
		player1View = new GridSpace[10][10];
		// initialize player 2's visible view of opponent's board
		player2View = new GridSpace[10][10];
		// puts a gridspace object into every space for every board
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				player1Board[i][j] = new GridSpace();
			}
		}
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				player2Board[i][j] = new GridSpace();
			}
		}

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				player1View[i][j] = new GridSpace();
			}
		}
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				player2View[i][j] = new GridSpace();
			}
		}
	}

	/**
	 * Handles each cell being clicked, treats as a ship placement or fire for
	 * current player depending on if the current phase is placing ships or battling
	 * 
	 * @param row
	 * @param col
	 */
	public void cellClicked(int row, int col) {
		if (currentPlayer == 1 && placingShips == true) {
			handleShipPlacement(row, col, currentDirection, player1Board);
		} else if (currentPlayer == 2 && placingShips == true) {
			handleShipPlacement(row, col, currentDirection, player2Board);
		} else if (currentPlayer == 1 && placingShips == false) {
			handleFire(row, col, player1View, player2Board);
		} else if (currentPlayer == 2 && placingShips == false) {
			handleFire(row, col, player2View, player1Board);
		} else {
			// Do nothing
		}
	}

	/**
	 * @param coords
	 * @return The state of each grid space for the main view of your own exposed
	 *         board during ship placement phase, and the opponents hidden board
	 *         during the battle phase
	 */
	public gridState getCoordState(Integer[] coords) {
		if (currentPlayer == 1 && placingShips == true) {
			return player1Board[coords[0]][coords[1]].getGridState();
		} else if (currentPlayer == 2 && placingShips == true) {
			return player2Board[coords[0]][coords[1]].getGridState();
		} else if (currentPlayer == 1 && placingShips == false) {
			return player1View[coords[0]][coords[1]].getGridState();
		} else if (currentPlayer == 2 && placingShips == false) {
			return player2View[coords[0]][coords[1]].getGridState();
		} else {
			return null;
		}
	}

	/**
	 * @param coords
	 * @return The state of each grid space for the smaller view of your own board
	 *         during the battle phase
	 */
	public gridState getYourCoordState(Integer[] coords) {
		if (currentPlayer == 1) {
			return player2View[coords[0]][coords[1]].getGridState();
		} else if (currentPlayer == 2) {
			return player1View[coords[0]][coords[1]].getGridState();
		} else {
			return null;
		}
	}

	/**
	 * Handles ship placed action, checks if it was a valid placement, when last
	 * ship of each player is placed it changes turn, and once player two finishes
	 * it moves to the battle portion of the game
	 * 
	 * @param row
	 * @param col
	 * @param direction
	 * @param board
	 */
	public void handleShipPlacement(int row, int col, int direction, GridSpace[][] board) {
		if (tryPlaceShip(row, col, direction, board)) {
			currentShipIndex++;
			pcs.firePropertyChange("placed", null, currentPlayer);
		}
		if (currentShipIndex >= SHIP_SIZES.length) {
			if (currentPlayer == 1) {
				currentShipIndex = 0;
				if (!singlePlayer) {
					changeTurn();
				}
			} else {
				if (!singlePlayer) {
					endShipPlacement();
				}
			}
		}
	}

	/**
	 * Just calls end ship placement, used for delaying turn change to allow for
	 * each user to see their last ship placed tempoarily before switching turns
	 */
	public void runEndShipPlacement() {
		endShipPlacement();
	}

	/**
	 * @return the current ship thats being placed
	 */
	public int getCurrentShipIndex() {
		return currentShipIndex;
	}

	/**
	 * @return whether or not the game is over
	 */
	public boolean isGameOver() {
		return isGameOver;
	}

	/**
	 * Handles fire action, checks if it was a hit or miss and adjusts ship sunk
	 * values, updates appropriate board, throws error if hit already performed
	 * there.
	 * 
	 * @param row
	 * @param col
	 * @param view
	 * @param board
	 */
	private void handleFire(int row, int col, GridSpace[][] view, GridSpace[][] board) {
		switch (view[row][col].getGridState()) {
		// prevent firing on already targeted cell
		case Miss:
			throw new IllegalArgumentException("You already fired here, try again!");
		case Hit:
			throw new IllegalArgumentException("You already fired here, try again!");
		default:
		}
		// Check if its a hit or miss
		switch (board[row][col].getGridState()) {
		// Hit
		case Ship:
			view[row][col].setGridState(gridState.Hit);
			int shipId = board[row][col].getShipId();
			Ship ship = ships.get(shipId);
			// update ship state and track sunk count
			if (ship != null) {
				ship.hitCount++;
				if (ship.isSunk()) {
					if (currentPlayer == 1) {
						player1ShipsSunk++;
					} else {
						player2ShipsSunk++;
					}
					// mark ship as sunk on the visible view
					for (int i = 0; i < 10; i++) {
						for (int j = 0; j < 10; j++) {
							if (board[i][j].getShipId() == shipId) {
								view[i][j].setGridState(gridState.Sunk);
							}
						}
					}
					checkGameOver();
					if (isGameOver) {
						return;
					}
					pcs.firePropertyChange("hit", null, currentPlayer);
					pcs.firePropertyChange("shipSunk", null, currentPlayer);
					break;
				}
			}
			pcs.firePropertyChange("hit", null, currentPlayer);
			break;
		// Miss
		case Empty:
			view[row][col].setGridState(gridState.Miss);
			checkGameOver();
			if (isGameOver) {
				return;
			}
			pcs.firePropertyChange("miss", null, currentPlayer);
			break;
		default:
			break;
		}
		checkGameOver();
		if (isGameOver) {
			return;
		}
		pcs.firePropertyChange("fire", null, currentPlayer);
	}

	/**
	 * Just calls change turn, used for delaying turn change to allow for each user
	 * to see their miss tempoarily before switching turns
	 */
	public void nextTurn() {
		changeTurn();
	}

	/**
	 * Checks if the selected ship placement is valid, if not throws an error
	 * 
	 * @param row
	 * @param col
	 * @param direction
	 * @param board
	 * @return
	 */
	public boolean tryPlaceShip(int row, int col, int direction, GridSpace[][] board) {

		int length = SHIP_SIZES[currentShipIndex];
		int shipId = nextShipId++;

		switch (currentDirection) {
		case 1:// validate vertical ship placement
			if (row - length + 1 < 0) {
				throw new IllegalArgumentException("Ship Placed Outside Board, Try Again!");
			}
			for (int i = 0; i < length; i++) {
				if (board[row - i][col].getGridState() == gridState.Ship)
					throw new IllegalArgumentException("Already a Ship Here, Try Again!");
			}
			for (int i = 0; i < length; i++) {
				board[row - i][col].setGridState(gridState.Ship);
				board[row - i][col].setShipId(shipId);
			}
			break;
		case 2: // validate horizontal ship placement
			if (col + length > 10)
				throw new IllegalArgumentException("Ship Placed Outside Board, Try Again!");
			for (int i = 0; i < length; i++) {
				if (board[row][col + i].getGridState() == gridState.Ship)
					throw new IllegalArgumentException("Already a Ship Here, Try Again!");
			}
			for (int i = 0; i < length; i++) {
				board[row][col + i].setGridState(gridState.Ship);
				board[row][col + i].setShipId(shipId);
			}
			break;
		}
		// add this ship to the map
		ships.put(shipId, new Ship(shipId, length, currentPlayer));
		return true;
	}

	/**
	 * @return if we are in ship placement mode
	 */
	public boolean areShipsPlacing() {
		return placingShips;
	}

	/**
	 * @return current player
	 */
	public int getCurrentPlayer() {
		return currentPlayer;
	}

	/**
	 * @return current ship name
	 */
	public String getCurrentShipName() {
		return SHIP_NAMES[currentShipIndex];
	}

	/**
	 * @return if the game mode is single player
	 */
	public boolean isSinglePlayer() {
		return singlePlayer;
	}

	/**
	 * Sets mode to singleplayer
	 */
	public void singlePlayer() {
		singlePlayer = true;
	}

	/**
	 * Sets mode to multiplayer
	 */
	public void multiPlayer() {
		singlePlayer = false;
	}

	/**
	 * changes the turn
	 */
	private void changeTurn() {
		currentPlayer = (currentPlayer == 1) ? 2 : 1;
		pcs.firePropertyChange("turnChange", null, currentPlayer);
	}

	/**
	 * end ship placement after player 2 finishes placing ship
	 */
	private void endShipPlacement() {
		placingShips = false;
		changeTurn();
		pcs.firePropertyChange("endPlacement", null, currentPlayer);
	}

	/**
	 * @return current ship length
	 */
	public int getCurrentShipLength() {
		if (currentShipIndex >= SHIP_SIZES.length) {
			return 0;
		}
		return SHIP_SIZES[currentShipIndex];
	}

	/**
	 * rotate current ship direction for placing ships
	 */
	public void rotateShipDirection() {
		currentDirection = (currentDirection % 2) + 1;
		pcs.firePropertyChange("rotate", null, currentPlayer);
	}

	/**
	 * @return current ship direction for placing ships
	 */
	public int getShipDirection() {
		return currentDirection;
	}

	/**
	 * @param aiPlayerIndex
	 * @return a read only view of the opponent of the AI's board for firing
	 */
	public gridState[][] getOpponentViewForAI(int aiPlayerIndex) {
		GridSpace[][] view = (aiPlayerIndex == 1) ? player1View : player2View;
		gridState[][] simplifiedView = new gridState[10][10];

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				simplifiedView[i][j] = view[i][j].getGridState();
			}
		}

		return simplifiedView;
	}

	/**
	 * @param playerIndex
	 * @return a read only view of the ai's own board for placing ships
	 */
	public gridState[][] getPlayerBoardState(int playerIndex) {
		GridSpace[][] board = (playerIndex == 1) ? player1Board : player2Board;
		gridState[][] simple = new gridState[10][10];
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				simple[i][j] = board[i][j].getGridState();
			}
		}
		return simple;
	}

	/**
	 * Counts number of sunk spaces for each player to check for a win (on 17)
	 */
	private void checkGameOver() {
		int player1hits = 0;
		int player2hits = 0;
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				if (player1View[i][j].getGridState() == gridState.Sunk) {
					player1hits++;
				}
				if (player2View[i][j].getGridState() == gridState.Sunk) {
					player2hits++;
				}
			}

		}
		if (player1hits >= 17 || player2hits >= 17) {
			isGameOver = true;
			pcs.firePropertyChange("won", null, currentPlayer);
		} else {
			return;
		}
	}

	/**
	 * @return player 1 unsunk ship lengths
	 */
	public List<Integer> getRemainingPlayer1ShipLengths() {
		List<Integer> lengths = new ArrayList<>();
		for (Ship ship : ships.values()) {
			// only include ships that aren't sunk and belong to player 1
			if (ship.playerOwner == 1 && !ship.isSunk()) {
				lengths.add(ship.length);
			}
		}
		return lengths;
	}

	/**
	 * returns number of ships sunk for each player
	 */
	public int[] getSunkCount() {
		return new int[] { player1ShipsSunk, player2ShipsSunk };
	}

	/**
	 * The gridspace class that fills each cell of each board for storing more
	 * complex information in each cell like grid state
	 */
	private class GridSpace {

		// ------------------------Attributes-----------------------
		// holds the current state
		private gridState curState;
		private int shipId = 0;

		// --------------------------Setters--------------------------

		/**
		 * @param newGridState The new state of the gridSpace, must be non-null
		 */
		public void setGridState(gridState newGridState) throws IllegalArgumentException {
			if (newGridState == null) {
				throw new IllegalArgumentException("Grid State can not be null");
			}
			curState = newGridState;
		}// setGridState

		/**
		 * @param id
		 */
		public void setShipId(int id) {
			this.shipId = id;
		} // setShipID

		// --------------------------Getters--------------------------
		/**
		 * @return the current Grid State
		 */
		public gridState getGridState() {
			return curState;
		}// getGridState

		/**
		 * @return ship Id
		 */
		public int getShipId() {
			return shipId;
		} // getShipID

		// --------------------------Functions-------------------------
		/**
		 * Creates a gridSpace object, sets it to empty by default
		 */
		public GridSpace() {
			curState = gridState.Empty;
		}
	}// GridSpace

	/**
	 * 
	 */
	private class Ship {
		int id;
		int length;
		int hitCount = 0;
		int playerOwner;

		Ship(int id, int length, int playerOwner) {
			this.id = id;
			this.length = length;
			this.playerOwner = playerOwner;
		}

		boolean isSunk() {
			return hitCount >= length;
		}
	}

	/**
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}// addPropertyChangeListener

	/**
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}// removePropertyChangeListener
}

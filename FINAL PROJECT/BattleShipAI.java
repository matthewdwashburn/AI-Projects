package battleShip;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import battleShip.BattleShipModel.gridState;

/**
 * This class defines the logic for the ai opponent in battleship. It uses
 * expected value targeting and randomly places ships avoiding adjacent
 * placements to simulate an intelligent human play style.
 * 
 * @author Matthew Washburn, Peyton Baker
 * @version Spring 2025
 *
 */
public class BattleShipAI {
	// Random object to randomize ai moves and reduce predictability
	private Random random = new Random();


	/**
	 * picks an optimal valid firing coordinate based on the view of opponent’s
	 * board, prioritizes cells next to hits first, then uses expected value heat
	 * map to determine where to fire.
	 * 
	 * @param opponentView gridState[][], remaining ship lengths
	 * @return [row, col] coordinates to fire at
	 */
	public int[] chooseMove(gridState[][] opponentView, List<Integer> remainingShipLengths) {
		ArrayList<int[]> priority = new ArrayList<>();
		int[][] heatMap = new int[10][10];

		for (int i = 0; i < opponentView.length; i++) {
			for (int j = 0; j < opponentView[i].length; j++) {
				if (opponentView[i][j] == BattleShipModel.gridState.Hit) {
					// check horizontal hit streak
					if ((j + 1 < 10 && opponentView[i][j + 1] == gridState.Hit)
							|| (j - 1 >= 0 && opponentView[i][j - 1] == gridState.Hit)) {
						int left = j - 1;
						int right = j + 1;
						// If it's in the middle of a hit streak, move to the right and left ends
						while (left >= 0 && opponentView[i][left] == gridState.Hit)
							left--;
						while (right < 10 && opponentView[i][right] == gridState.Hit)
							right++;
						// Once it finds the end add to priority hit list
						if (left >= 0 && opponentView[i][left] == gridState.Empty)
							priority.add(new int[] { i, left });
							System.out.println("AI: Target Mode - Horizontal Hit Streak Detected - Adding " + i + "," + left + " to Priority Targets");
						if (right < 10 && opponentView[i][right] == gridState.Empty)
							priority.add(new int[] { i, right });
							System.out.println("AI: Target Mode - Horizontal Hit Streak Detected - Adding " + i + "," + right + " to Priority Targets");

						// If you get to the end of horizontal streak and there's a miss on both ends, ships are
						// stacked
						if ((left < 0 || opponentView[i][left] != gridState.Empty)
								&& (right >= 10 || opponentView[i][right] != gridState.Empty)) {
							for (int col = left + 1; col < right; col++) {
								if (i - 1 >= 0 && opponentView[i - 1][col] == gridState.Empty)
									priority.add(new int[] { i - 1, col });
								System.out.println("AI: Target Mode - Vertical Ship Stack Detected - Backtracking, Adding " + (i - 1) + "," + col + " to Priority Targets");
								if (i + 1 < 10 && opponentView[i + 1][col] == gridState.Empty)
									priority.add(new int[] { i + 1, col });
								System.out.println("AI: Target Mode - Vertical Ship Stack Detected - Backtracking, Adding " + (i + 1) + "," + col + " to Priority Targets");
							}
						}

					} else if ((i + 1 < 10 && opponentView[i + 1][j] == gridState.Hit)
							|| (i - 1 >= 0 && opponentView[i - 1][j] == gridState.Hit)) {
						// check vertical hit streak
						int up = i - 1;
						int down = i + 1;
						// If it's in the middle of a hit streak, move to the top and bottom ends
						while (up >= 0 && opponentView[up][j] == gridState.Hit)
							up--;
						while (down < 10 && opponentView[down][j] == gridState.Hit)
							down++;
						// Once it finds the end add to priority hit list
						if (up >= 0 && opponentView[up][j] == gridState.Empty)
							priority.add(new int[] { up, j });
							System.out.println("AI: Target Mode - Vertical Hit Streak Detected - Adding " + up + "," + j + " to Priority Targets");

							
						if (down < 10 && opponentView[down][j] == gridState.Empty)
							priority.add(new int[] { down, j });
							System.out.println("AI: Target Mode - Vertical Hit Streak Detected - Adding " + down + "," + j + " to Priority Targets");

						// If you get to the end of vertical streak and there's a miss on both ends, ships are
						// stacked
						if ((up < 0 || opponentView[up][j] != gridState.Empty)
								&& (down >= 10 || opponentView[down][j] != gridState.Empty)) {
							for (int row = up + 1; row < down; row++) {
								if (j - 1 >= 0 && opponentView[row][j - 1] == gridState.Empty)
									System.out.println("AI: Target Mode - Horizontal Ship Stack Detected - Backtracking, Adding " + row + "," + (j-1) + " to Priority Targets");
									priority.add(new int[] { row, j - 1 });
								if (j + 1 < 10 && opponentView[row][j + 1] == gridState.Empty)
									System.out.println("AI: Target Mode - Horizontal Ship Stack Detected - Backtracking, Adding " + row + "," + j+1 + " to Priority Targets");
									priority.add(new int[] { row, j + 1 });
							}
						}
					}
					// isolated hit with no direction yet, check all 4 adjacent cells
					else {
						if (i + 1 < 10 && opponentView[i + 1][j] == BattleShipModel.gridState.Empty) {
							priority.add(new int[] { i + 1, j });
							System.out.println("AI: Target Mode - Isolated Hit Detected - Adding " + i+1 + "," + j + " to Priority Targets");
						}
						if (j + 1 < 10 && opponentView[i][j + 1] == BattleShipModel.gridState.Empty) {
							priority.add(new int[] { i, j + 1 });
							System.out.println("AI: Target Mode - Isolated Hit Detected - Adding " + i + "," + j+1 + " to Priority Targets");
						}
						if (i - 1 > -1 && opponentView[i - 1][j] == BattleShipModel.gridState.Empty) {
							priority.add(new int[] { i - 1, j });
							System.out.println("AI: Target Mode - Isolated Hit Detected - Adding " + (i-1) + "," + j + " to Priority Targets");
						}
						if (j - 1 > -1 && opponentView[i][j - 1] == BattleShipModel.gridState.Empty) {
							priority.add(new int[] { i, j - 1 });
							System.out.println("AI: Target Mode - Isolated Hit Detected - Adding " + i + "," + (j-1) + " to Priority Targets");
						}
					}
				}
			}
		}
		// If there's open spaces next to hit cells, fire at those first
		if (!priority.isEmpty()) {
			return priority.get(random.nextInt(priority.size()));
		}

		// If there's no hit visible, use expected value heat map
		for (int shipLength : remainingShipLengths) {
			// horizontal placements
			for (int row = 0; row < 10; row++) {
				for (int col = 0; col <= 10 - shipLength; col++) {
					if (canPlaceShipAt(opponentView, row, col, 2, shipLength)) {
						for (int i = 0; i < shipLength; i++) {
							heatMap[row][col + i]++;
						}
					}
				}
			}
			// vertical placements
			for (int row = shipLength - 1; row < 10; row++) {
				for (int col = 0; col < 10; col++) {
					if (canPlaceShipAt(opponentView, row, col, 1, shipLength)) {
						for (int i = 0; i < shipLength; i++) {
							heatMap[row - i][col]++;
						}
					}
				}
			}
		}
		// pick the coordinate with highest score
		int maxScore = -1;
		List<int[]> bestMoves = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				if (opponentView[i][j] == gridState.Empty) {
					if (heatMap[i][j] > maxScore) {
						bestMoves.clear();
						bestMoves.add(new int[] { i, j });
						System.out.println("AI: Hunt Mode - New Highest Ship Probability Target Found, Clearing ALl Hunt Targets - Adding " + i + ","  + j + " to Hunt Targets");
						maxScore = heatMap[i][j];
					} else if (heatMap[i][j] == maxScore) {
						bestMoves.add(new int[] { i, j });
						System.out.println("AI: Hunt Mode - Highest Ship Probability Target Match Found - Adding " + i + ","  + j + " to Hunt Targets");

					}
				}
			}
		}
		if (bestMoves.isEmpty())
			return null;
		return bestMoves.get(random.nextInt(bestMoves.size()));
	}

	/**
	 * picks a valid random placement for a ship, making sure the ship placement
	 * does not already have a ship or have ships directly adjacent to it
	 */
	/**
	 * @param board
	 * @param shipLength
	 * @return coordinates of ship placement
	 */
	public int[] chooseShipPlacement(BattleShipModel.gridState[][] board, int shipLength) {
		Random random = new Random();
		int row, col, direction;
		boolean valid;

		do {
			direction = random.nextBoolean() ? 1 : 2; // 1 = vertical, 2 = horizontal

			if (direction == 1) { // vertical
				row = random.nextInt(10 - shipLength + 1) + shipLength - 1;
				col = random.nextInt(10);
			} else { // horizontal
				row = random.nextInt(10);
				col = random.nextInt(10 - shipLength + 1);
			}

			valid = canPlaceShipWithoutNeighbors(board, row, col, direction, shipLength);
		} while (!valid);
		return new int[] { row, col, direction };
	}

	/**
	 * determines if a ship placement has direct neighbors
	 * 
	 * @param board
	 * @param startRow
	 * @param startCol
	 * @param direction
	 * @param shipLength
	 * @return if ship placement is already taken or has neighbors
	 */
	private boolean canPlaceShipWithoutNeighbors(gridState[][] board, int row, int col, int direction, int shipLength) {
		// Vertical Ship Placement
		if (direction == 1) {
			if (row - shipLength + 1 < 0)
				return false;
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					// Check 8 surrounding cells of each ship cell
					for (int i = 0; i < shipLength; i++) {
						if (row - i + j < 0 || col + k > 9 || row - i + j > 9 || col + k < 0) {
							if (board[row - i][col] == gridState.Ship)
								return false;
						} else if (board[row - i + j][col + k] == gridState.Ship)
							return false;

					}
				}
			}
		} else {
			// Horizontal Ship Placement
			if (col + shipLength > 10)
				return false;
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					// Check 8 surrounding cells of each ship cell
					for (int i = 0; i < shipLength; i++) {
						if (row + j > 9 || row + j < 0 || col + i + k > 9 || col + i + k < 0) {
							if (board[row][col + i] == gridState.Ship)
								return false;
						} else if (board[row + j][col + i + k] == gridState.Ship)
							return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * determines if ship placement is valid, to determine how many valid ship
	 * placements each cell has to create heat map
	 * 
	 * @param board
	 * @param row
	 * @param col
	 * @param direction
	 * @param shipLength
	 * @return If a ship can be placed here
	 */
	private boolean canPlaceShipAt(gridState[][] board, int row, int col, int direction, int shipLength) {
		if (direction == 1) {
			if (row - shipLength + 1 < 0)
				return false;
			for (int i = 0; i < shipLength; i++) {
				gridState state = board[row - i][col];
				if (state == gridState.Hit || state == gridState.Miss || state == gridState.Sunk)
					return false;
			}
		} else {
			if (col + shipLength > 10)
				return false;
			for (int i = 0; i < shipLength; i++) {
				gridState state = board[row][col + i];
				if (state == gridState.Hit || state == gridState.Miss || state == gridState.Sunk)
					return false;
			}
		}
		return true;
	}

}

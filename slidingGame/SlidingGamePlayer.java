package slidinggame;
import java.util.*;

public class SlidingGamePlayer {
    private SlidingGameState state;

    public SlidingGamePlayer(SlidingGameState newState) {
        state = newState;
    }

    public String planGame() throws Exception {
        if (state.isWin()) { // Already in a winning state
            return "This is already a winning state\n" + state.getSequence();
        }

        Queue<SlidingGameState> queue = new LinkedList<>(); // Explore game states
        Set<String> visitedStates = new HashSet<>(); // Track states by their string value

        queue.add(state); // Add state to queue
        visitedStates.add(state.toString()); 

        int[][] moves = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Up, down, left, right

        while (!queue.isEmpty()) {
            SlidingGameState current = queue.poll();
            // Check if current state is a win
            if (current.isWin()) {
                return "Solved in:\n" + current.getSequence();
            }
            // Otherwise generate all possible moves
            for (int[] move : moves) {
                int moveRow = move[0];
                int moveCol = move[1];
                // Check if move is valid
                if (current.isValid(moveRow, moveCol)) {
                	// If it is copy current state to new state
                    SlidingGameState newState = new SlidingGameState(current);
                    // Apply the move on the new state
                    newState.moveToken(moveRow, moveCol);

                    // track visited states and prevent loops
                    if (!visitedStates.contains(newState.toString())) {
                        queue.add(newState);
                        visitedStates.add(newState.toString());
                    }
                }
            }
        }

        return "There is no solution";
    }
}

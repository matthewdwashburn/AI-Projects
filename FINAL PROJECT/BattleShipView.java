package battleShip;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javafx.util.Duration;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * This class is the main view and controller for the battleship game. It
 * handles rendering the gui, responding to user interaction, and coordinating
 * with the model and ai.
 * 
 * @author Matthew Washburn, Peyton Baker
 * @version Spring 2025
 *
 */
public class BattleShipView extends Application implements PropertyChangeListener {
	// Create instance of model class
	private BattleShipModel myModel = new BattleShipModel();;
	// Create an instance of ai class
	private BattleShipAI ai = new BattleShipAI();
	// Create GUI components
	private Pane root;

	private GridPane grid;

	private GridPane yourGrid;

	private Button[][] buttonArray;

	private Button[][] yourButtonArray;

	private Label turnLabel;

	private Label gameLabel;

	private Label notisLabel;

	private Label yourBoardLabel;

	private Label opponentBoardLabel;

	private Label notisTitle;

	private Label winLabel;

	private Label rotationLabel;

	private Label currentDirectionLabel;

	private Label player1HitsLabel;

	private Label player2HitsLabel;

	private Label aiPlacing;

	private Button playAgainBtn;

	// Main method
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Controller class, handles each property change from the model
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case "placed":
			setGrid();
			if (myModel.getCurrentShipIndex() == 5) {
				gameLabel.setText("All Ships Placed!");
			} else {
				gameLabel.setText("Place Your " + myModel.getCurrentShipName());
			}
			if (myModel.isSinglePlayer() && myModel.getCurrentShipIndex() == 5) {
				playerButtonEnabler(false);
				PauseTransition pause = new PauseTransition(Duration.seconds(.5));
				pause.setOnFinished(event -> {
					if (myModel.getCurrentPlayer() == 1) {
						myModel.nextTurn();
					} else {
						myModel.runEndShipPlacement();
					}
				});
				pause.play();
			}
			break;
		case "fire":
			player1HitsLabel.setText("Player 1 Ships Sunk: " + myModel.getSunkCount()[0] + "/5");
			player2HitsLabel.setText("Player 2 Ships Sunk: " + myModel.getSunkCount()[1] + "/5");
			break;
		case "endPlacement":
			setGrid();
			setYourGrid();
			gameLabel.setText("Choose Your Target!");
			gameLabel.setLayoutX(244.5);
			notisTitle.setText("Target Result:");
			notisLabel.setText("No Shots Made");
			yourBoardLabel.setText("Your Board:");
			opponentBoardLabel.setLayoutX(251);
			opponentBoardLabel.setText("Opponents Board:");
			rotationLabel.setText("");
			currentDirectionLabel.setText("");
			player1HitsLabel.setText("Player 1 Ships Sunk: " + myModel.getSunkCount()[0] + "/5");
			player2HitsLabel.setText("Player 2 Ships Sunk: " + myModel.getSunkCount()[1] + "/5");
			root.getChildren().remove(aiPlacing);
			break;
		case "hit":
			setGrid();
			notisLabel.setText("Player " + myModel.getCurrentPlayer() + " Hit!");
			// if AI just hit, let it shoot again
			if (myModel.getCurrentPlayer() == 2 && !myModel.areShipsPlacing() && myModel.isSinglePlayer()) {
				BattleShipModel.gridState[][] view = myModel.getOpponentViewForAI(2);
				int[] move = ai.chooseMove(view, myModel.getRemainingPlayer1ShipLengths());

				if (move != null) {
					new Thread(() -> {
						try {
							Thread.sleep(500); // short delay to simulate thinking
							javafx.application.Platform.runLater(() -> {
								try {
									myModel.cellClicked(move[0], move[1]);
								} catch (IllegalArgumentException ex) {
									Alert alert = new Alert(Alert.AlertType.ERROR);
									alert.setTitle("AI Error");
									alert.setContentText(ex.getMessage());
									alert.show();
								}
							});
						} catch (InterruptedException ed) {
							ed.printStackTrace();
						}
					}).start();
				}
			}
			break;
		case "miss":
			notisLabel.setText("Player " + myModel.getCurrentPlayer() + " Missed!");
			setGrid();
			if (myModel.isSinglePlayer()) {
				playerButtonEnabler(false);
				PauseTransition pause = new PauseTransition(Duration.seconds(.5));
				pause.setOnFinished(event -> {
					myModel.nextTurn();
				});
				pause.play();
			}
			if (!myModel.isSinglePlayer()) {
				myModel.nextTurn();
			}
			break;
		case "turnChange":
			turnLabel.setText("Player " + myModel.getCurrentPlayer());
			if (!myModel.isSinglePlayer()) {
				Alert turnalert = new Alert(Alert.AlertType.INFORMATION);
				turnalert.setTitle("Turn Change!");
				turnalert.setContentText("Pass The Game To Player " + myModel.getCurrentPlayer() + "!");
				turnalert.showAndWait();
			}
			setGrid();
			if (!myModel.areShipsPlacing()) {
				setYourGrid();
			}
			// let the ai auto-place ships when it's its turn to place
			if (myModel.getCurrentPlayer() == 2 && myModel.areShipsPlacing() && myModel.isSinglePlayer()) {
				new Thread(() -> {
					try {
						for (int i = 0; i < 5; i++) {
							if (myModel.getCurrentShipLength() == 0)
								break;
							boolean placed = false;

							while (!placed) {
								int shipLength = myModel.getCurrentShipLength();
								BattleShipModel.gridState[][] aiBoard = myModel.getPlayerBoardState(2);
								int[] move = ai.chooseShipPlacement(aiBoard, shipLength);

								if (move[2] == 2) {
									javafx.application.Platform.runLater(() -> myModel.rotateShipDirection());
									Thread.sleep(100);
								}

								int finalRow = move[0];
								int finalCol = move[1];

								// try to place the ship
								javafx.application.Platform.runLater(() -> {
									try {
										myModel.cellClicked(finalRow, finalCol);
									} catch (IllegalArgumentException ex) {
										// placement failed, retry
									}
								});

								Thread.sleep(300);

								// check if ship was placed (if index advanced, it's successful)
								if (myModel.getCurrentShipLength() != shipLength) {
									placed = true;
								}
							}
						}

					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}).start();
			}
			// call ai here if it's player 2's turn to fire and placing ships phase is over
			if (myModel.getCurrentPlayer() == 2 && !myModel.areShipsPlacing() && myModel.isSinglePlayer()) {
				BattleShipModel.gridState[][] opponentView = myModel.getOpponentViewForAI(2);
				int[] move = ai.chooseMove(opponentView, myModel.getRemainingPlayer1ShipLengths());

				if (move != null) {
					// small delay to simulate thinking
					new Thread(() -> {
						try {
							Thread.sleep(1000); // 1 second delay
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						// run on javafx thread
						javafx.application.Platform.runLater(() -> {
							try {
								myModel.cellClicked(move[0], move[1]);
							} catch (IllegalArgumentException ex) {
								Alert alert = new Alert(Alert.AlertType.ERROR);
								alert.setTitle("AI Error");
								alert.setContentText(ex.getMessage());
								alert.show();
							}

						});
					}).start();
				}
			}
			break;
		case "shipSunk":
			notisLabel.setText("Ship Sunk!");
			setGrid();
			break;
		case "won":
			player1HitsLabel.setText("Player 1 Ships Sunk: " + myModel.getSunkCount()[0] + "/5");
			player2HitsLabel.setText("Player 2 Ships Sunk: " + myModel.getSunkCount()[1] + "/5");
			setGrid();
			winLabel.setText("Player " + myModel.getCurrentPlayer() + " Wins!");
			turnLabel.setText("");
			gameLabel.setText("");
			playAgainBtn.setVisible(true);
			// disables all the buttons
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 10; j++) {
					buttonArray[i][j].setDisable(true);
				}
			}
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Winner!");
			alert.setContentText("Player " + myModel.getCurrentPlayer() + " Wins!");
			alert.show();
			break;
		}
	}

	/**
	 * enables or disables all player buttons (firing grid)
	 * 
	 * @param enabled
	 */
	private void playerButtonEnabler(boolean enabled) {
		if (buttonArray == null)
			return;

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				buttonArray[i][j].setDisable(!enabled);
			}
		}
	}

	/**
	 * hides all player buttons on the main grid for when the AI is placing ships
	 * 
	 * @param visible
	 */
	private void playerButtonHider(boolean visible) {
		if (buttonArray == null)
			return;

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				buttonArray[i][j].setVisible(!visible);
			}
		}
	}

	/**
	 * Sets up the player selection GUI window
	 */
	@Override
	public void start(Stage primaryStage) {
		Stage modeStage = new Stage();
		modeStage.setTitle("Choose Game Mode");

		Button singlePlayerBtn = new Button("Single Player");
		Button multiPlayerBtn = new Button("Multiplayer");

		// load the image
		Image titleImage = new Image("title.png");
		ImageView imageView = new ImageView(titleImage);
		imageView.setFitWidth(125);
		imageView.setPreserveRatio(true);

		singlePlayerBtn.setPrefWidth(150);
		multiPlayerBtn.setPrefWidth(150);

		singlePlayerBtn.setOnAction(e -> {
			modeStage.close();
			try {
				launchGame(primaryStage);
				myModel.singlePlayer();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		multiPlayerBtn.setOnAction(e -> {
			modeStage.close();
			try {
				launchGame(primaryStage);
				myModel.multiPlayer();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		VBox modeLayout = new VBox(15, imageView, singlePlayerBtn, multiPlayerBtn);
		modeLayout.setAlignment(Pos.CENTER);
		Scene modeScene = new Scene(modeLayout, 300, 150);

		modeStage.setScene(modeScene);
		modeStage.show();
	}

	/**
	 * Creates the main game GUI window
	 * 
	 * @param primaryStage
	 * @throws Exception
	 */
	public void launchGame(Stage primaryStage) throws Exception {

		myModel.addPropertyChangeListener(this);

		try {
			// Create new border pane
			root = new Pane();

			// Set turn label
			turnLabel = new Label("Player " + myModel.getCurrentPlayer() + ":");
			turnLabel.setLayoutX(281);
			turnLabel.setLayoutY(585);
			root.getChildren().add(turnLabel);

			// Set game label
			gameLabel = new Label("Place Your " + myModel.getCurrentShipName());
			gameLabel.setLayoutX(220);
			gameLabel.setLayoutY(605);
			root.getChildren().add(gameLabel);

			// Set your board label
			yourBoardLabel = new Label("");
			yourBoardLabel.setLayoutX(452);
			yourBoardLabel.setLayoutY(585);
			root.getChildren().add(yourBoardLabel);

			// Set opponents board label
			opponentBoardLabel = new Label("Your Board:");
			opponentBoardLabel.setLayoutX(269);
			opponentBoardLabel.setLayoutY(85);
			root.getChildren().add(opponentBoardLabel);

			// Add Ai Placing Label
			aiPlacing = new Label("");
			aiPlacing.setLayoutX(230);
			aiPlacing.setLayoutY(325);
			root.getChildren().add(aiPlacing);

			// Set rotation label
			rotationLabel = new Label("Press \"R\" to Rotate Your Ship");
			rotationLabel.setLayoutX(220);
			rotationLabel.setLayoutY(625);
			root.getChildren().add(rotationLabel);

			// Set current rotation label
			currentDirectionLabel = new Label("Ship Direction: Vertical");
			currentDirectionLabel.setLayoutX(225);
			currentDirectionLabel.setLayoutY(645);
			root.getChildren().add(currentDirectionLabel);

			// Set notification title
			notisTitle = new Label("");
			notisTitle.setLayoutX(78);
			notisTitle.setLayoutY(585);
			root.getChildren().add(notisTitle);

			// Set notification label
			notisLabel = new Label("");
			notisLabel.setLayoutX(78);
			notisLabel.setLayoutY(605);
			root.getChildren().add(notisLabel);

			// Set player 1 hits label
			player1HitsLabel = new Label("");
			player1HitsLabel.setLayoutX(78);
			player1HitsLabel.setLayoutY(625);
			root.getChildren().add(player1HitsLabel);

			// Set player 2 hits label
			player2HitsLabel = new Label("");
			player2HitsLabel.setLayoutX(78);
			player2HitsLabel.setLayoutY(645);
			root.getChildren().add(player2HitsLabel);

			// Set win label
			winLabel = new Label("");
			winLabel.setLayoutX(262.6);
			winLabel.setLayoutY(585);
			root.getChildren().add(winLabel);

			// Button that allows to play again
			playAgainBtn = new Button("Play Again");
			playAgainBtn.setLayoutX(264);
			playAgainBtn.setLayoutY(650);
			playAgainBtn.setVisible(false); // hide initially
			root.getChildren().add(playAgainBtn);

			playAgainBtn.setOnAction(e -> {
				javafx.application.Platform.runLater(() -> {
					try {
						new BattleShipView().start(new Stage());
						((Stage) root.getScene().getWindow()).close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				});
			});

			// Place the grid
			setGrid();
			// Create title image
			Image titleImage = new Image("title.png");
			ImageView imageView = new ImageView(titleImage);

			// Set image size
			imageView.setFitWidth(200);
			imageView.setPreserveRatio(true);

			// Center the image
			imageView.setLayoutX(200); // X position
			imageView.setLayoutY(15); // Y position
			root.getChildren().add(imageView);

			// Create scene
			Scene scene = new Scene(root, 600, 800);
			primaryStage.setScene(scene);
			primaryStage.show();
			scene.setOnKeyPressed(event -> {
				switch (event.getCode()) {
				// Notify the model to rotate
				case R:
					myModel.rotateShipDirection();
					if (myModel.getShipDirection() == 1) {
						currentDirectionLabel.setText("Ship Direction: Vertical");
					}
					if (myModel.getShipDirection() == 2) {
						currentDirectionLabel.setText("Ship Direction: Horizontal");
					}
					break;
				default:
					break;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets up the main grid depending on the player and phase of the game
	 */
	private void setGrid() {
		// Remove previous grid
		if (grid != null) {
			root.getChildren().remove(grid); // only remove the old grid
		}

		// Create the new grid pane and button array
		grid = new GridPane();
		grid.setHgap(0); // Remove any gaps between cells
		grid.setVgap(0);

		// This prevents the grid from expanding beyond its preferred size
		grid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

		// Add column headers (A-J)
		for (int col = 0; col < 10; col++) {
			Label colLabel = new Label("     " + Character.toString((char) ('A' + col)));
			colLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
			grid.add(colLabel, col + 1, 0); // Add to first row
		}

		// Add row headers (1-10)
		for (int row = 0; row < 10; row++) {
			Label rowLabel = new Label(Integer.toString(row + 1));
			rowLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
			grid.add(rowLabel, 0, row + 1); // Add to first column
		}

		buttonArray = new Button[10][10];

		// Create button grid
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				int tmpI = i;
				int tmpJ = j;
				Integer[] coords = { tmpI, tmpJ };
				Button tempButton = new Button();
				if (myModel.getCoordState(coords) == BattleShipModel.gridState.Hit) {
					tempButton.setStyle(
							"-fx-background-color: red;" + " -fx-border-color: black; -fx-border-width: 1px;");
				} else if (myModel.getCoordState(coords) == BattleShipModel.gridState.Miss) {
					tempButton.setStyle(
							"-fx-background-color: black;" + " -fx-border-color: black; -fx-border-width: 1px;");
				} else if (myModel.getCoordState(coords) == BattleShipModel.gridState.Empty) {
					tempButton.setStyle(
							"-fx-background-color: gray;" + " -fx-border-color: black; -fx-border-width: 1px;");
				} else if (myModel.getCoordState(coords) == BattleShipModel.gridState.Ship) {
					tempButton.setStyle(
							"-fx-background-color: blue;" + " -fx-border-color: black; -fx-border-width: 1px;");
				} else if (myModel.getCoordState(coords) == BattleShipModel.gridState.Sunk) {
					tempButton.setStyle(
							"-fx-background-color: #654321;" + " -fx-border-color: black; -fx-border-width: 1px;");
				}
				tempButton.setPrefWidth(45);
				tempButton.setPrefHeight(45);

				// Button clicked listener
				tempButton.setOnAction(e -> {
					try {
						myModel.cellClicked(coords[0], coords[1]);
					} catch (IllegalArgumentException ex) {
						// if the bid was Illegal set-up and
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("ERROR");
						alert.setContentText(ex.getMessage());
						alert.show();
					}

				});

				buttonArray[i][j] = tempButton;
				// Offset by one to account for headers
				grid.add(tempButton, j + 1, i + 1);
			}
		}
		// Disable All Ai buttons on their turn
		if (myModel.isSinglePlayer()) {
			playerButtonEnabler(myModel.getCurrentPlayer() == 1);
		}
		// Hide All AI buttons on their turn when they are placing ships
		if (myModel.isSinglePlayer() && myModel.areShipsPlacing() && myModel.getCurrentPlayer() == 2) {
			playerButtonHider(myModel.getCurrentPlayer() == 2);
			aiPlacing.setText("Player 2 is Placing Their Ships...");
		}
		// Add and center grid
		grid.setLayoutX(51.5);
		grid.setLayoutY(100);
		root.getChildren().add(grid);
	}

	/**
	 * Sets up the smaller grid depending on the player and phase of the game
	 */
	private void setYourGrid() {
		// Remove previous grid
		if (yourGrid != null) {
			root.getChildren().remove(yourGrid); // only remove the old grid
		}

		// Create the new grid pane and button array
		yourGrid = new GridPane();
		yourGrid.setHgap(0); // Remove any gaps between cells
		yourGrid.setVgap(0);

		// Add column headers (A-J)
		for (int col = 0; col < 10; col++) {
			Label colLabel = new Label(" " + Character.toString((char) ('A' + col)));
			colLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0px;");
			yourGrid.add(colLabel, col + 1, 0); // Add to first row
		}

		// Add row headers (1-10)
		for (int row = 0; row < 10; row++) {
			Label rowLabel = new Label(Integer.toString(row + 1));
			rowLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0px;");
			yourGrid.add(rowLabel, 0, row + 1); // Add to first column
		}

		yourButtonArray = new Button[10][10];

		// Create button grid
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				int tmpI = i;
				int tmpJ = j;
				Integer[] coords = { tmpI, tmpJ };
				Button tempButton = new Button();
				if (myModel.getYourCoordState(coords) == BattleShipModel.gridState.Hit) {
					tempButton.setStyle(
							"-fx-background-color: red;" + " -fx-border-color: black; -fx-border-width: 1px;");
				} else if (myModel.getYourCoordState(coords) == BattleShipModel.gridState.Miss) {
					tempButton.setStyle(
							"-fx-background-color: black;" + " -fx-border-color: black; -fx-border-width: 1px;");
				} else if (myModel.getYourCoordState(coords) == BattleShipModel.gridState.Empty) {
					tempButton.setStyle(
							"-fx-background-color: gray;" + " -fx-border-color: black; -fx-border-width: 1px;");
				} else if (myModel.getYourCoordState(coords) == BattleShipModel.gridState.Ship) {
					tempButton.setStyle(
							"-fx-background-color: blue;" + " -fx-border-color: black; -fx-border-width: 1px;");
				} else if (myModel.getYourCoordState(coords) == BattleShipModel.gridState.Sunk) {
					tempButton.setStyle(
							"-fx-background-color: #654321;" + " -fx-border-color: black; -fx-border-width: 1px;");
				}
				tempButton.setMinSize(15, 15);
				tempButton.setPrefSize(15, 15);
				tempButton.setMaxSize(15, 15);
				yourButtonArray[i][j] = tempButton;
				// Offset by one to account for headers
				yourGrid.add(tempButton, j + 1, i + 1);
			}
		}
		// Add and center grid
		yourGrid.setLayoutX(400);
		yourGrid.setLayoutY(600);
		root.getChildren().add(yourGrid);

		// disables all the buttons
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				yourButtonArray[i][j].setDisable(true);
			}
		}
	}

}
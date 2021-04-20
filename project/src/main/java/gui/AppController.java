package gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import coordinates.Coordinates;
import coordinates.CoordinatesCalculator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import logic.Board;
import logic.HandleHighScores;
import logic.Score;
import shape.IShape;
import shape.JShape;
import shape.LShape;
import shape.SShape;
import shape.Shape;
import shape.SquareShape;
import shape.TShape;
import shape.ZShape;

public class AppController implements Initializable {
	
	@FXML private AnchorPane userPage;
    @FXML private AnchorPane gamePage;
	@FXML private AnchorPane activePane;
	@FXML private GridPane gameGrid;
	@FXML private GridPane nextBlock;
    @FXML private Button handleStartGame;
    @FXML private TextField userNameField;
    @FXML private Label currentPlayerField;
    @FXML private Label currentScore;
	@FXML private Label firstPlace;
	@FXML private Label secondPlace;
	@FXML private Label thirdPlace;
	@FXML private Label fourthPlace;
	@FXML private Label fifthPlace;
	@FXML private Pane JLabel;
	@FXML private Pane ILabel;
	@FXML private Pane SLabel;
	@FXML private Pane SquareLabel;
	@FXML private Pane ZLabel;
	@FXML private Pane TLabel;
	@FXML private Pane LLabel;
	@FXML private Pane gameOverPane;
	
	
    
//    private Coordinates currentPos;
	private Board board;
    private Boolean gameRunning;
    private String userName;
    private int userScore;
    HandleHighScores highScoreHandler;
    private Shape shape;
    private Timeline myTimeLine;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
		userPage.setDisable(false);
		userPage.setVisible(true);
		gamePage.setDisable(true);
		gamePage.setVisible(false);
		gameOverPane.setVisible(false);
    }
    
	@FXML
	public void userNameSubmit() {
		if (userNameField.getText() == "") {
			userNameField.setPromptText("Please enter a name");
		} else {
		this.userName = userNameField.getText();
		userPage.setDisable(true);
		userPage.setVisible(false);
		gamePage.setDisable(false);
		gamePage.setVisible(true);
		currentPlayerField.setText("CURRENT PLAYER: \n" + userName);
		highScoreHandler = new HandleHighScores();
		updateScores();
		}
	}
    
	@FXML
	public void handleStartGame() {
		board = new Board(10, 20);
		System.out.println("Start game!");
		gameGrid.setStyle("-fx-background-color: rgba(80,80,80, 0.4)");
		highScoreHandler.saveHighScores("src/main/resources/highscores.json");
		currentScore.setText(""+ userScore);
		board.insertNewBlock();
		gameGrid.requestFocus();
		myTimeLine = new Timeline(new KeyFrame(Duration.seconds(0.5), ev -> {
			if (!board.gameOver()) {
			board.moveDown();
        	currentScore.setText("" + board.getScore());
        	updateGrid();
		} else {
			gameOver();
			System.out.println("Game over!");
		}
			}));
		myTimeLine.setCycleCount(Animation.INDEFINITE);
	    myTimeLine.play();
	}
	
	@FXML
	public void updateScores() {
		highScoreHandler.getHighScoresFromFile("src/main/resources/highscores.json");
		List<Score> newScores = highScoreHandler.getHighScores();
		
		firstPlace.setText(newScores.size() >= 1 ? newScores.get(0).getName()  + ": " + newScores.get(0).getScore() : "-");
		secondPlace.setText(newScores.size() >= 2 ? newScores.get(1).getName()  + ": " + newScores.get(1).getScore() : "-");
		thirdPlace.setText(newScores.size() >= 3 ? newScores.get(2).getName()  + ": " + newScores.get(2).getScore() : "-");
		fourthPlace.setText(newScores.size() >= 4 ? newScores.get(3).getName()  + ": " + newScores.get(3).getScore() : "-");
		fifthPlace.setText(newScores.size() >= 5 ? newScores.get(4).getName()  + ": " + newScores.get(4).getScore() : "-");
        }
	
		
	@FXML 
	public void handleKeyPressed(KeyEvent e) {
		if (!board.gameOver()) {
			if (e.getCode().equals(KeyCode.A)) {
				board.moveLeft();
				updateGrid();
			}
			if (e.getCode().equals(KeyCode.S)) {
				board.moveDown();
				updateGrid();
			}
			if (e.getCode().equals(KeyCode.D)) {
				board.moveRight();
				updateGrid();
			}
			if (e.getCode().equals(KeyCode.W)) {
				board.rotateShape();
				updateGrid();
			}
			if (e.getCode().equals(KeyCode.SPACE)) {
				board.hardDrop();
				updateGrid();
			}
			else {
				return;
			}
		updateGrid();
		}
	}
	
	public void updateGrid() {
		gameGrid.getChildren().clear();
		for (int y = 0; y < gameGrid.getRowCount(); y++) {
			for (int x = 0; x < gameGrid.getColumnCount(); x++) {
				String color = board.getTile(x, y);
				StackPane pane = new StackPane();
				pane.setStyle("-fx-background-color: " + (color==null ? "#FFFFFF" : color));
				GridPane.setFillHeight(pane, true);
				GridPane.setFillWidth(pane, true);
				gameGrid.add(pane, x, y);				
			}
		}
		showNext();
	}
	
	public void showNext() {
		JLabel.setVisible(false);
		ILabel.setVisible(false);
		SLabel.setVisible(false);
		SquareLabel.setVisible(false);
		TLabel.setVisible(false);
		ZLabel.setVisible(false);
		LLabel.setVisible(false);
		
		
		switch(board.getNextShape().color) {
		
			case JShape.color: JLabel.setVisible(true);  break;
			case IShape.color: ILabel.setVisible(true); break;
			case SShape.color: SLabel.setVisible(true);  break;
			case SquareShape.color: SquareLabel.setVisible(true);  break;
			case TShape.color: TLabel.setVisible(true); break;
			case ZShape.color: ZLabel.setVisible(true); break;
			case LShape.color: LLabel.setVisible(true);  break;
		}
		
	}
	
	public void gameOver() {
		myTimeLine.stop();
		gameOverPane.setStyle("-fx-background-color: rgba(80,80,80, 0.5)");
		gameOverPane.setVisible(true);
		userScore = board.getScore();
		System.out.println("User: " + userName);
		System.out.println("Score: " + userScore);
		highScoreHandler.updateScore(userName, userScore, "src/main/resources/highscores.json");
	}
	
}

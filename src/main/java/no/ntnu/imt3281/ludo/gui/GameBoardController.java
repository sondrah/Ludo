package no.ntnu.imt3281.ludo.gui;
import no.ntnu.imt3281.ludo.logic.PlayerEvent;
import no.ntnu.imt3281.ludo.logic.DiceEvent;
import no.ntnu.imt3281.ludo.logic.Ludo;

import java.awt.Event;
import java.awt.Point;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * Sample Skeleton for 'GameBoard.fxml' Controller Class
 * A FXML controller class that handles all visuals for the
 * gameboard that the user sees and interacts with
 */
public class GameBoardController extends Ludo {

    @FXML private Label player1Name;
    @FXML private ImageView player1Active;
    @FXML private Label player2Name;
    @FXML private ImageView player2Active;
    @FXML private Label player3Name;
    @FXML private ImageView player3Active;
    @FXML private Label player4Name;
    @FXML private ImageView player4Active;
    @FXML private ImageView diceThrown;
    @FXML private Button throwTheDice;
    @FXML private TextArea chatArea;
    @FXML private TextField textToSay;
    @FXML private Button sendTextButton;
    @FXML private AnchorPane boardPane;
    @FXML private ImageView board;
    
	private TopLeftCorners corners = new TopLeftCorners();
	private Image playerPieceImages[] = new Image[4];
	private Rectangle playerPieces[][]= new Rectangle[4][4];
	private Rectangle moveFrom = new Rectangle(46,46);
	private Rectangle moveTo = new Rectangle(46,46);
	private int MAX_PLAYERS = 4; 
	private int PIECES = 4;
	private int CurrentPlayer = RED; 
	private boolean shouldMove = false; 
	private int movePlayerPieceFrom = -1;
	private int diceValue = -1;
	private int gameID;
	private static int SQUARE = 48;
	private Communication connection = Communication.getConnection();
	
	/**
	 *Start the game gui for this game. 
	 *
	 */
	public void StartGameBoard(StartGame gameInfo){
		this.gameID = gameInfo.getGameCode(); 
		
		// Hente ut spillernes navn 
		for ( String playerName : gameInfo.getPlayerNames()) {
			addPlayer(playerName);
		}
		player1Name.setText(getPlayerName(RED));
		player2Name.setText(getPlayerName(BLUE));
		player3Name.setText(getPlayerName(YELLOW));
		player4Name.setText(getPlayerName(GREEN));
		
		// Finne ut hvilken spiller er hvilken farge
		if(Ludo.properties.getProperties().getProoerty("username").equals(getPlayerName(BLUE)))
			CurrentPlayer = BLUE;
		if(Ludo.properties.getProperties().getProoerty("username").equals(getPlayerName(YELLOW)))
			CurrentPlayer = BLUE;
		if(Ludo.properties.getProperties().getProoerty("username").equals(getPlayerName(GREEN)))
			CurrentPlayer = BLUE;
		if(CurrentPlayer == activePlayer())			// Sjekker med Ludo Logic
			throwTheDice.setDisable(false);
		
		addDiceListener(dEvent -> 
				diceThrown.setImage(new Image(getClass().getResourceAsStream
						("/images/dice" + dEvent.getDice() + ".png")))
				);
		addPlayerListener(pEvent -> {			// getActivePlayer mulig getPlayer
			Platform.runLater(()-> playerChange(pEvent.getActivePlayer(), pEvent.getState()));
		});
		// TODO komprimere valgt bilde fra 300x300 til 48x48
		
		// Fordi, funker også for jar fil 
		playerPieceImages[0] = new Image(getClass().getResourceAsStream("/images/red.png"));
		playerPieceImages[1] = new Image(getClass().getResourceAsStream("/images/blue.png"));
		playerPieceImages[2] = new Image(getClass().getResourceAsStream("/images/yellow.png"));
		playerPieceImages[3] = new Image(getClass().getResourceAsStream("/images/green.png"));
		
		
		for(int pl = 0; pl < MAX_PLAYERS; pl++) {
			for(int pi = 0; pi < PIECES; pi++) {
				
				playerPieces[pl][pi] = new Rectangle(SQUARE,SQUARE);
				playerPieces[pl][pi].setFill(new ImagePattern(playerPieceImages[pl]));
				
				// Forskyver litt pga at alle skal synes når brikkene er på samme plass, for eks tårn/ mål
				playerPieces[pl][pi].setX(corners.point[pl*4+pi].getX()-8+pi*4);
				playerPieces[pl][pi].setY(corners.point[pl*4+pi].getX()-2+pi*2);
				playerPieces[pl][pi].setOnMouseClicked(e->clickOnPiece(e));
				boardPane.getChildren().add(playerPieces[pl][pi]);
			}
		}
		
		// Set up tiles used for showing selected piece and target square 
		moveFrom.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/images/selected.png"))));
		moveFrom.setX(-100);
		moveFrom.setY(-100);
		boardPane.getChildren().add(moveFrom);
		moveTo.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/images/selected.png"))));
		moveTo.setX(-100);
		moveTo.setY(-100);
		boardPane.getChildren().add(moveTo);
		moveTo.setOnMouseClicked(e->movePiece(e));
	}



	/**
	 * When the player change, this is called from playerListener
	 * Will be called when the state change between ( PLAYING,  WAITING, LEFTGAME, WON) 
	 * @param player the player it concerns 
	 * @param state the new state
	 */
	public void playerChange(int player, int state) { 	
		
		switch(state) {
		case PlayerEvent.PLAYING: { 
			diceThrown.setImage(new Image(getClass().getResourceAsStream ("/images/rolldice.png")));
			changePlayerState(player, false);
			break;
		}
		case PlayerEvent.WAITING: {
			changePlayerState(player, true);
			break;
		}
		case PlayerEvent.LEFTGAME: {
			switch(player) {
				case RED:{
					player1Active.setImage( new Image(getClass().getResourceAsStream("/images/red.png")));
					break;
				}
				case BLUE: {
					player1Active.setImage( new Image(getClass().getResourceAsStream("/images/blue.png")));
					break;
				}
				case YELLOW: {
					player1Active.setImage( new Image(getClass().getResourceAsStream("/images/yellow.png")));
					break;
				}
				case GREEN: {
					player1Active.setImage( new Image(getClass().getResourceAsStream("/images/green.png")));
					break;
				}
			}
		}
		case PlayerEvent.WON: {
			
			break;
			}
		}
		
	}
	
	/**
	 * 
	 * @param e
	 */
	@FXML
	void throwDice(ActionEvent e) {
		connection.send(new ThrowDice(gameId));
		
	}
	
	/**
	 * 
	 */
	@Override 
	public int throwDice(int dice) {
		super.throwDice(dice);
		shouldMove = false;
		if(CurrentPlayer == activePlayer() && shouldMove()) {
			diceValue = dice;
			shouldMove = true;
			Platform.runLater(()-> {
				throwTheDice.setText("Nå flyttes brikke!!");	// TODO I18N
				throwTheDice.setDisable(true);
			});	
		}
	}
	/**
	 * 
	 */
	public void updateBoard() {
		// TODO 
	}
	/**
	 * 
	 * @param event
	 */
	@FXML
	void clickOnPiece(MouseEvent event) {
		if(shouldMove) {	// Skjer bare hvis spiller skal flytte
			movePlayerPieceFrom = movePieceFrom(event);
			if(movePlayerPieceFrom > -1) {	// Dersom bruker trykket på aktiv spiller sin brikke
				// TODO sjekke brikke eller lignende canMove(movePlayerPieceFrom)
				if(canMove()) {	// Dersom brikken kan flytte
					if(movePlayerPieceFrom ==0) {		// Dersom fra start, flytt til 1
						moveTo.setX(corners.point[userGridToLudoBoardGrid(CurrentPlayer, 1)].getX());
						moveTo.setY(corners.point[userGridToLudoBoardGrid(CurrentPlayer, 1)].getY());
					}
					else {					// TODO skulle vært til? 
						// flytter brikken til point
							// Finner riktig ved å hente ut rikitg pos (svart verdi) .get er ukjent hvorfor
						moveTo.setX(corners.point[userGridToLudoBoardGrid(CurrentPlayer, movePlayerPieceFrom)].getX());
						moveTo.setY(corners.point[userGridToLudoBoardGrid(CurrentPlayer, movePlayerPieceFrom)].getY());
					}
				}
				else {	// Error?? skal ikke skje?? burde vært ikke gjør noe
					moveFrom.setX(-100);
					moveFrom.setY(-100);
				}
			}
			
		}
	}
	/**
	 * 
	 * @param player
	 * @param b
	 */
	private void changePlayerState(int player, boolean b) {		// Ka e tanken her?
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 * @param e
	 */
	private void movePiece(MouseEvent e) {			// Trenger chould move?? 
		connection.send(new MovePiece(gameId, CurrentPlayer, movePlayerPieceFrom, movePlayerPieceFrom==0 && shouldMove));
		
	}
	
	/**
	 * 
	 * @param e
	 */
	private int movePieceFrom(MouseEvent event) {
		int correctValue = -1;
		int x = (int) event.getX();
		int y = (int) event.getY(); 
		
		Object ob = event.getSource();
		for (int pi = 0; pi<4; pi++) {
			if( ob.equals(playerPieces[CurrentPlayer][pi])) {
				int blackPos = userGridToLudoBoardGrid(CurrentPlayer, getPlayerPieces(CurrentPlayer)[i]);
				int offset = 0;
				
				if(getPlayerPieces(CurrentPlayer)[pi] == 0) {
					offset = pi;
				}
				correctValue = getPlayerPieces(CurrentPlayer)[pi];
				moveFrom.setX(corners.point[blackPos+offset].getX());
				moveFrom.setY(corners.point[blackPos+offset].getY());
				// TODO ikke ferdig 
			}
		}
		
		return correctValue;
	}
	/**
	 * Holds all the different positions on the board 
	 */
	class TopLeftCorners {
		
		Point point[] = new Point[92];
		
		/**
		 * Sets all top left corners inn position 0-91
		 */
		public TopLeftCorners() {
			
			for(int i = 0; i<point.length; i ++) {		
				point[i] = new Point(); 
			}
			
			// Set RED start fields
			point[0].setLocation(554, 74);
			point[1].setLocation(554+SQUARE, 74+SQUARE);
			point[2].setLocation(554, 74+SQUARE*2);
			point[3].setLocation(554-SQUARE, 74+SQUARE);
			
			// Set Blue start fields
			point[4].setLocation(554, 506);
			point[5].setLocation(554+SQUARE, 506+SQUARE);
			point[6].setLocation(554, 506+SQUARE*2);
			point[7].setLocation(554-SQUARE, 506+SQUARE);
			
			// Set Yellow start fields
			point[8].setLocation(122, 506);
			point[9].setLocation(122+SQUARE, 506+SQUARE);
			point[10].setLocation(122, 506+SQUARE*2);
			point[11].setLocation(122-SQUARE, 506+SQUARE);
			
			// Set Green start fields
			point[12].setLocation(122, 74);
			point[13].setLocation(122+SQUARE, 74+SQUARE);
			point[14].setLocation(122, 74+SQUARE*2);
			point[15].setLocation(122-SQUARE, 74+SQUARE);
			
			
			//////// Set board 16-91 ////////////////////
			
			int xr = 0;  			// For runaway
			int yr = 0;	   			// For runaway
			
			int x = 8*SQUARE;		// starts on square 16
			int y = SQUARE;	   		
			
			for(int pos = 16; pos <= 20; pos++) {
				point[pos].setLocation(x, y);
				y += SQUARE;
			}
			
			x += SQUARE;
			for(int pos = 21; pos <= 26; pos++) {
				point[pos].setLocation(x, y);
				x += SQUARE;
			}
			
			x -= SQUARE; y += SQUARE;
			point[27].setLocation(x, y);
			
			// Blue Runaway 
			xr=x;
			yr=y;
			xr -= SQUARE;
			for(int pos = 74; pos <= 79; pos++) {
				point[pos].setLocation(xr, yr);
				xr -= SQUARE;
			}
			// Done Blue runaway
			
			y += SQUARE;
			for(int pos = 28; pos <= 33; pos++) {
				point[pos].setLocation(x, y);
				x -= SQUARE;
			}
			
			y += SQUARE;
			for(int pos = 34; pos <= 39; pos++) {
				point[pos].setLocation(x, y);
				y += SQUARE;
			}
			
			x -= SQUARE; y -= SQUARE;
			point[40].setLocation(x, y);
			
			// Yellow Runaway
			xr=x;
			yr=y;
			yr -= SQUARE;
			for(int pos = 80; pos <= 85; pos++) {
				point[pos].setLocation(xr, yr);
				yr -= SQUARE;
			}
			// Done Yellow runaway
			
			x -= SQUARE;
			for(int pos = 41; pos <= 46; pos++) {
				point[pos].setLocation(x, y);
				y -= SQUARE;
			}
			
			x -= SQUARE;
			for(int pos = 47; pos < 52; pos++) {
				point[pos].setLocation(x, y);
				x -= SQUARE;
			}
			
			point[52].setLocation(x, y);
			
			
			y -= SQUARE;
			point[53].setLocation(x, y);
			
			// Green Runaway
			xr=x;
			yr=y;
			xr += SQUARE;
			for(int pos = 86; pos <= 91; pos++) {
				point[pos].setLocation(xr, yr);
				xr += SQUARE;
			}
			// Done Green runaway
			
			y -= SQUARE;
			for(int pos = 54; pos <= 59; pos++) {
				point[pos].setLocation(x, y);
				x += SQUARE;
			}
			
			y -= SQUARE;
			for(int pos = 60; pos < 65; pos++) {
				point[pos].setLocation(x, y);
				y -= SQUARE;
			}
			
			point[65].setLocation(x, y);
			x += SQUARE;
			point[66].setLocation(x, y);
			
			// Red Runaway
			xr=x;
			yr=y;
			yr += SQUARE;
			for(int pos = 68; pos <= 73; pos++) {
				point[pos].setLocation(xr, yr);
				yr += SQUARE;
			}
			// Done Red runaway
			
			x += SQUARE;
			point[67].setLocation(x, y);
			
			
		}
	}

}

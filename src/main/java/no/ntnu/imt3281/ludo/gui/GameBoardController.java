package no.ntnu.imt3281.ludo.gui;

import no.ntnu.imt3281.ludo.logic.*; 
import no.ntnu.imt3281.ludo.server.GameInfo;
import javafx.event.ActionEvent;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Executors;

import javax.naming.CommunicationException;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
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

	// the different labels in the top right corner
    @FXML private Label player1Name;
    @FXML private ImageView player1Active;
    @FXML private Label player2Name;
    @FXML private ImageView player2Active;
    @FXML private Label player3Name;
    @FXML private ImageView player3Active;
    @FXML private Label player4Name;
    @FXML private ImageView player4Active;
    
    /** Displays the thrown dice */
    @FXML private ImageView diceThrown;
    
    /** Activate a dicethrow */
    @FXML private Button throwTheDice;
    
    /** Where chat should appear */
    @FXML private TextArea chatArea;
    
    /** Chat input */
    @FXML private TextField textToSay;
    
    /** The button to send the message in 'textToSay' */
    @FXML private Button sendTextButton;
    
    /** The main window */
    @FXML private AnchorPane boardPane;
    
    /** Shows the image of the actual board */
    @FXML private ImageView board;
    
    private BufferedReader input;
    private BufferedWriter output;
    private Socket socket;
    private int clientId;
    
    // change this plz?
	private TilePositions corners = new TilePositions();
	
	/** Holds the piece images for each players */
	private Image playerPieceImages[] = new Image[PIECES];
	/** Keeps the tiles the pieces are placed in */
	private Rectangle playerPieces[][]= new Rectangle[MAX_PLAYERS][PIECES];
	
	private static final int SQUARE = 48;
	
	/** Reference to a rectangle we are moving from */
	private Rectangle moveFrom = new Rectangle(SQUARE - 2, SQUARE - 2);
	/** Reference to a rectangle we are moving to */
	private Rectangle moveTo = new Rectangle(SQUARE - 2, SQUARE - 2);
	 
	private int CurrentPlayer = RED; 
	private boolean shouldMove = false; 
	private int movePlayerPieceFrom = -1;
	private int diceValue = -1;
	private int gameID;
	//private static int SQUARE = 48;
	private Socket connection;
	
	
    public void setUserId(int id) {
    	this.clientId = id;
    }
    
    private void setRoot(Stage stageroot) {
		// TODO Auto-generated method stub
		
	}

	public void setConnection(Socket socket) {
    	try {
			this.socket = socket;
			input = new BufferedReader(new InputStreamReader(
			        socket.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(
			        socket.getOutputStream()));
			
    	} catch(IOException ioe) {
    		System.err.println("fikk ikke connection, i gameBoardController");
    		ioe.printStackTrace();
    		
    	}
    }
    
	/**
	 * Start the game gui for this game
	 */
	public void StartGameBoard(GameInfo gameInfo){
		this.gameID = gameInfo.getGameID();
		
		// find the connection somhow
		// connection
		
		// Hente ut spillernes navn 
		for ( String playerName : gameInfo.getPlayerNames()) {
			addPlayer(playerName);
		}
		
		// Set the playerlabels to the players in the game
		// TODO: fix null??
		player1Name.setText(getPlayerName(RED));
		player2Name.setText(getPlayerName(BLUE));
		player3Name.setText(getPlayerName(YELLOW));
		player4Name.setText(getPlayerName(GREEN));
		
		// ROTAT som fy
		// Finne ut hvilken spiller er hvilken farge
		/*
		if(Ludo.properties.getProperties().getProoerty("username").equals(getPlayerName(BLUE)))
			CurrentPlayer = BLUE;
		if(Ludo.properties.getProperties().getProoerty("username").equals(getPlayerName(YELLOW)))
			CurrentPlayer = BLUE;
		if(Ludo.properties.getProperties().getProoerty("username").equals(getPlayerName(GREEN)))
			CurrentPlayer = BLUE;
		if(CurrentPlayer == activePlayer())			// Sjekker med Ludo Logic
			throwTheDice.setDisable(false);
		*/
		
		
		// adds a new listner that displays the current dicethrow
		// to the users
		addDiceListener(dEvent -> 
				diceThrown.setImage(new Image(getClass().getResourceAsStream
						("/images/dice" + dEvent.getDice() + ".png")))
				);
		
		// adds a new listner that changes the view
		// when Playerevents happen
		// This is run in the gui-thread
		addPlayerListener(pEvent -> {
			Platform.runLater(() -> playerChange(pEvent));
		});
		
		// TODO komprimere valgt bilde fra 300x300 til 48x48
		// muligens gjort automatisk / sondre fiksa det
		
		
		
		// Gets the images of the different playerpieces and
		// stores them respectably.
		// Bruker getClass fordi det funker for jar fil i tillegg
		playerPieceImages[0] = new Image(getClass().getResourceAsStream("/images/red.png"));
		playerPieceImages[1] = new Image(getClass().getResourceAsStream("/images/blue.png"));
		playerPieceImages[2] = new Image(getClass().getResourceAsStream("/images/yellow.png"));
		playerPieceImages[3] = new Image(getClass().getResourceAsStream("/images/green.png"));
		
		// Show RED as active player
		player1Active.setVisible(true);
		player2Active.setVisible(false);
		player3Active.setVisible(false);
		player4Active.setVisible(false);
		
		
		/* Looping through the players and their pieces
		 * and fills the players' tile to its piece
		 */
		for(int pl = 0; pl < MAX_PLAYERS; pl++) {
			for(int pi = 0; pi < PIECES; pi++) {
				
				playerPieces[pl][pi] = new Rectangle(SQUARE, SQUARE);
				playerPieces[pl][pi].setFill(new ImagePattern(playerPieceImages[pl]));
				
				// Forskyver litt pga at alle skal synes når brikkene er på samme plass, for eks tårn/ mål
				
				/* Math explained:
				 *   Each player has its home pieces in the 16 first tileindexes
				 *   As such: player 1 (BLUE) has his hometile indexes from 4 to 7 (inclusive)
				 *    pl = 0: 0 * 4 + (0 - 3) =  0 + (0 - 3) = ( 0 -  3)
				 *    pl = 1: 1 * 4 + (0 - 3) =  4 + (0 - 3) = ( 4 -  7)
				 *    pl = 2: 2 * 4 + (0 - 3) =  8 + (0 - 3) = ( 8 - 11)
				 *    pl = 3: 3 * 4 + (0 - 3) = 12 + (0 - 3) = (12 - 15)
				 *    
				 *   When the actual tile is found we retrieve the X and Y positions of its
				 *   left upper most corner. With this we can set the piece-rectangles offsett
				 *   The offset for X is gotten like this:
				 *    pi = 0: 8 - 0 * 4 =  8
				 *    pi = 1: 8 - 1 * 4 =  4 
				 *    pi = 2: 8 - 2 * 4 =  0
				 *    pi = 3: 8 - 3 * 4 = -4
				 *    
				 *    Offset for Y is gotten like this:
				 *     pi = 0: 2 + 0 * 2 = 2
				 *     pi = 1: 2 + 1 * 2 = 4
				 *     pi = 2: 2 + 2 * 2 = 6
				 *     pi = 3: 2 + 3 * 2 = 8
				 */
				
				playerPieces[pl][pi].setX(corners.point[pl * 4 + pi].getX() - 8 + pi * 4);
				playerPieces[pl][pi].setY(corners.point[pl * 4 + pi].getX() - 2 + pi * 2);
				
				// add a mouseListner to the rectangles of the pieces
				playerPieces[pl][pi].setOnMouseClicked(e->clickOnPiece(e));
				
				// adds the rectangles to the boardPanes nodelist over
				// updateable components 
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
		
		// when this rectangle is clicked we initiate to move a piece
		moveTo.setOnMouseClicked(e->movePiece(e));
	}



	/**
	 * When the player change, this is called from playerListener
	 * Will be called when the state change between ( PLAYING,  WAITING, LEFTGAME, WON) 
	 * @param event The playerevent that was coused 
	 */
	public void playerChange(PlayerEvent event) { 
		
		int state = event.getState();
		int player = event.getActivePlayer();
		
		switch(player) {
			case RED:
				switch(state) {
					case PlayerEvent.PLAYING:
						player1Active.setVisible(true);
						break;
						
					case PlayerEvent.WAITING:
						player1Active.setVisible(false);
						break;
						
					case PlayerEvent.LEFTGAME:
						// kommer bare hit om RED har blitt REMOVED
						//removePlayer(getPlayerName(RED));
						break;
					case PlayerEvent.WON:
						//endGame();
						break;
						
					default: break;
				} // state switch
			// red break
			break;
				
			case BLUE:
				switch(state) {
					case PlayerEvent.PLAYING:
						player2Active.setVisible(true);
						break;
						
					case PlayerEvent.WAITING:
						player2Active.setVisible(false);
						break;
						
					case PlayerEvent.LEFTGAME:
						// kommer bare hit om BLUE har blitt REMOVED
						//removePlayer(getPlayerName(BLUE));
						break;
					case PlayerEvent.WON:
						//endGame(YELLOW);
						break;
						
					default: break;
				} // stateswitch
			// break blue
			break;
				
			case YELLOW:
				switch(state) {
					case PlayerEvent.PLAYING:
						player3Active.setVisible(true);
						break;
						
					case PlayerEvent.WAITING:
						player3Active.setVisible(false);
						break;
						
					case PlayerEvent.LEFTGAME:
						// kommer bare hit om YELLOW har blitt REMOVED
						//removePlayer(getPlayerName(YELLOW));
						break;
					case PlayerEvent.WON:
						//endGame(YELLOW);
						break;
						
					default: break;	
				} // stateswitch
			// break yellow
			break;
			
			case GREEN:
				switch(state) {
					case PlayerEvent.PLAYING:
						player4Active.setVisible(true);
						break;
						
					case PlayerEvent.WAITING:
						player4Active.setVisible(false);
						break;
						
					case PlayerEvent.LEFTGAME:
						// kommer bare hit om GREEN har blitt REMOVED
						//removePlayer(getPlayerName(GREEN));
						break;
					case PlayerEvent.WON:
						//endGame(GREEN);
						break;
						
					default: break;	
				} // stateswitch
			// break green
			break;
		} // playerswitch
	}
	
	/**
	 * Handles the press on the 'throwDice'-button. Asks
	 * server to throw a dice
	 * @param e - The action performed (should be a buttonpress)
	 */
	@FXML
	void throwDice(ActionEvent e) {
		System.out.println("Du har trykket på dice din æss");
		try {
			ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
			// send a DiceEvent to proc the server to throw a dice
			// oos.writeObject(new GameEvent(gameID, new DiceEvent(this, activePlayer, 0)));
			
			// TODO: We should be in blockingmode here
			// TODO: get the events in some listner
			ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
			Object obj = ois.readObject();
			
			// we only want to handle GameEvents here
			// if(obj ) { // instanceof GameEvent
				// GameEvent ge = (GameEvent) obj;
				
				// we want to ensure we don't do changes
				// that isn't for our game
			/*
				if(ge.getGameID() == gameID) {
					
					// we only want to handle DiceEvents here
					if(ge.getEvent() instanceof DiceEvent) {
						DiceEvent de = (DiceEvent) obj;
						
						// check if we are synched
						if(de.getPlayer() == activePlayer) {
							
							// throw the dice in this instance
							// of the game and change the view
							super.throwDice(de.getDice());
							diceThrown.setImage(new Image(getClass()
									.getResourceAsStream("/images/dice" + dice + ".png")));
						} // if player
					} // if diceEvent
				} // if gameid
			} // if gameEvent
			*/
		}
		catch(IOException ioe) {
			// TODO: socket not found?
		}
		catch(ClassNotFoundException cnfe) {
			// TODO: invalid class
		}
	}
	
	/*
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
	}*/
	
	
	/**
	 * 
	 */
	public void updateBoard(){
		// TODO 
	}
	
	
	/**
	 * Handles mouseclicks on the board
	 * @param event
	 */
	@FXML
	void clickOnPiece(MouseEvent event) {
		
		int tile = 0;
		Object obj = event.getSource();
				
		for (int pi = 0; pi < PIECES; pi++) {
			if(obj.equals(playerPieces[activePlayer][pi])) {
				tile = corners.findTile(event.getSceneX(), event.getSceneY());
				moveFrom.setX(corners.point[tile].getX());
				moveFrom.setY(corners.point[tile].getY());
				
				moveTo.setX(corners.point[tile + dice].getX());
				moveTo.setY(corners.point[tile + dice].getY());
			}
		}
			
		/*	
			// Skjer bare hvis spiller skal flytte
			if(shouldMove) {	
		
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
				
			}*/
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
	 *  Handle the clickevent to mova a piece
	 * @param e The event from the click
	 */
	private void movePiece(MouseEvent e) { 
		
		if(e.getEventType() == MouseEvent.MOUSE_CLICKED) {
			int moveFromTile = corners.findTile(e.getSceneX(), e.getSceneY());
			int i = 0;
			
			// gets which piece is selected
			while(playerPieces[activePlayer][i].equals(moveFrom) && i++ < PIECES);
			
			int piece = getPieceAt(activePlayer, moveFromTile);
			int from = getPosition(activePlayer, piece);
			
			if(super.movePiece(activePlayer, from, from + dice)) {
				// TODO: call the function that moves the piece
				// on the screen
				// OR
				// just do this?
				// playerPieces[activePlayer][i].setX(moveTo.getX());
				// playerPieces[activePlayer][i].setY(moveTo.getY());
				
				try {
					// send the info to the server
					ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
					// oos.writeObject(new GameEvent(gameID, new PieceEvent(this, activePlayer, piece, from, from + dice)));
					//connection.send(new MovePiece(gameId, activePlayer, from, from + dice));
					
					//playerPieces[activePlayer][i].setX();
				}
				catch(IOException ioe) {
					// TODO: log?
				}
			}
			else {
				// If we can't move, remove destination selection
				// as indicator that the user can't move (with that piece)
				moveTo.setX(-100);
				moveTo.setY(-100);
			}
		}
		// ELSE do nothing
		
	}
	
	
	/**
	 * Tries to get a piece from the given player at the given position (actual)
	 * @param player The players to retrieve a piece from
	 * @param from The actual tileposition on the board
	 * @return The index of the players piece at the given position, or
	 * -1 if no piece where found
	 */
	private int getPieceAt(int player, int from) {
		boolean found = false;
		int i = 0;
		
		// loops through all a players pieces and sees if
		// their position matches the given
		// IF so, we select this piece
		while(!found && i < PIECES) {
			int pos = userGridToLudoBoardGrid(player, getPosition(player, i));
			
			if(pos == from) {
				found = true;
			}
		}
		
		if(!found) {
			i = -1;
		}
		
		return i;
	}
		
	/*
	private int movePieceFrom(MouseEvent event) {
		int correctValue = -1;
		int x = (int) event.getX();
		int y = (int) event.getY(); 
		
		Object ob = event.getSource();
		
			
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
		
		return ;
	}*/
	
	
	
	/**
	 * Holds all the different positions on the board 
	 */
	private class TilePositions {
		
		private static final int MAX_TILES = 92;
		private Point point[] = new Point[MAX_TILES];
		
		/** Specifies the common Y-coord of postions to the left*/
		private static final int LEFT_COLUMN_TILE = 122;
		/** Specifies the common Y-coord of postions to the right*/
		private static final int RIGHT_COLUMN_TILE = 554;
		
		/** Specifies the common X-coord of positions to the top*/
		private static final int TOP_COLUMN_TILE = 74;
		/** Specifies the common X-coord of positions to the bottom*/
		private static final int BOTTOM_COLUMN_TILE = 506;
		
		
		/**
		 * Fills and array of Points with the position of
		 * each tile in the game, represented by its upper
		 * left corner
		 */
		public TilePositions() {
			
			for(int i = 0; i < MAX_TILES; i++) {		
				point[i] = new Point(); 
			}
			
			/* all 'home' tiles defined as such:
			 *  - X: a tile
			 *  - O: and empty tile
			 * 
			 * Defined in this order:
			 *           X
			 *       X
			 *           X
			 *               X
			 *               
			 *  This results in:
			 *  		 X
			 *       X   O   X
			 *           X   
			 *               
			 */	
			
			// RED
			point[0].setLocation(RIGHT_COLUMN_TILE, TOP_COLUMN_TILE);
			point[1].setLocation(RIGHT_COLUMN_TILE + SQUARE, TOP_COLUMN_TILE + SQUARE);
			point[2].setLocation(RIGHT_COLUMN_TILE, TOP_COLUMN_TILE + SQUARE * 2);
			point[3].setLocation(RIGHT_COLUMN_TILE - SQUARE, TOP_COLUMN_TILE + SQUARE);
			
			// BLUE
			point[4].setLocation(RIGHT_COLUMN_TILE, BOTTOM_COLUMN_TILE);
			point[5].setLocation(RIGHT_COLUMN_TILE + SQUARE, BOTTOM_COLUMN_TILE + SQUARE);
			point[6].setLocation(RIGHT_COLUMN_TILE, BOTTOM_COLUMN_TILE + SQUARE * 2);
			point[7].setLocation(RIGHT_COLUMN_TILE - SQUARE, BOTTOM_COLUMN_TILE + SQUARE);
			
			// YELLOW
			point[8].setLocation(LEFT_COLUMN_TILE, BOTTOM_COLUMN_TILE);
			point[9].setLocation(LEFT_COLUMN_TILE + SQUARE, BOTTOM_COLUMN_TILE + SQUARE);
			point[10].setLocation(LEFT_COLUMN_TILE, BOTTOM_COLUMN_TILE + SQUARE * 2);
			point[11].setLocation(LEFT_COLUMN_TILE - SQUARE, BOTTOM_COLUMN_TILE + SQUARE);
			
			// GREEN
			point[12].setLocation(LEFT_COLUMN_TILE, TOP_COLUMN_TILE);
			point[13].setLocation(LEFT_COLUMN_TILE + SQUARE, TOP_COLUMN_TILE + SQUARE);
			point[14].setLocation(LEFT_COLUMN_TILE, TOP_COLUMN_TILE + SQUARE * 2);
			point[15].setLocation(LEFT_COLUMN_TILE - SQUARE, TOP_COLUMN_TILE + SQUARE);
			
			
			//////// Set board 16-91 ////////////////////
			
			int xr = 0;  			// For runaway
			int yr = 0;	   			// For runaway
			
			int x = 8 * SQUARE;		// starts on square 16
			int y = SQUARE;	   		
			
			// move from 16 -> 20
			for(int pos = 16; pos <= 20; pos++) {
				point[pos].setLocation(x, y);
				y += SQUARE;
			}
			
			// skipping blank space / move to 21
			x += SQUARE;
			
			// move from 21 to 26
			for(int pos = 21; pos <= 26; pos++) {
				point[pos].setLocation(x, y);
				x += SQUARE;
			}
			
			// move to 27
			x -= SQUARE;
			y += SQUARE;
			point[27].setLocation(x, y);
			
			
			// Blue Runaway 
			xr = x;
			yr = y;
			xr -= SQUARE;
			for(int pos = 74; pos <= 79; pos++) {
				point[pos].setLocation(xr, yr);
				xr -= SQUARE;
			}
			// Done Blue runaway
			
			// move to 28
			y += SQUARE;
			
			// move from 28 to 33
			for(int pos = 28; pos <= 33; pos++) {
				point[pos].setLocation(x, y);
				x -= SQUARE;
			}
			
			// skipping the middle / move to 34
			y += SQUARE;
			
			// move from 34 to 39
			for(int pos = 34; pos <= 39; pos++) {
				point[pos].setLocation(x, y);
				y += SQUARE;
			}
			
			// move to 40
			x -= SQUARE;
			y -= SQUARE;
			point[40].setLocation(x, y);
			
			
			// Yellow Runaway
			xr = x;
			yr = y;
			yr -= SQUARE;
			for(int pos = 80; pos <= 85; pos++) {
				point[pos].setLocation(xr, yr);
				yr -= SQUARE;
			}
			// Done Yellow runaway
			
			// move to 41
			x -= SQUARE;
			
			// move from 41 to 46
			for(int pos = 41; pos <= 46; pos++) {
				point[pos].setLocation(x, y);
				y -= SQUARE;
			}
			
			// move to 47
			x -= SQUARE;
			
			// move from 47 to 52
			for(int pos = 47; pos <= 52; pos++) {
				point[pos].setLocation(x, y);
				x -= SQUARE;
			}
			
			// TODO: må kanskje sette 52 annerledes pga
			// pixel > 0
			// point[52].setLocation(x, y);
			
			// move to 53
			x += SQUARE;
			y -= SQUARE;
			point[53].setLocation(x, y);
			
			// Green Runaway
			xr = x;
			yr = y;
			xr += SQUARE;
			for(int pos = 86; pos <= 91; pos++) {
				point[pos].setLocation(xr, yr);
				xr += SQUARE;
			}
			// Done Green runaway
			
			// move to 54
			y -= SQUARE;
			
			// move from 54 to 59
			for(int pos = 54; pos <= 59; pos++) {
				point[pos].setLocation(x, y);
				x += SQUARE;
			}
			
			// skip middle / move to 60
			y -= SQUARE;
			
			// move from 60 to 65
			for(int pos = 60; pos <= 65; pos++) {
				point[pos].setLocation(x, y);
				y -= SQUARE;
			}
			
			// TODO: må kanskje sette 65 annerledes pga
			// pixel > 0
			// point[65].setLocation(x, y);
			
			// move to 66
			x += SQUARE;
			y += SQUARE;
			point[66].setLocation(x, y);
			
			// Red Runaway
			xr = x;
			yr = y;
			yr += SQUARE;
			for(int pos = 68; pos <= 73; pos++) {
				point[pos].setLocation(xr, yr);
				yr += SQUARE;
			}
			// Done Red runaway
			
			// move to 67
			x += SQUARE;
			point[67].setLocation(x, y);	
		} // constructor end
		
		
		/**
		 * Finds a tile on the board based on the given position
		 * @param x - X position
		 * @param y - Y position
		 * @return The tileindex found, or -1 otherwise
		 */
		private int findTile(double x, double y) {
			
			boolean found = false;
			int i = 0;
			
			double xs; // start of tile
			double ys; // start of tile
			double xe; // end of tile
			double ye; // end of tile
			
			while(!found && i < MAX_TILES) {
				
				xs = point[i].getX();
				ys = point[i].getY();
				
				xe = point[i].getX() + SQUARE;
				ye = point[i].getY() + SQUARE;
				
				if(((xs < x) && (x < xe)) && ((ys < y) && (y < ye))) {
					found = true;
				}
				
				i++;
			}
			
			if(!found) {
				i = -1;
			}
			
			return i;
		}
	} // TilePositions end
	
    @FXML
    public void sendText(ActionEvent e) {
    	System.err.println("tull");
    	String txt = textToSay.getText();
    	if(!txt.equals("") && txt !=null) {
    		int currentChatID = 2;
    		int clientId = 302; 
    		try {			
    			System.out.println("1. ClientGAME!!: SaySomething fra client: "+txt);
    			output.write("CHAT,"+currentChatID+","+clientId +"," +txt);
				output.newLine();
				output.flush();

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    }

} // GameBoardController end

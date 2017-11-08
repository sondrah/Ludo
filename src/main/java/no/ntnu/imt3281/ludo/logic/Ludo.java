package no.ntnu.imt3281.ludo.logic;

import java.util.Random;
import java.util.Vector;

import org.apache.derby.impl.sql.catalog.SYSCONGLOMERATESRowFactory;
import org.apache.derby.impl.sql.catalog.SYSSEQUENCESRowFactory;

import com.sun.javafx.geom.transform.GeneralTransform3D;

/**
 * Main Class that represents the
 * actual game
 */
public class Ludo {
	/** Red player */
	public static final int RED = 0;
	/** Blue player */
	public static final int BLUE = 1;
	/** Yellow player */
	public static final int YELLOW = 2;
	/** Green player */
	public static final int GREEN = 3;
	/** Maximum number of players */
	private static final int MAX_PLAYERS = 4;
	/** Minimum number of players */
	private static final int MIN_PLAYERS = 2;
	/** Number of pieces */
	private static final int PIECES = 4;
	/** Number of shared/common squares on the board
	 * , also the start of the runway */
	private static final int COMMON_GRID_COUNT = 54;
	/** The last tile on the board */
	private static final int GOAL = 59;
	/** Number of subsequent throws*/
	private static int nrOfThrows = 0;
	
	/** An Vector with the current players */
	private Vector<String> players;
	
	/** The current activePlayer */
	private int activePlayer;
	
	/** The last dicethrow-value */
	private int dice;
	
	/** Used to simulate dicethrows */
	private Random randomGenerator;
	
	/** A 2D integer array to hold the different players pieces*/
	private int[][] playerPieces;
	
	/** A 2D integer array to hold the different players
	 * translated board positions */
	private int[][] userGridToPlayerGrid;
		
	/** A Vector with the different DiceListners */
	private Vector<DiceListener> diceListeners;
	
	/** A Vector with the different PieceListners */
	private Vector<PieceListener> pieceListeners;
	
	/** A Vector with the different PlayerListners */
	private Vector<PlayerListener> playerListeners;
	
	/**
	 * Default Constructor for the Ludo class
	 */
	public Ludo(){
		setUpGame();
	}
	
	/**
	 * Constructs a Ludo object with the given players
	 * 
	 * @param p1 player1
	 * @param p2 player2
	 * @param p3 player3
	 * @param p4 player4
	 * @throws NotEnoughPlayersException
	 */
	public Ludo(String p1, String p2, String p3, String p4) throws NotEnoughPlayersException {
		setUpGame();
		
		players.add(RED, p1);
		players.add(BLUE, p2);
		players.add(YELLOW, p3);
		players.add(GREEN, p4);
		
		if(nrOfPlayers() >= MIN_PLAYERS && nrOfPlayers() <= MAX_PLAYERS){
			//throw new NotEnoughPlayersException("No exception should be thrown");
		}
		else {
			players.clear();
			throw new NotEnoughPlayersException(
					  "Ludo#Ludo(String, String, String, String):"
					+ "The number of players must be more than 2!\n");
		}
	}
	
	/**
	 * Converts the given colured number to the relative
	 * board (black) number
	 * 
	 * @param player colour
	 * @param players number
	 * @return The translated pos as int (black number)
	 */
	public int userGridToLudoBoardGrid(int player, int userPosition){
		int gridPosition = 0;
		
		if(userPosition == 0) {
			switch(player) {
			case RED: 		gridPosition = 0;	break;
			case BLUE: 		gridPosition = 4;	break;
			case YELLOW:	gridPosition = 8;	break;
			case GREEN: 	gridPosition = 12;	break;
			}
		}
		else {
			gridPosition = userGridToPlayerGrid[player][userPosition];
		}
		
		return gridPosition;
	}
	
	/**
	 * Gives you the number of players in
	 * this ludogame
	 * @return number of players as int
	 */
	public int nrOfPlayers(){
		int ps = 0;
		
		for (int i = 0; i < players.size(); i++) {
			if(players.get(i) != null) ps++;
		}
		
		return ps;
	}
	
	/**
	 * Returns the number of active players
	 * in this game
	 * @return the number of active players
	 */
	public int activePlayers(){
		int ap = 0;
		for(int i = 0; i < players.size(); i++) {
			if(players.get(i) == null) {
				// do nothing
			}
			else if(!players.get(i).startsWith("Inactive: ")) ap++;
		}
		return ap;
	}
	
	/**
	 * Retrieves the name of a player at the given
	 * index in the playervector
	 * 
	 * @param player Index of the player
	 * @return the players name as String
	 */
	public String getPlayerName(int player){
		// allowed numbers = 0, 1, 2, 3
		if(player >= 0 && player <= nrOfPlayers()){
			return players.get(player);
		}
		else{
			// TODO: feilmelding?
			return null;
		}
	}
	
	/**
	 * Tries to add the given player to the game
	 * @param player the player to be added
	 * @throws IllegalPlayerNameException
	 * @throws NoRoomForMorePlayersException
	 */
	public void addPlayer(String player) throws IllegalPlayerNameException,
												NoRoomForMorePlayersException {
		if(nrOfPlayers() >= MAX_PLAYERS) {
			throw new NoRoomForMorePlayersException(
					  "Ludo#addPlayer(String): Game already "
					  + "has 4 players\n");
		}
		else {
			for(String p : players) {
				if(p == player)
					throw new IllegalPlayerNameException(
								  "Ludo#addPlayer(String): Name: "
								+ player + ", is already taken!\n"
										+ "");
			}
			
			if(player.startsWith("****"))
				throw new IllegalPlayerNameException(
								  "Ludo#addPlayer(String): Can't"
								+ " start with ****!");
			
			else players.add(player);
		}
	}
	
	/**
	 * "Removes" the given player from the game. Marks them as inactive
	 * @param player to be marked as inactive
	 * @throws NoSuchPlayerException
	 */
	public void removePlayer(String player) throws NoSuchPlayerException{
		boolean found = false;
		int i = 0;
		
		while(!found && i < players.size()) {
			if(players.get(i) == player) {
				StringBuilder sb = new StringBuilder("Inactive: ");
				sb.append(players.remove(i));
				players.add(i, sb.toString());
				found = true;
			}
			i++;
		}
		
		if(!found) {
			throw new NoSuchPlayerException(
					  "Ludo#removePlayer(String):"
					+ "The player" + player + " was not found"); 
		}
	}
	
	
	/**
	 * Gets the position of the given player's given peice,
	 * relative to him
	 * @param player the player whose piece we want
	 * @param piece we want to find
	 * @return the position, relative to the player, as int
	 */
	public int getPosition(int player, int piece){
		return playerPieces[player][piece];
	}
	

	/**
	 * Gets the current active player (RED, GREEN, YELLOW, BLUE) 
	 * @return the current active player
	 */
	public int activePlayer() {
		return this.activePlayer;
	}
	
	/**
	 * Throws a D6
	 * @return value between 1 and 6 (inclusive)
	 */
	public int throwDice() {
		int locDice = 0;
		randomGenerator = new Random();
		
		locDice = randomGenerator.nextInt(5) + 1;
		
		// might give the turn to the next player
		// if he/she has 3 throws
		if(nrOfThrows == 3) {
			nextPlayer();
		}
		
		nrOfThrows++;
		alertThrowDice(new DiceEvent(this, locDice, activePlayer));
		System.err.println("Threw dice:" + locDice);
		return locDice;
	}
	

	/**
	 * Clientside dicethrow, returns the given value
	 * @param value the value the throw should result in
	 * @return the given value
	 */
	public int throwDice(int value) {	
		System.err.println("throwDice: START");
		alertThrowDice(new DiceEvent(this, value, activePlayer));
		dice = value;
		
		System.err.println("Throw #: " + nrOfThrows);
		
		if(!canMove()) {
			if(nrOfPlayedPieces(activePlayer) > 0) {
				if(nrOfThrows + 1 == 1) {
					nextPlayer();
					nrOfThrows--;
				}
			}
			else{
				if(nrOfThrows + 1 == 3) {
					nextPlayer();
					nrOfThrows--;
				}
			}
		}
		nrOfThrows++;
		
		System.err.println("Threw dice: " + value);
		System.err.println("throwDice: END");
		return value;
	}

	
	/**
	 * Checks if the current move: 
	 * Is from correct position
	 * Is equal to dice
	 * Is allowed from start (must have a six to be allowed to move a piece)
	 * is not in conflict with a tower
	 * @param player which player is trying to move a piece
	 * @param piece 
	 * @param from coordinates piece is being moved from
	 * @param to coordinates piece is being moved to
	 * @return true if it can move, otherwise false
	 */
	/*private boolean canMove(int player, int piece, int from, int to) {			// TODO
		boolean movable = false;
		
		
		if(allHome(activePlayer) && dice == 6) {	// Om alle er hjemme må vi ha 6 for å flytte
			movable = true;
		}
		
		for(int i = 0; i < PIECES; i++) {
			getPosition(activePlayer, i);
		}
		
		// Om ikke alle brikker er hjemme, må vi sjekke om from-to diff. er OK
		// Om vi har < 4 brikker i hjem, men ønsker å flytte ut en ny
		// må vi bypasse from-to diff. sjekk
		else if(dice == from-to || getPosition(player, piece) == 0) {
			if(!checkBlockAt(player, from, to)) {
				movable = true; 
			}
			else movable = false;
			
			if(to > 59) movable = false;				// Alle to verdier over 59
														// er ikke mulig å flytte til 
		}
		return (movable);
	}*/
	
	
	/**
	 * Checks if the activePlayer have any pieces he/she
	 * can move.
	 * @return true if the player can move one piece
	 */
	private boolean canMove() {
		boolean moveable = false;
		
		System.err.println("canMove: START");
		
		// if one ore more pieces is in play
		if(nrOfPlayedPieces(activePlayer) > 0) moveable = true;
		
		// if all is home, we need 6
		if(allHome(activePlayer) && dice == 6) moveable = true;
		
		for(int pi = 0; pi < PIECES; pi++) {
			int pos = getPosition(activePlayer, pi);
			
			if(pos != 0) {
				if(!blocked(activePlayer, pos, pos + dice)) {
					moveable = true;
				}
				else moveable = false;
			}
		}
		
		for(int pi = 0; pi < PIECES; pi++) {
			int pos = getPosition(activePlayer, pi);
			
			if((pos + dice) <= GOAL) {
				moveable = false;
			}
		}
		
		System.err.println("canMove: " + moveable);
		System.err.println("canMove: END");
		return moveable;
	}
	
	
	/**
	 * Tries to moves a player's piece
	 * @param player which player is moving a piece
	 * @param from relative position to move from
	 * @param to relative position to move to
	 *  
	 * @return true if piece was moved, false otherwise
	 */
	public boolean movePiece(int player, int from, int to) {	
		boolean movable = false;
		
		System.err.println("movePiece: START");
		
		int pieceindex = -1;						// Trengs for å garantere at bare
		int i = 0;									// en og første brikke flyttes
		
		if(canMove()) {									// Hvis spilleren i det hele tatt 
														// har en brike som kna flyttes
			while ( i < PIECES && pieceindex == -1) {	// går igjennom alle brikkene frem til
				if (getPosition(player, i) == from) { 	// første brikke som er på denne ruten
					pieceindex = i;
				}
				i++;
			}
			
			System.err.println("pieceindex: " + pieceindex);
			
			/* if we found the valid piece,
			 * we check if that particular piece is blocked,
			 * then, we need to have a small check if the piece
			 * has the required number on the dice to finish
			 */
			if (pieceindex != -1) {
				if(!checkBlockAt(player, pieceindex, from, to)) {	// Hvis det ikke er en blokkade
					if((from + dice) < GOAL) {						// Hvis mål nås akkurat. 
						playerPieces[player][pieceindex] = to;
						System.err.println("pl: " + player + ", pi: " + pieceindex + ", to: " + to);
						
						checkUnfortunateOpponent(player, to);
						
						// tell clients that a piece is moved
						alertPieces(new PieceEvent(this, activePlayer, pieceindex, from, to));
						
						// give the turn to the next player
						// unless he got a 6 and isn't going
						// out of home
						if(from == 0 && dice == 6) nextPlayer();
						if(dice != 6) nextPlayer();
						
						System.err.println("Moved piece!");
						
						// WE CAN MOVE
						movable = true;
					} // if goal
				} // check tower
			} // found piece
		} // we can move
		
		System.err.println("movePiece: END");
		
		return movable;
	}
	
	/**
	 * Checks if there is a tower in the way for this players
	 * given piece between the two positions "to" and "from"
	 * 
	 * @param player whose piece to move
	 * @param piece we want to move
	 * @param from relative position to move from
	 * @param to relative position to move to
	 * 
	 * @return true if no towers were found, false otherwise
	 */
	private boolean checkBlockAt(int player, int piece, int from, int to) {
		
		System.err.println("checkBlockAt: START");
		
		// gets the actual positions on the board
		int fromGridPos = userGridToLudoBoardGrid(player, from);
		int toGridPos = userGridToLudoBoardGrid(player, to);
		
		// finds the number of tiles between "to" and "from"
		int dist = to - from;
		
		// holds how many pieces one player has on
		// one of the tiles between "to" and "from"
		int pieceAtPos[][] = new int[4][dist];
		//int twr[] = new int[4];
		
		boolean blockade = false;				// if we found a tower
		
		/*
		 * Loops through all possible players (they should have pieces
		 * even if the player is null)
		 * 
		 * Then loops through all the players pieces. If the
		 * grid position of that piece is in the path from
		 * "from" to "to", increment that players information on
		 * that tile.
		 */
		for(int pl = 0; pl < MAX_PLAYERS; pl++) {
			if(pl != player) {		// egne tårn skal ikke telles
				
				for(int pi = 0; pi < PIECES; pi++) {
					int pieceGridPos = userGridToLudoBoardGrid(pl, getPosition(pl, pi));
					
					/*System.err.println("fromGridPos: " + fromGridPos);
					System.err.println("toGridPos: " + toGridPos);
					System.err.println("pl: " + pl);
					System.err.println("dist: " + dist);
					System.err.println("piecePos: " + pieceGridPos);
					System.err.println((pieceGridPos > fromGridPos) && (pieceGridPos <= toGridPos));
					*/
					
					if(from == 0) {
						if(pieceGridPos == toGridPos) {
							pieceAtPos[pl][0]++;
						}
					}
					else if(pieceGridPos > fromGridPos && pieceGridPos <= toGridPos) {
						// should get positions relative to "from" as index
						// ex: pieceGridPos = 21, fromGridPos = 20
						// index = 21 - 20 - 1 = 0
						int index = pieceGridPos - fromGridPos - 1;
						pieceAtPos[pl][index] += 1;
					}
				}
			}
		}
		
		// we need something to itterate on :D (again)
		int i = 0;
		// loops untill we find a blockade or we've seen
		// all tiles between "to" and "from"
		while(!blockade && i < dist) {
			for(int pl = 0; pl < MAX_PLAYERS; pl++) {
				// we hava a tower if a player have 2 or more
				// pieces on one tile
				if(pieceAtPos[pl][i] >= 2) blockade = true;
				
				//System.err.println("pl: " + pl + " | tile: " + i + " = " + pieceAtPos[pl][i]);
			}
			i++;
		}
		
		System.err.println("checkBlockAt: " + blockade);
		System.err.println("checkBlockAt: END");
		return blockade;
	}
	

	/**
	 * Gets the current state of the game
	 * @return gamestate
	 */
	public String getStatus() {
		String res = null;
		
		// if we have 0 players in the game: CREATED
		if(nrOfPlayers() == 0) {
			res = "Created";
		}
		// if we have one or more players
		else if(activePlayers() >= 1){
			// if no dice is thrown: INITIATED
			if(dice == 0) {
				res = "Initiated";
			}
			// the game is started (couse a dice is thrown)
			// :STARTED
			else { 
				res = "Started";
			}
		}
		
		// if we have a winner: FINISHED
		if(getWinner() >= 0) {
			res = "Finished";
		}
		
		return res;
	}
	
	
	/**
	 * Checks if someone has won the game
	 * @return the winner of the game
	 */
	public int getWinner() {
		int winner = -1;
		
		for(int pl = 0; pl < activePlayers(); pl++) {
			for(int pi = 0; pi < PIECES; pi++) {
				if(getPosition(pl, pi) == GOAL) winner = pi; 
			}
		}
		
		return winner;
	}
	
	
	/**
	 * Adds a DiceListner to the game
	 * @param diceListner to be added
	 */
	public void addDiceListener(DiceListener diceListner) {
		diceListeners.add(diceListner);
	}
	
	/**
	 * Adds PlayerListner to the game
	 * @param playerListner to be added
	 */
	public void addPlayerListener(PlayerListener playerListner) {
		playerListeners.add(playerListner);
	}
	
	/**
	 * Adds a PieceListner to the game
	 * @param pieceListner to be added
	 */
	public void addPieceListener(PieceListener pieceListner) {
		pieceListeners.add(pieceListner);
	}
	
	
	/**
	 * Returns the array responsible for grid-convertions
	 * @return the relativ grid
	 */
	private int[][] getUserGridToPlayGrid(){
		// userGridToPlayerGrid[player][numbCol]
		return userGridToPlayerGrid;
	}
	
	/**
	 * Checks if all the players pieces are home
	 * @param player the player to check for
	 * @return true if all pieces are home, false otherwise
	 */
	private boolean allHome(int player) {
		int piecesInHome = 0;
		
		for(int piece = 0; piece < PIECES; piece++) {
			if(getPosition(player, piece) == 0) {
				piecesInHome++;
			}
		}
		
		if(piecesInHome == 4) return true;
		else return false;
	}
	
	
	/**
	 * Gives the turn to the next player in the queue
	 */
	private void nextPlayer() {
		System.err.println("nextPlayer: START");
		
		// changes state of prev player
		alertPlayers(new PlayerEvent(this, activePlayer, PlayerEvent.WAITING));
		
		// reset the throwcount for a new player
		nrOfThrows = 0;
		
		System.err.println("nextPlayer - prev: " + activePlayer);
		//if(activePlayer == MAX_PLAYERS - 1) {
		if(activePlayer == nrOfPlayers() - 1) {
			activePlayer = 0;
		}
		else activePlayer++;
		System.err.println("nextPlayer - cur: " + activePlayer);
		
		// changes state of next player
		alertPlayers(new PlayerEvent(this, activePlayer, PlayerEvent.PLAYING));
		
		System.err.println("nextPlayer: END");
	}
	
	
	
	/**
	 * Checks if the player is blocked
	 * 
	 * @param player to check for
	 * @param from the relative position to move from
	 * @param to the relatice position to move to
	 * 
	 * @return true if all pieces are unable to move, false otherwise
	 */
	private boolean blocked(int player, int from, int to) {
		
		System.err.println("blocked: START");
		
		boolean allBlocked = true;
		
		// perfomes a AND with all the pieces. If one of these can move
		// checkBlockAt() will return false and the AND will make
		// allBlocked be false.
		for(int i = 0; i < PIECES; i++) {
			allBlocked = allBlocked && checkBlockAt(player, i, from, to);
			//System.err.println("pi: " + i + " | block: " + allBlocked);
		}
		
		System.err.println("returns: " + allBlocked);
		System.err.println("blocked: END");
		return allBlocked;
	}
	
	
	/**
	 * check if there are Unfortunate Opponent on the position player moves to
	 * @param player that moves
	 * @param position he moved to (relative)
	 */
	private void checkUnfortunateOpponent(int player, int to) {
		int gridPos = userGridToLudoBoardGrid(player, to);
		
		System.err.println("gridPos: " + gridPos);
		
		for(int pl = 0; pl <= activePlayers(); pl++) {	
			if(pl != player) {
				for(int pi = 0; pi < PIECES; pi++) { 
					int pieceGridPos = userGridToLudoBoardGrid(pl, getPosition(pl, pi));
					
					System.err.println("pieceGridPos: " + pieceGridPos);
					
					if(pieceGridPos == gridPos) {
						alertPieces(new PieceEvent(this, pl, pi, to, 0));
						
						playerPieces[pl][pi] = 0;
					}
				}
			}
		}	
	}
	
	/**
	 * Checks if someone have won the game
	 */
	private void checkWinner() {
		boolean won = false;
		
		for(int pl = 0; pl < MAX_PLAYERS; pl++) {
			for(int pi = 0; pi < PIECES; pi++) {
				if(getPosition(pl, pi) == GOAL) won = true;
			}
		}
		
		if(won) {
			for(PlayerListener playerListener : playerListeners) {
				playerListener.playerStateChanged(
						new PlayerEvent(this, activePlayer, PlayerEvent.WON)
					);
			}
		}
	}
	
	
	/**
	 * Sets up the common stuff for the constructors. Such as
	 * the playerPieces and empty vectors
	 */
	private void setUpGame(){
		playerPieces = new int[MAX_PLAYERS][PIECES];
		userGridToPlayerGrid = new int[MAX_PLAYERS][COMMON_GRID_COUNT + 6];
		
		activePlayer = RED;
		dice = 0;
		nrOfThrows = 0;
		
		// empty vectors != null
		diceListeners = new Vector<>();
		pieceListeners = new Vector<>();
		playerListeners = new Vector<>();
		players = new Vector<>();
		
		
		// inits the playerPieces to be at home
		for(int player = 0; player < MAX_PLAYERS; player++) {
			for(int piece = 0; piece < PIECES; piece++) {
				playerPieces[player][piece] = 0;				
			}
		}
		
		// creates the playerToUserGrid
		setUpPos(RED, 16, 68);
		setUpPos(BLUE, 29, 74);
		setUpPos(GREEN, 55, 86);
		setUpPos(YELLOW, 42, 80);
	}
	
	
	/**
	 * Sets up each position in userGridToPlayerGrid
	 * From relative position to gridPosition 
	 * @param player to set up
	 * @param start position for the player (grid position)
	 * @param runway position of the players runway (grid position)
	 */
	private void setUpPos(int player, int start, int runway) {
		int startGridPos = start;							// Setter startverdien til den svarte
		
		for(int colInt = 1; colInt < COMMON_GRID_COUNT; colInt++) {	// Går rundt hele ytre bane
			userGridToPlayerGrid[player][colInt] = startGridPos;
			if(startGridPos == 67) startGridPos = 15;					// Spesialhådterer tallskifte
			startGridPos++;
		}

		int runwayGridPos = runway;  							//Setter startverdien på oppløpet
		for(int colInt = COMMON_GRID_COUNT; colInt < COMMON_GRID_COUNT + 6; colInt++) {	// Går opp hele oppløpet
			userGridToPlayerGrid[player][colInt] = runwayGridPos;
			runwayGridPos++;
		}
	}
	
	
	/**
	 * Alerts all registered PlayerListeners of the given event.
	 * @param event The PlayerEvent that should be sent to the listeners.
	 */
	private void alertPlayers(PlayerEvent event) {
		for(PlayerListener playerListener : playerListeners) {
			playerListener.playerStateChanged(event);
		}
	}
	
	
	/**
	 * Alerts all registered PieceEvents of the given event
	 * @param event The PieceEvent that should be sent to the listeners.
	 */
	private void alertPieces(PieceEvent event) {
		for(PieceListener pieceListener : pieceListeners) {
			pieceListener.pieceMoved(event);
		}
	}
	
	
	/**
	 * sends the DiceEvent to all registered DiceListners
	 * @param event The DiceEvent that should be sent to the listeners.
	 */
	private void alertThrowDice(DiceEvent event) {
		for(DiceListener dl : diceListeners) {
			dl.diceThrown(event);
		}
	}
	
	/**
	 * 
	 */
	private boolean checkThreeInARow() {
		System.err.println("checkThreeInARow: " + nrOfThrows);
		if(nrOfThrows == 3) {
			return true;
		}
		else {
			nrOfThrows++;
			return false;
		}
	}
	
	
	/**
	 * Gets the number of pieces the given player DON'T have
	 * in either his home or his goal
	 * @param player The player to check
	 * @return number of pieces in play
	 */
	private int nrOfPlayedPieces(int player) {
		
		System.err.println("nrOfPlayedPieces: START");
		int homeOrDone = 0;
		
		for(int pi = 0; pi < PIECES; pi++) {
			int pos = getPosition(activePlayer, pi);
			
			if(pos == GOAL || pos == 0) {
				homeOrDone++;
			}
		}
		System.err.println("return: " + (PIECES - homeOrDone));
		System.err.println("nrOfPlayedPieces: START");
		return (PIECES - homeOrDone);
	}
}

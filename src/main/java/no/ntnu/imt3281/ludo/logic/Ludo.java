package no.ntnu.imt3281.ludo.logic;

import java.util.Random;
import java.util.Vector;

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
		int dice = 0;
		randomGenerator = new Random();
		
		dice = randomGenerator.nextInt(5) + 1;
		
		checkNrOfThrows();
		
		alertThrowDice(dice);
		return dice;
	}
	

	/**
	 * Clientside dicethrow, returns the given value
	 * @param value the value the throw should result in
	 * @return the given value
	 */
	public int throwDice(int value) {
		alertThrowDice(new DiceEvent(this, value, activePlayer));
		dice = value;
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
	
	
	private boolean canMove(int player, int piece, int from, int to) {			// TODO
		boolean movable = false;
		
		if(allHome(activePlayer) && dice == 6) {	// Om alle er hjemme må vi ha 6 for å flytte
			movable = true;
		}
		
		while(block)
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
		
		int pieceindex = -1;						// Trengs for å garantere at bare
		int i = 0;									// en og første brikke flyttes
		
		while ( i < PIECES && pieceindex == -1) {	// går igjennom alle brikkene frem til
			if (getPosition(player, i) == from) { 	// første brikke som er på denne ruten
				pieceindex = i;
			}
			i++;
		}
		if (pieceindex != -1) {								// Hvis den fant brikke
			if(canMove(player, pieceindex, from, to) ) {	// Hvis den kan flytte
							
				playerPieces[player][pieceindex] = to;
				
				alertPieces(new PieceEvent("Piece moved", activePlayer, pieceindex, from, to));
				nextPlayer();
				alertPlayers(new PlayerEvent("Next player", activePlayer, PlayerEvent.PLAYING));
				movable = true;
			}
			else movable = false;	// blokkert / kan ikkje flytte
		}
		else movable = false;		// har ingen brikke på den pos.
		
		return movable;
	}
	
	/**
	 * 
	 * @param player
	 * @return
	 */

	// TODO Dette er samme som all home, + all runway
	private boolean needASixToGetStarted(int player) {		
		int nrOfPiecesInStart = 0;
		int nrOfPiecesInRunway = 0;
		for(int pi=0; pi < PIECES; pi++) {
			if (getPosition(player, pi) == 0) nrOfPiecesInStart++;
			if (getPosition(player, pi) > 54) nrOfPiecesInRunway++;
		}
		if(nrOfPiecesInStart == 4) return true;
		if(nrOfPiecesInRunway == 4) return true;		 // FIXME blir dette riktig??
		else return false;
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
		
		// gets the actual positions on the board
		int fromGridPos = userGridToLudoBoardGrid(player, from);
		int toGridPos = userGridToLudoBoardGrid(player, to);
		
		// finds the number of tiles btween "to" and "from"
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
			// if(pl == player)  // --> sett inn denne om egne tårn ikke blokker
			for(int pi = 0; pi < PIECES; pi++) {
				int pieceGridPos = userGridToLudoBoardGrid(pl, getPosition(pl, pi));
				
				if(pieceGridPos > fromGridPos && pieceGridPos <= toGridPos) {
					// should get positions relative to "from" as index
					// ex: pieceGridPos = 21, fromGridPos = 20
					// index = 21 - 20 - 1 = 0
					int index = pieceGridPos - fromGridPos - 1; 
					pieceAtPos[pl][index]++;
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
			}
		}
		
		/*
		if ( twr[0] == twr[1] || twr[0] == twr[2] || twr[0] == twr[3] ||
			 twr[1] == twr[2] || twr[1] == twr[3] || twr[2] == twr[3])
			return true;
		 */
		
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
		if(getWinner() > 0) {
			res = "Finished";
		}
		
		return res;
	}
	
	
	/**
	 * Checks if someone has won the game
	 * @return the winner of the game
	 */
	public int getWinner() {
		int i;
		int j;
		
		for(i = 0; i < players.size(); i++) {
			for(j = 0; j < 4; j++) {
				// TODO: check if 59 is correct pos
				if(getPosition(i, j) == 59) return i;
				else return -1;
			}
		}
		// return j;
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
		// changes state of prev player
		alertPlayers(new PlayerEvent("nextPlayer", activePlayer, PlayerEvent.WAITING));
		
		// reset the throwcount for a new player
		nrOfThrows = 0;
		
		if(activePlayer == MAX_PLAYERS - 1) {
			activePlayer = 0;
		}
		else activePlayer++;
		
		// changes state of next player
		alertPlayers(new PlayerEvent("nextPlayer", activePlayer, PlayerEvent.PLAYING));
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
		boolean allBlocked = true;
		
		// perfomes a AND with all the pieces. If one of these can move
		// checkBlockAt() will return false and the AND will make
		// allBlocked be false.
		for(int i = 0; i < PIECES; i++) {
			allBlocked = allBlocked && checkBlockAt(player, i, from, to);
		}
		
		return allBlocked;
	}
	
	
	/**
	 * check if there are Unfortunate Opponent on the position player moves to
	 * @param player that moves
	 * @param to position
	 */
	private void checkUnfortunateOpponent(int player, int to) {
		int toBlack = userGridToLudoBoardGrid(player, to);
		for(int pl = 0; pl<=MAX_PLAYERS; pl++) {		
			for(int pi=0; pi <= 3; pi++) {
				int posCol = getPosition(pl, pi); 
				int posBlack = userGridToLudoBoardGrid(pl, posCol); 
				if(posBlack == toBlack) {
					new PieceEvent(this, pl, pi, posCol, 0);
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
				if(getPosition(pl, pi) == 59) won = true;
			}
		}
		
		if(won) {
			for(PlayerListener playerListener : playerListeners) {
				playerListener.playerStateChanged(
						new PlayerEvent("Ludo#checkWinner()", activePlayer, PlayerEvent.WON)
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
	private void checkNrOfThrows() {
		if(nrOfThrows + 1 == 3) {
			nextPlayer();
		}
		else nrOfThrows++;
	}
}

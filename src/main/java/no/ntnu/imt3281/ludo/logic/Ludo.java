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
	/** Number of shared/common squares on the board */
	private static final int COMMON_GRID_COUNT = 54;
	
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
	public int userGridToLudoBoardGrid(int player, int numbCol){
		return userGridToPlayerGrid[player][numbCol];
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
		alertThrowDice(dice);
		return dice;
	}
	

	/**
	 * Clientside dicethrow, returns the given value
	 * @param value the value the throw should result in
	 * @return the given value
	 */
	public int throwDice(int value) {
		alertThrowDice(value);
		dice = value;
		return value;
	}
	
	
	/**
	 * sends the DiceEvent to all registered DiceListners
	 * @param value of the dicethrow
	 */
	private void alertThrowDice(int value) {
		for(DiceListener dl : diceListeners) {
			dl.diceThrown(new DiceEvent("Server", value, activePlayer));
		}
	}
	
	/**
	 * Checks if the current move: 
	 * Is from correct position
	 * Is equal to dice
	 * Is allowed from start (must have a six to be allowed to move a piece)
	 * is not in conflict with a tower
	 * @return false if can't move,  true otherwise 
	 */
	
	private boolean canMove(int player, int from, int to) {					// TODO
		boolean canMove = false;
		
		if(from == playerPieces[player][from]) {		// Sjekker om brikken er der
			if(dice == from-to) {						// Sjekker om diff er lig dice
				if(from == 0 && dice ==6) {
					to = 1; 		// TODO sjekk om dette holder, er to allerede 1?
					canMove = true; // må ha 6 om det er fra start
				}
				if(!checkBlockAt(player, from, to)) canMove = true;
				else canMove = false;
				if(to> 59) 	canMove = false;				 // Må ha akkurat verdi i mål
			}
		}
		return canMove;
	}
	
	/**
	 * Handles all movements of pieces in the game 
	 * @param player the player whose piece we want to move 
	 * @param from position to move from (relative to the player)
	 * @param to position to move from (relative to he player)
	 * 
	 * @return true if the piece could move, false otherwise
	 */
/*
	private boolean movePiece(int player, int from, int to) {
		// FIXME
		if (from == 0) {
			if(dice == 6) {
				int i = 0;
				boolean moved = false,
				while(!moved && playerPieces[player][i] == 0) {
					playerPieces[player][i] = to;
					i++;
				}
			}
*/
	
	public boolean movePiece(int player, int from, int to) {	//FIXME
		
		int pieceindex = 0;					// Trengs for å garantere at bare
											// en brikke flyttes
		if(canMove(player, from, to)) {
			
			for (int i = 0; i < PIECES; i++) {	// går igjennom alle brikkene til
				if (playerPieces[player][i] == from) // en spiller
					pieceindex = i;
			}
			alertPieces(new PieceEvent("Piece moved", activePlayer, pieceindex, from, to));
			
			playerPieces[player][pieceindex] = to;
			
			nextPlayer();
			alertPlayers(new PlayerEvent("Next player", activePlayer, PlayerEvent.PLAYING));
			
			return true;
		}
		else return false;	
	}
	
	

	
	/**
	 * Checks if there are any blockades for specific piece in distance it's about to move
	 * Iterate through all players, 
	 * If the pos is in the actual area, saves the position for each pice in p[]
	 * For each player, check if one of the pos is equal to each other
	 * 
	 */
	private boolean checkBlockAt(int player, int from, int to) { 
		
		int fromBlack = userGridToLudoBoardGrid(player, from);
		int toBlack = userGridToLudoBoardGrid(player, to);
		for(int pl = 0; pl<=MAX_PLAYERS; pl++) {
			int twr[] =  new int [4];			// lagrer alle posisjonene for hver spiller
			for(int pi=0; pi <= 3; pi++) {
				int posCol = playerPieces[pl][pi]; 
				int posBlack = userGridToLudoBoardGrid(pl, posCol); 
				if(posBlack >fromBlack && posBlack<= toBlack) 
					twr[pi] =posBlack;
					
			}
			if(twr[0]==twr[1] || twr[0]==twr[2] || twr[0]==twr[3] || twr[1]==twr[2] ||
					twr[1]==twr[3] || twr[2]==twr[3]) {
				return true;
			}
		}
		return false;
	}
	

	/**
	 * Gets the current state of the game
	 * @return gamestate
	 */
	public String getStatus() {
		if(activePlayers() == 0) {
			return "Created";
		}
		else if(activePlayers() >= 1){
			if(dice == 0) {
				return "Initiated";
			}
			else { 
				return "Started";
			}
		}
		else {
			if(getWinner() > 0) {
				return "Finished";
			}
			else return null;
		}
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
				if(playerPieces[i][j] == 59) return i;
				else return -1;
			}
		}
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
	 * Converts the userGrid to the playerGrid. (Relativ til actual)
	 * @return the relativ grid
	 */
	private int[][] getUserGridToPlayGrid(){
		// userGridToPlayerGrid[player][numbCol]
		// fra svart til hvilket av de fargede tallene??
		return 0;
	}
	
	/**
	 * Checks if all the pieces are home?
	 * @return true if all pieces are home, false otherwise
	 * piecesInStart counts down for each pice in start
	 */
	private boolean allHome() {
		int piecesInHome = 4;
		for(int piece = 0; piece < PIECES; piece++) {
			if(playerPieces[activePlayer][piece] != 0) {
				--piecesInHome;
			}
		}
		if(piecesInHome==4) return true;
		else return false;
	}
	
	
	/**
	 * Gives the turn to the next player in the queue
	 */
	private void nextPlayer() {
		if(activePlayer == MAX_PLAYERS - 1) {
			activePlayer = 0;
		}
		else activePlayer++;
	}
	
	
	
	/**
	 * 
	 * @param player
	 * @param piece
	 * @param to
	 * @param from
	 * @return
	 */
	private boolean blocked(int player, int piece, int to, int from) {
		return false;
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
				int posCol = playerPieces[pl][pi]; 
				int posBlack = userGridToLudoBoardGrid(pl, posCol); 
				if(posBlack == toBlack) {
					new PieceEvent("movePieceBackToStart", pl,pi, posCol, 0);
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
				if(playerPieces[pl][pi] == 59) won = true;
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
		userGridToPlayerGrid = new int[MAX_PLAYERS][COMMON_GRID_COUNT];
		
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
	 * Set up each position in userGridToPlayerGrid
	 * From color int to black int. 
	 * @param player 
	 * @param start value for player
	 * @param startEnd value for each players runway
	 */
	private void setUpPos(int player, int start, int startEnd) {
		int blackInt = start;							// Setter startverdien til den svarte
		for(int colInt = 1; colInt < COMMON_GRID_COUNT; colInt++) {	// Går rundt hele ytre bane
			userGridToPlayerGrid[player][colInt] = blackInt;
			if(blackInt == 67) blackInt=15;				// Spesialhådterer tallskifte
			blackInt++;
		}

		blackInt=startEnd;  							//Setter startverdien på oppløpet
		for(int colInt = COMMON_GRID_COUNT; colInt <= 6; colInt++) {	// Går opp hele oppløpet
			userGridToPlayerGrid[player][colInt]= blackInt;
			blackInt++;
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
			pieceListener.piceMoved(event);
		}
	}
}

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
	
	// make a type for this
	private int[][] userGridToPlayerGrid =
		{
			/* Red Player    */ {  0, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34,
							      35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54,
							      55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 16, 68, 69, 70, 71, 72, 73},
			
			/* Blue Player   */ {  4, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
		    				      48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67,
							      16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 74, 75, 76, 77, 78, 79},
			
			/* Yellow Player */ {  8, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60,
							      61, 62, 63, 64, 65, 66, 67, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
							      29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 80, 81, 82, 83, 84, 85},
			
			/* Green Player */  { 12, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 16, 17, 18, 19, 20, 21,
							      22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41,
							      42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 86, 87, 88, 89, 90, 91}
		};
		
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
		
		// ingen ting å catche her
		if(MIN_PLAYERS > nrOfPlayers() && MAX_PLAYERS < nrOfPlayers()){
			players.clear();
			throw new NotEnoughPlayersException(
					  "Ludo#Ludo(String, String, String, String):"
					+ "The number of players must be more than 2");
		}
		else {
			//throw new NotEnoughPlayersException("No exception should be thrown");
		}
	}
	
	/**
	 * Converts the given coordinates to the relative
	 * board coordinates
	 * 
	 * @param player colour
	 * @param players number
	 * @return The translated coord as int (black number)
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
		
		if(players.size() != 0) {
			for (int i = 0; i < MAX_PLAYERS; i++) {
				if(players.get(i) != null) ps++;
			}
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
		for(int i = 0; i < MAX_PLAYERS; i++) {
			if(!players.get(i).startsWith("Inactive: ")) ap++;
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
		if(player < 0 || player < nrOfPlayers() - 1){
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
		if(nrOfPlayers() > MAX_PLAYERS) {
			throw new NoRoomForMorePlayersException(
					  "Ludo#addPlayer(String): Game already "
					  + "has 4 players");
			// a WILD return appeared
			return;
		}
		else {
			for(String p : players) {
				if(p == player) throw new IllegalPlayerNameException(
										  "Ludo#addPlayer(String): Name: "
										+ player + ", is already taken!");
			}
		}

		// TODO: sjekk om navnet starter med 4x*
		
		// TODO: bør legges i egen funksjon
		// om vi trenger mer funksjonalitet her
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
	 * Checks if the current player must have a six 
	 * to be allowed to move a piece
	 * 
	 * @return false if all pieces in home and no six on dice, true otherwise 
	 */
	
	private boolean canMove(int from, int to) {					// TODO
		
		if(from == 0) {
			if(dice !=6) {
				if(checkBlocAt(to)) 
			}
		}
		if(checkBlocAt()) 
				 return false;
		
		return true;
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
																	
		if(canMove(from, to)) {
			// TODO, hvilken brikke skal flyttes
			
			for 
			
			playerPieces[player][0] = to;	// pos. må vel mappes også 
			
			return true;
		}
		else return false;	
	}
	
	

	
	/**
	 * Checks if there are any blockades for specific piece in distance it's about to move
	 */
	private void checkBlocAt(int player, int piece, int from, int to) { 
		int fromBlack = userGridToLudoBoardGrid(player, from);
		int toBlack = userGridToLudoBoardGrid(player, to);
		for(int pl = 0; pl<=MAX_PLAYERS; pl++) {
			for(int pi=0; pi <= 3; pi++) {
				int posCol = playerPieces[pl][pi]; 
				int posBlack = userGridToLudoBoardGrid(pl, posCol); 
				
			}
		}
		
	}
	
	
	/**
	 * Gets the current state of the game
	 * @return gamestate
	 */
	public String getStatus() {
		if(activePlayers() == 0) {
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
	 * Probably solve some kind of random shenadigans
	 * @param player1 player one in question
	 * @param player2 player two in question
	 */
	private void checkUnfortunateOpponent(int player1, int player2) {
		
	}
	
	/**
	 * Checks if someone have won the game
	 */
	private void checkWinner() {
		
	}
	
	/**
	 * Checks if enough actual players are in game
	 * @return true if we have at least 2, false otherwise
	 */
	private boolean enoughPlayers() {
		int ps = 0;
		for (int i = 0; i < MAX_PLAYERS; i++) {
			if(players.get(i) != null) ps++;
		}
		
		if(ps >= MIN_PLAYERS) return true;
		else return false;
	}
	
	
	/**
	 * Sets up the common stuff for the constructors. Such as
	 * the playerPieces and empty vectors
	 */
	private void setUpGame(){
		playerPieces = new int[4][4];
		players = new Vector<>();
		
		for(int player = 0; player < MAX_PLAYERS; player++) {
			for(int piece = 0; piece < PIECES; piece++) {
				playerPieces[player][piece] = 0;
				
				if(player == RED) setUpPos(RED, 16, 68);
				if(player == BLUE) setUpPos(BLUE, 29, 74);
				if(player == GREEN) setUpPos(GREEN, 55, 86);
				if(player == YELLOW) setUpPos(YELLOW, 42, 80);
			}
		}
		
		activePlayer = RED;
		dice = 0;
		
		diceListeners = new Vector<>();
		pieceListeners = new Vector<>();
		playerListeners = new Vector<>();
	}
	
	private void setUpPos(int player, int start, int startEnd) {
		int colInt = 1;
		for(int i = start; i <= 52; i++) {
			userGridToPlayerGrid[player][colInt]= i;
			if(colInt == 67) 
			colInt++;
		}
		userGridToPlayerGrid[player][colInt]= mid;
		
		for(int i = startEnd; i <= 6; i++) {
			userGridToPlayerGrid[player][colInt]= i;
			colInt++;
		}
	}
}

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
	
	/** A 2D int array to hold the different players pieces*/
	private int[][] playerPieces;
	
	// make a type for this
	private [][] userGridToPlayerGrid;
	
	/** A Vector with the different DiceListners */
	private Vector<DiceListner> diceListners;
	
	/** A Vector with the different PieceListners */
	private Vector<PieceListner> pieceListners;
	
	/** A Vector with the diffenet PlayerListners */
	private Vector<PlayerListner> playerListners;
	
	/**
	 * Default Constructor for the Ludo calss
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
		players.add(p1);
		players.add(p2);
		players.add(p3);
		players.add(p4);
		
		if(MIN_PLAYERS <= nrOfPlayers() && MAX_PLAYERS >= nrOfPlayers()){
			players.clear();
			throw new NotEnoughPlayersException(
					  "Ludo#Ludo(String, String, String, String):"
					+ "The number of players must be more than 2");
		}
		else {
			throw new NotEnoughPlayersException("No exception should be thrown");
		}
		
		setUpGame();
	}
	
	/**
	 * Converts the given coordinates to the relative
	 * board coordinates
	 * 
	 * @param x coordinate
	 * @param y coordinate
	 * @return The translated coord as int
	 */
	public int userGridToLudoBoardGrid(int x, int y){
		
		
		return 0;
	}
	
	/**
	 * Gives you the number of players in
	 * this ludogame
	 * @return number of players as int
	 */
	public int nrOfPlayers(){
		return players.size();
	}
	
	/**
	 * Returns the number og active players
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
			//TODO: feilmelding
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
		for(DiceListner dl : diceListners) {
			dl.diceThrown(new DiceEvent("Server", value, activePlayer));
		}
	}
	

	private boolean canMove() {					// TODO
		
		if(needASixToGetStarted()) {
			// if(checkBlocAt()) 
				 return false;
		}
		
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
	private boolean movePiece(int player, int from, int to) {
		// FIXME
		if(canMove()) {
			// TODO, hvilken brikke skal flyttes
			playerPieces[player][0] = to;
			// ??? playerPieces[player][x] = to;
		}
	}
	/**
	 * probably check on dice = 6
	 */
	private boolean needASixToGetStarted() {
		if(allHome() && dice !=6)return false;
		else return true;
	}
	
	/*
	 * 
	 */
	private void checkBlocAt(int a, int b, int c, int d) { 
		
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
		for(int i = 0; i < players.size(); i++) {
			for(int j = 0; j < 4; j++) {
				// TODO: check if 59 is correct pos
				if(playerPieces[i][j] == 59);
			}
		}
	}
	
	/**
	 * Adds a DiceListner to the game
	 * @param diceListner to be added
	 */
	public void addDiceListner(DiceListner diceListner) {
		diceListners.add(diceListner);
	}
	
	/**
	 * Adds PlayerListner to the game
	 * @param playerListner to be added
	 */
	public void addPlayerListner(PlayerListner playerListner) {
		playerListners.add(playerListner);
	}
	
	/**
	 * Adds a PieceListner to the game
	 * @param pieceListner to be added
	 */
	public void addPieceListner(PieceListner pieceListner) {
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
	
	
	
	
	private boolean blocked(int player, int piece, int to, int from) {
		
	}
	
	private boolean checkBlockAt(int player, int piece, int a, int b) {
		
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

	/*	mulig unødvendig funksjon
	
	private boolean enoughPlayers() {
		int ps;
		for (int i = 0; i < MAX_PLAYERS; i++) {
			if(players.get(i) != null) ps++;
		}
		
		if(ps >= MIN_PLAYERS) return true;
		else return false;
	}
 		*/
	
	
	/**
	 * Sets up the common stuff for the constructors. Such as
	 * the playerPieces and empty vectors
	 */
	private void setUpGame(){
		for(int player = 0; player < MAX_PLAYERS; player++) {
			for(int piece = 0; piece < PIECES; piece++) {
				playerPieces[player][piece] = 0;
			}
		}
		
		activePlayer = RED;
		
		diceListners = new Vector<>();
		pieceListners = new Vector<>();
		playerListners = new Vector<>();
	}
}

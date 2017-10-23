package no.ntnu.imt3281.ludo.logic;

import java.util.Random;
import java.util.Vector;

/**
 * Main Class that represents the
 * actual game
 */
public class Ludo {
	public static final int RED = 0;
	public static final int BLUE = 1;
	public static final int YELLOW = 2;
	public static final int GREEN = 3;
	
	
	private Vector<String> players;
	private int activePlayer;
	private int dice;
	
	private Random randomGenerator;
	
	private int[][] playerPieces;
	
	// Missing type in UML -> assume int
	// make a type for this
	private [][] userGridToPlayerGrid;
	
	private Vector<DiceListner> diceListners;
	private Vector<PieceListner> pieceListners;
	private Vector<PlayerListner> playerListners;
	
	/**
	 * Default Constructor for the Ludo calss
	 */
	public Ludo(){
		
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
		if(p1 != null) players.add(p1);
		if(p2 != null) players.add(p2);
		if(p3 != null) players.add(p3);
		if(p4 != null) players.add(p4);
		
		if(players.size() < 2){
			players.clear();
			throw new NotEnoughPlayersException(
					  "Ludo#Ludo(String, String, String, String):"
					+ "The number of players must be more than 2");
		}
		else{
			// TODO: her må vi gjøre resten av initen
			// av objectet.
		}
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
	 * in this ludogame
	 * @return the number of active players
	 */
	public int activePlayers(){
		// TODO: lage en sjekk om en spiller er
		// aktiv. mest sannsynlig loope gjennom
		// playervector og pinge elns
		return 0;
	}
	
	/**
	 * Retrieves the name of a player at the given
	 * index in the playervector
	 * 
	 * @param player Index of the player
	 * @return the players name as String
	 */
	public String getPlayerName(int player){
		// allowede numbers = 0, 1, 2, 3
		if(player < 0 || player < nrOfPlayers() - 1){
			return players.get(player);
		}
		else{
			//TODO: feilmelding
		}
	}
	
	
	public void addPlayer(String player) throws IllegalPlayerNameException,
												NoRoomForMorePlayersException {
		
		// TODO: sjekk om vi har 4 fra før, kast exception
		// TODO: sjekk om navnet starter med 4x*
		
		// TODO: bør legges i egen funksjon
		// om vi trenger mer funksjonalitet her
		for(String p : players){
			if(p == player){
				throw new IllegalPlayerNameException(
						  "Ludo#addPlayer(String): Can't have the name: "
						+ player);
			}
		}
	}
	
	/**
	 * Removes the given player from the game
	 * @param player the player as string
	 * @throws NoSuchPlayerException
	 */
	public void removePlayer(String player) throws NoSuchPlayerException{
		boolean found = false;
		int i = 0;
		
		while(!found && i < players.size()) {
			if(players.get(i++) == player) {
				players.remove(i);
				found = true;
			}
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
		// TODO: FIXME
		return 0;
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
		if (from == 0) {
			if(dice == 6) {
				playerPieces[player][0] = to;
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
}

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
			throw new NotEnoughPlayersException();
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
				throw new IllegalPlayerNameException();
			}
		}
	}
	
	/**
	 * Removes the given player from the game
	 * @param player the player as string
	 * @throws NoSuchPlayerException
	 */
	public void removePlayer(String player) throws NoSuchPlayerException{
		
	}
	
	
	/**
	 * Gets the position of the given player's given peice,
	 * relative to him
	 * @param player the player whose piece we want
	 * @param piece we want to find
	 * @return the position, relative to the player, as int
	 */
	public int getPosition(int player, int piece){
		return 0;
	}
	
	
	
}

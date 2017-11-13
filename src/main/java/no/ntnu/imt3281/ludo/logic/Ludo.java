package no.ntnu.imt3281.ludo.logic;

import java.util.Random;
import java.util.Vector;

import javax.jws.soap.SOAPBinding;

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
	/** A constant of 'Inactive: ' used in all cases where
	 * we need to check for inactivity */
	private static final String INACTIVE = "Inactive: "; 
	
	
	/** Number of subsequent throws*/
	private int nrOfThrows = 0;
	
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
	 * @throws NotEnoughPlayersException if the number of players is less than 2
	 */
	public Ludo(String p1, String p2, String p3, String p4) throws NotEnoughPlayersException {
		setUpGame();
		
		players.add(RED, p1);
		players.add(BLUE, p2);
		players.add(YELLOW, p3);
		players.add(GREEN, p4);
		
		if(nrOfPlayers() >= MIN_PLAYERS && nrOfPlayers() <= MAX_PLAYERS) {
			/* Might want to invert this statement */
		
		} else {
			players.clear();
			throw new NotEnoughPlayersException(
					  "Ludo#Ludo(String, String, String, String):"
					+ "The number of players must be more than 2!\n");
		}
	}

	
	/**
	 * Converts a given relative position for the given player
	 * to the corresponding gridPosition
	 * @param player The player relative to
	 * @param userPosition The relative position to convert
	 * 
	 * @return The gridPosition of the given relative position
	 */
	public int userGridToLudoBoardGrid(int player, int userPosition){
		int gridPosition = 0;
		
		if(userPosition == 0) {
			switch(player) {
				case RED:
					gridPosition = 0;
					break;
					
				case BLUE:
					gridPosition = 4;
					break;
					
				case YELLOW:
					gridPosition = 8;
					break;
					
				case GREEN:
					gridPosition = 12;
					break;
					
				default:
					break;
			}
			
		} else {
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
		
		for (int i = 0; i < MAX_PLAYERS; i++) {
			if(players.get(i) != null) {
				ps++;
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
			if(isActive(i)) {
				ap++;
			}
		}
		return ap;
	}
	
	/**
	 * Retrieves the name of a player at the given
	 * index in the playervector
	 * 
	 * @param player Index of the player
	 * @return The players name as String
	 */
	public String getPlayerName(int player){
		// allowed numbers = 0, 1, 2, 3
		if(player >= 0 && player <= nrOfPlayers()){
			return players.get(player);
		} else {
			return null;
		}
	}
	
	/**
	 * Tries to add the given player to the game
	 * @param player The name of a player to be added
	 * @throws IllegalPlayerNameException 
	 *		If the player has the same name as a player
	 *		already in the game or if the names starts
	 *		with 4x'*'
	 *    
	 * @throws NoRoomForMorePlayersException
	 * 		If there are 4 players in the game already
	 */
	public void addPlayer(String player) throws IllegalPlayerNameException,
												NoRoomForMorePlayersException {
		// if we have 4 or more players 
		if(nrOfPlayers() >= MAX_PLAYERS) {
			throw new NoRoomForMorePlayersException(
					  "Ludo#addPlayer(String): Game already "
					  + "has 4 players\n");
		} else {	// check the names
			for(String p : players) {
				if(p == player) {
					throw new IllegalPlayerNameException(
								  "Ludo#addPlayer(String): Name: "
								+ player + ", is already taken!\n"
										+ "");
				}
			}
			if(player.startsWith("****")) {
				throw new IllegalPlayerNameException(
								  "Ludo#addPlayer(String): Can't"
								+ " start with ****!");
			} else {
				int i = nrOfPlayers();	// start at the next valid (0 - 3)
				boolean added = false;
				
				// As long as there is space, try to add a the given
				// player to the players vector. This is filled with
				// nulls and thus we need to remove these first, then
				// insert the given player at the position we found
				// open (as null)
				while(nrOfPlayers() < MAX_PLAYERS && i < MAX_PLAYERS && !added) {
					players.remove(i);
					players.add(i, player);
					added = true;
					i++;
				} // while
			} // if **
		} // if players > 4
	} // end function
	
	/**
	 * "Removes" the given player from the game. Marks them as inactive
	 * @param player A player that is inactive
	 * @throws NoSuchPlayerException If the given player isn't in this game
	 */
	public void removePlayer(String player) throws NoSuchPlayerException{
		boolean found = false;
		int i = 0;
		
		/* Loops through the playersvector and looks for
		 * the given player. We stop if we find him/her
		 * or if we have looked through all the players
		 */
		while(!found && i < MAX_PLAYERS) {
			
			/* If we find a suitable player, construct
			 * a new StringBuilder with the inactive
			 * prefix, then, while removing the player
			 * from the playersvector, append the players
			 * name to the StringBuilder.
			 * 
			 * Then we add the player we modified back into
			 * the playervector where we found him/her
			 */
			if(players.get(i) == player) {
				StringBuilder sb = new StringBuilder(INACTIVE);
				sb.append(players.remove(i));
				players.add(i, sb.toString());
				found = true;
				
				for(int pi = 0; pi < PIECES; pi++) {
					playerPieces[i][pi] = 0;
				}
				
				// alerts all other players that a player
				// has left the game / has come inactive
				alertPlayers(new PlayerEvent(this, activePlayer, PlayerEvent.LEFTGAME));
				
				// We need to give the turn to the next player
				nextPlayer();
			}
			
			// progress in the playervector
			i++;
		} // while
		
		// if no player with the given name where found
		// we throw a new NoSuchPlayerException
		if(!found) {
			throw new NoSuchPlayerException(
					  "Ludo#removePlayer(String):"
					+ "The player" + player + " was not found"); 
		}
	} // removePlayer end
	
	
	/**
	 * Gets the position of the given player's given piece,
	 * relative to him
	 * @param player The player whose piece we want
	 * @param piece The index of the piece we want to find
	 * @return The position, relative to the player, as integer
	 */
	public int getPosition(int player, int piece){
		return playerPieces[player][piece];
	}
	

	/**
	 * Gets the current active player (RED, GREEN, YELLOW, BLUE) 
	 * @return The current active player
	 */
	public int activePlayer() {
		return this.activePlayer;
	}
	
	/**
	 * Throws a D6
	 * @return Value between 1 and 6 (inclusive)
	 */
	public int throwDice() {
		// TODO: finn ut om denne må se ut som
		// clientside throwdice
		
		int locDice = 0;
		randomGenerator = new Random();
		
		locDice = randomGenerator.nextInt(5) + 1;
		
		alertThrowDice(new DiceEvent(this, activePlayer, locDice));
		return locDice;
	}
	

	/**
	 * Clientside dicethrow, returns the given value
	 * @param value A integer representing the value
	 * of a D6 dice
	 * @return The given value (as this should only
	 * simulate a dicethrow)
	 */
	public int throwDice(int value) {
		// alert all players that a dice is thrown
		alertThrowDice(new DiceEvent(this, activePlayer, value));
		dice = value;			// updates the current value of dice
		
		/* If a player cannot move one or more of his/her
		 * pieces we need to check if he either:
		 * - Needs to get out of home
		 * - Has a piece thats locked
		 * 
		 * Based on this we allow the player 1 or 3 throws
		 * 
		 * In both cases we want to give the turn to the next
		 * player (Couse the player has used up all his/her
		 * tries)
		 * 
		 * The number of throws is reset to 0 when 'nextPlayer'
		 * is called, but since this function ALWAYS increments the 
		 * number of throws, we need to decrement it in the special
		 * cases where the player cannot move and is forced to give
		 * up his/her turn
		 */
		if(!canMove()) {
			if(nrOfPlayedPieces(activePlayer) > 0) {
				if(nrOfThrows + 1 == 1) {
					nextPlayer();
					nrOfThrows--;
				}
			} else if(nrOfThrows + 1 == 3) {
				nextPlayer();
				nrOfThrows--;
			}
		}
		
		// if a player CAN move, increment the number of throws
		nrOfThrows++;
		
		/* if this throw was his 3.
		 * AND he got 6 on the dice
		 * AND he has at least 1 piece played
		 * 
		 * THEN the player immediately gives up his turn
		 * without beeing able to move his piece 
		 */
		if(dice == 6 && nrOfThrows == 3 && nrOfPlayedPieces(activePlayer) > 0) {
			nextPlayer();
		}

		// TODO: we might want to reset the value to 0
		// if a player isn't allowed to move so we can
		// check this at the client
		return value;
	}

	
	/**
	 * Checks if the active player has at least 1 piece
	 * he/she can move.
	 * <br>
	 * Checks:
	 * <ul>
	 *   <li>Need a 6 on the dice because all pieces are either
	 *       home or in goal</li>
	 *   <li>A piece on the board doesn't move past the goal.
	 *       This ensures that a player can't move his piece
	 *       unless the exact number of tiles to the goal is
	 *       equal to the value of his dice</li>
	 *   <li>All pieces are blocked by towers</li>
	 * </ul>
	 * 
	 * @return True if there is 1 piece that can be moved
	 */
	private boolean canMove() {			
		boolean movable = false;
		
		/* checks if all of the players pieces is either
		 * home or in goal. In this case the player
		 * NEEDS 6 on the dice
		 */
		if(nrOfPlayedPieces(activePlayer) == 0 && dice == 6) {
			movable = true;
		
		} else {
			/* Loops through all the players pieces
			 * and gets its position.
			 */
			for(int pi = 0; pi < PIECES; pi++) {
				int pos = getPosition(activePlayer, pi);

				/* As long as we are NOT moving out of
				 * home, check if the player can move
				 * in to the goal with this piece
				 * 
				 * THEN check if the player has all
				 * his pieces blocked.
				 * if NOT we can allow the player to move
				 */
				if(pos != 0) {
					if((pos + dice) <= GOAL) {	
						if(!blocked(activePlayer, pos, pos + dice)) {
							movable = true;
						} // if blocked
					} // if == goal
				} // if != 0
			} // for
		} // home
		
		return movable;
	}
	
	/**
	 * Tries to move a players piece
	 * @param player Which player is moving a piece
	 * @param from Relative position to move from
	 * @param to Relative position to move to
	 *  
	 * @return True if a piece was moved, false otherwise
	 */
	public boolean movePiece(int player, int from, int to) {	
		boolean movable = false;
		
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
			
			/* if we found the valid piece,
			 * we check if that particular piece is blocked,
			 * then, we need to have a small check if the piece
			 * has the required number on the dice to finish
			 */
			if (pieceindex != -1) {
				if(!checkBlockAt(player, pieceindex, from, to)) {	// Hvis det ikke er en blokkade
					if((from + dice) <= GOAL) {						// Hvis mål nås akkurat. 
						playerPieces[player][pieceindex] = to;
						
						// tell clients that a piece is moved
						alertPieces(new PieceEvent(this, activePlayer, pieceindex, from, to));
						
						// check if someone needs to be sent
						// home at the given tile
						checkUnfortunateOpponent(player, to);
						
						// check if we have a winner
						checkWinner();
						
						// give the turn to the next player
						// unless he got a 6 and isn't going
						// out of home
						if(from == 0 && dice == 6) {
							nextPlayer();
						}
						if(dice != 6) {
							nextPlayer();
						}
						
						// WE CAN MOVE
						movable = true;
					} // if goal
				} // check tower
			} // found piece
		} // we can move
		
		return movable;
	} // movePiece end
	
	
	/**
	 * Checks if there is a tower in the way for this players
	 * given piece between the two relative positions "to" and "from"
	 * 
	 * @param player The player whose piece to move
	 * @param piece The index of the players piece to move
	 * @param from Relative position to move from
	 * @param to Relative position to move to
	 * 
	 * @return true if no towers were found, false otherwise
	 */
	private boolean checkBlockAt(int player, int piece, int from, int to) {
		
		// gets the actual positions on the board
		int fromGridPos = userGridToLudoBoardGrid(player, from);
		int toGridPos = userGridToLudoBoardGrid(player, to);
		
		// finds the number of tiles between "to" and "from"
		int dist = to - from;
		
		// holds how many pieces one player has on
		// one of the tiles between "to" and "from"
		int pieceAtPos[][] = new int[4][dist];
		
		// if we found a tower
		boolean blockade = false;				
		
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
					
					if(from == 0) {
						if(pieceGridPos == toGridPos) {
							pieceAtPos[pl][0]++;
						}
					
					} else if(pieceGridPos > fromGridPos && pieceGridPos <= toGridPos) {
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
		// loops untill we find a tower or we've seen
		// all tiles between "to" and "from"
		while(!blockade && i < dist) {
			for(int pl = 0; pl < MAX_PLAYERS; pl++) {
				// we hava a tower if a player have 2 or more
				// pieces on one tile
				if(pieceAtPos[pl][i] >= 2) {
					blockade = true;
				}
			} // for
			
			// progress through the tiles
			i++;
		} // while
		
		return blockade;
	} // checkBlockAt end
	

	/**
	 * Gets the current state of the game
	 * @return The gamestate
	 */
	public String getStatus() {
		String res = null;
		
		// if we have 0 players in the game: CREATED
		if(nrOfPlayers() == 0) {
			res = "Created";
		
		} else if(activePlayers() >= 1) {	// if we have one or more players
			
			// if no dice is thrown: INITIATED
			if(dice == 0) {
				res = "Initiated";
			
			} else {					// the game is started (couse a dice is thrown)					
				res = "Started";	// :STARTED
			}
		} // if
		
		// if we have a winner: FINISHED
		if(getWinner() != -1) {
			res = "Finished";
		}
		
		return res;
	}
	
	
	/**
	 * Checks if someone has won the game
	 * @return The winner of the game, or -1 
	 */
	public int getWinner() {
		
		// keeps the winner
		int winner = -1;
		int i = 0;
		
		// loops through all players pieces
		// if all (4) of them is in goal we have a winner
		for(int pl = 0; pl < MAX_PLAYERS; pl++) {
			for(int pi = 0 ; pi < PIECES; pi++) {
				if(getPosition(pl, pi) == GOAL) {
					i++;
				}
			}
			
			if(i == PIECES) {
				winner = pl;
			}
			i = 0;	// reset piece counter
		}
		
		return winner;
	}
	
	
	/**
	 * Adds a DiceListner to the game
	 * @param diceListner A DiceListener
	 */
	public void addDiceListener(DiceListener diceListner) {
		diceListeners.add(diceListner);
	}
	
	/**
	 * Adds PlayerListner to the game
	 * @param playerListner A PlayerListener
	 */
	public void addPlayerListener(PlayerListener playerListner) {
		playerListeners.add(playerListner);
	}
	
	/**
	 * Adds a PieceListner to the game
	 * @param pieceListner A PieceListener
	 */
	public void addPieceListener(PieceListener pieceListner) {
		pieceListeners.add(pieceListner);
	}
	
	
	/**
	 * Returns the array responsible for grid-convertions
	 * @return The grid
	 */
	private int[][] getUserGridToPlayGrid(){
		return userGridToPlayerGrid;
	}
	
	/**
	 * @deprecated Replaced by: {@linkplain nrOfPlayedPieces}.
	 * This returns 0 if all are home or in goal. 'allHome'
	 * is primarily used to check if a player needs 6 to play
	 * a piece, but this is also the case for all cases with 1-3
	 * pieces in either goal or home
	 * 
	 * Checks if all the players pieces are home
	 * @param player the player to check for
	 * @return true if all pieces are home, false otherwise
	 */
	@Deprecated
	private boolean allHome(int player) {
		int piecesInHome = 0;
		
		// loops through all pieces of that player and
		// checks if he's home
		for(int piece = 0; piece < PIECES; piece++) {
			if(getPosition(player, piece) == 0) {
				piecesInHome++;
			}
		}
		
		// if all pieces are in home, we are good
		return (piecesInHome ==  4);
	}
	
	
	/**
	 * Gives the turn to the next player in the queue
	 */
	private void nextPlayer() {
		boolean found = false;	// if we found a valid player
		
		// Alert everyone that the previous player
		// is done with his/her turn
		alertPlayers(new PlayerEvent(this, activePlayer, PlayerEvent.WAITING));
		
		// reset the throwcount for a new player
		nrOfThrows = 0;
		
		/* As long as no valid player is found, check
		 * if we're the last player.
		 * IF SO: reset activePlayer
		 */
		do {
			if(activePlayer == GREEN) {
				activePlayer = RED;
			
			} else {
				activePlayer++;
			}
			if(isActive(activePlayer)) {
				found = true;
			}
		} while(!found);
		
		// alert every player about whose turn it is
		alertPlayers(new PlayerEvent(this, activePlayer, PlayerEvent.PLAYING));	
	}
	
	
	
	/**
	 * Checks if any of the given players pieces
	 * is blocked in the given distance between
	 * 'from' and 'to'
	 * 
	 * @param player The player to check for
	 * @param from Relative position to move from
	 * @param to Relative position to move to
	 * 
	 * @return True if all pieces are unable to move, false otherwise
	 */
	private boolean blocked(int player, int from, int to) {
		boolean allBlocked = true; 	// is every piece blocked?
		int piece = 0; 
		
		// loops untill we find 1 piece that
		// isn't blocked or we looked at all pieces
		while(allBlocked && piece < PIECES) {
			allBlocked = checkBlockAt(player, piece, from, to);
			piece++;
		}
		
		return allBlocked;
	}
	
	
	/**
	 * Check if there are an Unfortunate Opponent on the
	 * given relative position the given player moves to
	 * 
	 * @param player The player that moves
	 * @param to The relative position moved to
	 */
	private void checkUnfortunateOpponent(int player, int to) {
		// we want to start looking from the active player
		int pl = activePlayer;
		boolean found = false;	// if we found a valid player
		
		// gets the grid position of the given piece
		int gridPos = userGridToLudoBoardGrid(player, to);
		
		// loops through all active players
		for(int i = 0; i < activePlayers(); i++) {
			
			// Skip all players that are inactive
			do {
				if(pl == GREEN) {
					pl = RED;
				
				} else {
					pl++;
				}
				
				if(isActive(pl)) {
					found = true;
				}
			} while(!found);
			
			/* As long as it isn't our self
			 * loop through all the pieces and get their grid
			 * position. If this position is the same as the
			 * one the given player moved to, the other players
			 * pieve has to be moved back to its home
			 */
			if(pl != player) {
				for(int pieces = 0; pieces < PIECES; pieces++) { 
					int pieceGridPos = userGridToLudoBoardGrid(pl, getPosition(pl, pieces));
					
					if(pieceGridPos == gridPos) {
						// alert all players that a piece is sent home
						alertPieces(new PieceEvent(this, pl, pieces, getPosition(pl, pieces), 0));
						
						// move the piece
						playerPieces[pl][pieces] = 0;
					} // if same pos
				} // for piece
			} // if same player
		} // for i (active player)
	} // checkUnfortunateOpponent end
	
	
	/**
	 * Checks if someone have won the game
	 */
	private void checkWinner() {
		// if getWinner returns something other
		// than '-1' someone has won
		if(getWinner() != -1) {
			// alert all players that the activePlayer has won
			alertPlayers(new PlayerEvent(this, activePlayer, PlayerEvent.WON));
		}
	}
	
	
	/**
	 * Sets up the common stuff for the constructors. Such as
	 * the playerPieces and empty vectors
	 */
	private void setUpGame(){
		// makes a 4 X 4 int array
		playerPieces = new int[MAX_PLAYERS][PIECES];
		
		// makes a 4 X 60 in array (1 - 59)
		userGridToPlayerGrid = new int[MAX_PLAYERS][COMMON_GRID_COUNT + 6];
		
		activePlayer = RED;
		dice = 0;
		nrOfThrows = 0;
		
		// empty vectors != nulls
		diceListeners = new Vector<>();
		pieceListeners = new Vector<>();
		playerListeners = new Vector<>();
		players = new Vector<>();
		
		// inits the players to null and
		// playerPieces to be at home
		for(int player = 0; player < MAX_PLAYERS; player++) {
			players.add(null);
			
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
		int startGridPos = start;									// Setter startverdien til den svarte
		
		for(int colInt = 1; colInt < COMMON_GRID_COUNT; colInt++) {	// Går rundt hele ytre bane
			userGridToPlayerGrid[player][colInt] = startGridPos;

			if(startGridPos == 67) {
				startGridPos = 15;									// Spesialhåndterer tallskifte fra overgang  
			}
			startGridPos++;											//  67 til 16, 15 vil bli inkrementert til 16  
		}															//    før neste iterasjon

		int runwayGridPos = runway;  								//Setter startverdien på oppløpet
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
	 * Gets the number of pieces the given player DON'T have
	 * in either his home or his goal
	 * @param player The player to check
	 * @return number of pieces in play
	 */
	private int nrOfPlayedPieces(int player) {
		
		int homeOrDone = 0;
		
		for(int pi = 0; pi < PIECES; pi++) {
			int pos = getPosition(activePlayer, pi);
			
			if(pos == GOAL || pos == 0) {
				homeOrDone++;
			}
		}
		
		return (PIECES - homeOrDone);
	}
	
	
	private boolean isActive(int player) {
		String pl = players.get(player);
		return ((pl != null) && (!pl.startsWith(INACTIVE)));
	}
}

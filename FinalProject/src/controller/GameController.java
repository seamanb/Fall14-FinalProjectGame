package controller;

import java.util.List;
import java.util.Stack;

import model.Difficulty;
import model.Map;
import model.Player;
import units.*;

/**
 * TODO Finish this
 * 
 * The controller for a game. Sends messages to map, Saves Data, Loads Data,
 * sets up players, calculate which map is needed, sends messages to the enemy
 * team factory, etc.
 * 
 * @author Brian Seaman
 *
 */
public class GameController {
	public Player player1;
	public Player player2;
	private Map map;
	private List<Unit> tempUnitList;
	private Unit currUnit;
	private Player currPlayer;
	private int turns;
	private boolean playerTurn;
	
	private int currRow;
	private int currCol;
	
	/*
	 * Will work on being able to control each unit on the map. Things 
	 * included in this are making sure units can move, attack, use items 
	 * and show results of the battle along with the user being able to 
	 * create a new game.
	 */
	
	/**
	 * TODO Work on this. Will add a createAI method soon
	 * 
	 * Constructor for one player.
	 * 
	 * @param player1
	 * @param i
	 */
	public GameController(Player player1, Difficulty i){
		this.map = new Map(i.getValue());
		this.player1 = player1;
		
		Stack<Unit> temp = new Stack<Unit>();
		
		// Put the player's units into a stack and put it into the Map
		for(Unit k: player1.getTeam()){
			k.setCanMove();
			temp.push(k);
		}
	
		// Place the players on the map
		map.addUnitsToMap(temp);
			
		currPlayer = player1;
		tempUnitList = currPlayer.allAliveUnits();
		turns = 0;
		playerTurn = true;
	}
	
	/**
	 * Set the the current unit to the unit located at this space.
	 * Will return
	 * @param row
	 * @param col
	 */
	public boolean setCurrentUnit(int row, int col){
		if(map.getUnitAt(row, col)!=null && map.getUnitAt(row, col).canMove()){
			currUnit = map.getUnitAt(row, col);
			currRow = row;
			currCol = col;
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Get method for CurrUnit
	 * 
	 * @return the currently selected unit
	 */
	public Unit getCurrentUnit(){
		return currUnit;
	}
	
	/**
	 * WARNING: May need to be deleted
	 * 
	 * Used when selecting a unit from the GUI.
	 * If the Unit is there and can move, set it to be the current.
	 * Return the unit, or null.
	 * 
	 * @param r, the row
	 * @param c, the column
	 * @return the current Unit
	 */
	public Unit getUnitOnMap(int r, int c){
		Unit temp = map.getUnitAt(r, c);
		return temp;
	}
	
	/**
	 * TODO Test and Finish
	 * 
	 * Move a selected Unit to a another space.
	 * 
	 * @param sr, the starting row
	 * @param sc, the starting column
	 * @param er, the ending row
	 * @param ec, the ending column
	 */
	public boolean move(int endingRow, int endingCol){
		
		if(currUnit != null){
			if(map.getUnitAt(currRow,currCol).canMove() && !map.isOccupied(endingRow, endingCol)){
				map.moveUnit(currRow, currCol, endingRow, endingCol);
				tempUnitList.remove(currUnit);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * TODO Check if the selected player can move.
	 */
	public boolean playerCanMove(){
		return !tempUnitList.isEmpty();
		}
	
	/**
	 * TODO 1) Check if friendly 2) Check within range
	 * 
	 * Get it to attack
	 * 
	 * 
	 * @param sr
	 * @param sc
	 * @param er
	 * @param ec
	 * @return
	 */
	public boolean attack(int targetRow, int targetCol){
		if(currUnit.canMove() && map.isOccupied(targetRow, targetCol))
			// Send attack message to map
			return true;
		else
			return false;
	}
	
	/**
	 * TODO Finish this method.
	 */
//	public boolean hasItem(){
//		return currUnit.hasItem();
//	}
	
	/**
	 * TODO Finish this method
	 * 
	 * Use the item of a selected unit. 
	 * 
	 * @param sr, the row where the player is in
	 * @param sc, column where the selected unit is
	 * @param er, where the item will be used (row)
	 * @param ec, where the item will be used (column)
	 * @return if the item was used.
	 */
	public boolean useItem(int endingRow, int endingCol){
		if(false)
			return false;
		else{
			// If attack item, use on target space
			
			// If health item, use on target space/self
			
			// If defense item, use on target space/self
			
			return true;
		}
	}
	
	/**
	 * TODO Finish this method
	 * 
	 * Checks both of the player's aliveUnits to see if all of their 
	 * units are dead. If either of them are out of units they can move,
	 * return true and end the game. Checked after every move and attack.
	 * @return if the game is over or not
	 */
	
	public boolean gameOver(){
		if(player1.allAliveUnits().isEmpty())
			//Display some kind of message telling player 2 won
			return true;
		else if (player2.allAliveUnits().isEmpty())
			// Display some kind of message telling player 1 won
			return true;
		else
			return false;
	}
	
	/**
	 * TODO Finish this.
	 * Get the stats for the selected player.
	 * @param p
	 * @return
	 */
	public Object getTeamStats(Player p){
		return p.getTeamStats();
	}
	
	/**
	 * TODO Get this working
	 * Get the selected unit's stats
	 * 
	 * @param p, the player that is asking
	 * @param u, the player's unit
	 * @return
	 */
	public Object getCurrUnitStats(Player p, Unit u){
//		return p.getUnitStats(u);
		return false;
	}
	
	/**
	 * Get the number of turns gone through in the game
	 * @return the number of turns taken in game
	 */
	public int getTurns(){
		return turns;
	}
	
	/** 
	 * This method is used when a player wants to do nothing and end
	 * that current unit's turn. Doesn't end the entire turn, just the
	 * turn of the currently selected unit.
	 */
	public void unitDoNothing(){
		currUnit.setCanMove();
		tempUnitList.remove(currUnit);
		if(tempUnitList.isEmpty())
			endTurn();
	}
	
	/**
	 * TODO Test
	 * Return the map. Used in setting up the GUI with the current game.
	 * @return the map of the current game
	 */
	public Map getMap(){
		return map;
	}
		
	/**
	 * TODO Edit when work is done
	 * When called, ends a turn.
	 * 
	 */
	public void endTurn(){
		if(playerTurn){
			playerTurn = false;
			tempUnitList.clear();
			// Add methods to switch to AI
		}
		else{
			playerTurn = true;
			tempUnitList.clear();
			tempUnitList = player1.allAliveUnits();
			turns++;
		}
	}
	
	/**
	 * TODO Add test to see if it is on the same side
	 * 
	 * Heal a friendly unit. Checks to see if on the same side, and if the
	 * unit can heal.
	 * 
	 * @param er, target row
	 * @param ec, target col
	 * @return can heal, or can't heal
	 */
	public boolean heal(int targetRow, int targetCol){
		return false;
	}
	
	
}
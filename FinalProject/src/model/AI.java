package model;

import java.util.LinkedList;
import java.util.List;

import units.*;

/**
 * Going to have a similar design as {@link Player}. In iteration 2, will be able
 * to assign AI behaviors to different units.
 * 
 * @author Brian Seaman
 *
 */
public class AI {

	private List<Unit> livingUnits;
	private List<Unit> allUnits;
	private List<Unit> deadUnits;
	private double d;
	private int totalAILeft;

	/**
	 * The constructor for the AI. Designs the enemy team by difficulty.
	 * Sets AI behavior.
	 * 
	 * @param The game difficulty
	 */
	public AI(Difficulty d){
		this.d = d.getValue();
		allUnits = new LinkedList<Unit>();
		deadUnits = new LinkedList<Unit>();
		livingUnits = new LinkedList<Unit>();
		generateTeam();
		totalAILeft=0;
	}

	/**
	 * Generates the enemy team. Will be used in Iteration 2.
	 */
	private void generateTeam() {
		// TODO Use the factory to make a team
	}

	/**
	 * Gets the team's stats.
	 *
	 * @return the team stats
	 */
	public String getTeamStats() {
		String temp = "";
		for(Unit i: allUnits)
			temp = temp + i.getStats() + "\n";
		temp = temp + "\n";
		return temp;
	}
	
	/**
	 * Gets the units of the team that are currently alive.
	 *
	 * @return the team
	 */
	public List<Unit> getTeam(){
		return allUnits;
	}
	
	/**
	 * Checks to see if all of the AI's team is dead by
	 * checking to see if the LivingUnits list is empty. If it
	 * is, return true.
	 *
	 * @return whether or not the AI's team is dead.
	 */
	public boolean everyonesDeadDave(){
		return livingUnits.isEmpty();
	}

	/**
	 * Gets all of the currently living units.
	 *
	 * @return the list of all of the alive units
	 */
	public List<Unit> allAliveUnits() {
		return livingUnits;
	}

	/**
	 * Adds units to the AI's team.
	 *
	 * @param toAdd: unit to add to the the player's team.
	 */
	public void addUnits(Unit toAdd){
		totalAILeft++;
		livingUnits.add(toAdd);
	}
	
	/**
	 * Get the total number of alive units left.
	 *
	 * @return The number of alive members of the team
	 */
	public int getAliveNum(){
		return totalAILeft;
	}
	
	/**
	 * Called when a Unit is killed in game. Removes it from the living units.
	 *
	 * @param The unit to be removed
	 */
	public void unitKilled(Unit dead){
		livingUnits.remove(dead);
		totalAILeft--;
	}
	
	/**
	 * Set a predefined list of units to be the AI's team.
	 * 
	 * @param list of Units
	 */
	public void addListOfUnits(List<Unit> list){
		totalAILeft = list.size();
		allUnits = new LinkedList<Unit>(list);
		livingUnits = new LinkedList<Unit>(list);
	}
}

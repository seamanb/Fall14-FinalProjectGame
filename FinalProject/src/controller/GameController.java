package controller;

import gametype.*;

import java.awt.Graphics2D;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.JOptionPane;

import model.*;
import songplayer.Songs;
import space.*;
import units.*;
import view.GraphicalView;
import view.TRPGGUI;
import item.*;

/**
 * 
 * The controller for a game. Is responsible for delegating most of the gameplay. Sends messages to map, Saves Data, Loads Data,
 * sets up players, calculate which map is needed, sends messages to the enemy
 * team factory, etc.
 * 
 * 
 */
public class GameController implements Serializable {

	private transient GraphicalView graphical;
	private Player player1;
	private AI player2;
	private Map map;
	private List<Unit> tempUnitList;
	private Unit currUnit;
	private int turns;
	private boolean playerTurn;
	private boolean gameOver;
	private boolean playerWon;
	private boolean hasAttacked;
	private AIPathFinder aiMove;
	private int currRow;
	private int currCol;
	private int endRow = 51;
	private int endCol = 51;
	private int attackRow;
	private int attackCol;
	private Player currPlayer;
	private GameTypeInterface gameType;
	private Object winConditions;
	private boolean moveOn;
	private int rowValue;
	private int colValue;
	private boolean testing;
	boolean notShownNE;
	boolean notShownSW;
	boolean notShownSE;
	private ItemType usingItemType;

	/**
	 * Constructor the game controller. Sets all players, difficulties, what type of game it is, if it's testing, sets the map, sets the constructor, etc.
	 * 
	 * @param player1
	 *            the player1
	 * @param i
	 *            the i
	 * @param gameT
	 *            the game t
	 * @param testing
	 *            the testing
	 */
	public GameController(Player player1, Difficulty i, String gameT, boolean testing) {
		this.map = new Map(i.getValue(), gameT, testing);
		this.player1 = player1;
		this.player2 = new AI(i);

		this.testing = testing;
		if (gameT.equalsIgnoreCase("corner"))
			gameType = new FourCorners();
		else if (gameT.equalsIgnoreCase("survive"))
			gameType = new Survive(10);
		else
			gameType = new CaptureTower();
		// winConditions = false;

		Stack<Unit> temp = new Stack<Unit>();

		// Put the player's units into a stack and put it into the Map
		for (Unit k : player1.getTeam()) {
			k.setCanMove(true);
			temp.push(k);
		}

		// Place the players on the map
		map.addUnitsToMap(temp, testing);
		// Place the enemy on the map / Get Enemy from map
		player2.addListOfUnits(map.getEnemyUnits());

		tempUnitList = new ArrayList<Unit>(player1.allAliveUnits());
		for (Unit j : tempUnitList)
			j.setCanMove(true);
		turns = 0;
		playerTurn = true;
		currPlayer = player1;
		hasAttacked = false;

		// Give the enemy units behaviors.
		aiMove = new AIPathFinder(map);

		checkWinConditions();
	}

	/**
	 * Gets the curr player name.
	 * 
	 * @return the curr player name
	 */
	public String getCurrPlayerName() {
		if (playerTurn)
			return player1.getID();
		else
			return "AI Enemy ";
	}

	/**
	 * Set the the current unit to the unit located at this space. Will return
	 * true if it is.
	 * 
	 * @param row
	 *            the row
	 * @param col
	 *            the col
	 * @return true, if successful
	 */
	public boolean setCurrentUnit(int row, int col) {
		if (map.getUnitAt(row, col) != null) {
			if (map.getUnitAt(row, col).canMove()) {
				map.resetMapCanMove();

				currUnit = map.getUnitAt(row, col);
				currRow = row;
				currCol = col;
				setCanMove(row, col);
				
				map.updateObservers();
				return true;
			}

			else {
				return false;
			}
		}

		else {
			return false;
		}
	}

	/**
	 * Get method for the CurrUnit.
	 * 
	 * @return the currently selected unit
	 */
	public Unit getCurrentUnit() {
		return currUnit;
	}

	/**
	 * Used when selecting a unit from the GUI. If the Unit is there and can
	 * move, set it to be the current. Return the unit, or null.
	 * 
	 * @param r
	 *            , the row
	 * @param c
	 *            , the column
	 * @return the current Unit
	 */
	public Unit getUnitOnMap(int r, int c) {
		Unit temp = map.getUnitAt(r, c);
		return temp;
	}

	/**
	 * Move a selected Unit to another space. Checks to see if the player can
	 * move to the targeted space, and if can, move them.
	 */
	public void move() {
		// Check to see if the end Row and end Col point to something
		if (endRow != 51 || endCol != 51) {
			if (!(map.getSpace(endCol, endRow).getSpaceType().equals("Wall"))) {
				
				if (currUnit != null) {
					if (currUnit.canMove() && (!map.isOccupied(endRow, endCol) || map.getUnitAt(endRow, endCol) == currUnit) && map.getSpace(endRow, endCol).getCanMoveTo()) {

						moveOn = false;

						if (currUnit instanceof Doctor || currUnit instanceof Engineer || currUnit instanceof Ranger || currUnit instanceof Sniper || currUnit instanceof Soldier)
							goodUnitMove();

						map.resetMapCanMove();
						map.moveUnit(currRow, currCol, endRow, endCol);

						if (playerTurn)
							pickUpItem();

						// Set the new CurrRow and CurrCol, and check
						currRow = endRow;
						currCol = endCol;
						gameOver();

						if (playerTurn)
							attackAfterMove();

						// Take the unit that can no longer move out of the
						// tempUnitList
						if (!moveOn) {
							try {
								currUnit.setCanMove(false);
								tempUnitList.remove(currUnit);
								setCurrentUnitSelected(false);
								currUnit = null;
							} catch (Exception e) {

							}
						}

						endRow = 51;
						endCol = 51;

						map.updateObservers();

						if (tempUnitList.isEmpty())
							endTurn();

						return;
					}
				}

				if (currUnit == null) {
					if (playerTurn)
						JOptionPane.showMessageDialog(null, "Please select a Unit to move first");

				} else if (!currUnit.canMove()) {
					if (playerTurn)
						JOptionPane.showMessageDialog(null, "Unit can't move anymore. Select a new unit.");

				} else if (map.isOccupied(endRow, endCol)) {
					if (playerTurn)
						JOptionPane.showMessageDialog(null, "Space is occupied, you can't move there");

				} else if (!map.getSpace(endRow, endCol).getCanMoveTo()) {
					if (playerTurn)
						JOptionPane.showMessageDialog(null, "Space is out of range.");
				}

				return;

			} else
				JOptionPane.showMessageDialog(null, "You can't move on top of walls!");
		} else
			JOptionPane.showMessageDialog(null, "Pick a space to move to before you try moving...");

	}

	/**
	 * Good unit move.
	 */
	private void goodUnitMove() {
		int STARTCol = currCol;
		int STARTRow = currRow;
		while (currCol != endCol || currRow != endRow) {
			if (currRow > endRow) { // Up
				// map.moveUnit(currRow, currCol, currRow - 1, currCol);
				currRow--;
			} else if (currCol > endCol) { // Left
				// map.moveUnit(currRow, currCol, currRow, currCol - 1);
				currCol--;
			} else if (currRow < endRow) { // Down
				// map.moveUnit(currRow, currCol, currRow + 1, currCol);
				currRow++;
			} else if (currCol < endCol) { // Right
				// map.moveUnit(currRow, currCol, currRow, currCol + 1);
				currCol++;
			}
			currUnit.setCurrentPosition(currCol, currRow);
			Graphics2D g2 = (Graphics2D) graphical.getGraphics();
			g2.scale(1 * graphical.getScaleFactor(), 1 * graphical.getScaleFactor());
			currUnit.drawUnit(g2);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		Unit[][] tempUnits = map.getUnits();
		tempUnits[currCol][currRow] = tempUnits[STARTCol][STARTRow];
		tempUnits[STARTCol][STARTRow] = null;
		Space[][] tempSpaces = map.getSpaces();
		tempSpaces[STARTCol][STARTRow].setOccupied(false);
		tempSpaces[currCol][currRow].setOccupied(true);
		graphical.repaint();

	}

	/**
	 * Pick up item. Puts it into the Unit's inventory.
	 */
	private void pickUpItem() {

		Item[][] itemsOnMap = map.getItems();

		if (!(itemsOnMap[endCol][endRow] == null)) {

			if (itemsOnMap[endCol][endRow] instanceof RandomItem) {

				Item newItem = RandomItem.generateItem();
				if (newItem.getItemType() == ItemType.MEDKIT)
					JOptionPane.showMessageDialog(null, "Your " + currUnit.getUnitType() + " picked up a basic medkit!");
				if (newItem.getItemType() == ItemType.MINE)
					JOptionPane.showMessageDialog(null, "Your " + currUnit.getUnitType() + " picked up a basic mine!");
				if (newItem.getItemType() == ItemType.GRENADE)
					JOptionPane.showMessageDialog(null, "Your " + currUnit.getUnitType() + " picked up a basic grenade!");
				currUnit.addItem(newItem);

			} else {

				Item newItem = RandomBoost.generateBoost();
				if (newItem.getItemType() == ItemType.HP)
					JOptionPane.showMessageDialog(null, "Your " + currUnit.getUnitType() + " picked up an HP boost!");
				if (newItem.getItemType() == ItemType.ATK)
					JOptionPane.showMessageDialog(null, "Your " + currUnit.getUnitType() + " picked up an attack boost!");
				if (newItem.getItemType() == ItemType.DEF)
					JOptionPane.showMessageDialog(null, "Your " + currUnit.getUnitType() + " picked up a defense boost!");

				currUnit.addItem(newItem);
				currUnit.UpdateBoosts();
			}
			map.removeItem(endRow, endCol);
		}

	}

	/**
	 * Attack after moving. Only works if the unit is a player controlled units.
	 */
	private void attackAfterMove() {

		if (!gameOver) {

			for (Unit p : player2.allAliveUnits()) {

				if (inAttackRange((int) p.getY(), (int) p.getX())) {
					int answer = JOptionPane.showConfirmDialog(null, "There are possible Units to attack in range. Would you like to attack one of them?", "Attack?", JOptionPane.YES_NO_OPTION);
					if (answer == JOptionPane.YES_OPTION) {

						moveOn = true;

						endCol = (int) p.getX();
						endRow = (int) p.getY();
						attack();

						break;

					}
					break;
				}
			}
		}

	}

	/**
	 * Attack the unit at the endrow/column.
	 */
	public void attack() {

		if (!(currUnit == null)) {

			if (endRow != 51 || endCol != 51) {

				// Makes sure that the target is not the current unit
				if (currUnit != map.getUnitAt(endRow, endCol)) {

					// Checks to see if on the same side.
					if (!SameTeam()) {

						// Checks to see if either of the units are false.
						if (currUnit != null && map.getUnitAt(endRow, endCol) != null) {
							// if both exist, check if one can move
							if (inAttackRange(currRow, currCol)) {
								actAttack();

								// If no other unit can move, end the turn
								currUnit.setCanMove(false);
								currUnit.setIsSelected(false);
								tempUnitList.remove(currUnit);
								currUnit = null;
								endRow = 51;
								endCol = 51;

								map.resetMapCanMove();

								gameOver();
								map.updateObservers();
								if (tempUnitList.isEmpty())
									endTurn();
							}

							else {
								if (playerTurn)
									JOptionPane.showMessageDialog(null, "Enemy out of attack Range.");
							}
						} else
							JOptionPane.showMessageDialog(null, "Nothing to Attack!");
					} else
						JOptionPane.showMessageDialog(null, "You cannot attack your own teammates...");
				} else
					JOptionPane.showMessageDialog(null, "You can't attack youself!");
			} else
				JOptionPane.showMessageDialog(null, "Pick a unit to attack before you try attacking...");
		} else
			JOptionPane.showMessageDialog(null, "Pick a unit to commit the attack before you try attacking...");

		if (currUnit != null && !playerTurn) {
			currUnit.setCanMove(false);
			tempUnitList.remove(currUnit);
			currUnit = null;
			if (tempUnitList.isEmpty())
				endTurn();
		}
	}


	/**
	 * Check to see if units are on the same team. If they are, return true.
	 * 
	 * @return whether or not the target and the current are on the same team.
	 */
	private boolean SameTeam() {

		List<Unit> tempList;

		// Checks the player lists depending on whose turn it is
		if (playerTurn) {
			tempList = player1.getTeam();
		} else {
			tempList = player2.getTeam();

		}

		// Goes through the lists, checking to see if the target unit is in the
		// temporary list. Return whether or not it is.
		for (Unit j : tempList) {
			if (j == map.getUnitAt(endRow, endCol)) {
				return true;
			}

		}
		return false;
	}

	/**
	 * Simply check if the enemy is in range. Range is based on the four
	 * cardinal directions.
	 * 
	 * @param row
	 *            the row
	 * @param col
	 *            the col
	 * @return If the current unit is in range of the target.
	 */
	private boolean inAttackRange(int row, int col) {
		int temp = currUnit.getRange();

		while (temp >= 0) {
			// Try to see if the weapon will reach.
			if (row < endRow) {
				row++;
				temp--;
			} else if (row > endRow) {
				row--;
				temp--;
			} else if (col < endCol) {
				col++;
				temp--;
			} else if (col > endCol) {
				col--;
				temp--;
			}
			// Else, both are equal; return true.
			else
				return true;
		}

		return false;
	}

	/**
	 * Actually attack the target. Only called if the currUnit can attack the
	 * targeted one.
	 */
	private void actAttack() {
		map.getUnitAt(endRow, endCol).reduceHealth(currUnit.getAttack());
		targetDead(endRow, endCol);

		hasAttacked = true;
		attackRow = endRow;
		attackCol = endCol;
	}

	/**
	 * Gets the attack row.
	 * 
	 * @return the attack row
	 */
	public int getAttackRow() {
		return attackRow;
	}

	/**
	 * Gets the attack col.
	 * 
	 * @return the attack col
	 */
	public int getAttackCol() {
		return attackCol;
	}

	/**
	 * Gets the checks for attacked.
	 * 
	 * @return the checks for attacked
	 */
	public boolean getHasAttacked() {
		return hasAttacked;
	}

	/**
	 * Checks to see if the unit has an item.
	 * 
	 * @param item
	 *            the item
	 * @return true, if it does
	 */
	public boolean currUnitHasItem(ItemType item) {
		return currUnit.hasItem(item);
	}

	/**
	 * Use the item of a selected unit.
	 * 
	 * @return if the item was used.
	 */
	public void useItem() {

		if (!(currUnit == null)) {
			if (endRow != 51 || endCol != 51) {
				if (inAttackRange(currRow, currCol)) {

					Object[] options = { "Health Kit", "Mine", "Grenade", "Cancel" };
					int answer = JOptionPane.showOptionDialog(null, "What item would you like to use?", "Use Item?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

					if (answer == JOptionPane.YES_OPTION) {
						usingItemType = ItemType.MEDKIT;

					} else if (answer == JOptionPane.NO_OPTION) {
						usingItemType = ItemType.MINE;

					} else if (answer == JOptionPane.CANCEL_OPTION) {
						usingItemType = ItemType.GRENADE;

					} else {

					}

					if (currUnit.hasItem(usingItemType)) {

						if (usingItemType == ItemType.MEDKIT) {

							if (!(map.getUnitAt(endRow, endCol) == null)) {

								if (SameTeam()) {

									map.getUnitAt(endRow, endCol).restoreHealth();
									JOptionPane.showMessageDialog(null, "The " + map.getUnitAt(endRow, endCol).getUnitType() + " you selected has had their health fully restored.");
									currUnit.setCanMove(false);
									currUnit.removeItem(usingItemType);
									currUnit.setIsSelected(false);
									tempUnitList.remove(currUnit);
									currUnit = null;
									endRow = 51;
									endCol = 51;
									map.resetMapCanMove();
									map.updateObservers();

								} else
									JOptionPane.showMessageDialog(null, "You don't want to heal a " + map.getUnitAt(endRow, endCol).getUnitType() + "!");

							} else
								JOptionPane.showMessageDialog(null, "There is nothing there to heal!");
						} // end health kit

						if (usingItemType == ItemType.MINE) {

							JOptionPane.showMessageDialog(null, "A Mine has been placed");
							map.getSpace(endCol, endRow).setHasMine(true);
							currUnit.setCanMove(false);
							currUnit.removeItem(usingItemType);
							currUnit.setIsSelected(false);
							tempUnitList.remove(currUnit);
							currUnit = null;
							endRow = 51;
							endCol = 51;
							map.resetMapCanMove();
							map.updateObservers();

						} // end Mine

						if (usingItemType == ItemType.GRENADE) {

							JOptionPane.showMessageDialog(null, "Your " + map.getUnitAt(currRow, currCol).getUnitType() + " threw a grenade.");
							blowShitUp(endRow, endCol);
							currUnit.setCanMove(false);
							currUnit.removeItem(usingItemType);
							currUnit.setIsSelected(false);
							tempUnitList.remove(currUnit);
							currUnit = null;
							endRow = 51;
							endCol = 51;
							map.resetMapCanMove();
							map.updateObservers();
						}

					} else
						JOptionPane.showMessageDialog(null, "The unit you selected does not have that item.");
				} else
					JOptionPane.showMessageDialog(null, "The place you are tying to use the item is out of this units range");

			} else
				JOptionPane.showMessageDialog(null, "Pick a space to use the Item first");

		} else
			JOptionPane.showMessageDialog(null, "Pick a unit to use an Item first");

		checkWinConditions();

	}

	/**
	 * Blow those zombies up, kid. Just try not to blow
	 * yourself up.
	 * 
	 * @param row
	 *            the row
	 * @param col
	 *            the col
	 */
	private void blowShitUp(int row, int col) {

		int baseRow = row;
		int baseCol = col;

		if (map.getSpace(col, row).getOccupied()) {
			// if (!(SameTeam())) {
			map.getUnitAt(row, col).reduceHealth(100);
			targetDead(row, col);
			row = baseRow;
			col = baseCol;

			// }
		}

		if (map.getSpace(col, row + 1).getOccupied()) {
			// if (!(SameTeam())) {
			map.getUnitAt(row + 1, col).reduceHealth(75);
			targetDead(row + 1, col);
			row = baseRow;
			col = baseCol;
			// }
		}

		if (map.getSpace(col, row - 1).getOccupied()) {
			// if (!(SameTeam())) {
			map.getUnitAt(row - 1, col).reduceHealth(75);
			targetDead(row - 1, col);
			row = baseRow;
			col = baseCol;
			// }
		}

		if (map.getSpace(col + 1, row).getOccupied()) {
			// if (!(SameTeam())) {
			map.getUnitAt(row, col + 1).reduceHealth(75);
			targetDead(row, col + 1);
			row = baseRow;
			col = baseCol;
			// }
		}

		if (map.getSpace(col - 1, row).getOccupied()) {
			// if (!(SameTeam())) {
			map.getUnitAt(row, col - 1).reduceHealth(75);
			targetDead(row, col - 1);
			row = baseRow;
			col = baseCol;
		}
		// }
	}

	/**
	 * Checks both of the player's aliveUnits to see if all of their units are
	 * dead. If either of them are out of units they can move, return true and
	 * end the game. Checked after every move and attack.
	 * 
	 * @return if the game is over or not
	 */
	public boolean gameOver() {
		if (!gameOver) {

			if (checkWinConditions()) {
				for (Unit u : tempUnitList)
					u.setCanMove(false);
				tempUnitList.clear();

				gameOver = true;
				playerWon = true;
				player1.gameFinished();

				return true;
			} else if (player1.everyonesDeadDave()) {
				for (Unit u : tempUnitList)
					u.setCanMove(false);
				tempUnitList.clear();

				gameOver = true;
				playerWon = false;
				// Display some kind of message telling player 1 won
				JOptionPane.showMessageDialog(null, "AI won! Better luck next time...");

				player1.gameFinished();
				TRPGGUI.setdontAskAgain(true);
				Songs.setPlaying(true);
				Songs.toogleSound();
				TRPGGUI.dispose();


				return true;
			} else
				return false;
		}

		return false;
	}

	/**
	 * Determine if a game of a particular type has been won. Sends the relevant
	 * information to the right file to check.
	 * 
	 * @return Depending on the game type, if the game has been won
	 */
	public boolean checkWinConditions() {
		if (gameType instanceof gametype.CaptureTower) {

			if (((map.getSpace(currCol, currRow) instanceof space.TowerSpace && playerTurn))) {
				JOptionPane.showMessageDialog(null, "Congrats you captured the tower! You win!");
				TRPGGUI.setdontAskAgain(true);
				Songs.setPlaying(true);
				Songs.toogleSound();
				TRPGGUI.dispose();
				return true;

			} else if (player2.everyonesDeadDave()) {
				JOptionPane.showMessageDialog(null, "Congrats you killed all the Zombies! You win!");
				TRPGGUI.setdontAskAgain(true);
				Songs.setPlaying(true);
				Songs.toogleSound();
				TRPGGUI.dispose();
				return true;

			} else {
				return false;
			}

		} else if (gameType instanceof gametype.Survive) {
			if ((!player1.everyonesDeadDave() && gameType.CheckWinCondition(turns))) {
				JOptionPane.showMessageDialog(null, "Congrats you survived the zombie attack! You win!");
				TRPGGUI.setdontAskAgain(true);
				Songs.setPlaying(true);
				Songs.toogleSound();
				TRPGGUI.dispose();
				return true;
			}
			if (player2.everyonesDeadDave()) {
				JOptionPane.showMessageDialog(null, "Congrats you killed all the Zombies! You win!");
				TRPGGUI.setdontAskAgain(true);
				Songs.setPlaying(true);
				Songs.toogleSound();
				TRPGGUI.dispose();
				return true;
			}

			else
				return false;
		}

		else if (gameType instanceof gametype.FourCorners) {
			if (!testing) {
				if (map.getSpace(0, 0).getOccupied()) {
					((CaptureCornerSpace) map.getSpace(0, 0)).setHasBeenCaptured(true);
					// JOptionPane.showMessageDialog(null,
					// "Northwest Tower captured.");
				}
				if (map.getSpace(0, 49).getOccupied()) {
					((CaptureCornerSpace) map.getSpace(0, 49)).setHasBeenCaptured(true);
					if (!notShownNE) {
						JOptionPane.showMessageDialog(null, "Northeast Tower captured.");
						notShownNE = true;
					}
				}
				if (map.getSpace(49, 0).getOccupied()) {
					((CaptureCornerSpace) map.getSpace(49, 0)).setHasBeenCaptured(true);
					if (!notShownSW) {
						JOptionPane.showMessageDialog(null, "Southwest Tower captured.");
						notShownSW = true;
					}
				}
				if (map.getSpace(49, 49).getOccupied()) {
					((CaptureCornerSpace) map.getSpace(49, 49)).setHasBeenCaptured(true);
					if (!notShownSE) {
						JOptionPane.showMessageDialog(null, "Southeast Tower captured.");
						notShownSE = true;
					}
				}

				if (player2.everyonesDeadDave()) {
					JOptionPane.showMessageDialog(null, "Congrats you killed all the Zombies! You win!");
					TRPGGUI.setdontAskAgain(true);
					Songs.setPlaying(true);
					Songs.toogleSound();
					TRPGGUI.dispose();
					return true;
				}
				if (((CaptureCornerSpace) map.getSpace(0, 0)).getHasBeenCaptured())
					if (((CaptureCornerSpace) map.getSpace(49, 0)).getHasBeenCaptured())
						if (((CaptureCornerSpace) map.getSpace(0, 49)).getHasBeenCaptured())
							if (((CaptureCornerSpace) map.getSpace(49, 49)).getHasBeenCaptured()) {
								JOptionPane.showMessageDialog(null, "Congrats you secured all the towers! You win!");
								TRPGGUI.setdontAskAgain(true);
								Songs.setPlaying(true);
								Songs.toogleSound();
								TRPGGUI.dispose();
								return true;
							}
			} else {

				if (map.getSpace(1, 1).getOccupied()) {
					((CaptureCornerSpace) map.getSpace(1, 1)).setHasBeenCaptured(true);
					// JOptionPane.showMessageDialog(null,
					// "Northwest Tower captured.");
				}
				if (map.getSpace(1, 8).getOccupied()) {
					((CaptureCornerSpace) map.getSpace(1, 8)).setHasBeenCaptured(true);
					if (!notShownNE) {
						JOptionPane.showMessageDialog(null, "Northeast Tower captured.");
						notShownNE = true;
					}
				}
				if (map.getSpace(8, 1).getOccupied()) {
					((CaptureCornerSpace) map.getSpace(8, 1)).setHasBeenCaptured(true);
					if (!notShownSW) {
						JOptionPane.showMessageDialog(null, "Southwest Tower captured.");
						notShownSW = true;
					}
				}
				if (map.getSpace(8, 8).getOccupied()) {
					((CaptureCornerSpace) map.getSpace(8, 8)).setHasBeenCaptured(true);
					if (!notShownSE) {
						JOptionPane.showMessageDialog(null, "Southeast Tower captured.");
						notShownSE = true;
					}
				}

				if (player2.everyonesDeadDave()) {
					JOptionPane.showMessageDialog(null, "Congrats you killed all the Zombies! You win!");
					TRPGGUI.setdontAskAgain(true);
					Songs.setPlaying(true);
					Songs.toogleSound();
					TRPGGUI.dispose();
					return true;
				}
				if (((CaptureCornerSpace) map.getSpace(1, 1)).getHasBeenCaptured())
					if (((CaptureCornerSpace) map.getSpace(1, 8)).getHasBeenCaptured())
						if (((CaptureCornerSpace) map.getSpace(8, 1)).getHasBeenCaptured())
							if (((CaptureCornerSpace) map.getSpace(8, 8)).getHasBeenCaptured()) {
								JOptionPane.showMessageDialog(null, "Congrats you secured all the towers! You win!");
								TRPGGUI.setdontAskAgain(true);
								Songs.setPlaying(true);
								Songs.toogleSound();
								TRPGGUI.dispose();
								return true;
							}
			}
			return false;
		}

		else
			return false;
	}

	/**
	 * Get all of the stats for the selected player.
	 * 
	 * @return the team stats
	 */
	public String getTeamStats() {
		if (this.playerTurn) {
			return player1.getTeamStats();
		} else { // Finish once AI is working
			return player2.getTeamStats();
		}
	}

	/**
	 * Get the selected unit's stats.
	 * 
	 * @param u
	 *            , the player's unit
	 * @return the curr unit stats
	 */
	public String getCurrUnitStats(Unit u) {
		return currUnit.getStats();
	}

	/**
	 * Get the number of turns gone through in the game.
	 * 
	 * @return the number of turns taken in game
	 */
	public int getTurns() {
		return turns;
	}

	/**
	 * This method is used when a player wants to do nothing and end that
	 * current unit's turn. Doesn't end the entire turn, just the turn of the
	 * currently selected unit.
	 */
	public void unitDoNothing() {
		if (currUnit != null) {
			endCol = currCol;
			endRow = currRow;

			map.resetMapCanMove();
			// Take the unit that can no longer move out of the
			// tempUnitList
			if (!moveOn) {
				try {
					currUnit.setCanMove(false);
					tempUnitList.remove(currUnit);
					setCurrentUnitSelected(false);
					currUnit = null;
				} catch (Exception e) {

				}
			}

			endRow = 51;
			endCol = 51;

			map.updateObservers();

			if (tempUnitList.isEmpty())
				endTurn();
		}

		else
			JOptionPane.showMessageDialog(null, "Please select a unit.");
	}

	/**
	 * Return the map. Used in setting up the GUI with the current game.
	 * 
	 * @return the map of the current game
	 */
	public Map getMap() {
		return map;
	}


	/**
	 * When called, ends a turn. Checks to see whose turn it is, clears the
	 * temporary unit list, sets the current unit to null. Sets the can move to
	 * false.
	 * 
	 */
	public void endTurn() {
		map.resetMapCanMove();
		if (!gameOver) {
			if (playerTurn) {
				map.setIsPlayerTurn();

				// Remove all of the player's units from tempList
				playerTurn = false;
				for (Unit i : tempUnitList)
					i.setCanMove(false);
				tempUnitList.clear();
				if(currUnit != null)
					currUnit.setIsSelected(false);
				currUnit = null;

				JOptionPane.showMessageDialog(null, "It is now the Computers turn.");

				// Switch to AI
				tempUnitList = new ArrayList<Unit>(player2.allAliveUnits());
				for (Unit i : tempUnitList)
					i.setCanMove(true);

				map.updateObservers();
				enemyTurn();

			} else {

				playerTurn = true;
				map.setIsPlayerTurn();

				//
				for (Unit i : player2.allAliveUnits())
					i.setCanMove(false);
				tempUnitList.clear();
				if(currUnit != null)
					currUnit.setIsSelected(false);
				currUnit = null;

				tempUnitList = new ArrayList<Unit>(player1.allAliveUnits());
				for (Unit i : tempUnitList)
					i.setCanMove(true);
				turns++;

				JOptionPane.showMessageDialog(null, "It is now your turn.");

				currCol = currRow = 0;

				ArrayList<Point> mines = aiMove.getSteppedOnMines();

				for (Point p : mines) {
					blowShitUp(p.x, p.y);
					map.getSpace(p.y, p.x).setHasMine(false);

				}
				aiMove.clearSteppedOnMines();
				gameOver();

				map.updateObservers();
				if (tempUnitList.isEmpty())
					endTurn();
			}
		}
	}


	/**
	 * Sets the new endColumn. Used in attack and movement.
	 * 
	 * @param endRow
	 *            , the new ending row
	 */
	public void setEndRow(int endRow) {
		this.endRow = endRow;
	}

	/**
	 * Sets the new endColumn. Used in attack and movement.
	 * 
	 * @param endCol
	 *            , the new ending column
	 */
	public void setEndColumn(int endCol) {
		this.endCol = endCol;
	}

	/**
	 * Gets the new endColumn. Used in attack and movement.
	 * 
	 * @return the end row
	 */
	public int getEndRow() {
		return endRow;
	}

	/**
	 * Gets the new endColumn. Used in attack and movement.
	 * 
	 * @return the end column
	 */
	public int getEndColumn() {
		return endCol;
	}

	/**
	 * Decides if the current unit can move onto a surrounding space. Called
	 * twice, before and after a move/attack.
	 * 
	 * @param row
	 *            the row
	 * @param col
	 *            the col
	 */
	private void setCanMove(int row, int col) {
		map.getSpace(currRow, currCol).setCanMoveTo(true);

		if (row < 49)
			if (map.getSpace(row + 1, col).getWalkable())
				canMoveHelper(currUnit.getMovement(), row + 1, col);
		if (row > 0)
			if (map.getSpace(row - 1, col).getWalkable())
				canMoveHelper(currUnit.getMovement(), row - 1, col);
		if (col < 49)
			if (map.getSpace(row, col + 1).getWalkable())
				canMoveHelper(currUnit.getMovement(), row, col + 1);
		if (col > 0)
			if (map.getSpace(row, col - 1).getWalkable())
				canMoveHelper(currUnit.getMovement(), row, col - 1);
	}

	/** 
	 * Helper method for setCanMove.
	 * 
	 * @param movesAvail
	 *            the moves avail
	 * @param row
	 *            the row
	 * @param col
	 *            the col
	 */
	private void canMoveHelper(int movesAvail, int row, int col) {

		if (movesAvail >= map.getSpace(col, row).getMoveHinderance() && map.getSpace(row, col).getWalkable()) {
			movesAvail = movesAvail - map.getSpace(col, row).getMoveHinderance();
			map.getSpace(row, col).setCanMoveTo(true);
			if (row < 49)
				if (map.getSpace(row + 1, col).getWalkable())
					canMoveHelper(movesAvail, row + 1, col);
			if (row > 0)
				if (map.getSpace(row - 1, col).getWalkable())
					canMoveHelper(movesAvail, row - 1, col);
			if (col < 49)
				if (map.getSpace(row, col + 1).getWalkable())
					canMoveHelper(movesAvail, row, col + 1);
			if (col > 0)
				if (map.getSpace(row, col - 1).getWalkable())
					canMoveHelper(movesAvail, row, col - 1);
		}
	}

	/**
	 * Checks to see if a specific unit is dead. If it is, remove it from the
	 * map and from the alive unit lists in the associated team.
	 * 
	 * @param row
	 *            the row
	 * @param col
	 *            the col
	 */
	private void targetDead(int row, int col) {
		Unit temp = map.getUnitAt(row, col);
		if (map.getUnitAt(row, col) instanceof Hole) {
			map.coverUpHole(row, col);
		}
		if (!temp.isAlive()) {

			if (playerTurn) {

				if (map.getUnitAt(col, row) instanceof Hole)
					JOptionPane.showMessageDialog(null, "You threw a bomb down the hole and stopped zombies from crawling out of it!");
				else
					JOptionPane.showMessageDialog(null, "The attacked " + map.getUnitAt(row, col).getUnitType() + " was left with no health and has died!" + '\n' + "Number of units remaining on both sides: " + player1.getID() + " - " + player1.getAliveNum() + ", Zombies - " + (player2.getAliveNum() - 1));

			} else {

				JOptionPane.showMessageDialog(null, "The attacked " + map.getUnitAt(row, col).getUnitType() + " was left with no health and has died!" + '\n' + "Number of units remaining on both sides: " + player1.getID() + " - " + (player1.getAliveNum() - 1) + ", Zombies - " + (player2.getAliveNum()));
			}

			map.removeUnit(col, row);

			// Remove them from the associated list
			if (player1.allAliveUnits().contains(temp))
				player1.unitKilled(temp);
			else
				player2.unitKilled(temp);

			// If the unit is in the temporary list, remove it.
			if (tempUnitList.contains(temp))
				tempUnitList.remove(temp);

		} else {

			if (map.getUnitAt(row, col).getNoDamage()) {
				JOptionPane.showMessageDialog(null, "The attacked " + map.getUnitAt(row, col).getUnitType() + " had too high of a defense for it to be harmed");
				map.getUnitAt(row, col).setNoDamage(false);
			} else {
				JOptionPane.showMessageDialog(null, "The attacked " + map.getUnitAt(row, col).getUnitType() + " was left with " + map.getUnitAt(row, col).getHealth() + " health after the attack!");

			}
		}
	}

	/**
	 * Checks to see if the game is over. If it is, return true;
	 * 
	 * @return if the game is over or not
	 */
	public boolean isGameOver() {
		return gameOver;
	}

	/**
	 * Called upon to check if the player has won. Returns true if the player
	 * wins the game.
	 * 
	 * @return Whether or not the game has won
	 */
	public boolean playerWon() {
		return playerWon;
	}

	/**
	 * Checks to see if the Player is currently moving. If the player is, return
	 * true.
	 * 
	 * @return Whether or not the player is moving.
	 */
	public boolean playerTurn() {
		return playerTurn;
	}

	/**
	 * Gets the point locations of all of the player locations. Used with enemy
	 * AI, gives them a list of targets that they can move to.
	 * 
	 * @return A List of Points with all of the locations of monsters.
	 */
	public List<Unit> getPlayerUnits() {
		return player1.allAliveUnits();
	}

	private Point toAttack;

	/**
	 * The method used to go through the enemy units. Moves or attacks them.
	 */
	public synchronized void enemyTurn() {
		List<Unit> enemyUnitList = new ArrayList<Unit>(player2.allAliveUnits());

		if (!playerTurn) {
			Point temp = null;

			for (Unit u : enemyUnitList) {
				toAttack = null;

				
				temp = this.nearestPlayerUnit(new Point(u.getX(), u.getY()));
				this.setCurrentUnit(u.getY(), u.getX());
				this.endRow = temp.y;
				this.endCol = temp.x;
			
				if (aIInAttackRange(u.getY(), u.getX(), u.getRange())) {
					endCol = toAttack.x;
					endRow = toAttack.y;

					map.getUnitAt(endRow, endCol).reduceHealth(u.getAttack());
					targetDead(endRow, endCol);
					gameOver();
					map.updateObservers();
					tempUnitList.remove(u);
					if (tempUnitList.isEmpty()) {
						break;
					}
				}

				else
					enemyMove(new Point(u.getX(), u.getY()));

			}
		}
		if (!playerTurn) {
			endTurn();
		}
	}

	/**
	 * Attacks the nearest unit. Used by the AI.
	 * 
	 * @param row
	 *            the row
	 * @param col
	 *            the col
	 * @param rangeLeft
	 *            the range left
	 * @return true, if successful
	 */
	private boolean aIInAttackRange(int row, int col, int rangeLeft) {

		if (rangeLeft >= 0) {
			if (map.isOccupied(row, col)) {
				if (player1.allAliveUnits().contains(map.getUnitAt(row, col))) {
					toAttack = new Point(col, row);
					return true;
				}
			}

			if (rangeLeft > 0) {
				boolean toReturn = false;

				if (!toReturn && row < 49)
					toReturn = aIInAttackRange(row + 1, col, rangeLeft - 1);
				if (!toReturn && row > 0)
					toReturn = aIInAttackRange(row - 1, col, rangeLeft - 1);
				if (!toReturn && col < 49)
					toReturn = aIInAttackRange(row, col + 1, rangeLeft - 1);
				if (!toReturn && col > 0)
					toReturn = aIInAttackRange(row, col - 1, rangeLeft - 1);

				return toReturn;
			}
		}

		return false;
	}

	/**
	 * Automatically moves the enemy AI. Moves them
	 * toward the closest human based on their behavior. If they are near enough
	 * to a player's unit, attack.
	 * 
	 * @param em
	 *            the em
	 */
	public synchronized void enemyMove(Point em) {
		Point p = nearestPlayerUnit(em);

		rowValue = aiMove.traverse(em.y, em.x, p.y, p.x, currUnit.getMovement()).x;
		colValue = aiMove.traverse(em.y, em.x, p.y, p.x, currUnit.getMovement()).y;

		endRow = rowValue;
		endCol = colValue;
		move();
	}

	/**
	 * Finds the nearest player location point closest to an enemy unit. Returns
	 * the nearest point based on how many moves would be needed to make it.
	 * 
	 * @param enemyLoc
	 *            the enemy loc
	 * @return the point
	 */
	public Point nearestPlayerUnit(Point enemyLoc) {
		int spaceNear = 0;
		int tempSN = 0;
		Point toReturn = null;

		for (Unit p : player1.allAliveUnits()) {
			tempSN = Math.abs(enemyLoc.x - p.getX()) + Math.abs(enemyLoc.y - p.getY());
			if (tempSN <= spaceNear || spaceNear == 0) {
				spaceNear = tempSN;
				toReturn = new Point(p.getX(), p.getY());
			}
		}

		return toReturn;
	}

	/**
	 * Sets the current Player.
	 * 
	 * @param player
	 *            the new current player
	 */

	public void setCurrentPlayer(Player player) {
		currPlayer = player;
	}

	/**
	 * Sets the player turn.
	 * 
	 * @param whosTurn
	 *            the new player turn
	 */
	public void setPlayerTurn(boolean whosTurn) {
		playerTurn = whosTurn;

	}

	/**
	 * Sets the checks for attacked.
	 * 
	 * @param hasAttacked
	 *            the new checks for attacked
	 */
	public void setHasAttacked(boolean hasAttacked) {
		this.hasAttacked = hasAttacked;
	}


	/**
	 * Sets the current unit selected.
	 * 
	 * @param v
	 *            the new current unit selected
	 */
	public void setCurrentUnitSelected(boolean v) {
		if (currUnit != null) {
			currUnit.setIsSelected(v);
		}
	}

	public void setGraphicalView(GraphicalView graphical) {
		this.graphical = graphical;
	}

}
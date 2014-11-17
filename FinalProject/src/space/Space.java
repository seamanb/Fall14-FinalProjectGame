package space;

public abstract class Space {

	private String spaceType;
	private Boolean walkable;
	private Boolean occupied;
	private Boolean visable;
	private int moveHinderance;
	private int visablityModifier;
	
	public Space (String spaceType, Boolean walkable, Boolean occupied, Boolean visable, int moveHinderance, int visablityModifier){
		
		this.spaceType = spaceType;
		this.walkable = walkable;
		this.occupied = occupied;
		this.visable = visable;
		this.moveHinderance = moveHinderance;
		this.visablityModifier = visablityModifier;
		
	}
	
	/**
	 * @return the spaceType
	 */
	public String getSpaceType() {
		return spaceType;
	}

	/**
	 * @return the walkable
	 */
	public Boolean getWalkable() {
		return walkable;
	}

	/**
	 * @return the moveHinderance
	 */
	public int getMoveHinderance() {
		return moveHinderance;
	}

	/**
	 * @return the occupied
	 */
	public Boolean getOccupied() {
		return occupied;
	}

	/**
	 * @param occupied the occupied to set
	 */
	public void setOccupied(Boolean occupied) {
		this.occupied = occupied;
	}

	/**
	 * @return the visable
	 */
	public Boolean getVisable() {
		return visable;
	}

	/**
	 * @param visable the visable to set
	 */
	public void setVisable(Boolean visable) {
		this.visable = visable;
	}

	/**
	 * @return the visablityModifier
	 */
	public int getVisablityModifier() {
		return visablityModifier;
	}


}

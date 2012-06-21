package organisms.g5;

import java.util.*;
import java.io.*;
import java.awt.Color;

import organisms.*;

public final class Grumpy implements Player {

	static final String _CNAME = "Farmer";
	static final Color _CColor = new Color(1.0f, 1.00f, 0.33f);
	private int state;
	private Random rand;
	private OrganismsGame game;
	private int age;
	private boolean isFarming = true;
	private int farmDirection;
	private int childrenLeft;


	/*
	 * This method is called when the Organism is created.
	 * The key is the value that is passed to this organism by its parent (not used here)
	 */
	public void register(OrganismsGame game, int key) throws Exception
	{
		rand = new Random();
		state = key;
		this.game = game;
		this.age = 0;
		this.isFarming = false;
		this.farmDirection = SOUTH;
		this.childrenLeft = (int)(1.2+Math.random()*1);
	}

	/*
	 * Return the name to be displayed in the simulator.
	 */
	public String name() throws Exception {
		return _CNAME;
	}

	/*
	 * Return the color to be displayed in the simulator.
	 */
	public Color color() throws Exception {
		return _CColor;
	}

	/*
	 * Not, uh, really sure what this is...
	 */
	public boolean interactive() throws Exception {
		return false;
	}

	/*
	 * This is the state to be displayed to other nearby organisms
	 */
	public int externalState() throws Exception {
		return state;
	}

	/*
	 * This is called by the simulator to determine how this Organism should move.
	 * foodpresent is a four-element array that indicates whether any food is in adjacent squares
	 * neighbors is a four-element array that holds the externalState for any organism in an adjacent square
	 * foodleft is how much food is left on the current square
	 * energyleft is this organism's remaining energy
	 */
	public Move move(boolean[] foodpresent, int[] neighbors, int foodleft, int energyleft) throws Exception {

		this.age++;
		Move m = null; // placeholder for return value

		if ( this.age < 200 && !(foodleft>50) ) {
			m = new Move(STAYPUT);
		} else {
			if(this.childrenLeft > 0){
				if ( neighbors[NORTH] == -1 ) {
					m = new Move(REPRODUCE, NORTH, NORTH);
					childrenLeft--;
				} else if ( neighbors[SOUTH] == -1 ) {
					m = new Move(REPRODUCE, SOUTH, SOUTH);
					childrenLeft--;
				} else if ( neighbors[EAST] == -1 ) {
					m = new Move(REPRODUCE, EAST, EAST);
					childrenLeft--;
				} else if ( neighbors[WEST] == -1 ) {
					m = new Move(REPRODUCE, WEST, WEST);
					childrenLeft--;
				} else {
					m = new Move(STAYPUT);
				}
			
			}
		}

		return m;
	}
}

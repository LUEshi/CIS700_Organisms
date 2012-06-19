package organisms.g5;

import java.util.*;
import java.io.*;
import java.awt.Color;

import organisms.*;

public final class Locust implements Player {

	static final String _CNAME = "Locust";
	static final Color _CColor = new Color(1.0f, 0.00f, 0.33f);
	private int state;
	private Random rand;
	private OrganismsGame game;
	private int age;
	private boolean isFarming = true;
	private boolean isReproducing;
	private int reproduceDirection;
	private int farmDirection;
	private int currentDirection;


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
		this.isReproducing = false;
		this.reproduceDirection = 1;
		this.farmDirection = SOUTH;
		this.currentDirection = key;
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


		// if energy less than movement cost, stay put unless food adjacent
		if ( energyleft < this.game.v() ) {
			this.isReproducing = false;
			for ( int i = 1; i <= 4; i ++  ) {
				if (foodpresent[i]) {
					m = new Move(i);
				}
			}
			m = new Move(STAYPUT);
		}

		
		if ( this.isReproducing ) {
			m = new Move(REPRODUCE, this.reproduceDirection, this.reproduceDirection);
			this.reproduceDirection++;
			if ( this.reproduceDirection > 4 ) {
				this.reproduceDirection = 1;
			}
		}
		
		// if max energy, start reproducing like crazy
		if ( energyleft >= this.game.M()*.90 ) {
			this.isReproducing = true;
		}

		// if we are on food, don't move!
		if ( foodleft > 0 && m == null ) {
			m = new Move(STAYPUT);
		}
		
		
		if ( m == null ) {
			// this player selects moves in the same direction, changing only when food is detected
			if ( foodpresent[NORTH] ) {
				currentDirection = NORTH;
			} else if ( foodpresent[EAST] ) {
				currentDirection = EAST;
			} else if ( foodpresent[SOUTH] ) {
				currentDirection = SOUTH;
			} else if ( foodpresent[WEST] ) {
				currentDirection = WEST;
			}
			// Move only every third turn
			if ( age %1 == 0 ) {
				m = new Move(currentDirection);
			} else {
				m = new Move(STAYPUT);
			}
		}

		return m;
	}
}

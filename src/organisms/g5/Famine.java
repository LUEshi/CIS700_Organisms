package organisms.g5;

import java.util.*;
import java.io.*;
import java.awt.Color;

import organisms.*;

public final class Famine implements Player {

	static final String _CNAME = "Famine";
	static final Color _CColor = new Color(0.9f, 1.00f, 0.9f);
	private int state;
	private Random rand;
	private OrganismsGame game;
	private int age;
	private List<Integer> foodSeen;
	private int stepsCounted;
	private boolean isFarming = true;
	private boolean isHoarding;
	private boolean isEating;
	private int farmDirection;
	private int currentDirection;
	private int moveInterval;


	/*
	 * This method is called when the Organism is created.
	 * The key is the value that is passed to this organism by its parent (not used here)
	 */
	public void register(OrganismsGame game, int key) throws Exception
	{
		rand = new Random();
		state = rand.nextInt(256);
		this.game = game;
		this.age = 0;
		this.isHoarding = false;
		this.isFarming = false;
		this.isEating = false;
		this.farmDirection = SOUTH;
		this.foodSeen = new ArrayList<Integer>();
		this.currentDirection = SOUTH;
		this.moveInterval = this.game.v()/this.game.s();
		this.stepsCounted = 10 * moveInterval;
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
	 * update the amount of food we can see
	 */
	private void updateFoodSeen( boolean[] foodpresent, int foodleft ) {
		int f = 0;
		for ( int i = 0; i < foodpresent.length; i++ ) {
			if ( foodpresent[i] ) {
				f++;
			}
		}
		if ( foodleft > 0 ) {
			f++;
		}
		this.foodSeen.add(f);
		while ( this.foodSeen.size() > this.stepsCounted ) {
			this.foodSeen.remove(0);
		}
	}
	
	private double foodPerStep() {
		double avg = 0.0;
		Iterator<Integer> iter = this.foodSeen.iterator();
		while ( iter.hasNext() ) {
			int fs = iter.next();
			avg += fs;
		}
		avg = avg / this.foodSeen.size();
		return avg;
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
		updateFoodSeen(foodpresent,foodleft);
		//this.game.println(" "+ foodPerStep());
		Move m = null; // placeholder for return value
		
		
		if ( this.isHoarding ) {
			if ( (this.isEating && isHungry(energyleft)) || (!this.isEating && !isHungry(energyleft)) ) {
				if ( foodleft == 0 ) {
					// we ate everything! start roaming again
					this.isEating = false;
					this.isHoarding = false;
				} else {
					// keep eating
					m = new Move(STAYPUT);					
				}
			}
			if ( !this.isEating && isHungry(energyleft)) {
				m = new Move(SOUTH);
				this.isEating = true;
			}
			if ( this.isEating && !isHungry(energyleft)) {
				m = new Move(NORTH);
				this.isEating = false;
			}
		}

		// Start hoarding valuable spaces, based on an arbitrary value
		// TODO: we could record stats and look for spaces above average or well above average (instead of an arbitrary fixed number)
		if ( !this.isHoarding && foodleft > 10 ) {
			m = new Move(NORTH);
			this.isHoarding = true;
		}
		
		
		// Reproduce if:
		// - We've seen more than 
		// - We're hoarding
		// - We're not hungry (age%100 == 0)
		// - turns mod 100 = 0 (slows growth)
		// - we have few immediate neighbors
		// TODO no immediate neighbors can lead to situations where there is plenty of food on the board, but clusters of existing organisms cannot propagate
		if ( foodPerStep() > 1d/moveInterval && this.isHoarding && !isHungry(energyleft) && numNeighbors(neighbors)<3 ) {
			if ( neighbors[NORTH] == -1 ) {
				m = new Move(REPRODUCE, NORTH, state);
			} else if (neighbors[EAST] == -1) {
				m = new Move(REPRODUCE, EAST, state);
			} else if (neighbors[WEST] == -1) {
				m = new Move(REPRODUCE, WEST, state);
			} else {
				m = new Move(STAYPUT);
			}
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
			// Move only every moveInterval turns
			if ( age % moveInterval == 0 ) {
				m = new Move(currentDirection);
			} else {
				m = new Move(STAYPUT);
			}
		}

		return m;
	}

	private int numNeighbors( int[] n ) {
		int result = 0;
		if ( n[NORTH] != -1 ) {
			result++;
		}
		if ( n[EAST] != -1 ) {
			result++;
		}
		if ( n[SOUTH] != -1 ) {
			result++;
		}
		if ( n[WEST] != -1 ) {
			result++;
		}
		return result;		
	}
	
	private boolean hasNeighbors ( int[] n ) {
		return numNeighbors(n)>0;
	}
	
	private boolean isHungry( int energyLeft ) {
		if ( energyLeft < this.game.M()*.4 ) {
			return true;
		}
		return false;
	}
}

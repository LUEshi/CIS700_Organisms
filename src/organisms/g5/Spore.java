package organisms.g5;

import java.util.*;
import java.io.*;
import java.awt.Color;

import organisms.*;

public final class Spore implements Player {

	static final String _CNAME = "Spore";
	static final Color _CColor = new Color(0.9f, 1.00f, 0.9f);
	private int state;
	private Random rand;
	private OrganismsGame game;
	private int age;
	private List<Integer> foodSeen;
	private int stepsCounted;
	private boolean germinated = false;
	private int childCount = 0; 
	private int currentDirection;
	private int moveInterval;
	private int waitTime;


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
		this.foodSeen = new ArrayList<Integer>();
		this.currentDirection = SOUTH;
		// TODO does this make sense when the probability of food is low?
		this.moveInterval = this.game.v()/this.game.s(); // Since stayput always costs 1, the move interval is always the cost of moving?
		this.stepsCounted = 10 * moveInterval;
		// TODO why 400 again?
		this.waitTime = 400 * ( this.game.v()/ this.game.u() ); // If moving requires as much energy as is gained from eating, wait a long time!
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
		
		// TODO why 80% Arbitrary, but suggests food may be plentiful nearby
		if ( foodleft > this.game.K()*.8 ) {
			this.germinated = true;
		} else if ( age < waitTime ) {
			m = new Move(STAYPUT);
			return m;			
		}

		
		if ( this.germinated ) {
			// TODO why 80%? Arbitrary, but suggest the organism is strong
			if ( this.childCount < 4 && numNeighbors(neighbors) < 4 && this.germinated && energyleft > this.game.M()*.8 ) {
				// generate spores
				// Start checking in random direction. (May make a difference--rather than always looking one direction first)
				int d = 1+rand.nextInt(4); // 1 to 4
				int direction = 0;
				for ( int i = d; i <= d+4; i++ ) {
					System.out.println("check dir");
					int nesw = (i%4) + 1;
					if ( neighbors[nesw]==-1 ) {
						direction = nesw;
					}
					break;
				}
				m = new Move(REPRODUCE, direction, state);
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

}

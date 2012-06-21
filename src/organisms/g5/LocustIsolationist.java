package organisms.g5;

import java.util.*;
import java.io.*;
import java.awt.Color;

import organisms.*;

public final class LocustIsolationist implements Player {

	static final String _CNAME = "Locust Isolationist";
	static final Color _CColor = Color.YELLOW;
	private int state;
	private Random rand;
	private OrganismsGame game;
	private int age;
	private boolean isFarming = true;
	private boolean isSuspicious = false;
	private boolean isReproducing;
	private int reproduceDirection;
	private int farmDirection;
	private int currentDirection;
	private List<Integer> foodSeen;
	private int stepsCounted = 20;
	private double reproducePct = 0.7;
	private int seckey = 13*13;
	boolean isGreedy;


	/*
	 * This method is called when the Organism is created.
	 * The key is the value that is passed to this organism by its parent (not used here)
	 */
	public void register(OrganismsGame game, int key) throws Exception
	{
		rand = new Random();
		if ( key < seckey ) {
			state = seckey;
		} else {
			state = key;
		}
		this.game = game;
		this.age = 0;
		this.isFarming = false;
		this.isReproducing = false;
		this.reproduceDirection = 1;
		this.farmDirection = SOUTH;
		this.currentDirection = key%seckey;
		this.foodSeen = new ArrayList<Integer>();
		this.isGreedy = false;
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
	
	private int turnRight() {
		int c = currentDirection;
		switch ( currentDirection ) {
	        case NORTH:
	        	c = EAST;
	            break;
	        case EAST:
	        	c = SOUTH;
	            break;
	        case SOUTH:
	        	c = WEST;
	            break;
	        case WEST:
	        	c = NORTH;
	            break;
		}
		return c;
	}
	
	public int getNeighborLocustCount( int[] neighbors ) {
		int neighborCount = 0;
		for ( int i = 1; i < neighbors.length; i++ ) {
			if ( neighbors[i] >= seckey && neighbors[i] <= seckey+4 ) {
				neighborCount++;
			}
		}
		return neighborCount;
	}

	public int getForeignerCount( int[] neighbors ) {
		int foreignerCount = 0;
		for ( int i = 1; i < neighbors.length; i++ ) {
			if ( neighbors[i]!=-1 && (neighbors[i] < seckey || neighbors[i] > seckey+4) ) {
				foreignerCount++;
			}
		}
		return foreignerCount;		
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
		
		updateFoodSeen(foodpresent,foodleft);
		this.age++;
		Move m = null; // placeholder for return value


		// if energy less than movement cost, stay put unless food adjacent
		if ( energyleft < this.game.v() ) {
			this.isReproducing = false;
			for ( int i = 1; i <= 4; i ++  ) {
				if (foodpresent[i] && neighbors[i] == -1) {
					m = new Move(i);
				}
			}
			m = new Move(STAYPUT);
		}

		
		if ( this.isReproducing ) {
			// There has to be a point at which reproducing no longer makes sense.
			// Some factor of the cost required to take a step
			// Possibly combined with how much food we saw?
			if ( energyleft <= this.game.v()*5 ) {
				this.isReproducing = false;
			} else {
				m = new Move(REPRODUCE, this.reproduceDirection, seckey+this.reproduceDirection);
				this.reproduceDirection++;
				if ( this.reproduceDirection > 4 ) {
					this.reproduceDirection = 1;
				}
			}
		}


		// if max energy, start reproducing like crazy
		if ( energyleft >= this.game.M()*reproducePct && getNeighborLocustCount(neighbors) < 1 ) {
			this.isReproducing = true;
		}

		// if we are on food, don't move!
		if ( foodleft > 0 && m == null ) {
			m = new Move(STAYPUT);
			if ( this.isGreedy ) {
				// Look for adjacent unoccupied food
				for ( int i = 0; i < neighbors.length; i++ ) {
					if ( neighbors[i] >= 0 && foodpresent[i] ) {
						// Move to food?
						m = new Move(i);
						// Reproduce onto food?
						//m = new Move(REPRODUCE, i, i);
						break;
					}
				}
			}
		}

		//System.out.println( "foreigner count " + getForeignerCount( neighbors ));
		if ( getForeignerCount( neighbors ) > 0 ) {
			this.isSuspicious = true;
			currentDirection = STAYPUT;
			// Start checking in random direction. May make a difference?
			int d = 1+rand.nextInt(4); // 1 to 4
			boolean isFood = false;
			for ( int i = d; i <= d+4; i++ ) {
				int nesw = (i%4) + 1;
				if ( foodpresent[nesw] && neighbors[nesw]==-1 ) {
					currentDirection = nesw;
					isFood = true;
					break;
				}
			}
			if ( !isFood ) {
				m = new Move(STAYPUT);
			}
		}
		
		
		if ( m == null ) {
			// this player selects moves in the same direction, changing only when food is detected
			/*
			if ( foodpresent[EAST] && neighbors[EAST]==-1 ) {
				currentDirection = EAST;
			} else if ( foodpresent[SOUTH] && neighbors[SOUTH]==-1 ) {
				currentDirection = SOUTH;
			} else if ( foodpresent[WEST] && neighbors[WEST]==-1 ) {
				currentDirection = WEST;
			} else if ( foodpresent[NORTH] && neighbors[NORTH]==-1 ) {
				currentDirection = NORTH;
			}
			*/
			// Start checking in random direction. May make a difference?
			int d = 1+rand.nextInt(4); // 1 to 4
			for ( int i = d; i <= d+4; i++ ) {
				int nesw = (i%4) + 1;
				if ( foodpresent[nesw] && neighbors[nesw]==-1 ) {
					currentDirection = nesw;
				}
			}
			
			int turnInterval = 1;
			if ( age > this.stepsCounted && foodPerStep() < 0.05 ) {
				//turnInterval = 10;
				//this.isGreedy = true;
				//this.reproducePct = 1.0;
				//if ( neighbors[currentDirection] >= 0 ) {
				//	currentDirection = turnRight();
				//}
			}
			if ( age % turnInterval == 0 ) {
				m = new Move(currentDirection);
			} else {
				m = new Move(STAYPUT);
			}
		}

		return m;
	}
}

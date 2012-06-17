package organisms.g5;

import java.awt.Color;
import java.util.Random;

import organisms.Move;
import organisms.OrganismsGame;
import organisms.Player;

public final class Wumpus implements Player {

	static final String _CNAME = "Random Player";
	static final Color _CColor = new Color(1.0f, 0.67f, 0.67f);
	private int state;
	private Random rand;
	private OrganismsGame game;


	/*
	 * This method is called when the Organism is created.
	 * The key is the value that is passed to this organism by its parent (not used here)
	 */
	public void register(OrganismsGame game, int key) throws Exception
	{
		rand = new Random();
		state = rand.nextInt(256);
		this.game = game;
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

		Move m = null; // placeholder for return value
		if(energyleft > game.M()*.9){
			int direction = rand.nextInt(4);
			// if this organism will reproduce:
			// the second argument to the constructor is the direction to which the offspring should be born
			// the third argument is the initial value for that organism's state variable (passed to its register function)
			if (direction == 0) return new Move(REPRODUCE, WEST, state);
			else if (direction == 1) return new Move(REPRODUCE, EAST, state);
			else if (direction == 2) return new Move(REPRODUCE, NORTH, state);
			else return new Move(REPRODUCE, SOUTH, state);
		}
			
		if(foodleft > 0)
			return new Move(STAYPUT);

		//game.print("food " + state + ":" + foodpresent[NORTH] + " " + foodpresent[EAST] + " " + foodpresent[SOUTH] + " " + foodpresent[WEST] + "\n");
		//game.print("neighbors " + state + ":" + neighbors[NORTH] + " " + neighbors[EAST] + " " + neighbors[SOUTH] + " " + neighbors[WEST] + "\n");		
		if(foodpresent[EAST] && neighbors[EAST] == -1) {
			return new Move(EAST);
		}
		if(foodpresent[SOUTH] && neighbors[SOUTH] == -1)
			return new Move(SOUTH);
		if(foodpresent[WEST] && neighbors[WEST] == -1)
			return new Move(WEST);
		if(foodpresent[NORTH] && neighbors[NORTH] == -1)
			return new Move(NORTH);
		
		
		
		// this player selects randomly
		int direction = rand.nextInt(4) + 1;
		
		switch (direction) {
		case 1: m = new Move(WEST); break;
		case 2: m = new Move(EAST); break;
		case 3: m = new Move(NORTH); break;
		case 4: m = new Move(SOUTH); break;
		}
		return m;
	}

}

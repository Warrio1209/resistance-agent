package utility;
import java.util.Set;
/*
 * @Author Callum Sullivan
 * @Version 3/11/2016
 * The mission class is intended to hold information about individual missions which have been completed, rejected or are currently being voted on
 */
public class Mission {
	public static final int PENDING = -1; //Mission currently being voted on
	public static final int REJECTED = -2; //Mission rejeted at voting stage
	private int betrayals; //number of spies on mission wh chose to betray
	private char proposer; //name of player who proposed the mission
	private Set<Character> participants; //set of players on the mission
	private Set<Character> yays; //set of players who voted yes on the mission
	/*Constructor which copies values of mission into this mission */
	//@param oldMission - the mission object this mission will be based on
	public Mission(Mission oldMission)
	{
		this.proposer = oldMission.getProposer();
		this.participants = oldMission.getParticipants();
		this.yays = oldMission.getVotes();
		this.betrayals = oldMission.getBetrayals();
	}
	/*Constructor which copies values of mission into this mission */
	//@param proposer - the player who proposed the mission
	//@param participants - players on the mission
	//@param yays - the players who voted yes on the mission
	//@param betrayals - the number of players on the mission who chose to betray
	public Mission(char proposer, Set<Character> participants, Set<Character> yays, int betrayals)
	{
		this.proposer = proposer;
		this.participants = participants;
		this.yays = yays;
		this.betrayals = betrayals;
	}
	//@param proposer - the player who proposed the mission
	//@param participants - players on the mission
	public Mission(char proposer, Set<Character> participants)
	{
		this.proposer = proposer;
		this.participants = participants;
		this.yays = null;
		this.betrayals = PENDING;
	}
	public int getBetrayals()
	{
		return this.betrayals;
	}
	public char getProposer()
	{
		return this.proposer;
	}
	public Set<Character> getParticipants()
	{
		return this.participants;
	}
	public Set<Character> getVotes()
	{
		return this.yays;
	}
	public void updateBetrayals(int betrayals)
	{
		this.betrayals = betrayals;
	}
	public void updateVotes(Set<Character> newVotes)
	{
		this.yays = newVotes;
	}
}

package agents;

import java.util.*;

import utility.Mission;
import utility.Player;
import utility.Utility;
/* ComboAgent is an agent that plays resistance
 * It implements the Agent Interface
 * It maintains all possible combinations of spies and attatches a likelihood rating to each
 * This rating is modified by voting habits, betrayals of missions, and proposals of missions in ways specified by the parameters
 * A genetic algorithm exists to find the optimal values for each of these parameters at each mission size
 * @Version 3/11/2016
 * @author Callum Sullivan
 */
public class TrustAgent implements Agent {
	private int mission = 0; //Current mission number
	private int failures; //number of failed missions
	private static final int NUM_SPIES[] = {2,2,3,3,3,4}; //spy numbers indexed by number of players
	//mission numbers indexed by number of players and mission number
	private static final int MISSION_SIZES[][] = {{2,3,2,3,3},{2,3,4,3,4},{2,3,3,4,4},{3,4,4,5,5},{3,4,4,5,5},{3,4,4,5,5}};
	private ArrayList<Mission> missions[]; //Missions proposed indexed by mission number, last one was actually completed
	private HashSet<Player> players; //Set of players in this game
	private HashSet<Player> spies; //Set of players who are spies - null if we are resistance
	private int proposalLoss; //weight of proposing a failed mission
	private int participationLoss; //weight of participating in a failed mission
	private int voteLoss; //weight of voting yes on a failed mission
	private int possibVoteLoss; //how much less suspicious an action would need to be to take it as a spy and cause a mission to succeed
	private Player me;
	/* Constructor
	 * @param proposalLoss - weight of proposing a failed mission
	 * @param participationLoss - weight of participating in a failed mission
	 * @param voteloss - weight of voting yes on a failed mission
	 * @param proposalThreshold - how much less suspicious an action would need to be to take it as a spy and cause a mission to succeed
	 */
	public TrustAgent(int proposalLoss, int participationLoss, int voteLoss, int possibVoteLoss)
	{
		this.proposalLoss = proposalLoss;
		this.participationLoss = participationLoss;
		this.voteLoss = voteLoss;
		this.possibVoteLoss = possibVoteLoss;
	}
	@Override
	//{@inheritDoc}
	//Updates mission, failures
	//If this is the first time this mission was called, initializes agent for this gameState
	public void get_status(String name, String players, String spies, int mission, int failures) {
		this.mission = mission;
		this.failures = failures;
		if(this.me == null)
		{
			this.initialise(name,players,spies);
		}
	}
	//@param  name - name of this player
	//@param players - String of all players in this game
	//@param spies - String of players who are spies in this game, consisting of appropriate number of question marks if we are resistance
	//Initialise game specific variables, including generating spy possibilities.
	public void initialise(String name, String players, String spies)
	{
		this.failures = 0;
		this.me = new Player(name.charAt(0));
		this.players = stringToPlayerSet(players);
		missions = new ArrayList [5];
		for(int i = 0; i< missions.length; i++)
		{
			missions[i] = new ArrayList<Mission>();
		}
		if(spies.charAt(0) != '?')
		{
			this.spies = this.stringToPlayerSet(spies);
		}
	}

	@Override
	//{@inheritDoc}
	//Nominate the mission containing ourselvs and the two players we are least suspicious of
	public String do_Nominate(int number) {
		String proposal = "";
		HashSet<Player> nomination = new HashSet<Player>(this.players);
		Player [] players = this.players.toArray(new Player[]{});
		Arrays.sort(players);	
		int i = players.length-1;
		nomination.add(me);
		while(nomination.size()<number)
		{
			nomination.add(players[i]);
			i--;
		}
		Iterator<Player>  nomIter = nomination.iterator();
		while(nomIter.hasNext())
		{
			proposal += nomIter.next().getPlayer();
		}
		return proposal;
	}
	//{@inheritDoc}
	//stores data about proposed mission
	@Override
	public void get_ProposedMission(String leader, String mission) {
		missions[this.mission-1].add(new Mission(leader.charAt(0),this.stringToSet(mission)));

	}
	
	
	//{@inheritDoc}
	//If resistance, vote no if mission contains 1 spy from most likely spy combo, otherwise vote yes
	//If spy vote in a way that makes the mission fail unless it would be too suspicious
	//always vote yes on first mission
	@Override
	public boolean do_Vote() {
		Mission proposed = new Mission(this.missions[this.mission-1].get(this.missions[this.mission-1].size()-1));
		if(proposed.getProposer() == this.me.getPlayer())//if this is the first mission, or I proposed this mission, vote yes
		{
			return true;
		}
		Player sortedPlayers [] = this.players.toArray(new Player[]{});
		Set<Character> participants = proposed.getParticipants();
		if(this.spies == null) // if resistance
		{
			return this.isApprovable(proposed);
		}
		else //if spy
		{
			boolean approvable = this.isApprovable(proposed);
			Player spyArray[] = this.spies.toArray(new Player[]{});
			Set<Player> spiesOnMission = new HashSet<Player>();
			for(int i = 0; i< spyArray.length; i++)
			{
				if(participants.contains(spyArray[i].getPlayer()))
				{
					spiesOnMission.add(spyArray[i]);
				}
			}
			if(spiesOnMission.size() > 0) //if there are no spies on this mission
			{
				if(approvable)// if I would vote true on this mission as resistance
				{
					return true;
				}
				return false;
			}
			else //if this mission does not
			{
				//if succeeding this mission would cost us the game, or this mission does not contain a player from the most likely spy combo
				if( !approvable || 5 - this.mission <= 3 - this.failures)
				{
					return false;
				}
				return true;
			}
		}
	}

	//{@inheritDoc}
	//update mission, if it didn't get accepted, update suspicions
	@Override
	public void get_Votes(String yays) {
		missions[this.mission-1].get(missions[this.mission-1].size()-1).updateVotes(this.stringToSet(yays));
		if(yays.length()<= this.players.size()/2 )//if the mission did not get accepted
		{
			missions[this.mission-1].get(missions[this.mission-1].size()-1).updateBetrayals(Mission.REJECTED);
			this.updateSuspicions(missions[this.mission-1].get(missions[this.mission-1].size()-1));
		}
	}

	//{@inheritDoc}
	@Override
	public void get_Mission(String mission) {
	}

	//{@inheritDoc}
	//if resistance never fail
	//if spy, fail unless the alternative makes us less suspicious enough and won't lose us the game
	@Override
	public boolean do_Betray() {
		return this.spies != null;
	}

	//{@inheritDoc}
	//Reevaluates spy combo likelihoods based on previous mission
	@Override
	public void get_Traitors(int traitors) {
		missions[mission-1].get(missions[mission-1].size()-1).updateBetrayals(traitors);
		this.updateSuspicions(missions[mission-1].get(missions[mission-1].size()-1));
	}
	//{@inheritDoc}
	//I don't think enough agents will use this properly to make it useful
	@Override
	public String do_Accuse() {return "";}

	//{@inheritDoc}
	//I don't think enough agents will use this properly to make it useful
	@Override
	public void get_Accusation(String accuser, String accused) {}
	
	private boolean isApprovable(Mission mission)
	{
		Player sortedPlayers [] = this.players.toArray(new Player[]{});
		Set<Character> participants = mission.getParticipants();
		for(int i = 0; i< NUM_SPIES[this.players.size()];i++)
		{
			if(participants.contains(sortedPlayers[i].getPlayer()))
			{
				return false;
			}
		}
		return true;
	}
	
	private void updateSuspicions(Mission mission)
	{
		int betrayals = mission.getBetrayals();
		Set<Character> participants = mission.getParticipants();
		Set<Character> votes = mission.getVotes();
		Iterator<Player> playerIter = this.players.iterator();
		boolean approvable = this.isApprovable(mission);
		while(playerIter.hasNext())
		{
			Player currPlayer = playerIter.next();
			if(currPlayer.getPlayer() != this.me.getPlayer())
			{
				if(betrayals>0)
				{
					if(participants.contains(currPlayer.getPlayer()))
					{
						if(participants.contains(me.getPlayer()))
						{
							
							currPlayer.addSuspicion(this.participationLoss/(participants.size()-1));
						}
						else
						{
							currPlayer.addSuspicion(this.participationLoss/participants.size());
						}
					}
					if(votes != null)
					{
						if(votes.contains(currPlayer.getPlayer()))
						{
							currPlayer.addSuspicion(this.voteLoss);
						}
						if(mission.getProposer() == currPlayer.getPlayer())
						{
							currPlayer.addSuspicion(this.participationLoss);
						}
					}
				}
				else if(betrayals == 0 && votes !=null)
				{
					if(!votes.contains(currPlayer.getPlayer()))
					{
						currPlayer.addSuspicion(this.voteLoss);
					}
				}
				if(votes != null)
				{
					if(votes.contains(currPlayer.getPlayer()) != approvable)
					{
						currPlayer.addSuspicion(possibVoteLoss);
					}
				}
			}
		}
	}
	
	//converts a string to a set, probably should be static method in utility
	//@param string to be converted
	//@return HashSet<Character> containing all characters in the string and nothing else
	private HashSet<Character> stringToSet(String str)
	{
		HashSet<Character> set = new HashSet<Character>();
		for(int i = 0; i< str.length(); i++)
		{
			set.add(str.charAt(i));
		}
		return set;
	}
	
	//converts a string to a set, probably should be static method in utility
	//@param string to be converted
	//@return HashSet<Player> containing all characters in the string and nothing else
	private HashSet<Player> stringToPlayerSet(String str)
	{
		HashSet<Player> set = new HashSet<Player>();
		for(int i = 0; i< str.length(); i++)
		{
			set.add(new Player(str.charAt(i)));
		}
		return set;
	}
	
	public int getProposalLoss() {
		return proposalLoss;
	}
	
	public int getParticipationLoss() {
		return this.participationLoss;
	}
	
	public int getVoteLoss() {
		return this.voteLoss;
	}

	public float getpossibVoteLoss() {
		return this.possibVoteLoss;
	}

	

	@Override
	public String toString()
	{
		String str = "";
		str +=  this.proposalLoss +" "+  this.participationLoss+" "+ this.possibVoteLoss+" "+ this.possibVoteLoss;
		return str;
	}

	
	
}
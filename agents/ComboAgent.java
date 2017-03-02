package agents;

import java.util.*;

import utility.Mission;
import utility.SpyCombo;
import utility.Utility;
/* ComboAgent is an agent that plays resistance
 * It implements the Agent Interface
 * It maintains all possible combinations of spies and attatches a likelihood rating to each
 * This rating is modified by voting habits, betrayals of missions, and proposals of missions in ways specified by the parameters
 * A genetic algorithm exists to find the optimal values for each of these parameters at each mission size
 * @Version 3/11/2016
 * @author Callum Sullivan
 */
public class ComboAgent implements Agent {
	private int mission = 0; //Current mission number
	private int failures; //number of failed missions
	private Character name; //name of player this agent is playing
	private HashSet<SpyCombo> possibilities; //possible combinations of spies
	private static final int NUM_SPIES[] = {2,2,3,3,3,4}; //spy numbers indexed by number of players
	//mission numbers indexed by number of players and mission number
	private static final int MISSION_SIZES[][] = {{2,3,2,3,3},{2,3,4,3,4},{2,3,3,4,4},{3,4,4,5,5},{3,4,4,5,5},{3,4,4,5,5}};
	private static final int CERTANITY = 100; //Weight added if agent is certain. Constant so algorithm can adjust around it if need be.
	private ArrayList<Mission> missions[]; //Missions proposed indexed by mission number, last one was actually completed
	private HashSet<Character> players; //Set of players in this game
	private HashSet<Character> spies; //Set of players who are spies - null if we are resistance
	//Weights of different aspects of the game when deciding spycombo likelihood, positive means more likely, negative means less likely
	private int proposalLoss; //weight of proposing a failed mission
	private int participationLoss; //weight of participating in a failed mission
	private int voteLoss; //weight of voting yes on a failed mission
	private int voteSuccess; //weight of voting no on a succeeded mission
	private float proposalThreshold; //how much less suspicious an action would need to be to take it as a spy and cause a mission to succeed
	private int singleFailureProposal; //weight of proposing a mission which exactly one person fails, the best case for spies
	private int nonSelfProposal; //weight of proposing a mission not containing yourself
	private int nonSelfVote; //weight of voting yes on a mission not containing yourself
	private int spiesSucceeded; //weight of spies having to have chosen to succeed a mission
	private int notApprovable; //weight of voting yes on a mission this agent believes contains spies
	private int failedApproved; //weight of voting no on a mission this agent believes does not contain spies
	/* Constructor
	 * @param proposalLoss - weight of proposing a failed mission
	 * @param participationLoss - weight of participating in a failed mission
	 * @param voteloss - weight of voting yes on a failed mission
	 * @param voteSuccess - weight of voting no on a succeeded mission
	 * @param proposalThreshold - how much less suspicious an action would need to be to take it as a spy and cause a mission to succeed
	 * @param singleFailureProposal - weight of proposing a mission which exactly one person fails, the best case for spies
	 * @param nonSelfProposal - weight of proposing a mission not containing yourself
	 * @param nonSelfVote - weight of voting yes on a mission not containing yourself
	 * @param spiesSucceeded - weight of spies having to have chosen to succeed a mission
	 * @param notApprovable - weight of voting yes on a mission this agent believes contains spies
	 * @param failedApproved - weight of voting no on a mission this agent believes does not contain spies
	 */
	public ComboAgent(int proposalLoss, int participationLoss, int voteLoss, float proposalThreshold,int singleFailureProposal,
			int voteSuccess, int nonSelfProposal, int nonSelfVote, int spiesSucceeded, int notApprovable, int failedApproved)
	{
		this.proposalLoss = proposalLoss;
		this.participationLoss = participationLoss;
		this.voteLoss = voteLoss;
		this.proposalThreshold = proposalThreshold;
		this.singleFailureProposal = singleFailureProposal;
		this.voteSuccess = voteSuccess;
		this.nonSelfProposal = nonSelfProposal;
		this.nonSelfVote = nonSelfVote;
		this.spiesSucceeded = spiesSucceeded;
		this.notApprovable = notApprovable;
		this.failedApproved = failedApproved;
	}
	@Override
	//{@inheritDoc}
	//Updates mission, failures
	//If this is the first time this mission was called, initializes agent for this gameState
	public void get_status(String name, String players, String spies, int mission, int failures) {
		this.mission = mission;
		this.failures = failures;
		if(this.name == null)
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
		this.name = name.charAt(0);
		this.players = stringToSet(players);
		missions = new ArrayList [5];
		for(int i = 0; i< missions.length; i++)
		{
			missions[i] = new ArrayList<Mission>();
		}
		if(spies.charAt(0) != '?')
		{
			this.spies = this.stringToSet(spies);
		}
		Character[] possSpies;
		if(this.spies == null)
		{
			HashSet<Character> temp = new HashSet<Character>(this.players);
			temp.remove(this.name);
			possSpies = temp.toArray(new Character[]{});
		}
		else
		{
			possSpies = this.players.toArray(new Character[]{});;
		}
		Set<Set<Character>> combos = generatePossibleCombos(possSpies,NUM_SPIES[players.length()-5]);
		Iterator<Set<Character>> comboIterator = combos.iterator();
		possibilities = new HashSet<SpyCombo>();
		while(comboIterator.hasNext())
		{
			possibilities.add(new SpyCombo(comboIterator.next()));
		}
	}
	
	@Override
	//{@inheritDoc}
	//If resistance, nominates mission containing players least likely to be spies as determined by likelihoods of spycombos
	//If spy, nominates mission containing spies it thinks will get through that will fail that gives least information to resistance
	//Can also nominate a mission it thinks will obscure the truth enough if doing so won't lose the game and doing otherwise wouldn'w win the game
	public String do_Nominate(int number) {
		String proposal = "";
		SpyCombo sortedPossibilities [] = this.possibilities.toArray(new SpyCombo[]{});
		HashSet<Character> nomination = new HashSet<Character>(this.players);
		Arrays.sort(sortedPossibilities);
		if(this.spies == null) //If Resistance
		{
			int i = 0;
			Iterator<Character> possibIterator = sortedPossibilities[0].getSpies().iterator();
			while(nomination.size() > number && (i<sortedPossibilities.length || possibIterator.hasNext())) //while set is larger than mission and possible spies remain
			{
				//Remove next most likely spy from set
				if(!possibIterator.hasNext())
					possibIterator = sortedPossibilities[i].getSpies().iterator();
				nomination.remove(possibIterator.next());
				if(!possibIterator.hasNext())
					i++;
			}
			Iterator<Character> finalIter = nomination.iterator();
			while(finalIter.hasNext())
			{
				//propose mission containing first (missionNum) players from set
				proposal += finalIter.next();
			}
		}
		else //if spy
		{
			float bestApprovableFailVal = -1, bestApprovableVal = -1, bestFailVal = -1, bestVal = -1;
			Mission bestApprovableFail = null, bestApprovable = null, bestFail = null, best = null;
			HashSet<Set<Character>> possibleMissions = generatePossibleCombos(this.players.toArray((new Character[]{})),number);
			Iterator<Set<Character>> missionIterator = possibleMissions.iterator();
			while(missionIterator.hasNext()) //for each possible proposed mission
			{
				Set<Character> current = missionIterator.next();
				Mission currentMission = new Mission(this.name, current);
				int spiesOnMission = 0;
				Iterator<Character> currIter = current.iterator();
				boolean approvable = true;
				while(currIter.hasNext()) //for each player on mission
				{
					Character currChar = currIter.next();
					if(this.spies.contains(currChar)) //if mission contains a spy
					{
						spiesOnMission++;
					}
					if(sortedPossibilities[0].getSpies().contains(currChar)) //if mission contains a player from most likely spy combo
					{
						approvable = false;
					}
				}
				currentMission.updateBetrayals(spiesOnMission);
				SpyCombo aftermath[] = evaluatePossibilities(possibilities,currentMission ).toArray(new SpyCombo[]{});//get likelihoods if mission succeeds
				Arrays.sort(aftermath); //sort by likelihood
				//find relative likelihood of actual spies
				int actualPos = 0;
				while(actualPos < aftermath.length)
				{
					if(aftermath[actualPos].getSpies().equals(this.spies))
					{
						break;
					}
					actualPos++;
				}
				if((float)actualPos /aftermath.length >bestVal) //if this is the best relative value we've found
				{
					bestVal = (float)actualPos/aftermath.length;
					best = currentMission;
				}
				if(spiesOnMission > 0 && (float)actualPos /aftermath.length >bestFailVal) // if this is the best relative value we've found after failing a mission
				{
					bestFailVal = (float)actualPos /aftermath.length;
					bestFail = currentMission;
				}
				//if this is the best relative value we've found that doesn't contain anyone from the most likely spy combo
				if((float)actualPos /aftermath.length >bestApprovableVal && approvable)
				{
					bestApprovableVal = (float)actualPos/aftermath.length;
					bestApprovable = currentMission;
				}
				//if this is the best relative value we've found that doesn't contain anyone from the most likely spy combo and fails
				if(spiesOnMission > 0 && (float)actualPos /aftermath.length >bestFailVal && approvable)
				{
					bestApprovableFailVal = (float)actualPos /aftermath.length;
					bestApprovableFail = currentMission;
				}
				if(spiesOnMission == 1 && nomination.contains(this.name)) //if we choose whether or not mission succeeds or fails
				{
					//reevaluate non-failing values for the case we choose to succeed mission
					Mission currSucceeded = new Mission(currentMission);
					currSucceeded.updateBetrayals(0);
					SpyCombo succeededAftermath[] = evaluatePossibilities(possibilities,currSucceeded ).toArray(new SpyCombo[]{});
					Arrays.sort(succeededAftermath);
					int successPos = 0;
					while(successPos < succeededAftermath.length)
					{
						if(succeededAftermath[successPos].getSpies().equals(this.spies))
						{
							break;
						}
						successPos++;
					}
					if((float)successPos /succeededAftermath.length >bestVal)
					{
						bestVal = (float)successPos/succeededAftermath.length;
						best = currentMission;
					}
					if((float)successPos /succeededAftermath.length >bestApprovableVal && approvable)
					{
						bestApprovableVal = (float)successPos/succeededAftermath.length;
						bestApprovable = currentMission;
					}
				}
			}
			Iterator <Character> proposalIterator;
			//if succeeding this mission doesn't cost us the game and there's a mission that obscures the truth enough and doesn't contain a player from the most
			//likely spy combo
			if(this.failures < 2 && 5 - this.mission > 3 - this.failures && bestApprovableVal - bestApprovableFailVal > this.proposalThreshold)
			{
				proposalIterator = bestApprovable.getParticipants().iterator();
			}
			else if(bestApprovableFail != null) //otherwise if there is no mission that will fail and doesn't contain a player from the most likely spycombo
			{
				proposalIterator = bestFail.getParticipants().iterator();
			}
			//otherwise if succeeding this mission doesn't cost us the game and there's a mission that obscures the truth enough
			else if(this.failures < 2 && 5 - this.mission > 3 - this.failures && bestVal - bestFailVal > this.proposalThreshold)
			{
				proposalIterator = best.getParticipants().iterator();
			}
			else //otherwise
			{
				proposalIterator = bestFail.getParticipants().iterator();
			}
			while(proposalIterator.hasNext()) //for each player in proposed mission
			{
				proposal += proposalIterator.next();
			}
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
		if(proposed.getProposer() == this.name)//if this is the first mission, or I proposed this mission, vote yes
		{
			return true;
		}
		SpyCombo sortedPossibilities [] = this.possibilities.toArray(new SpyCombo[]{});
		Set<Character> participants = proposed.getParticipants();
		if(this.spies == null) // if resistance
		{
			Arrays.sort(sortedPossibilities);
			HashSet<Character> determinant = new HashSet<Character>(participants);
			determinant.retainAll(sortedPossibilities[0].getSpies());
			if(determinant.size() > 0)
			{
				return false;
			}
			return true;
		}
		else //if spy
		{
			boolean approvable = true;
			int spiesOnMission = 0;
			Iterator<Character> charIter = participants.iterator();
			while(charIter.hasNext()) //for each character in mission
			{
				Character current = charIter.next();
				if(this.spies.contains(current)) //if this player is a spy
				{
					spiesOnMission++;
				}
				if(sortedPossibilities[0].getSpies().contains(current)) //if this player is part of the mos likely spy combo
				{
					approvable = false;
				}
			}
			if(spiesOnMission == 0) //if there are no spies on this mission
			{
				//if succeeding this mission would cost us the game or this mission contains a player from the most likely spy combo
				if(5 - this.mission <= 3 - this.failures || !approvable || this.failures == 2)
				{
					return false;
				}
				return true;
			}
			else //if this mission does contain a spy
			{
				//if succeeding this mission would cost us the game, or this mission does not contain a player from the most likely spy combo
				if(!this.spies.contains(proposed.getProposer()) || approvable || 5 - this.mission <= 3 - this.failures)
				{
					return true;
				}
				return false;
			}
		}
	}

	//{@inheritDoc}
	//update mission, if it didn't get accepted, update spycombo likelihoods
	@Override
	public void get_Votes(String yays) {
		missions[this.mission-1].get(missions[this.mission-1].size()-1).updateVotes(this.stringToSet(yays));
		if(yays.length()<= this.players.size()/2 )//if the mission did not get accepted
		{
			missions[this.mission-1].get(missions[this.mission-1].size()-1).updateBetrayals(Mission.REJECTED);
			this.possibilities = evaluatePossibilities(this.possibilities, missions[mission-1].get(missions[mission-1].size()-1));
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
		if(spies == null)//if resistance
		{
			return false;
		}
		else //if spy
		{
			if(this.failures == 2 || 5-this.mission <= 3-this.failures)//if succeeding this mission would cost us the game
			{
				return true;
			}
			Mission currMission = new Mission(missions[this.mission-1].get(missions[this.mission-1].size()-1));
			int spiesOnMission = 0;
			Iterator<Character> charIter = currMission.getParticipants().iterator();
			while(charIter.hasNext())//for each player on mission
			{
				Character current = charIter.next();
				if(this.spies.contains(current))
				{
					spiesOnMission++;
				}
			}
			currMission.updateBetrayals(spiesOnMission);
			SpyCombo[] failAftermath = evaluatePossibilities(this.possibilities,currMission).toArray(new SpyCombo[]{});
			Arrays.sort(failAftermath);
			int failPos = 0;
			while(failPos < failAftermath.length)//for each combo
			{
				if(failAftermath[failPos].getSpies().equals(this.spies))//if this combo is the actual set of spies
				{
					break;
				}
				failPos++;
			}
			float failVal = failPos/failAftermath.length;
			currMission.updateBetrayals(spiesOnMission-1);
			SpyCombo[] successAftermath =  evaluatePossibilities(this.possibilities,currMission).toArray(new SpyCombo[]{});
			Arrays.sort(successAftermath);
			int successPos = 0;
			while(successPos < successAftermath.length)//for each combo
			{
				if(successAftermath[successPos].getSpies().equals(this.spies))//if this combo is the actual
				{
					break;
				}
				successPos++;
			}
			float successVal = successPos/successAftermath.length;
			if(successVal - failVal > proposalThreshold)//if succeeding this mission obscures the truth enough
				return false;
			return true;
		}
	}

	//{@inheritDoc}
	//Reevaluates spy combo likelihoods based on previous mission
	@Override
	public void get_Traitors(int traitors) {
		missions[mission-1].get(missions[mission-1].size()-1).updateBetrayals(traitors);
		this.possibilities = evaluatePossibilities(this.possibilities, missions[mission-1].get(missions[mission-1].size()-1));
	}
	//{@inheritDoc}
	//I don't think enough agents will use this properly to make it useful
	@Override
	public String do_Accuse() {return "";}

	//{@inheritDoc}
	//I don't think enough agents will use this properly to make it useful
	@Override
	public void get_Accusation(String accuser, String accused) {}
	//This method generates all possible k choose n combinations disregarding order and without duplicates where possSpies is k and numSpies is n
	//@param possSpies - the domain of the function k choose n
	//@param numSpies the length of the returned values
	//@return the range of the function
	public  HashSet<Set<Character>> generatePossibleCombos(Character[] possSpies, int numSpies)
	{
		char currentCombo[] = new char [numSpies];
		Arrays.sort(possSpies);
		Iterator <Character> unused[] =  new Iterator[numSpies];
		HashSet<Character> allowed = new HashSet<Character>();
		for(int i = 0; i< possSpies.length; i++)
		{
			allowed.add(possSpies[i]);
		}
		HashSet<Set<Character>> possibilities = new HashSet<Set<Character>>();
		int currIndex = 0; 
		while(currIndex>=0)
		{
			if(unused[currIndex] == null)
			{
				unused[currIndex] = ((HashSet<Character>) allowed.clone()).iterator();
			}
			if(unused[currIndex].hasNext())
			{
				if(currentCombo[currIndex] != '\u0000')
				{
					allowed.add(currentCombo[currIndex]);
				}
				currentCombo[currIndex] = unused[currIndex].next();
				allowed.remove(currentCombo[currIndex]);
				if(currIndex < currentCombo.length-1)
				{
					currIndex++;
					unused[currIndex] = ((HashSet<Character>) allowed.clone()).iterator();
				}
				else
				{
					Set<Character> newPossibility = new HashSet<Character>();
					for(int i = 0; i< currentCombo.length; i++)
					{
						newPossibility.add(currentCombo[i]);
					}
					possibilities.add(newPossibility);
				}
			}
			else
			{
				allowed.add(currentCombo[currIndex]);
				currIndex--;
			}
		}
		return possibilities;
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
	//Re-evaluates possible spycombinations based on a mission
	//This is my main heuristic function which I base all my decisions on. Both the spy and the resistance make use of this, details to follow 
	//@param
	private HashSet<SpyCombo> evaluatePossibilities(HashSet<SpyCombo> priorPossibilities, Mission change)
	{
		Set<Character> participants = change.getParticipants();
		Set<Character> votes = change.getVotes();
		int betrayals = change.getBetrayals();
		HashSet<SpyCombo> postProbabilities = new HashSet<SpyCombo>();
		Iterator<SpyCombo> preIterator = priorPossibilities.iterator();
		SpyCombo sortedPriorPossibilities[] = priorPossibilities.toArray(new SpyCombo[]{});
		Arrays.sort(sortedPriorPossibilities);
		boolean approvable = true;
		Iterator<Character> participantsIterator = participants.iterator();
		while(participantsIterator.hasNext())//for each participant in mission
		{
			if(sortedPriorPossibilities[0].getSpies().contains(participantsIterator.next()))// if the mission contains members of the most likely spycombo
			{
				approvable = false;
			}
		}
		while(preIterator.hasNext()) //for each spycombo currently considered
		{
			int ayes = 0;
			int spiesOnMission = 0;
			int nonParticipantAyes = 0;
			int maxResistanceNotParticipating =  this.players.size() -NUM_SPIES[this.players.size()-5] - participants.size();
			SpyCombo curr = preIterator.next();
			Iterator<Character> currIter = curr.getSpies().iterator();
			while(currIter.hasNext())//for each player in currently considered spycombo
			{
				Character currentCharacter = currIter.next(); 
				if(participants.contains(currentCharacter))
				{
					spiesOnMission++;
				}
				if(votes != null) //if the mission has been voted on
				{
					if(votes.contains(currentCharacter)) //if the current player voted aye
					{
						ayes++;
						if(!participants.contains(currentCharacter))//if the mission does not contain the current character
						{
							nonParticipantAyes ++;
						}
					}
				}
			}
			if(spiesOnMission>=betrayals)//if there were at least as many spies on the mission as betrayed the mission the combo is possible
			{
				SpyCombo newCombo = new SpyCombo(curr);
				if(approvable && spiesOnMission == 0)//if the mission was approvable and no spies were on the mission
				{
					newCombo.addLikelihood(this.failedApproved*(newCombo.getSpies().size() - ayes));
				}
				else if(spiesOnMission > 0) //otherwise is there were spies on this mission
				{
					newCombo.addLikelihood(ayes * this.notApprovable);
				}
				if(betrayals >= 0)//if this mission was betrayed
				{
					newCombo.addLikelihood(this.spiesSucceeded * (spiesOnMission - betrayals));
				}
				if(nonParticipantAyes != 0 && spiesOnMission > 0) //if someone not on this mission voted aye and this mission had a spy on it
				{
					if(maxResistanceNotParticipating > 0) //if it is possible for a resistance member not to be on the mission and the mission to still succeed
					{
						newCombo.addLikelihood(nonParticipantAyes*this.nonSelfVote/maxResistanceNotParticipating);
					}
					else//otherwise
					{
						newCombo.addLikelihood(nonParticipantAyes*this.nonSelfVote*CERTANITY);
					}
				}
				if(curr.getSpies().contains(change.getProposer()))//if the proposer was a spy
				{
					if(this.nonSelfProposal != 0 && !participants.contains(change.getProposer()) && spiesOnMission >0)//if the proposed mission contained a spy but not the proposer
					{
						if(maxResistanceNotParticipating > 0)//if it is possible for a resistance member not to be on the mission and the mission to still succeed
						{
							newCombo.addLikelihood(this.nonSelfProposal/maxResistanceNotParticipating);
						}
						else //otherwise
						{
							newCombo.addLikelihood(this.nonSelfProposal * CERTANITY);
						}
					}
					if(betrayals == 1)//if exactly one person betrayed the mission
					{
						newCombo.addLikelihood(this.singleFailureProposal);
					}
					else if(betrayals > 0)//otherwise, if at least 2 people betrayed the mission
					{
					newCombo.addLikelihood(this.proposalLoss);
					}
				}
				if(betrayals > 0)//if at least one person betrayed the mission
				{
					newCombo.addLikelihood(ayes * this.voteLoss);
					newCombo.addLikelihood(ayes);
					if(this.participationLoss != 0)//if we take into account being on a failed mission
					{
						newCombo.addLikelihood((double)betrayals * (double)this.participationLoss/(double)participants.size());
					}
				}
				else if(betrayals == 0)//otherwise, if noone betrayed the mission
				{
					newCombo.addLikelihood(newCombo.getSpies().size()-ayes* voteSuccess);
				}
				postProbabilities.add(newCombo);
			}
		}
		return postProbabilities;
	}
	public int getProposalLoss() {
		return proposalLoss;
	}
	
	public int getParticipationLoss() {
		return participationLoss;
	}
	
	public int getVoteLoss() {
		return voteLoss;
	}
	
	public int getVoteSuccess() {
		return voteSuccess;
	}
	
	public float getProposalThreshold() {
		return proposalThreshold;
	}
	
	public int getSingleFailureProposal() {
		return singleFailureProposal;
	}
	
	public int getNonSelfProposal() {
		return nonSelfProposal;
	}
	
	public int getNonSelfVote() {
		return nonSelfVote;
	}
	
	public int getSpiesSucceeded() {
		return spiesSucceeded;
	}
	
	public int getNotApprovable() {
		return notApprovable;
	}
	
	public int getFailedApproved() {
		return failedApproved;
	}
	@Override
	public String toString()
	{
		String str = "";
		str +=  proposalLoss +" "+  participationLoss+" "+ voteLoss+" "+ proposalThreshold+" "+ singleFailureProposal+" "+ voteSuccess+" "+ nonSelfProposal+" "+ nonSelfVote
				+" "+ spiesSucceeded+" "+ notApprovable+" "+ failedApproved;
		return str;
	}
	public HashSet<SpyCombo> getPossibilities() {
		return possibilities;
	}
	
	
}

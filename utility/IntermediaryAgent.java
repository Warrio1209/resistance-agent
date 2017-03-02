package utility;

import agents.Agent;
import agents.ComboAgent;

public class IntermediaryAgent implements Agent {
	
	private Agent backend;//the agent which actually makes all the decisions
	
	public IntermediaryAgent(){}//noArgs constructor
	//initialises parameters of actual agent based on number of players and whether or not we are resisitance
	@Override
	public void get_status(String name, String players, String spies, int mission, int failures) {
		if(this.backend == null)
		{
			//hardcode these based on results from genetic algorithm.
			this.backend = new ComboAgent(50,50,50,(float)0.5,50,50,50,50,50,50,50);
			switch(players.length())
			{
			case 5:
				if(spies.charAt(0) == '?')
				{
				}
				else
				{
					
				}
				break;
			case 6:
				if(spies.charAt(0) == '?')
				{
					
				}
				else
				{
					
				}
				break;
			case 7:
				if(spies.charAt(0) == '?')
				{
					
				}
				else
				{
					
				}
				break;
			case 8:
				if(spies.charAt(0) == '?')
				{
					
				}
				else
				{
					
				}
				break;
			case 9:
				if(spies.charAt(0) == '?')
				{
					
				}
				else
				{
					
				}
				break;
			case 10:
				if(spies.charAt(0) == '?')
				{
					
				}
				else
				{
					
				}
				break;
			default:
				if(spies.charAt(0) == '?')
				{
					
				}
				else
				{
					
				}
				break;
			}
		}
	}

	@Override
	public String do_Nominate(int number) {
		return this.backend.do_Nominate(number);
	}

	@Override
	public void get_ProposedMission(String leader, String mission) {
		this.backend.get_ProposedMission(leader, mission);
	}

	@Override
	public boolean do_Vote() {
		return this.backend.do_Vote();
	}

	@Override
	public void get_Votes(String yays) {
		this.backend.get_Votes(yays);

	}

	@Override
	public void get_Mission(String mission) {
		this.backend.get_Mission(mission);

	}

	@Override
	public boolean do_Betray() {
		return this.backend.do_Betray();
	}

	@Override
	public void get_Traitors(int traitors) {
		this.backend.get_Traitors(traitors);

	}

	@Override
	public String do_Accuse() {
		return this.backend.do_Accuse();
	}

	@Override
	public void get_Accusation(String accuser, String accused) {
		this.backend.get_Accusation(accuser, accused);

	}

}

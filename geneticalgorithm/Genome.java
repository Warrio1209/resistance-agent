package geneticalgorithm;

import java.util.Random;

import agents.ComboAgent;

public class Genome implements Comparable{
	private ComboAgent agent;
	private int gamesPlayed;
	private int gamesWon;
	public Genome(ComboAgent agent)
	{
		this.agent = agent;
		this.gamesPlayed = 0;
		this.gamesWon = 0;
	}
	public Genome(Genome old)
	{
		this.agent = old.getAgent();
		this.gamesPlayed = old.getGamesPlayed();
		this.gamesWon = old.getGamesWon();
	}
	
	public ComboAgent getAgent()
	{
		return this.agent;
	}
	public int getGamesPlayed()
	{
		return this.gamesPlayed;
	}
	public int getGamesWon()
	{
		return this.gamesWon;
	}
	public double getWinrate()
	{
		return (double)this.gamesWon/(double)this.gamesPlayed;
	}
	public void playedGame(boolean won)
	{
		this.gamesPlayed++;
		if(won)
			this.gamesWon++;
	}
	@Override
	public int compareTo(Object arg0) {
		Genome g = (Genome) arg0;
		if(this.getWinrate() > g.getWinrate())
		{
			return -1;
		}
		else if(this.getWinrate()< g.getWinrate())
		{
			return 1;
		}
		return 0;
	}
	public Genome crossbreed(Genome g)
	{
		ComboAgent a1 = g.getAgent();
		Random rand = new Random(System.currentTimeMillis());
		int proposalLoss,  participationLoss,  voteLoss, singleFailureProposal,
		 voteSuccess,  nonSelfProposal,  nonSelfVote,  spiesSucceeded,  notApprovable, failedApproved;
		float proposalThreshold;
		if(rand.nextBoolean())
			proposalLoss = this.agent.getProposalLoss();
		else
			proposalLoss = a1.getProposalLoss();
		if(rand.nextBoolean())
			participationLoss = this.agent.getParticipationLoss();
		else
			participationLoss = a1.getParticipationLoss();
		if(rand.nextBoolean())
			voteLoss = this.agent.getVoteLoss();
		else
			voteLoss = a1.getVoteLoss();
		if(rand.nextBoolean())
			proposalThreshold = this.agent.getProposalThreshold();
		else
			proposalThreshold = a1.getProposalThreshold();
		if(rand.nextBoolean())
			singleFailureProposal = this.agent.getSingleFailureProposal();
		else
			singleFailureProposal = a1.getSingleFailureProposal();
		if(rand.nextBoolean())
			voteSuccess = this.agent.getVoteSuccess();
		else
			voteSuccess = a1.getVoteSuccess();
		if(rand.nextBoolean())
			nonSelfProposal = this.agent.getNonSelfProposal();
		else
			nonSelfProposal = a1.getNonSelfProposal();
		if(rand.nextBoolean())
			nonSelfVote = this.agent.getNonSelfVote();
		else
			nonSelfVote = a1.getNonSelfVote();
		if(rand.nextBoolean())
			spiesSucceeded = this.agent.getSpiesSucceeded();
		else
			spiesSucceeded = a1.getSpiesSucceeded();
		if(rand.nextBoolean())
			notApprovable = this.agent.getNotApprovable();
		else
			notApprovable = a1.getNotApprovable();
		if(rand.nextBoolean())
			failedApproved = this.agent.getFailedApproved();
		else
			failedApproved = a1.getFailedApproved();
		return new Genome(new ComboAgent(proposalLoss,  participationLoss,  voteLoss,proposalThreshold, singleFailureProposal,
				 voteSuccess,  nonSelfProposal,  nonSelfVote,  spiesSucceeded,  notApprovable, failedApproved));
	}
	public Genome mutate(float mutationSeverity,float mutationLikelihood)
	{
		Random rand = new Random(System.currentTimeMillis());
		int proposalLoss,  participationLoss,  voteLoss, singleFailureProposal,
		 voteSuccess,  nonSelfProposal,  nonSelfVote,  spiesSucceeded,  notApprovable, failedApproved;
		float proposalThreshold;
		if(rand.nextFloat()>=mutationLikelihood)
			proposalLoss = this.agent.getProposalLoss();
		else
			proposalLoss = (int) (this.agent.getProposalLoss() + (rand.nextInt()%(GeneticAlgorithm.RANGE*mutationSeverity)));
		if(rand.nextFloat()>=mutationLikelihood)
			participationLoss = this.agent.getParticipationLoss();
		else
			participationLoss = (int) (this.agent.getParticipationLoss() + (rand.nextInt()%(GeneticAlgorithm.RANGE*mutationSeverity)));
		if(rand.nextFloat()>=mutationLikelihood)
			voteLoss = this.agent.getVoteLoss();
		else
			voteLoss = (int) (this.agent.getVoteLoss() + (rand.nextInt()%(GeneticAlgorithm.RANGE*mutationSeverity)));
		if(rand.nextFloat()>=mutationLikelihood)
			proposalThreshold = this.agent.getProposalThreshold();
		else
			proposalThreshold = this.agent.getProposalThreshold() - 1*mutationSeverity + rand.nextFloat()*mutationSeverity*2;
		if(rand.nextFloat()>=mutationLikelihood)
			singleFailureProposal = this.agent.getSingleFailureProposal();
		else
			singleFailureProposal = (int) (this.agent.getSingleFailureProposal() + (rand.nextInt()%(GeneticAlgorithm.RANGE*mutationSeverity)));
		if(rand.nextFloat()>=mutationLikelihood)
			voteSuccess = this.agent.getVoteSuccess();
		else
			voteSuccess = (int) (this.agent.getVoteSuccess() + (rand.nextInt()%(GeneticAlgorithm.RANGE*mutationSeverity)));
		if(rand.nextFloat()>=mutationLikelihood)
			nonSelfProposal = this.agent.getNonSelfProposal();
		else
			nonSelfProposal = (int) (this.agent.getNonSelfProposal() + (rand.nextInt()%(GeneticAlgorithm.RANGE*mutationSeverity)));
		if(rand.nextFloat()>=mutationLikelihood)
			nonSelfVote = this.agent.getNonSelfVote();
		else
			nonSelfVote = (int) (this.agent.getNonSelfVote() + (rand.nextInt()%(GeneticAlgorithm.RANGE*mutationSeverity)));
		if(rand.nextFloat()>=mutationLikelihood)
			spiesSucceeded = this.agent.getSpiesSucceeded();
		else
			spiesSucceeded = (int) (this.agent.getSpiesSucceeded() + (rand.nextInt()%(GeneticAlgorithm.RANGE*mutationSeverity)));
		if(rand.nextFloat()>=mutationLikelihood)
			notApprovable = this.agent.getNotApprovable();
		else
			notApprovable = (int) (this.agent.getNotApprovable() + (rand.nextInt()%(GeneticAlgorithm.RANGE*mutationSeverity)));
		if(rand.nextFloat()>=mutationLikelihood)
			failedApproved = this.agent.getFailedApproved();
		else
			failedApproved = (int) (this.agent.getFailedApproved() + (rand.nextInt()%GeneticAlgorithm.RANGE)*mutationSeverity);
		return new Genome(new ComboAgent(proposalLoss,  participationLoss,  voteLoss,proposalThreshold, singleFailureProposal,
				 voteSuccess,  nonSelfProposal,  nonSelfVote,  spiesSucceeded,  notApprovable, failedApproved));
	}
	@Override
	public String toString()
	{
		String str = "";
		str += this.gamesPlayed + " " + this.gamesWon + " " + this.getWinrate() + " " +this.agent.toString();
		return str;
	}
}

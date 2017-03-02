package geneticalgorithm;
import java.util.*;
import java.io.*;
import java.lang.instrument.Instrumentation;

import agents.Agent;
import agents.ComboAgent;
import agents.TrustAgent;
import game.Game;
import utility.SpyCombo;
import utility.Utility;
public class GeneticAlgorithm 
{
	private static final int NUM_SPIES[] = {2,2,3,3,3,4};
	private static final int MISSION_SIZES[][] = {{2,3,2,3,3},{2,3,4,3,4},{2,3,3,4,4},{3,4,4,5,5},{3,4,4,5,5},{3,4,4,5,5}};
	public static final int SPY = 0;
	public static final int RESISTANCE = 1;
	private HashSet<Genome> population[];
	private Agent[] opponents;
	public static final int RANGE = 100;
	private int numPlayers;
	private Random rand;
	private int populationSize;
	private int generation;
	private int selectionSize;
	private int numTrials;
	private float mutationRate;
	private float mutationLikelihood;
	private float mutationSeverity;
	public GeneticAlgorithm(int numPlayers, int populationSize,int selectionSize,int numTrials, float mutationRate,
			float mutationLikelihood, float mutationSeverity,Agent opponents[])
	{
		this.numPlayers = numPlayers;
		this.rand = new Random(System.currentTimeMillis());
		this.populationSize = populationSize;
		this.population = new HashSet[RESISTANCE+1];
		this.population[SPY] = generateInitialPopulation();
		this.population[RESISTANCE] = generateInitialPopulation();
		this.generation = 0;
		this.selectionSize = selectionSize;
		this.numTrials = numTrials;
		this.mutationRate = mutationRate;
		this.mutationLikelihood = mutationLikelihood;
		this.mutationSeverity = mutationSeverity;
		this.opponents = opponents;
	}
	public void log(String filename) throws FileNotFoundException, UnsupportedEncodingException
	{
		System.out.println("Logging " + filename + " " + generation);
		PrintWriter spyWriter = new PrintWriter(filename + "_Spies_"+this.generation, "UTF-8");
		PrintWriter resistanceWriter = new PrintWriter(filename + "_Resistance_"+this.generation, "UTF-8");
		HashSet<Genome> toLog []= this.getFittest();
		Iterator<Genome> spyIter = toLog[SPY].iterator();
		Iterator<Genome> resIter = toLog[RESISTANCE].iterator();
		while(spyIter.hasNext())
		{
			spyWriter.println(spyIter.next().toString());
		}
		while(resIter.hasNext())
		{
			resistanceWriter.println(resIter.next().toString());
		}
	    spyWriter.close();
	    resistanceWriter.close();
	}
	public HashSet<Genome> generateInitialPopulation()
	{
		HashSet<Genome> newPopulation = new HashSet<Genome>();
		for(int i = 0; i< this.populationSize; i++)
		{
			newPopulation.add(new Genome(new ComboAgent(rand.nextInt()%RANGE,rand.nextInt()%RANGE,rand.nextInt()%RANGE,
					2*rand.nextFloat()-1,rand.nextInt()%RANGE,rand.nextInt()%RANGE,rand.nextInt()%RANGE,
					rand.nextInt()%RANGE,rand.nextInt()%RANGE,rand.nextInt()%RANGE,rand.nextInt()%RANGE)));
		}
		return newPopulation;
	}
	public HashSet<Genome>[] getFittest()
	{
		HashSet<Genome> fittest[] = new HashSet[RESISTANCE+1];
		fittest[SPY] = new HashSet<Genome>();
		fittest[RESISTANCE] = new HashSet<Genome>();
		Game currentGame;
		Genome currGenome;
		Iterator<Genome> genomeIterator;
		for(int k = 0; k< population.length; k++)
		{
			genomeIterator = this.population[k].iterator();
			while(genomeIterator.hasNext())
			{	
				currGenome = genomeIterator.next();
				for(int j = 0; j<numTrials; j++)
				{
					currentGame = new Game();
					for(int i = 0; i< this.numPlayers; i++)
					{
						if((k == SPY && i == 0) || (k == RESISTANCE && i == NUM_SPIES[this.numPlayers]))
						{
							currentGame.addPlayer(currGenome.getAgent());
						}
						else
						{
							int r = rand.nextInt()%opponents.length;
							if(r<0)
								r *= -1;
							currentGame.addPlayer(opponents[r]);
						}
					}
					currentGame.setupGeneticAlgorithm();
					currGenome.playedGame(currentGame.playGeneticAlgorithm() == population[RESISTANCE].contains(currGenome));
				}
			}
			Genome sortedGenomes[] = population[k].toArray(new Genome[]{});
			Arrays.sort(sortedGenomes);
			for(int j = 0; j< this.selectionSize;j++)
			{
				fittest[k].add(sortedGenomes[j]);
			}
		}
		return fittest;
	}
	
	public Genome[] nextGeneration()
	{
		Genome best[] = new Genome[2];
		HashSet<Genome> progenitors[] = this.getFittest();
		for(int i = 0; i< progenitors.length; i++)
		{
			HashSet<Genome> nextGeneration = new HashSet<Genome>();
			Genome[] currProgenitors = progenitors[i].toArray(new Genome[]{});
			Arrays.sort(currProgenitors);
			for(int j = 0; j< populationSize; j++)
			{
				if(mutationRate > rand.nextFloat())
				{
					nextGeneration.add(currProgenitors[j%currProgenitors.length].mutate(mutationSeverity, mutationLikelihood));
				}
				else
				{
					int r = rand.nextInt();
					if(r<0)
					{
						r*= -1;
					}
					nextGeneration.add(currProgenitors[j%currProgenitors.length].crossbreed(currProgenitors[r%currProgenitors.length]));
				}
			}
			if(i == SPY)
			{
				this.population[SPY] = nextGeneration;
			}
			else
			{
				this.population[RESISTANCE] = nextGeneration;
			}
		}
		this.generation++;
		Genome spies[] = progenitors[SPY].toArray(new Genome[]{});
		Genome resistance[] = progenitors[RESISTANCE].toArray(new Genome[]{});
		Arrays.sort(spies);
		Arrays.sort(resistance);
		best[SPY] = spies[0];
		best[RESISTANCE] = resistance[0];
		return best;
	}
	public void setOpponents(Agent opponents[])
	{
		this.opponents = opponents;
	}
	public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException
	{
		//Runtime runtime = Runtime.getRuntime();
		Random rand = new Random(System.currentTimeMillis());
		System.out.println("entered main");
		GeneticAlgorithm algorithm;
		System.out.println("initialised superArrays");
		HashSet<Agent> opponents = new HashSet<Agent>();
		for(int i = 0; i< 100; i++)
		{
			opponents.add(new TrustAgent(rand.nextInt(100),rand.nextInt(100),rand.nextInt(100),rand.nextInt(100)));
		}
		System.out.println("Passed massive opponent initilisation loop");
		for(int i = 0; i< NUM_SPIES.length; i++)
		{
			algorithm = new GeneticAlgorithm(5+i, 100,20,20, (float)0.2, (float)0.5, (float)0.1,opponents.toArray(new Agent[]{}));
			System.out.println("Initialised algorithms");
			for(int j = 0; j< 15; j++)
			{
				Genome best[]  = algorithm.nextGeneration();
				System.out.println(j);
				//System.out.println(runtime.totalMemory());
				//System.gc();
				for(int k = 0; k< best.length; k++)
				{
					System.out.println(best[k].toString());
				}
			};
			algorithm.log(i+"Players_");
		}
	}
}

package utility;
import java.util.*;
/*
 * @Author Callum Sullivan
 * @Version 3/11/2016
 * The mission class is intended to hold information about possible combinations of players who could be spies
 */
public class SpyCombo implements Comparable{
	private Set<Character> spies; //The players who are spies in this combination
	private double likelihood; //The likelihood of this combination being the real case
	//@param spies the players who are spies in this world
	public SpyCombo(Set<Character> spies)
	{
		this.spies = spies;
		likelihood = 0;
	}
	//@param oldCombo the spyCombo this one is a deep copy of
	public SpyCombo(SpyCombo oldCombo)
	{
		this.spies = oldCombo.getSpies();
		this.likelihood = oldCombo.getLikelihood();
	}
	public Set<Character> getSpies()
	{
		return this.spies;
	}
	public double getLikelihood()
	{
		return this.likelihood;
	}
	//@param increase the amount likelihood is to be increased by
	public void addLikelihood(double increase)
	{
		this.likelihood += increase;
	}
	//@param arg0 the object this one is being compared to
	@Override
	public int compareTo(Object arg0) {
		if(((SpyCombo) arg0).getLikelihood() > this.likelihood)
		{
			return 1;
		}
		else if(((SpyCombo) arg0).getLikelihood() < this.likelihood)
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}
}

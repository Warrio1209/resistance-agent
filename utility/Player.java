package utility;
import java.util.*;
/*
 * @Author Callum Sullivan
 * @Version 3/11/2016
 * The mission class is intended to hold information about possible combinations of players who could be spies
 */
public class Player implements Comparable{
	private Character player; //The players who are spies in this combination
	private double suspicion; //The likelihood of this combination being the real case
	//@param spies the players who are spies in this world
	public Player(Character player)
	{
		this.player = player;
		this.suspicion = 0;
	}
	//@param oldCombo the spyCombo this one is a deep copy of
	public Player(Player oldPlayer)
	{
		this.player = oldPlayer.getPlayer();
		this.suspicion = oldPlayer.getSuspicion();
	}
	public Character getPlayer()
	{
		return this.player;
	}
	public double getSuspicion()
	{
		return this.suspicion;
	}
	//@param increase the amount likelihood is to be increased by
	public void addSuspicion(double increase)
	{
		this.suspicion += increase;
	}
	//@param arg0 the object this one is being compared to
	@Override
	public int compareTo(Object arg0) {
		if( ((Player) arg0).getSuspicion() > this.suspicion )
		{
			return 1;
		}
		else if(((Player) arg0).getSuspicion() < this.suspicion )
		{
			return -1;
		}
		return 0;
	}
}

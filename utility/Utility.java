package utility;

import java.util.Iterator;
import java.util.Set;

public class Utility {
	public static <E> void printSet(Set<E> set)
	{
		Iterator<E> current = set.iterator();
		boolean flag = false;
		while(current.hasNext())
		{
			if(flag)
				System.out.print(",");
			else
				flag = true;
			System.out.print(current.next().toString());
		}
		System.out.println();
	}
}

import java.io.*;
import java.util.*;

public class StringAppend extends StringLiteral
{
	LinkedList appending;

	public StringAppend(String s)
	{
		super(s);
	}

	public StringAppend(String s, Atom a, Atom b)
	{
		super(s);
		appending = new LinkedList();
		appending.offer(a);
		appending.offer(b);
	}

	public StringAppend(String s, LinkedList l)
	{
		super(s);
		appending = l;
	}
	
	public Atom copy()
	{
		StringAppend sl = new StringAppend(value, (LinkedList)appending.clone());
		if(meta != null) sl.meta = (Map)meta.copy();
		return sl;
	}

public Atom deepcopy(int myd, int datadelta, Map defEnv) //only valid for data
{
	if(isStatic()) return this;

	StringLiteral sl = new StringAppend(value, (LinkedList)appending.clone());
	int dl = dataLevel();
	if(meta != null)
	{
		sl.meta = (Map)meta.copy();
		((MapData)sl.meta).put(StringLiteral.isData, new NumberLiteral(dl + datadelta));
	}
	if(dl + datadelta <= 0)
	{
		if(sl.getMeta(StringLiteral.defEnv) == null)
            sl.putMeta(StringLiteral.defEnv, defEnv);
		return new MObject(sl);
	}
	return sl;
}

	public synchronized String computeAppend()
	{
		if(appending.isEmpty()) return null;
	
		Atom a = (Atom)appending.poll();
		StringLiteral sl = (StringLiteral)a.evalForData();
		
		value = value+sl.value;
		if(StringAppend.class.isAssignableFrom(sl.getClass()))
		{
			appending.addAll(0, ((StringAppend)sl).appending );
		}
		
		return sl.value;
	}
	
	public NumberLiteral length()
	{
		while(computeAppend() != null);
		
		return new NumberLiteral(value.length());
	}
	
	public void print(Writer out, int dataLevel, int depth, boolean asData) throws java.io.IOException
	{
		int d = dataLevel() - dataLevel;
		dataLevel += d;
		//if((MapData.class == getClass() || MapUnion.class == obj.getClass()) || obj.isName())
		{
			while(d-- > 0)
				out.write("'");
			while(d++ < -1)
				out.write(",");
		}
		
		out.write( "\"" );
		out.write( value.toString() );
			
		while(appending.isEmpty() == false)
		{
			out.write( computeAppend() );
		}
		
		out.write( "\"" );
		
		out.flush();
	}

	public int hashCode()
	{
		return value.hashCode();
	}
	
	public boolean equals(Object o)
	{
		if(o.getClass() == StringLiteral.class)
		{
			return value.equals(((StringLiteral)o).string());
		}
		if(o.getClass() == String.class)
		{
			return value.equals(o);
		}
		
		return false;
	}

	public Atom get(Atom key)
	{
		if(key.getClass() == NumberLiteral.class)
		{
			NumberLiteral num = (NumberLiteral)key;
			if(num.compareTo(0) >= 0 && num.compareTo(value.length()) < 0)
			{
				return new NumberLiteral((long)value.charAt((int)num.longValue()));
			}
		}
	
		return null;
	}

public Map keys()
{
	MapData mkeys = new MapData();
	
	NumberLiteral index = NumberLiteral.zero;
	for (int i=0;i<value.length();i++)
	{
		mkeys.put(index, new NumberLiteral(i));
		index = index.add(NumberLiteral.one);
	}
	
	return mkeys;
}
}

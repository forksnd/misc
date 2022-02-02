import java.util.*;

public class Map extends DataAtom
{

public boolean equals(Object b)
{
	if(Map.class.isAssignableFrom(b.getClass()) == false) return false;
	
	Map m = (Map)b;
	Map mkeys = keys();
	Atom key;
	int kindex = 0;
	while((key = mkeys.get(new NumberLiteral(kindex))) != null)
	{
		Atom o = get(key).evalForData();
		Atom o2 = m.get(key);
		if(o2 == null) return false;
		
		o2 = o2.evalForData();

		if(o.equals(o2) == false) return false;
		kindex++;
	}
	return true;
}

public Atom getKnown(Atom key)
{
	return get(key);
}

public Atom get(Atom key)
{
    return NullLiteral.nullValue;
}

Atom getData(Atom key)
{
	return get(key).evalForData();
}

NumberLiteral getNumber(Atom key)
{
	return (NumberLiteral)get(key).evalForData();
}

public String toString()
{
	StringBuffer s = new StringBuffer("[");
	
	Map mkeys = keys();
	Atom key;
	int kindex = 0;
	while((key = mkeys.get(new NumberLiteral(kindex))) != null)
	{
		Atom o = get(key).evalForData();
		
		s.append(key+":"+o+" ");
		kindex++;
	}

	if(s.length() > 1) s.deleteCharAt(s.length()-1);
	s.append("]");
	
	return s.toString();
}

public NumberLiteral length()
{
	NumberLiteral index = NumberLiteral.zero;
	while(get(index) != null)
	{
		index = index.add(NumberLiteral.one);
	}
	return index;
}

public Atom deepcopy(int datadelta, Map defEnv)
{
	return deepcopy(datadelta, datadelta, defEnv);
}

public Atom deepcopy(int mydd, int datadelta, Map defEnv)
{
    return this;
}

public Map keys()
{
    return this;
}

public Map knownKeys()
{
	return keys();
}
}
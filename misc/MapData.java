import java.util.*;

public class MapData extends Map
{
public LinkedHashMap dataMap;

public MapData()
{
	super();
	dataMap = new LinkedHashMap(5);
}

public MapData(LinkedHashMap dm)
{
	super();
	dataMap = dm;
}

public Atom get(Atom key)
{
	return (Atom)dataMap.get(key);
}

// Mutators

public void put(Atom key, Atom value)
{
	dataMap.put(key, value);
}

public void remove(Atom key)
{
	dataMap.remove(key);
}

public void putAll(MapData all)
{
	dataMap.putAll(all.dataMap);
}

public void putAny(Object key, Atom value)
{
	if(!Atom.class.isAssignableFrom(key.getClass()))
		dataMap.put(new StringLiteral(key.toString()), value);
	else
		dataMap.put(key, value);
}

// Descriptors
/*
public String toString()
{
	StringBuffer s = new StringBuffer("[");
	
	// unnamed indexes
	NumberLiteral index = NumberLiteral.zero;
	while(true)
	{
		Atom o = dataMap.get(index);
		if(o == null) break;
	
		if(o == this)
			s.append("self ");
		else
			s.append(o+" ");
	
		index = index.add(NumberLiteral.one);
	}
	
	for (Iterator it = dataMap.keySet().iterator(); it.hasNext();)
	{
		Atom key = (MObject)it.next();
		Atom o = (MObject)dataMap.get(key);
		
		if(key.value().getClass() == NumberLiteral.class && (((NumberLiteral)key.value()).compareTo(index) < 0 && ((NumberLiteral)key.value()).compareTo(NumberLiteral.zero) >= 0))
			continue;

		if(o.value() == this)
			s.append(key+":self ");
		else
			s.append(key+":"+o+" ");
	}

	if(s.length() > 1) s.deleteCharAt(s.length()-1);
	s.append("]");
	
	return s.toString();
}*/

public Atom copy()
{
	Map m = new MapData(new LinkedHashMap(dataMap));
	if(meta != null) m.meta = (Map)meta.copy();
	return m;
}

public Atom deepcopy(int datadelta, Map defEnv)
{
	return deepcopy(datadelta, datadelta, defEnv);
}

public Atom deepcopy(int mydd, int datadelta, Map defEnv)
{
	MapData m = new MapData(new LinkedHashMap(dataMap));
	
	for (Iterator it = dataMap.keySet().iterator(); it.hasNext();)
	{
		Atom key = (Atom)it.next();
		Atom o = (Atom)dataMap.get(key);
		
        o.evalForData();
		m.put(key, o.deepcopy(datadelta, defEnv));
	}
    if(isStatic() == false) {
        int dl = dataLevel();
        if(meta != null)
        {
            m.meta = (Map)meta.copy();
            ((MapData)m.meta).put(StringLiteral.isData, new NumberLiteral(dl + mydd)); 
        }
        if(dl + mydd <= 0)
        {
            if(m.getMeta(StringLiteral.defEnv) == null)
                m.putMeta(StringLiteral.defEnv, defEnv); 
            return new MObject(m);
        }
    }
	return m;
}

public Set keySet()
{
	return dataMap.keySet();
}

public Map keys()
{
	MapData allKeys = new MapData();
	
	NumberLiteral index = NumberLiteral.zero;
	Set keyS = keySet();
	for (Iterator it = keyS.iterator(); it.hasNext();)
	{
		allKeys.put(index, (Atom)it.next());
		index = index.add(NumberLiteral.one);
	}
	
	return allKeys;
}
}
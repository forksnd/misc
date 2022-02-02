import java.util.*;

public class MapUnion extends MapData
{
//public Atom unionWith = null;
public MapUnion allKeys = null;
public MapUnion parent = null;
public boolean isKeys = false, append = false;

LinkedList<Atom> unionWith = new LinkedList<Atom>();

public MapUnion()
{
	super();
}

public MapUnion(LinkedHashMap dm)
{
	super(dm);
}

public MapUnion(Atom a, Atom b)
{
	super();
	unionWith.add(a);
	unionWith.add(b);
}

static public MapUnion unionWithAppend(Atom a, Atom b)
{
	MapUnion mu = new MapUnion();
	mu.unionWith.add(a);
	mu.unionWith.add(b);
	mu.append = true;
	
/*	LinkedHashMap ndata = new LinkedHashMap(5);
	
	
	Map keys = a.keys();
	NumberLiteral index = NumberLiteral.zero;
	while(true)
	{
		Atom k = keys.get(index);
		if(k == null) break;
		ndata.put(k, a.get(k));
		index = index.add(NumberLiteral.one);
	}
	

	MapUnion mu = new MapUnion(ndata);
	mu.unionWith = u;
	mu.append = true;
	
	return mu;*/
	return mu;
}

public Atom getKnown(Atom key)
{
	return super.get(key);
}

public Atom get(Atom key)
{
//	System.out.println(hashCode()+">"+isKeys+" get:"+key);
	while(true)
	{
		Atom v = super.get(key);
		if(v != null) return v;
		//System.out.println("miss: " +key);
		if(!computeUnion()) return null;
	}
}

public void addUnionObject(Atom k, Atom o)
{
	if(!dataMap.containsKey(k))
	{
		dataMap.put(k, o);
		
		if(allKeys != null)
		{
			allKeys.appendUnionObject(allKeys.knownLength(), k);
		}
	}
}

public void appendUnionObject(Atom k, Atom o)
{
	dataMap.put(k, o);
	
	if(allKeys != null)
	{
		allKeys.appendUnionObject(allKeys.knownLength(), k);
	}
}

public synchronized boolean computeUnion()
{
	if(isKeys && parent != null) return parent.computeUnion();
	if(unionWith.isEmpty()) return false;
	
	Atom u = unionWith.poll();
	u = u.evalForData();
	while(u.getClass() == MapUnion.class && ((MapUnion)u).append == false)
	{
		unionWith.addAll(0, ((MapUnion)u).unionWith);
		if(((MapUnion)u).dataMap.size() > 0) break;
		if(unionWith.isEmpty()) break;
		u = unionWith.poll();
		u = u.evalForData();
	}
	
	if(append) return computeAppend((Map)u);
	
	if(u.getClass() == MapUnion.class)// && ((MapUnion)u).append == true)
	{
		while(((MapUnion)u).unionWith.isEmpty() == false)
		{
			((MapUnion)u).computeUnion();
		}
	}
	
	Map keys = ((MapData)u).knownKeys();
	NumberLiteral index = NumberLiteral.zero;
	while(true)
	{
		Atom k = keys.get(index);
		if(k == null) break;
		//System.out.println("add: " +k+"="+((Map)u).getKnown(k));
		addUnionObject(k, ((Map)u).getKnown(k));
		index = index.add(NumberLiteral.one);
	}
	
	return true;
}

public boolean computeAppend(Map u)
{
	if(u.getClass() == MapUnion.class && ((MapUnion)u).append == false)
	{
		while(((MapUnion)u).unionWith.isEmpty() == false)
		{
			((MapUnion)u).computeUnion();
		}
	}
	
	NumberLiteral index = NumberLiteral.zero;
	NumberLiteral len = knownLength();
	
	while(true)
	{
		Atom o = u.getKnown(index);
		if(o == null) break;
		appendUnionObject(index.add(len), o);
		index = index.add(NumberLiteral.one);
	}

	return true;
}

public Atom copy()
{
	MapUnion m = new MapUnion(new LinkedHashMap(dataMap));
	m.unionWith = unionWith;
	m.isKeys = isKeys;
	m.parent = parent;
	m.allKeys = allKeys;
	if(meta != null) m.meta = (Map)meta.copy();
	return m;
}

public Atom deepcopy(int datadelta, Map defEnv)
{
	return deepcopy(datadelta, datadelta, defEnv);
}

public Atom deepcopy(int mydd, int datadelta, Map defEnv)
{

	while(unionWith.isEmpty() == false)
	{
		computeUnion();/*
		MapData aw = new MapData();
		aw.put(NumberLiteral.zero, Misc.rootEnvironment.get(new StringLiteral("eval")));
		aw.put(NumberLiteral.one, unionWith);
		aw.put(new StringLiteral("in"), defEnv);
		aw.put(NumberLiteral.two, new NumberLiteral(datadelta));
		aw.setDataLevel(0);
		
		m.unionWith = new MObject(aw);*/
	}
	
	MapUnion m = new MapUnion(new LinkedHashMap(dataMap));
	
	for (Iterator it = dataMap.keySet().iterator(); it.hasNext();)
	{
		Atom key = (Atom)it.next();
		Atom o = (Atom)dataMap.get(key);
		
		m.put(key, o.deepcopy(datadelta, defEnv));
	}
	//m.isKeys = isKeys;
	//m.parent = parent;
	//m.allKeys = allKeys;
	if(isStatic() == false) {
        int dl = dataLevel();
        if(meta != null)
        {
            m.meta = (Map)meta.copy();
            ((MapData)m.meta).put(StringLiteral.isData, new NumberLiteral(dl + mydd)); 
        }
        if(dl + mydd <= 0)
        {
            m.putMeta(StringLiteral.defEnv, defEnv); 
            return new MObject(m);
        }
    }
	return m;
}

public Map keys()
{
	if(allKeys != null) return allKeys;

	allKeys = new MapUnion();
	
	NumberLiteral index = NumberLiteral.zero;
	Set keyS = keySet();
	for (Iterator it = keyS.iterator(); it.hasNext();)
	{
		allKeys.put(index, (Atom)it.next());
		index = index.add(NumberLiteral.one);
	}
	
	allKeys.isKeys = true;
	allKeys.parent = this;

	return allKeys;
}

public NumberLiteral knownLength()
{
	Set keys = keySet();
	
	NumberLiteral index = NumberLiteral.zero;
	while(true)
	{
		if(keys.contains(index) == false) return index;
		index = index.add(NumberLiteral.one);
	}
}

public Map knownKeys()
{
	return super.keys();
}
}
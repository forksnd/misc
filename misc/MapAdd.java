import java.util.*;

public class MapAdd extends MapData
{
Atom k = null, o = null;
MapAdd allKeys = null;
MapAdd parent = null;
boolean isKeys = false, done = false;

public MapAdd(MapData m, Atom kk, Atom oo)
{
	super();
	put(kk.evalForData(), oo);
	k = kk; o = oo;
}

public Atom get(Atom key)
{
	Atom v = super.get(key);
	if(v != null) return v;
	
	if(done || !computeAdd()) return null;
	return super.get(key);
}

public boolean computeAdd()
{
	done = true;
	
	if(isKeys && parent != null) {return parent.computeAdd();}
	if(dataMap.containsKey(k)) return false;
	
	k = k.evalForData();
	dataMap.put(k, o);
	
	if(allKeys != null)
	{
		((MapData)allKeys).put(allKeys.length(), k);
	}
	
	return true;
}

public Atom copy()
{
	MapAdd m = new MapAdd(this, k, o);
	//m.isKeys = isKeys;
	//m.parent = parent;
	//m.allKeys = allKeys;
	if(meta != null) m.meta = (Map)meta.copy();
	return m;
}

public Atom deepcopy(int mydd, int datadelta, Map defEnv)
{
	return new MapAdd((MapData)super.deepcopy(mydd, datadelta, defEnv), k, o.deepcopy(datadelta, defEnv));
}

public Map keys()
{
	if(allKeys != null) return allKeys;

	allKeys = new MapAdd((MapData)super.keys(), length(), k);
	allKeys.isKeys = true;
	allKeys.parent = this;

	return allKeys;
}
}
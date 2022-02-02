public abstract class DataAtom extends Atom
{
	DataAtom()
	{
	}
	
	Atom deepcopy(int dd, Map defEnv)
	{
		if(isStatic()) return this;
		
		Atom c = copy();
		int dl = dataLevel();
		c.putMeta(StringLiteral.isData, new NumberLiteral(dl + dd));
		if(dl+dd <= 0)
		{
			if(c.getMeta(StringLiteral.defEnv) == null)
                c.putMeta(StringLiteral.defEnv, defEnv);
			return new MObject(c);
		}
		return c; 
	}
	
	public String toKeyString()
	{
		return toString();
	}
	
	public Atom evalAsFunction(Map args, Map environment)
	{
		return null;
	}
	public boolean isFunction()
	{
		return false;
	}
	public boolean isData()
	{
		if(dataLevel() > 0) return true;
		return false;
	}
	public boolean isName()
	{
		if(getMeta(StringLiteral.isName) != null) return true;
		return false;
	}
	public boolean isStatic()
	{
		Atom idata = getMeta(StringLiteral.isData);
		if(idata == null) return true;
		return false;
	}
	public boolean isStrict()
	{
		Atom idata = getMeta(StringLiteral.strict);
		if(idata == null) return false;
		return true;
	}
	public int dataLevel()
	{
		Atom idata = getMeta(StringLiteral.isData);
		if(idata == null) return 1;
		idata = idata.evalForData();
		return (int)((NumberLiteral)idata).longValue();
	}
	public void setDataLevel(int dl)
	{
        if(meta == null) meta = new MapData();
		putMeta(StringLiteral.isData, new NumberLiteral(dl));
	}
	public Atom meta()
	{
        if(meta == null) meta = new MapData();
		return meta;
	}
	public Atom getMeta(Atom key)
	{
        if(meta == null) return null;
		return meta.get(key);
	}
	public void putMeta(Atom key, Atom v)
	{
        if(meta == null) meta = new MapData();
		((MapData)meta).put(key, v);
	}
	public void removeMeta(Atom key)
	{
        if(meta == null) return;
		((MapData)meta).remove(key);
	}
}

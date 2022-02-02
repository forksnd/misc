public abstract class Atom
{
	Map meta = null;

	Atom()
	{
	}
	
	Atom evalForData()
	{
		return this;
	}
	
	Atom copy()
	{
		return this; 
	}
	
	Atom deepcopy(int dd, Map defEnv)
	{
		return this; 
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
		return true;
	}
	public boolean isName()
	{
		return false;
	}
	public boolean isStatic()
	{
		return true;
	}
	public boolean isStrict()
	{
		return false;
	}
	public int dataLevel()
	{
		return 1;
	}
	abstract public void setDataLevel(int dl);
	abstract public Atom getMeta(Atom key);
	abstract public void putMeta(Atom key, Atom v);
	abstract public void removeMeta(Atom key);
}

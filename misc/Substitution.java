import java.util.*;

public class Substitution extends Functions.Function
{
	Atom function;
	public Substitution(Atom f)
	{
		function = f;
	}
	public Atom copy()
	{
		Substitution s = new Substitution(function.copy());
		s.meta = (Map)meta.copy();
		return s;
	}

	public Atom evalAsFunction(Map inputargs, Map environment)
	{
		MapData newEnvironment = new MapData();
		newEnvironment.put(StringLiteral.defEnv, getMeta(StringLiteral.defEnv));
		Atom ia = inputargs.copy();
        ia.removeMeta(StringLiteral.isData);
		newEnvironment.put(StringLiteral.args, ia);
		newEnvironment.put(StringLiteral.callEnv, environment);
		
		Atom r = function.deepcopy(-1, newEnvironment);
        
		newEnvironment.put(new StringLiteral("self"), r);
		
		return r;
	}
	
	public String toString()
	{
		return "@"+getClass().getSimpleName();
	}
	public boolean isFunction()
	{
		return true;
	}
	public boolean isStatic()
	{
		return true;
	}
	public boolean isData()
	{
		return true;
	}
	public boolean isName()
	{
		return false;
	}
	public boolean isStrict()
	{
		return true;
	}
}

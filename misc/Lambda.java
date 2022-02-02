import java.util.*;

public class Lambda extends Functions.Function
{
	Map args;
	Atom function;
	public Lambda(Map a, Atom f)
	{
		args = a;
		function = f;
	}
	public Atom copy()
	{
	//	System.out.println("Unexpected copy of lambda");
		Lambda lf = new Lambda((Map)args.copy(), function.copy());
		lf.meta = (Map)meta.copy();
		return lf;
	}
	Atom deepcopy(int dd, Map defEnv)
	{
		return this;
	}

	public Atom evalAsFunction(Map inputargs, Map environment)
	{
		Atom newEnvironment = getMeta(StringLiteral.defEnv);
		newEnvironment = newEnvironment.evalForData().copy();

		Map mkeys = args.keys();
		Atom k;
		NumberLiteral index = NumberLiteral.zero;
		while((k = mkeys.get(index)) != null)
		{
			Atom o = null, key = args.get(k);
            
            o = inputargs.get(key);
			/*
            if(o == null) {
                if(key.equals("args"))
                {
                    inputargs.removeMeta(StringLiteral.isData);
                    o = inputargs;
                }
                else if(key.equals("def-env"))
                {
                    o = getMeta(StringLiteral.defEnv);
                }
                else if(key.equals("call-env"))
                {
                    o = environment;
                }
            }
			
			if(key.isStrict())
			{
				o = o.evalForData();
			}
			*/
			((MapData)newEnvironment).put(k, o);
			index = index.add(NumberLiteral.one);
		}
		
		return function.deepcopy(-1, (Map)newEnvironment);
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

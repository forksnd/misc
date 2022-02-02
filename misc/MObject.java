import java.io.*;
import java.util.*;

public class MObject extends Atom
{
	Atom value = null;
	
	MObject()
	{
	}
	MObject(Atom v)
	{
		value = v;
	}
	static public Atom withAtom(Atom a, int dlevel)
	{
		if(dlevel <= 0)
			a = new MObject(a);
		
		a.putMeta(StringLiteral.isData, new NumberLiteral(dlevel));
			
		return a;
	}
	static public Atom withName(Atom a, int dlevel)
	{
		if(dlevel <= 0)
			a = new MObject(a);
		
		a.putMeta(StringLiteral.isData, new NumberLiteral(dlevel));
		a.putMeta(StringLiteral.isName, BoolLiteral.yes);
		
		return a;
	}
	static public Atom withNumber(String s, int dlevel)
	{
		if(s.startsWith("$"))
			return MObject.withName( new NumberLiteral(s.substring(1)), dlevel);
			
		return new NumberLiteral(s);
	}
	
	public Atom evalForDataNoMemo()
	{
		// am i data?
		if(isData())
			return this;
		
		try{
			while(!isData())
			{
				eval();
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in: "+value);
			return null;
		}
		
		return value;
	}
	
	public Atom evalForData()
	{
		Atom oldvalue = null;
		
		while(!value.isData())
		{
			oldvalue = value;
			
			try {
				eval();
				if(value.getClass() == MObject.class)
					value = value.evalForData();
				if(value == null) throw new Exception();
			}
			catch(Exception e)
			{
				//e.printStackTrace();
				Misc.printObject(new OutputStreamWriter(System.out), ((Map)oldvalue).get(NumberLiteral.zero), 0, 0, false);
				return null;
			}
		}
		return value;
	}
	
	public void eval()
	{
		// get def-environment
		Map dEnv = (Map)getMeta(StringLiteral.defEnv).evalForData();
		
		// am i a name
		if(isName())
		{
			Atom dvalue = dEnv.get(value);
			if(dvalue != null)
			{
				value = dvalue;
			}
			else
				value = NullLiteral.nullValue;
			return;
		}
		
		if(!Map.class.isAssignableFrom(value.getClass()))
		{
			value.removeMeta(StringLiteral.isData);
			value.removeMeta(StringLiteral.defEnv);
			return;
		}

		// do i have a function?
		Atom function = ((Map)value).get(NumberLiteral.zero);
		if(function == null)
		{
			System.out.println("No function");
			value = null;
			return;
		}
		
		function = function.evalForData();
		
		if(function == null || !function.isFunction())
		{
			System.out.println("Function unknown: "+function);
			value = null;
			return;
		}

		value = function.evalAsFunction((Map)value, dEnv);
	}
	
	public int hashCode()
	{
		return value.hashCode();
	}
	
	public boolean equals(Object o)
	{
		if(o == this) return true;
		if(o.getClass() != MObject.class) return false;
		if(value == ((MObject)o).value) return true;
		if(value.equals(((MObject)o).value()) )/*&& metaMap.equals(((MObject)o).metaMap))*/ return true;
		return false;
	}

	public String toString()
	{
		return value.toString();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	
	
	public Atom copy()
	{
		MObject mo = new MObject();
		mo.value = value.copy();
		return mo;
	}
	Atom deepcopy(int dd, Map defEnv)
	{
        evalForData();
		return value.deepcopy(dd, defEnv); 
	}
	
	public boolean isData()
	{
		return value.isData();
	}
	public boolean isName()
	{
		return value.isName();
	}
	public boolean isStatic()
	{
		return value.isStatic();
	}
	public int dataLevel()
	{
		return value.dataLevel();
	}
	public void setDataLevel(int dl)
	{
		value.setDataLevel(dl);
	}
	public Atom getMeta(Atom key)
	{
		return value.getMeta(key);
	}
	public void putMeta(Atom key, Atom v)
	{
		value.putMeta(key, v);
	}
	public void removeMeta(Atom key)
	{
		value.removeMeta(key);
	}
	
	
	public Atom value()
	{
		return value;
	}
}
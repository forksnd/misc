public class StringLiteral extends Map
{
	public String value;

	static StringLiteral defEnv = new StringLiteral("def-env");
	static StringLiteral callEnv = new StringLiteral("call-env");
	static StringLiteral args = new StringLiteral("args");
	static StringLiteral doc = new StringLiteral("doc");
	static StringLiteral self = new StringLiteral("$");
	static StringLiteral expression = new StringLiteral("expr");
	static StringLiteral isData = new StringLiteral("data");
	static StringLiteral isName = new StringLiteral("is-name");
	static StringLiteral doEval = new StringLiteral("evaluate");
	static StringLiteral head = new StringLiteral("head");
	static StringLiteral tail = new StringLiteral("tail");
	static StringLiteral generator = new StringLiteral("?");
	static StringLiteral strict = new StringLiteral("strict");
	static StringLiteral then = new StringLiteral("then");
	static StringLiteral else_ = new StringLiteral("else");

	public StringLiteral(String s)
	{
		value = s;
	}
	
	public String string()
	{
		return value;
	}

	public String toString()
	{
		return toPrettyString();
	}

	public Atom copy()
	{
		StringLiteral sl = new StringLiteral(value);
		if(meta != null) sl.meta = (Map)meta.copy();
		return sl;
	}

public Atom deepcopy(int myd, int datadelta, Map defEnv) //only valid for data
{
	if(isStatic()) return this;

	StringLiteral sl = new StringLiteral(value);
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
	
	public NumberLiteral length()
	{
		return new NumberLiteral(value.length());
	}

	public String toPrettyString()
	{
//		int d = dataLevel();
		String prefix="";
		boolean quoted = false;
//		while(d-- > 0)
//			prefix = "'"+prefix;
			
		String printValue;
		
		if(value.indexOf('"') >= 0)
		{
			printValue = "\"--->"+value+"<---\"";
			quoted = true;
		}
		else if(value.indexOf(' ') >= 0 || value.indexOf('\n') >= 0 || !isName())
		{
			printValue = "\""+value+"\"";
			quoted = true;
		}
		else
			printValue = value;
		
		return ((isName() && quoted)? "$" : "")+printValue;
	}

	public String toKeyString()
	{
		return value;
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

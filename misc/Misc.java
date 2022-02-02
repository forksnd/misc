import java.io.*;
import java.util.Iterator;
import java.net.URL;

public class Misc
{
	static MapData rootEnvironment;
	static URL baseURL;

	public Misc()
	{
		rootEnvironment = new MapData();
		
		rootEnvironment.putAny("true", BoolLiteral.yes);
		rootEnvironment.putAny("false", BoolLiteral.no);
		rootEnvironment.putAny("null", NullLiteral.nullValue);

		addToRoot("+", new Functions.Add(), "Numeric addition.");
		addToRoot("-", new Functions.Subtract(), "Numeric subtraction.");
		addToRoot("*", new Functions.Times(), "Numeric multiplication.");
		addToRoot("/", new Functions.Divide(), "Numeric division.");
		addToRoot("%", new Functions.Remainder(), "Numeric remainder.");
			
		addToRoot("if", new Functions.If(), "If the first argument is true then the value of 'if' is 'then' otherwise it is 'else'.");
		addToRoot("=", new Functions.Equals(), "If the two arguments are equal = is true otherwise it is false.");
		addToRoot("<", new Functions.LessThan(), "If the first argument is less than the second < is true otherwise it is false.");
		
		addToRoot("map?", new Functions.IsMap(), "True if $1 is a map.");
		addToRoot("lambda?", new Functions.IsFunction(), "True if $1 is a lambda.");
		addToRoot("number?", new Functions.IsNumber(), "True if $1 is a number.");
		addToRoot("string?", new Functions.IsString(), "True if $1 is a string.");
		addToRoot("null?", new Functions.IsNull(), "True if $1 is null.");

		addToRoot("map", new Functions.MapTo(), "The singluar mapping of $1 to $2.");
		addToRoot("keys", new Functions.Keys(), "The array of all the keys in $1.");
				
		addToRoot("get", new Functions.Get(), "Get the second argument from the first argument which is a map.");
		addToRoot("contains?", new Functions.Contains(), "True if $1 contains the key $2.");

		addToRoot("union", new Functions.Union(), "The lazy union of two maps.");
		addToRoot("append", new Functions.Append(), "Appends two maps.");
		addToRoot("string+", new Functions.StringApp(), "Appends two strings.");
		

		addToRoot("meta", new Functions.Meta(), "The value of the meta-data of $1.");
		addToRoot("meta=", new Functions.SetMeta(), "Sets the meta-data of $1 to the map $2.");
        
		addToRoot("force", new Functions.Force(), "Forces the evaluation of the first argument, returns the second.");
		addToRoot("log", new Functions.Log(), "Forces the evaluation of the first argument, returns the second.");

		addToRoot("lambda", new Functions.LambdaF(), "Lambda maps the first argument's values in the calling environment to the first argument's keys in current environment.");
		addToRoot("sub", new Functions.SubstitutionF(), "Substitution provides call-env, def-env and fn-env to its body.");
		
		addToRoot("let", new Functions.Let(), "Returns the value of $2 in an environment that includes $1.");
		addToRoot("letrec", new Functions.Letrec(), "Returns the value of $2 in an environment that includes $1 recursively.");
		
		addToRoot("load", new Functions.LoadFile(), "Opens a file as a string.");
		addToRoot("parse", new Functions.Parse(), "Parses a string into data.");
		addToRoot("display", new Functions.ToString(), "Prints an object as a string.");
		addToRoot("parse-xml", new Functions.ParseXML(), "Parses xml into data.");
		addToRoot("eval", new Functions.Eval(), "Dequotes data.");
		addToRoot("apply", new Functions.Apply(), "Applies a function to a set of parameters.");
		addToRoot("quote", new Functions.Quote(), "Quotes data.");
		addToRoot("seq", new Functions.Sequence(), "Evaluates its first argument before returning the second one.");
		
		// load up the bootstrap file
		try {
			Atom bootstrap = Functions.LoadFile.load(new URL(baseURL, "bootstrap.misc"));
			bootstrap = Functions.Parse.parse(((StringLiteral)bootstrap).value);
			bootstrap = Functions.Eval.eval((Map)bootstrap, null, rootEnvironment);
			rootEnvironment = (MapData)bootstrap.evalForData();
			System.out.println( "Loaded bootstrap. " );
		}
		catch (Exception e)
		{e.printStackTrace();}
	}

	void addToRoot(String name, Atom value, String doc)
	{
		rootEnvironment.putAny(name, value);
		
		value.putMeta(StringLiteral.doc, new StringLiteral(doc));
		value.putMeta(new StringLiteral("builtin"), BoolLiteral.yes);
	}
	
/*	static public boolean prettyPrint(Writer out, Atom pinfo, int dataLevel, int depth, boolean asData, boolean printedKey) throws Exception
	{
		int pindex = 0;
		Atom item;
		while(true)
		{
			item = ((Map)pinfo).get(new NumberLiteral(pindex));
			if(item == null) break;
			
			if(item.getClass() == NumberLiteral.class)
			{
				long num_returns = ((NumberLiteral)item.value()).longValue();
				while(num_returns-- > 0)
				{
					out.write("\n");
					for(int dd=0; dd<depth; dd++) out.write("\t");
				}
			}
			else
			{
				if(printedKey) out.write(" ");
				out.write("#:");
				printObject(out, item, dataLevel, depth, false);
				printedKey = true;
			}
			pindex++;
		}
		return printedKey;
	}*/
	
	static public void printObject(Writer out, Atom obj, int dataLevel, int depth, boolean asData)
	{
		printObject(out, obj, dataLevel, depth, asData, false);
	}
	
	static public void printObject(Writer out, Atom obj, int dataLevel, int depth, boolean asData, boolean asMeta)
	{
		try{
		
		if(obj == null)
		{
			out.write("null");
			return;
		}
		
		if(!asData)
			obj = obj.evalForData();
		
		if(obj == null)
		{
			out.write("NULL");
			return;
		}
		
		if(obj.isStatic() && MapData.class.isAssignableFrom(obj.getClass()))
		{
			if(!asMeta) out.write("`");
		}
		else
		{
			int d = obj.dataLevel() - dataLevel;
			dataLevel += d;
			if((MapData.class == obj.getClass() || MapUnion.class == obj.getClass()) || obj.isName())
			{
				while(d-- > 0)
					out.write("'");
				while(d++ < -1)
					out.write(",");
			}
		}
		
		if(StringAppend.class == obj.getClass())
		{
			((StringAppend)obj).print(out, dataLevel, depth, asData);
		}
		else if(MapData.class != obj.getClass() && MapUnion.class != obj.getClass())
		{
			out.write( obj.toString() );
		}
		else
		{
			Map ppmo = (Map)obj.getMeta(new StringLiteral("style"));
            if(ppmo != null) ppmo = (Map)ppmo.get(new StringLiteral("format"));
		
			out.write("[");
			
			boolean printedKey = false;
			NumberLiteral index = NumberLiteral.zero;
			
			Map env;
			Atom enva = obj.getMeta(StringLiteral.defEnv);
			if(enva != null)
				env = (Map)enva.evalForData();
			else
				env = null;
                
            if(ppmo != null)
            {
                int kindex = 0;
                Atom key;
                while((key = ppmo.get(new NumberLiteral(kindex))) != null)
                {
                    if(printedKey) out.write(" ");
                
                    if(NumberLiteral.class == key.getClass())
                    {
                        long returns = ((NumberLiteral)key).longValue();
                        while(returns-- > 0)
                            out.write("\n");
                        for(int dd=0; dd<depth; dd++) out.write("\t");
                        printedKey = false;
                    }
                    else
                    {
                        Atom type = ((Map)key).get(NumberLiteral.zero);
                        
                        if(type.equals(new StringLiteral("comment")))
                        {
                            out.write("#:");
                            printObject(out, ((Map)key).get(NumberLiteral.one), dataLevel, depth+1, false);
                        }
                        else if(type.equals(new StringLiteral("key")))
                        {
                            Atom keyk = ((Map)key).get(NumberLiteral.one);
                            Atom o = ((Map)obj).get(keyk);
                            
                            if(NumberLiteral.class == keyk.getClass())
                            {
                                if(((NumberLiteral)keyk).compareTo(index) != 0)
                                    out.write(keyk.toKeyString()+":");
                                
                                index = ((NumberLiteral)keyk).add(NumberLiteral.one);
                            }
                            else
                            {
                                out.write(keyk.toKeyString()+":");
                            }
                            
                            printObject(out, o, dataLevel, depth+1, false);
                        }
                        printedKey = true;
                    }
                
                    kindex++;
                }
            }
            else
            {
                int dindex = 0;
                // start with digits
                if(obj.getClass() == MapData.class)
                {
                    Atom value;
                    while((value = ((Map)obj).get(new NumberLiteral(dindex))) != null)
                    {
                        if(printedKey) out.write(" ");
                        printObject(out, value, dataLevel, depth+1, false);
                        printedKey = true;
                        dindex++;
                    }
                }
                else
                {
                    ((MapUnion)obj).computeUnion();
                    if(((MapUnion)obj).getKnown(new NumberLiteral(dindex)) != null)
                    {
                        Atom value;
                        while((value = ((MapUnion)obj).getKnown(new NumberLiteral(dindex))) != null)
                        {
                            if(printedKey) out.write(" ");
                            printObject(out, value, dataLevel, depth+1, false);
                            printedKey = true;
                            dindex++;
                        }
                    }
                }
            
                //others
                Map mkeys = ((Map)obj).keys();
                Atom key;
                int kindex = 0;
                while((key = mkeys.get(new NumberLiteral(kindex))) != null)
                {
                    if(NumberLiteral.class == key.getClass() && ((NumberLiteral)key).compareTo(dindex) < 0)
                    {
                        kindex++;
                        continue;
                    }
                
                    Atom o = ((Map)obj).get(key);
                    
                    if(printedKey) out.write(" ");
                    
                        if(NumberLiteral.class == key.getClass())
                        {
                            if(((NumberLiteral)key).compareTo(index) != 0)
                                out.write(key.toKeyString()+":");
                            
                            index = ((NumberLiteral)key).add(NumberLiteral.one);
                        }
                        else
                        {
                            if(asMeta && (key.equals(StringLiteral.isData) || key.equals(StringLiteral.isName) || key.equals(StringLiteral.defEnv)))
                            { kindex++; printedKey = false; continue; }
                            
                            out.write(key.toKeyString()+":");
                        }
                            
                    printObject(out, o, dataLevel, depth+1, false);
                    kindex++;
                    printedKey = true;
                }
            }
			
			//if(atom.getMeta(MObject.isDataMap) != null) out.print("}");
			out.write("]");
			out.flush();
		}

//Dont write out meta data for now
/*		if(obj.meta != null)
		{
			boolean gotExtra = false;
			
			Map mkeys = ((Map)obj.meta).keys();
			int kindex = 0;
			Atom key;
			while((key = mkeys.get(new NumberLiteral(kindex))) != null)
			{
				if(!(key.equals(StringLiteral.isData) || key.equals(StringLiteral.isName) || key.equals(StringLiteral.defEnv)))
				{
					gotExtra = true;
					break;
				}
						
				kindex++;
			}
			
			if(gotExtra)
			{
				out.write("@");
				printObject(out, obj.meta, dataLevel, 0, false, true);
			}
		}
*/		
		out.flush();
		
		}
		catch(Exception e)
		{
			e.printStackTrace( );
		}
	}

	public void test()
	{
		String test[] = {"[+ 2 3]", "[+ 3 [+ 4 5]]", "[if [= 3 4] then:5 else:6]", "[[if true then:* else:+] 4 5]", "[letrec '[fac:[lambda '[n:n]\n		'[if [= n 1]\n			then: 1\n			else: [* [fac n:[- n 1]] n]\n		]\n	]]\n	'[fac n:10]\n]"};
		Atom results[] = {new NumberLiteral(5), new NumberLiteral(12), new NumberLiteral(6), new NumberLiteral(20), new NumberLiteral(3628800)};
	
		for(int i=0;i<test.length;i++)
		{
			BufferedReader br = new BufferedReader(new StringReader(test[i]));

			Atom result = NullLiteral.nullValue;
			try {
				result = Parser.parseExpr(br, 1);
				result = result.deepcopy(-1, rootEnvironment);
				result = result.evalForData();

				if(result.equals(results[i])) continue;
			}
			catch(Exception e){}
			System.out.println( "Failed test:\n"+test[i] );
			System.out.println( "!= "+results[i] );
			return;
		}
		System.out.println( "Passed unit tests. " );
	}

	public void parseLine()
	{
		System.out.print( "=> ");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		Atom result = NullLiteral.nullValue;
		try {
			result = Parser.parseExpr(br, 1);
			result = result.deepcopy(-1, rootEnvironment);
		//	result = result.evalForData();

			Misc.printObject(new OutputStreamWriter(System.out), result, 0, 0, false);
		}
		catch(Exception e) {
			e.printStackTrace( );
		}
		
		System.out.print( "\n" );
	}
		
	public static void main (String args[])
	{
		try{
			File file = new File(System.getProperty("user.dir"));
			baseURL = file.toURL();
		}catch(Exception e){}
	
		System.out.println("/============================================");
		System.out.println("|  Welcome to MISC \n|");
		System.out.println("|  Version  r500");
		System.out.println("\\============================================\n");
		
		Misc l = new Misc();
		
		l.test();
		
		while(true)
		{
			l.parseLine();
		}
	}
}
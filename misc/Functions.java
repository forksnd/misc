import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;
import java.io.File;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

public class Functions
{
	static public abstract class Function extends DataAtom
	{
		public String toString()
		{
			return "@"+getClass().getSimpleName();
		}
		public Atom copy()
		{
			/*try{
				throw new Exception(""+this);
			}
			catch(Exception e)
			{
				e.printStackTrace( );
			}
			System.out.println("Unexpected copy of fn");
			return this;*/
			try{
				return (Atom) getClass().newInstance();
			}
			catch(Exception e) {}
			return null;
		}
		public boolean isFunction()
		{
			return true;
		}
	}
	
	static public abstract class MathF extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom a = args.get(NumberLiteral.one).evalForData();
			NumberLiteral result = NumberLiteral.numberFrom(a);
            
			Atom num;
			int kindex = 2;

			while((num = args.get(new NumberLiteral(kindex))) != null)
			{
                num = num.evalForData();
				result = function(result, NumberLiteral.numberFrom(num));
				
				kindex++;
			}

			return result;
		}
		
		abstract NumberLiteral function(NumberLiteral a, NumberLiteral b);
	}
	static public class Add extends MathF
	{
		public NumberLiteral function(NumberLiteral a, NumberLiteral b)
		{
			return a.add(b);
		}
	}
	static public class Times extends MathF
	{
		public NumberLiteral function(NumberLiteral a, NumberLiteral b)
		{
			return a.multiply(b);
		}
	}
	static public class Divide extends MathF
	{
		public NumberLiteral function(NumberLiteral a, NumberLiteral b)
		{
			return a.divide(b);
		}
	}
	static public class Subtract extends MathF
	{
		public NumberLiteral function(NumberLiteral a, NumberLiteral b)
		{
			return a.subtract(b);
		}
	}
	static public class Remainder extends MathF
	{
		public NumberLiteral function(NumberLiteral a, NumberLiteral b)
		{
			return a.remainder(b);
		}
	}
	
	static Atom isA(Map args, Class c)
	{
		Atom a = args.get(NumberLiteral.one).evalForData();
			
		if(c.isAssignableFrom(a.getClass()))
			return BoolLiteral.yes;
		return BoolLiteral.no;
	}
	
	static public class IsNull extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			return isA(args, NullLiteral.class);
		}
	}
	
	static public class IsNumber extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			return isA(args, NumberLiteral.class);
		}
	}
	
	static public class IsMap extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			return isA(args, Map.class);
		}
	}
	
	static public class IsString extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			return isA(args, StringLiteral.class);
		}
	}
	
	static public class IsFunction extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom a = args.get(NumberLiteral.one).evalForData();
			
			if(a.isFunction())
				return BoolLiteral.yes;
			return BoolLiteral.no;
		}
	}
	
	static public class If extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom result = args.get(NumberLiteral.one).evalForData();
			
			if(result.equals(BoolLiteral.no) || result.equals(NullLiteral.nullValue))
			{
				return args.get(StringLiteral.else_);
			}
			else
			{
				return args.get(StringLiteral.then);
			}
		}
	}
	
	static public class Equals extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom a = args.get(NumberLiteral.one).evalForData();
			Atom b = args.get(NumberLiteral.two).evalForData();
			
			if(a.equals(b))
			{
				return BoolLiteral.yes;
			}
			else
			{
				return BoolLiteral.no;
			}
		}
	}
	
	static public class LessThan extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom a = args.get(NumberLiteral.one).evalForData();
			Atom b = args.get(NumberLiteral.two).evalForData();
					
			if(((NumberLiteral)a).compareTo(b) < 0)
			{
				return BoolLiteral.yes;
			}
			else
			{
				return BoolLiteral.no;
			}
		}
	}
	
	static public class Keys extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Map a = (Map)args.get(NumberLiteral.one).evalForData();
			
			return a.keys();
		}
	}
	
	static public class Force extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			args.get(NumberLiteral.one).evalForData();
			return args.get(NumberLiteral.two);
		}
	}
	
	static public class LambdaF extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Map myargs = (Map)args.get(NumberLiteral.one).evalForData();
			Atom func = args.get(NumberLiteral.two).evalForData();
			
			Atom result = new Lambda((Map)myargs, func);
			result.putMeta(StringLiteral.defEnv, environment);
			result.putMeta(new StringLiteral("args"), myargs);
			result.putMeta(new StringLiteral("body"), func);
try{
            ((MapData)result.meta).putAll((MapData)args.meta);
}catch(Exception e)
{}
			
			return result;
		}
	}
	
	static public class SubstitutionF extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom func = args.get(NumberLiteral.one).evalForData();
			
			Atom result = new Substitution(func);
			result.putMeta(StringLiteral.defEnv, environment);
			//result.putMeta(new StringLiteral("body"), func);
			
			return result;
		}
	}
	
	static public class Let extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Map myargs = (Map)args.get(NumberLiteral.one).evalForData();
			Atom func = args.get(NumberLiteral.two).evalForData();
		
			return let(myargs, func, environment, false);
		}
		static public Atom let(Map myargs, Atom func, Map environment, boolean recursive)
		{			
			MapData newEnvironment = (MapData)environment.copy();
			Map recEnv = recursive? newEnvironment : environment;

			Map mkeys = (Map)myargs.keys();
			Atom key;
			int kindex = 0;

			while((key = mkeys.get(new NumberLiteral(kindex))) != null)
			{
				Atom o = myargs.get(key).evalForData();
				newEnvironment.put(key, o.deepcopy(-1, recEnv));
				
				kindex++;
			}
			
			return func.deepcopy(-1, newEnvironment);
		}
	}
	static public class Letrec extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Map myargs = (Map)args.get(NumberLiteral.one).evalForData();
			Atom func = args.get(NumberLiteral.two).evalForData();
		
			return Functions.Let.let(myargs, func, environment, true);
		}
	}

	static public class Union extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			MapData a = (MapData)args.get(NumberLiteral.one).evalForData();
			Atom b = args.get(NumberLiteral.two);
			return new MapUnion(a, b);
		}
	}
/*	static public class Union extends Function
	{
		public MObject evalAsFunction(Map args, Map environment)
		{
			MObject a = args.get(MObject.one);
			a=MObject.evalForData(a);
			//MObject b = MObject.evalForData(args.get(MObject.two));
			MObject b = args.get(MObject.two);
			if(a.equals(MObject.nullValue)) return MObject.nullValue;
			if(b.equals(MObject.nullValue)) return MObject.nullValue;
		
			return union(a, b);
		}
		
		static public MObject union(MObject a, MObject b)
		{
			while(((Map)a.value).computeUnion());
			
			Map newMap = (Map)a.value.copy();
			Map umap = newMap;
			umap.unionWith = b;
			//b.putMeta(MObject.isData, MObject.one);
			//	System.out.println(umap+" U "+b);
			MObject result = new MObject((Map)a.value.meta.copy(), newMap);
//			result.putMeta(MObject.defEnv, MObject.withMap(args));

			//((Map)result.value).computeUnion();

			return result;
		}
	}*/
	static public class MapTo extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom a = args.get(NumberLiteral.one).evalForData();
			Atom b = args.get(NumberLiteral.two);
			
			MapData newMap = new MapData();
			newMap.put(a,b);
			return newMap;
		}
	}
	
	static public class Get extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Map a = (Map)args.get(NumberLiteral.one).evalForData();
			Atom b = args.get(NumberLiteral.two).evalForData();

			Atom result = a.get(b);
			if(result == null) return NullLiteral.nullValue;
			return result;
		}
	}
	static public class Contains extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Map a = (Map)args.get(NumberLiteral.one).evalForData();
			Atom b = args.get(NumberLiteral.two).evalForData();

			Atom result = a.get(b);
			if(result == null) return BoolLiteral.no;
			return BoolLiteral.yes;
		}
	}
	
	static public class ParseXML extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom a = args.get(NumberLiteral.one).evalForData();
			
			try{
				XMLReader xr = XMLReaderFactory.createXMLReader();
				XMLHandler xh = new XMLHandler();
				xr.setContentHandler(xh);
				xr.setErrorHandler(xh);
				xr.parse(new InputSource(new StringReader(((StringLiteral)a).value)));
			
				return xh.xml;
			}
			catch(Exception e)
			{
				System.out.println(e);
				return null;
			}
		}

		public class XMLHandler extends DefaultHandler
		{
			Map xml = null;
			Stack s = new Stack();
		
			public void startElement (String uri, String name, String qName, Attributes attrs)
			{
				MapData m = new MapData();
				m.put(NumberLiteral.zero, new StringLiteral(qName));
				
				// attributes
				int i, c = attrs.getLength();
				for(i=0;i<c;i++)
				{
					String key = attrs.getLocalName(i);
					String val = attrs.getValue(i);
					m.put(new StringLiteral(key), new StringLiteral(val));
				}
				
				if(xml == null)
				{
					xml = m;
				}
				else
				{
					MapData curr = (MapData)s.peek();
					MapData sub = (MapData)curr.get(NumberLiteral.one);
					if(sub == null)
					{
						sub = new MapData();
						curr.put(NumberLiteral.one, sub);
					}
					sub.put(sub.length(), m);
				}
				s.push(m);
			}

			public void endElement (String uri, String name, String qName)
			{
				s.pop();
			}

			public void characters (char ch[], int start, int length)
			{
				MapData curr = (MapData)s.peek();
				MapData sub = (MapData)curr.get(NumberLiteral.one);
				if(sub == null)
				{
					sub = new MapData();
					curr.put(NumberLiteral.one, sub);
				}
				String chars = new String(ch, start, length);
				if(chars.trim().length() == 0) return;
				sub.put(sub.length(), new StringLiteral(chars));
			}
		}
	}
	
	static public class Parse extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom a = args.get(NumberLiteral.one).evalForData();
			return parse(((StringLiteral)a).value);
		}
		
		static public Atom parse(String string)
		{
			BufferedReader br = new BufferedReader(new StringReader(string));
			Atom result = NullLiteral.nullValue;
			try {
				result = Parser.parseExpr(br, 1);
			}
			catch(Exception e) {}
			return result;
		}
	}
	static public class Eval extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom a = args.get(NumberLiteral.one).evalForData();
			
			return eval(a, args, environment);
		}
		
		static public Atom eval(Atom a, Map args, Map environment)
		{ 
			Map env = environment;
			long delta = -1;
			if(args != null)
			{
				Atom in = args.get(new StringLiteral("in"));
				if(in != null)
					env = (Map)in.evalForData();
				Atom n = args.get(NumberLiteral.two);
				if(n != null)
				{
					n = n.evalForData();
					delta = ((NumberLiteral)n).longValue();
				}
			}
			
			return a.deepcopy((int)delta, env);
		}
	}
	static public class Apply extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom fn = args.get(NumberLiteral.one).evalForData();
			Atom myargs = args.get(NumberLiteral.two).evalForData();
			Atom env = args.get(NumberLiteral.three).evalForData();
            
            if(fn == null) System.out.println("fn null");
            if(myargs == null) System.out.println("myargs null");
            if(env == null) System.out.println("env null");
			
			return ((Function)fn).evalAsFunction((Map)myargs, (Map)env);
		}
	}
	static public class Log extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom fn = args.get(NumberLiteral.one).evalForData();
            
			StringWriter sw = new StringWriter();
			Misc.printObject(sw, fn, 0, 0, false);
			
			System.out.println(sw.toString() + " : "+fn.hashCode());
			
			return args.get(NumberLiteral.two);
		}
	}
	static public class Sequence extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			// force first argument
			args.get(NumberLiteral.one).evalForData();
			
			return args.get(NumberLiteral.two);
		}
	}
	static public class Quote extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom a = args.get(NumberLiteral.one).evalForData();
			
			return a.deepcopy(1, null);
		}
	}
	
	static public class ToString extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom a = args.get(NumberLiteral.one);
				
			StringWriter sw = new StringWriter();
			Misc.printObject(sw, a, 0, 0, false);
			
			return new StringLiteral(sw.toString());
		}
	}
	
	static public class StringApp extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{	
			Atom a = args.get(NumberLiteral.one);
			Atom b = args.get(NumberLiteral.two);
			return new StringAppend("", a, b);
			/*			LinkedList ll = new LinkedList();
			NumberLiteral ind = NumberLiteral.one;
			while(true)
			{
				Atom a = args.get(ind);
				if(a == null) break;
				ll.offer(a);
				ind = ind.add(NumberLiteral.one);
			}
			return new StringAppend("", ll);
*/
		}
	}
	
	static public class Append extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Map a = (Map)args.get(NumberLiteral.one).evalForData();
			Atom b = args.get(NumberLiteral.two);
			return MapUnion.unionWithAppend(a, b);
		}
	}
	static public class Meta extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom a = args.get(NumberLiteral.one).evalForData();
			if(a.meta != null)
			{
				return a.meta;
			}
			return new MapData();
		}
	}
	static public class SetMeta extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom a = args.get(NumberLiteral.one).evalForData();
			Map b = (Map)args.get(NumberLiteral.two).evalForData();
			
			a = a.copy();
			a.meta = b;
			return new MObject(a);
		}
	}
	static public class LoadFile extends Function
	{
		public Atom evalAsFunction(Map args, Map environment)
		{
			Atom a = args.get(NumberLiteral.one).evalForData();
			
			return load(((StringLiteral)a).value);
		}
		
		static public Atom load(String filename)
		{
			try{
				return load(new URL(Misc.baseURL, filename));
			}catch(Exception ee){}
			return NullLiteral.nullValue;
		}
		
		static public Atom load(URL fileURL)
		{
			try{
				URLConnection conn = fileURL.openConnection();
				conn.connect();
	  
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuffer fileData = new StringBuffer(1000);
				char[] buf = new char[1024];
				int numRead=0;
				while((numRead=br.read(buf)) != -1)
				{
					String readData = String.valueOf(buf, 0, numRead);
					fileData.append(readData);
					buf = new char[1024];
				}
				br.close();
				return new StringLiteral(fileData.toString());
			}catch(Exception ee){System.out.println(ee);}
			
			System.out.println("Can't open file "+fileURL);
			return NullLiteral.nullValue;
		}
	}
}
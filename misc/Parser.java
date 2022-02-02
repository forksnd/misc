import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Vector;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Parser
{

static public String version()
{
	return "$Id: Parser.java 108 2006-02-21 11:22:10Z will $";
}

static public int consumeWhite(BufferedReader s) throws Exception
{
	s.mark(1);
	int character = s.read();
	if(character == -1) return 0;
	
	int num_returns = 0;
	while(Character.isWhitespace(character) && character != -1)
	{
		if(character == '\n')
			num_returns++;
		
		s.mark(1);
		character = s.read();
	}
	
	s.reset();
	if(num_returns == 0) return -1;
	return num_returns;
}

static HashSet<Character> specialChars = new HashSet<Character>();
static HashMap<Character, Character> escapeChars = new HashMap<Character, Character>();

static {

	specialChars.add(':');
	specialChars.add('@');
	specialChars.add('[');
	specialChars.add(']');
	specialChars.add('{');
	specialChars.add('}');
	specialChars.add('(');
	specialChars.add(')');
	specialChars.add('\'');
	specialChars.add(',');
	
	escapeChars.put('"', '"');
	escapeChars.put('\\', '\\');
	escapeChars.put('/', '/');
	escapeChars.put('b', '\b');
	escapeChars.put('f', '\f');
	escapeChars.put('n', '\n');
	escapeChars.put('r', '\r');
	escapeChars.put('t', '\t');

}

static public Atom consumeName(BufferedReader s, int dlevel) throws Exception
{
	s.mark(1);
	int character = s.read();
	int qchar;
	StringBuffer buf = new StringBuffer();
	boolean quoted = false;
	
	if(character == '"')
	{
		boolean escaped = false;
		quoted = true;
		
		while(true)
		{
			character = s.read();
			if(escaped)
			{
				//need \uABCD
				buf.append(escapeChars.get((char)character));
				escaped = false;
			}
			else if(character == '"')
				break;
			else if(character == '\\')
				escaped = true;
			else
				buf.append((char)character);
		}
		
		s.mark(1);
	}
	else
	{
		while(!Character.isWhitespace(character) && character != -1 && specialChars.contains((char)character) == false && !(buf.length() > 0 && character == '/'))
		{
			buf.append((char)character);
			s.mark(1);
			character = s.read();
		}
	}

	s.reset();
	if(!quoted)
	{
		if(buf.length() == 0)
			return null;
		
		try {
			if(dlevel >= 0)
				return MObject.withNumber(buf.toString(), dlevel);
			return new NumberLiteral(buf.toString());
		}
		catch(Exception e) {}
	}
	
	String str = buf.toString();
	Atom result;
	
	if(quoted || dlevel < 0) result = new StringLiteral(str);
	else result = MObject.withName(new StringLiteral(str), dlevel);
	
	return result;
}

static public int parseQuotes(BufferedReader s) throws Exception
{
	int dlevel = 0;
	
	s.mark(1);
	int character = s.read();
	if(character == -1) return dlevel;
	
	if(character == '`') return Integer.MIN_VALUE;
	
	while(character == '\'' || character == ',')
	{
		if(character == ',')
			dlevel--;
		else
			dlevel++;
		s.mark(1);
		character = s.read();
		if(character == -1) return dlevel;
	}
	s.reset();
	
	return dlevel;
}

static public Atom parseExpr(BufferedReader s, int dlevel) throws Exception
{
	consumeWhite(s);
	
	int quotes = parseQuotes(s);
	if(quotes != Integer.MIN_VALUE)
		dlevel += quotes;
		
	s.mark(2);
	int character = s.read();
	if(character == '/')
	{	
		character = s.read();
		s.reset();
		
		if(Character.isWhitespace(character) || character == ':')
			return consumeName(s, dlevel);
		character = s.read();
		
		Vector q = new Vector();
		
		while(true)
		{
			q.add(parseExpr2(s, dlevel));
			
			s.mark(1);
			character = s.read();
			if(character != '/')
				break;
		}
		s.reset();
		
		Atom result = (Atom)q.remove(0);
		while(q.isEmpty() == false)
		{
			Atom next = (Atom)q.remove(0);
			
			MapData map = new MapData();
			map.put(NumberLiteral.zero, Misc.rootEnvironment.get(new StringLiteral("get")));
			map.put(NumberLiteral.one, result);
			map.put(NumberLiteral.two, next);
			
			result = MObject.withAtom(map, dlevel);
		}
		return result;
	}
	s.reset();
	
	return parseExpr2(s, dlevel);
}

static public Atom parseExpr2(BufferedReader s, int dlevel) throws Exception
{
	consumeWhite(s);
	
	int quotes = parseQuotes(s);
	if(quotes != Integer.MIN_VALUE)
		dlevel += quotes;
	
	s.mark(1);
	int character = s.read();
	
	boolean strict = false;
	if(character == '!')
	{
		strict = true;
		s.mark(1);
		character = s.read();
	}
	
	Atom result = NullLiteral.nullValue;
	if(character == '[' || character == '{' || character == '(')
	{
		if(quotes == Integer.MIN_VALUE)
			result = parsePairs(s, dlevel);
		else if(character == '(')
			result = MObject.withAtom(parseList(s, dlevel), dlevel);
		else
			result = MObject.withAtom(parsePairs(s, dlevel), (character == '{')? dlevel+1 : dlevel);
		character = s.read(); //character == ']'
	}
	else
	{
		s.reset();
		result = consumeName(s, dlevel);
	}
	s.mark(1);
	character = s.read();
	if(character == '@')
	{
		consumeWhite(s);
		character = s.read();
		if(character == '[')
		{
			if(result.meta != null)
				((MapData)result.meta).putAll( (MapData)parsePairs(s, dlevel) );
			else
				result.meta = parsePairs(s, dlevel);
			character = s.read(); //character == '>'
		}
		else
			s.reset();
	}
	else
		s.reset();
	
	if(strict)
		result.putMeta(StringLiteral.strict, BoolLiteral.yes);
	
	return result;
}

static public Map parseList(BufferedReader s, int dlevel) throws Exception
{
	MapData map = new MapData();
	
	Atom a = parseExpr(s, dlevel);
	if(a == null) return null;
	map.put(NumberLiteral.zero, a);
	
	Atom b = parseList(s, dlevel);
	if(b != null) map.put(NumberLiteral.one, MObject.withAtom(b, dlevel));
	
	return map;
}

static public Map parsePairs(BufferedReader s, int dlevel) throws Exception
{
	NumberLiteral unnamed_index = NumberLiteral.zero;
	MapData map = new MapData();
	MapData ppmap = new MapData();
	int counter = 0;
	
	while(true)
	{
		int num_returns = consumeWhite(s);
		if(num_returns > 0)
		{
			ppmap.put(ppmap.length(), new NumberLiteral(num_returns));
		}

		//parse a key
		Atom key = parseExpr(s, dlevel);
		if(key == null) break;
		
//		consumeWhite(s);
		
		//if is named (eg A:B) then record otherwise it is an unnamed value
		s.mark(1);
		int character = s.read();
		if(character == ':')
		{
			consumeWhite(s);
			
			Atom object = parseExpr(s, dlevel);
			
			if(key.equals("#"))
			{
                MapData comment = new MapData();
                comment.put(NumberLiteral.zero, new StringLiteral("comment"));
                comment.put(NumberLiteral.one, object);
                ppmap.put(ppmap.length(), comment);
				continue;
			}
			else
			{
				if(object != null)
				{
					key.meta = null;
					map.put(key, object);
                    
                    MapData ppkey = new MapData();
                    ppkey.put(NumberLiteral.zero, new StringLiteral("key"));
                    ppkey.put(NumberLiteral.one, key);
                    ppmap.put(ppmap.length(), ppkey);
				}
				if(key.getClass() == NumberLiteral.class)
					unnamed_index = ((NumberLiteral)key).add(NumberLiteral.one);
			}
		}
		else
		{
			s.reset();
			map.put(unnamed_index, key);
                    
            MapData ppkey = new MapData();
            ppkey.put(NumberLiteral.zero, new StringLiteral("key"));
            ppkey.put(NumberLiteral.one, unnamed_index);
            ppmap.put(ppmap.length(), ppkey);
                    
			unnamed_index = unnamed_index.add(NumberLiteral.one);
		}
		counter++;
	}
    if(ppmap.dataMap.size() > 1) {
        MapData styleMap = new MapData();
        styleMap.put(new StringLiteral("format"), ppmap);
        map.putMeta(new StringLiteral("style"), styleMap);
    }
	return map;
}
}

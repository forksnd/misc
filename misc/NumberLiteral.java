import java.math.BigDecimal;

public class NumberLiteral extends DataAtom
{
	BigDecimal bi;
	static NumberLiteral zero = new NumberLiteral(0);
	static NumberLiteral one = new NumberLiteral(1);
	static NumberLiteral two = new NumberLiteral(2);
	static NumberLiteral three = new NumberLiteral(3);
	static NumberLiteral four = new NumberLiteral(4);
	static NumberLiteral five = new NumberLiteral(5);
	static NumberLiteral six = new NumberLiteral(6);
	
	public NumberLiteral(String s)
	{
		bi = new BigDecimal(s);
	}
	
	public NumberLiteral(BigDecimal b)
	{
		bi = b;
	}
	
	public NumberLiteral(long b)
	{
		bi = BigDecimal.valueOf(b);
	}
	
	static public NumberLiteral numberFrom(Object o)
	{
		if(o.getClass() == NumberLiteral.class)
			return (NumberLiteral)o;
		
		return null;
	}
	
	public Atom copy()
	{
		NumberLiteral sl = new NumberLiteral(bi);
		if(meta != null) sl.meta = (Map)meta.copy();
		return sl;
	}
	
	public NumberLiteral add(NumberLiteral s)
	{
		NumberLiteral nl = new NumberLiteral(bi.add(s.bi));
		return nl;
	}
	
	public NumberLiteral multiply(NumberLiteral s)
	{
		BigDecimal bd = bi.multiply(s.bi);
		if(bd.scale() > 5) bd = bd.setScale(5, BigDecimal.ROUND_HALF_UP);
		return new NumberLiteral(bd);
	}
	
	public NumberLiteral subtract(NumberLiteral s)
	{
		return new NumberLiteral(bi.subtract(s.bi));
	}
	
	public NumberLiteral divide(NumberLiteral s)
	{
		return new NumberLiteral(bi.divide(s.bi, 5, BigDecimal.ROUND_HALF_UP));
	}
	
	public NumberLiteral remainder(NumberLiteral s)
	{
		return new NumberLiteral(bi.remainder(s.bi));
	}
	
	public String toString()
	{
		return (isName()? "$" : "")+ bi.toString();
	}
	
	public int compareTo(Object o)
	{
		return bi.compareTo(((NumberLiteral)o).bi);
	}
	
	public int compareTo(long n)
	{
		return bi.compareTo(BigDecimal.valueOf(n));
	}
	
	public long longValue()
	{
		return bi.longValue();
	}
	
	public float floatValue()
	{
		return bi.floatValue();
	}
	
	public int hashCode()
	{
		return bi.hashCode();
	}
	
	public boolean equals(Object o)
	{
		if(o.getClass() != NumberLiteral.class) return false;
		return (bi.compareTo(((NumberLiteral)o).bi) == 0);
	}
}

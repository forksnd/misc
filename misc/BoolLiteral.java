public class BoolLiteral extends DataAtom
{
	static BoolLiteral yes = new BoolLiteral();
	static BoolLiteral no = new BoolLiteral();
	
	private BoolLiteral()
	{
		
	}
	
	static public BoolLiteral get(boolean b)
	{
		if(b) return yes;
		return no;
	}
		
	public String toString()
	{
		if(this == yes)
			return "true";
		return "false";
	}
}
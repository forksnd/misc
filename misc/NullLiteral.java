public class NullLiteral extends DataAtom
{
	static NullLiteral nullValue = new NullLiteral();
	
	private NullLiteral()
	{
	}
	
	public Atom copy()
	{
		return this;
	}
	
	public String toString()
	{
		return "nil";
	}
}

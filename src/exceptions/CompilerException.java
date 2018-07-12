package exceptions;

public class CompilerException extends KaizenException{

	private static final long serialVersionUID = 1L;
	
	String errorMessage;

	public CompilerException(String msg)
	{
		super(msg);
		errorMessage = msg;
	}
	
	public String getErrorMessage()
	{
		return errorMessage;
	}
	
}

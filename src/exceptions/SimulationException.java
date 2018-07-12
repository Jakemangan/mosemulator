package exceptions;

public class SimulationException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	String errorMessage;

	public SimulationException(String msg)
	{
		super(msg);
		errorMessage = msg;
	}
	
	public String getErrorMessage()
	{
		return errorMessage;
	}
	
	
}

package compiler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.CompilerException;
import util.Utils;

/*
 * TODO: Fix "[COMPILER EXCEPTION] Syntax Error: (LOOPX,X) can only contain one X or Y symbol
 */

public class SyntaxChecker {

	private String errorMessage;
	private int TOKEN_TYPE = -1; //1 for branch, 2 for instruction, 3 for operand
	private boolean errorFound = false;
	
	private InstructionTable insTable;
	
	public SyntaxChecker()
	{
		insTable = new InstructionTable();
	}
	
	public boolean checkTokenSyntax(int tokenNum, String token) throws CompilerException
	{
		errorFound = false;
		errorMessage = "";
		
		
		
		determineTokenType(token);
		System.out.println("Token " + token + " is of type " + TOKEN_TYPE);
		
		if(TOKEN_TYPE == 1)
		{
			System.out.println("Checking " + "\"" + token + "\" against branch syntax.");
			checkBranchSyntax(token);
		}
			
		if(TOKEN_TYPE == 2)
		{
			System.out.println("\"" + token + "\" is an instruction. No need to check syntax.");
			
		}
				
		if(TOKEN_TYPE == 3)
		{
			System.out.println("Checking " + "\"" + token + "\" against operand syntax.");
			checkOperandSyntax(token);
		}
			
		
		System.out.println("");
		return errorFound;
	}
	
	/*
	 * If token matches with any symbol in the symbol table then it is an instruction
	 * If the token doesnt match with a symbol but contains an underscore, it is a branch label
	 * If the token does not meet either of the above conditions, it is an operand
	 * 
	 * There is no need to check the syntax of instructions, as only instructions will
	 * match with an entry in the symbol table and as such will always be correct
	 */
	public void determineTokenType(String token)
	{	
		boolean tokenDetermined = false;
		
		for(String symbol : insTable.getSymbols())
		{
			if(token.equals(symbol) && !tokenDetermined)
			{
				TOKEN_TYPE = 2;
				tokenDetermined = true;
			}
		}
		
		if(token.contains("_") && !tokenDetermined)
		{
			TOKEN_TYPE = 1;
			tokenDetermined = true;
		}
		
		if(!tokenDetermined)
		{
			TOKEN_TYPE = 3;
			tokenDetermined = true;
		}
		
	}
	
	/*
	 * Branch labels cannot contain any characters aside from alphanumeric
	 */
	public void checkBranchSyntax(String token)
	{
		token = token.substring(1);
		
		Pattern p = Pattern.compile("[^a-zA-Z$]");
		Matcher m = p.matcher(token);
		
		
		if(m.find())
		{
			errorFound = true;
			setErrorMessage(token + " contains illegal character");
		}
	}
	
	/*
	 * 	  Operands cannot contain symbols outside of "( ) $ # , + -" DONE
	 *    Opening parentheses in operands must be closed.  DONE
	 *    Operands can only contain one pair of parentheses. DONE
	 *    $ and # must be followed by numeric characters in operands. WIP - need methods for both $ and #
	 *    Commas must exist between a value and a value OR a parenthesis and a value i.e. (xxx,yyy) or (xxx),yyy DONE 
	 *    All operands must be below 0xFF DONE
	 */
	public void checkOperandSyntax(String token) throws CompilerException
	{
		checkIllegalCharacters(token);
		checkBranchLabelLettersOnly(token);
		checkNumericValues(token);
		checkParenthesesClosed(token);
		checkParenthesesNumber(token);
		checkNumericValueSymbols(token);
		checkCommaNeighbours(token);
	}
	
	
	
	
	
	/*
	 * 
	 * Individual syntax checking methods
	 * 
	 */
	
	
	/*
	 * Ensure branch instructions i.e. BNE etc are the only instructions to have operands
	 * that include branch labels. Other instructions (ADC, LDA, etc) can NOT have branch 
	 * labels as their operands
	 * "BNE LOOP" ok
	 * "ADC LOOP" not ok
	 */

	
	
	/*
	 * Operands cannot contain symbols outside of "( ) $ # , + - a-z A-Z 0-9"
	 */
	private void checkIllegalCharacters(String token)
	{
		token = token.toUpperCase();
		
		Pattern p = Pattern.compile("[^(),$#*+-a-zA-Z0-9]"); //match any character that is not in this set
		Matcher m = p.matcher(token);
		
		if(m.find())
		{
			errorFound = true;
			setErrorMessage(token + " contains illegal character");
		}
		
		int commaCount = token.length() - token.replace(",", "").length();
		
		if(commaCount > 1)
		{
			errorFound = true;
			setErrorMessage(token + " cannot contain more than one comma");
		}
		
		int xyCount = token.length() - token.replace("X", "").length();
		xyCount = xyCount + token.length() - token.replace("Y", "").length();
		
		if(xyCount > 1)
		{
			errorFound = true;
			setErrorMessage(token + " can only contain one X or Y symbol");
		}
		
		if(token.contains("(") && token.contains(")") && token.contains(","))
		{
			System.out.println("Token is either indexed indirect or indirect indexed");
			
			int indexOfClosingBracket = token.indexOf(")");
			int indexOfComma = token.indexOf(",");
			
			if(indexOfClosingBracket > indexOfComma) //indexed indirect
			{
				String substring = token.substring(indexOfComma);
				if(substring.contains("X"))
				{
					System.out.println("Indexed indirect token contains X at correct position");
				}
				else
				{
					errorFound = true;
					setErrorMessage("Indexed indirect token does not contain X or does not contain X at correct position: " + token);
				}
			}
			
			if(indexOfClosingBracket < indexOfComma) //indirect indexed
			{
				String substring = token.substring(indexOfComma);
				if(substring.contains("Y"))
				{
					System.out.println("Indirect indexed token contains Y at correct position");
				}
				else
				{
					errorFound = true;
					setErrorMessage("Indirect indexed token does not contain Y or does not contain Y at correct position: " + token);
				}
			}
			
			
		}
	}
	
	/*
	 * Check branch labels only contain letters, no numerics
	 */
	private void checkBranchLabelLettersOnly(String token)
	{
		if(token.contains("#")) 
		{
			System.out.println("[CBLLO] " + token + " is not a branch label");
		} 
		else if(token.contains("$")) 
		{
			System.out.println("[CBLLO] " + token + " is not a branch label");
		} 
		else if(token.contains("*")) 
		{
			System.out.println("[CBLLO] " + token + " is not a branch label");
		} 
		else if(token.contains(".")) 
		{
			System.out.println("[CBLLO] " + token + " is not a branch label");
		}
		else
		{
			System.out.println("[CBLLO] Token before special char removal: " + token);
			String editedToken = token;
			editedToken = editedToken.replace("(", ""); //remove special character from branch label
			editedToken = editedToken.replace(")", "");
			editedToken = editedToken.replace("X", "");
			editedToken = editedToken.replace("Y", "");
			editedToken = editedToken.replace(",", "");
			editedToken = editedToken.replace("#", "");
			editedToken = editedToken.replace("$", "");
			editedToken = editedToken.replace("+", "");
			editedToken = editedToken.replace("-", "");
			System.out.println("[CBLLO] Token after special char removal: " + editedToken);
			
			editedToken = editedToken.replaceAll("[*a-zA-Z]", ""); //remove all letters from string
			
			if(editedToken.length() > 0) //if string still contains characters after all letters have been removed, then numbers are present
			{
				errorFound = true;
				setErrorMessage("Error with branch label: " + token + " \nBranch labels must only contains letters");
			}
		}
		
		
	}
	
	
	/*
	 * Opening parentheses in operands must be closed.
	 */
	private void checkParenthesesClosed(String token)
	{

		if(token.contains("("))
		{
			if(!token.contains(")"))
			{
				errorFound = true;
				setErrorMessage(token + " contains open parenthesis but not closing parenthesis");
			}		
		}
		
		if(token.contains(")"))
		{
			if(!token.contains("("))
			{
				errorFound = true;
				setErrorMessage(token + " contains closing parentheses but not opening parentheses");
			}		
		}
	}
	
	/*
	 * Operands can only contain one pair of parentheses
	 */
	private void checkParenthesesNumber(String token)
	{
		int count = token.length() - token.replace("(", "").length();
		if(count > 1)
		{
			errorFound = true;
			setErrorMessage(token + " contains more than one set of parentheses");
		}
	}
	
	/*
	 * $ and # must be followed by numeric characters in operands. NOT WORKING
	 */
	private void checkNumericValueSymbols(String token) throws CompilerException
	{

		if(token.contains("$"))
		{
			System.out.println("[CNVS] $ found.");
			int symbolPosition = token.indexOf("$");
			int startPosition = symbolPosition + 1; //current char to check is next char after $
			int endPosition = 0;

			if(token.contains(")") && !token.contains(","))
			{
				System.out.println("[CNVS] Token contains \")\", only checking numerics before this symbol");
				endPosition = token.indexOf(")");
			}
			
			if(token.contains(",") && !token.contains(")"))
			{
				System.out.println("[CNVS] Token contains \",\", only checking numerics before this symbol");
				endPosition = token.indexOf(",");
			}
			
			if(token.contains(",") && token.contains(")"))
			{
				int indexOfClosingBracket = token.indexOf(")");
				int indexOfComma = token.indexOf(",");
				
				if(indexOfClosingBracket < indexOfComma) //i.e. indirect indexed ($40),Y
				{
					System.out.println("[CNVS] Token closing bracket comes before token comma, using closing bracket for end position");
					endPosition = indexOfClosingBracket;
				}
				else
				if(indexOfClosingBracket > indexOfComma) //i.e. indexed indirect ($40,X)
				{
					System.out.println("[CNVS] Token comma comes before token closing bracket, using comma for end position");
					endPosition = indexOfComma;
				}
			}
			
			if(!token.contains(")") && !token.contains(","))
			{
				System.out.println("[CNVS] Token does not contain either \" or ), checking all numerics");
				endPosition = token.length();
			}
			
			
			
			System.out.println("[CNVS] char end position = " + endPosition);
			for(int i = startPosition; i < endPosition; i++)
			{
				char currentChar = token.charAt(i); 
				System.out.println("[CNVS] " + currentChar);
				
				Pattern p = Pattern.compile("[^0-9ABCDEFabcdef+-]"); //match any character that is not a number or hexadecimal
				Matcher m = p.matcher(Character.toString(currentChar));
				
				if(m.find())
				{
					errorFound = true;
					setErrorMessage("Invalid characters for token: " + token + ". Characters immediately after '$' must be hexidecimal format only");
				}
			}
			
			
		}
		
		if(token.contains("#"))
		{
			System.out.println("[CNVS] # found.");
			int symbolPosition = token.indexOf("#");
			int startPosition = symbolPosition + 1; //current char to check is next char after $
			int endPosition;
			
			if(token.contains(")"))
			{
				System.out.println("[CNVS] Token contains \")\", only checking numerics before this symbol");
				endPosition = token.indexOf(")");
			}
			
			if(token.contains(","))
			{
				System.out.println("[CNVS] Token contains \",\", only checking numerics before this symbol");
				endPosition = token.indexOf(",");
			}
			else
			{
				System.out.println("[CNVS] Token does not contain either \" or ), checking all numerics");
				endPosition = token.length();
			}
			
			
			
			System.out.println("[CNVS] char end position = " + endPosition);
			for(int i = startPosition; i < endPosition; i++)
			{
				char currentChar = token.charAt(i); 
				System.out.println("[CNVS]" + currentChar);
				
				Pattern p = Pattern.compile("[^0-9ABCDEFabcdef]"); //match any character that is not a number or hexadecimal
				Matcher m = p.matcher(Character.toString(currentChar));
				
				if(m.find())
				{
					errorFound = true;
					setErrorMessage("Invalid characters for token: " + token + ". Characters immediately after '#' must be hexadecimal format only");
				}
			}
			
			
		}
		
	}
	
	/*
	 * Commas must exist between a value and a value OR a parenthesis and a value i.e. (xxx,yyy) or (xxx),yyy
	 */
	private void checkCommaNeighbours(String token) throws CompilerException
	{

		if(token.contains(","))
		{
			System.out.println("[CCN] Comma found");
			int commaPosition = token.indexOf(","); //get position of comma
			boolean isBranchLabel = false;
			
			String slicedToken = token.substring(0, commaPosition); //slice token to remove chars including and after the comma
			slicedToken = slicedToken.replace("(", "");
			System.out.println("[CCN] Slicedtoken: " + slicedToken);
			
			boolean allLetters = slicedToken.chars().allMatch(Character::isLetter); //if sliceToken contains only letters, then token is a branch label
			
			if(allLetters)
				isBranchLabel = true;
			else if(!allLetters)
				isBranchLabel = false;
			
			char leftSide = '\0'; //initalise char to value 0
			char rightSide = '\0'; //initalise char to value 0
			
			try
			{
				leftSide = token.charAt(commaPosition - 1); //get character to left side of comma
				rightSide = token.charAt(commaPosition + 1); //get character to right side of comma
			}
			catch(IndexOutOfBoundsException e) //catch exception in case of "$10," strings or similar where comma is final character
			{
				throw new CompilerException("[COMPILER EXCEPTION] Syntax Error: Comma cannot be final character in token: " + token);
			}
			System.out.println("[CCN] Left side of comma: " + leftSide);
			System.out.println("[CCN] Right side of comma: " + rightSide);
			
			Pattern p = null;
			
			if(isBranchLabel)
				p = Pattern.compile("[^A-Za-z)0-9]"); //match any character that is not in this set
			else
				p = Pattern.compile("[^A-Fa-f)0-9]"); //match any character that is not in this set
			
			Matcher m = p.matcher(Character.toString(leftSide));
			
			if(m.find())
			{
				errorFound = true;
				setErrorMessage(token + " contains illegal character on left side of comma");
			}
			
			p = Pattern.compile("[^XxYy]"); //match any character that is not in this set
			
			m = p.matcher(Character.toString(rightSide));
			
			if(m.find())
			{
				errorFound = true;
				setErrorMessage(token + " contains illegal character on right side of comma");
			}
			
			if(token.substring(token.length()-1).equals(",")) //ensures final character in token is not a comma i.e. "($40), or $30,X,
			{
				errorFound = true;
				setErrorMessage("Comma cannot be final character in token: " + token);
			}
		}
	}
	
	/*
	 * All numeric values must be below 0xFF and above 0x00
	 * This method really needs to be re-done
	 */
	private void checkNumericValues(String token) throws CompilerException
	{
		token = token.toUpperCase();
		
		if(token.contains("$") || token.contains("#"))
		{
			System.out.println("[CNV] Token before special char removal: " + token);
			token = token.replace("(", ""); 
			token = token.replace(")", "");
			token = token.replace("X", "");
			token = token.replace("Y", "");
			token = token.replace(",", "");
			token = token.replace("#", "");
			token = token.replace("$", "");
			System.out.println("[CNV] Token after special char removal: " + token);
			
			int finalValue = determineTokenDecimalValue(token);
			
			System.out.println("[CNV] Final value: " + finalValue);
			
			if(finalValue < 0) //if numerical value is greater than 0xFF
			{
				errorFound = true;
				setErrorMessage(token + " contains a value that is less than 0x00.");
			}
			
			if(finalValue > 255) //if numerical value is greater than 0xFF
			{
				errorFound = true;
				setErrorMessage(token + " contains a value that is greater than 0xFF (255).");
			}
		}
		else
		{
			System.out.println("[CNV] Token before special char removal: " + token);
			token = token.replace("(", ""); //remove special character from branch label
			token = token.replace(")", "");
			token = token.replace("X", "");
			token = token.replace("Y", "");
			token = token.replace(",", "");
			System.out.println("[CNV] Token after special char removal: " + token);
			
			
			/*
			 * Slice relative value off end of token and pass to matcher.
			 * If value is a branch label i.e. only contains A-Z, then it 
			 * will pass the matcher as intended.
			 */
			
			boolean relative = false;
			
			String slicedToken = token; //alternative string that can be altered to preserve the original token
			
			if(slicedToken.contains("+"))
			{
//				int indexOfSymbol = token.indexOf("+"); //get the index of the + symbol
//				slicedToken = token.substring(0, indexOfSymbol);
				
				relative = true;
			}
			
			if(slicedToken.contains("-"))
			{
//				int indexOfSymbol = slicedToken.indexOf("-"); //get the index of the + symbol
//				slicedToken = slicedToken.substring(0, indexOfSymbol);
				
				relative = true;
			}
			
			if(slicedToken.contains(","))
			{
				int indexOfSymbol = slicedToken.indexOf(","); //get the index of the + symbol
				slicedToken = slicedToken.substring(0, indexOfSymbol);
			}
			
			Pattern p = Pattern.compile("^[A-Z]+$"); //check to see if token is only alphabetical characters, if so token is a branch label and does not need checked
			Matcher m = p.matcher(token);
			
			if(m.find())
			{
				if(slicedToken.equals("A") && slicedToken.length() == 1)
					System.out.println("[CNV] Token " + token + " is an accumulator operand and does not require value checking");
				else
					System.out.println("[CNV] Token " + token + " is a branch label and does not require value checking");
			}
			else //check numerical value of token to ensure it is not above 0xFF
			{
				int finalValue = determineTokenDecimalValue(slicedToken);
				
				System.out.println("[CNV] Final value: " + finalValue);
				
				if(!relative)
				{
					if(finalValue < 0) //if numerical value is greater than 0xFF
					{
						errorFound = true;
						setErrorMessage(token + " contains a value that is less than 0x00.");
					}
					
					if(finalValue > 255) //if numerical value is greater than 0xFF
					{
						errorFound = true;
						setErrorMessage(token + " contains a value that is greater than 0xFF (255).");
					}
				}
				else
				if(relative)
				{
					if(finalValue > 127)
					{
						errorFound = true;
						setErrorMessage(token + " is a 8-bit relative offset value and therefore must between -128 and +127 decimal (-80 and 7F hexadecimal).");
					}
					
					if(finalValue < -128)
					{
						errorFound = true;
						setErrorMessage(token + " is a 8-bit relative offset value and therefore must between -128 and +127 decimal (-80 and 7F hexadecimal).");
					}
				}
			}
		}
	}
	
	/*
	 * Utility method for checkNumericValues [CNV] method, takes a string as input, parses the decimal value from the
	 * string then returns the final decimal value
	 */
	private int determineTokenDecimalValue(String token)
	{
		int finalValue;
		
		if(token.contains("+")) //if token contains a relative value using +
		{
//			int indexOfSymbol = token.indexOf("+"); //get the index of the + symbol
//			String tokenNumerics = token.substring(0, indexOfSymbol); //substring token to extract only the base value with no relative
//			//tokenNumerics = token.replaceAll("[^0-9ABCDEFabcdef]", ""); //remove all non-numerical characters from string
//			System.out.println("[CNV] TN: " + tokenNumerics);
//			int tokenDecimal = Utils.HexToDec(tokenNumerics); //convert hex value to decimal  
//			System.out.println("[CNV] TokenDecimal: " + tokenDecimal);
//			finalValue = tokenDecimal;
//			
//			//Add on the relative value to the base value if one exists
//			String relativeValue = token.substring(indexOfSymbol+1); //use the + symbol index to find the relative value to be added
//			System.out.println("[CNV] Relative value (hex): " + relativeValue);
//			int relativeValueDec = Utils.HexToDec(relativeValue); //convert the relative value to decimal
//			System.out.println("[CNV] Relative value (decimal): " + relativeValueDec);
//			finalValue = finalValue + relativeValueDec; //add the relative value to the token value
			System.out.println("Token contains \"+\"");
			token = token.replace("*", "");
			token = token.replace("+", "");
			String tokenNumerics = token;
			System.out.println("[CNV] TN: " + tokenNumerics);
			int tokenDecimal = Utils.HexToDec(tokenNumerics); //convert hex value to decimal  
			System.out.println("[CNV] TokenDecimal: " + tokenDecimal);
			finalValue = tokenDecimal;
		}
		else
		if(token.contains("-")) //if token contains a relative value using +
		{
//			int indexOfSymbol = token.indexOf("-"); //get the index of the + symbol
//			String tokenNumerics = token.substring(0, indexOfSymbol); //substring token to extract only the base value with no relative
//			//tokenNumerics = token.replaceAll("[^0-9ABCDEFabcdef]", ""); //remove all non-numerical characters from string
//			System.out.println("[CNV] TN: " + tokenNumerics);
//			int tokenDecimal = Utils.HexToDec(tokenNumerics); //convert hex value to decimal  
//			System.out.println("[CNV] TokenDecimal: " + tokenDecimal);
//			finalValue = tokenDecimal;
//			
//			//Add on the relative value to the base value if one exists
//			String relativeValue = token.substring(indexOfSymbol+1); //use the + symbol index to find the relative value to be added
//			System.out.println("[CNV] Relative value (hex): " + relativeValue);
//			int relativeValueDec = Utils.HexToDec(relativeValue); //convert the relative value to decimal
//			System.out.println("[CNV] Relative value (decimal): " + relativeValueDec);
//			finalValue = finalValue - relativeValueDec; //add the relative value to the token value
			System.out.println("Token contains \"-\"");
			token = token.replace("*", "");
			token = token.replace("-", "");
			String tokenNumerics = token;
			System.out.println("[CNV] TN: " + tokenNumerics);
			int tokenDecimal = Utils.HexToDec(tokenNumerics); //convert hex value to decimal  
			System.out.println("[CNV] TokenDecimal: " + tokenDecimal);
			finalValue = tokenDecimal; 
			finalValue = -finalValue; //negate value since offset is a negative
			
		}
		else
		{
			String tokenNumerics = token.replaceAll("[^0-9ABCDEFabcdef]", ""); //remove all non-numerical characters from string
			System.out.println("[CNV] TN: " + tokenNumerics);
			int tokenDecimal = Utils.HexToDec(tokenNumerics); //convert hex value to decimal  
			System.out.println("[CNV] TokenDecimal: " + tokenDecimal);
			finalValue = tokenDecimal;
		}
		
		return finalValue;
	}
	
	
	
	
	public void setErrorMessage(String msg)
	{
		errorMessage = msg;
	}
	
	public String getErrorMessage()
	{
		return errorMessage;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}

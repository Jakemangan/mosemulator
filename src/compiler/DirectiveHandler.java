package compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.CompilerException;
import util.Utils;

/*
 * Syntax checks:
 * 	- Memory addresses and memory values contain $
 *  - Memory addresses and memory values only contain hexadecimal values (0-9 and A-F)
 *  - Memory addresses and memory values actually contain a numerical value
 *  - Memory address to write to does not exceed 0xff after being incremented
 *  - Memory address and memory values are between 0-255 dec
 *  - Directives dc.b and dc.s contain only three tokens
 *  - Directive dc.v contains at least three tokens
 */

public class DirectiveHandler {

	String[] directiveLines;
	String[] directiveBinaryOutput;
	
	int numberOfLines;
	
	/*
	 * The directive will 
	 */
	private static final int BYTE_DIRECTIVE = 1; 
	private static final int STRING_DIRECTIVE = 2;
	private static final int VALUEARRAY_DIRECTIVE = 3;
	
	private int directiveType = -1;
	
	
	public DirectiveHandler(String[] directiveLines)
	{
		this.directiveLines = directiveLines;
		numberOfLines = directiveLines.length;
	}
	
	public void constructDirectiveOutput() throws CompilerException
	{
		System.out.println("Construct Directive Output:");
		
		for(String dLine : directiveLines)
		{
			determineDirectiveType(dLine);
			checkDirectiveSyntax(dLine);
			convertLineToBinary(dLine);
		}
		
		System.out.println("");
		printDirectiveBinaryOutputContents();
	}
	
	public void determineDirectiveType(String dLine) throws CompilerException
	{
			if(dLine.contains("dc.b"))
			{
				directiveType = 1;
			}
			else
			if(dLine.contains("dc.s"))
			{
				directiveType = 2;
			}
			else
			if(dLine.contains("dc.v"))
			{
				directiveType = 3;
			}
			else
			{
				throw new CompilerException("[Compiler] Line: " + dLine + " has invalid directive instruction");
			}
	}
	
	
	public void checkDirectiveSyntax(String dLine) throws CompilerException
	{
		System.out.println("Check Directive Syntax:");
		System.out.println("Directive type: " + directiveType);
		
		String[] lineTokens = dLine.split(" ");
		
		System.out.print("Line tokens: "); 
		for(String token : lineTokens)
		{
			System.out.print(token + " ");
		}
		System.out.println("");
		
		int numTokens = lineTokens.length;
		boolean syntaxCorrect = true;
		
		String errorMessage = "";
		
		/*
		 * For byte directive:
		 * 1. Line must contain three tokens 
		 * 2. Tokens after the first must contain $
		 * 3. Tokens after the first must only contain 0-9 A-F
		 * 4. Token must contain a numerical value
		 * 5. Tokens numerical value must be between 0-255 
		 * 
		 */
		if(directiveType == 1)
		{
			/*
			 * Test 1
			 */
			if(numTokens > 3 || numTokens < 3)
			{
				syntaxCorrect = false;
				errorMessage = "Line " + dLine + " must contain exactly 3 tokens.";
			}
			
			for(int i = 1; i < numTokens; i++)
			{
				String currentToken = lineTokens[i];
				
				/*
				 * Test 2
				 */
				if(currentToken.contains("$"))
				{
					System.out.println("Token '" + currentToken + "' contains '$'.");
				}
				else
				{
					syntaxCorrect = false;
					errorMessage = "Token '" + currentToken + "' does not contains '$'.";
				}
				
				currentToken = currentToken.replace("$", ""); //remove $ from token
				currentToken = currentToken.toUpperCase();
				
				if(currentToken.length() == 1) //token has a single numerical value
					currentToken = "0".concat(currentToken); //concat a 0 onto the front 
				
				System.out.println("Token with $ removed: " + currentToken);
				
				/*
				 * Test 3
				 */
				if(currentToken.matches("^([0-9A-Fa-f]{2})+$"))
				{
					System.out.println("Token '" + currentToken + "' contains valid hexadecimal values");
				}
				else
				{
					syntaxCorrect = false;
					errorMessage = "Token '" + currentToken + "' does not contain valid hexadecimal values";
				}
				
				/*
				 * Test 4
				 */
				if(currentToken.equals(""))
				{
					syntaxCorrect = false;
					errorMessage = "Token '" + lineTokens[i] + "' must contain a hexadecimal value";
				}
				
				int tokenDecimal = Utils.HexToDec(currentToken);
				System.out.println(currentToken + " : " + tokenDecimal);
				
				if(tokenDecimal > 255 || tokenDecimal < 0)
				{
					syntaxCorrect = false;
					errorMessage = "Token '" + lineTokens[i] + "' must have a value between 0-255 decimal.";
				}
				
			}
		}
		
		
		
		
		
		
		
		
		/*
		 * For string directive:
		 * 1. Line must contain three tokens 
		 * 2. The second token must contain $
		 * 3. The second token must contain A-F0-9, the third token can contain anything
		 * 4. Second token must contain a numerical value
		 * 5. Second tokens numerical value must be between 0-255 
		 * 
		 */
		if(directiveType == 2)
		{
			/*
			 * Test 1
			 */
			if(numTokens > 3 || numTokens < 3)
			{
				syntaxCorrect = false;
				errorMessage = "Line '" + dLine + "' must contain exactly 3 tokens.";
			}
			
			String currentToken = lineTokens[1]; //Second token
			
			/*
			 * Test 2
			 */
			if(currentToken.contains("$"))
			{
				System.out.println("Token '" + currentToken + "' contains '$'.");
			}
			else
			{
				syntaxCorrect = false;
				errorMessage = "Token '" + currentToken + "' does not contains '$'.";
			}
			
			currentToken = currentToken.replace("$", ""); //remove $ from token
			currentToken = currentToken.toUpperCase();
			
			if(currentToken.length() == 1) //token has a single numerical value
				currentToken = "0".concat(currentToken); //concat a 0 onto the front 
			
			System.out.println("Token with $ removed: " + currentToken);
			
			/*
			 * Test 3
			 */
			if(currentToken.matches("^([0-9A-Fa-f]{2})+$"))
			{
				System.out.println("Token '" + currentToken + "' contains valid hexadecimal values");
			}
			else
			{
				syntaxCorrect = false;
				errorMessage = "Token '" + currentToken + "' does not contain valid hexadecimal values";
			}
			
			/*
			 * Test 4
			 */
			if(currentToken.equals(""))
			{
				syntaxCorrect = false;
				errorMessage = "Token '" + lineTokens[1] + "' must contain a hexadecimal value";
			}
			
			int tokenDecimal = Utils.HexToDec(currentToken);
			System.out.println(currentToken + " : " + tokenDecimal);
			
			if(tokenDecimal > 255 || tokenDecimal < 0)
			{
				syntaxCorrect = false;
				errorMessage = "Token '" + lineTokens[1] + "' must have a value between 0-255 decimal.";
			}
				
		}
		
		
		/*
		 * For value array directive:
		 * 1. Line must contain at least three tokens 
		 * 2. All tokens after the first must contain $
		 * 3. All tokens after the first must contain 0-9A-F
		 * 4. All tokens after the first must contain a numerical value
		 * 5. All tokens after the first must have a value between 0-255
		 * 
		 */
		if(directiveType == 3)
		{
			/*
			 * Test 1
			 */
			if(numTokens < 3)
			{
				syntaxCorrect = false;
				errorMessage = "Line " + dLine + " must contain 3 or more tokens.";
			}
			
			for(int i = 1; i < numTokens; i++)
			{
				String currentToken = lineTokens[i];
				
				/*
				 * Test 2
				 */
				if(currentToken.contains("$"))
				{
					System.out.println("Token '" + currentToken + "' contains '$'.");
				}
				else
				{
					syntaxCorrect = false;
					errorMessage = "Token '" + currentToken + "' does not contains '$'.";
				}
				
				currentToken = currentToken.replace("$", ""); //remove $ from token
				currentToken = currentToken.toUpperCase();
				
				if(currentToken.length() == 1) //token has a single numerical value
					currentToken = "0".concat(currentToken); //concat a 0 onto the front 
				
				System.out.println("Token with $ removed: " + currentToken);
				
				/*
				 * Test 3
				 */
				if(currentToken.matches("^([0-9A-Fa-f]{2})+$"))
				{
					System.out.println("Token '" + currentToken + "' contains valid hexadecimal values");
				}
				else
				{
					syntaxCorrect = false;
					errorMessage = "Token '" + currentToken + "' does not contain valid hexadecimal values";
				}
				
				/*
				 * Test 4
				 */
				if(currentToken.equals(""))
				{
					syntaxCorrect = false;
					errorMessage = "Token '" + lineTokens[i] + "' must contain a hexadecimal value";
				}
				
				int tokenDecimal = Utils.HexToDec(currentToken);
				System.out.println(currentToken + " : " + tokenDecimal);
				
				if(tokenDecimal > 255 || tokenDecimal < 0)
				{
					syntaxCorrect = false;
					errorMessage = "Token '" + lineTokens[i] + "' must have a value between 0-255 decimal.";
				}
				
			}
				
		}
		
		
		
		if(syntaxCorrect == false)
		{
			throw new CompilerException("[Compiler] DirectiveHandler error. \n" + errorMessage);
		}
	}
	
	
	public void convertLineToBinary(String dLine) throws CompilerException
	{
		System.out.println("\nConvert Directive To Binary: " + dLine);
		
		//AL to temporarily store the binary strings created by this method
		ArrayList<String> directiveBinaryAL = new ArrayList<String>(); 
		
		String[] lineTokens = dLine.split(" ");
		
		/*
		 * Byte directive
		 */
		if(directiveType == 1)
		{
			System.out.println("Directive type: Byte");
			
			String location = lineTokens[1].replace("$", ""); //Remove $ symbol from location byte
			String data = lineTokens[2].replace("$", ""); //Remove $ symbol from data byte
			
			System.out.println("Location (hex): " + location);
			System.out.println("Data (hex): " + data);
			
			String locationBinary = Utils.hexToBinary(location);
			String dataBinary = Utils.hexToBinary(data);
			
			System.out.println("Location (binary): " + locationBinary);
			System.out.println("Data (binary): " + dataBinary);
			
			/*
			 * If greater than ff, binary wrap around may occur, forcing the int to go from 8-bit to 32-bit
			 * This prevents any binary strings greater than 8-bit from being processed
			 */
			if(locationBinary.length() > 8) 
			{
				throw new CompilerException("Line '" + dLine + "' cannot be processed as memory address will exceed 0xFF (255) \nAssembly halted.");
			}
			
			String outputBinary = "*" + locationBinary + " *" + dataBinary;
			
			directiveBinaryAL.add(outputBinary);
		}
		
		/*
		 * String directive
		 */
		if(directiveType == 2)
		{
			System.out.println("Directive type: String");
			
			//Get starting location
			String startingLocationHex = lineTokens[1].replace("$", ""); //Remove $ symbol from location byte
			int startingLocationDec = Utils.HexToDec(startingLocationHex);
			
			System.out.println("Starting location (Hex): " + startingLocationHex);
			System.out.println("Starting location (Dec): " + startingLocationDec + "\n");
			
			//Get the string to be processed 
			String stringToSplit = lineTokens[2];
			
			//Split the string into individual characters
			char[] splitString = stringToSplit.toCharArray();
			
			
			
			//For each character, convert to ascii value, then convert to binary along with location to be stored at 
			int currentLocation = startingLocationDec;
			for(char character : splitString)
			{
				System.out.println(currentLocation + " : " + character);
				
				String currentLocationHex = Utils.DecToHex(currentLocation); //Convert current location from int to hex 
				String currentLocationBinary = Utils.hexToBinary(currentLocationHex); //Convert current location from hex to binary
				
				int asciiValueDec = (int) character; //Convert character to asciiValue in decimal
				String asciiValueHex = Utils.DecToHex(asciiValueDec); //Convert ascii value decimal to hexadecimal
				String asciiValueBinary = Utils.hexToBinary(asciiValueHex); //Convert hexadecimal ascii value to binary
				
				System.out.println("Hex values: " + currentLocationHex + " " + asciiValueHex);
				System.out.println("Binary values: " + currentLocationBinary + " " + asciiValueBinary);
				
				/*
				 * If greater than ff, binary wrap around may occur, forcing the int to go from 8-bit to 32-bit
				 * This prevents any binary strings greater than 8-bit from being processed
				 */
				if(currentLocationBinary.length() > 8) 
				{
					throw new CompilerException("Line '" + dLine + "' cannot be processed as memory address will exceed 0xFF (255) \nAssembly halted.");
				}
				
				String outputBinary = "*" + currentLocationBinary + " *" + asciiValueBinary;
				
				directiveBinaryAL.add(outputBinary);
				
				currentLocation++;
			}
		}
		
		/*
		 * Values array directive
		 */
		if(directiveType == 3)
		{
			System.out.println("Directive type: Value array");
			
			String startingLocationHex = lineTokens[1].replace("$", ""); //Remove $ symbol from location byte
			int startingLocationDec = Utils.HexToDec(startingLocationHex);
			int numValues = (lineTokens.length - 2);
			
			System.out.println("Starting location (Hex): " + startingLocationHex);
			System.out.println("Starting location (Dec): " + startingLocationDec + "\n");
			
			ArrayList<String> dataValues = new ArrayList<String>(); 
			//Get data values - iterate through lineTokens array, missing the first two tokens 
			for(int i = 2; i < lineTokens.length; i++)
			{
				dataValues.add(lineTokens[i]);
			}
			
			System.out.println("Data Values AL contents:");
			for(String value : dataValues)
			{
				System.out.println(value);
			}
			
			/*
			 * For each data value, convert location and data to binary, output to directiveBinaryAL, then increment location
			 */
			int currentLocation = startingLocationDec;
			for(String data : dataValues)
			{
				String currentLocationHex = Utils.DecToHex(currentLocation); //Convert current location from int to hex 
				String currentLocationBinary = Utils.hexToBinary(currentLocationHex); //Convert current location from hex to binary
				
				data = data.replace("$", "");
				String dataBinary = Utils.hexToBinary(data); //Remove $ then convert the current value from hex to binary
				
				System.out.println("Hex values: " + currentLocationHex + " " + data);
				System.out.println("Binary values: " + currentLocationBinary + " " + dataBinary);
				
				/*
				 * If greater than ff, binary wrap around may occur, forcing the int to go from 8-bit to 32-bit
				 * This prevents any binary strings greater than 8-bit from being processed
				 */
				if(currentLocationBinary.length() > 8) 
				{
					throw new CompilerException("Line '" + dLine + "' cannot be processed as memory address will exceed 0xFF (255) \nAssembly halted.");
				}
				
				String outputBinary = "*" + currentLocationBinary + " *" + dataBinary;
				
				directiveBinaryAL.add(outputBinary);
				
				currentLocation++; //increment current location
			}
			
		
		}
		
		System.out.println("\ndirectiveBinaryAL contents:");
		for(String str : directiveBinaryAL)
		{
			System.out.println(str);
		}
		
		
		
		/*
		 * To add the output onto the directiveBinaryOutput array, the array must be first converted to an AL so 
		 * the size of the final array can be changed. The array is converted to an AL, new values are added, then 
		 * the AL is converted back into an array.
		 */
		
		//Check if directiveBinaryOutput array has been instantiated, if not create array with new values
		//If yes, convert to AL, add new values, then convert back to array
		if(directiveBinaryOutput == null)
		{
			directiveBinaryOutput = directiveBinaryAL.toArray(new String[directiveBinaryAL.size()]);
		}
		else
		{
			ArrayList<String> directiveBinaryOutputAL = new ArrayList<String>(Arrays.asList(directiveBinaryOutput)); 
			
			//Add all values into the arraylist
			for(String binaryStr : directiveBinaryAL)
			{
				directiveBinaryOutputAL.add(binaryStr);
			}
					
			//Convert AL back into array with new values
			directiveBinaryOutput = directiveBinaryOutputAL.toArray(new String[directiveBinaryAL.size()]);
		}
		
		System.out.println("\ndirectiveBinaryAL converted to array.\n");
	}
	
	public void printDirectiveBinaryOutputContents()
	{
		System.out.println("Print DirectiveBinaryOutput contents:");
		if(directiveBinaryOutput != null)
		{
			for(String str : directiveBinaryOutput)
			{
				System.out.println(str);
			}
		}
	}
	
	public String[] getDirectiveBinaryOutput()
	{
		return directiveBinaryOutput;
	}
}




















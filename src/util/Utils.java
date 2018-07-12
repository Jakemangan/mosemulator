package util;

public class Utils {
	
	static char hexaChar[] = {'F', 'E', 'D', 'C', 'B', 'A'};
	
    static final String[] HEX_CONSTANTS = {"00", "01", "02", "03", "04", "05", "06", "07",
								            "08", "09", "0A", "0B", "0C", "0D", "0E", "0F",
								            "10", "11", "12", "13", "14", "15", "16", "17",
								            "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
								            "20", "21", "22", "23", "24", "25", "26", "27",
								            "28", "29", "2A", "2B", "2C", "2D", "2E", "2F",
								            "30", "31", "32", "33", "34", "35", "36", "37",
								            "38", "39", "3A", "3B", "3C", "3D", "3E", "3F",
								            "40", "41", "42", "43", "44", "45", "46", "47",
								            "48", "49", "4A", "4B", "4C", "4D", "4E", "4F",
								            "50", "51", "52", "53", "54", "55", "56", "57",
								            "58", "59", "5A", "5B", "5C", "5D", "5E", "5F",
								            "60", "61", "62", "63", "64", "65", "66", "67",
								            "68", "69", "6A", "6B", "6C", "6D", "6E", "6F",
								            "70", "71", "72", "73", "74", "75", "76", "77",
								            "78", "79", "7A", "7B", "7C", "7D", "7E", "7F",
								            "80", "81", "82", "83", "84", "85", "86", "87",
								            "88", "89", "8A", "8B", "8C", "8D", "8E", "8F",
								            "90", "91", "92", "93", "94", "95", "96", "97",
								            "98", "99", "9A", "9B", "9C", "9D", "9E", "9F",
								            "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7",
								            "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF",
								            "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7",
								            "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF",
								            "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7",
								            "C8", "C9", "CA", "CB", "CC", "CD", "CE", "CF",
								            "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7",
								            "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF",
								            "E0", "E1", "E2", "E3", "E4", "E5", "E6", "E7",
								            "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF",
								            "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7",
								            "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF"};
	
    public static String byteToHex(int val) {
        return HEX_CONSTANTS[val & 0xff];
    }
    
    public static void printStringArray(String[] array)
    {
    	for(String str : array)
    	{
    		System.out.println(str);
    	}
    }
    
    public static String DecToHex(int input)
	{
		String output = "";
		int remainder;
	
		if(input >= 256)
		{
			return "-1";
		}
		
		while(input > 0)
		{
			remainder = input % 16;
			
			if(remainder > 9)
			{
				output = hexaChar[15 - remainder] + output;
			}
			else
			{
				output = remainder + output;
			}
			
			input = input / 16;
				
		}
		return output;
	}
    
    public static int HexToDec(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
        return val;
    }
   
    public static String convertAddrModeToString(int mode)
    {
    	String returnString = "";
    	
    	if(mode == 1)
    	{
    		returnString = "ADDR_IMP";
    	}
    	
    	if(mode == 2)
    	{
    		returnString = "ADDR_ACC";
    	}
    	
    	if(mode == 3)
    	{
    		returnString = "ADDR_IMMEDIATE";
    	}
    	
    	if(mode == 4)
    	{
    		returnString = "ADDR_RELATIVE";
    	}
    	
    	if(mode == 5)
    	{
    		returnString = "ADDR_ABSOLUTE";
    	}
    	
    	if(mode == 6)
    	{
    		returnString = "ADDR_ABSOLUTE_X";
    	}
    	
    	if(mode == 7)
    	{
    		returnString = "ADDR_ABSOLUTE_Y";
    	}
    	
    	if(mode == 8)
    	{
    		returnString = "ADDR_INDIRECT";
    	}
    	
    	if(mode == 9)
    	{
    		returnString = "ADDR_INDEXED_INDIRECT";
    	}
    	
    	if(mode == 10)
    	{
    		returnString = "ADDR_INDIRECT_INDEXED";
    	}
    	
    	return returnString;
    }
    
    public static int convertAddrModeToNumber(String mode)
	{
		int returnNumber = 0;
		
		if(mode.equals("implied"))
		{
			returnNumber = 1;
		}
		
		if(mode.equals("accumulator"))
		{
			returnNumber = 2;
		}
		
		if(mode.equals("immediate"))
		{
			returnNumber = 3;
		}
		
		if(mode.equals("relative"))
		{
			returnNumber = 4;
		}
		
		if(mode.equals("absolute"))
		{
			returnNumber = 5;
		}
		
		if(mode.equals("absolute,X"))
		{
			returnNumber = 6;
		}
		
		if(mode.equals("absolute,Y"))
		{
			returnNumber = 7;
		}
		
		if(mode.equals("indirect"))
		{
			returnNumber = 8;
		}
		
		if(mode.equals("(indirect,X)"))
		{
			returnNumber = 9;
		}
		
		if(mode.equals("(indirect),Y"))
		{
			returnNumber = 10;
		}
		
		return returnNumber;
	}
    
    public static String hexToBinary(String hex)
    {
    	String bin = Integer.toBinaryString(Integer.parseInt(hex,16));
    	int length = bin.length();
    	
    	if(length < 8)
    		if(length == 1)
    			return "0000000" + bin;
    		else if(length == 2)
    			return "000000" + bin;
    		else if(length == 3)
    			return "00000" + bin;
    		else if(length == 4)
    			return "0000" + bin;
    		else if(length == 5)
    			return "000" + bin;
    		else if(length == 6)
    			return "00" + bin;
    		else if(length == 7)
    			return "0" + bin;
    	
    	return bin;	
    }
}



















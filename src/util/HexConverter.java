package util;

public class HexConverter {

	static char hexaChar[] = {'F', 'E', 'D', 'C', 'B', 'A'};
	
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
	
}

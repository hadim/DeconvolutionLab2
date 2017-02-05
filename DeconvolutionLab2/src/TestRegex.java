import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String c = "-image -image -- image image -image \t language -image";
		String regex = "\bimage\b";
		Pattern p = Pattern.compile(regex);

		// Check each entry of list to find the correct value
		Matcher matcher = p.matcher(c);
		
		while (matcher.find())
		{
		 System.out.print("Start index: " + matcher.start());
		 System.out.print(" End index: " + matcher.end() + " ");
		 System.out.println(matcher.group());
		}
	}

}

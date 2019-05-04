package andUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

final class InputScanner {
	public static String getInput() {
		String inputLine = null;
		try {
			BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
			inputLine = is.readLine();
			if (inputLine.length() == 0) {
				return null; 
			}
			is.close();
		} catch (IOException e){
			System.out.println("IOException: " + e);
		}

		return inputLine;

	}
}

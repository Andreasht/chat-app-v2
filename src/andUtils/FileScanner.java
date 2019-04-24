package andUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public final class FileScanner {

	public static String readFromFile(String f) {
		String read = "";
		try {
			read = new String(Files.readAllBytes(Paths.get(f)));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
        return read;
    }
	
	public static void writeToFile(String s, String f) {

        Path path = Paths.get(s);
        byte[] data = f.getBytes();
        try {
            Files.write(path, data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

	}

	public static ArrayList<String> readEachLine(String f) {
		ArrayList<String> list = new ArrayList<>();
		File file = new File(f);
		if(file.exists()) {
			try {
				list = (ArrayList<String>) Files.readAllLines(file.toPath(),Charset.defaultCharset());
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	public static ArrayList<Integer> readEachInt(String f) {
		return Utils.toIntArray(readEachLine(f));
	}

}

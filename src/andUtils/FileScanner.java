package andUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public final class FileScanner {

	public static String readFromFile(String filePath) {
		String read = "";
		try {
			read = new String(Files.readAllBytes(Paths.get(filePath)));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
        return read;
    }
	
	public static void writeToFile(String filePath, String contentToWrite) throws IOException {

        Path path = Paths.get(filePath);
        byte[] data = contentToWrite.getBytes();
		Files.write(path, data);

	}

	private static ArrayList<String> readEachLine(String filePath) {
		ArrayList<String> list = new ArrayList<>();
		File file = new File(filePath);
		if(file.exists()) {
			try {
				list = (ArrayList<String>) Files.readAllLines(file.toPath(),Charset.defaultCharset());
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	public static ArrayList<Integer> readEachInt(String filePath) {
		return Utils.toIntArray(readEachLine(filePath));
	}

}

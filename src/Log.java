import java.io.File;
import java.io.IOException;

import static andUtils.FileScanner.*;
@SuppressWarnings("Duplicates")
final class Log implements java.io.Serializable {
    private static final File LOGS_DIR;

    static {
        LOGS_DIR = new File("ChatLogs");
        if(!LOGS_DIR.exists()) LOGS_DIR.mkdir();
    }

    static void writeToLog(User user1, User user2, String in) {
        String fileName = "ChatLogs/"+user1.getUsername()+"+"+user2.getUsername()+".txt";
        String altFileName = "ChatLogs/"+user2.getUsername()+"+"+user1.getUsername()+".txt";
        String finalName;

        File file = new File(fileName);
        File altFile = new File(altFileName);

        if(file.exists()) {
            finalName = fileName;
        } else if(altFile.exists()) {
            finalName = altFileName;
        } else {
            finalName = fileName;
        }
        try {
            String previousContent = readFromFile(finalName);
            writeToFile(finalName, previousContent+in+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static String getLog(String user1, String user2) {
        String fileName = "ChatLogs/"+user1+"+"+user2+".txt";
        String altFileName = "ChatLogs/"+user2+"+"+user1+".txt";
        String finalName;

        File file = new File(fileName);
        File altFile = new File(altFileName);

        if(file.exists()) {
            finalName = fileName;
        } else if(altFile.exists()) {
            finalName = altFileName;
        } else {
            finalName = fileName;
        }

        return readFromFile(finalName);
    }

}

import java.io.File;
import static andUtils.FileScanner.*;

public class Log implements java.io.Serializable {
    User user1;
    User user2;
    private static final File LOGS_DIR = new File("ChatLogs");
    private File logsFolder;
    private String finalPath;

    Log(User from, User to) {
        if(!LOGS_DIR.exists()) LOGS_DIR.mkdir();
        user1 = from;
        user2 = to;
        String fileName = from.getUsername()+"+"+to.getUsername();
        String logPath = LOGS_DIR + "/" + fileName;
        logsFolder = new File(logPath);
        finalPath = logPath+"/"+fileName+".txt";
        createLog();
    }

    void createLog() {
        if(!logsFolder.exists()) {
            System.out.println("No log found. Directory will be created...");
            logsFolder.mkdir();
        }

    }

    static void writeToLog(User user1, User user2, String in) {
        String fileName = user1.getUsername()+"+"+user2.getUsername();
        String logPath = LOGS_DIR + "/" + fileName;

        writeToFile(String.format("%s/%s.txt", logPath, fileName), in);

    }

    static String getLog(User user1, User user2) {
        String fileName = user1.getUsername()+"+"+user2.getUsername();
        String logPath = LOGS_DIR + "/" + fileName;

        return readFromFile(String.format("%s/%s.txt", logPath, fileName));
    }

    static boolean logExists(User user1, User user2) {
        String fileName = user1.getUsername()+"+"+user2.getUsername();
        String logPath = LOGS_DIR + "/" + fileName;
        File logFile = new File(String.format("%s/%s.txt",logPath,fileName));
        return logFile.exists();
    }
}

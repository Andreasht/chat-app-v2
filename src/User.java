import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class User implements java.io.Serializable {
    private static int users;
    private char[] passwd;
    private String username;
    private static final String DEFAULT_UN = "admin";
    private static final char[] DEFUALT_PASS = {'a','d','m','i','n'};
    private static final String TEST_UN = "test";
    private static final char[] TEST_PASS = {'t','e','s','t'};
    private ArrayList<User> contacts;
    private Status status;
    private Socket socket;
    private ObjectOutputStream streamOut;
    private ObjectInputStream streamIn;

    User(boolean isTestUser) {
        if(isTestUser) {
            username = TEST_UN;
            passwd = TEST_PASS;
            contacts = new ArrayList<>();
            status = Status.OFF;
        } else {
            username = DEFAULT_UN;
            passwd = DEFUALT_PASS;
            contacts = new ArrayList<>();
            status = Status.OFF;
        }
    }



    User(String username , char[] pass, ObjectOutputStream out, ObjectInputStream in) {
        this.username = username;
        passwd = pass;
        status = Status.OFF;
        streamOut = out;
        streamIn = in;
        System.out.println("linked streams to user!");
        users++;
    }

    String getUsername() {
        return username;
    }

    boolean authenticate(char[] kodeIn) {
        return Arrays.equals(this.passwd, kodeIn);
    }

    ArrayList<User> getContacts() {
        return contacts;
    }

    void addContact(User user) {
        contacts.add(user);
    }

    String getStatus() {
        // status = socket.isConnected() ? Status.ON : Status.OFF;
      //  status = Status.OFF;
        return status.getText();
    }




    public void setStatus(Status status) {
        this.status = status;
    }

    public ObjectOutputStream getStreamOut() {
        return streamOut;
    }

    public ObjectInputStream getStreamIn() {
        return streamIn;
    }

    public Socket getSocket() {
        return socket;
    }

}
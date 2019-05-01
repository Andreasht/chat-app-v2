import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
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
    private ArrayList<User> contacts;
    private Status status;
    private Socket socket;
    private PrintStream streamOut;
    private InputStream streamIn;

    User() {
        username = DEFAULT_UN;
        passwd = DEFUALT_PASS;
        contacts = new ArrayList<>();
        status = Status.OFF;
    }

    User(String username , char[] pass, Socket socket) {
        this.username = username;
        passwd = pass;
        status = Status.OFF;
        System.out.println("Is socket closed? from user (1)"+socket.isClosed());
        this.socket = socket;
        System.out.println("Is socket closed? from user (2)"+this.socket.isClosed());
        try {
            this.streamIn = socket.getInputStream();
            this.streamOut = new PrintStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.err.println("Failed to create new user!\n");
            ex.printStackTrace();
        }

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

//    public Socket getSocket() {
//        return socket;
//    }

//    public void connect(String address, int port) {
//        try {
//            socket = new Socket(address, port);
//            System.out.printf("Connected user %s to server with address %s:%d%n", username, address, port);
//        } catch(IOException ex) {
//            System.err.println("Error in connecting UserSocket!");
//
//        }
//    }


    public void setStatus(Status status) {
        this.status = status;
    }

    public PrintStream getStreamOut() {
        return streamOut;
    }

    public InputStream getStreamIn() {
        return streamIn;
    }

    public Socket getSocket() {
        return socket;
    }
}
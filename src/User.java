import java.net.Socket;
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

    User() {
        username = DEFAULT_UN;
        passwd = DEFUALT_PASS;
        contacts = new ArrayList<>();
        status = Status.OFF;
    }

    User(String bN /*, char[] k */) {
        username = bN;
     //   passwd = k;
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
        status = socket.isConnected() ? Status.ON : Status.OFF;
      //  status = Status.OFF;
        return status.getText();
    }

    public Socket getSocket() {
        return socket;
    }
}
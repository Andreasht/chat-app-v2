import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

class User implements java.io.Serializable {
    private static int users;

    private String username;

    private ArrayList<User> contacts;
    private Status status;
    private Socket socket;
    private ObjectOutputStream streamOut;
    private ObjectInputStream streamIn;

    User(String username, ObjectOutputStream out, ObjectInputStream in) {
        this.username = username;
        status = Status.OFF;
        streamOut = out;
        streamIn = in;
        System.out.println("linked streams to user!");
        users++;
    }

    String getUsername() {
        return username;
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
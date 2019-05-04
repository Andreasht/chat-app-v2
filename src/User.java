import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import static andUtils.FileScanner.*;


class User {
    private static int users;
    private final String username;
    private ArrayList<String> contacts;
    private Status status;
    private Socket socket;
    private final ObjectOutputStream streamOut;
    private final ObjectInputStream streamIn;
    private final File contactsFile;

    User(String usernameIn, ObjectOutputStream out, ObjectInputStream in) {
        username = usernameIn;
        status = Status.OFF;
        streamOut = out;
        streamIn = in;
        contactsFile = new File(String.format("UserInfo/%s/contacts.txt",usernameIn));
        initContacts();
        users++;
        System.out.println("Created new user object!");
    }

    String getUsername() {
        return username;
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

    private void initContacts() {
        if(!contactsFile.exists()) {
            try {
                contactsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        refreshContacts();
    }

    private void refreshContacts() {
        contacts = readEachLine(contactsFile.getPath());
    }

    void addContact(String name) {
        if(Server.hasRegisteredUser(name)) {
            try {
                if(!hasContact(name)) {
                    if(!username.equals(name)) {
                        String previousContent = readFromFile(contactsFile.getPath());
                        writeToFile(contactsFile.getPath(),previousContent+name+"\n");
                        refreshContacts();
                    } else {
                        throw new IllegalArgumentException("Can't add yourself!");
                    }
                } else {
                    throw new IllegalArgumentException("User already has this contact!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Couldn't add contact. No user with this name is registered!");
        }
    }

    public ArrayList<String> getContacts() {
        return contacts;
    }

    private boolean hasContact(String name) {
        for(String contact : contacts) {
            if(contact.equals(name)) return true;
        }
        return false;
    }
}
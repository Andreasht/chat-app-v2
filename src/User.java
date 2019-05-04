import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import static andUtils.FileScanner.*;


class User {
    private final String username;
    private ArrayList<String> contacts;
    private final ObjectOutputStream streamOut;
    private final ObjectInputStream streamIn;
    private final File contactsFile;

    User(String usernameIn, ObjectOutputStream out, ObjectInputStream in) {
        username = usernameIn;
        streamOut = out;
        streamIn = in;
        contactsFile = new File(String.format("UserInfo/%s/contacts.txt",usernameIn));
        initContacts();
        System.out.println("Created new user object!");
    }

    String getUsername() {
        return username;
    }

    ObjectOutputStream getStreamOut() {
        return streamOut;
    }

    ObjectInputStream getStreamIn() {
        return streamIn;
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

    ArrayList<String> getContacts() {
        return contacts;
    }

    private boolean hasContact(String name) {
        for(String contact : contacts) {
            if(contact.equals(name)) return true;
        }
        return false;
    }
}
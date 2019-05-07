import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import static andUtils.FileScanner.*;
import static andUtils.SecurityUtils.*;
@SuppressWarnings({"Duplicates", "ResultOfMethodCallIgnored"})

class Server {
    private final int port;
    private ArrayList<User> activeClients;

    public static void main(String[] args) {
        new Server();
    }

    private Server() {
        this.port = 50000;
        activeClients = new ArrayList<>();
        File userDirectory = new File("UserInfo");                      // laver mappen hvis den ikke findes..
        if(!userDirectory.exists()) {
            userDirectory.mkdir();
            System.out.println("Created main userinfo directory...");
        }
        run();
    }

    private void run() {
        try {
            // start serveren med den givne port:
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server launched. Listening on port "+port);
            //noinspection InfiniteLoopStatement
            while(true) {
                // modtag forbindelse fra klient:
                Socket clientSocket = serverSocket.accept();
                System.out.printf("Received connection from: %s%n", clientSocket.getRemoteSocketAddress());
                // åben streams der bruges til at læse input og skrive output:
                ObjectOutputStream objOut = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream objIn = new ObjectInputStream(clientSocket.getInputStream());

                System.out.println("Opened streams.");
                try {
                    // læs hvilken type signal denne forbindelse bringer:
                    Signal signalIn = (Signal) objIn.readObject();
                    System.out.printf("Received signal of type %s%n.",signalIn.getType());

                    if(signalIn.equals(Signal.LOGIN)) {
                        // hent navn og kode fra inputstream
                        String receivedName = (String) objIn.readObject();
                        char[] receivedPass = (char[]) objIn.readObject();
                        try {
                            //check login med auth() metoden:
                            Boolean authenticated = auth(receivedName, receivedPass);
                            System.out.printf("Authenticated: %s%n", authenticated);
                            // send resultat til klient:
                            objOut.writeObject(authenticated);
                            if(authenticated) {
                                // instantier ny user:
                                User connectingUser = new User(receivedName,objOut,objIn);
                                activeClients.add(connectingUser);
                                System.out.println("Added client to active clients!");

                                // send brugerens kontakter:
                                objOut.writeObject(connectingUser.getContacts());

                                // start en ny ClientHandler tråd til brugeren:
                                new Thread(new ClientHandler(this, connectingUser)).start();

                                System.out.println("Started new ClientHandler linked to connecting client!");

                            } else {
                                System.out.println("User entered wrong password!\nDenying login.");
                                clientSocket.close();
                            }

                        } catch(IllegalArgumentException ex) {
                            System.err.println("No user found!");
                            objOut.writeObject(ex);
                        }

                    } else if(signalIn.equals(Signal.REG)) {
                        // registrerings signalet
                        RegisterPackage registerPackageIn = (RegisterPackage) objIn.readObject();
                        System.out.println("Got register package!");
                        try {
                            Boolean success = registerUser(registerPackageIn);
                            if(success) {
                                System.out.println("Registered user!");
                            }
                            objOut.writeObject(success);
                        } catch (IllegalArgumentException ex) {                     // hvis en exception fanges, send den istedet
                            System.err.println("IllegalArgumentException thrown");
                            objOut.writeObject(ex);
                        }
                        clientSocket.close();

                    } else if(signalIn.equals(Signal.CONT)) {
                        // add contact signalet
                        String receivedActiveName = (String) objIn.readObject();
                        String receivedContactName = (String) objIn.readObject();
                        User activeUser = getActiveUser(receivedActiveName);
                        try {
                            activeUser.addContact(receivedContactName);
                            System.out.println("Added contact!");
                            System.out.println(activeUser.getContacts());
                            objOut.writeObject(activeUser.getContacts());
                        } catch (IllegalArgumentException ex) {
                            objOut.writeObject(ex);
                        }
                        clientSocket.close();

                    } else if(signalIn.equals(Signal.CHECK)) {
                        // checker om en bruger er online
                        String receivedName = (String) objIn.readObject();
                        System.out.println("Received name: "+receivedName);
                        objOut.writeObject(hasActiveUser(receivedName));

                    } else if(signalIn.equals(Signal.GET)) {
                        // henter chatlogs
                        String[] in = ((String) objIn.readObject()).split("\\+");
                        String log = Log.getLog(in[0],in[1]);
                        objOut.writeObject(log);
                        System.out.println("sent log");
                    }
                }    catch(ClassNotFoundException ex) {
                    System.err.println("Error in reading object input stream!");
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            System.err.println("Error in 'run' method!");
            ex.printStackTrace();
        }

    }

    private void removeUser(User user) {
        activeClients.remove(user);
    }

    static Boolean hasRegisteredUser(String name) {
        return new File(String.format("UserInfo/%s", name)).exists();
    }

    private Boolean auth(String name, char[] pass) {
        // check om brugeren er registreret:
        if(hasRegisteredUser(name)) {
            // hent info fra brugerens UserInfo fil
            String filePath = String.format("UserInfo/%s/%s.txt",name,name);
            ArrayList<String> userInfo = readEachLine(filePath);
            String salt = userInfo.get(1);
            String hash = userInfo.get(2);
            // kald SecurityUtils authenticate() metode
            return authenticate(pass, salt, hash);
        }

        // hvis ikke, throw IllegalArgumentException:
        throw new IllegalArgumentException("No user with this name found!");
    }

    private User getActiveUser(String name) {
        // return active client with given name:
        for (User client : activeClients) {
            if(client.getUsername().equals(name)) return client;
        }
        throw new IllegalArgumentException("Something went wrong in getting the user. Is the name correct? Checked name:"+name);
    }

    private Boolean hasActiveUser(String name) {
        for(User client : activeClients) {
            if(client.getUsername().equals(name)) return true;
        }
        return false;
    }

    private Boolean registerUser(RegisterPackage registerPackage) {
        StringBuilder toWrite = new StringBuilder();
        for(String line : registerPackage.getData()) {
            toWrite.append(line).append("\n");
        }
        String name = registerPackage.getData().get(0);
        File personalDirectory = new File(String.format("UserInfo/%s", name));
        if(personalDirectory.exists()) {
            throw new IllegalArgumentException("User with this name already exists!");
        }
        personalDirectory.mkdir();
        System.out.println("Created personal directory...");

        String finalPath = String.format("UserInfo/%s/%s.txt",name,name);

        try {
            writeToFile(finalPath, toWrite.toString());
            System.out.println("Wrote content to file!");
            return true;
        } catch (IOException e) {
            System.err.println("Error in registering user!");
            e.printStackTrace();
            return false;
        }
    }

    private void sendMessage(String msg, User sender, User receiver) throws IOException {
        receiver.getStreamOut().writeObject(msg);
        sender.getStreamOut().writeObject(msg);
        Log.writeToLog(sender,receiver,msg);
    }

    class ClientHandler implements Runnable {
        private final Server server;
        private final User client;

        ClientHandler(Server server, User user) {
            this.server = server;
            this.client = user;
        }

        @Override
        public void run() {
            System.out.println("start run!");
            String message;
            ObjectInputStream in = client.getStreamIn(); // bind en variabel til brugerens input
            try {
                while((message = (String) in.readObject()) != null) {   //læs beskeder uendeligt
                    String[] msgSplit = message.split("\\|",2);     // split besked
                    User recipient = getActiveUser(msgSplit[0]);       // bind en variabel til senderen af beskeden
                    String msg = msgSplit[1];                       // hent den konkrete besked
                    sendMessage(client.getUsername()+": "+msg,client,recipient);    // send besked
                }
            } catch (IOException | ClassNotFoundException e) {
                if(e instanceof SocketException) {
                    System.out.println("User disconnected! Ending thread.\n");
                } else {
                    System.err.println("Catastrophic error in ClientHandler!");
                    e.printStackTrace();
                }

            }

            server.removeUser(client);
            System.out.println("end of handler!");

        }
    }


}

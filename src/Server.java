import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    private int port;
    private static ArrayList<User> registeredClients;
    private ArrayList<User> activeClients;
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        new Server(50000);
    }

    private Server(int p) {
        port = p;
        registeredClients = new ArrayList<>();
        System.out.println("Created list!");
        activeClients = new ArrayList<>();
        registeredClients.add(new User());
        System.out.println("-------------------------");
        for(User user : registeredClients) {
            System.out.println(user.getUsername());
        }
        System.out.println("-------------------------");
        run();
    }

    private void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server launched. Listening on port "+port);
            while(true) {
                //receive connection from client
                Socket clientSocket = serverSocket.accept();
                System.out.printf("Received connection from: %s%n", clientSocket.getRemoteSocketAddress());
                //open input stream
                ObjectInputStream objIn = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream objOut = new ObjectOutputStream(clientSocket.getOutputStream());
                System.out.println("Opened streams.");
                try {
                    // receive name
                    String receivedName = (String) objIn.readObject();
                    //receive pass
                    char[] receivedPass = (char[]) objIn.readObject();
                    //check if login is ok
                    System.out.printf("Received name and pass.\n%s\n%s%n", receivedName, receivedPass.toString());
                    try {
                        Boolean authenticated = auth(receivedName, receivedPass);
                        System.out.printf("authenticated: %s%n", authenticated);
                        //send result
                        objOut.writeObject(authenticated);
                        System.out.println("wrote result");
                        System.out.println("is socket closed? (1) "+clientSocket.isClosed());
                        if(authenticated) {
                            Thread.sleep(500);
//                            activeClients.add(getUser(receivedName));
                            User connectingUser = new User(receivedName,receivedPass,clientSocket); // fix!
                            activeClients.add(connectingUser);
                            System.out.println("added client to active clients");
//                            objIn.close();
//                            objOut.close();
//                            System.out.println("closed streams (1)");
                            System.out.println("Is socket closed? (2) "+clientSocket.isClosed());
                            new Thread(new ClientHandler(this, connectingUser)).start();
                            System.out.println("Started new ClientHandler thread for connecting client!");
                        }
                    } catch(NoUserException ex) {
                        System.err.println("No user exception!\n");
                        objOut.writeObject(ex);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                } catch(ClassNotFoundException ex) {
                    System.err.println("Error in reading object input stream!");
                    ex.printStackTrace();
                }
//                objIn.close();
//                objOut.close();
                System.out.println("closed streams (2)");
                System.out.println("Is socket closed? (4) "+clientSocket.isClosed());
            }

        } catch (IOException ex) {
            System.err.println("Error in 'run' method!");
            ex.printStackTrace();
        }

        System.out.println("pong!");
    }

    void removeUser(User user) {
        activeClients.remove(user);
    }

    private User getUser(String name) {
        for (User user : registeredClients) {
            if(user.getUsername().equals(name)) return user;
        }
        throw new NoUserException();
    }

    private Boolean auth(String name, char[] pass) {
        for (User user : registeredClients) {
            if (user.getUsername().equals(name)) {
                return user.authenticate(pass);
            }
        }
        throw new NoUserException();
    }


    //    void sendMessage(String msg, User sender, String receiver) {
//        boolean find = false;
//        for(User client : activeClients) {
//            if (client.getU)
//        }
//    }
    class ClientHandler implements Runnable {
        private Server server;
        private User client;

        public ClientHandler(Server server, User user) {
            this.server = server;
            this.client = user;
            System.out.println("is socket closed? start of client handler "+client.getSocket().isClosed());
        }

        @Override
        public void run() {
            String message = "";
            System.out.println("Is socket closed? (3) "+client.getSocket().isClosed());
            Scanner scanner = new Scanner(client.getStreamIn());
            try {
                System.out.println(client.getStreamIn().available());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while(scanner.hasNextLine()) {
                message = scanner.nextLine();
                System.out.printf("Read %s%n", message);
            }

            System.out.println("end of clienthandler thread");
            server.removeUser(client);
            scanner.close();

        }
    }

}

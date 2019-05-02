import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
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
        registeredClients.add(new User(false));
        registeredClients.add(new User(true));
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
                ObjectOutputStream objOut = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream objIn = new ObjectInputStream(clientSocket.getInputStream());

                System.out.println("Opened streams.");
                try {
                    // receive name
                    String receivedName = (String) objIn.readObject();
                    //receive pass
                    char[] receivedPass = (char[]) objIn.readObject();
                    //check if login is ok
                    System.out.printf("Received name and pass.\n%s\n%s%n", receivedName, Arrays.toString(receivedPass));
                    try {
                        Boolean authenticated = auth(receivedName, receivedPass);
                        System.out.printf("authenticated: %s%n", authenticated);
                        //send result
                        objOut.writeObject(authenticated);
                        System.out.println("wrote result");
                        if(authenticated) {
                            String in = (String) objIn.readObject();
                            boolean ready = in.equals("READY");
                            if(ready) {
                                System.out.println("Got ready!");

//                            activeClients.add(getUser(receivedName));
                                User connectingUser = new User(receivedName,receivedPass,objOut,objIn); // fix!
                                activeClients.add(connectingUser);
                                System.out.println("added client to active clients");


                                new Thread(new ClientHandler(this, connectingUser)).start();

                                System.out.println("Started new ClientHandler thread for connecting client!");
                            }
                        } else {
                            System.out.println("User entered wrong password!\nDenying login.");
                        }

                    } catch(NoUserException ex) {
                        System.err.println("No user exception!\n");
                        objOut.writeObject(ex);
                    }
                } catch(ClassNotFoundException ex) {
                    System.err.println("Error in reading object input stream!");
                    ex.printStackTrace();
                }
            }

        } catch (IOException ex) {
            System.err.println("Error in 'run' method!");
            ex.printStackTrace();
        }

        System.out.println("pong!");
    }

    private void removeUser(User user) {
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

    void writeToAll(Object input) {
        for(User client : activeClients) {
            try {
                client.getStreamOut().writeObject(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

        ClientHandler(Server server, User user) {
            System.out.println("con");
            this.server = server;
            this.client = user;

        }

        @Override
        public void run() {
            System.out.println("start run!");
            String message;
            ObjectInputStream in = client.getStreamIn();
            ObjectOutputStream out = client.getStreamOut();
            try {
                while((message = (String) in.readObject()) != null) {
                    System.out.printf("read %s%n",message);
//                    out.writeObject(message);
                    writeToAll(message);
                    System.out.println("wrote!");

                }
            } catch (IOException | ClassNotFoundException e) {
                if(e instanceof SocketException) {
                    System.err.println("User disconnected! Ending thread.");
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

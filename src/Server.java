import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

import static andUtils.FileScanner.*;
import static andUtils.SecurityUtils.*;
@SuppressWarnings({"Duplicates", "ResultOfMethodCallIgnored"})
class Server {
    private final int port;
    private final ArrayList<User> activeClients;

    public static void main(String[] args) {
        new Server();
    }

    private Server() {
        port = 50000;
        activeClients = new ArrayList<>();
        File userDirectory = new File("UserInfo");
        if(!userDirectory.exists()) {
            userDirectory.mkdir();
            System.out.println("Created main userinfo directory...");
        }
        run();
    }

    private void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server launched. Listening on port "+port);
            //noinspection InfiniteLoopStatement
            while(true) {
                //receive connection from client
                Socket clientSocket = serverSocket.accept();
                System.out.printf("Received connection from: %s%n", clientSocket.getRemoteSocketAddress());
                //open input stream
                ObjectOutputStream objOut = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream objIn = new ObjectInputStream(clientSocket.getInputStream());

                System.out.println("Opened streams.");
                try {
                    // receive type signal
                    Signal signalIn = (Signal) objIn.readObject();
                    if(signalIn.equals(Signal.LOGIN)) {
                        System.out.println("Received signal of type LOGIN. Treating event as login request.");
                        // receive name
                        String receivedName = (String) objIn.readObject();
                        // receive pass
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
                                User connectingUser = new User(receivedName,objOut,objIn); // fix!
                                activeClients.add(connectingUser);
                                System.out.println("added client to active clients");

                                new Thread(new ClientHandler(this, connectingUser)).start();

                                System.out.println("Started new ClientHandler linked to connecting client!");

                            } else {
                                System.out.println("User entered wrong password!\nDenying login.");
                                clientSocket.close();
                            }

                        } catch(NoUserException ex) {
                            System.err.println("No user exception!\n");
                            objOut.writeObject(ex);
                        }
                    } else if(signalIn.equals(Signal.REG)) {
                        System.out.println("Received signal of type REGISTER. Treating event as register request.");
                        RegisterPackage registerPackageIn = (RegisterPackage) objIn.readObject();
                        System.out.println("Got register package!");
                        try {
                            Boolean success = registerUser(registerPackageIn);
                            if(success) {
                                System.out.println("Registered user!");
                            }
                            objOut.writeObject(success);
                        } catch (ExistingUserException ex) {
                            System.err.println("ExistingUserException thrown");
                            objOut.writeObject(ex);
                        }

                        clientSocket.close();
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

        System.out.println("pong!");
    }

    private void removeUser(User user) {
        activeClients.remove(user);
    }

//    private User getUser(String name) {
//        for (User user : registeredClients) {
//            if(user.getUsername().equals(name)) return user;
//        }
//        throw new NoUserException();
//    }

    private Boolean auth(String name, char[] pass) {
        //check if the user is registered:
        File infoDir = new File(String.format("UserInfo/%s", name));
        if(infoDir.exists()) {
            String filePath = String.format("UserInfo/%s/%s.txt",name,name);
            String[] userInfo = readFromFile(filePath).split(" ");
            String salt = userInfo[1];
            String hash = userInfo[2];
            return authenticate(pass, salt, hash);
        }

        // if not, throw exception:
        throw new NoUserException();
    }


    private void writeToAll(Object input) {
        for(User client : activeClients) {
            try {
                client.getStreamOut().writeObject(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Boolean registerUser(RegisterPackage registerPackage) {

        StringBuilder toWrite = new StringBuilder();
        for(String line : registerPackage.getData()) {
            toWrite.append(line).append(" ");
        }

        String name = registerPackage.getData().get(0);
        File personalDirectory = new File(String.format("UserInfo/%s", name));
        if(personalDirectory.exists()) {
            throw new ExistingUserException();
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



    //    void sendMessage(String msg, User sender, String receiver) {
//        boolean find = false;
//        for(User client : activeClients) {
//            if (client.getU)
//        }
//    }
    class ClientHandler implements Runnable {
        private final Server server;
        private final User client;

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
                    writeToAll(client.getUsername()+": "+message);
                    System.out.println("wrote!");

                }
            } catch (IOException | ClassNotFoundException e) {
                if(e instanceof SocketException) {
                    System.out.println("User disconnected! Ending thread.\n");
                    writeToAll("User "+client.getUsername()+" disconnected.");
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private int port;
    private ArrayList<User> clients;
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        new Server(50000);
    }

    private Server(int p) {
        port = p;
        clients = new ArrayList<>();
        run();
    }

    private void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server launched. Listening on port "+port);
            while(true) {
                Socket clientSocket = serverSocket.accept();
                ObjectInputStream objIn = new ObjectInputStream(clientSocket.getInputStream());
                try {
                    Object receivedObject = objIn.readObject();
                    objIn.close();
                    if (receivedObject instanceof User) {
                        User connectingUser = (User) receivedObject;
                        clients.add(connectingUser);
                        // new Thread(new UserHandler) start
                        System.out.println("Received user!");
                    } else {
                        System.err.println("Received object not instance of User!");
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
    }

    void removeUser(User user) {
        clients.remove(user);
    }

//    void sendMessage(String msg, User sender, String receiver) {
//        boolean find = false;
//        for(User client : clients) {
//            if (client.getU)
//        }
//    }
}

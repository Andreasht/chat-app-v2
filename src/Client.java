import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;


import static andUtils.Utils.*;
import static andUtils.SecurityUtils.*;

@SuppressWarnings({"Duplicates", "OptionalGetWithoutIsPresent"})
class Client {
    private static final int PORT = 50000;
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private JTextArea chatArea;
    private JTextField inputArea;
    private JFrame frame;
    private JPanel panel;
    private Thread readThread;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Socket socket;
    private String activeUser;
    private String recipient;
    private ArrayList<String> contacts;

    public static void main(String[] args) {
        new Client();
    }

    private Client() {
        makeGUILookNice("Segoe UI Semilight", Font.PLAIN, 14);
        init();
    }

    private void init() {

        frame = new JFrame("Chat");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        panel = new JPanel(null);
        panel.setBounds(700,550,700,500);

        ArrayList<JLabel> labels = new ArrayList<>();
        labels.add(new JLabel("Username:"));
        labels.add(new JLabel("Password:"));

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        ArrayList<JTextComponent> fields = new ArrayList<>();
        fields.add(userField);
        fields.add(passField);

        int y = 60;
        for(JLabel l : labels) {
            l.setBounds(200, y, 150, 150);
            y = y + 100;
            panel.add(l);
        }
        y = 110;
        for(JTextComponent f : fields) {
            f.setBounds(330,y,150,50);
            y = y + 100;
            panel.add(f);
        }

        JButton logInButton = new JButton("Login");
        logInButton.setBounds(250, 310,200,50);
        panel.add(logInButton);
        JButton registerButton = new JButton("Register");
        registerButton.setBounds(250, 380,200,50);
        panel.add(registerButton);

        userField.requestFocus();

        logInButton.addActionListener(e -> {
            try {
                try {
                    // make connection to server:
                    socket = new Socket(SERVER_ADDRESS,PORT);
                    System.out.println("Opened connection to server");
                    output = new ObjectOutputStream(socket.getOutputStream());
                    input = new ObjectInputStream(socket.getInputStream());
                    System.out.println("Opened streams!");
                    //send "login" signal to server:
                    Signal loginSignal = Signal.LOGIN;
                    output.writeObject(loginSignal);

                    //send name to server to check if user exists:
                    String enteredName = userField.getText();
                    output.writeObject(enteredName);
                    char[] enteredPass = passField.getPassword();
                    output.writeObject(enteredPass);

                    System.out.println("Wrote info to server.");
                    Object readObject = input.readObject();
                    if(!(readObject instanceof IllegalArgumentException)) {
                        Boolean auth = (Boolean) readObject;
                        if(auth) {
                            System.out.println("Logged in!");
                            activeUser = enteredName;

                            //get the users contacts:
                            //noinspection unchecked
                            contacts = (ArrayList<String>) input.readObject();
                            System.out.println("Got contacts: "+contacts);
                            new PopUp().infoBox("Successfully logged in as: "+enteredName);
                            drawMainWindow();
                            System.out.println("Main window drawn!");
                            readThread = new Thread(new ReadThread());
                            readThread.start();
                            System.out.println("Started readthread!");

                        } else {
                            new PopUp().errorBox("Wrong login information.");
                            socket.close();
                            System.out.println("Wrong pass!\nWebsocket closed!");
                        }
                    } else {
                        ((IllegalArgumentException) readObject).printStackTrace();
                        new PopUp().errorBox("Wrong login information.");

                        socket.close();
                    }
                } catch(Exception ex) {
                    System.err.println("Error in connecting to server!");
                    new PopUp().errorBox("Couldn't connect to the server. Please wait a moment before trying again.");
                }
            } catch (Exception ex) {
                System.err.println("Some weird exception!");
                ex.printStackTrace();
            }
        });

        registerButton.addActionListener(e -> drawRegister());
        frame.setContentPane(panel);
        frame.setSize(700,525);
        frame.setResizable(false);
        frame.setVisible(true);

    }

    private void drawMainWindow() {
        redrawBasic();
        chatArea = new JTextArea();
        inputArea = new JTextField();

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem refresh = new JMenuItem("Refresh chat");

        refresh.addActionListener(e -> drawMainWindow());

        menu.add(refresh);
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);


        chatArea.setMargin(new Insets(6,6,6,6));
        chatArea.setEditable(false);
        JScrollPane chatPane = new JScrollPane(chatArea);
        chatPane.setBounds(185,25,490,320);
        chatPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        chatPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        chatArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        inputArea.setMargin(new Insets(6,6,6,6));
        JScrollPane inputPane = new JScrollPane(inputArea);
        inputPane.setBounds(185,350,490,50);
        inputPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        inputPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JButton sendButton = new JButton("Send");
        sendButton.setBounds(575,410,100,35);

        JButton findButton = new JButton("Find Contacts");
        findButton.setBounds(25,410,130,35);

        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        findButton.addActionListener(this::findContactAction);

        sendButton.addActionListener(e -> sendMessage());

        String[] users = contacts.toArray(String[]::new);
        JList<String> contactsList = new JList<>(users);
        JScrollPane listScroller = new JScrollPane(contactsList);
        listScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        listScroller.setBounds(25,25,160,320);


        contactsList.addListSelectionListener(new ListListener());

        chatArea.setText("Choose a contact from the menu");
        inputArea.setEditable(false);
        panel.add(chatPane);
        panel.add(inputPane);
        panel.add(listScroller);
        panel.add(sendButton);
        panel.add(findButton);
        refreshFrame();

    }

    private void drawRegister() {
        redrawBasic();

        ArrayList<JLabel> labels = new ArrayList<>();
        labels.add(new JLabel("Username: "));
        labels.add(new JLabel("Password: "));
        labels.add(new JLabel("Confirm password: "));

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();

        ArrayList<JTextComponent> fields = new ArrayList<>();
        fields.add(userField);
        fields.add(passField);
        fields.add(confirmField);

        int y = 50;
        for(JLabel l : labels) {
            l.setBounds(200, y, 150, 150);
            y = y + 100;
            panel.add(l);
        }
        y = 100;
        for(JTextComponent f : fields) {
            f.setBounds(330,y,150,50);
            y = y + 100;
            panel.add(f);
        }

        JButton registerButton = new JButton("Register user");
        registerButton.setBounds(250, 375,200,50);
        panel.add(registerButton);

        registerButton.addActionListener(e -> {
            boolean matches = Arrays.equals(passField.getPassword(), confirmField.getPassword());
            if (matches) {
                String enteredName = userField.getText();
                String newSalt = generateSalt(160).get();
                String securePassword = hashPassword(passField.getPassword(), newSalt).get();
                RegisterPackage pack = new RegisterPackage(enteredName, newSalt, securePassword);
                try {
                    socket = new Socket(SERVER_ADDRESS, PORT);
                    output = new ObjectOutputStream(socket.getOutputStream());
                    input = new ObjectInputStream(socket.getInputStream());

                    // create new signal and send it to server so it knows this is a register request:
                    Signal registerSignal = Signal.REG;
                    output.writeObject(registerSignal);
                    System.out.println("Wrote register signal!");

                    //write register package:
                    output.writeObject(pack);
                    System.out.println("Wrote register package!");

                    // get success signal:
                    Object readObject = input.readObject();
                    if (!(readObject instanceof IllegalArgumentException)) {
                        Boolean registerSucceeded = (Boolean) readObject;
                        if (registerSucceeded) {
                            new PopUp().infoBox("Registered successfully! Returning to login screen.");
                            socket.close();
                            frame.dispose();
                            Client.this.init();
                        } else {
                            new PopUp().errorBox("Error occurred during registering. Please wait a moment before trying again.");
                            socket.close();
                        }
                    } else {
                        ((IllegalArgumentException) readObject).printStackTrace();
                        new PopUp().errorBox("Username taken!");
                        socket.close();
                    }

                } catch (IOException | ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
            } else {
                new PopUp().errorBox("Passwords don't match!");
            }
        });

        refreshFrame();
    }

    private void redrawBasic() {
        frame.getContentPane().removeAll();
        panel = new JPanel(null);
        frame.setContentPane(panel);
    }

    private void refreshFrame() {
        frame.validate();
        frame.repaint();
        frame.setVisible(true);
    }

    private void sendMessage() {
        try {
            String message = inputArea.getText().trim();
            if(message.equals("")) return;
            output.writeObject(recipient+"|"+message);
            System.out.printf("Wrote message %s%n", message);
            inputArea.requestFocus();
            inputArea.setText(null);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(null,"Error.\n"+ex.toString()+"\nPerhaps the server was closed?","Error!",JOptionPane.ERROR_MESSAGE);
            System.err.println("Error in sending message!");
            ex.printStackTrace();
            System.exit(0);
        }
    }


    private void findContactAction(ActionEvent e) {
        String addedContact = JOptionPane.showInputDialog(null, "Find contact:", "Add new contact", JOptionPane.PLAIN_MESSAGE);
        boolean found = false;
        Signal signal = Signal.CON;
        try {
            Socket tempSocket = new Socket(SERVER_ADDRESS, PORT);
            ObjectOutputStream tempOut = new ObjectOutputStream(tempSocket.getOutputStream());
            ObjectInputStream tempIn = new ObjectInputStream(tempSocket.getInputStream());
            // send addcontact signal:
            tempOut.writeObject(signal);

            // send active user:
            tempOut.writeObject(activeUser);

            // send name of added contact:
            tempOut.writeObject(addedContact);

            System.out.println("Finished sending addcontact req!");

            // get success message:
            Object readObject = tempIn.readObject();
            if (!(readObject instanceof IllegalArgumentException)) {
                System.out.println("Added contact successfully!");
                found = true;
                // get newly updated contact list:
                // noinspection unchecked
                contacts = (ArrayList<String>) readObject;
                System.out.printf("Contacts: %s%n", contacts);
                new PopUp().infoBox("Added new contact!");
                drawMainWindow();
            } else {
                System.err.println("Couldn't add contact!");
                ((IllegalArgumentException) readObject).printStackTrace();
                if (((IllegalArgumentException) readObject).getMessage().equals("User already has this contact!")) {
                    found = true;
                    new PopUp().errorBox("You already have this contact.");
                } else if (((IllegalArgumentException) readObject).getMessage().equals("Can't add yourself!")) {
                    found = true;
                    new PopUp().errorBox("You cannot add yourself as a contact!");
                }
            }
            tempSocket.close();
        } catch (IOException | ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        if (!found) {
            new PopUp().errorBox("No user with this username found.");
        }
    }

    class PopUp
    {
        void infoBox(String infoMessage)
        {
            JOptionPane.showMessageDialog(null, infoMessage, "Info", JOptionPane.INFORMATION_MESSAGE);
        }

        void errorBox(String errorMessage) {
            JOptionPane.showMessageDialog(null, errorMessage,"Error!",JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ListListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            JList list = (JList) e.getSource();
            String[] value = ((String) list.getSelectedValue()).split(" ");
            String name = value[0];
            recipient = name;
            System.out.printf("Changed recipient: %s%n", recipient);
            if(!e.getValueIsAdjusting() && !name.equals("")) {
                try {
                    Socket tempSocket = new Socket(SERVER_ADDRESS,PORT);
                    System.out.println("Opened temp socket...");
                    ObjectOutputStream tempOut = new ObjectOutputStream(tempSocket.getOutputStream());
                    ObjectInputStream tempIn = new ObjectInputStream(tempSocket.getInputStream());
                    Signal signal = Signal.CHECK;
                    tempOut.writeObject(signal);
                    System.out.println("Wrote signal...");
                    tempOut.writeObject(recipient);
                    System.out.println("Wrote recipient name...");
                    Boolean isOnline = (Boolean) tempIn.readObject();
                    if(!isOnline) {
                        System.out.println("Not online!");
                        inputArea.setEditable(false);
                        chatArea.setText("User is offline.");
                    } else {
                        System.out.println("Online!");
                        inputArea.setEditable(true);
                        chatArea.setText(getLog(activeUser,recipient).get());
                    }
                    tempSocket.close();
                    System.out.println("Closed socket.");
                } catch (IOException | ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        }

        Optional<String> getLog(String user1, String user2) {
            try {
                Socket tempSocket = new Socket(SERVER_ADDRESS,PORT);
                ObjectOutputStream tempOut = new ObjectOutputStream(tempSocket.getOutputStream());
                ObjectInputStream tempIn = new ObjectInputStream(tempSocket.getInputStream());
                Signal signal = Signal.GET;
                tempOut.writeObject(signal);
                tempOut.writeObject(user1+"+"+user2);

                String in = (String) tempIn.readObject();
                System.out.println("read "+in);
                tempSocket.close();
                return Optional.of(in);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }
    }

    class ReadThread implements Runnable {
        @Override
        public void run() {
            String message;
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    message = (String) input.readObject();
                    String[] msgSplit = message.split(":");
                    String sender = msgSplit[0];
                    if(!message.isEmpty()) {
                        if(sender.equals(activeUser) || sender.equals(recipient)) {
                            chatArea.append(message + "\n");
                        }
                    }
                } catch(Exception ex) {
                    System.err.println("failed to parse incoming message");
                    ex.printStackTrace();
                }
            }
        }
    }
}

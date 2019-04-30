import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;

import java.net.Socket;
import java.util.ArrayList;


import static andUtils.Utils.*;

public class Client {
    private static final int PORT = 50000;
    private JTextArea chatArea;
    private JTextField inputArea;
    private JFrame frame;
    private JPanel panel;
    private Thread readThread;
    private BufferedReader input;
    private PrintWriter output;
    private static final String SERVER_ADDRESSS = "127.0.0.1";
    private Socket socket;
    private final ArrayList<User> userList;
    private User activeUser;
    private User recipient;

    private Client() {
        makeGUILookNice("Segoe UI Semilight", Font.PLAIN, 14);

        userList = new ArrayList<>();
        userList.add(new User());
        userList.add(new User("test"));
        userList.add(new User("test2"));

        init();
    }
    private void init() {

        frame = new JFrame("Chat");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        panel = new JPanel(null);
        panel.setBounds(700,500,700,500);

        ArrayList<JLabel> labels = new ArrayList<>();
        labels.add(new JLabel("Username:"));
        labels.add(new JLabel("Password:"));

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        ArrayList<JTextComponent> fields = new ArrayList<>();
        fields.add(userField);
        fields.add(passField);

        int y = 100;
        for(JLabel l : labels) {
            l.setBounds(200, y, 150, 150);
            y = y + 100;
            panel.add(l);
        }
        y = 150;
        for(JTextComponent f : fields) {
            f.setBounds(330,y,150,50);
            y = y + 100;
            panel.add(f);
        }

        JButton logInButton = new JButton("Login");
        logInButton.setBounds(250, 350,200,50);
        panel.add(logInButton);

        userField.requestFocus();

        logInButton.addActionListener(e -> {
            boolean foundUser = false;
            for(User b : userList) {
                if(b.getUsername().equals(userField.getText())) {
                    foundUser = true;
                    //noinspection PointlessBooleanExpression
                    if(b.authenticate(passField.getPassword()) == true) {
                        new PopUp().infoBox("Du er nu logget ind som: "+b.getUsername());
                        activeUser = b;

                        try {
                            socket = new Socket(SERVER_ADDRESSS,PORT);
                            activeUser.setStatus(Status.ON);
                            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            output= new PrintWriter(socket.getOutputStream());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            System.out.println("Couldn't connect to server!");
                        }
                        System.out.println(activeUser.getStatus());
                        try (ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream())) {
                            objOut.writeObject(activeUser);
                            System.out.println("Wrote user to server!");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        drawMainWindow();
                    } else new PopUp().infoBox("Forkert kodeord. Prøv igen.");
                }
            }
            if(!foundUser) {
                new PopUp().infoBox("Ingen bruger med dette navn fundet.");
            }
        });



        frame.setContentPane(panel);
        frame.setSize(700,500);
        frame.setResizable(false);
        frame.setVisible(true);

    }

    private void drawMainWindow() {
        redrawBasic();
        chatArea = new JTextArea();
        inputArea = new JTextField();


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

        findButton.addActionListener(e -> {
            String addedContact = JOptionPane.showInputDialog(null,"Find contact:","Add new contact",JOptionPane.PLAIN_MESSAGE);
            boolean found = false;
            for(User user : userList) {
                if(addedContact.equals(user.getUsername())) {
                    activeUser.addContact(user);
                    found = true;
                    new PopUp().infoBox("New contact added: " + user.getUsername());
                    new Log(activeUser,user).createLog();
                    drawMainWindow();
                }
            }
            if(!found) {
                new PopUp().infoBox("No user with this username found.");
            }
        });

        sendButton.addActionListener(e -> sendMessage());

        String[] users = activeUser.getContacts().stream().map(b -> b.getUsername() + " | " + b.getStatus()).toArray(String[]::new);
    /*    StringBuilder users = new StringBuilder();
        for(User u : activeUser.getContacts()) {
            users.append(u.getUsername()).append(" | ").append(u.getStatus()).append("\n");
        }
        JTextField testfield = new JTextField(users.toString());
        testfield.setEditable(true); */
        JList<String> contactsList = new JList<>(users);
        JScrollPane listScroller = new JScrollPane(contactsList);
        listScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        listScroller.setBounds(25,25,160,320);


        contactsList.addListSelectionListener(new ListListener());
        chatArea.getDocument().addDocumentListener(new ChatListener());

        panel.add(chatPane);
        panel.add(inputPane);
        panel.add(listScroller);
        panel.add(sendButton);
        panel.add(findButton);
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

    void sendMessage() {
        try {
            String message = inputArea.getText().trim();
            if(message.equals("")) return;
            output.println(message);
            inputArea.requestFocus();
            inputArea.setText(null);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(null,ex.getMessage());
            System.exit(0);
        }


    }

    public static void main(String[] args) {
        new Client();
    }

    class PopUp
    {
        void infoBox(String infoMessage)
        {
            JOptionPane.showMessageDialog(null, infoMessage, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    class ListListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            readThread.interrupt();
            JList list = (JList) e.getSource();
            String[] value = ((String) list.getSelectedValue()).split(" ");
            String name = value[0];
            if(!e.getValueIsAdjusting()) {
                for(User u : userList) {
                    if (name.equals(u.getUsername())) {
                        recipient = u;
                    }
                }
            }
            if(Log.logExists(activeUser, recipient)) {
                chatArea.setText(Log.getLog(activeUser,recipient));
            }
            readThread = new Thread(new ReadThread());
            readThread.start();
        }
    }

    class ChatListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            Log.writeToLog(activeUser,recipient,chatArea.getText());
            System.out.println("Wrote log");
        }

        @Override
        public void removeUpdate(DocumentEvent e) {

        }

        @Override
        public void changedUpdate(DocumentEvent e) {

        }
    }

    class ReadThread implements Runnable {

        @Override
        public void run() {
            String message;
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    message = input.readLine();
                    if(!message.isEmpty()) {
                        chatArea.append(message + "\n");
                    }
                } catch(Exception ex) {
                    System.err.println("failed to parse incoming message");
                }
            }
        }
    }
}

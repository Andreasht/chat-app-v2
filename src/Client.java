
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


import static andUtils.Utils.*;

public class Client {
    static final int PORT = 50000;
    JTextPane chatArea;
    JTextField inputArea;
    JFrame frame;
    JPanel panel;
    Thread readThread;
    BufferedReader input;
    PrintWriter output;
    InetAddress serverAddress;
    Socket server;
    ArrayList<User> userList;

    public Client() {
        makeGUILookNice("Segoe UI Semilight", Font.PLAIN, 14);
        try {
            serverAddress = InetAddress.getByName("10.147.20.221");
        } catch (UnknownHostException ex) {
            System.out.println("Error in connecting to server. Is the server running?\n");
            ex.printStackTrace();
        }
        userList = new ArrayList<>();
        userList.add(new User());

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
                if(b.getBrugernavn().equals(userField.getText())) {
                    foundUser = true;
                    //noinspection PointlessBooleanExpression
                    if(b.authenticate(passField.getPassword()) == true) {
                        new PopUp().infoBox("Du er nu logget ind som: "+b.getBrugernavn());
                        drawChatWindow();
                    } else {
                        new PopUp().infoBox("Forkert kodeord. Prøv igen.");
                    }
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

    private void drawChatWindow() {
        chatArea = new JTextPane();
        inputArea = new JTextField();
        chatArea.setBounds(25,25,490,320);
        chatArea.setMargin(new Insets(6,6,6,6));
        chatArea.setEditable(false);
        JScrollPane chatPane = new JScrollPane(chatArea);
        chatPane.setBounds(25,25,490,320);

        chatArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        inputArea.setBounds(0,350,400,50);
        inputArea.setMargin(new Insets(6,6,6,6));
        JScrollPane inputPane = new JScrollPane(inputArea);
        inputPane.setBounds(25,350,650,50);

        JButton sendButton = new JButton("Send");
        sendButton.setBounds(575,410,100,35);

        JButton disconnectButton = new JButton("Disconnect");
        disconnectButton.setBounds(25,410,130,35);

        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // sendMessage();
                }
            }
        });

        // sendButton.addActionListener(e -> sendMessage());
    }

    void redrawBasic() {
        frame.getContentPane().removeAll();
        panel = new JPanel();
        frame.setContentPane(panel);
    }

    void refreshFrame() {
        frame.validate();
        frame.repaint();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new Client();
    }

    class PopUp
    {
        public void infoBox(String infoMessage)
        {
            JOptionPane.showMessageDialog(null, infoMessage, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

}

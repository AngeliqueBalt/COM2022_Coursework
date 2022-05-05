package UDP_Chat;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

//@SuppressWarnings("serial")
public class ClientWindow extends JFrame {
    String hostName;
    JTextPane messageField;
    JTextPane roomField;

    String message = "";
    boolean messageReady = false;

    public ClientWindow() {

        JDialog hostNameDialog = new JDialog(this, "Enter server address: ", true);
        JTextField hostField = new JTextField("                            ");
        JButton ok = new JButton("OK");

        hostNameDialog.setLayout(new FlowLayout());
        hostNameDialog.add(hostField);
        hostNameDialog.add(ok);

        hostNameDialog.setLocationRelativeTo(null);
        hostNameDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        hostNameDialog.setSize(250, 65);
        hostNameDialog.setResizable(false);

        ok.addActionListener(e -> {
            hostName = hostField.getText().trim();
            hostNameDialog.dispose();
        });

        hostNameDialog.setVisible(true);

        setSize(800, 600);
        setTitle("Chat room");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        roomField = new JTextPane();
        messageField = new JTextPane();
        roomField.setEditable(false);

        ScrollPane x = new ScrollPane();
        x.add(roomField);
        ScrollPane z = new ScrollPane();
        z.add(messageField);
        z.setPreferredSize(new Dimension(100, 100));
        add(x, BorderLayout.CENTER);
        add(z, BorderLayout.SOUTH);

        setVisible(true);
        messageField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == 10) {
                    messageField.setCaretPosition(0);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10 && !messageReady) {
                    message = messageField.getText().trim();
                    messageField.setText(null);
                    //noinspection ConstantConditions
                    if (!message.equals(null) && !message.equals("")) {
                        messageReady = true;
                    }
                }
            }
        });
    }

    public void displayMessage(String receivedMessage) {
        StyledDocument doc = roomField.getStyledDocument();

        try {
            doc.insertString(doc.getLength(), receivedMessage + "\n", null);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
    }

    public void setMessageReady(boolean messageReady) {
        this.messageReady = messageReady;
    }

    public String getMessage() {
        return message;
    }

    public String getHostName() {
        return hostName;
    }
}

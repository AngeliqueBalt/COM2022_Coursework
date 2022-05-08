package UDP_Chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import static UDP_Chat.MessageSender.PORT;

class MessageSender implements Runnable {
    public final static int PORT = 4545;    // The servers port number
    private final DatagramSocket socket;    // Socket to handle connection
    private final String hostName;
    private final ClientWindow window;

    MessageSender(DatagramSocket sock, String host, ClientWindow win) {
        socket = sock;
        hostName = host;
        window = win;
    }

    public void run() {
        while (!socket.isClosed()) {
            // While the client is connected send messages else throw exception
            try {
                while (!window.messageReady) {
                    Thread.sleep(100);
                }
                PacketWriter.sendMessage(socket, hostName, PORT, window.getMessage());
                window.setMessageReady(false);
            } catch (Exception e) {
                window.displayMessage(e.getMessage());
            }
        }
    }
}

class MessageReceiver implements Runnable {
    DatagramSocket socket;
    byte[] buffer;
    ClientWindow window;

    // Constructor
    MessageReceiver (DatagramSocket sock, ClientWindow win) {
        socket = sock;
        buffer = new byte[1024];
        window = win;
    }

    public void run () {
        while (!socket.isClosed()) {
            try {
                System.out.println("Waiting for packet...");

                // Create a DatagramPacket
                Arrays.fill(buffer, (byte) 0);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                System.out.println("Received packet...");

                // Create new string to be sent when packet received by server
//                String received = new String(packet.getData());
                String received = PacketReader.handlePacket(packet, socket, (server, signal) -> {
                    if (signal.equals("ALV")){
                        var payload = new byte[]{'A', 'L', 'V'};
                        try {
                            socket.send(new DatagramPacket(payload, payload.length, server.address, server.port));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                if (received == null) continue;

                System.out.println(received);
                window.displayMessage(received);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

public class Client {
    public static void main(String[] args) throws Exception {
        // Request server IP.
        String hostName = ClientWindow.requestHostName();

        // Initialize socket connection
        DatagramSocket socket = new DatagramSocket();
        boolean connected = false;

        // Check to see if the client is connected or not
        ClientWindow window = new ClientWindow();
        do {
            try {
                var hostAddress = InetAddress.getByName(hostName);

                var NEW = new byte[]{'N', 'E', 'W'};
                socket.send(new DatagramPacket(NEW, NEW.length, hostAddress, PORT));

                var alvBuffer = new byte[3];
                var alvDatagram = new DatagramPacket(alvBuffer, alvBuffer.length);
                socket.receive(alvDatagram);

                if (new String(alvDatagram.getData()).equals("ALV")) {
                    var ALV = new byte[]{'A', 'L', 'V'};
                    socket.send(new DatagramPacket(ALV, ALV.length, hostAddress, PORT));
                    connected = true;
                }
            } catch (Exception e) {
                window.displayMessage(e.getMessage());
            }
        } while (!connected);

        System.out.println("Connected to server.");

        // Open chat client window
        window.setTitle("UDP CHAT | Server: " + hostName);

        MessageReceiver receiver = new MessageReceiver(socket, window);
        MessageSender sender = new MessageSender(socket, hostName, window);
        Thread receiverThread = new Thread(receiver);
        Thread senderThread = new Thread(sender);
        receiverThread.start();
        senderThread.start();
    }
}

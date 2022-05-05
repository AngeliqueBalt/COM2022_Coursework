package UDP_Chat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.zip.CRC32;

class MessageSender implements Runnable {
    public final static int PORT = 2468;    // The servers port number
    private final DatagramSocket socket;    // Socket to handle connection
    private final String hostName;
    private final ClientWindow window;

    MessageSender(DatagramSocket sock, String host, ClientWindow win) {
        socket = sock;
        hostName = host;
        window = win;
    }

    private void sendMessage (String s) throws Exception {

        var sb = s.getBytes();
        int packetCount = (int) Math.ceil(sb.length/1008F);
        InetAddress address = InetAddress.getByName(hostName);

        for (int i = 0; i < packetCount; i++) {

            var byteOutput = new ByteArrayOutputStream();
            var output = new DataOutputStream(byteOutput);

            // Make the packet
            var payload = new byte[1008];
            var packetLength = payload.length;
            if (sb.length % 1008 != 0 && i == packetCount - 1) packetLength = sb.length % 1008;

            System.arraycopy(sb, i*1008, payload, 0, packetLength);

            var checksum = new CRC32();
            checksum.update(payload);

            output.writeInt((int) checksum.getValue());
            output.writeInt(i);
            output.writeInt(packetCount);
            output.write(payload);

            output.flush();
            var buffer = byteOutput.toByteArray();

            var packet = new DatagramPacket(buffer, buffer.length, address, PORT);
            socket.send(packet);
        }

    }

    public void run() {
        boolean connected = false;

        // Check to see if the client is connected or not
        do {
            try {
                sendMessage("New client connected");
                connected = true;
            } catch (Exception e) {
                window.displayMessage(e.getMessage());
            }
        } while (!connected);

        while (!socket.isClosed()) {
            // While the client is connected send messages else throw exception
            try {
                while (!window.messageReady) {
                    Thread.sleep(100);
                }
                sendMessage(window.getMessage());
                window.setMessageReady(false);
            } catch (Exception e) {
                window.displayMessage(e.getMessage());
            }
        }
    }
}

class MessageReceiver implements Runnable {
    DatagramSocket socket;
    byte buffer[];
    ClientWindow window;

    MessageReceiver (DatagramSocket sock, ClientWindow win) {
        socket = sock;
        buffer = new byte[1024];
        window = win;
    }

    public void run () {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                // Create a DatagramPacket
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Create new string to be sent when packet received by server
                String received = new String(packet.getData());
                System.out.println(received);
                window.displayMessage(received);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

public class Client {
    public static void main(String args[]) throws Exception {

        ClientWindow window = new ClientWindow();
        String host = window.getHostName();
        window.setTitle("UDP CHAT  Server: " + host);
        DatagramSocket socket = new DatagramSocket();
        MessageReceiver receiver = new MessageReceiver(socket, window);
        MessageSender sender = new MessageSender(socket, host, window);
        Thread receiverThread = new Thread(receiver);
        Thread senderThread = new Thread(sender);
        receiverThread.start();
        senderThread.start();
    }
}

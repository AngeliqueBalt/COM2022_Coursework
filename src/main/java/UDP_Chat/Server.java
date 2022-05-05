package UDP_Chat;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server implements Runnable {
    public final static int PORT = 2468;    // Servers port number
    private final static int BUFFER = 1024; // Buffer size

    private final DatagramSocket socket;    // Servers socket
    private final ArrayList<InetAddress> clientAddresses;   // Array list of all client addresses
    private final ArrayList<Integer> clientPorts;   // Array List of all  client ports
    private final HashSet<String> existingClients;  // Hash set of all existing clients

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Server() throws IOException {
        socket = new DatagramSocket(PORT);  // Create new socket
        System.out.println("Server running on port " + PORT);

        // Initialise arrays and hashset
        clientAddresses = new ArrayList();
        clientPorts = new ArrayList();
        existingClients = new HashSet();
    }

    public void run() {
        byte[] buffer = new byte[BUFFER];

        while (!socket.isClosed()) {
            try {
                // Arrays.fill(buffer, (byte) 0);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Read the input stream from the sender.
                var message = PacketReader.handlePacket(packet);

                if (message == null) {
                    continue;
                }

                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                String id = clientAddress.toString() + "|" + clientPort;

                // See if the client exists or not by using the id created above
                if (!existingClients.contains(id)) {
                    existingClients.add(id);
                    clientPorts.add(clientPort);
                    clientAddresses.add(clientAddress);
                }

                System.out.println(id + ": " + message);
//                byte[] data = (id + ": " + message).getBytes(StandardCharsets.UTF_8);

                // Loop through the client addresses and create a new packet with the data
                for (int i = 0; i < clientAddresses.size(); i++) {
                    InetAddress cl_address = clientAddresses.get(i);
                    int cl_port = clientPorts.get(i);

                    PacketWriter.sendMessage(socket, cl_address, cl_port, id + ": " + message);
//                    packet = new DatagramPacket(data, data.length, cl_address, cl_port);
//                    socket.send(packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Server serverThread = new Server();     // Create the thread
        serverThread.run();     // Start running the thread
    }
}

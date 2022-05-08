package UDP_Chat;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server implements Runnable {
    public final static int PORT = 4545;    // Servers port number
    private final static int BUFFER = 1024; // Buffer size

    private final DatagramSocket socket;    // Servers socket
    private final ArrayList<InetAddress> clientAddresses;   // Array list of all client addresses
    private final ArrayList<Integer> clientPorts;   // Array List of all  client ports
    private final HashSet<ConnectedClient> existingClients;  // Hash set of all existing clients

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
                var message = PacketReader.handlePacket(packet, socket, (client, signal) -> {
                    switch (signal) {
                        case "NEW" -> {
                            var payload = new byte[]{'A', 'L', 'V'};
                            existingClients.add(client);
                            for (var existingClient : existingClients) {
                                try {
                                    socket.send(new DatagramPacket(payload, payload.length, existingClient.address, existingClient.port));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            existingClients.clear();
                        }
                        case "ALV" -> existingClients.add(client);
                        case "BYE" -> existingClients.remove(client);
                    }
                });

                if (message == null) {
                    continue;
                }


                var client = new ConnectedClient(packet.getAddress(), packet.getPort());

                // See if the client exists or not by using the id created above
//                if (!existingClients.contains(id)) {
//                    existingClients.add(id);
//                    clientPorts.add(clientPort);
//                    clientAddresses.add(clientAddress);
//                }

                System.out.println(client + ": " + message);
//                byte[] data = (id + ": " + message).getBytes(StandardCharsets.UTF_8);

                // Loop through the client addresses and create a new packet with the data
                for (var existingClient: existingClients) {
                    PacketWriter.sendMessage(socket, existingClient.address, existingClient.port, existingClient + ": " + message);
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

package UDP_Chat;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.CRC32;

interface SignalHandler {
    void handleSignal(ConnectedClient client, String signal);
}

public class PacketReader {

    private static final Map<Integer, byte[][]> partialPackets = new HashMap<>();

    public static String handlePacket(DatagramPacket packet, DatagramSocket socket, SignalHandler handler) throws IOException {

        // Read the input stream from the sender.
        InetAddress clientAddress = packet.getAddress();
        int clientPort = packet.getPort();

        String sender = clientAddress.toString() + "|" + clientPort;

        if (packet.getLength() == 3) {
            var message = new String(packet.getData(), 0, 3, StandardCharsets.UTF_8);

            System.out.println("Received " + message);
            var client = new ConnectedClient(packet.getAddress(), packet.getPort());
            handler.handleSignal(client, message);
            return null;
        }

        var byteInput = new ByteArrayInputStream(packet.getData());
        var input = new DataInputStream(byteInput);

        var id = input.readInt();
        var checksum = input.readInt();
        var current = input.readInt();
        var total = input.readInt();

        var payload = new byte[1008];
        input.read(payload);

        input.close();
        byteInput.close();

        var crc32 = new CRC32();
        crc32.update(payload);
        int expectedChecksum = (int) crc32.getValue();


        System.out.println("Sender = " + sender);
        System.out.println("ID = " + id);
        System.out.println("expectedChecksum = " + expectedChecksum);
        System.out.println("checksum = " + checksum);
        System.out.println("current packet = " + current);
        System.out.println("total packets = " + total);

        if (checksum != expectedChecksum) {
//            throw new RuntimeException("Checksum does not match");
            System.err.println("Checksum does not match");
            return null;
        }

        if (!partialPackets.containsKey(id)) {
            partialPackets.put(id, new byte[total][]);
        }

        var ack = new byte[]{'A','C','K'};
        socket.send(new DatagramPacket(ack, ack.length, clientAddress, clientPort));
        System.out.println("Sending ACK");

        partialPackets.get(id)[current] = payload;

        if (hasEntirePacket(id)) {
            System.out.println("Entire packet received");

            try (var allPackets = new ByteArrayOutputStream()) {
                Arrays.stream(partialPackets.get(id)).forEachOrdered(partialPacket -> {
                    try {
                        allPackets.write(partialPacket);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                partialPackets.remove(id);

                return allPackets.toString(StandardCharsets.UTF_8).split("\0")[0];
            }
        }
        return null;
    }

    /**
     * Check whether an entire packet has been received based on its id.
     *
     * @param id The id to lookup.
     * @return True, if the entire packet corresponding to the id has been
     * received, otherwise false.
     */
    private static boolean hasEntirePacket(Integer id) {
        // Look up the array of packet parts in the list of all partial packets.
        var packetParts = partialPackets.get(id);

        if (packetParts.length < 1) throw new RuntimeException("Invalid packet (has less than 1 part.)");

        // Ensure that no value in the packetParts array is null. If there is a
        // null value it means that part has not yet been received.
        return Arrays.stream(packetParts).noneMatch(Objects::isNull);
    }

}

package UDP_Chat;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.CRC32;

public class PacketReader {

    private static final Map<String, byte[][]> partialPackets = new HashMap<>();

    public static String handlePacket(DatagramPacket packet) throws IOException {

        // Read the input stream from the sender.
        InetAddress clientAddress = packet.getAddress();
        int clientPort = packet.getPort();

        String id = clientAddress.toString() + "|" + clientPort;

        var byteInput = new ByteArrayInputStream(packet.getData());
        var input = new DataInputStream(byteInput);

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

        if (checksum != expectedChecksum) {
            System.out.println("expectedChecksum = " + expectedChecksum);
            System.out.println("checksum = " + checksum);
//            throw new RuntimeException("Checksum does not match");
            System.err.println("Checksum does not match");
            return null;
        }

        if (!partialPackets.containsKey(id)) {
            partialPackets.put(id, new byte[total][]);
        }

        partialPackets.get(id)[current] = packet.getData();

        if (hasEntirePacket(id)) {
            try (var allPackets = new ByteArrayOutputStream()) {
                Arrays.stream(partialPackets.get(id)).forEachOrdered(partialPacket -> {
                    try {
                        allPackets.write(partialPacket);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                partialPackets.remove(id);
                return allPackets.toString(StandardCharsets.UTF_8).split("\u0000")[0];
            }
        }
        return null;
    }

    /**
     * Check whether an entire packet has been received based on its id.
     * <p>
     * This method looks up the value of the specified id in partialPackets
     * and checks that list to ensure there are no null values (which would
     * suggest that the entire packet has been received).
     *
     * @param id The id to lookup.
     * @return True, if the entire packet corresponding to the id has been
     * received, otherwise false.
     */
    private static boolean hasEntirePacket(String id) {
        // Look up the array of packet parts in the list of all partial packets.
        var packetParts = partialPackets.get(id);

        if (packetParts.length < 1) throw new RuntimeException("Invalid packet (has less than 1 part.)");

        // Ensure that no value in the packetParts array is null. If there is a
        // null value it means that part has not yet been received.
        return Arrays.stream(packetParts).noneMatch(Objects::isNull);
    }

}

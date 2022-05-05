package UDP_Chat;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;
import java.util.zip.CRC32;

//import static jdk.internal.org.jline.utils.Colors.s;

public class PacketReader {

    private Map<String, byte[][]> partialPackets;

    String id = packet.getAddress().toString() + "|" + packet.getPort();

    public String handlePacket(DatagramPacket packet) throws IOException {


        // TODO: parse packet header



        // TODO: validate packet with checksum

        private void receiveMessage (String s) throws Exception {

            var sb = s.getBytes();
            int packetCount = (int) Math.ceil(sb.length/8F);
            InetAddress address = InetAddress.getByName(hostName);

            for (int i = 0; i < packetCount; i++) {

                var byteOutput = new ByteArrayInputStream();
                var output = new DataInputStream(byteOutput);

                // Make the packet
                var payload = new byte[8];
                var packetLength = payload.length;
                if (sb.length % 8 != 0 && i == packetCount - 1) packetLength = sb.length % 8;

                System.arraycopy(sb, i*8, payload, 0, packetLength);

                var checksum = new CRC32();
                checksum.update(payload);

                output.readInt();
                output.readInt();
                output.readInt();
                output.read(payload);

                output.flush();
                var buffer = byteOutput.toByteArray();

                var packet = new DatagramPacket(buffer, buffer.length, address, PORT);
                socket.send(packet);
            }

            if (!partialPackets.containsKey(id)) {
                partialPackets.put(id, new ArrayList<Object>(total));
            }

            partialPackets.get(id).set(current, packet.getData());
        }
        
        /**
         * Check whether an entire packet has been received based on its id.
         *
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
            return String.valueOf(Arrays.stream(packetParts).noneMatch(Objects::isNull));
        }

    }

    public static void main(String[] args) {
        partialPackets = new HashMap<>();

        var pkt = new byte[][];
        partialPackets.put(id, pkt);

        System.out.println(hasEntirePacket(id));
    }

}

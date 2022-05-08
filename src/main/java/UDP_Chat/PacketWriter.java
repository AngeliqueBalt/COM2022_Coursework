package UDP_Chat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.CRC32;

public class PacketWriter {

    public static void sendMessage (DatagramSocket socket, String hostName, int port, String s) throws Exception {
        sendMessage(socket, InetAddress.getByName(hostName), port, s);
    }

    public static void sendMessage (DatagramSocket socket, InetAddress address, int port, String s) throws Exception {

        var sb = s.getBytes(StandardCharsets.UTF_8);
        int packetCount = (int) Math.ceil(sb.length/1008F);
        int id = ThreadLocalRandom.current().nextInt();

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

            output.writeInt(id);
            output.writeInt((int) checksum.getValue());
            output.writeInt(i);
            output.writeInt(packetCount);
            output.write(payload);

            output.flush();
            var buffer = byteOutput.toByteArray();

            var packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
        }

    }
}

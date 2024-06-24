package src;

import java.util.zip.CRC32;
import java.nio.charset.StandardCharsets;

public class CRC32Check {
    public static boolean isChecksumValid(String data, long expectedChecksum) {
        CRC32 crc32 = new CRC32();
        crc32.update(data.getBytes(StandardCharsets.UTF_8));
        long calculatedChecksum = crc32.getValue();
        return calculatedChecksum == expectedChecksum;
    }

    public static long getCRC32Checksum(String input) {
        byte[] bytes = input.getBytes();
        CRC32 crc = new CRC32();
        crc.update(bytes, 0, bytes.length);
        return crc.getValue();
    }
}

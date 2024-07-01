package src;

import java.util.Arrays;
import java.util.zip.CRC32;
import java.nio.charset.StandardCharsets;

public class CRC32Check {
    public static boolean isChecksumValid(String data, long expectedChecksum) {
        //main2.logger.info(String.format("[isChecksumValid] Data to check: %s", data));
        CRC32 crc32 = new CRC32();
        crc32.update(data.getBytes(StandardCharsets.UTF_8));
        long calculatedChecksum = crc32.getValue();
        main2.logger.info(String.format("[isChecksumValid] bytes to check: %s", Arrays.toString(data.getBytes(StandardCharsets.UTF_8))));
        main2.logger.info(String.format("[isChecksumValid] Calculated checksum: %s", calculatedChecksum));
        main2.logger.info(String.format("[isChecksumValid] Expected checksum: %s", expectedChecksum));
        return calculatedChecksum == expectedChecksum;
    }

    public static long getCRC32Checksum(String input) {
        main2.logger.info(String.format("[getCRC32Checksum] input: %s", input));
        byte[] bytes = input.getBytes();
        CRC32 crc = new CRC32();
        crc.update(bytes, 0, bytes.length);
        main2.logger.info(String.format("[getCRC32Checksum] Calculated checksum: %s", crc.getValue()));
        return crc.getValue();
    }
}

package software.bigbade.serializationtest;

import java.nio.ByteBuffer;

public class DoubleUtils {
    private static final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

    public static byte[] doubleToBytes(double value) {
        buffer.clear();
        buffer.putDouble(value);
        return buffer.array();
    }

    public static double bytesToDouble(byte[] bytes) {
        buffer.clear();
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getDouble();
    }
}

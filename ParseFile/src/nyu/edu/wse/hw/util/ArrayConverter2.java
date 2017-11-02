package nyu.edu.wse.hw.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ArrayConverter2 {

    public static byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    public static int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}

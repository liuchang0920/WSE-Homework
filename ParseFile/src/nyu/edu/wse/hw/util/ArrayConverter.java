package nyu.edu.wse.hw.util;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class ArrayConverter {

    public static int[] toIntArray(byte buf[]) {
        final ByteBuffer buffer = ByteBuffer.wrap(buf);
        buffer.rewind();
        IntBuffer ib = buffer.asIntBuffer();
        int[] result = new int[buf.length/4];
        int i = 0;
        while(ib.hasRemaining()) {
            result[i++] = ib.get();
        }
        return result;
    }

    public static byte[] toByteArray(int[] ints) {
        final ByteBuffer buf = ByteBuffer.allocate(ints.length * 4);
        buf.asIntBuffer().put(ints);
        return buf.array();
    }

    public static void main(String[] args) {
        int[] testArr = {1,2,3,4,5, 9999, 21};
        byte[] byteArray = ArrayConverter.toByteArray(testArr);

        int [] converted = ArrayConverter.toIntArray(byteArray);
        for(int item: converted) {
            System.out.println(item);
        }
    }
}

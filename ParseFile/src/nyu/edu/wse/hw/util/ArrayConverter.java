package nyu.edu.wse.hw.util;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

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
//        int[] testArr = {1,2,3,4,5, 9999, 21};
//        byte[] byteArray = ArrayConverter.toByteArray(testArr);
//
//        int [] converted = ArrayConverter.toIntArray(byteArray);
//        for(int item: converted) {
//            System.out.println(item);
//        }
        List<Integer> auxiliaryList = new ArrayList<>();
        auxiliaryList.add(1);
        auxiliaryList.add(2);
        auxiliaryList.add(3);
        auxiliaryList.add(4);
        int[] array = auxiliaryList.stream().mapToInt(i -> i).toArray();
        for(int item: array) {
            System.out.println(item);
        }
    }
}

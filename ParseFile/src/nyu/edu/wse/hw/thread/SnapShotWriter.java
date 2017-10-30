package nyu.edu.wse.hw.thread;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class SnapShotWriter implements Runnable{

    private static final String UTF_8 = "UTF-8";

    private final String filePath;
    private int fileName; // docId
    private String content;

    public SnapShotWriter(String filePath, int fileName, String content) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.content = content;
    }

    @Override
    public void run() {
        try{
            PrintWriter writer = new PrintWriter(this.filePath+this.fileName, UTF_8);
            writer.write(content);
            writer.close();

        } catch (UnsupportedEncodingException uee) {
            System.out.println("error utf-8 encoding");
        } catch (FileNotFoundException ffe) {
            System.out.println("file not found exception");
        }
    }
}

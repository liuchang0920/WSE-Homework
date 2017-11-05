package nyu.edu.wse.hw.main;

import com.sun.net.httpserver.HttpServer;
import nyu.edu.wse.hw.handler.QueryHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryExecution {

    private static final Logger log = Logger.getLogger("QueryExecution");

    public static void main(String[] args) {
        // start backend server
        try{
            HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
            server.createContext("/query", new QueryHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException ioe) {
            log.log(Level.SEVERE, "error creating http server");
        }
    }

}

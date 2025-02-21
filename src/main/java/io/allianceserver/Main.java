package io.allianceserver;

import spark.Spark;

public class Main {
    public static void main(String[] args) {
        HTTPAccessServer HTTPServer = new HTTPAccessServer();
        TCPServer TCPServer = new TCPServer();
        try {
            TCPServer.start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
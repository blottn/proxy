package org.blottn;

import java.io.*;
import java.net.*;
import java.util.*;

public class Manage extends Thread {

    ServerSocket socket;
    boolean dead;

    public Manage(int port, int max) throws IOException {
        socket = new ServerSocket(port,max);
        dead = false;
    }

    @Override
    public void run() {
        while (!dead) {
            try {
                new ManageHandler(socket.accept()).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ManageHandler extends Thread {
        
        private Socket connection;
        private PrintWriter out;
        private BufferedReader in;

        ManageHandler(Socket connection) throws IOException {
            this.connection = connection;
            this.out = new PrintWriter(connection.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        }
        @Override
        public void run() {
            try {
                ArrayList<String> requestLines = new ArrayList<String>();
                for (String line = in.readLine() ; line != null && line.length() != 0 ;line = in.readLine()) {
                    requestLines.add(line);
                    System.out.println(line);
                }
                String type = requestLines.get(0).split(" ")[0];
                if (type.equals("GET")){
                    String endpoint = requestLines.get(0).split(" ")[1];
                    if (endpoint.split("\\?")[0].equals("/block") && endpoint.split("\\?").length == 2) {
                        String name = endpoint.split("\\?")[1];
                        Proxy.log("Blocked: " + name);
                        Proxy.blocked.add(name);
                    }
                }
                out.println("HTTP/1.1 200 OK\r\n\r\n");
                connection.close();
            }
            catch (IOException e) {}
        }

        private void output(String line) throws IOException{
            out.print(line + "\r\n");
        }
    }
}

package org.blottn;

import java.io.*;
import java.net.*;
import java.util.*;

public class UI extends Thread {

    ServerSocket socket;
    boolean dead;
    List<String> lines;

    public UI(int port, int max) throws IOException {
        socket = new ServerSocket(port,max);
        lines = new ArrayList<String>();
        dead = false;
    }

    @Override
    public void run() {
        while (!dead) {
            try {
                new UIHandler(socket.accept(), lines).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void print(String s) {
        lines.add(s);
    }

    class UIHandler extends Thread {
        
        private Socket connection;
        private PrintWriter out;
        private BufferedReader in;
        private List<String> lines;

        UIHandler(Socket connection, List<String> lines) throws IOException {
            this.connection = connection;
            this.out = new PrintWriter(connection.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            this.lines = lines;
        }
        @Override
        public void run() {
            try {
                output("HTTP/1.1 200 ");
                output("Content-type: text/html");
                output("Connection: close");
                output("");
                // clear input
                for (String line = in.readLine() ; line != null && line.length() != 0; line = in.readLine()) {}
                for (String line : lines) {
                    out.print("<p>" + line + "</p>");
                }
                out.close();
                in.close();

                connection.close();
            }
            catch (IOException e) {}
            System.out.println("closed");
        }

        private void output(String line) throws IOException{
            out.print(line + "\r\n");
        }
    }
}

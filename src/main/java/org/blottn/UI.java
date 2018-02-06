package org.blottn;

import java.io.*;
import java.net.*;

public class UI extends Thread {

    ServerSocket socket;
    boolean dead;
    

    public UI(int port, int max) throws IOException {
        socket = new ServerSocket(port,max);
        dead = false;
    }

    @Override
    public void run() {
        while (!dead) {
            try {
                new UIHandler(socket.accept()).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class UIHandler extends Thread {
        
        private Socket connection;
        private PrintWriter out;
        private BufferedReader in;

        UIHandler(Socket connection) throws IOException {
            this.connection = connection;
            this.out = new PrintWriter(connection.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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

                out.print("<html><head><title>hello</title></head><body><h1>TITLE!</h1><p>paragraph</p></body></html>\r\n");
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

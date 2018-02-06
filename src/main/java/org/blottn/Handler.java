package org.blottn;

import java.net.*;
import java.io.*;

public class Handler extends Thread {
    
    private Socket connection;
    private PrintWriter out;
    private BufferedReader in;

    public Handler(Socket connection) throws IOException {
        this.connection = connection;
        this.out = new PrintWriter(connection.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    public void run() {
        System.out.println("Running new handler: " + connection.toString());
        System.out.println(connection.getRemoteSocketAddress().toString());
        try {
            out.print("HTTP/1.1 200 \r\n");
            out.print("Content-Type: text/plain\r\n");
            out.print("Connection: close\r\n");
            out.print("\r\n");
            for (String line = in.readLine() ; line != null && line.length() != 0; line = in.readLine()) {
                out.print(line + "\r\n");
            }
            out.close();
            in.close();
            connection.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

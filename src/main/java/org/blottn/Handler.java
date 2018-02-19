package org.blottn;

import java.net.*;
import java.io.*;
import java.util.*;

public class Handler extends Thread {
    
    private Socket connection;
    private PrintWriter out;
    private BufferedReader in;
    private ArrayList<String> lines;
    private UI ui;
    private String host;
    private int port;

    public Handler(Socket connection, UI ui) throws IOException {
        this.connection = connection;
        this.out = new PrintWriter(connection.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        lines = new ArrayList<String>();
        for (String line = in.readLine() ; line != null && line.length() != 0; line = in.readLine()) {
            ui.print(line + "\r\n");
            lines.add(line);
        }
        log(lines);
        parseHost(lines);
        this.ui = ui;
    }

    

    public void run() {
        
        System.out.println("Running new handler: " + connection.toString());
        try {
            Socket socket = new Socket(host,port);
            System.out.println("opened socket");
            PrintWriter serverWriteStream = new PrintWriter(socket.getOutputStream(), true);
            for (String line : lines) {
                serverWriteStream.print(line + "\r\n");
            }
            serverWriteStream.println("");
            serverWriteStream.flush();

            BufferedReader serverReader  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String text = serverReader.readLine();
            while (text != null) {
                System.out.println(text);
                out.println(text);
                text = serverReader.readLine();
            }

            serverReader.close();
            System.out.println("/received");

            out.close();
            in.close();
            connection.close();

        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println(host + " " + port);
        }
    }

    private void log(ArrayList<String> request) {
        System.out.println("Request: ");
        for (String s : request) {
            System.out.println(s);
        }

        System.out.println("/Request\n");
    }

    private void parseHost(ArrayList<String> lines) {
        if (lines.get(0).split(" ")[0].equals("CONNECT")) { //https
            String val = lines.get(0).split(" ")[1];
            if (val.split(":").length == 1) {
                host = val;
                port = 443;
            }
            else {
                host = val.split(":")[0];
                port = Integer.parseInt(val.split(":")[1]);
            }
        }
        else {      //http
            String val = lines.get(1).split(" ")[1];
            if (val.split(":").length == 1) {
                host = val;
                port = 80;
            }
            else {
                host = val.split(":")[0];
                port = Integer.parseInt(val.split(":")[1]);
            }
        }
    }

}

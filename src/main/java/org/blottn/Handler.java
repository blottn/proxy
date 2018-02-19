package org.blottn;

import java.net.*;
import java.io.*;
import java.util.*;

public class Handler extends Thread {
    
    private static final int BUFFER_SIZE = 100000;

    private Socket connection;
    final private OutputStream out;
    private BufferedReader in;
    final private InputStream in_b;
    private ArrayList<String> lines;
    private UI ui;
    private String host;
    private int port;

    public Handler(Socket connection, UI ui) throws IOException {
        this.connection = connection;
        this.out = connection.getOutputStream();
        this.in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        this.in_b = connection.getInputStream();
        lines = new ArrayList<String>();
        this.ui = ui;
    }

    public void run() {
        while (true) {      //keep going till connection goes down (caught below)
            if (lines.size() > 0) { //if we have a new request do the thing
                //check if https or http...
                if (lines.get(0).split(" ")[0].equals("CONNECT")) {
                    https();
                }
                else {
                    try {
                        parseHost(lines);
                        Socket socket = new Socket(host,port);
                        PrintWriter serverWriteStream = new PrintWriter(socket.getOutputStream(), true);
                        BufferedInputStream serverReader = new BufferedInputStream(socket.getInputStream());

                        log(lines); //log request
                        System.out.println("sending request");
                        for (String line : lines) {
                            serverWriteStream.println(line);
                        }
                        serverWriteStream.println("");
                        serverWriteStream.flush();
                    
                        byte[] input = new byte[BUFFER_SIZE];
                        int number = serverReader.read(input);
                        if (number > 0) {
                            out.write(input,0,number);
                        }
                        out.flush();
                        
                        lines.clear();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(host + " " + port);
                    }
                }
            }
            else {
                try {
                    for (String line = in.readLine() ; line != null && line.length() != 0; line = in.readLine()) {
                        lines.add(line);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void https() {
        parseHost(lines);
        try {
            final Socket socket = new Socket(host,port);
            System.out.println(host + " " + port);
            final OutputStream sW = socket.getOutputStream();
            final InputStream sR = socket.getInputStream();
            
            out.write(new String("HTTP/1.1 200 OK\r\n\r\n").getBytes());
            out.flush();

            Thread serverConnection = new Thread() {

                @Override
                public void run() {
                    try {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        while(true) {
                            int num = sR.read(buffer);
                            if (num != -1) {
                                out.write(buffer,0,num);
                            }
                        }
                    }catch(Exception e) {
                        e.printStackTrace();    //stop when socket is closed
                    }
                }
            };
            serverConnection.start();

            while(true) {
                log(lines);
                //pass from client to server
                byte[] buff = new byte[BUFFER_SIZE];
                int read = in_b.read(buff);
                if (read != -1) {
                    sW.write(buff,0,read);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
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

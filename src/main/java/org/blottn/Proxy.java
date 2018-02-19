package org.blottn;

import java.net.*;
import java.io.*;
public class Proxy extends Thread {

    public static final int MAX_PENDING = 100;

    ServerSocket serverSocket;
    UI ui;
    boolean dead;

    public Proxy(int port) {
        dead = false;
        try {
            serverSocket = new ServerSocket(port, MAX_PENDING);
            ui = new UI(8081, MAX_PENDING);
            ui.start();
        } catch(IOException e) {
            dead = true;
            e.printStackTrace();
        }
    }

    public void start() {
        while (!dead) {
            try {
                Handler handler = new Handler(serverSocket.accept(),ui);
                handler.start();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }




	public static void main(String[] args) {
        Proxy proxy = new Proxy(Integer.parseInt(args[0]));
        proxy.start();
	}
}

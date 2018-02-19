package org.blottn;

import java.net.*;
import java.io.*;
import java.util.*;
public class Proxy extends Thread {

    public static final int MAX_PENDING = 100;
    public static List<String> blocked;

    ServerSocket serverSocket;
    Manage manage;
    boolean dead;

    public Proxy(int port) {
        dead = false;
        try {
            serverSocket = new ServerSocket(port, MAX_PENDING);
            manage = new Manage(8081, MAX_PENDING);
            manage.start();
        } catch(IOException e) {
            dead = true;
            e.printStackTrace();
        }
    }

    public void start() {
        while (!dead) {
            try {
                Handler handler = new Handler(serverSocket.accept(),manage);
                handler.start();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void log(String s) {
        System.out.println(s);
    }


	public static void main(String[] args) {
        Proxy proxy = new Proxy(Integer.parseInt(args[0]));
        blocked = new ArrayList<String>();
//        blocked.add("http://example.org/");
        proxy.start();
	}
}

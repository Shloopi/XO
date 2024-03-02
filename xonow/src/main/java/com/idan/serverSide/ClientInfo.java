package com.idan.serverSide;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientInfo {
    private Socket socket;
    private InputStream input;
    private OutputStream output;

    public ClientInfo(Socket socket, InputStream input, OutputStream output) {
        this.socket = socket;
        this.input = input;
        this.output = output;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public InputStream getInput() {
        return this.input;
    }

    public OutputStream getOutput() {
        return this.output;
    }
}

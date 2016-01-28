package sr.akarbarc.node;

import sr.akarbarc.msgs.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Observable;

/**
 * Created by ola on 06.01.16.
 */
public class Connection extends Observable {
    private String id;
    private Socket socket;
    private Thread receiver;
    private boolean running = true;

    public Connection(Socket socket) {
        this.socket = socket;
        receiver = new Thread() {
            @Override
            public void run() {
                try {
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    byte input[] = new byte[1024];
                    int size;
                    while (!isInterrupted()) {
                        size = in.readInt();
                        in.read(input, 0, size);
                        setChanged();
                        notifyObservers(new String(input));
                    }
                    setState(false);
                } catch (Exception e) {
                    setState(false);
                }
            }
        };
        receiver.start();
    }

    public Connection(String id, Socket socket) {
        this(socket);
        this.id = id;
    }

    public synchronized void write(Message msg) {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            byte data[] = msg.toString().getBytes();
            out.writeInt(data.length);
            out.write(data);
        } catch (IOException e) {
            setState(false);
        }
    }

    public synchronized void closeNoNotify() {
        try {
            receiver.interrupt();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void close() {
        setState(false);
        closeNoNotify();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private void setState(boolean newRunning) {
        if(running != newRunning) {
            running = newRunning;
            setChanged();
            notifyObservers(running);
        }
    }
}

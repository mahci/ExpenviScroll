package control;


import static tools.Consts.STRINGS.*;

import tools.Logs;
import tools.Memo;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    private final static String NAME = "Server/";
    //----------------------------------------------------------------------------------------
    private static Server instance; // Singelton

    private final int PORT = 8000; // always the same
    private final int CONNECTION_TIMEOUT = 60 * 1000; // 1 min

    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter outPW;
    private BufferedReader inBR;

    private ExecutorService executor;

    //----------------------------------------------------------------------------------------

    //-- Runnable for waiting for incoming connections
    private class ConnWaitRunnable implements Runnable {
        String TAG = NAME + "ConnWaitRunnable";

        @Override
        public void run() {
            try {
                Logs.d(TAG, "Waiting for connections...");
                if (serverSocket == null) serverSocket = new ServerSocket(PORT);
                socket = serverSocket.accept();

                // When reached here, Moose is connected
                Logs.d(TAG, "Moose connected!");

                // Create streams
                inBR = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outPW = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);

                // Start receiving
                executor.execute(new InRunnable());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //-- Runnable for sending messages to Moose
    private class OutRunnable implements Runnable {
        String TAG = NAME + "OutRunnable";
        Memo message;

        public OutRunnable(Memo mssg) {
            message = mssg;
        }

        @Override
        public void run() {
            if (message != null && outPW != null) {
                outPW.println(message);
                outPW.flush();
                Logs.d(TAG, message.toString());
            }
        }
    }

    //-- Runnable for receiving messages from Moose
    private class InRunnable implements Runnable {
        String TAG = NAME + "InRunnable";
        String mssg;

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted() && inBR != null) {
                try {
                    Logs.d(TAG, "Reading messages...");
                    if ((mssg = inBR.readLine()) != null) {
                        Memo memo = Memo.valueOf(mssg);
                        Logs.d(TAG, "Action: " + memo.getAction());
                        switch (memo.getAction()) {
                            case SCROLL -> {
                                Controller.get().scroll(memo);
                            }
                            case CONNECTION -> {
                                if (memo.getMode().equals(KEEP_ALIVE)) {
                                    Logs.d(TAG, "KA Received: " + memo);
                                    send(memo); // Send back the message (as confimation)
                                }
                            }
                        }

                    } else {
                        Logs.d(TAG, "Moose disconnected.");
                        Thread.currentThread().interrupt();
                        openConnection();
                        return;
                    }
                } catch (IOException e) {
                    System.out.println("Error in reading from Moose");
                    // Reconnect
                    Thread.currentThread().interrupt();
                    openConnection();
//                    e.printStackTrace();
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------

    /**
     * Get the instance
     * @return single instance
     */
    public static Server get() {
        if (instance == null) instance = new Server();
        return instance;
    }

    /**
     * Constructor
     */
    public Server() {
        String TAG = NAME;

        // Init executerService for running threads
        executor = Executors.newCachedThreadPool();
    }

    /**
     * Start receving connections
     */
    public void openConnection() {
        executor.execute(new ConnWaitRunnable());
    }

    public void send(Memo mssg) {
        executor.execute(new OutRunnable(mssg));
    }

}

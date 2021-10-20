package control;

import tools.Logs;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    private final static String cName = "Server--";
    //----------------------------------------------------------------------------------------
    private static Server instance; // Singelton

    private final int PORT = 8000; // always the same
    private final int CONNECTION_TIMEOUT = 60 * 1000; // 1 min

    private ServerSocket socket;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    //----------------------------------------------------------------------------------------
    private Callable<String> waitForConnectionCallable = new Callable<String>() {
        String TAG = cName + "waitForConnectionCallable";

        @Override
        public String call() throws IOException {
            Logs.info(TAG, "Opening socket...");
            if (socket == null) socket = new ServerSocket(PORT);
            socket.setSoTimeout(CONNECTION_TIMEOUT);
            Logs.info(TAG, "Socket opened, waiting for the Moose...");

            try {
                Socket inSocket = socket.accept();
                // When reached here, Moose is connected
                Logs.info(TAG, "Moose connected!");
                return "CONNECTED";
            } catch (SocketTimeoutException ste) {
                return "TIMEOUT";
            }

        }
    };


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
        String TAG = cName;

        Logs.info(TAG, findServerIP());
    }

    /**
     * Start the server
     */
    public void start() {
        String TAG = cName + "start";

        try {
            Future<String> connectionFuture = executorService.submit(waitForConnectionCallable);
            String connectionResult = connectionFuture.get();

            // Moose connected
            if (connectionResult == "SUCCESS") {

            }

            // Timeout in accepting connections
            if (connectionResult == "TIMEOUT") {

            }

        } catch (ExecutionException | InterruptedException e) {
            Logs.error(TAG, "No connection recieved. Starting again...");
            start();
//            e.printStackTrace();
        }
    }

    /**
     * Find what is the IP of this computer
     * @return (String) IP
     */
    public String findServerIP(){
        String mName = cName + "findServerIP";

        try {
            String address = InetAddress.getLocalHost().toString();
            return address.split("/")[1];
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return "Not found";
    }
}

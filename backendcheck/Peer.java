import java.io.*;
import java.net.*;
import java.util.*;

public class Peer {
    
    private static ServerSocket peerServerSocket;
    private static int myListeningPort;
    
    public static void main(String[] args) {
        String serverAddress = "localhost";  // Server IP address
        int serverPort = 12345;  // Server port for peer discovery
        
        try {
            // Step 0: Start our own server socket for P2P connections
            peerServerSocket = new ServerSocket(0); // 0 means any available port
            myListeningPort = peerServerSocket.getLocalPort();
            System.out.println("Started peer server on port: " + myListeningPort);
            
            // Start a thread to handle incoming P2P connections
            new Thread(() -> {
                try {
                    while (true) {
                        Socket incomingPeer = peerServerSocket.accept();
                        System.out.println("Incoming P2P connection from: " + incomingPeer.getInetAddress());
                        
                        // Handle incoming message
                        BufferedReader reader = new BufferedReader(new InputStreamReader(incomingPeer.getInputStream()));
                        String message = reader.readLine();
                        System.out.println("Received from peer: " + message);
                        
                        // Send response
                        PrintWriter writer = new PrintWriter(incomingPeer.getOutputStream(), true);
                        writer.println("Hello back from Peer!");
                        
                        incomingPeer.close();
                    }
                } catch (IOException e) {
                    System.out.println("Peer server stopped.");
                }
            }).start();
            // Step 1: Connect to the server to register and get peer list
            Socket serverSocket = new Socket(serverAddress, serverPort);
            
            // Send our listening port to the server
            PrintWriter serverWriter = new PrintWriter(serverSocket.getOutputStream(), true);
            serverWriter.println("REGISTER:" + myListeningPort);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            String line;
            
            // Display the list of available peers
            List<String> peerList = new ArrayList<>();
            System.out.println("Connected to the server. Available peers:");
            
            // Read available peers until END marker
            while ((line = reader.readLine()) != null) {
                if (line.equals("END")) {
                    break; // Stop reading when END marker is received
                }
                if (line.contains("IP:")) {
                    peerList.add(line);
                    System.out.println(line);
                }
            }
            
            // Close server connection
            serverSocket.close();
            
            // Step 2: Ask the user to select a peer to connect to
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the number of the peer you want to connect to (1-" + peerList.size() + "): ");
            
            // Ensure the user input is taken correctly after the peers are listed
            int choice = scanner.nextInt();
            
            // Validate user input
            if (choice < 1 || choice > peerList.size()) {
                System.out.println("Invalid choice. Exiting.");
                return;
            }
            
            // Get the selected peer's IP and port
            String selectedPeer = peerList.get(choice - 1);
            // Parse format "1. IP: 127.0.0.1 Port: 50123"
            String[] parts = selectedPeer.split(" ");
            String peerIP = parts[2]; // IP address
            int peerPort = Integer.parseInt(parts[4]); // Port number
            
            // Step 3: Connect to the selected peer (P2P connection)
            Socket peerSocket = new Socket(peerIP, peerPort);
            System.out.println("Connected to Peer at " + peerIP + ":" + peerPort);
            
            // Perform network health checks for P2P connection
            NetworkProbe probe = new NetworkProbe();
            System.out.println("RTT to Peer: " + probe.checkRTT(peerIP) + " ms");
            System.out.println("Packet Loss Rate to Peer: " + probe.checkPacketLoss(peerIP, 10) + " %");
            System.out.println("Retransmission Rate to Peer: " + probe.checkRetransmissionRate(peerIP, 10) + " %");
            
            // Step 4: Data transfer between peers (send message to the connected peer)
            OutputStream outputStream = peerSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
            writer.println("Hello from Peer!");
            
            // Step 5: Receive data from the peer
            InputStream inputStream = peerSocket.getInputStream();
            BufferedReader peerReader = new BufferedReader(new InputStreamReader(inputStream));
            String receivedMessage = peerReader.readLine();
            System.out.println("Received from Peer: " + receivedMessage);
            
            // Close the connection
            peerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
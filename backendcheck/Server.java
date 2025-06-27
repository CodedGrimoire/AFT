import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    
    private static final int PORT = 12345;  // Server port for listening
    private static Set<PeerInfo> connectedPeers = new HashSet<>();
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started, waiting for peers to connect...");
            
            while (true) {
                // Accept connections from peers
                Socket peerSocket = serverSocket.accept();
                System.out.println("Peer connected: " + peerSocket.getInetAddress());
                
                // Create a new thread to handle the peer
                new PeerHandler(peerSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Inner class to handle each peer's connection
    static class PeerHandler extends Thread {
        private Socket peerSocket;
        
        public PeerHandler(Socket peerSocket) {
            this.peerSocket = peerSocket;
        }
        
        @Override
        public void run() {
            try {
                // Get peer's information (IP address, port, etc.)
                InetAddress peerAddress = peerSocket.getInetAddress();
                
                // Read registration message with actual listening port
                BufferedReader in = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
                String registrationMsg = in.readLine();
                
                int peerListeningPort;
                if (registrationMsg != null && registrationMsg.startsWith("REGISTER:")) {
                    peerListeningPort = Integer.parseInt(registrationMsg.split(":")[1]);
                } else {
                    peerListeningPort = 50000 + (int)(Math.random() * 10000); // fallback
                }
                
                PeerInfo peerInfo = new PeerInfo(peerAddress.getHostAddress(), peerListeningPort);
                
                // Add the peer to the connected list
                synchronized (connectedPeers) {
                    connectedPeers.add(peerInfo);
                }
                
                // Send the list of connected peers to the new peer
                PrintWriter out = new PrintWriter(peerSocket.getOutputStream(), true);
                synchronized (connectedPeers) {
                    int index = 1;
                    for (PeerInfo p : connectedPeers) {
                        out.println(index + ". " + p);
                        index++;
                    }
                }
                // Send END marker so client knows when to stop reading
                out.println("END");
                
                // Keep connection open briefly for any response
                String message = in.readLine();
                if (message != null && message.equals("CONNECT_TO_PEER")) {
                    System.out.println("Ready to connect Peer: " + peerSocket.getInetAddress());
                }
                
                peerSocket.close();
                
                // Note: Don't remove peer here - keep them in the list for other peers to connect to
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // PeerInfo class to store peer information
    static class PeerInfo {
        private String ip;
        private int port;
        
        public PeerInfo(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
        
        @Override
        public String toString() {
            return "IP: " + ip + " Port: " + port;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PeerInfo peerInfo = (PeerInfo) obj;
            return port == peerInfo.port && Objects.equals(ip, peerInfo.ip);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(ip, port);
        }
    }
}
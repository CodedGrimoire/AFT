import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NetworkProbe {

    // Method to check RTT (Round-Trip Time) using ping
    public static long checkRTT(String host) {
        long startTime = System.nanoTime();
        try {
            // Use the system ping command for accurate network reachability check
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            // For Windows
            if (os.contains("win")) {
                pb = new ProcessBuilder("ping", "-n", "1", host);
            }
            // For Mac/Linux
            else {
                pb = new ProcessBuilder("ping", "-c", "1", host);
            }

            Process process = pb.start();
            int exitCode = process.waitFor();  // Wait for the ping to finish
            if (exitCode != 0) {
                System.out.println("Host is not reachable.");
                return -1;
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error checking RTT: " + e.getMessage());
            return -1;
        }
        long endTime = System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(endTime - startTime);  // in milliseconds
    }

    // Method to check Packet Loss Rate (Simulation)
    public static double checkPacketLoss(String host, int numPackets) {
        int lostPackets = 0;
        for (int i = 0; i < numPackets; i++) {
            try {
                // Use the system ping command for each packet
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;
                
                if (os.contains("win")) {
                    pb = new ProcessBuilder("ping", "-n", "1", host);
                } else {
                    pb = new ProcessBuilder("ping", "-c", "1", host);
                }

                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    lostPackets++;
                }
            } catch (IOException | InterruptedException e) {
                lostPackets++;
            }
        }
        return (double) lostPackets / numPackets * 100;  // percentage of packet loss
    }

    public static long checkThroughput(String host, int fileSizeInKB) {
    long startTime = System.nanoTime();
    try {
        // Attempt to establish the connection to the host
        URL url = new URL("http://" + host);  // Use the host with HTTP
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(2000);  // 2-second timeout for connection
        connection.connect();

        InputStream in = connection.getInputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        int totalBytes = 0;
        while ((bytesRead = in.read(buffer)) != -1) {
            totalBytes += bytesRead;  // Sum up all bytes read
        }
        in.close();
        connection.disconnect();

        // Calculate elapsed time
        long endTime = System.nanoTime();
        long elapsedTime = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);  // in milliseconds

        // If elapsed time is zero, the operation did not complete successfully
        if (elapsedTime == 0) {
            System.out.println("Error: Elapsed time is zero, the download did not complete successfully.");
            return -1;
        }

        // Check if the total downloaded size is smaller than expected, indicating failure
        if (totalBytes < fileSizeInKB * 1024) {
            System.out.println("Error: The file download was incomplete. Total bytes: " + totalBytes);
            return -1;
        }

        // Calculate throughput in KB/s
        return (totalBytes / 1024) / (elapsedTime / 1000);  // Throughput in KB/s
    } catch (IOException e) {
        System.out.println("Error measuring throughput: " + e.getMessage());
        return -1;
    }
}


    // Method to check Congestion Window Size (Simulated via Packet Size)
    public static String checkCWS(String host) {
        // Simulate congestion window by monitoring data flow
        long initialThroughput = checkThroughput(host, 500);
        if (initialThroughput == -1) {
            return "Error in Throughput Calculation, skipping CWS check.";
        }
        if (initialThroughput < 100) {
            return "Congestion Detected - Small Window Size";
        } else if (initialThroughput < 500) {
            return "Moderate Congestion - Adjusting Window Size";
        } else {
            return "Network Stable - Optimal Window Size";
        }
    }

    // Method to check Retransmission Rate (Simulation)
    public static double checkRetransmissionRate(String host, int numPackets) {
        int retransmissions = 0;
        for (int i = 0; i < numPackets; i++) {
            try {
                // Use the system ping command to simulate retransmission detection
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;
                
                if (os.contains("win")) {
                    pb = new ProcessBuilder("ping", "-n", "1", host);
                } else {
                    pb = new ProcessBuilder("ping", "-c", "1", host);
                }

                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    retransmissions++;
                }
            } catch (IOException | InterruptedException e) {
                retransmissions++;
            }
        }
        return (double) retransmissions / numPackets * 100;
    }

    // Method to check Latency Variability (Jitter)
    public static double checkLatencyJitter(String host, int numMeasurements) {
        List<Long> latencies = new ArrayList<>();
        for (int i = 0; i < numMeasurements; i++) {
            long latency = checkRTT(host);
            if (latency != -1) {
                latencies.add(latency);
            }
        }
        
        if (latencies.isEmpty()) {
            return -1;  // No successful measurements
        }
        
        long sum = 0;
        for (Long latency : latencies) {
            sum += latency;
        }
        long average = sum / latencies.size();
        
        long varianceSum = 0;
        for (Long latency : latencies) {
            varianceSum += Math.pow(latency - average, 2);
        }
        double jitter = Math.sqrt(varianceSum / latencies.size());
        return jitter;
    }

    // Method to check Connection Stability
    public static boolean checkConnectionStability(String host) {
        try {
            // Use the system ping command to check if the connection is stable
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                pb = new ProcessBuilder("ping", "-n", "1", host);
            } else {
                pb = new ProcessBuilder("ping", "-c", "1", host);
            }

            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    // Method to check Bandwidth Utilization
    public static double checkBandwidthUtilization(String host) {
        long startTime = System.nanoTime();
        long fileSize = 500;  // in KB
        long throughput = checkThroughput(host, (int) fileSize);
        if (throughput == -1) {
            return -1;  // Skip bandwidth utilization if throughput calculation failed
        }
        long elapsedTime = System.nanoTime() - startTime;
        double bandwidthUtilization = (double) throughput / (fileSize / TimeUnit.NANOSECONDS.toSeconds(elapsedTime)) * 100;
        return bandwidthUtilization;
    }

    // Method to check Error Rate (Simulated checksum errors)
    public static double checkErrorRate(String host, int numPackets) {
        int errorRate = 0;
        for (int i = 0; i < numPackets; i++) {
            try {
                // Use the system ping command to check for errors
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;
                
                if (os.contains("win")) {
                    pb = new ProcessBuilder("ping", "-n", "1", host);
                } else {
                    pb = new ProcessBuilder("ping", "-c", "1", host);
                }

                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    errorRate++;
                }
            } catch (IOException | InterruptedException e) {
                errorRate++;
            }
        }
        return (double) errorRate / numPackets * 100;
    }

    // Method to check Connection Setup and Teardown Time
    public static long checkConnectionTime(String host) {
        long startTime = System.nanoTime();
        try {
            InetAddress address = InetAddress.getByName(host);
            address.isReachable(2000);  // 2 seconds timeout
        } catch (IOException e) {
            return -1;  // Error in connection
        }
        long endTime = System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(endTime - startTime);  // in milliseconds
    }
}

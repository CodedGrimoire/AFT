public class Main {

    public static void main(String[] args) {
        String host = "www.google.com";  // This is Google's public DNS server
        
        // Initialize NetworkProbe instance
        NetworkProbe probe = new NetworkProbe();
        
        // Check RTT
        long rtt = probe.checkRTT(host);
        System.out.println("RTT: " + rtt + " ms");
        
        // Check Packet Loss Rate
        double packetLoss = probe.checkPacketLoss(host, 10);
        System.out.println("Packet Loss Rate: " + packetLoss + " %");
        
        // Check Throughput
        long throughput = probe.checkThroughput(host, 500);
        System.out.println("Throughput: " + throughput + " KB/s");
        
        // Check Congestion Window Size
        String cws = probe.checkCWS(host);
        System.out.println("Congestion Window Size: " + cws);
        
        // Check Retransmission Rate
        double retransmissionRate = probe.checkRetransmissionRate(host, 10);
        System.out.println("Retransmission Rate: " + retransmissionRate + " %");
        
        // Check Latency Variability (Jitter)
        double jitter = probe.checkLatencyJitter(host, 5);
        System.out.println("Latency Jitter: " + jitter + " ms");
        
        // Check Connection Stability
        boolean stableConnection = probe.checkConnectionStability(host);
        System.out.println("Connection Stability: " + (stableConnection ? "Stable" : "Unstable"));
        
        // Check Bandwidth Utilization
        double bandwidthUtilization = probe.checkBandwidthUtilization(host);
        System.out.println("Bandwidth Utilization: " + bandwidthUtilization + " %");
        
        // Check Error Rate
        double errorRate = probe.checkErrorRate(host, 10);
        System.out.println("Error Rate: " + errorRate + " %");
        
        // Check Connection Setup Time
        long connectionTime = probe.checkConnectionTime(host);
        System.out.println("Connection Setup Time: " + connectionTime + " ms");
    }
}

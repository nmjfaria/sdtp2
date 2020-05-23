package sd1920.trab2;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * <p>A class to perform service discovery, based on periodic service contact endpoint
 * announcements over multicast communication.</p>
 *
 * <p>Servers announce their *name* and contact *uri* at regular intervals. The server actively
 * collects received announcements.</p>
 *
 * <p>Service announcements have the following format:</p>
 *
 * <p>&lt;service-name-string&gt;&lt;delimiter-char&gt;&lt;service-uri-string&gt;</p>
 */
public class Discovery {
    private static Logger Log = Logger.getLogger(Discovery.class.getName());

    static {
        // addresses some multicast issues on some TCP/IP stacks
        System.setProperty("java.net.preferIPv4Stack", "true");
        // summarizes the logging format
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
    }

    // The pre-agreed multicast endpoint assigned to perform discovery.
    static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);

    static final int DISCOVERY_PERIOD = 1000;
    static final int DISCOVERY_TIMEOUT = 5000;

    // Used separate the two fields that make up a service announcement.
    private static final String DELIMITER = "\t";

    private static final Map<String, Map<URI, Long>> knownURIs = new ConcurrentHashMap<>();

    //Make sure we only start each service once
    private static AtomicBoolean startedAnnounce = new AtomicBoolean(false);
    private static AtomicBoolean startedDiscovery = new AtomicBoolean(false);

    private Discovery(){};

    public static void startAnnounce(String serviceName, URI serviceURI) {

        boolean andSet = startedAnnounce.getAndSet(true);
        if (andSet) {
            Log.warning("Attempted to start Announce service twice");
            return;
        }

        Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s",
                DISCOVERY_ADDR, serviceName, serviceURI));

        byte[] announceBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();
        DatagramPacket announcePkt = new DatagramPacket(announceBytes, announceBytes.length, DISCOVERY_ADDR);

        try {
            DatagramSocket ms = new DatagramSocket();
            // start thread to send periodic announcements
            new Thread(() -> {
                System.out.println("Announcement started");
                for (; ; ) {
                    try {
                        ms.send(announcePkt);
                        Thread.sleep(DISCOVERY_PERIOD);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startDiscovery() {
        boolean andSet = startedDiscovery.getAndSet(true);
        if (andSet) {
            Log.warning("Attempted to start Discovery service twice");
            return;
        }

        try {
            MulticastSocket ms = new MulticastSocket(DISCOVERY_ADDR.getPort());
            ms.joinGroup(DISCOVERY_ADDR.getAddress());
            new Thread(() -> {
                System.out.println("Discovery started");
                DatagramPacket pkt = new DatagramPacket(new byte[1024], 1024);
                for (; ; ) {
                    try {
                        pkt.setLength(1024);
                        ms.setSoTimeout(1000);
                        try {
                            ms.receive(pkt);
                            String msg = new String(pkt.getData(), 0, pkt.getLength());
                            String[] msgElements = msg.split(DELIMITER);
                            if (msgElements.length == 2) {    //periodic announcement
                                long currentTime = System.currentTimeMillis();
                                knownURIs.computeIfAbsent(msgElements[0], k -> new ConcurrentHashMap<>())
                                        .put(URI.create(msgElements[1]), currentTime);
                            }
                        } catch (SocketTimeoutException ignored) {
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static URI[] knownUrisOf(String serviceName, int intervalMillis, int nTries) {
        int attempt = 0;
        URI[] result = new URI[0];
        while (attempt < nTries && result.length == 0) {

            result = knownURIs.getOrDefault(serviceName, Collections.emptyMap()).keySet().toArray(new URI[0]);
            attempt++;

            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException ignored) {
            }
        }
        return result;
    }

    public static URI[] knownUrisOf(String serviceName) {
        return knownUrisOf(serviceName, 0, 1);
    }
}

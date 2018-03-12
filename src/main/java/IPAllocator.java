import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Tanvi on 12/03/18.
 */
public class IPAllocator {
    private static int levels = 2;
    private static int addressesPerLevel = 256;
    private static int totalIPAddresses = (int) Math.pow(addressesPerLevel, levels);
    private static String[] allotedIPAddresses = new String[totalIPAddresses];
    private static long[] allotedTimestamp = new long[totalIPAddresses];
    private static Map<String, String> macVsIP = new ConcurrentHashMap<String, String>();
    private static long expiryMs = 2 * 60 * 60 * 1000;

    public static long getAllotedTimestamp(int index) {
        return allotedTimestamp[index];
    }

    public static void setAllotedTimestamp(int index, long allotedTimestamp) {
        IPAllocator.allotedTimestamp[index] = allotedTimestamp;
    }

    public static String getAllotedIPAddresses(int index) {
        return allotedIPAddresses[index];
    }

    public static void setAllotedIPAddress(int index, String allotedIPAddress) {
        IPAllocator.allotedIPAddresses[index] = allotedIPAddress;
    }

    public static long getExpiryMs() {
        return expiryMs;
    }

    public static int getTotalIPAddresses() {
        return totalIPAddresses;
    }

    protected static int calculateIndexForIP(String ipAddress) {
        String[] add = ipAddress.split(".");
        int pow = 0;
        int index = 0;
        for (int i = add.length - 1; i > -1; i++) {
            index += Integer.parseInt(add[i]) * Math.pow(256, pow);
            pow += 1;
        }
        return index;
    }

    protected static String calculateIPForIndex(int index) {
        String ip = "";
        for (int i = 0; i < levels; i++) {
            ip = Integer.toString(index % 256) + "." + ip;
            index = index / 256;
        }
        return ip.substring(0, ip.length() - 1);
    }

    protected static boolean isExpired(String ipAddress) {
        return !isIndexFree(calculateIndexForIP(ipAddress));
    }

    private static boolean isIndexFree(int index) {
        return (IPAllocator.getAllotedTimestamp(index) <= 0 ||
                 System.currentTimeMillis() - IPAllocator.getAllotedTimestamp(index) > IPAllocator.getExpiryMs());
    }

    protected static String getIPForMac(String macAddress) {
        String ip = null;
        if (macVsIP.containsKey(macAddress)) {
            ip = macVsIP.get(macAddress);
        }
        return ip;

    }

    protected static String allocateNewIP(String macAddress) {
        String ip = null;
        int index = macAddress.hashCode() % IPAllocator.getTotalIPAddresses();
        //check if this index is free
        //if yes = allocate
        //if no = loop through the array
        if (isIndexFree(index))
        {
            setAllotedIPAddress(index, macAddress);
            setAllotedTimestamp(index, System.currentTimeMillis());
            ip = calculateIPForIndex(index);
            macVsIP.put(macAddress, ip);
        }
        else {
            int nIndex = (index + 1) < IPAllocator.getTotalIPAddresses() ? index + 1 : 0;
            while (index != nIndex) {
                if (isIndexFree(nIndex))
                {
                    IPAllocator.setAllotedIPAddress(nIndex, macAddress);
                    IPAllocator.setAllotedTimestamp(nIndex, System.currentTimeMillis());
                    ip = IPAllocator.calculateIPForIndex(nIndex);
                    macVsIP.put(macAddress, ip);
                    break;
                }
                nIndex += 1;
            }
        }
        return ip;
    }

    protected static String allocateOrRefresh(String macAddress) {
        String ip = IPAllocator.getIPForMac(macAddress);
        //if ip alloted
        if (ip != null) {
            //if alloted is not expired, update the timestamp
            if (IPAllocator.isExpired(ip)) {
                IPAllocator.setAllotedTimestamp(IPAllocator.calculateIndexForIP(ip), System.currentTimeMillis());
            }
            else {
                //allot new ip
                IPAllocator.allocateNewIP(macAddress);
            }
        }
        else {
            //check if ips are full - allocate a new ip
            if (!IPAllocator.allIPsAllocated()) {
                IPAllocator.allocateNewIP(macAddress);
            }
        }
        return ip;
    }

    protected static boolean allIPsAllocated() {
        return macVsIP.size() >= totalIPAddresses;
    }
}

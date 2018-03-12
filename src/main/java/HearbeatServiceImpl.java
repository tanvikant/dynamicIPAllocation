/**
 * Created by Tanvi on 12/03/18.
 */
public class HearbeatServiceImpl implements HeartbeatService {
    public String refresh(String macAddress, String allocatedIPAddress) {
        return IPAllocator.allocateOrRefresh(macAddress);
    }
}

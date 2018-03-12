/**
 * Created by Tanvi on 12/03/18.
 */
public class AllocationServiceImpl implements AllocationService {

    public String allocate(String macAddress) {
        return IPAllocator.allocateOrRefresh(macAddress);
    }

}

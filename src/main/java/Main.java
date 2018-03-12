import java.util.Scanner;

/**
 * Created by Tanvi on 12/03/18.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AllocationService allocationService = new AllocationServiceImpl();
        System.out.println(allocationService.allocate("00-14-22-01-23-45"));
    }
}

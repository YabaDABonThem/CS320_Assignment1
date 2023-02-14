import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception {
        RouteFinder rf = new RouteFinder();

        Scanner console = new Scanner(System.in);
        // System.out.println("Please enter a letter that your destinations start with: ");
        // char destinationInitial = console.nextLine().charAt(0);

        System.out.println(rf.getBusRoutesUrls('B'));

        Map<String, String> brier = Map.of("532/535", "https://www.communitytransit.org/busservice/schedules/route/532-535");
        Map<String, String> brier2 = Map.of("111", "https://www.communitytransit.org/busservice/schedules/route/111");
        // Take in the user's input as the key to the amd pass the value into the next function.
        System.out.println(rf.getBusRouteTripsLengthsInMinutesToAndFromDestination(brier));
        System.out.println(rf.getBusRouteTripsLengthsInMinutesToAndFromDestination(brier2));
    }
}

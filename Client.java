import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception {
        RouteFinder rf = new RouteFinder();

        Scanner console = new Scanner(System.in);
        // System.out.println("Please enter a letter that your destinations start with: ");
        // char destinationInitial = console.nextLine().charAt(0);

        rf.getBusRoutesUrls('B');

        // Take in the user's input as the key to the amd pass the value into the next function.
        // rf.getBusRouteTripsLengthsInMinutesToAndFromDestination();
    }
}

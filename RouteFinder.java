import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RouteFinder implements IRouteFinder{
    public RouteFinder() {

    }

    private String getWebPageSource(String URL) {
        try {
            String webPageSource = "";
            // defualt URL: "https://www.communitytransit.org/busservice/schedules/"
            URLConnection tc = new URL(URL).openConnection();
            tc.setRequestProperty("user-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

            BufferedReader in = new BufferedReader(new InputStreamReader(tc.getInputStream()));

            String inputLine = "";


            while ((inputLine = in.readLine()) != null) {
                webPageSource += inputLine + " ";

            }

            in.close();
            return webPageSource;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Map<String, String>> getBusRoutesUrls(char destInitial) {
        // Location, Number, URL
        String URL = "https://www.communitytransit.org/busservice/schedules/";
        Map<String, Map<String, String>> stationRouteMap = new HashMap<>();

        // Get all the locations that start with that initial letter
        // Get the URLs to those locations
        ArrayList<String> destinations = getDestinationsFromInitial('B', getWebPageSource(URL));

        for (String s : destinations) {
            stationRouteMap.put(s, getBusInfo(getTextForDestination(s, getWebPageSource(URL))));
        }


        return stationRouteMap;
    }

    // get a list of destinations from an initial letter
    private ArrayList<String> getDestinationsFromInitial(char initial, String text) {
        ArrayList<String> destinations = new ArrayList<>();

        Pattern initialPattern = Pattern.compile("\\Q<li class=\"col-sm-4 col-xs-6\"><a href=\"#\\E\\w*\">(" + initial + "\\w*)");
        Matcher initialMatcher = initialPattern.matcher(text);

        while (initialMatcher.find()) {
            // System.out.println("group 1: " + initialMatcher.group(1));
            destinations.add(initialMatcher.group(1));
        }

        return destinations;
    }

    // given a list of destinations go through them and get their info.
    private String getTextForDestination(String destination, String text) {

        // NOTE: Last one will not match (Tulalip)
        Pattern stationPattern = Pattern.compile("(<h3>(" + destination + ")</h3>.*?)<hr");
        Matcher stationMatcher = stationPattern.matcher(text);

        if (stationMatcher.find()) {
            System.out.println(stationMatcher.group(2));

            return stationMatcher.group(1);
        } else {
            return null;
        }
    }

    // given a destination get a list of their bus numbers
    private Map<String, String> getBusInfo(String textForDestination) {
        // Note that we have to return a list of strings because some bus "numbers" are not ints
        // Bus Number, URL
        HashMap<String, String> busNumbers = new HashMap<>();
        // <strong><a href="/schedules/route/532-535"  class=text-success>532/535</a></strong>
        // Pattern routePattern = Pattern.compile("<strong><a href=\"/schedules/route/\\w*\" ( class=text-success)?>([\\w/]*)</a></strong>");
        Pattern routePattern = Pattern.compile("<strong><a href=\"/schedules/(route/[\\w-]*)\" ( class=text-success)?>([\\w /]*)</a></strong>");
        Matcher routeMatcher = routePattern.matcher(textForDestination);

        String schedulesURL = "https://www.communitytransit.org/busservice/schedules/";

        while (routeMatcher.find()) {
            System.out.println("Bus Number: " + routeMatcher.group(3));
            System.out.println("URL: " + schedulesURL + routeMatcher.group(1));
            busNumbers.put(routeMatcher.group(2), schedulesURL + routeMatcher.group(1)); // Note that this doesn't put in the first half of the URL yet
        }

        return busNumbers;
    }


    private Map<String, String> getDestinationBusesMap(Map<String, Map<String, String>> busRouteURLs) {
        // should we process one location, or should we process all of them?

    }

    @Override
    public Map<String, List<Long>> getBusRouteTripsLengthsInMinutesToAndFromDestination(Map<String, String> destinationBusesMap) {
        // First item should be the list and Bus Number, and the second item should be a list of times.

        Map<String, List<Long>> busRoute

        return null;
    }
}

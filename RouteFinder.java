import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
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
        String mainURL = "https://www.communitytransit.org/busservice/schedules/";
        Map<String, Map<String, String>> stationRouteMap = new HashMap<>();

        // Get all the locations that start with that initial letter
        // Get the URLs to those locations
        ArrayList<String> destinations = getDestinationsFromInitial('B', getWebPageSource(mainURL));

        for (String s : destinations) {
            // Format of items inside the hashmap: Destination, <Bus Number, URL>
            // example:
            //    {
            //       'Bothell': {
                    //    '105': 'https://www.communitytransit.org/busservice/schedules/route/105',
                    //    <106: https://www.communitytransit.org/busservice/schedules/route/106>
            //       },
            // j    'Bellevue' : {},
            //  }
            stationRouteMap.put(s, getBusInfo(getTextForDestination(s, getWebPageSource(mainURL))));
        }
        //getTextForStationRoute(stationRouteMap.get("Bellevue").get("532/535"));
        // getTravelTime("https://www.communitytransit.org/busservice/schedules/route/532-535");

        return stationRouteMap;
    }

    // get a list of destinations from an initial letter
    private ArrayList<String> getDestinationsFromInitial(char initial, String text) {
        ArrayList<String> destinations = new ArrayList<>();

        Pattern initialPattern = Pattern.compile("\\Q<li class=\"col-sm-4 col-xs-6\"><a href=\"#\\E\\w*\">(" + initial + "\\w*)");
        Matcher initialMatcher = initialPattern.matcher(text);

        while (initialMatcher.find()) {
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

            return stationMatcher.group(1);
        } else {
            return null;
        }
    }

    private String getTextForStationRoute (String URL) {

        Pattern stationPattern = Pattern.compile("<h2>Weekday<small>To Bellevue Transit Center</small></h2>.*</table>");
        String webPageSource = getWebPageSource(URL);
        Matcher stationMatcher = stationPattern.matcher(webPageSource);

        if (stationMatcher.find()) {


            return stationMatcher.group(0);
        } else {
            return null;
        }
    }

    // given a destination get a Map containing their bus numbers AND URLs
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
            busNumbers.put(routeMatcher.group(3), schedulesURL + routeMatcher.group(1)); // Note that this doesn't put in the first half of the URL yet
        }

        return busNumbers;
    }


    private long getTimeDifference (int startH, int startM, int endH, int endM) {
        if (endM < startM) {
            endM += 60;
            endH -= 1;
        }
        int minDiff = endM - startM;
        if (endH < startH) {
            endH += 12;
        }
        return (endH - startH) * 60L + minDiff;

    }


    private ArrayList<Long> getTravelTime(String busRouteURL) {
        // This gets the bus route number, destination name, as well as all travel times for one URL.
        ArrayList<Long> travelTimeList = new ArrayList<>();

        Pattern stationPattern = Pattern.compile("<h2>Weekday<small>.*?</small></h2>.*?<tbody>(.*?)id=\"Saturday");
        String allSource = getWebPageSource(busRouteURL);
        Matcher stationMatcher = stationPattern.matcher(getWebPageSource(busRouteURL));

        String routeScheduleSource; // Split this into each category
        if (!stationMatcher.find()) {
            routeScheduleSource = allSource;
        } else {
            routeScheduleSource = stationMatcher.group(0); // Split this into each category

        }
        Pattern stationGroupPattern = Pattern.compile(".*?<td class=\"text-center\">.*?(.*?)</tr>");
        Matcher stationGroupMatcher = stationGroupPattern.matcher(routeScheduleSource);

        // CHECK FOR THOSE BUSES WITHOUT SCHEDULES
        Matcher specialRouteDetector = stationGroupPattern.matcher(routeScheduleSource);
        if (!specialRouteDetector.find()) {
            ArrayList<Long> times = new ArrayList<>();
            times.add(12L);
            return times;
        }

        while (stationGroupMatcher.find()) {
            Pattern travelTimePattern = Pattern.compile("(\\d+):(\\d+)");
            Matcher travelTimeMatcher = travelTimePattern.matcher(stationGroupMatcher.group(1));
            travelTimeMatcher.find();
            String startHour = travelTimeMatcher.group(1);
            String startMin = travelTimeMatcher.group(2);
            String lastHour = "";
            String lastMin = "";
            while (travelTimeMatcher.find()) {
                lastHour = travelTimeMatcher.group(1);
                lastMin = travelTimeMatcher.group(2);
            }
            travelTimeList.add(getTimeDifference(Integer.parseInt(startHour), Integer.parseInt(startMin),
                    Integer.parseInt(lastHour), Integer.parseInt(lastMin)));

        }


        return travelTimeList;
    }

    @Override                                                                         // BUS NUMBER, BUS ROUTE URL
    public Map<String, List<Long>> getBusRouteTripsLengthsInMinutesToAndFromDestination(Map<String, String> destinationBusesMap) {
        // For the returned map: first item should be the list and Bus Number, and the second item should be a list of times.
        Map<String, List<Long>> allTripLengths = new HashMap<>();

        // iterate through the map and use the Bus Number/URL
        Iterator destinationBusesMapIterator = destinationBusesMap.entrySet().iterator();

        while (destinationBusesMapIterator.hasNext()) {
            Map.Entry busRoute = (Map.Entry)destinationBusesMapIterator.next();

            // Now we have extracted the bus number and URL.
            // We need to get the value of busRoute and get the name, as well as the times.

            Pattern routePattern = Pattern.compile("<h2>Weekday<small>To (.*?)</small></h2>");
            Matcher routeMatcher = routePattern.matcher(getWebPageSource(busRoute.getValue().toString()));

            // idk why there's a while loop, there should only be one at most to match
            while (routeMatcher.find()) {
                String locationName = routeMatcher.group(1);

                String routeName = busRoute.getKey().toString()+ " - " + locationName;

                // We have the name of the route, now we need the times.
                ArrayList<Long> times = getTravelTime(busRoute.getValue().toString());
                allTripLengths.put(routeName, times);
            }


        }
        return allTripLengths;
        //String text = getWebPageSource(destinationBusesMap.get()); // What should the key be? should we just iterate through the map?
        //HashMap<String, List<Long>> BusRouteTripsLengthsInMinutesToAndFromDestination = new HashMap<>();

    }
}

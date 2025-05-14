import java.util.*;

public class RouteFinder {

    // Represents a road connection between two locations
    static class Connection {
        int destination;
        double distance;
        double time;
        double traffic;

        public Connection(int destination, double distance, double time, double traffic) {
            this.destination = destination;
            this.distance = distance;
            this.time = time;
            this.traffic = traffic;
        }
    }

    // Stores the final route details
    static class RouteResult {
        List<String> path;
        double totalDistance;
        double totalTime;
        double totalTraffic;
        double cost;

        public RouteResult(List<String> path, double totalDistance, double totalTime, double totalTraffic, double cost) {
            this.path = path;
            this.totalDistance = totalDistance;
            this.totalTime = totalTime;
            this.totalTraffic = totalTraffic;
            this.cost = cost;
        }

        public String toString() {
            return "Route: " + String.join(" -> ", path) +
                   "\nDistance: " + totalDistance + " km" +
                   "\nTime: " + totalTime + " min" +
                   "\nTraffic: " + totalTraffic +
                   "\nCost: " + cost;
        }
    }

    // Represents the map and handles routing
    static class RoadNetwork {
        int n;
        String[] names;
        List[] map; 

        public RoadNetwork(int n) {
            this.n = n;
            names = new String[n];
            map = new List[n];
            for (int i = 0; i < n; i++) {
                map[i] = new ArrayList(); // initialize each list
            }
        }
        public void setNames(String[] names) {
            this.names = names;
        }

        // Add a bidirectional road between two locations
        public void addConnection(int from, int to, double distance, double time, double traffic) {
            map[from].add(new Connection(to, distance, time, traffic));
            map[to].add(new Connection(from, distance, time, traffic));
        }

        // Uses Dijkstra’s algorithm with weighted criteria to find the best route
        public RouteResult findOptimalRoute(int start, int end, double alpha, double beta, double gamma) {
            double[] cost = new double[n]; // total cost to reach each location
            boolean[] visited = new boolean[n];
            int[] prev = new int[n]; // to reconstruct the path
            Arrays.fill(cost, Double.MAX_VALUE);
            Arrays.fill(prev, -1);
            cost[start] = 0;
            for (int count = 0; count < n; count++) {
                int curr = -1;
                for (int i = 0; i < n; i++) {
                    if (!visited[i] && (curr == -1 || cost[i] < cost[curr])) {
                        curr = i;
                    }
                }
                if (curr == -1 || cost[curr] == Double.MAX_VALUE) break; // no reachable nodes left
                visited[curr] = true;

                // Check all neighbors of current node
                for (Object obj : map[curr]) {
                    Connection c = (Connection) obj;
                    int next = c.destination;
                    if (visited[next]) continue;

                    // Calculate cost using weights (alpha = distance, beta = time, gamma = traffic)
                    double edgeCost = alpha * c.distance + beta * c.time + gamma * c.traffic;

                    if (cost[curr] + edgeCost < cost[next]) {
                        cost[next] = cost[curr] + edgeCost;
                        prev[next] = curr;
                    }
                }
            }

            if (cost[end] == Double.MAX_VALUE) return null; // no path found

            // Reconstruct path
            List<Integer> pathIds = new ArrayList<>();
            for (int at = end; at != -1; at = prev[at]) pathIds.add(at);
            Collections.reverse(pathIds);

            List<String> pathNames = new ArrayList<>();
            double totalDistance = 0, totalTime = 0, totalTraffic = 0;

            // Collect path info and convert IDs to names
            for (int i = 0; i < pathIds.size() - 1; i++) {
                int from = pathIds.get(i), to = pathIds.get(i + 1);
                pathNames.add(names[from]);
                for (Object obj : map[from]) {
                    Connection c = (Connection) obj;
                    if (c.destination == to) {
                        totalDistance += c.distance;
                        totalTime += c.time;
                        totalTraffic += c.traffic;
                        break;
                    }
                }
            }
            pathNames.add(names[end]);

            return new RouteResult(pathNames, totalDistance, totalTime, totalTraffic, cost[end]);
        }
    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of locations: ");
        int n = sc.nextInt();
        sc.nextLine();

        RoadNetwork rn = new RoadNetwork(n);
        String[] names = new String[n];

        // Get location names
        for (int i = 0; i < n; i++) {
            System.out.print("Enter name for location " + i + ": ");
            names[i] = sc.nextLine();
        }
        rn.setNames(names);

        // Input connections
        while (true) {
            System.out.print("Enter from ID (-1 to stop): ");
            int from = sc.nextInt();
            if (from == -1) break;
            System.out.print("Enter to ID: ");
            int to = sc.nextInt();
            System.out.print("Enter distance (km): ");
            double dist = sc.nextDouble();
            System.out.print("Enter time (min): ");
            double time = sc.nextDouble();
            System.out.print("Enter traffic (1-5): ");
            double traffic = sc.nextDouble();
            rn.addConnection(from, to, dist, time, traffic);
        }

        // Input weights
        System.out.print("Distance importance (0-10): ");
        double alpha = sc.nextDouble() / 10.0; // distance weight
        System.out.print("Time importance (0-10): ");
        double beta = sc.nextDouble() / 10.0;  // time weight
        System.out.print("Traffic importance (0-10): ");
        double gamma = sc.nextDouble() / 10.0; // traffic weight

        // Input start and end locations
        System.out.print("Start location ID: ");
        int start = sc.nextInt();
        System.out.print("End location ID: ");
        int end = sc.nextInt();

        // Find and print the route
        RouteResult result = rn.findOptimalRoute(start, end, alpha, beta, gamma);
        if (result != null) {
            System.out.println("\n--- Best Route Found ---");
            System.out.println(result);
        } else {
            System.out.println("No route available between these locations.");
        }
    }
}

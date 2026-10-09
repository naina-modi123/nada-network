
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;

public class Main {
    private static final Map<Integer, String> people = new HashMap<>();
    private static int nextId = 1;
    private static final Map<Integer, java.util.Set<Integer>> connections = new HashMap<>();
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        server.createContext("/", exchange -> {
            String response = "Nada Network API is running!";
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (var output = exchange.getResponseBody()) {
                output.write(response.getBytes());
            }
        });
        
server.createContext("/people", exchange -> {
    if (!exchange.getRequestMethod().equals("POST")) {
        exchange.sendResponseHeaders(405, -1);
        exchange.close();
        return;
    }

   String body = new String(
        exchange.getRequestBody().readAllBytes(),
        java.nio.charset.StandardCharsets.UTF_8
);

String name;
try {
    if (!body.trim().startsWith("{")
            || !body.trim().endsWith("}")
            || !body.contains("\"name\"")) {
        throw new IllegalArgumentException();
    }

    name = body.replaceAll(".*\"name\"\\s*:\\s*\"", "")
            .replaceAll("\".*", "").trim();

    if (name.isBlank() || name.equals(body.trim())) {
        throw new IllegalArgumentException();
    }
} catch (IllegalArgumentException e) {
    byte[] error = "{\"error\":\"invalid JSON or name\"}"
            .getBytes(java.nio.charset.StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set(
            "Content-Type", "application/json; charset=utf-8");
    exchange.sendResponseHeaders(400, error.length);
    exchange.getResponseBody().write(error);
    exchange.close();
    return;
}
    if (name.isBlank() || name.equals(body.trim())) {
        byte[] error = "{\"error\":\"invalid name\"}".getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(400, error.length);
        exchange.getResponseBody().write(error);
        exchange.close();
        return;
    }

    int id = nextId++;
    people.put(id, name);

    String response = "{\"id\":" + id + ",\"name\":\"" + name + "\"}";
    byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);

    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
    exchange.sendResponseHeaders(201, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.close();
});
        
server.createContext("/knows", exchange -> {
    if (!exchange.getRequestMethod().equals("POST")) {
        exchange.sendResponseHeaders(405, -1);
        exchange.close();
        return;
    }

    String body = new String(exchange.getRequestBody().readAllBytes());

    try {
        java.util.regex.Matcher matcher =
            java.util.regex.Pattern.compile("\"a\"\\s*:\\s*(\\d+).*\"b\"\\s*:\\s*(\\d+)")
                .matcher(body);

        if (!matcher.find()) {
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
            return;
        }

        int a = Integer.parseInt(matcher.group(1));
        int b = Integer.parseInt(matcher.group(2));

        if (a == b || !people.containsKey(a) || !people.containsKey(b)) {
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
            return;
        }

        connections.computeIfAbsent(a, k -> new java.util.HashSet<>()).add(b);
        connections.computeIfAbsent(b, k -> new java.util.HashSet<>()).add(a);

        exchange.sendResponseHeaders(204, -1);
        exchange.close();

    } catch (Exception e) {
        exchange.sendResponseHeaders(400, -1);
        exchange.close();
    }
});

        
server.createContext("/people/", exchange -> {
    String path = exchange.getRequestURI().getPath();
    String[] parts = path.split("/");

    if (parts.length != 4 || !parts[3].equals("contacts")) {
        exchange.sendResponseHeaders(404, -1);
        exchange.close();
        return;
    }

    if (!exchange.getRequestMethod().equals("GET")) {
        exchange.sendResponseHeaders(405, -1);
        exchange.close();
        return;
    }

    try {
        int id = Integer.parseInt(parts[2]);

        if (!people.containsKey(id)) {
            byte[] error = ("{\"error\":\"no person " + id + "\"}")
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(404, error.length);
            exchange.getResponseBody().write(error);
            exchange.close();
            return;
        }

        java.util.Set<Integer> contactIds = connections.getOrDefault(
            id, java.util.Collections.emptySet()
        );

        StringBuilder response = new StringBuilder("[");
        boolean first = true;

        for (int contactId : contactIds) {
            if (!first) response.append(",");
            first = false;

            String name = people.get(contactId).replace("\\", "\\\\").replace("\"", "\\\"");
            response.append("{\"id\":").append(contactId)
                    .append(",\"name\":\"").append(name).append("\"}");
        }

        response.append("]");
        byte[] bytes = response.toString().getBytes(
            java.nio.charset.StandardCharsets.UTF_8
        );

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();

    } catch (NumberFormatException e) {
        exchange.sendResponseHeaders(400, -1);
        exchange.close();
    }
});
        
server.createContext("/path", exchange -> {
    if (!exchange.getRequestMethod().equals("GET")) {
        exchange.sendResponseHeaders(405, -1);
        exchange.close();
        return;
    }

    String query = exchange.getRequestURI().getRawQuery();
    if (query == null || !query.matches(".*(^|&)from=[0-9]+(&|$).*")
            || !query.matches(".*(^|&)to=[0-9]+(&|$).*")) {
        exchange.sendResponseHeaders(400, -1);
        exchange.close();
        return;
    }

    int from = -1, to = -1;
    for (String param : query.split("&")) {
        String[] pair = param.split("=", 2);
        if (pair.length == 2) {
            try {
                if (pair[0].equals("from")) from = Integer.parseInt(pair[1]);
                if (pair[0].equals("to")) to = Integer.parseInt(pair[1]);
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(400, -1);
                exchange.close();
                return;
            }
        }
    }

    if (!people.containsKey(from) || !people.containsKey(to)) {
        int missing = !people.containsKey(from) ? from : to;
        byte[] error = ("{\"error\":\"no person " + missing + "\"}")
            .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(404, error.length);
        exchange.getResponseBody().write(error);
        exchange.close();
        return;
    }

    java.util.Queue<Integer> queue = new java.util.ArrayDeque<>();
    java.util.Map<Integer, Integer> previous = new java.util.HashMap<>();
    java.util.Map<Integer, Integer> depth = new java.util.HashMap<>();

    queue.add(from);
    depth.put(from, 0);
    previous.put(from, -1);

    while (!queue.isEmpty()) {
        int current = queue.remove();

        if (current == to) break;
        if (depth.get(current) == 3) continue;

        for (int neighbor : connections.getOrDefault(
                current, java.util.Collections.emptySet())) {
            if (!depth.containsKey(neighbor)) {
                depth.put(neighbor, depth.get(current) + 1);
                previous.put(neighbor, current);
                queue.add(neighbor);
            }
        }
    }

    boolean connected = depth.containsKey(to) && depth.get(to) <= 3;
    String response;

    if (!connected) {
        response = "{\"connected\":false}";
    } else {
        java.util.List<Integer> path = new java.util.ArrayList<>();
        for (int at = to; at != -1; at = previous.get(at)) {
            path.add(at);
        }
        java.util.Collections.reverse(path);

        StringBuilder json = new StringBuilder();
        json.append("{\"connected\":true,\"hops\":")
            .append(path.size() - 1).append(",\"path\":[");

        for (int i = 0; i < path.size(); i++) {
            if (i > 0) json.append(",");
            int id = path.get(i);
            String name = people.get(id).replace("\\", "\\\\")
                                      .replace("\"", "\\\"");
            json.append("{\"id\":").append(id)
                .append(",\"name\":\"").append(name).append("\"}");
        }

        response = json.append("]}").toString();
    }

    byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
    exchange.sendResponseHeaders(200, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.close();
});

        server.start();
        System.out.println("Nada Network API running at http://localhost:8080");
    }
}


package tests;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PathTest {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL = "http://localhost:8080";

    private static String post(String endpoint, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static HttpResponse<String> get(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static int createPerson(String name) throws Exception {
        String response = post("/people", "{\"name\":\"" + name + "\"}");

        int start = response.indexOf("\"id\":") + 5;
        int end = response.indexOf(",", start);

        return Integer.parseInt(response.substring(start, end));
    }

    private static void connect(int a, int b) throws Exception {
        post("/knows", "{\"a\":" + a + ",\"b\":" + b + "}");
    }

    public static void main(String[] args) throws Exception {
        int a = createPerson("TestA");
        int b = createPerson("TestB");
        int c = createPerson("TestC");
        int d = createPerson("TestD");
        int e = createPerson("TestE");

        connect(a, b);
        connect(b, c);
        connect(c, d);
        connect(d, e);

        HttpResponse<String> threeHops =
                get("/path?from=" + a + "&to=" + d);

        if (threeHops.statusCode() != 200
                || !threeHops.body().contains("\"connected\":true")
                || !threeHops.body().contains("\"hops\":3")) {
            throw new AssertionError(
                    "Three-hop path test failed: " + threeHops.body());
        }

        HttpResponse<String> fourHops =
                get("/path?from=" + a + "&to=" + e);

        if (fourHops.statusCode() != 200
                || !fourHops.body().contains("\"connected\":false")) {
            throw new AssertionError(
                    "Four-hop path test failed: " + fourHops.body());
        }

        System.out.println("PASS: Three-hop path is accepted.");
        System.out.println("PASS: Four-hop path is rejected.");
    }
}

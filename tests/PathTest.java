
package tests;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class PathTest {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL = "http://localhost:8080";

    private String post(String endpoint, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private HttpResponse<String> get(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private int createPerson(String name) throws Exception {
        String response = post("/people", "{\"name\":\"" + name + "\"}");

        int start = response.indexOf("\"id\":") + 5;
        int end = response.indexOf(",", start);

        if (end == -1) {
            end = response.indexOf("}", start);
        }

        return Integer.parseInt(response.substring(start, end));
    }

    private void connect(int a, int b) throws Exception {
        post("/knows", "{\"a\":" + a + ",\"b\":" + b + "}");
    }

    @Test
    void acceptsPathOfThreeHops() throws Exception {
        int a = createPerson("TestA");
        int b = createPerson("TestB");
        int c = createPerson("TestC");
        int d = createPerson("TestD");

        connect(a, b);
        connect(b, c);
        connect(c, d);

        HttpResponse<String> response =
                get("/path?from=" + a + "&to=" + d);

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains("\"connected\":true"), response.body());
        assertTrue(response.body().contains("\"hops\":3"), response.body());
    }

    @Test
    void rejectsPathOfFourHops() throws Exception {
        int a = createPerson("TestE");
        int b = createPerson("TestF");
        int c = createPerson("TestG");
        int d = createPerson("TestH");
        int e = createPerson("TestI");

        connect(a, b);
        connect(b, c);
        connect(c, d);
        connect(d, e);

        HttpResponse<String> response =
                get("/path?from=" + a + "&to=" + e);

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains("\"connected\":false"), response.body());
    }
}

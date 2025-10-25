package com.tomcat;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TomcatServlet extends HttpServlet {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try {
            String username = req.getParameter("username");
            if (username == null || username.isEmpty()) {
                resp.sendError(400, "Missing 'username' parameter");
                return;
            }

            String userUrl = "http://localhost:8081/JettyServlet/api/user?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
            JsonObject userData = makeRequest(userUrl);

            if (!userData.has("userId")) {
                resp.sendError(404, "User data not found or invalid response from UserService");
                return;
            }
            String userId = userData.get("userId").getAsString();

            String inventoryUrl = "http://localhost:8082/WildflyServlet/api/inventory?userId=" + userId;
            JsonObject inventoryData = makeRequest(inventoryUrl);

            JsonObject finalResponse = new JsonObject();
            finalResponse.add("user", userData);
            finalResponse.add("inventory", inventoryData);

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(finalResponse));

        } catch (Exception e) {
            resp.sendError(500, "Internal gateway error: " + e.getMessage());
        }
    }

    private JsonObject makeRequest(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), JsonObject.class);
        } else {
            JsonObject error = new JsonObject();
            error.addProperty("error", true);
            error.addProperty("status", response.statusCode());
            error.addProperty("url", url);
            return error;
        }
    }
}
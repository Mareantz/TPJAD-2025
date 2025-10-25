package com.jetty;

import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class JettyServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private Map<String, User> userDatabase;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        loadUserDatabase(config.getServletContext());
    }

    private void loadUserDatabase(ServletContext context) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("users.json")) {
            if (stream == null) {
                log("File not found");
                return;
            }
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            User[] users = gson.fromJson(reader, User[].class);

            this.userDatabase = Arrays.stream(users)
                    .collect(Collectors.toMap(user -> user.username, user -> user));

            log("Database loaded with " + userDatabase.size() + " users.");
        } catch (Exception e) {
            log("Error loading database", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json");

        String username = req.getParameter("username");

        Optional<User> user = Optional.ofNullable(userDatabase.get(username));

        if (user.isPresent()) {
            resp.getWriter().write(gson.toJson(user.get()));
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
        }
    }
}
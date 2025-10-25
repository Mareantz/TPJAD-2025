package com.wildfly;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class WildflyServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private Map<String, Inventory> inventoryDatabase;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        loadInventoryDatabase(config.getServletContext());
    }

    private void loadInventoryDatabase(ServletContext context) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("inventory.json")) {
            if (stream == null) {
                log("File not found");
                return;
            }
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

            Type type = new TypeToken<Map<String, Inventory>>(){}.getType();
            this.inventoryDatabase = gson.fromJson(reader, type);

            log("Database loaded with " + inventoryDatabase.size() + " inventories.");
        } catch (Exception e) {
            log("Error loading database", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json");

        String userId = req.getParameter("userId");
        Optional<Inventory> inventory = Optional.ofNullable(inventoryDatabase.get(userId));

        if (inventory.isPresent()) {
            resp.getWriter().write(gson.toJson(inventory.get()));
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Inventory not found for user");
        }
    }
}
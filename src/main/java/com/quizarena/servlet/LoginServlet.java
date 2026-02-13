package com.quizarena.servlet;

import com.quizarena.util.DBConnection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        writeJson(resp, 200, Map.of("message", "Submit email and password via POST /login"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            writeJson(resp, 400, Map.of("error", "Email and password are required."));
            return;
        }

        // Authentication algorithm: find email, compare password, then create session.
        String sql = "SELECT id, name, role, password FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    writeJson(resp, 401, Map.of("error", "Invalid credentials."));
                    return;
                }

                String dbPassword = rs.getString("password");
                if (!password.equals(dbPassword)) {
                    writeJson(resp, 401, Map.of("error", "Invalid credentials."));
                    return;
                }

                HttpSession session = req.getSession(true);
                session.setAttribute("userId", rs.getInt("id"));
                session.setAttribute("userName", rs.getString("name"));
                session.setAttribute("role", rs.getString("role"));

                Map<String, Object> payload = new HashMap<>();
                payload.put("message", "Login successful");
                payload.put("userId", rs.getInt("id"));
                payload.put("name", rs.getString("name"));
                payload.put("role", rs.getString("role"));
                writeJson(resp, 200, payload);
            }

        } catch (Exception e) {
            writeJson(resp, 500, Map.of("error", "Server error: " + e.getMessage()));
        }
    }
}

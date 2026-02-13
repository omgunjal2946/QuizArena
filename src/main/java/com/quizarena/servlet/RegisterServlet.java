package com.quizarena.servlet;

import com.quizarena.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/register")
public class RegisterServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        writeJson(resp, 200, Map.of("message", "Submit name, email, password, role(student/admin) via POST /register"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String role = req.getParameter("role");

        if (isBlank(name) || isBlank(email) || isBlank(password)) {
            writeJson(resp, 400, Map.of("error", "Name, email, and password are required."));
            return;
        }

        // Default role = student to keep regular signups safe.
        if (isBlank(role) || !("student".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role))) {
            role = "student";
        }

        try (Connection con = DBConnection.getConnection()) {
            String checkSql = "SELECT id FROM users WHERE email = ?";
            try (PreparedStatement checkStmt = con.prepareStatement(checkSql)) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    writeJson(resp, 409, Map.of("error", "Email already registered."));
                    return;
                }
            }

            String insertSql = "INSERT INTO users(name, email, password, role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(insertSql)) {
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, password); // Plain text for assignment simplicity.
                stmt.setString(4, role.toLowerCase());
                stmt.executeUpdate();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Registration successful");
            result.put("role", role.toLowerCase());
            writeJson(resp, 201, result);
        } catch (Exception e) {
            writeJson(resp, 500, Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

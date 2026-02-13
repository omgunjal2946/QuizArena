package com.quizarena.servlet;

import com.quizarena.util.DBConnection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/leaderboard")
public class LeaderboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Optional admin-only view via query: ?adminOnly=true
        String adminOnly = req.getParameter("adminOnly");
        if ("true".equalsIgnoreCase(adminOnly)) {
            Object role = req.getSession(false) != null ? req.getSession(false).getAttribute("role") : null;
            if (role == null || !"admin".equalsIgnoreCase(role.toString())) {
                writeJson(resp, 403, Map.of("error", "Admin role required."));
                return;
            }
        }

        // Leaderboard sorting algorithm:
        // 1) Highest score first
        // 2) For ties, lower time_taken first
        String sql = "SELECT u.name, q.title, s.score, s.time_taken " +
                     "FROM scores s " +
                     "JOIN users u ON s.user_id = u.id " +
                     "JOIN quizzes q ON s.quiz_id = q.id " +
                     "ORDER BY s.score DESC, s.time_taken ASC LIMIT 50";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Map<String, Object>> rows = new ArrayList<>();
            int rank = 1;
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("rank", rank++);
                row.put("name", rs.getString("name"));
                row.put("quizTitle", rs.getString("title"));
                row.put("score", rs.getInt("score"));
                row.put("timeTaken", rs.getInt("time_taken"));
                rows.add(row);
            }

            writeJson(resp, 200, Map.of("leaderboard", rows));
        } catch (Exception e) {
            writeJson(resp, 500, Map.of("error", "Server error: " + e.getMessage()));
        }
    }
}

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

@WebServlet("/quizzes")
public class QuizListServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sql = "SELECT id, title, description, imageURL FROM quizzes ORDER BY id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Map<String, Object>> quizzes = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> quiz = new HashMap<>();
                quiz.put("id", rs.getInt("id"));
                quiz.put("title", rs.getString("title"));
                quiz.put("description", rs.getString("description"));
                quiz.put("imageURL", rs.getString("imageURL"));
                quizzes.add(quiz);
            }

            writeJson(resp, 200, Map.of("quizzes", quizzes));
        } catch (Exception e) {
            writeJson(resp, 500, Map.of("error", "Server error: " + e.getMessage()));
        }
    }
}

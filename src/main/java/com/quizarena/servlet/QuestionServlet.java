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

@WebServlet("/quiz/*")
public class QuestionServlet extends BaseServlet {
    private final ScoreServlet scoreServlet = new ScoreServlet();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Expected path: /quiz/{id}/questions
        String path = req.getPathInfo();
        if (path == null || !path.endsWith("/questions")) {
            writeJson(resp, 404, Map.of("error", "Use /quiz/{id}/questions"));
            return;
        }

        Integer quizId = parseQuizId(path);
        if (quizId == null) {
            writeJson(resp, 400, Map.of("error", "Invalid quiz id."));
            return;
        }

        String sql = "SELECT id, question, option1, option2, option3, option4 FROM questions WHERE quiz_id = ? ORDER BY id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, quizId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Map<String, Object>> questions = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> q = new HashMap<>();
                    q.put("id", rs.getInt("id"));
                    q.put("question", rs.getString("question"));
                    q.put("options", List.of(
                            rs.getString("option1"),
                            rs.getString("option2"),
                            rs.getString("option3"),
                            rs.getString("option4")
                    ));
                    questions.add(q);
                }
                writeJson(resp, 200, Map.of("quizId", quizId, "questions", questions));
            }
        } catch (Exception e) {
            writeJson(resp, 500, Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Expected path: /quiz/{id}/submit
        String path = req.getPathInfo();
        if (path == null || !path.endsWith("/submit")) {
            writeJson(resp, 404, Map.of("error", "Use POST /quiz/{id}/submit"));
            return;
        }

        Integer quizId = parseQuizId(path);
        if (quizId == null) {
            writeJson(resp, 400, Map.of("error", "Invalid quiz id."));
            return;
        }

        scoreServlet.handleSubmit(req, resp, quizId);
    }

    private Integer parseQuizId(String path) {
        try {
            // /{id}/questions OR /{id}/submit
            String[] parts = path.split("/");
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return null;
        }
    }
}

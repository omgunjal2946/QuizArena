package com.quizarena.servlet;

import com.google.gson.reflect.TypeToken;
import com.quizarena.util.DBConnection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ScoreServlet contains score calculation and score persistence logic.
 * It is invoked by QuestionServlet when route is POST /quiz/{id}/submit.
 */
public class ScoreServlet extends BaseServlet {

    public void handleSubmit(HttpServletRequest req, HttpServletResponse resp, int quizId) throws IOException {
        Object sessionUser = req.getSession(false) != null ? req.getSession(false).getAttribute("userId") : null;
        if (sessionUser == null) {
            writeJson(resp, 401, Map.of("error", "Login required."));
            return;
        }

        int userId = (Integer) sessionUser;

        Type mapType = new TypeToken<Map<String, List<Map<String, Object>>>>() {}.getType();
        Map<String, List<Map<String, Object>>> payload = gson.fromJson(req.getReader(), mapType);
        List<Map<String, Object>> answers = payload.get("answers");

        if (answers == null || answers.isEmpty()) {
            writeJson(resp, 400, Map.of("error", "Answers are required."));
            return;
        }

        int correctCount = 0;
        int totalScore = 0;

        // Score algorithm:
        // Base points for correct answer = 10
        // Bonus points = remaining seconds (0..10)
        // Wrong answer = 0
        String qSql = "SELECT correct_option FROM questions WHERE id = ? AND quiz_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement qStmt = con.prepareStatement(qSql)) {

            for (Map<String, Object> answer : answers) {
                int questionId = ((Number) answer.get("questionId")).intValue();
                int selectedOption = ((Number) answer.get("selectedOption")).intValue();
                int timeLeft = ((Number) answer.get("timeLeft")).intValue();

                qStmt.setInt(1, questionId);
                qStmt.setInt(2, quizId);

                try (ResultSet rs = qStmt.executeQuery()) {
                    if (rs.next()) {
                        int correctOption = rs.getInt("correct_option");
                        if (selectedOption == correctOption) {
                            correctCount++;
                            totalScore += 10 + Math.max(timeLeft, 0);
                        }
                    }
                }
            }

            int timeTaken = answers.stream()
                    .mapToInt(a -> 10 - Math.max(((Number) a.get("timeLeft")).intValue(), 0))
                    .sum();

            String insertSql = "INSERT INTO scores(user_id, quiz_id, score, time_taken) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insert = con.prepareStatement(insertSql)) {
                insert.setInt(1, userId);
                insert.setInt(2, quizId);
                insert.setInt(3, totalScore);
                insert.setInt(4, timeTaken);
                insert.executeUpdate();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Quiz submitted");
            result.put("correctAnswers", correctCount);
            result.put("totalQuestions", answers.size());
            result.put("score", totalScore);
            result.put("timeTaken", timeTaken);
            writeJson(resp, 200, result);

        } catch (Exception e) {
            writeJson(resp, 500, Map.of("error", "Server error: " + e.getMessage()));
        }
    }
}

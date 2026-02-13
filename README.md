# QuizArena

Interactive quiz game built using Java Servlets, JDBC (MySQL), HTML/CSS/JS.

## Features
- Student/Admin registration and login
- Quiz listing with images
- Question-by-question gameplay with 10-second timer
- Score + speed bonus
- Leaderboard sorted by score and time

## Tech Stack
- Java Servlet API (no Spring)
- JDBC + MySQL
- HTML/CSS/JavaScript

## Setup
1. Create DB and seed data:
   ```bash
   mysql -u root -p < db/schema.sql
   ```
2. Update DB credentials in `src/main/java/com/quizarena/util/DBConnection.java`.
3. Build WAR:
   ```bash
   mvn clean package
   ```
4. Deploy `target/QuizArena.war` to Tomcat 9+.

## Routes
- `GET /login`
- `POST /login`
- `GET /register`
- `POST /register`
- `GET /quizzes`
- `GET /quiz/{id}/questions`
- `POST /quiz/{id}/submit`
- `GET /leaderboard`

See algorithm explanations in `docs/ALGORITHMS.md`.

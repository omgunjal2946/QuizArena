# QuizArena Algorithms (Student-Friendly)

## 1) Authentication Algorithm
1. User submits email + password from login form.
2. Backend runs SQL query using email.
3. If no user row exists -> reject login.
4. If user exists, compare plain-text password with DB password (for assignment simplicity).
5. If matched -> create HTTP session and store `userId`, `name`, `role`.
6. Role (`student`/`admin`) is later used for protected actions (for example admin-only leaderboard view).

## 2) Score Calculation Algorithm
For each answer record sent from frontend:
1. Get correct option from DB for that question.
2. If selected option is correct:
   - Add base points: `10`
   - Add speed bonus: `timeLeft` seconds
3. If wrong: add `0`.
4. Total score is sum across all questions.
5. Time taken is calculated as sum of `(10 - timeLeft)` per question.

Formula:
- `score_per_correct = 10 + timeLeft`
- `total_score = sum(score_per_correct for correct answers)`

## 3) Timer Logic Algorithm
Each question starts with 10 seconds:
1. Initialize `timeLeft = 10`.
2. Every second, decrement `timeLeft`.
3. Update timer UI and animated progress bar.
4. If user clicks an option before timeout, store `selectedOption` + `timeLeft`.
5. If timer reaches `0`, auto-submit the answer as skipped (`selectedOption = 0`).
6. Move to next question.

## 4) Leaderboard Sorting Algorithm
Leaderboard query uses SQL ordering:
1. Sort by `score DESC` (highest score first).
2. If scores tie, sort by `time_taken ASC` (faster wins tie).
3. Add rank number (1, 2, 3, ...) in response payload.

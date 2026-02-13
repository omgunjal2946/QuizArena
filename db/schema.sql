CREATE DATABASE IF NOT EXISTS quizarena;
USE quizarena;

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    password VARCHAR(120) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'student'
);

CREATE TABLE IF NOT EXISTS quizzes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    imageURL VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS questions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    quiz_id INT NOT NULL,
    question VARCHAR(255) NOT NULL,
    option1 VARCHAR(200) NOT NULL,
    option2 VARCHAR(200) NOT NULL,
    option3 VARCHAR(200) NOT NULL,
    option4 VARCHAR(200) NOT NULL,
    correct_option INT NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);

CREATE TABLE IF NOT EXISTS scores (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    quiz_id INT NOT NULL,
    score INT NOT NULL,
    time_taken INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);

INSERT INTO users(name, email, password, role) VALUES
('Admin', 'admin@quizarena.com', 'admin123', 'admin'),
('Alice', 'alice@student.com', '123456', 'student')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO quizzes(id, title, description, imageURL) VALUES
(1, 'Science Sprint', 'Physics, chemistry, and biology quick round.', 'images/quiz1.svg'),
(2, 'Web Basics', 'HTML, CSS, JavaScript fundamentals.', 'images/quiz2.svg')
ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description), imageURL=VALUES(imageURL);

INSERT INTO questions(quiz_id, question, option1, option2, option3, option4, correct_option) VALUES
(1, 'What planet is known as the Red Planet?', 'Venus', 'Mars', 'Jupiter', 'Saturn', 2),
(1, 'H2O is the chemical formula for?', 'Hydrogen', 'Salt', 'Water', 'Oxygen', 3),
(1, 'What gas do plants absorb?', 'Carbon Dioxide', 'Nitrogen', 'Oxygen', 'Helium', 1),
(1, 'Human DNA is shaped as?', 'Single Helix', 'Double Helix', 'Circle', 'Triangle', 2),
(1, 'How many bones in an adult human body?', '206', '201', '210', '180', 1),
(2, 'HTML stands for?', 'Hyper Text Markup Language', 'High Text Markdown Language', 'Home Tool Markup Language', 'Hyper Tool Machine Language', 1),
(2, 'Which CSS property changes text color?', 'font-style', 'text-color', 'color', 'background-color', 3),
(2, 'Which symbol is used for id in CSS selector?', '.', '#', '*', '&', 2),
(2, 'JavaScript runs in?', 'Browser', 'Database only', 'Printer', 'Router', 1),
(2, 'Which keyword declares a constant in JS?', 'var', 'let', 'const', 'static', 3);

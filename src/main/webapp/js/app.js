const state = {
  user: null,
  quizId: null,
  questions: [],
  index: 0,
  timeLeft: 10,
  timer: null,
  answers: []
};

const loginTab = document.getElementById('loginTab');
const registerTab = document.getElementById('registerTab');
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const authMessage = document.getElementById('authMessage');
const quizCards = document.getElementById('quizCards');

const quizSection = document.getElementById('quizSection');
const questionText = document.getElementById('questionText');
const options = document.getElementById('options');
const progressText = document.getElementById('progressText');
const timerValue = document.getElementById('timerValue');
const timerBar = document.getElementById('timerBar');

const resultSection = document.getElementById('resultSection');
const resultSummary = document.getElementById('resultSummary');
const leaderboardBtn = document.getElementById('leaderboardBtn');
const leaderboardSection = document.getElementById('leaderboardSection');
const leaderboardBody = document.getElementById('leaderboardBody');

loginTab.onclick = () => switchTab('login');
registerTab.onclick = () => switchTab('register');

function switchTab(tab) {
  loginForm.classList.toggle('hidden', tab !== 'login');
  registerForm.classList.toggle('hidden', tab !== 'register');
  loginTab.classList.toggle('active', tab === 'login');
  registerTab.classList.toggle('active', tab === 'register');
  authMessage.textContent = '';
}

loginForm.onsubmit = async (e) => {
  e.preventDefault();
  const formData = new FormData(loginForm);
  const res = await fetch('login', { method: 'POST', body: formData });
  const data = await res.json();
  authMessage.textContent = data.message || data.error;
  if (res.ok) state.user = data;
};

registerForm.onsubmit = async (e) => {
  e.preventDefault();
  const formData = new FormData(registerForm);
  const res = await fetch('register', { method: 'POST', body: formData });
  const data = await res.json();
  authMessage.textContent = data.message || data.error;
  if (res.ok) switchTab('login');
};

async function loadQuizzes() {
  const res = await fetch('quizzes');
  const data = await res.json();
  quizCards.innerHTML = '';
  (data.quizzes || []).forEach(q => {
    const card = document.createElement('div');
    card.className = 'quiz-card';
    card.innerHTML = `
      <img src="${q.imageURL}" alt="${q.title}" />
      <div class="content">
        <h3>${q.title}</h3>
        <p>${q.description}</p>
        <button>Start Quiz</button>
      </div>`;
    card.querySelector('button').onclick = () => startQuiz(q.id);
    quizCards.appendChild(card);
  });
}

async function startQuiz(id) {
  if (!state.user) {
    authMessage.textContent = 'Please login first.';
    return;
  }
  const res = await fetch(`quiz/${id}/questions`);
  const data = await res.json();
  state.quizId = id;
  state.questions = data.questions || [];
  state.index = 0;
  state.answers = [];
  resultSection.classList.add('hidden');
  leaderboardSection.classList.add('hidden');
  quizSection.classList.remove('hidden');
  showQuestion();
}

function showQuestion() {
  clearInterval(state.timer);
  if (state.index >= state.questions.length) {
    submitQuiz();
    return;
  }

  const q = state.questions[state.index];
  progressText.textContent = `Question ${state.index + 1} / ${state.questions.length}`;
  questionText.textContent = q.question;
  options.innerHTML = '';
  state.timeLeft = 10;
  timerValue.textContent = state.timeLeft;

  timerBar.style.animation = 'none';
  void timerBar.offsetWidth;
  timerBar.style.animation = 'countdown 10s linear forwards';

  q.options.forEach((op, i) => {
    const btn = document.createElement('button');
    btn.className = 'option-btn';
    btn.textContent = `${i + 1}. ${op}`;
    btn.onclick = () => answer(i + 1);
    options.appendChild(btn);
  });

  // Timer algorithm: decrement each second, auto-submit as wrong when it reaches 0.
  state.timer = setInterval(() => {
    state.timeLeft--;
    timerValue.textContent = state.timeLeft;
    if (state.timeLeft <= 0) {
      clearInterval(state.timer);
      answer(0);
    }
  }, 1000);
}

function answer(selectedOption) {
  clearInterval(state.timer);
  const q = state.questions[state.index];
  state.answers.push({ questionId: q.id, selectedOption, timeLeft: state.timeLeft });
  state.index++;
  showQuestion();
}

async function submitQuiz() {
  const res = await fetch(`quiz/${state.quizId}/submit`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ answers: state.answers })
  });

  const data = await res.json();
  quizSection.classList.add('hidden');
  resultSection.classList.remove('hidden');
  resultSummary.innerHTML = `Correct: <b>${data.correctAnswers}/${data.totalQuestions}</b><br/>Score: <b>${data.score}</b><br/>Time: <b>${data.timeTaken}s</b>`;
}

leaderboardBtn.onclick = loadLeaderboard;

async function loadLeaderboard() {
  const res = await fetch('leaderboard');
  const data = await res.json();
  leaderboardSection.classList.remove('hidden');
  leaderboardBody.innerHTML = '';
  (data.leaderboard || []).forEach(r => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${r.rank}</td><td>${r.name}</td><td>${r.quizTitle}</td><td>${r.score}</td><td>${r.timeTaken}</td>`;
    leaderboardBody.appendChild(tr);
  });
}

loadQuizzes();

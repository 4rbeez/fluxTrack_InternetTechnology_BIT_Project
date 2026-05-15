// =============================================================
// fluxTrack - auth.js
// Handles login, token storage in localStorage, and provides
// authFetch() — a fetch wrapper that attaches the JWT and redirects
// to /login if the token is missing or rejected.
// =============================================================

const TOKEN_KEY = 'fluxtrack_token';
const USER_KEY = 'fluxtrack_user';

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function getUser() {
    return localStorage.getItem(USER_KEY);
}

function setAuth(token, username) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, username);
}

function clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

function logout() {
    clearAuth();
    window.location.href = '/login';
}

// Page guard: call at the top of any protected page
function requireAuth() {
    if (!getToken()) {
        window.location.href = '/login';
    }
}

// Wrapped fetch that adds the JWT and handles auth errors centrally.
async function authFetch(url, options = {}) {
    const token = getToken();
    if (!token) {
        window.location.href = '/login';
        return null;
    }
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
        'Authorization': `Bearer ${token}`,
    };
    const response = await fetch(url, { ...options, headers });
    if (response.status === 401 || response.status === 403) {
        clearAuth();
        window.location.href = '/login';
        return null;
    }
    return response;
}

// =============================================================
// Login form (only present on /login)
// =============================================================
const loginForm = document.getElementById('login-form');
if (loginForm) {
    // If already logged in, skip the form
    if (getToken()) {
        window.location.href = '/products';
    }

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const errorDiv = document.getElementById('login-error');
        errorDiv.textContent = '';

        if (!username || !password) {
            errorDiv.textContent = 'Please enter both username and password.';
            return;
        }

        try {
            const basicAuth = btoa(`${username}:${password}`);
            const response = await fetch('/partner/token', {
                method: 'POST',
                headers: { 'Authorization': `Basic ${basicAuth}` },
            });
            if (!response.ok) {
                throw new Error(response.status === 401
                    ? 'Invalid username or password'
                    : `Login failed (HTTP ${response.status})`);
            }
            const token = (await response.text()).trim();
            if (!token) throw new Error('Empty token returned by server');
            setAuth(token, username);
            window.location.href = '/products';
        } catch (err) {
            errorDiv.textContent = err.message;
        }
    });
}

// Topbar: company name + logout (present on app pages)
const companyNameEl = document.getElementById('company-name');
if (companyNameEl && getUser()) {
    companyNameEl.textContent = getUser();
}
 
const logoutBtn = document.getElementById('logout-btn');
if (logoutBtn) {
    logoutBtn.addEventListener('click', logout);
}
 
// Hide admin-only sidebar items for non-admin users.
// The Partners link is marked with id="sidebar-partners" in the fragment.
if (getUser() && getUser() !== 'admin') {
    const partnersLink = document.getElementById('sidebar-partners');
    if (partnersLink) {
        partnersLink.classList.add('hidden');
    }
}
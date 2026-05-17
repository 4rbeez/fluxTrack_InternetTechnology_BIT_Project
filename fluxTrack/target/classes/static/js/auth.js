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
    if (getToken()) {
        window.location.href = '/dashboard';
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
            // UTF-8 safe base64 for usernames/passwords with non-ASCII characters
            const basicAuth = btoa(unescape(encodeURIComponent(`${username}:${password}`)));
            const response = await fetch('/token', {
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
            window.location.href = '/dashboard';
        } catch (err) {
            errorDiv.textContent = err.message;
        }
    });
}

// =============================================================
// Topbar: logo + display name + logout (present on app pages)
// =============================================================

// Map technical login username → display name + logo file.
// Keeping the lookup here means the topbar fragment stays static
// HTML and any page that includes the fragment gets the swap for free.
const USER_PROFILES = {
    'admin':        { displayName: 'Administrator',    logo: '/images/partners/fluxed.png' },
    'wylaade':      { displayName: 'Wylaade GmbH',     logo: '/images/partners/wylaade.png' },
    'drachehoehli': { displayName: 'Drachehöhli GmbH', logo: '/images/partners/drachehoehli.png' },
};

const currentUsername = getUser();
const currentProfile  = currentUsername ? USER_PROFILES[currentUsername] : null;

const companyNameEl = document.getElementById('company-name');
if (companyNameEl && currentUsername) {
    companyNameEl.textContent = currentProfile ? currentProfile.displayName : currentUsername;
}

const companyLogoEl = document.getElementById('company-logo');
if (companyLogoEl && currentProfile) {
    companyLogoEl.src = currentProfile.logo;
    companyLogoEl.alt = currentProfile.displayName + ' logo';
}

const logoutBtn = document.getElementById('logout-btn');
if (logoutBtn) {
    logoutBtn.addEventListener('click', logout);
}

// Hide admin-only sidebar items for non-admin users.
if (currentUsername && currentUsername !== 'admin') {
    const partnersLink = document.getElementById('sidebar-partners');
    if (partnersLink) {
        partnersLink.classList.add('hidden');
    }
}

// =============================================================
// Sidebar collapse toggle
// =============================================================
// Two hamburger buttons cooperate: the one in the sidebar (visible
// when the sidebar is shown) and the one in the topbar (which is
// visibility:hidden until the sidebar is collapsed). Clicking either
// flips a body class and the CSS does the rest. State persists across
// navigation in localStorage so it doesn't reset on every page load.
const SIDEBAR_COLLAPSED_KEY = 'fluxtrack_sidebarCollapsed';

function applySidebarState() {
    if (localStorage.getItem(SIDEBAR_COLLAPSED_KEY) === '1') {
        document.body.classList.add('sidebar-collapsed');
    } else {
        document.body.classList.remove('sidebar-collapsed');
    }
}

function toggleSidebar() {
    const collapsed = document.body.classList.toggle('sidebar-collapsed');
    localStorage.setItem(SIDEBAR_COLLAPSED_KEY, collapsed ? '1' : '0');
}

applySidebarState();

document.querySelectorAll('.sidebar-toggle, .topbar-toggle').forEach(btn => {
    btn.addEventListener('click', toggleSidebar);
});
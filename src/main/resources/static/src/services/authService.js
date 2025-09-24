// Handles user authentication, token management, and session control
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => error ? prom.reject(error) : prom.resolve(token));
    failedQueue = [];
};

export function logout() {
    localStorage.clear();
    window.location.href = '/auth/login';
}

async function refreshToken() {
    const refreshTokenValue = localStorage.getItem('refreshToken');
    if (!refreshTokenValue) {
        logout();
        return Promise.reject('No refresh token');
    }

    try {
        const res = await fetch('/api/v1/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 'refresh_token': refreshTokenValue })
        });

        if (!res.ok) throw new Error('Failed to refresh token');

        const data = await res.json();
        localStorage.setItem('accessToken', data.access_token);
        processQueue(null, data.access_token);
        return data.access_token;
    } catch (error) {
        processQueue(error, null);
        logout();
        return Promise.reject(error);
    }
}

export async function fetchWithAuth(url, options = {}) {
    let token = localStorage.getItem('accessToken');
    options.headers = { ...options.headers, 'Authorization': `Bearer ${token}` };

    let response = await fetch(url, options);

    if (response.status === 401) {
        if (isRefreshing) {
            const newToken = await new Promise((resolve, reject) => failedQueue.push({ resolve, reject }));
            options.headers['Authorization'] = `Bearer ${newToken}`;
            response = await fetch(url, options);
        } else {
            isRefreshing = true;
            try {
                const newToken = await refreshToken();
                options.headers['Authorization'] = `Bearer ${newToken}`;
                response = await fetch(url, options);
            } finally {
                isRefreshing = false;
            }
        }
    }
    return response;
}

export function checkAuth() {
    if (!localStorage.getItem('accessToken')) {
        window.location.href = '/auth/login';
    }
}

export function redirectIfLoggedIn() {
    if (localStorage.getItem('accessToken')) {
        window.location.href = '/dashboard';
    }
}
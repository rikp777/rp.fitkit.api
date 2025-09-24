import { redirectIfLoggedIn } from '../../services/authService.js';
import { loginUser } from '../../services/apiService.js';
import { qs, setButtonLoadingState } from '../../utils/dom.js';
import { initializePasswordToggle } from '../../utils/form.js';

/**
 * Initializes all functionality for the login page.
 */
export function initLoginPage() {
    redirectIfLoggedIn();

    const loginForm = qs('#loginForm');
    if (!loginForm) return;

    const loginButton = qs('#loginButton');
    const messageDiv = qs('#message');

    loginButton.disabled = false;

    initializePasswordToggle();

    loginForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        const username = loginForm.username.value;
        const password = loginForm.password.value;

        setButtonLoadingState(loginButton, true, 'Signing in...');
        messageDiv.textContent = '';

        try {
            const response = await loginUser(username, password);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.detail || 'Login failed! Please check your credentials.');
            }

            const data = await response.json();
            localStorage.setItem('accessToken', data.access_token);
            localStorage.setItem('refreshToken', data.refresh_token);
            localStorage.setItem('user', JSON.stringify(data.user));

            window.location.href = '/dashboard';
        } catch (error) {
            setButtonLoadingState(loginButton, false);
            messageDiv.textContent = error.message;
            messageDiv.style.color = 'red';
            console.error('Login Error:', error);
        }
    });
}
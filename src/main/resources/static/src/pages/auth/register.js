import { redirectIfLoggedIn } from '../../services/authService.js';
import { registerUser } from '../../services/apiService.js';
import { qs, setButtonLoadingState } from '../../utils/dom.js';
import { initializePasswordToggle } from '../../utils/form.js';

/**
 * Initializes all functionality for the registration page.
 */
export function initRegisterPage() {
    redirectIfLoggedIn();

    const registerForm = qs('#registerForm');
    if (!registerForm) return;

    const registerButton = qs('#registerButton');
    const messageDiv = qs('#message');

    registerButton.disabled = false;

    initializePasswordToggle();

    registerForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        const username = registerForm.username.value;
        const email = registerForm.email.value;
        const password = registerForm.password.value;

        setButtonLoadingState(registerButton, true, 'Creating account...');
        messageDiv.textContent = '';

        try {
            const response = await registerUser(username, email, password);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.detail || 'Registration failed! Please try again.');
            }

            messageDiv.textContent = 'Registration successful! Redirecting to login...';
            messageDiv.style.color = 'green';
            setTimeout(() => {
                window.location.href = '/auth/login';
            }, 2000);

        } catch (error) {
            setButtonLoadingState(registerButton, false);
            messageDiv.textContent = error.message;
            messageDiv.style.color = 'red';
            console.error('Registration Error:', error);
        }
    });
}
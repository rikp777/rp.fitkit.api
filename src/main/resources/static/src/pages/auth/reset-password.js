import * as api from '../../services/apiService.js';
import { qs, setButtonLoadingState } from '../../utils/dom.js';
import { initializePasswordToggle } from '../../utils/form.js';

export function initResetPasswordPage() {
    const resetForm = qs('#resetPasswordForm');
    if (!resetForm) return;

    const resetButton = qs('#resetPasswordButton');
    const messageDiv = qs('#message');

    resetButton.disabled = false;
    initializePasswordToggle();

    resetForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        setButtonLoadingState(resetButton, true, 'Resetting...');
        messageDiv.textContent = '';

        const requestBody = {
            username: resetForm.username.value,
            recoveryCode: resetForm.recoveryCode.value,
            newPassword: resetForm.password.value
        };

        try {
            const response = await api.resetPasswordWithCode(requestBody);
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.detail || 'Password reset failed. Please check your details.');
            }

            messageDiv.textContent = 'Password reset successful! Redirecting to login...';
            messageDiv.style.color = 'green';
            setTimeout(() => { window.location.href = '/auth/login'; }, 2000);

        } catch (error) {
            setButtonLoadingState(resetButton, false);
            messageDiv.textContent = error.message;
            messageDiv.style.color = 'red';
            console.error('Reset Password Error:', error);
        }
    });
}
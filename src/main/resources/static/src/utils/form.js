/**
 * Initializes the show/hide password toggle functionality for a password input.
 * It expects a specific HTML structure with IDs: 'togglePassword', 'password', 'eyeIcon', 'eyeSlashIcon'.
 */
export function initializePasswordToggle() {
    const togglePassword = document.getElementById('togglePassword');
    if (!togglePassword) return;

    const passwordInput = document.getElementById('password');
    const eyeIcon = document.getElementById('eyeIcon');
    const eyeSlashIcon = document.getElementById('eyeSlashIcon');

    if (!passwordInput || !eyeIcon || !eyeSlashIcon) return;

    togglePassword.addEventListener('click', () => {
        const isPassword = passwordInput.type === 'password';
        passwordInput.type = isPassword ? 'text' : 'password';
        eyeIcon.classList.toggle('hidden', !isPassword);
        eyeSlashIcon.classList.toggle('hidden', isPassword);
    });
}
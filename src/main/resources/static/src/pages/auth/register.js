import { redirectIfLoggedIn } from '../../services/authService.js';
import * as api from '../../services/apiService.js';
import { qs, setButtonLoadingState } from '../../utils/dom.js';
import { initializePasswordToggle } from '../../utils/form.js';

export function initRegisterPage() {
    redirectIfLoggedIn();

    const step1Div = qs('#registration-step-1');
    const registerForm = qs('#registerForm');
    const registerButton = qs('#registerButton');
    const messageDiv = qs('#message');

    const step2Div = qs('#registration-step-2');
    const codesLineNumbers = qs('#recovery-codes-linenumbers');
    const codesDisplay = qs('#recovery-codes-display');
    const copyBtn = qs('#copy-codes-btn');
    const savedCheckbox = qs('#codes-saved-checkbox');
    const finishBtn = qs('#finish-registration-btn');
    const downloadBtn = qs('#download-codes-btn');

    const formTitle = qs('#form-title');
    const loginLinkContainer = qs('#login-link-container');

    if (!registerForm) return;

    registerButton.disabled = false;
    initializePasswordToggle();

    registerForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        if (registerForm.website.value) {
            console.warn('Honeypot field filled. Submission blocked as likely bot.');
            return;
        }

        setButtonLoadingState(registerButton, true, 'Creating account...');
        messageDiv.textContent = '';

        try {
            const response = await api.registerUser(
                registerForm.username.value,
                registerForm.email.value,
                registerForm.password.value
            );

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.detail || 'Registration failed! Please try again.');
            }

            const loginData = await response.json();
            localStorage.setItem('accessToken', loginData.access_token);
            localStorage.setItem('refreshToken', loginData.refresh_token);

            step1Div.classList.add('hidden');
            messageDiv.classList.add('hidden');
            loginLinkContainer.classList.add('hidden');
            step2Div.classList.remove('hidden');
            formTitle.textContent = 'Save Your Recovery Codes';

            fetchAndDisplayRecoveryCodes();

        } catch (error) {
            setButtonLoadingState(registerButton, false);
            messageDiv.textContent = error.message;
            messageDiv.style.color = 'red';
            console.error('Registration Error:', error);
        }
    });

    async function fetchAndDisplayRecoveryCodes() {
        codesLineNumbers.textContent = '';
        codesDisplay.innerHTML = '<span class="text-gray-400">Generating...</span>';
        copyBtn.classList.add('hidden');
        downloadBtn.classList.add('hidden');

        try {
            const response = await api.generateRecoveryCodes();
            if (!response.ok) throw new Error('Could not fetch recovery codes.');

            const data = await response.json();
            const codes = data.recoveryCodes;

            codesLineNumbers.textContent = codes.map((_, i) => i + 1).join('\n');
            codesDisplay.textContent = codes.join('\n');
            copyBtn.classList.remove('hidden');
            downloadBtn.classList.remove('hidden');

        } catch (error) {
            console.error(error);
            codesDisplay.innerHTML = `
                <span class="text-red-400">Error loading codes.</span>
                <button id="retry-codes-btn" class="ml-2 text-indigo-400 hover:underline">Retry</button>
            `;
            qs('#retry-codes-btn').addEventListener('click', fetchAndDisplayRecoveryCodes);
        }
    }

    downloadBtn.addEventListener('click', () => {
        const codesText = codesDisplay.textContent;
        if (!codesText || codesText.includes('Error') || codesText.includes('Generating')) {
            return;
        }
        const blob = new Blob([codesText], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'fitkit-recovery-codes.txt';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    });

    copyBtn.addEventListener('click', () => {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(codesDisplay.textContent);
            copyBtn.textContent = 'Copied!';
            setTimeout(() => { copyBtn.textContent = 'Copy Codes'; }, 2000);
        }
    });

    savedCheckbox.addEventListener('change', () => {
        finishBtn.disabled = !savedCheckbox.checked;
    });

    finishBtn.addEventListener('click', () => {
        window.location.href = '/dashboard';
    });
}
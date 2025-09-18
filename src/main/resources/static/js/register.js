redirectIfLoggedIn();

const registerForm = document.getElementById('registerForm');
if (registerForm) {
    const registerButton = document.getElementById('registerButton');
    const messageDiv = document.getElementById('message');

    registerButton.disabled = false;
    initializePasswordToggle();

    registerForm.addEventListener('submit', function (event) {
        event.preventDefault();

        const username = registerForm.username.value;
        const email = registerForm.email.value;
        const password = registerForm.password.value;

        setButtonLoadingState(registerButton, true, 'Creating account...');
        messageDiv.textContent = '';

        const registrationData = { username, email, password };

        fetch('/api/v1/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(registrationData)
        })
            .then(async response => {
                if (response.ok) return response.json();
                const errorData = await response.json();
                throw new Error(errorData.detail || 'Registration failed!');
            })
            .then(data => {
                messageDiv.textContent = 'Registration successful! Redirecting to login...';
                messageDiv.style.color = 'green';
                setTimeout(() => { window.location.href = '/auth/login'; }, 2000);
            })
            .catch(error => {
                setButtonLoadingState(registerButton, false);
                messageDiv.textContent = error.message;
                messageDiv.style.color = 'red';
                console.error('Error:', error.message);
            });
    });
}
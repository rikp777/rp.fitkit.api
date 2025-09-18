redirectIfLoggedIn();
const loginForm = document.getElementById('loginForm');

if (loginForm) {
    const loginButton = document.getElementById('loginButton');
    const messageDiv = document.getElementById('message');

    loginButton.disabled = false;
    initializePasswordToggle();

    loginForm.addEventListener('submit', function (event) {
        event.preventDefault();

        const username = loginForm.username.value;
        const password = loginForm.password.value;

        setButtonLoadingState(loginButton, true, 'Signing in...');
        messageDiv.textContent = '';

        const details = new URLSearchParams();
        details.append('username', username);
        details.append('password', password);

        fetch('/api/v1/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: details
        })
            .then(response => {
                if (response.ok) return response.json();
                throw new Error('Login failed! Please check your credentials.');
            })
            .then(data => {
                localStorage.setItem('accessToken', data.access_token);
                localStorage.setItem('refreshToken', data.refresh_token);
                localStorage.setItem('user', JSON.stringify(data.user));
                window.location.href = '/dashboard';
            })
            .catch(error => {
                setButtonLoadingState(loginButton, false);
                messageDiv.textContent = error.message;
                messageDiv.style.color = 'red';
                console.error('Error:', error.message);
            });
    });
}
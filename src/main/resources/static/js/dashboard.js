document.addEventListener('DOMContentLoaded', function() {
    checkAuth();

    const greetingElement = document.getElementById('greeting');

    fetchWithAuth('/api/v1/auth/me')
        .then(response => {
            if (!response.ok) {
                throw new Error('Could not fetch user details.');
            }
            return response.json();
        })
        .then(user => {
            const hour = new Date().getHours();

            if (hour >= 0 && hour < 5) {
                greetingElement.textContent = `Shouldn't you be asleep, ${user.username}? 😉`;
            } else {
                let timeOfDayGreeting;
                if (hour >= 5 && hour < 12) {
                    timeOfDayGreeting = 'Good morning';
                } else if (hour >= 12 && hour < 18) {
                    timeOfDayGreeting = 'Good afternoon';
                } else {
                    timeOfDayGreeting = 'Good evening';
                }
                greetingElement.textContent = `${timeOfDayGreeting}, ${user.username}!`;
            }
        })
        .catch(error => {
            console.error("Failed to load dashboard data:", error);
            greetingElement.textContent = 'Welcome!';
        });

    document.getElementById('logoutButton').addEventListener('click', function() {
        logout();
    });
});

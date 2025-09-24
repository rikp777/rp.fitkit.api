document.addEventListener('DOMContentLoaded', function() {
    checkAuth();

    loadGreeting();

    loadDashboardStats();

    document.getElementById('logoutButton').addEventListener('click', function() {
        logout();
    });
});

/**
 * Fetches the current user's details to display a personalized greeting
 * based on their username and the time of day.
 */
function loadGreeting() {
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

            // --- CORRECTED LOGIC ---
            // Handles the special late-night message
            if (hour >= 0 && hour < 5) {
                greetingElement.textContent = `Shouldn't you be asleep, ${user.username}? 😉`;
            } else {
                // Handles the standard greetings for the rest of the day
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
            console.error("Failed to load user data:", error);
            greetingElement.textContent = 'Welcome!'; // Fallback greeting
        });
}

/**
 * Fetches statistics for the various modules and updates the dashboard cards.
 */
async function loadDashboardStats() {
    const mentalHealthCountEl = document.getElementById('mental-health-log-count');

    try {
        const response = await fetchWithAuth('/api/v1/logbook/stats/total-count');
        if (!response.ok) {
            throw new Error(`Server responded with status: ${response.status}`);
        }

        const count = await response.json();

        mentalHealthCountEl.textContent = `${count} ${count === 1 ? 'journal' : 'journals'}`;

    } catch (error) {
        console.error('Error fetching mental health stats:', error);
        mentalHealthCountEl.textContent = 'N/A';
    }

}
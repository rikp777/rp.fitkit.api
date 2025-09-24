import { checkAuth, logout } from '../services/authService.js';
import { getUserDetails, getDashboardStats } from '../services/apiService.js';
import { qs } from '../utils/dom.js';

/**
 * Initializes all functionality for the main dashboard page.
 */
export function initDashboardPage() {
    checkAuth();

    /**
     * Fetches the user's details to display a personalized greeting.
     */
    async function loadGreeting() {
        const greetingElement = qs('#greeting');
        if (!greetingElement) return;

        try {
            const response = await getUserDetails();
            if (!response.ok) throw new Error('Could not fetch user details.');

            const user = await response.json();
            const hour = new Date().getHours();
            let timeOfDayGreeting;

            if (hour >= 0 && hour < 5) {
                greetingElement.textContent = `Shouldn't you be asleep, ${user.username}? 😉`;
                return;
            } else if (hour < 12) {
                timeOfDayGreeting = 'Good morning';
            } else if (hour < 18) {
                timeOfDayGreeting = 'Good afternoon';
            } else {
                timeOfDayGreeting = 'Good evening';
            }
            greetingElement.textContent = `${timeOfDayGreeting}, ${user.username}!`;

        } catch (error) {
            console.error("Failed to load user data:", error);
            greetingElement.textContent = 'Welcome!';
        }
    }

    /**
     * Fetches statistics for modules and updates the dashboard cards.
     */
    async function loadDashboardStats() {
        const countEl = qs('#mental-health-log-count');
        if (!countEl) return;

        try {
            const response = await getDashboardStats();
            if (!response.ok) throw new Error(`Server error: ${response.status}`);

            const count = await response.json();
            countEl.textContent = `${count} ${count === 1 ? 'journal' : 'journals'}`;

        } catch (error) {
            console.error('Error fetching mental health stats:', error);
            countEl.textContent = 'N/A';
        }
    }

    const logoutButton = qs('#logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', logout);
    }

    loadGreeting();
    loadDashboardStats();
}
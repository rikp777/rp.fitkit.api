import { renderFooter } from './components/Footer.js';
import { initializeEasterEgg } from './utils/effects.js';
import { initLoginPage } from './pages/auth/login.js';
import { initRegisterPage } from './pages/auth/register.js';
import { initDashboardPage } from './pages/dashboard.js';
import { initMentalHealthPage } from './pages/mental-health.js';
import { initResetPasswordPage } from "./pages/auth/reset-password.js";

document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM fully loaded and parsed. Initializing app...');
    const path = window.location.pathname;

    if (path.startsWith('/auth/login')) {
        initLoginPage();
    } else if (path.startsWith('/auth/register')) {
        initRegisterPage();
    } else if (path.includes('/auth/reset')) {
        initResetPasswordPage();
    } else if (path.startsWith('/dashboard')) {
        initDashboardPage();
    } else if (path.startsWith('/mental-health')) {
        initMentalHealthPage();
    }

    renderFooter();
    initializeEasterEgg();
});
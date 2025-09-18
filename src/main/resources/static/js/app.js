/**
 * Redirects to the login page if the user is not authenticated. Must be called by pages that require a user to be logged in.
 */
function checkAuth() {
    if (!localStorage.getItem('accessToken')) {
        window.location.href = '/auth/login';
    }
}

/**
 * Redirects to the dashboard if the user is already authenticated. Should be called from pages like login/register.
 */
function redirectIfLoggedIn() {
    if (localStorage.getItem('accessToken')) {
        window.location.href = '/dashboard';
    }
}

/**
 * Logs the user out by clearing credentials and redirecting to the login page.
 */
function logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    window.location.href = '/auth/login';
}

/**
 * Initializes the show/hide password toggle functionality for a given password input.
 */
function initializePasswordToggle() {
    const togglePassword = document.getElementById('togglePassword');
    if (togglePassword) {
        const passwordInput = document.getElementById('password');
        const eyeIcon = document.getElementById('eyeIcon');
        const eyeSlashIcon = document.getElementById('eyeSlashIcon');

        togglePassword.addEventListener('click', function () {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            eyeIcon.classList.toggle('hidden');
            eyeSlashIcon.classList.toggle('hidden');
        });
    }
}

/**
 * Sets the loading state for a form button.
 * @param {HTMLButtonElement} button The button element.
 * @param {boolean} isLoading Whether to show the loading state or not.
 * @param {string} [loadingText='Loading...'] The text to display when loading.
 */
function setButtonLoadingState(button, isLoading, loadingText = 'Loading...') {
    const spinner = button.querySelector('svg');
    const buttonTextEl = button.querySelector('span');
    const originalText = button.dataset.originalText || buttonTextEl.textContent;

    if (!button.dataset.originalText) {
        button.dataset.originalText = originalText;
    }

    if (isLoading) {
        button.disabled = true;
        spinner.classList.remove('hidden');
        buttonTextEl.textContent = loadingText;
    } else {
        button.disabled = false;
        spinner.classList.add('hidden');
        buttonTextEl.textContent = originalText;
    }
}

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

async function refreshToken() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
        console.error('No refresh token available.');
        logout();
        return Promise.reject();
    }

    try {
        const response = await fetch('/api/v1/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 'refresh_token': refreshToken })
        });

        if (!response.ok) {
            throw new Error('Failed to refresh token');
        }

        const data = await response.json();
        localStorage.setItem('accessToken', data.access_token);
        processQueue(null, data.access_token);
        return data.access_token;

    } catch (error) {
        console.error('Refresh token failed:', error);
        processQueue(error, null);
        logout();
        return Promise.reject(error);
    }
}

/**
 * A wrapper for the native fetch function that automatically handles access token refreshing.
 * Use this for all authenticated API calls.
 */
async function fetchWithAuth(url, options = {}) {
    let token = localStorage.getItem('accessToken');

    if (!options.headers) {
        options.headers = {};
    }
    options.headers['Authorization'] = `Bearer ${token}`;

    let response = await fetch(url, options);

    if (response.status === 401) {
        if (isRefreshing) {
            try {
                const newToken = await new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                });
                options.headers['Authorization'] = `Bearer ${newToken}`;
                response = await fetch(url, options);
            } catch (err) {
                return Promise.reject(err);
            }
        } else {
            isRefreshing = true;
            try {
                const newToken = await refreshToken();
                options.headers['Authorization'] = `Bearer ${newToken}`;
                response = await fetch(url, options);
            } catch (err) {
                return Promise.reject(err);
            } finally {
                isRefreshing = false;
            }
        }
    }

    return response;
}

/**
 * Renders a common footer into any element with the ID 'page-footer'.
 */
function renderFooter() {
    const footerElement = document.getElementById('page-footer');
    if (!footerElement) return;

    const currentYear = new Date().getFullYear();
    const konamiCode = ['↑', '↑', '↓', '↓', '←', '→'];
    const konamiString = konamiCode.join(' ');

    footerElement.innerHTML = `
        <div class="relative w-full text-center">
            <p>
                FitKit POC Client 
                <a href="${appConfig.app.link}" target="_blank" rel="noopener noreferrer" class="font-semibold text-indigo-400 hover:text-indigo-300">
                    v${appConfig.app.version}
                </a>
            </p>
            <p class="mt-1">
                &copy; ${currentYear} | Made with <span id="heart" class="inline-block">❤️</span> by 
                <a href="${appConfig.dev.link}" target="_blank" rel="noopener noreferrer" 
                   id="dev-link" 
                   class="font-semibold text-indigo-400 hover:text-indigo-300">
                    ${appConfig.dev.name}
                </a>
            </p>
        </div>
        <div id="easter-egg-hint" class="fixed bottom-4 right-4 p-2 bg-gray-700 text-white text-xs rounded-md shadow-lg opacity-0 transition-opacity duration-300">
             ${konamiString}
        </div>
    `;

    const devLink = document.getElementById('dev-link');
    const hint = document.getElementById('easter-egg-hint');
    const heart = document.getElementById('heart');

    if (devLink && hint) {
        devLink.addEventListener('mouseenter', () => hint.classList.remove('opacity-0'));
        devLink.addEventListener('mouseleave', () => hint.classList.add('opacity-0'));
    }

    if (footerElement && heart) {
        footerElement.addEventListener('mouseenter', () => {
            heart.style.animation = 'heartbeat 1s infinite';
        });
        footerElement.addEventListener('mouseleave', () => {
            heart.style.animation = '';
        });
    }
}

document.addEventListener('DOMContentLoaded', function() {
    renderFooter();
    initializeEasterEgg();
});

/**
 * Renders a common sidebar into the '#sidenav-container' element.
 * @param {object} moduleInfo - Information about the current module.
 * @param {string} moduleInfo.title - The main title (e.g., 'FitKit').
 * @param {string} moduleInfo.subtitle - The subtitle for the section (e.g., 'Mental Health').
 * @param {Array<object>} navItems - An array of navigation item objects.
 * @param {string} navItems[].icon - The emoji/icon for the link.
 * @param {string} navItems[].label - The text for the link.
 * @param {string} navItems[].href - The URL for the link.
 * @param {string} activePath - The current page's path to highlight the active link.
 */
function renderSidenav(moduleInfo, navItems, activePath) {
    const container = document.getElementById('sidenav-container');
    if (!container) return;

    const navLinksHtml = navItems.map(item => `
        <li class="mt-2">
            <a href="${item.href}" class="sidenav-link ${item.href === activePath ? 'sidenav-link-active' : ''}">
                <span class="mr-3">${item.icon}</span> ${item.label}
            </a>
        </li>
    `).join('');

    container.innerHTML = `
        <aside id="sidenav" class="w-64 bg-gray-800 p-4 flex flex-col fixed lg:static lg:translate-x-0 h-full z-50 transform -translate-x-full transition-transform duration-300 ease-in-out">
            <a class="mb-6" href="${moduleInfo.link}">
                 <h1 class="text-2xl font-bold text-indigo-400 text-center">${moduleInfo.title}</h1>
                 <p class="text-sm text-gray-400 text-center">${moduleInfo.subtitle}</p>
            </a>
            <nav class="flex-grow">
                <ul>${navLinksHtml}</ul>
            </nav>
            <div class="border-t border-gray-700 pt-4">
                <a href="/dashboard" class="sidenav-link">
                    <span class="mr-3">&larr;</span> Back to Main Menu
                </a>
                 <button id="sidenav-close-button" class="w-full mt-4 sidenav-link lg:hidden">
                    <span class="mr-3">&times;</span> Close Menu
                </button>
            </div>
        </aside>
    `;
}

/**
 * Initializes the toggle functionality for a mobile sidenav.
 */
function initializeSidenavToggle() {
    const sidenav = document.getElementById('sidenav');
    const openButton = document.getElementById('mobile-menu-button');
    const closeButton = document.getElementById('sidenav-close-button');
    const overlay = document.getElementById('sidenav-overlay');

    if (!sidenav || !openButton || !closeButton || !overlay) {
        return;
    }

    const openSidenav = () => {
        sidenav.classList.remove('-translate-x-full');
        overlay.classList.remove('hidden');
    };

    const closeSidenav = () => {
        sidenav.classList.add('-translate-x-full');
        overlay.classList.add('hidden');
    };

    openButton.addEventListener('click', openSidenav);
    closeButton.addEventListener('click', closeSidenav);
    overlay.addEventListener('click', closeSidenav);
}

/**
 * Initializes a listener for the Konami Code to trigger a fun animation.
 */
function initializeEasterEgg() {
    const konamiCode = ['ArrowUp', 'ArrowUp', 'ArrowDown', 'ArrowDown', 'ArrowLeft', 'ArrowRight'];
    let sequence = [];

    let mouseX = window.innerWidth / 2;
    let mouseY = window.innerHeight / 2;
    document.addEventListener('mousemove', (e) => {
        mouseX = e.clientX;
        mouseY = e.clientY;
    });

    const secretAction = () => {
        console.log('💥 MOUSE-PLOSION ACTIVATED! 💥');

        triggerScreenShake(300);
        triggerConfetti(mouseX, mouseY);
    };

    const triggerScreenShake = (duration) => {
        document.body.style.transition = 'none';
        const startTime = Date.now();
        const shakeInterval = setInterval(() => {
            const elapsedTime = Date.now() - startTime;
            if (elapsedTime > duration) {
                clearInterval(shakeInterval);
                document.body.style.transform = '';
                return;
            }
            const x = (Math.random() - 0.5) * 20;
            const y = (Math.random() - 0.5) * 20;
            document.body.style.transform = `translate(${x}px, ${y}px)`;
        }, 50);
    };

    const triggerConfetti = (originX, originY) => {
        const confettiCount = 250;
        const colors = ['#f44336', '#e91e63', '#9c27b0', '#673ab7', '#3f51b5', '#2196f3', '#03a9f4', '#009688', '#4caf50', '#ffeb3b', '#ff9800'];

        for (let i = 0; i < confettiCount; i++) {
            const confetti = document.createElement('div');
            confetti.style.position = 'fixed';
            confetti.style.left = `${originX}px`;
            confetti.style.top = `${originY}px`;
            confetti.style.width = `${Math.random() * 12 + 6}px`;
            confetti.style.height = `${Math.random() * 12 + 6}px`;
            confetti.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
            confetti.style.opacity = '1';
            confetti.style.zIndex = '1000';
            confetti.style.transition = 'transform 1.5s ease-out, opacity 1.5s ease-out';
            confetti.style.transform = 'translate(-50%, -50%)';

            document.body.appendChild(confetti);

            const angle = Math.random() * 2 * Math.PI;
            const blastRadius = Math.random() * 400 + 200;
            const finalX = Math.cos(angle) * blastRadius;
            const finalY = Math.sin(angle) * blastRadius;
            const rotation = Math.random() * 1080 - 540;

            setTimeout(() => {
                confetti.style.transform = `translate(-50%, -50%) translate(${finalX}px, ${finalY}px) rotate(${rotation}deg)`;
                confetti.style.opacity = '0';
            }, 10);

            setTimeout(() => {
                confetti.remove();
            }, 1500);
        }
    };

    document.addEventListener('keyup', (e) => {
        sequence.push(e.key);
        sequence.splice(-konamiCode.length - 1, sequence.length - konamiCode.length);

        if (sequence.join('') === konamiCode.join('')) {
            secretAction();
        }
    });

}





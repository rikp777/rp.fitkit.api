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
export function renderSidenav(moduleInfo, navItems, activePath) {
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
export function initializeSidenavToggle() {
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
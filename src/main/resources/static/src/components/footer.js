import { appConfig } from '../config.js';
import { qs } from '../utils/dom.js';

/**
 * Renders the common application footer into the '#page-footer' element.
 */
export function renderFooter() {
    console.log('Rendering footer...');
    const footerElement = qs('#page-footer');
    if (!footerElement) return;

    const currentYear = new Date().getFullYear();
    const konamiString = '↑ ↑ ↓ ↓ ← →'; // Simplified for the hint

    // Set the complete inner HTML for the footer
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

    // Re-attach event listeners after setting innerHTML
    const devLink = qs('#dev-link');
    const hint = qs('#easter-egg-hint');
    const heart = qs('#heart');

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
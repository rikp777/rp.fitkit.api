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
    const konamiString = '↑ ↑ ↓ ↓ ← →';

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

            <div id="extra-footer-info" class="relative hidden opacity-0 transition-opacity duration-300 mt-4 text-xs text-gray-500 space-y-1">
                <p>
                    <strong>Tech Stack:</strong> Java 21 | Spring Boot 3 (WebFlux) | R2DBC | PostgreSQL | Vanilla JS (ESM) | Tailwind CSS
                </p>
                <p class="italic">
                    Note: This is a Proof of Concept, intentionally built with pure vanilla JS without 
                    <span id="framework-trigger" class="text-white font-semibold cursor-pointer">frameworks</span>
                    or build tools.
                </p>
                
                <div id="framework-jokes" class="hidden opacity-0 transition-opacity duration-300 absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-max p-2 rounded bg-gray-900 ring-1 ring-gray-700">
                    <p class="text-indigo-400">
                        ThePrimeagen told me not to use a framework haha
                    </p>
                    <p class="text-indigo-400">
                        ☝🏽 Isn't Tailwind a framework? shhhtfup
                    </p>
                </div>
            </div>
        </div>
        <div id="easter-egg-hint" class="fixed bottom-4 right-4 p-2 bg-gray-700 text-white text-xs rounded-md shadow-lg opacity-0 transition-opacity duration-300">
             ${konamiString}
        </div>
    `;

    const devLink = qs('#dev-link');
    const hint = qs('#easter-egg-hint');
    const heart = qs('#heart');
    const extraInfo = qs('#extra-footer-info');

    const frameworkTrigger = qs('#framework-trigger');
    const frameworkJokes = qs('#framework-jokes');
    let jokesHideTimer = null;

    if (devLink && hint) {
        devLink.addEventListener('mouseenter', () => hint.classList.remove('opacity-0'));
        devLink.addEventListener('mouseleave', () => hint.classList.add('opacity-0'));
    }

    if (footerElement && heart && extraInfo) {
        footerElement.addEventListener('mouseenter', () => {
            heart.style.animation = 'heartbeat 1s infinite';
            extraInfo.classList.remove('hidden');
            setTimeout(() => extraInfo.classList.remove('opacity-0'), 10);
        });
        footerElement.addEventListener('mouseleave', () => {
            heart.style.animation = '';
            extraInfo.classList.add('opacity-0');
            setTimeout(() => extraInfo.classList.add('hidden'), 300);
        });
    }

    if (frameworkTrigger && frameworkJokes) {
        const showJokes = () => {
            clearTimeout(jokesHideTimer);
            frameworkJokes.classList.remove('hidden');
            setTimeout(() => frameworkJokes.classList.remove('opacity-0'), 10);
        };
        const hideJokes = () => {
            frameworkJokes.classList.add('opacity-0');
            jokesHideTimer = setTimeout(() => frameworkJokes.classList.add('hidden'), 300);
        };

        frameworkTrigger.addEventListener('mouseenter', showJokes);
        frameworkTrigger.addEventListener('mouseleave', hideJokes);
        frameworkJokes.addEventListener('mouseenter', showJokes);
        frameworkJokes.addEventListener('mouseleave', hideJokes);
    }
}
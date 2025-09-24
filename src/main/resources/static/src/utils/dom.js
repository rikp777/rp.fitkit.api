export const qs = (selector, parent = document) => parent.querySelector(selector);
export const qsa = (selector, parent = document) => Array.from(parent.querySelectorAll(selector));

export function setButtonLoadingState(button, isLoading, loadingText = 'Loading...') {
    const spinner = qs('svg', button);
    const buttonTextEl = qs('span', button);
    const originalText = button.dataset.originalText || buttonTextEl.textContent;

    if (!button.dataset.originalText) {
        button.dataset.originalText = originalText;
    }

    if (isLoading) {
        button.disabled = true;
        spinner?.classList.remove('hidden');
        buttonTextEl.textContent = loadingText;
    } else {
        button.disabled = false;
        spinner?.classList.add('hidden');
        buttonTextEl.textContent = originalText;
    }
}
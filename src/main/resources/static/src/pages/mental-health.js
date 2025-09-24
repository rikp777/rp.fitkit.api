import { checkAuth } from '../services/authService.js';
import * as api from '../services/apiService.js';
import { renderSidenav, initializeSidenavToggle } from '../components/Sidenav.js';
import { qs, qsa } from '../utils/dom.js';

export function initMentalHealthPage() {
    checkAuth();

    const SECTION_TYPES = ['MORNING', 'AFTERNOON', 'EVENING', 'NIGHT'];
    const state = {
        journalCurrentPage: 0,
        journalPageSize: 5,
        auditCurrentPage: 0,
        auditPageSize: 5,
        targetDate: null,
        linkDataMap: new Map(),
        popoverHideTimer: null
    };
    const els = {};

    const clamp = (n, min, max) => Math.min(Math.max(n, min), max);
    const escapeHTML = (s = '') => String(s).replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[m]);
    const formatDate = (dateString) => new Intl.DateTimeFormat('en-US', { year: 'numeric', month: 'long', day: 'numeric', timeZone: 'UTC' }).format(new Date(dateString));
    const formatTimestamp = (iso) => iso ? new Date(iso).toLocaleTimeString('en-GB') : 'N/A';

    function parseSummaryForLinks(summary, linkDataMap) {
        if (!summary) return null;
        const safe = escapeHTML(summary);
        const linkRegex = /\[([^\]]+)\]\(log:(\d+)\)/g;

        return safe.replace(linkRegex, (_, anchorText, targetId) => {
            const trimmed = anchorText.trim();
            const key = `${trimmed}:${String(targetId)}`;
            const linkData = linkDataMap.get(key);
            if (!linkData) return escapeHTML(trimmed);

            const title = escapeHTML(linkData.remoteTitle || 'Link Preview');
            const snippet = escapeHTML(linkData.remoteSnippet || 'No preview available.');
            return `<button type="button" class="log-link" data-title="${title}" data-snippet="${snippet}" aria-haspopup="dialog">${escapeHTML(trimmed)}</button>`;
        });
    }

    function getTargetDateFromURL() {
        const pathSegments = window.location.pathname.split('/');
        const dateSegment = pathSegments.find(s => /^\d{4}-\d{2}-\d{2}$/.test(s));
        return dateSegment || new Date().toISOString().split('T')[0];
    }

    function modifyDate(dateString, days) {
        const date = new Date(dateString);
        const newDate = new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate() + days));
        return newDate.toISOString().split('T')[0];
    }

    function highlightCurrentSection() {
        SECTION_TYPES.forEach(type => {
            sectionEls(type).container?.classList.remove('current-section-highlight');
        });

        const today = new Date().toISOString().split('T')[0];
        if (state.targetDate !== today) {
            return;
        }

        const currentHour = new Date().getHours();
        let currentSectionType = '';

        if (currentHour >= 5 && currentHour < 12) {        // 5:00 AM - 11:59 AM
            currentSectionType = 'MORNING';
        } else if (currentHour >= 12 && currentHour < 17) { // 12:00 PM - 4:59 PM
            currentSectionType = 'AFTERNOON';
        } else if (currentHour >= 17 && currentHour < 21) { // 5:00 PM - 8:59 PM
            currentSectionType = 'EVENING';
        } else {                                            // 9:00 PM - 4:59 AM
            currentSectionType = 'NIGHT';
        }

        const currentContainer = sectionEls(currentSectionType).container;
        if (currentContainer) {
            currentContainer.classList.add('current-section-highlight');
        }
    }

    function renderListSkeleton(container, count) {
        let html = '';
        for (let i = 0; i < count; i++) {
            html += `<li class="border-b border-gray-700 pb-2 animate-pulse"><div class="h-4 bg-gray-700 rounded w-1/3 mb-2"></div><div class="h-3 bg-gray-700 rounded w-full"></div></li>`;
        }
        container.innerHTML = html;
    }

    function sectionEls(type) {
        const view = qs(`#view-${type}`);
        return {
            view,
            edit: qs(`#edit-${type}`),
            summaryText: qs(`#summary-text-${type}`),
            moodText: qs(`#mood-text-${type}`),
            textarea: qs(`#textarea-${type}`),
            moodInput: qs(`#mood-${type}`),
            saveBtn: qs(`#save-${type}`),
            cancelBtn: qs(`#cancel-${type}`),
            container: view?.parentElement
        };
    }
    function showSectionPlaceholder(type) {
        const { view, edit, summaryText, moodText } = sectionEls(type);
        if (!view || !summaryText || !moodText) return;

        summaryText.innerHTML = '<p class="italic">Click to add an entry...</p>';
        summaryText.dataset.summary = '';
        summaryText.classList.add('italic');
        moodText.textContent = '';
        moodText.dataset.mood = '';

        view.classList.remove('hidden');
        edit?.classList.add('hidden');
        view.removeAttribute('aria-busy');
    }

    function renderTodaysLogSkeleton() {
        SECTION_TYPES.forEach((type) => {
            const { view, edit, summaryText, moodText } = sectionEls(type);
            if (!view || !summaryText || !moodText) return;

            view.classList.remove('hidden');
            edit?.classList.add('hidden');
            view.setAttribute('aria-busy', 'true');

            summaryText.classList.remove('italic');
            summaryText.innerHTML = `
            <div class="animate-pulse space-y-2">
              <div class="h-4 bg-gray-700 rounded w-3/4"></div>
              <div class="h-4 bg-gray-700 rounded w-2/3"></div>
              <div class="h-4 bg-gray-700 rounded w-1/2"></div>
            </div>`;
            moodText.innerHTML = `
            <div class="animate-pulse h-4 bg-gray-700 rounded w-24 mt-2"></div>`;
        });
    }
    function renderMoodSkeleton() {
        const moodContainer = qs('#mood-stats-container');
        if (!moodContainer) return;

        let html = '';
        for (let i = 0; i < 3; i++) {
            html += `
            <div class="stat-card animate-pulse">
              <div class="h-8 w-8 mx-auto bg-gray-600 rounded-full"></div>
              <div class="h-3 w-12 mx-auto bg-gray-600 rounded mt-2"></div>
              <div class="h-5 w-4 mx-auto bg-gray-600 rounded mt-1"></div>
            </div>`;
        }
        moodContainer.innerHTML = html;
    }

    /**
     * Renders the mood statistics using pre-calculated counts from the backend.
     * @param {object} moodStats - The mood stats object from the API response,
     * e.g., { positiveCount: 2, neutralCount: 1, negativeCount: 0 }.
     */
    function renderMoodStats(moodStats) {
        const moodContainer = qs('#mood-stats-container');
        if (!moodContainer) return;

        const stats = moodStats || { positiveCount: 0, neutralCount: 0, negativeCount: 0 };

        moodContainer.innerHTML = `
        <div class="stat-card" aria-label="Positive moods">
            <span class="text-3xl">😊</span>
            <p class="text-sm mt-1">Positive</p>
            <p class="text-lg font-bold">${stats.positiveCount}</p>
        </div>
        <div class="stat-card" aria-label="Neutral moods">
            <span class="text-3xl">😐</span>
            <p class="text-sm mt-1">Neutral</p>
            <p class="text-lg font-bold">${stats.neutralCount}</p>
        </div>
        <div class="stat-card" aria-label="Negative moods">
            <span class="text-3xl">😔</span>
            <p class="text-sm mt-1">Negative</p>
            <p class="text-lg font-bold">${stats.negativeCount}</p>
        </div>
    `;
    }

    function updateNavButtons() {
        const today = new Date().toISOString().split('T')[0];
        els.todayBtn.disabled = state.targetDate === today;
    }
    function reloadAllData() {
        updateNavButtons();
        highlightCurrentSection();

        Promise.all([
            loadLogForDate(),
            fetchAndRenderJournals(0),
            fetchAndRenderAudits(0)
        ]);
    }

    function navigateToDate(newDate) {
        state.targetDate = newDate;
        history.pushState(null, '', `/mental-health/dashboard/${newDate}`);
        reloadAllData();
    }


    /**
     * Fetches the log data for the date stored in the state,
     * showing skeleton loaders first, then rendering the results.
     */
    async function loadLogForDate() {
        if (els.logTitle) {
            const today = new Date().toISOString().split('T')[0];
            els.logTitle.textContent = (state.targetDate === today)
                ? "Today's Log"
                : `Log for ${formatDate(state.targetDate)}`;
        }

        renderTodaysLogSkeleton();
        renderMoodSkeleton();

        try {
            const response = await api.getLogForDate(state.targetDate);

            if (response.status === 404) {
                SECTION_TYPES.forEach(showSectionPlaceholder);
                renderMoodStats([]);
                return;
            }
            if (!response.ok) {
                throw new Error('Failed to load log for the selected date');
            }

            const data = await response.json();

            state.linkDataMap = new Map(
                data.outgoingLinks?.map(link => [`${link.anchorText?.trim()}:${link.remoteEntityId}`, link]) || []
            );

            const sectionsByType = new Map(data.sections?.map(s => [s.sectionType, s]) || []);

            SECTION_TYPES.forEach((type) => {
                const section = sectionsByType.get(type);
                const { view, edit, summaryText, moodText } = sectionEls(type);
                if (!view || !summaryText || !moodText) return;

                if (section) {
                    const summary = section.summary || '';
                    const mood = section.mood || '';

                    summaryText.innerHTML = parseSummaryForLinks(summary, state.linkDataMap) || '<p class="italic">Click to add an entry...</p>';
                    summaryText.dataset.summary = summary;
                    summaryText.classList.toggle('italic', !summary);

                    moodText.textContent = mood ? `Mood: ${mood}` : '';
                    moodText.dataset.mood = mood;

                    view.classList.remove('hidden');
                    edit?.classList.add('hidden');
                    view.removeAttribute('aria-busy');
                } else {
                    showSectionPlaceholder(type);
                }
            });

            renderMoodStats(data.moodStats || []);
            initializePopoverSystem();

        } catch (err) {
            console.error('Could not pre-load log:', err);
            renderMoodStats([]);
            SECTION_TYPES.forEach(showSectionPlaceholder);
        }
    }

    async function fetchAndRenderJournals(page = 0) {
        state.journalCurrentPage = page;
        renderListSkeleton(els.journalList, state.journalPageSize);
        els.journalPagination.innerHTML = '';
        try {
            const response = await api.getRecentJournals(page, state.journalPageSize);
            if (!response.ok) throw new Error('Failed to fetch recent entries.');
            const pageData = await response.json();
            renderJournalList(pageData.content || []);
            renderJournalPagination(pageData);
        } catch (err) {
            console.error('Error loading recent journals:', err);
            els.journalList.innerHTML = `<li><p class="text-red-500">Could not load entries.</p></li>`;
        }
    }

    /**
     * Renders the list of recent journal entries into the DOM.
     * @param {Array<object>} entries - An array of journal entry objects from the API.
     */
    function renderJournalList(entries) {
        els.journalList.innerHTML = '';

        if (!entries.length && state.journalCurrentPage === 0) {
            els.journalList.innerHTML = `<li><p class="text-gray-400">No journal entries found.</p></li>`;
            return;
        }

        const fragment = document.createDocumentFragment();

        entries.forEach((entry) => {
            const li = document.createElement('li');
            li.className = 'border-b border-gray-700 last:border-b-0';

            const a = document.createElement('a');
            a.href = `/mental-health/dashboard/${entry.logDate}`;
            a.className = 'block p-3 rounded-lg hover:bg-gray-700/50 transition-colors';

            const sectionType = entry.sectionType
                ? entry.sectionType.charAt(0).toUpperCase() + entry.sectionType.slice(1).toLowerCase()
                : 'General';

            a.innerHTML = `
            <div class="flex items-center justify-between">
                <p class="font-semibold text-white">${escapeHTML(formatDate(entry.logDate))}</p>
                <span class="px-2 py-1 text-xs font-semibold rounded-full bg-indigo-500 text-white">
                    ${escapeHTML(sectionType)}
                </span>
            </div>
            <p class="text-sm text-gray-400 truncate mt-1">
                ${entry.summaryPreview ? escapeHTML(entry.summaryPreview) : 'No summary available.'}
            </p>
        `;

            li.appendChild(a);
            fragment.appendChild(li);
        });

        els.journalList.appendChild(fragment);
    }

    /**
     * Renders the pagination controls for the recent journals list.
     * This version includes page numbers for easier navigation through longer lists.
     * @param {object} pageData - The pagination object from the API.
     * (e.g., { number: 0, totalPages: 10, first: true, last: false })
     */
    function renderJournalPagination(pageData) {
        els.journalPagination.innerHTML = '';

        if (!pageData || pageData.totalPages <= 1) {
            return;
        }

        const current = pageData.number;
        const totalPages = pageData.totalPages;
        const isFirst = pageData.first;
        const isLast = pageData.last;

        const makeBtn = (text, targetPage, isDisabled = false, isActive = false) => {
            const btn = document.createElement('button');
            btn.innerHTML = text;
            btn.disabled = isDisabled;

            let baseClasses = 'px-3 py-1 rounded-md transition-colors ';
            if (isActive) {
                baseClasses += 'bg-indigo-600 text-white font-bold';
            } else if (isDisabled) {
                baseClasses += 'text-gray-500 cursor-not-allowed';
            } else {
                baseClasses += 'bg-gray-700 hover:bg-gray-600';
            }
            btn.className = baseClasses;

            if (!isDisabled) {
                btn.onclick = () => fetchAndRenderJournals(targetPage);
            }
            return btn;
        };

        const paginationElements = [];

        paginationElements.push(makeBtn('&laquo; First', 0, isFirst));
        paginationElements.push(makeBtn('&lsaquo; Prev', clamp(current - 1, 0, totalPages - 1), isFirst));

        const start = Math.max(0, current - 2);
        const end = Math.min(totalPages - 1, current + 2);

        if (start > 0) {
            paginationElements.push(makeBtn('...', start - 1));
        }
        for (let i = start; i <= end; i++) {
            paginationElements.push(makeBtn(String(i + 1), i, false, i === current));
        }
        if (end < totalPages - 1) {
            paginationElements.push(makeBtn('...', end + 1));
        }

        paginationElements.push(makeBtn('Next &rsaquo;', clamp(current + 1, 0, totalPages - 1), isLast));
        paginationElements.push(makeBtn('Last &raquo;', totalPages - 1, isLast));

        els.journalPagination.append(...paginationElements);
    }

    async function fetchAndRenderAudits(page = 0) {
        state.auditCurrentPage = page;
        renderListSkeleton(els.auditList, state.auditPageSize);
        els.auditPagination.innerHTML = '';
        try {
            const response = await api.getAuditLogForDate(state.targetDate, page, state.auditPageSize);
            if (!response.ok) throw new Error('Failed to fetch audit log.');
            const pageData = await response.json();
            renderAuditList(pageData.content || []);
            renderAuditPagination(pageData);
        } catch (err) {
            console.error('Error loading audit log:', err);
            els.auditList.innerHTML = `<li><p class="text-red-500">Could not load audit log.</p></li>`;
        }
    }

    function renderAuditList(audits) {
        els.auditList.innerHTML = '';
        if (!audits.length) {
            els.auditList.innerHTML = `<li><p class="text-gray-400">No audit activity found for this day.</p></li>`;
            return;
        }
        const fragment = document.createDocumentFragment();
        audits.forEach(audit => {
            const li = document.createElement('li');
            li.className = 'flex items-center justify-between p-2 rounded-md';
            const actionColor = audit.action === 'CREATE' ? 'text-green-400' : audit.action === 'UPDATE' ? 'text-yellow-400' : 'text-gray-400';
            li.innerHTML = `<div><span class="font-mono text-sm font-bold ${actionColor} mr-2">${audit.action}</span><span class="text-sm text-gray-300">${audit.entityType}</span></div><span class="text-xs text-gray-500 font-mono">${formatTimestamp(audit.timestamp)}</span>`;
            fragment.appendChild(li);
        });
        els.auditList.appendChild(fragment);
    }

    /**
     * Renders the pagination controls for the audit log list.
     * @param {object} pageData - The pagination object from the API.
     * (e.g., { number: 0, totalPages: 5, first: true, last: false })
     */
    function renderAuditPagination(pageData) {
        els.auditPagination.innerHTML = '';

        if (!pageData || pageData.totalPages <= 1) {
            return;
        }

        const current = pageData.number;
        const totalPages = pageData.totalPages;
        const isFirst = pageData.first;
        const isLast = pageData.last;


        const makeBtn = (text, targetPage, isDisabled) => {
            const btn = document.createElement('button');
            btn.innerHTML = text;
            btn.disabled = isDisabled;
            btn.className = `px-3 py-1 rounded-md transition-colors bg-gray-700 ${isDisabled ? 'text-gray-500 cursor-not-allowed' : 'hover:bg-gray-600'}`;

            if (!isDisabled) {
                btn.onclick = () => fetchAndRenderAudits(targetPage);
            }
            return btn;
        };

        const prevButton = makeBtn('&lsaquo; Prev', clamp(current - 1, 0, totalPages - 1), isFirst);

        const pageInfo = document.createElement('span');
        pageInfo.className = 'text-sm text-gray-400';
        pageInfo.textContent = `Page ${current + 1} of ${totalPages}`;

        const nextButton = makeBtn('Next &rsaquo;', clamp(current + 1, 0, totalPages - 1), isLast);

        els.auditPagination.append(prevButton, pageInfo, nextButton);
    }

    /**
     * Saves a specific log section (Morning, Afternoon, etc.) to the backend.
     * @param {string} sectionType - The type of section (e.g., 'MORNING').
     * @param {HTMLTextAreaElement} textarea - The textarea element with the summary.
     * @param {HTMLInputElement} moodInput - The input element with the mood.
     * @param {HTMLButtonElement} saveButton - The button that was clicked.
     */
    async function saveSection(sectionType, textarea, moodInput, saveButton) {
        const summary = textarea.value.trim();
        const mood = moodInput.value.trim();
        const body = { summary, mood };

        const originalButtonText = `Save ${sectionType.charAt(0) + sectionType.slice(1).toLowerCase()}`;
        saveButton.disabled = true;
        saveButton.textContent = 'Saving...';

        try {
            const response = await api.saveLogSection(state.targetDate, sectionType, body);

            if (!response.ok) {
                const errorData = await response.json().catch(() => null);
                throw new Error(errorData?.detail || 'Failed to save the section.');
            }

            saveButton.textContent = 'Saved ✔️';

            await Promise.all([
                loadLogForDate(),
                fetchAndRenderJournals(0),
                fetchAndRenderAudits(state.auditCurrentPage)
            ]);

        } catch (err) {
            console.error(`Error saving ${sectionType} section:`, err);
            alert(err.message || 'An unknown error occurred during save.');
            saveButton.textContent = 'Save failed';
        } finally {
            setTimeout(() => {
                saveButton.disabled = false;
                saveButton.textContent = originalButtonText;
            }, 1500);
        }
    }

    function initTodaySectionEditors() {
        SECTION_TYPES.forEach((type) => {
            const { view, edit, summaryText, moodText, textarea, moodInput, saveBtn, cancelBtn } =
                sectionEls(type);
            if (!view || !edit || !summaryText || !moodText || !textarea || !moodInput || !saveBtn || !cancelBtn) {
                return;
            }

            view.addEventListener('click', () => {
                textarea.value = summaryText.dataset.summary || '';
                moodInput.value = moodText.dataset.mood || '';
                view.classList.add('hidden');
                edit.classList.remove('hidden');
                textarea.focus();
            });

            cancelBtn.addEventListener('click', () => {
                edit.classList.add('hidden');
                view.classList.remove('hidden');
            });

            saveBtn.addEventListener('click', async () => {
                await saveSection(type, textarea, moodInput, saveBtn);
            });

            textarea.addEventListener('keydown', async (e) => {
                if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
                    await saveSection(type, textarea, moodInput, saveBtn);
                }
            });
        });
    }

    function initializePopoverSystem() {
        els.popover = qs('#link-popover');
        if (!els.popover) return;

        const titleEl = qs('#popover-title', els.popover);
        const contentEl = qs('#popover-content', els.popover);

        const show = (trigger) => {
            clearTimeout(state.popoverHideTimer);

            const title = trigger.dataset.title || 'Link Preview';
            const snippet = trigger.dataset.snippet || 'No preview available.';

            titleEl.textContent = title;
            contentEl.innerHTML = parseSummaryForLinks(snippet, state.linkDataMap) || escapeHTML(snippet);

            const rect = trigger.getBoundingClientRect();
            els.popover.style.left = `${rect.left + window.scrollX}px`;
            els.popover.style.top = `${rect.bottom + window.scrollY + 8}px`;
            els.popover.classList.remove('hidden');
            requestAnimationFrame(() => els.popover.classList.remove('opacity-0'));
        };

        const hide = () => {
            els.popover.classList.add('opacity-0');

            state.popoverHideTimer = setTimeout(() => {
                els.popover.classList.add('hidden');
            }, 180);
        };

        document.body.addEventListener('mouseenter', (e) => {
            const t = e.target;
            if (t && t.matches('.log-link')) show(t);
        }, true);

        document.body.addEventListener('mouseleave', (e) => {
            const t = e.target;
            if (t && t.matches('.log-link')) hide(t);
        }, true);

        document.body.addEventListener('focusin', (e) => {
            const t = e.target;
            if (t && t.matches('.log-link')) show(t);
        });

        document.body.addEventListener('focusout', (e) => {
            const t = e.target;
            if (t && t.matches('.log-link')) hide(t);
        });

        els.popover.addEventListener('mouseenter', () => clearTimeout(state.popoverHideTimer));
        els.popover.addEventListener('mouseleave', hide);
        window.addEventListener('scroll', hide, { passive: true });
        window.addEventListener('keydown', (e) => e.key === 'Escape' && hide());
    }


    els.logTitle = qs('#log-title');

    els.prevDayBtn = qs('#prev-day-btn');
    els.todayBtn = qs('#today-btn');
    els.nextDayBtn = qs('#next-day-btn');

    els.journalList = qs('#recent-journals-list');
    els.journalPagination = qs('#pagination-container');
    els.journalPageSize = qs('#page-size-selector');
    els.auditList = qs('#audit-log-list');
    els.auditPagination = qs('#audit-pagination-container');
    els.auditPageSize = qs('#audit-page-size-selector');
    els.auditTitle = qs('#audit-title');

    state.targetDate = getTargetDateFromURL();
    state.journalPageSize = parseInt(els.journalPageSize.value, 10);
    state.auditPageSize = parseInt(els.auditPageSize.value, 10);

    const navItems = [
        { icon: '🏠', label: 'Dashboard', href: '/mental-health/dashboard' },
        { icon: '📓', label: 'Journal', href: '#' },
        { icon: '🎯', label: 'Habit Tracker', href: '#' }
    ];
    renderSidenav({ title: 'FitKit', subtitle: 'Mental Health' }, navItems, window.location.pathname);
    initializeSidenavToggle();
    initializePopoverSystem();
    initTodaySectionEditors();

    els.prevDayBtn.addEventListener('click', () => {
        navigateToDate(modifyDate(state.targetDate, -1));
    });

    els.todayBtn.addEventListener('click', () => {
        const today = new Date().toISOString().split('T')[0];
        navigateToDate(today);
    });

    els.nextDayBtn.addEventListener('click', () => {
        navigateToDate(modifyDate(state.targetDate, 1));
    });


    els.journalPageSize.addEventListener('change', (e) => {
        state.journalPageSize = parseInt(e.target.value, 10);
        fetchAndRenderJournals(0);
    });
    els.auditPageSize.addEventListener('change', (e) => {
        state.auditPageSize = parseInt(e.target.value, 10);
        fetchAndRenderAudits(0);
    });

    reloadAllData();
}
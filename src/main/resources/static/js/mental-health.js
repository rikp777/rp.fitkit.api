(() => {
    'use strict';

    const SECTION_TYPES = ['MORNING', 'AFTERNOON', 'EVENING', 'NIGHT'];

    const state = {
        currentPage: 0,
        pageSize: 5,
        targetDate: null,
        linkDataMap: new Map(),
        timers: {
            popoverHide: null
        }
    };

    const els = {
        list: null,
        pagination: null,
        pageSize: null,
        popover: null,
        logTitle: null
    };

    const qs = (sel, root = document) => root.querySelector(sel);
    const qsa = (sel, root = document) => Array.from(root.querySelectorAll(sel));

    const clamp = (n, min, max) => Math.min(Math.max(n, min), max);

    const escapeHTML = (s = '') =>
        String(s)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;');

    const escapeAttr = (s = '') =>
        String(s)
            .replaceAll('&', '&amp;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;');

    function formatDate(dateString) {
        const date = new Date(dateString);
        const options = { year: 'numeric', month: 'long', day: 'numeric', timeZone: 'UTC' };
        return new Intl.DateTimeFormat('en-US', options).format(date);
    }

    function getTargetDateFromURL() {
        const pathSegments = window.location.pathname.split('/');
        const dateSegment = pathSegments.pop() || pathSegments.pop();

        const dateRegex = /^\d{4}-\d{2}-\d{2}$/;

        if (dateSegment && dateRegex.test(dateSegment)) {
            const parsedDate = new Date(dateSegment);
            if (!isNaN(parsedDate.getTime())) {
                return dateSegment;
            }
        }

        return new Date().toISOString().split('T')[0];
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
        </div>
      `;
            moodText.innerHTML = `
        <div class="animate-pulse h-4 bg-gray-700 rounded w-24 mt-2"></div>
      `;
        });
    }

    function renderListSkeleton(count) {
        let html = '';
        for (let i = 0; i < count; i++) {
            html += `
        <li class="border-b border-gray-700 pb-2 animate-pulse">
          <div class="h-4 bg-gray-700 rounded w-1/3 mb-2"></div>
          <div class="h-3 bg-gray-700 rounded w-full"></div>
        </li>
      `;
        }
        return html;
    }

    function renderMoodSkeleton() {
        let html = '';
        for (let i = 0; i < 3; i++) {
            html += `
        <div class="stat-card animate-pulse">
          <div class="h-8 w-8 mx-auto bg-gray-600 rounded-full"></div>
          <div class="h-3 w-12 mx-auto bg-gray-600 rounded mt-2"></div>
          <div class="h-5 w-4 mx-auto bg-gray-600 rounded mt-1"></div>
        </div>
      `;
        }
        return html;
    }

    function parseSummaryForLinks(summary, linkDataMap) {
        if (!summary) return null;

        const safe = escapeHTML(summary);
        const linkRegex = /\[([^\]]+)\]\(log:(\d+)\)/g;

        return safe.replace(linkRegex, (_, anchorText, targetId) => {
            const trimmed = anchorText.trim();
            const key = `${trimmed}:${String(targetId)}`;
            const linkData = linkDataMap.get(key);

            if (!linkData) {
                return escapeHTML(trimmed);
            }

            const title = escapeAttr(linkData.remoteTitle || 'Link Preview');
            const snippet = escapeAttr(linkData.remoteSnippet || 'No preview available.');

            return `<buttontype="button" class="log-link" data-title="${title}" data-snippet="${snippet}" aria-haspopup="dialog">${escapeHTML(trimmed)}</button>`;
        });
    }

    function initializePopoverSystem() {
        els.popover = qs('#link-popover');
        if (!els.popover) return;

        const titleEl = qs('#popover-title', els.popover);
        const contentEl = qs('#popover-content', els.popover);

        const show = (trigger) => {
            clearTimeout(state.timers.popoverHide);

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
            state.timers.popoverHide = setTimeout(() => {
                els.popover.classList.add('hidden');
            }, 180);
        };

        document.body.addEventListener(
            'mouseenter',
            (e) => {
                const t = e.target;
                if (t && t.matches('.log-link')) show(t);
            },
            true
        );

        document.body.addEventListener(
            'mouseleave',
            (e) => {
                const t = e.target;
                if (t && t.matches('.log-link')) hide();
            },
            true
        );

        document.body.addEventListener('focusin', (e) => {
            const t = e.target;
            if (t && t.matches('.log-link')) show(t);
        });

        document.body.addEventListener('focusout', (e) => {
            const t = e.target;
            if (t && t.matches('.log-link')) hide();
        });

        els.popover.addEventListener('mouseenter', () => clearTimeout(state.timers.popoverHide));
        els.popover.addEventListener('mouseleave', hide);
        window.addEventListener('scroll', hide, { passive: true });
        window.addEventListener('keydown', (e) => e.key === 'Escape' && hide());
    }

    function highlightCurrentSection() {
        const today = new Date().toISOString().split('T')[0];
        if (state.targetDate !== today) {
            SECTION_TYPES.forEach(type => {
                sectionEls(type).container?.classList.remove('current-section-highlight');
            });
            return;
        }

        const currentHour = new Date().getHours();
        let currentSectionType = '';

        if (currentHour >= 5 && currentHour < 12) {
            currentSectionType = 'MORNING';
        } else if (currentHour >= 12 && currentHour < 17) {
            currentSectionType = 'AFTERNOON';
        } else if (currentHour >= 17 && currentHour < 21) {
            currentSectionType = 'EVENING';
        } else {
            currentSectionType = 'NIGHT';
        }

        SECTION_TYPES.forEach(type => {
            const { container } = sectionEls(type);
            if (container) {
                if (type === currentSectionType) {
                    container.classList.add('current-section-highlight');
                } else {
                    container.classList.remove('current-section-highlight');
                }
            }
        });
    }


    async function loadLogForDate() {
        if (els.logTitle) {
            const today = new Date().toISOString().split('T')[0];
            if (state.targetDate === today) {
                els.logTitle.textContent = "Today's Log";
            } else {
                els.logTitle.textContent = `Log for ${formatDate(state.targetDate)}`;
            }
        }

        const moodContainer = qs('#mood-stats-container');
        if (moodContainer) moodContainer.innerHTML = renderMoodSkeleton();
        renderTodaysLogSkeleton();

        try {
            const res = await fetchWithAuth(`/api/v1/logbook/${state.targetDate}`);
            if (!res.ok) {

                if (res.status === 404) {
                    SECTION_TYPES.forEach(showSectionPlaceholder);
                    renderMoodStats([]);
                    return;
                }
                throw new Error('Failed to load log for the selected date');
            }
            const data = await res.json();

            state.linkDataMap = new Map();
            if (Array.isArray(data.outgoingLinks)) {
                data.outgoingLinks.forEach((link) => {
                    const k = `${(link.anchorText || '').trim()}:${String(link.remoteEntityId || '')}`;
                    state.linkDataMap.set(k, link);
                });
            }

            const byType = new Map(
                (Array.isArray(data.sections) ? data.sections : []).map((s) => [s.sectionType, s])
            );

            SECTION_TYPES.forEach((type) => {
                const section = byType.get(type);
                const { view, edit, summaryText, moodText } = sectionEls(type);
                if (!view || !summaryText || !moodText) return;

                if (!section) {
                    showSectionPlaceholder(type);
                    return;
                }

                const summary = section.summary || '';
                const mood = section.mood || '';
                const summaryHtml =
                    parseSummaryForLinks(summary, state.linkDataMap) ||
                    '<p class="italic">Click to add an entry...</p>';
                summaryText.innerHTML = summaryHtml;
                summaryText.dataset.summary = summary;
                summaryText.classList.toggle('italic', !summary);

                if (mood) {
                    moodText.textContent = `Mood: ${mood}`;
                    moodText.dataset.mood = mood;
                } else {
                    moodText.textContent = '';
                    moodText.dataset.mood = '';
                }

                view.classList.remove('hidden');
                edit?.classList.add('hidden');
                view.removeAttribute('aria-busy');
            });

            renderMoodStats(Array.isArray(data.sections) ? data.sections : []);
            initializePopoverSystem();
        } catch (err) {
            console.error('Could not pre-load log:', err);
            renderMoodStats([]);
            SECTION_TYPES.forEach(showSectionPlaceholder);
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

    async function saveSection(sectionType, textarea, moodInput, saveButton) {
        const summary = textarea.value;
        const mood = moodInput.value;

        const original = saveButton.textContent;
        saveButton.disabled = true;
        saveButton.textContent = 'Saving...';

        try {
            const body = { summary, mood };
            const res = await fetchWithAuth(`/api/v1/logbook/${state.targetDate}/${sectionType}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });

            if (!res.ok) throw new Error('Failed to save the section.');

            saveButton.textContent = 'Saved ✔️';

            await Promise.all([loadLogForDate(), fetchAndRenderJournals(0)]);

        } catch (err) {
            console.error(`Error saving ${sectionType} section:`, err);
            alert(err.message || 'Save failed.');
            saveButton.textContent = 'Save failed';
        } finally {
            setTimeout(() => {
                saveButton.disabled = false;
                saveButton.textContent = original;
            }, 1200);
        }
    }

    async function fetchAndRenderJournals(page) {
        if (!els.list || !els.pagination) return;

        state.currentPage = page;
        els.list.innerHTML = renderListSkeleton(state.pageSize);
        els.pagination.innerHTML = '';

        try {
            const res = await fetchWithAuth(
                `/api/v1/logbook?page=${page}&size=${state.pageSize}&sort=logDate,desc`
            );
            if (!res.ok) throw new Error('Failed to fetch recent entries.');

            const pageData = await res.json();
            renderJournalList(Array.isArray(pageData.content) ? pageData.content : []);
            renderPagination(pageData);
        } catch (err) {
            console.error('Error loading recent journals:', err);
            els.list.innerHTML = `<li><p class="text-red-500">Could not load entries.</p></li>`;
        }
    }

    function renderJournalList(entries) {
        els.list.innerHTML = '';

        if (!entries.length && state.currentPage === 0) {
            els.list.innerHTML = `<li><p class="text-gray-400">No journal entries found.</p></li>`;
            return;
        }

        entries.forEach((entry) => {
            const li = document.createElement('li');
            li.className = 'border-b border-gray-700 pb-2';

            const sectionType = entry.sectionType
                ? entry.sectionType.charAt(0).toUpperCase() + entry.sectionType.slice(1).toLowerCase()
                : 'General';

            li.innerHTML = `
        <div class="flex items-center justify-between">
          <p class="font-semibold text-white">${escapeHTML(formatDate(entry.logDate))}</p>
          <span class="px-2 py-1 text-xs font-semibold rounded-full bg-indigo-500 text-white">${escapeHTML(
                sectionType
            )}</span>
        </div>
        <p class="text-sm text-gray-400 truncate mt-1">${
                entry.summaryPreview ? escapeHTML(entry.summaryPreview) : 'No summary available.'
            }</p>
      `;

            els.list.appendChild(li);
        });
    }

    function renderPagination(pageData) {
        els.pagination.innerHTML = '';
        if (!pageData || typeof pageData.totalPages !== 'number' || pageData.totalPages <= 1) return;

        const current = pageData.number ?? state.currentPage;
        const totalPages = pageData.totalPages ?? 1;
        const isFirst = pageData.first ?? current === 0;
        const isLast = pageData.last ?? current >= totalPages - 1;

        const makeBtn = (html, targetPage, disabled = false, active = false) => {
            const btn = document.createElement('button');
            btn.innerHTML = html;
            btn.disabled = disabled;
            btn.className = [
                'px-3 py-1 rounded-md transition-colors',
                disabled ? 'text-gray-500 cursor-not-allowed' : 'hover:bg-gray-600',
                active ? 'bg-indigo-600 text-white font-bold' : 'bg-gray-700'
            ].join(' ');
            if (!disabled) btn.onclick = () => fetchAndRenderJournals(targetPage);
            return btn;
        };

        els.pagination.appendChild(makeBtn('&laquo; First', 0, isFirst));
        els.pagination.appendChild(makeBtn('&lsaquo; Prev', clamp(current - 1, 0, totalPages - 1), isFirst));

        const start = Math.max(0, current - 2);
        const end = Math.min(totalPages - 1, current + 2);

        if (start > 0) els.pagination.appendChild(makeBtn('…', start - 1));
        for (let i = start; i <= end; i++) {
            els.pagination.appendChild(makeBtn(String(i + 1), i, false, i === current));
        }
        if (end < totalPages - 1) els.pagination.appendChild(makeBtn('…', end + 1));

        els.pagination.appendChild(
            makeBtn('Next &rsaquo;', clamp(current + 1, 0, totalPages - 1), isLast)
        );
        els.pagination.appendChild(makeBtn('Last &raquo;', totalPages - 1, isLast));
    }

    function renderMoodStats(sections) {
        const moodContainer = qs('#mood-stats-container');
        if (!moodContainer) return;

        //todo remove and replace with machine learning in be
        const moodCategories = {
            Positive: ['Opgelucht', 'Productief', 'Tevreden', 'Energiek', 'Blij', 'Zelfverzekerd', 'Gemotiveerd', 'Inspiratie'],
            Neutral: ['Rustig', 'Afwachtend', 'Strategisch'],
            Negative: ['Gefrustreerd', 'Geduldig']
        };

        const counts = { Positive: 0, Neutral: 0, Negative: 0 };

        const uniqueMoods = [...new Set((sections || []).map((s) => s.mood).filter(Boolean))];
        uniqueMoods.forEach((mood) => {
            if (moodCategories.Positive.includes(mood)) counts.Positive++;
            else if (moodCategories.Neutral.includes(mood)) counts.Neutral++;
            else if (moodCategories.Negative.includes(mood)) counts.Negative++;
        });

        moodContainer.innerHTML = `
      <div class="stat-card" aria-label="Positive moods">
        <span class="text-3xl">😊</span>
        <p class="text-sm mt-1">Positive</p>
        <p class="text-lg font-bold">${counts.Positive}</p>
      </div>
      <div class="stat-card" aria-label="Neutral moods">
        <span class="text-3xl">😐</span>
        <p class="text-sm mt-1">Neutral</p>
        <p class="text-lg font-bold">${counts.Neutral}</p>
      </div>
      <div class="stat-card" aria-label="Negative moods">
        <span class="text-3xl">😔</span>
        <p class="text-sm mt-1">Negative</p>
        <p class="text-lg font-bold">${counts.Negative}</p>
      </div>
    `;
    }

    async function handlePageSizeChange(e) {
        const raw = e?.target?.value ?? els.pageSize?.value;
        const parsed = parseInt(String(raw), 10);
        state.pageSize = Number.isFinite(parsed) && parsed > 0 ? parsed : 5;
        await fetchAndRenderJournals(0);
    }

    document.addEventListener('DOMContentLoaded', async () => {
        els.list = qs('#recent-journals-list');
        els.pagination = qs('#pagination-container');
        els.pageSize = qs('#page-size-selector');
        els.logTitle = qs('#log-title');

        state.targetDate = getTargetDateFromURL();

        if (els.pageSize) {
            state.pageSize = parseInt(els.pageSize.value, 10) || 5;
            els.pageSize.addEventListener('change', handlePageSizeChange);
        }

        initTodaySectionEditors();
        initializePopoverSystem();

        await loadLogForDate();
        await fetchAndRenderJournals(state.currentPage);

        highlightCurrentSection();
    });
})();
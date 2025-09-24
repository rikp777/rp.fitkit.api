(() => {
    'use strict';

    const state = {
        currentPage: 0,
        pageSize: 5,
        targetDate: null
    };

    const els = {
        list: null,
        pagination: null,
        pageSize: null,
        title: null
    };

    const qs = (sel) => document.querySelector(sel);
    const clamp = (n, min, max) => Math.min(Math.max(n, min), max);

    function formatTimestamp(isoString) {
        if (!isoString) return 'N/A';
        const date = new Date(isoString);
        return date.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    }

    function renderAuditSkeleton(count) {
        let html = '';
        for (let i = 0; i < count; i++) {
            html += `
            <li class="p-2 animate-pulse">
              <div class="flex justify-between items-center">
                <div class="h-4 bg-gray-700 rounded w-3/5"></div>
                <div class="h-4 bg-gray-700 rounded w-1/5"></div>
              </div>
            </li>
          `;
        }
        els.list.innerHTML = html;
    }

    function renderAuditList(audits) {
        els.list.innerHTML = '';
        if (!audits.length) {
            els.list.innerHTML = `<li><p class="text-gray-400">No audit activity found for this day.</p></li>`;
            return;
        }

        const fragment = document.createDocumentFragment();
        audits.forEach(audit => {
            const li = document.createElement('li');
            li.className = 'flex items-center justify-between p-2 rounded-md';

            const actionColor = audit.action === 'CREATE' ? 'text-green-400' :
                audit.action === 'UPDATE' ? 'text-yellow-400' :
                    audit.action === 'DELETE' ? 'text-red-400' : 'text-gray-400';

            li.innerHTML = `
                <div class="flex-1">
                    <span class="font-mono text-sm font-bold ${actionColor} mr-2">${audit.action}</span>
                    <span class="text-sm text-gray-300">${audit.entityType}</span>
                </div>
                <span class="text-xs text-gray-500 font-mono">${formatTimestamp(audit.timestamp)}</span>
            `;
            fragment.appendChild(li);
        });
        els.list.appendChild(fragment);
    }

    function renderAuditPagination(pageData) {
        els.pagination.innerHTML = '';
        if (pageData.totalPages <= 1) return;

        const current = pageData.number;
        const totalPages = pageData.totalPages;
        const isFirst = pageData.first;
        const isLast = pageData.last;

        const makeBtn = (html, targetPage, disabled = false) => {
            const btn = document.createElement('button');
            btn.innerHTML = html;
            btn.disabled = disabled;
            btn.className = 'px-3 py-1 rounded-md transition-colors ' +
                (disabled ? 'text-gray-500 cursor-not-allowed' : 'bg-gray-700 hover:bg-gray-600');
            if (!disabled) btn.onclick = () => fetchAndRenderAudits(targetPage);
            return btn;
        };

        els.pagination.append(makeBtn('&lsaquo; Prev', clamp(current - 1, 0, totalPages - 1), isFirst));
        els.pagination.append(
            document.createTextNode(` Page ${current + 1} of ${totalPages} `)
        );
        els.pagination.append(makeBtn('Next &rsaquo;', clamp(current + 1, 0, totalPages - 1), isLast));
    }


    async function fetchAndRenderAudits(page = 0) {
        if (!els.list) return;

        state.currentPage = page;
        renderAuditSkeleton(state.pageSize);
        els.pagination.innerHTML = '';

        try {
            const res = await fetchWithAuth(
                `/api/v1/audit/by-date?date=${state.targetDate}&page=${page}&size=${state.pageSize}&sort=timestamp,desc`
            );
            if (!res.ok) throw new Error('Failed to fetch audit log.');

            const pageData = await res.json();
            renderAuditList(pageData.content || []);
            renderAuditPagination(pageData);

        } catch (err) {
            console.error('Error loading audit log:', err);
            els.list.innerHTML = `<li><p class="text-red-500">Could not load audit log.</p></li>`;
        }
    }

    function handlePageSizeChange(e) {
        state.pageSize = parseInt(e.target.value, 10) || 5;
        fetchAndRenderAudits(0);
    }

    window.initializeAuditModule = (targetDate) => {
        els.list = qs('#audit-log-list');
        els.pagination = qs('#audit-pagination-container');
        els.pageSize = qs('#audit-page-size-selector');
        els.title = qs('#audit-title');

        if (!els.list) return;

        state.targetDate = targetDate;

        const today = new Date().toISOString().split('T')[0];
        els.title.textContent = (targetDate === today) ? "Today's Audit Log" : "Audit Log";

        els.pageSize.addEventListener('change', handlePageSizeChange);
        state.pageSize = parseInt(els.pageSize.value, 10);

        fetchAndRenderAudits(0);
    };

    window.refreshAudits = () => {
        if (els.list) {
            fetchAndRenderAudits(state.currentPage);
        }
    };

})();
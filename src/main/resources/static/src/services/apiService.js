import { fetchWithAuth } from './authService.js';

export const loginUser = (username, password) => {
    const details = new URLSearchParams({ username, password });
    return fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: details
    });
};
export const registerUser = (username, email, password) => {
    const registrationData = { username, email, password };
    return fetch('/api/v1/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(registrationData)
    });
};
export const resetPasswordWithCode = (requestBody) => {
    return fetch('/api/v1/auth/reset-password-with-code', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody)
    });
};
export const generateRecoveryCodes = () => fetchWithAuth('/api/v1/auth/generate-recovery-codes', { method: 'POST' });
export const getUserDetails = () => fetchWithAuth('/api/v1/auth/me');

// Dashboard APIs
export const getDashboardStats = () => fetchWithAuth('/api/v1/logbook/stats/total-count');

// Mental Health Log APIs
export const getLogForDate = (date) => fetchWithAuth(`/api/v1/logbook/${date}`);
export const getRecentJournals = (page, size) => fetchWithAuth(`/api/v1/logbook?page=${page}&size=${size}&sort=logDate,desc`);
export const saveLogSection = (date, type, body) => {
    return fetchWithAuth(`/api/v1/logbook/${date}/${type}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
};
export const getAuditLogForDate = (date, page, size) => fetchWithAuth(`/api/v1/audit/by-date?date=${date}&page=${page}&size=${size}&sort=timestamp,desc`);
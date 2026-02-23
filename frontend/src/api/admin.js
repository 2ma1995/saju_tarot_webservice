// admin.js
import apiClient from './apiClient';

export function getAllUsers(params = {}) {
    return apiClient.get('/admin/users', { params }).then(r => r.data);
}

export function changeUserRole(userId, newRole) {
    return apiClient.put(`/admin/users/${userId}/role`, null, { params: { newRole } }).then(r => r.data);
}

export function deactivateUser(userId) {
    return apiClient.put(`/admin/users/${userId}/deactivate`).then(r => r.data);
}

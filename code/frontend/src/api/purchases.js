import api from './axios';

const BASE = '/api/v1/purchases';

export const getPurchases = () => api.get(BASE);
export const getPurchase = (id) => api.get(`${BASE}/${id}`);
export const createPurchase = (data) => api.post(BASE, data);
export const updatePurchase = (id, data) => api.put(`${BASE}/${id}`, data);
export const receivePurchase = (id, data) => api.post(`${BASE}/${id}/receive`, data);
export const approvePurchase = (id) => api.post(`${BASE}/${id}/approve`);
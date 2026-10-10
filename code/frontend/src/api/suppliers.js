import api from './axios';

const BASE = '/api/v1/suppliers';

export const getSuppliers = () => api.get(BASE);
export const getSupplier = (id) => api.get(`${BASE}/${id}`);
export const createSupplier = (data) => api.post(BASE, data);
export const updateSupplier = (id, data) => api.put(`${BASE}/${id}`, data);
export const deleteSupplier = (id) => api.delete(`${BASE}/${id}`);
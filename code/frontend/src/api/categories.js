import api from './axios';

const BASE = '/api/v1/categories';

export const getCategories = () => api.get(BASE);
export const getCategory = (id) => api.get(`${BASE}/${id}`);
export const createCategory = (data) => api.post(BASE, data);
export const updateCategory = (id, data) => api.put(`${BASE}/${id}`, data);
export const deleteCategory = (id) => api.delete(`${BASE}/${id}`);
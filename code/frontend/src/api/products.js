import api from './axios';

const BASE = '/api/v1/products';

export const getProducts = (params) => api.get(BASE, { params });
export const getProduct = (id) => api.get(`${BASE}/${id}`);
export const getProductsAdmin = (params) => api.get(`${BASE}/admin`, { params });
export const getProductAdmin = (id) => api.get(`${BASE}/admin/${id}`);
export const createProduct = (data) => api.post(BASE, data);
export const updateProduct = (id, data) => api.put(`${BASE}/${id}`, data);
export const deleteProduct = (id) => api.delete(`${BASE}/${id}`);
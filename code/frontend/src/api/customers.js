import api from './axios';

const BASE = '/api/v1/customers';

export const getCustomers = () => api.get(BASE);
export const getCustomer = (id) => api.get(`${BASE}/${id}`);
export const createCustomer = (data) => api.post(BASE, data);
export const updateCustomer = (id, data) => api.put(`${BASE}/${id}`, data);
export const deleteCustomer = (id) => api.delete(`${BASE}/${id}`);
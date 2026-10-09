import api from './axios';

const BASE = '/api/v1/orders';

export const getOrders = () => api.get(BASE);
export const getOrder = (id) => api.get(`${BASE}/${id}`);
export const createOrder = (data) => api.post(BASE, data);
export const updateOrder = (id, data) => api.put(`${BASE}/${id}`, data);
export const updateOrderStatus = (id, statusData) =>
  api.post(`${BASE}/${id}/status`, statusData);
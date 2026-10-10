import api from './axios';

const BASE = '/api/v1/stock-movements';

export const getStockMovements = () => api.get(BASE);
export const getStockMovementsByProduct = (productId) =>
  api.get(`${BASE}/products/${productId}`);
export const createStockMovement = (data) => api.post(BASE, data);
export const approveStockMovement = (id) => api.post(`${BASE}/${id}/approve`);
export const rejectStockMovement = (id) => api.post(`${BASE}/${id}/reject`);

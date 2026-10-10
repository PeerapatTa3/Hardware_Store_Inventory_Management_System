import api from './axios';

export const getInventoryByProduct = (productId) =>
  api.get(`/api/v1/inventory/products/${productId}`);

export const updateInventory = (productId, data) =>
  api.put(`/api/v1/inventory/products/${productId}`, data);
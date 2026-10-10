import api from './axios';

export const getDashboardSummary = () => {
  return api.get('/api/v1/dashboard/summary');
};
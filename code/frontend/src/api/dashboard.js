import api from './axios';

export const getDashboardSummary = () => {
  return api.get('/dashboard/summary');
};
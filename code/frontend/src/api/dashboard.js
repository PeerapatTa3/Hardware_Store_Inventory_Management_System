import api from './config';

export const getDashboardSummary = () => {
  return api.get('/dashboard/summary');
};
import api from './axios';

export const login = (username, password) =>
  api.post('/api/auth/login', { username, password });

export const getCurrentUser = () => {
  const token = localStorage.getItem('token');
  const username = localStorage.getItem('username');
  const role = localStorage.getItem('role');
  return token ? { token, username, role } : null;
};

export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('username');
  localStorage.removeItem('role');
};
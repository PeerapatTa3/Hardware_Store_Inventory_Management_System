import React, { createContext, useContext, useState, useCallback } from 'react';
import { login as apiLogin, logout as apiLogout } from '../api/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const role = localStorage.getItem('role');
    return token ? { token, username, role } : null;
  });

  const login = useCallback(async (username, password) => {
    const res = await apiLogin(username, password);
    const { token, username: uname, role } = res.data;
    localStorage.setItem('token', token);
    localStorage.setItem('username', uname);
    localStorage.setItem('role', role);
    setUser({ token, username: uname, role });
    return res.data;
  }, []);

  const logout = useCallback(() => {
    apiLogout();
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
import React, { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage, Notice } from './ResourcePage';

function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (user) return <Navigate to="/" replace />;

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username.trim(), password);
      navigate('/', { replace: true });
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="login-page">
      <div className="login-shell">
        <section className="login-intro">
          <div className="brand-line"><span className="brand-mark">H</span> Hardware Store</div>
          <h1>Inventory, in good order.</h1>
          <p>One workspace for products, stock, suppliers and every order moving through your store.</p>
        </section>
        <form className="login-card" onSubmit={submit}>
          <h2>Welcome back</h2>
          <p className="page-subtitle">Sign in to your store workspace.</p>
          <Notice>{error}</Notice>
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input id="username" className="form-control" autoComplete="username" required value={username}
              onChange={(event) => setUsername(event.target.value)} />
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input id="password" className="form-control" type="password" autoComplete="current-password" required value={password}
              onChange={(event) => setPassword(event.target.value)} />
          </div>
          <button className="btn btn-primary" type="submit" disabled={loading}>{loading ? 'Signing in…' : 'Sign in'}</button>
        </form>
      </div>
    </main>
  );
}

export default LoginPage;
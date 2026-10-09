import React, { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navItems = [
  { section: 'Workspace' },
  { path: '/', label: 'Overview', icon: 'OV', end: true },
  { section: 'Catalog' },
  { path: '/products', label: 'Products', icon: 'PR' },
  { path: '/categories', label: 'Categories', icon: 'CA' },
  { path: '/suppliers', label: 'Suppliers', icon: 'SU' },
  { path: '/customers', label: 'Customers', icon: 'CU' },
  { section: 'Stockroom' },
  { path: '/inventory', label: 'Inventory', icon: 'IN' },
  { path: '/stock-movements', label: 'Movements', icon: 'MV' },
  { section: 'Operations' },
  { path: '/purchases', label: 'Purchases', icon: 'PU' },
  { path: '/orders', label: 'Sales orders', icon: 'SO' },
];

function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const closeMenu = () => setMenuOpen(false);

  return (
    <div className="layout">
      <div className={`mobile-overlay ${menuOpen ? 'visible' : ''}`} onClick={closeMenu} />
      <aside className={`sidebar ${menuOpen ? 'open' : ''}`}>
        <div className="sidebar-header">
          <div className="brand-line"><span className="brand-mark">H</span> Hardware Store</div>
          <p className="brand-caption">Inventory workspace</p>
        </div>
        <nav className="sidebar-nav">
          {navItems.map((item, i) =>
            item.section ? (
              <div key={i} className="nav-section">{item.section}</div>
            ) : (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.end}
                onClick={closeMenu}
                className={({ isActive }) => isActive ? 'active' : ''}
              >
                <span className="nav-icon">{item.icon}</span>
                <span>{item.label}</span>
              </NavLink>
            )
          )}
        </nav>
        <div className="sidebar-footer">
          <div className="sidebar-user">
            Signed in as
            <strong>{user?.username || 'User'}{user?.role ? ` · ${user.role}` : ''}</strong>
          </div>
          <button onClick={handleLogout}>Sign out</button>
        </div>
      </aside>
      <main className="main-content">
        <div className="mobile-brand">
          <button className="mobile-menu" onClick={() => setMenuOpen(true)} aria-label="Open navigation">MENU</button>
          <span className="brand-mark">H</span>
          <strong>Hardware Store</strong>
        </div>
        <Outlet />
      </main>
    </div>
  );
}

export default Layout;
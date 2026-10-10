import React, { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const getNavItems = (role) => {
  const allItems = [
    { section: 'Workspace', roles: ['OWNER', 'STOCK_MANAGER', 'CASHIER'] },
    { path: '/', label: 'Overview', icon: 'OV', end: true, roles: ['OWNER', 'STOCK_MANAGER', 'CASHIER'] },
    { section: 'Catalog', roles: ['OWNER', 'STOCK_MANAGER', 'CASHIER'] },
    { path: '/products', label: 'Products', icon: 'PR', roles: ['OWNER', 'STOCK_MANAGER', 'CASHIER'] },
    { path: '/categories', label: 'Categories', icon: 'CA', roles: ['OWNER', 'STOCK_MANAGER'] },
    { path: '/suppliers', label: 'Suppliers', icon: 'SU', roles: ['OWNER', 'STOCK_MANAGER'] },
    { path: '/customers', label: 'Customers', icon: 'CU', roles: ['OWNER', 'CASHIER'] },
    { section: 'Stockroom', roles: ['OWNER', 'STOCK_MANAGER'] },
    { path: '/inventory', label: 'Inventory', icon: 'IN', roles: ['OWNER', 'STOCK_MANAGER'] },
    { path: '/stock-movements', label: 'Movements', icon: 'MV', roles: ['OWNER', 'STOCK_MANAGER'] },
    { section: 'Operations', roles: ['OWNER', 'STOCK_MANAGER', 'CASHIER'] },
    { path: '/purchases', label: 'Purchases', icon: 'PU', roles: ['OWNER', 'STOCK_MANAGER'] },
    { path: '/orders', label: 'Sales orders', icon: 'SO', roles: ['OWNER', 'CASHIER'] },
  ];

  const userRole = role || 'OWNER';
  
  // Filter items by role
  const filtered = allItems.filter(item => item.roles.includes(userRole));
  
  // Remove empty sections
  return filtered.filter((item, index, array) => {
    if (item.section) {
      // Keep section only if the NEXT item is NOT a section (meaning it has items)
      const nextItem = array[index + 1];
      return nextItem && !nextItem.section;
    }
    return true;
  });
};

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
          {getNavItems(user?.role).map((item, i) =>
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
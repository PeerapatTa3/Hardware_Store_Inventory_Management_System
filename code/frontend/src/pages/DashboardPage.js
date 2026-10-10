import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getDashboardSummary } from '../api/dashboard';
import { approveStockMovement, rejectStockMovement } from '../api/stockMovements';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';
import { formatDate, formatMoney, PageHeader, Notice, getErrorMessage } from './ResourcePage';

const shortcuts = [
  ['/products/new', 'PR', 'Add product', ['OWNER', 'STOCK_MANAGER']],
  ['/purchases/new', 'PU', 'Receive stock', ['OWNER', 'STOCK_MANAGER']],
  ['/orders/new', 'SO', 'Create sales order', ['OWNER', 'CASHIER']],
  ['/inventory', 'IN', 'Review inventory', ['OWNER', 'STOCK_MANAGER']],
];

function DashboardPage() {
  const { user } = useAuth();
  const role = user?.role;
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState('');
  const [refresh, setRefresh] = useState(0);
  const handleApprove = async (id) => {
    try { await approveStockMovement(id); toast.success('Approved successfully'); setRefresh(r => r + 1); } catch (err) { setError(getErrorMessage(err)); }
  };
  const handleReject = async (id) => {
    try { await rejectStockMovement(id); toast.success('Rejected successfully'); setRefresh(r => r + 1); } catch (err) { setError(getErrorMessage(err)); }
  };

  useEffect(() => {
    let active = true;
    getDashboardSummary()
      .then((res) => {
        if (active) setSummary(res.data);
      })
      .catch((err) => {
        if (active) setError('Could not load dashboard data. Check that the backend is running and your account has access.');
      });
    return () => { active = false; };
  }, [refresh]);

  return (
    <>
      <PageHeader eyebrow="Store operations" title="Overview" subtitle="A clear view of stock and activity across your store." />
      <Notice>{error}</Notice>
      <section className="stats-grid">
        {(role === 'OWNER' || role === 'STOCK_MANAGER' || role === 'CASHIER') && (
          <article className="stat-card" style={{ '--stat-accent': 'var(--green)' }}><div className="stat-label">Products</div><div className="stat-value">{summary?.products ?? '—'}</div><div className="stat-foot">Catalog items</div></article>
        )}
        {(role === 'OWNER' || role === 'STOCK_MANAGER') && (
          <article className="stat-card" style={{ '--stat-accent': 'var(--blue)' }}><div className="stat-label">Available stock</div><div className="stat-value">{summary?.stockUnits ?? '—'}</div><div className="stat-foot">Units across loaded products</div></article>
        )}
        {(role === 'OWNER' || role === 'STOCK_MANAGER') && (
          <article className="stat-card" style={{ '--stat-accent': 'var(--orange)' }}><div className="stat-label">Low stock</div><div className="stat-value">{summary?.lowStock ?? '—'}</div><div className="stat-foot">At or below minimum level</div></article>
        )}
        {(role === 'OWNER' || role === 'CASHIER') && (
          <article className="stat-card" style={{ '--stat-accent': 'var(--yellow)' }}><div className="stat-label">Sales orders</div><div className="stat-value">{summary?.orders ?? '—'}</div><div className="stat-foot">{summary?.purchases ?? '—'} purchase orders</div></article>
        )}
      </section>
      <div className="dashboard-grid">
        {(role === 'OWNER' || role === 'STOCK_MANAGER') && (
        <section className="panel">
          <div className="panel-title"><h2>Action required</h2><Link to="/stock-movements" className="btn btn-secondary btn-small">View all</Link></div>
          {!summary ? <div className="loading">Loading…</div> : summary.pendingApprovals?.length === 0 && summary.lowStockItems?.length === 0 ? (
            <div className="empty-state"><strong>All caught up!</strong>No pending approvals or low stock items.</div>
          ) : <div>
            {summary.lowStockItems?.length > 0 && <div className="table-wrap" style={{marginBottom: 16}}>
              <h3 style={{fontSize: '0.9rem', color: 'var(--orange)', marginBottom: 8}}>Low stock alerts</h3>
              <table>
                <thead><tr><th>Product ID</th><th>Available</th><th>Status</th><th style={{textAlign: "right"}}>Action</th></tr></thead>
                <tbody>{summary.lowStockItems.map((item) => <tr key={item.id}>
                  <td>{item.productId}</td>
                  <td>{item.availableQuantity}</td>
                  <td><span className="badge badge-pending">Low</span></td>
                </tr>)}</tbody>
              </table>
            </div>}
            {summary.pendingApprovals?.length > 0 && <div className="table-wrap">
              <h3 style={{fontSize: '0.9rem', color: 'var(--blue)', marginBottom: 8}}>Pending stock approvals</h3>
              <table>
                <thead><tr><th>Date</th><th>Product</th><th>Qty</th><th>Status</th><th style={{textAlign: "right"}}>Action</th></tr></thead>
                <tbody>{summary.pendingApprovals.map((m) => <tr key={m.id}>
                  <td>{formatDate(m.movementAt)}</td>
                  <td>{m.productName}</td>
                  <td>{m.movementType === 'OUT' ? '-' : '+'}{m.quantity}</td>
                  <td><span className="badge badge-pending">Pending</span></td>
                  <td style={{textAlign: 'right'}}>
                    <button className="btn btn-sm btn-primary" style={{marginRight: 8}} onClick={() => handleApprove(m.id)}>Approve</button>
                    <button className="btn btn-sm btn-secondary" onClick={() => handleReject(m.id)}>Reject</button>
                  </td>
                </tr>)}</tbody>
              </table>
            </div>}
          </div>}
        </section>
        )}
        {(role === 'OWNER' || role === 'CASHIER') && (
        <section className="panel">
          <div className="panel-title"><h2>Recent sales orders</h2><Link to="/orders" className="btn btn-secondary btn-small">View all</Link></div>
          {!summary ? <div className="loading">Loading store activity…</div> : summary.recentOrders.length === 0 ? (
            <div className="empty-state"><strong>No sales orders yet</strong>New orders will appear here.</div>
          ) : <div className="table-wrap"><table>
            <thead><tr><th>Order</th><th>Customer</th><th>Date</th><th>Total</th><th>Status</th><th style={{textAlign: "right"}}>Action</th></tr></thead>
            <tbody>{summary.recentOrders.map((order) => <tr key={order.id}>
              <td><Link to={`/orders/${order.id}`}>{order.orderNumber || `#${order.id}`}</Link></td>
              <td>{order.customerName || 'Walk-in'}</td><td>{formatDate(order.createdAt)}</td><td>{formatMoney(order.totalAmount)}</td>
              <td><span className={`badge badge-${String(order.status || '').toLowerCase()}`}>{order.status || '—'}</span></td>
            </tr>)}</tbody>
          </table></div>}
        </section>
        )}
        <section className="panel">
          <div className="panel-title"><h2>Quick actions</h2></div>
          <div className="quick-links">{shortcuts.filter(s => s[3].includes(role)).map(([path, code, label]) => <Link className="quick-link" to={path} key={path}>
            <span className="quick-key">{code}</span><strong>{label}</strong>
          </Link>)}</div>
          {summary && <p className="page-subtitle" style={{ marginTop: 18 }}>{(role === 'OWNER' || role === 'CASHIER') ? `${summary.customers} customers` : ''} {(role === 'OWNER') ? '·' : ''} {(role === 'OWNER' || role === 'STOCK_MANAGER') ? `${summary.suppliers} suppliers` : ''}</p>}
        </section>
      </div>
    </>
  );
}

export default DashboardPage;

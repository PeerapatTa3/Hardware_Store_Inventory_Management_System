import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getCustomers } from '../api/customers';
import { getInventoryByProduct } from '../api/inventory';
import { getOrders } from '../api/orders';
import { getProductsAdmin } from '../api/products';
import { getPurchases } from '../api/purchases';
import { getSuppliers } from '../api/suppliers';
import { useAuth } from '../context/AuthContext';
import { formatDate, formatMoney, PageHeader, Notice } from './ResourcePage';

const shortcuts = [
  ['/products/new', 'PR', 'Add product', ['OWNER', 'STOCK_MANAGER']],
  ['/purchases/new', 'PU', 'Receive stock', ['OWNER', 'STOCK_MANAGER']],
  ['/orders/new', 'SO', 'Create sales order', ['OWNER', 'CASHIER']],
  ['/inventory', 'IN', 'Review inventory', ['OWNER', 'STOCK_MANAGER']],
];

function dataOf(result, fallback = []) {
  return result.status === 'fulfilled' ? result.value.data : fallback;
}

function DashboardPage() {
  const { user } = useAuth();
  const role = user?.role || 'OWNER';
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    Promise.allSettled([
      getProductsAdmin({ page: 0, size: 50, sortBy: 'id', direction: 'desc' }),
      getPurchases(), getOrders(), getSuppliers(), getCustomers(),
    ]).then(async (results) => {
      if (!active) return;
      const productPage = dataOf(results[0], {});
      const products = productPage.content || [];
      const stockResults = await Promise.allSettled(products.map((product) => getInventoryByProduct(product.id)));
      if (!active) return;
      const stocks = stockResults.filter((item) => item.status === 'fulfilled').map((item) => item.value.data);
      const purchases = dataOf(results[1]);
      const orders = dataOf(results[2]);
      const suppliers = dataOf(results[3]);
      const customers = dataOf(results[4]);
      setSummary({
        products: productPage.totalElements ?? products.length,
        stockUnits: stocks.reduce((sum, stock) => sum + Number(stock.availableQuantity ?? stock.quantity ?? 0), 0),
        lowStock: stocks.filter((stock) => {
          const product = products.find((item) => item.id === stock.productId);
          return product && Number(stock.quantity) <= Number(product.minimumStock || 0);
        }).length,
        purchases: Array.isArray(purchases) ? purchases.length : 0,
        orders: Array.isArray(orders) ? orders.length : 0,
        suppliers: Array.isArray(suppliers) ? suppliers.length : 0,
        customers: Array.isArray(customers) ? customers.length : 0,
        recentOrders: (Array.isArray(orders) ? orders : []).slice().sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 5),
      });
      if (results.every((item) => item.status === 'rejected')) setError('Could not load dashboard data. Check that the backend is running and your account has access.');
    });
    return () => { active = false; };
  }, []);

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
        {(role === 'OWNER' || role === 'CASHIER') && (
        <section className="panel">
          <div className="panel-title"><h2>Recent sales orders</h2><Link to="/orders" className="btn btn-secondary btn-small">View all</Link></div>
          {!summary ? <div className="loading">Loading store activity…</div> : summary.recentOrders.length === 0 ? (
            <div className="empty-state"><strong>No sales orders yet</strong>New orders will appear here.</div>
          ) : <div className="table-wrap"><table>
            <thead><tr><th>Order</th><th>Customer</th><th>Date</th><th>Total</th><th>Status</th></tr></thead>
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
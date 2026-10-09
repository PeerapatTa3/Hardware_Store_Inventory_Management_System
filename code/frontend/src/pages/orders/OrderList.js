import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getOrders } from '../../api/orders';
import { formatDate, formatMoney, getErrorMessage, Notice, PageHeader } from '../ResourcePage';

function OrderList() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    getOrders().then(({ data }) => active && setOrders(Array.isArray(data) ? data : []))
      .catch((requestError) => active && setError(getErrorMessage(requestError)))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, []);

  return <>
    <PageHeader eyebrow="Operations" title="Sales orders" subtitle="Follow customer orders from confirmation through completion."
      action={<Link className="btn btn-primary" to="/orders/new">＋ New order</Link>} />
    <section className="panel"><Notice>{error}</Notice>{loading ? <div className="loading">Loading sales orders…</div> : orders.length === 0 ? <div className="empty-state"><strong>No sales orders yet</strong>Create a sales order to get started.</div> : (
      <div className="table-wrap"><table><thead><tr><th>Order</th><th>Customer</th><th>Created</th><th>Items</th><th>Total</th><th>Status</th><th></th></tr></thead>
        <tbody>{orders.map((order) => <tr key={order.id}><td><strong>{order.orderNumber || `#${order.id}`}</strong></td><td>{order.customerName || 'Walk-in'}</td>
          <td>{formatDate(order.createdAt)}</td><td>{order.items?.length ?? 0}</td><td>{formatMoney(order.totalAmount)}</td><td><span className={`badge badge-${String(order.status).toLowerCase()}`}>{order.status}</span></td>
          <td><Link className="btn btn-secondary btn-small" to={`/orders/${order.id}`}>Open</Link></td></tr>)}</tbody>
      </table></div>
    )}</section>
  </>;
}

export default OrderList;
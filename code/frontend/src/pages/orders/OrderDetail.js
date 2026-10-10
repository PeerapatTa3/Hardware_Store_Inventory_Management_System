import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getOrder, updateOrderStatus } from '../../api/orders';
import { formatDate, formatMoney, getErrorMessage, Notice, PageHeader } from '../ResourcePage';

const transitions = {
  PENDING: ['CONFIRMED', 'CANCELLED'],
  CONFIRMED: ['SHIPPED', 'CANCELLED'],
  SHIPPED: ['COMPLETED'],
  COMPLETED: ['CANCELLED'],
  CANCELLED: [],
};

function OrderDetail() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');
  const [updating, setUpdating] = useState(false);
  const [refresh, setRefresh] = useState(0);

  useEffect(() => {
    let active = true;
    getOrder(id).then(({ data }) => active && setOrder(data))
      .catch((requestError) => active && setError(getErrorMessage(requestError)));
    return () => { active = false; };
  }, [id, refresh]);

  const changeStatus = async (status) => {
    setUpdating(true);
    setError('');
    try {
      await updateOrderStatus(id, { status });
      toast.success(`Order ${status.toLowerCase()}`);
      setRefresh((value) => value + 1);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setUpdating(false);
    }
  };

  return <>
    <PageHeader eyebrow="Sales orders" title={order?.orderNumber || 'Sales order'} subtitle={order ? `Created ${formatDate(order.createdAt)}` : 'Order details'}
      action={<Link to="/orders" className="btn btn-secondary">Back to orders</Link>} />
    <section className="panel"><Notice>{error}</Notice>{!order ? <div className="loading">Loading order…</div> : <>
      <div className="detail-grid"><div className="detail-field"><span>Customer</span><strong>{order.customerName || 'Walk-in'}</strong></div>
        <div className="detail-field"><span>Status</span><strong><span className={`badge badge-${String(order.status).toLowerCase()}`}>{order.status}</span></strong></div>
        <div className="detail-field"><span>Payment</span><strong>{order.paymentMethod || '—'}</strong></div><div className="detail-field"><span>Shipping address</span><strong>{order.shippingAddress || '—'}</strong></div></div>
      <div className="line-items"><div className="panel-title"><h2>Order items</h2></div><div className="table-wrap"><table><thead><tr><th>Product</th><th>Quantity</th><th>Unit price</th><th>Subtotal</th></tr></thead>
        <tbody>{(order.items || []).map((item) => <tr key={item.id || item.productId}><td>{item.productName || `Product #${item.productId}`}</td><td>{item.quantity}</td><td>{formatMoney(item.unitPrice)}</td><td>{formatMoney(item.subtotal)}</td></tr>)}</tbody>
      </table></div><div className="total-row">Order total&nbsp; {formatMoney(order.totalAmount)}</div></div>
      {(transitions[order.status] || []).length > 0 && <div className="form-actions"><div className="status-actions">{transitions[order.status]
        .filter(status => status !== 'CANCELLED' || localStorage.getItem('role') === 'OWNER')
        .map((status) => <button key={status} className={`btn ${status === 'CANCELLED' ? 'btn-danger' : 'btn-primary'}`} disabled={updating} onClick={() => changeStatus(status)}>{updating ? 'Updating…' : status === 'CONFIRMED' ? 'Confirm order' : status === 'SHIPPED' ? 'Mark shipped' : status === 'COMPLETED' ? 'Complete order' : 'Cancel order'}</button>)}</div></div>}
    </>}</section>
  </>;
}

export default OrderDetail;
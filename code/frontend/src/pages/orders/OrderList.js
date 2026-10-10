import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { getOrders } from '../../api/orders';
import { formatDate, formatMoney, getErrorMessage, Notice, PageHeader } from '../ResourcePage';

function OrderList() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const location = useLocation();
  const navigate = useNavigate();
  const receiptOrder = location.state?.receiptOrder;

  useEffect(() => {
    let active = true;
    getOrders().then(({ data }) => active && setOrders(Array.isArray(data) ? data : []))
      .catch((requestError) => active && setError(getErrorMessage(requestError)))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, []);

  const closeReceipt = () => {
    navigate(location.pathname, { replace: true });
  };

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

    {receiptOrder && (
      <div className="modal-overlay" style={{position:'fixed', top:0, left:0, right:0, bottom:0, background:'rgba(0,0,0,0.5)', display:'flex', alignItems:'center', justifyContent:'center', zIndex:1000}}>
        <div className="modal-content" style={{background:'#fff', padding:'2rem', borderRadius:'8px', width:'400px', maxWidth:'90%'}}>
          <h2 style={{marginTop:0, textAlign:'center'}}>Order Completed</h2>
          <div style={{textAlign:'center', marginBottom:'1.5rem', color:'#666'}}>
            {receiptOrder.orderNumber}
          </div>
          
          <div style={{marginBottom:'1rem'}}>
            <strong>Customer:</strong> {receiptOrder.customerName || 'Walk-in'}<br/>
            <strong>Date:</strong> {formatDate(receiptOrder.createdAt)}<br/>
            <strong>Payment:</strong> {receiptOrder.paymentMethod || 'CASH'}
          </div>

          <table style={{width:'100%', marginBottom:'1rem', borderCollapse:'collapse'}}>
            <thead>
              <tr style={{borderBottom:'1px solid #ddd', textAlign:'left'}}>
                <th style={{paddingBottom:'0.5rem'}}>Item</th>
                <th style={{paddingBottom:'0.5rem', textAlign:'right'}}>Qty</th>
                <th style={{paddingBottom:'0.5rem', textAlign:'right'}}>Total</th>
              </tr>
            </thead>
            <tbody>
              {(receiptOrder.items || []).map(item => (
                <tr key={item.productId} style={{borderBottom:'1px solid #eee'}}>
                  <td style={{padding:'0.5rem 0'}}>{item.productName}</td>
                  <td style={{padding:'0.5rem 0', textAlign:'right'}}>{item.quantity}</td>
                  <td style={{padding:'0.5rem 0', textAlign:'right'}}>{formatMoney(item.subtotal)}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <td colSpan="2" style={{paddingTop:'1rem', fontWeight:'bold', textAlign:'right'}}>Net Total:</td>
                <td style={{paddingTop:'1rem', fontWeight:'bold', textAlign:'right', fontSize:'1.2rem'}}>{formatMoney(receiptOrder.totalAmount)}</td>
              </tr>
            </tfoot>
          </table>

          <div style={{textAlign:'center'}}>
            <button className="btn btn-primary" onClick={closeReceipt} style={{width:'100%'}}>OK</button>
          </div>
        </div>
      </div>
    )}
  </>;
}

export default OrderList;
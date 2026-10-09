import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getPurchase, receivePurchase } from '../../api/purchases';
import { formatDate, formatMoney, getErrorMessage, Notice, PageHeader } from '../ResourcePage';

function PurchaseDetail() {
  const { id } = useParams();
  const [purchase, setPurchase] = useState(null);
  const [error, setError] = useState('');
  const [receiving, setReceiving] = useState(false);
  const [refresh, setRefresh] = useState(0);

  useEffect(() => {
    let active = true;
    getPurchase(id).then(({ data }) => active && setPurchase(data))
      .catch((requestError) => active && setError(getErrorMessage(requestError)));
    return () => { active = false; };
  }, [id, refresh]);

  const receive = async () => {
    setReceiving(true);
    setError('');
    try {
      await receivePurchase(id);
      toast.success('Purchase received and inventory updated');
      setRefresh((value) => value + 1);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setReceiving(false);
    }
  };

  return <>
    <PageHeader eyebrow="Purchases" title={purchase?.purchaseNumber || 'Purchase order'} subtitle={purchase ? `Created ${formatDate(purchase.createdAt)}` : 'Purchase order details'}
      action={<Link to="/purchases" className="btn btn-secondary">Back to purchases</Link>} />
    <section className="panel"><Notice>{error}</Notice>{!purchase ? <div className="loading">Loading purchase…</div> : <>
      <div className="detail-grid"><div className="detail-field"><span>Supplier</span><strong>{purchase.supplierName || `Supplier #${purchase.supplierId}`}</strong></div>
        <div className="detail-field"><span>Status</span><strong><span className={`badge badge-${String(purchase.status).toLowerCase()}`}>{purchase.status}</span></strong></div>
        <div className="detail-field"><span>Purchase number</span><strong>{purchase.purchaseNumber || `#${purchase.id}`}</strong></div>
        <div className="detail-field"><span>Total</span><strong>{formatMoney(purchase.totalAmount)}</strong></div></div>
      <div className="line-items"><div className="panel-title"><h2>Ordered items</h2></div><div className="table-wrap"><table><thead><tr><th>Product</th><th>Quantity</th><th>Unit cost</th><th>Subtotal</th></tr></thead>
        <tbody>{(purchase.items || []).map((item) => <tr key={item.id || item.productId}><td>{item.productName || `Product #${item.productId}`}</td><td>{item.quantity}</td><td>{formatMoney(item.unitCost)}</td><td>{formatMoney(item.subtotal)}</td></tr>)}</tbody>
      </table></div></div>
      {purchase.status === 'PENDING' && <div className="form-actions"><button className="btn btn-primary" onClick={receive} disabled={receiving}>{receiving ? 'Receiving…' : 'Receive purchase'}</button></div>}
    </>}</section>
  </>;
}

export default PurchaseDetail;
import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getPurchase, receivePurchase, approvePurchase } from '../../api/purchases';
import { formatDate, formatMoney, getErrorMessage, Notice, PageHeader } from '../ResourcePage';

function PurchaseDetail() {
  const { id } = useParams();
  const [purchase, setPurchase] = useState(null);
  const [error, setError] = useState('');
  const [receiving, setReceiving] = useState(false);
  const [refresh, setRefresh] = useState(0);
  const [receivedQtys, setReceivedQtys] = useState({});
  const role = localStorage.getItem('role');

  useEffect(() => {
    let active = true;
    getPurchase(id).then(({ data }) => {
      if (active) {
        setPurchase(data);
        const qtys = {};
        (data.items || []).forEach(item => {
           qtys[item.productId] = (item.quantity || 0) - (item.receivedQuantity || 0);
        });
        setReceivedQtys(qtys);
      }
    }).catch((requestError) => active && setError(getErrorMessage(requestError)));
    return () => { active = false; };
  }, [id, refresh]);

  const handleQtyChange = (productId, val) => {
    setReceivedQtys(prev => ({ ...prev, [productId]: parseInt(val) || 0 }));
  };

  const receive = async () => {
    setReceiving(true);
    setError('');
    try {
      const payload = {
        items: Object.entries(receivedQtys).map(([productId, qty]) => ({
          productId: parseInt(productId),
          receivedQuantity: qty
        }))
      };
      await receivePurchase(id, payload);
      toast.success('Purchase received and inventory updated');
      setRefresh((value) => value + 1);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setReceiving(false);
    }
  };

  const approve = async () => {
    setError('');
    try {
      await approvePurchase(id);
      toast.success('Purchase approved');
      setRefresh((value) => value + 1);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
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
      <div className="line-items"><div className="panel-title"><h2>Ordered items</h2></div><div className="table-wrap"><table>
        <thead><tr><th>Product</th><th>Ordered</th><th>Received</th>{ (purchase.status === 'APPROVED' || purchase.status === 'PARTIAL') && <th>Receive Now</th> }<th>Unit cost</th><th>Subtotal</th></tr></thead>
        <tbody>{(purchase.items || []).map((item) => <tr key={item.id || item.productId}>
          <td>{item.productName || `Product #${item.productId}`}</td>
          <td>{item.quantity}</td>
          <td>{item.receivedQuantity || 0}</td>
          { (purchase.status === 'APPROVED' || purchase.status === 'PARTIAL') && <td>
             <input type="number" min="0" max={item.quantity - (item.receivedQuantity || 0)} value={receivedQtys[item.productId] ?? 0} onChange={(e) => handleQtyChange(item.productId, e.target.value)} style={{width: 80}} />
          </td> }
          <td>{formatMoney(item.unitCost)}</td><td>{formatMoney(item.subtotal)}</td>
        </tr>)}</tbody>
      </table></div></div>
      <div className="form-actions">
        {purchase.status === 'PENDING' && role === 'OWNER' && <button className="btn btn-primary" onClick={approve}>Approve Order</button>}
        {(purchase.status === 'APPROVED' || purchase.status === 'PARTIAL') && <button className="btn btn-primary" onClick={receive} disabled={receiving}>{receiving ? 'Receiving…' : 'Receive items'}</button>}
      </div>
    </>}</section>
  </>;
}

export default PurchaseDetail;

import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getPurchases } from '../../api/purchases';
import { formatDate, formatMoney, getErrorMessage, Notice, PageHeader } from '../ResourcePage';

function PurchaseList() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    getPurchases().then(({ data }) => active && setItems(Array.isArray(data) ? data : []))
      .catch((requestError) => active && setError(getErrorMessage(requestError)))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, []);

  return <>
    <PageHeader eyebrow="Operations" title="Purchases" subtitle="Track supplier orders and receive stock into inventory."
      action={<Link className="btn btn-primary" to="/purchases/new">＋ New purchase</Link>} />
    <section className="panel"><Notice>{error}</Notice>
      {loading ? <div className="loading">Loading purchases…</div> : items.length === 0 ? <div className="empty-state"><strong>No purchase orders yet</strong>Create a purchase order to receive stock.</div> : (
        <div className="table-wrap"><table><thead><tr><th>Purchase</th><th>Supplier</th><th>Created</th><th>Lines</th><th>Total</th><th>Status</th><th></th></tr></thead>
          <tbody>{items.map((purchase) => <tr key={purchase.id}><td><strong>{purchase.purchaseNumber || `#${purchase.id}`}</strong></td><td>{purchase.supplierName || '—'}</td><td>{formatDate(purchase.createdAt)}</td>
            <td>{purchase.items?.length ?? 0}</td><td>{formatMoney(purchase.totalAmount)}</td><td><span className={`badge badge-${String(purchase.status).toLowerCase()}`}>{purchase.status}</span></td>
            <td><Link className="btn btn-secondary btn-small" to={`/purchases/${purchase.id}`}>Open</Link></td></tr>)}</tbody>
        </table></div>
      )}
    </section>
  </>;
}

export default PurchaseList;
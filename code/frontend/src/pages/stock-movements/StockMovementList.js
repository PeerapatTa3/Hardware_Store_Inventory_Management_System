import React, { useEffect, useState } from 'react';
import { getStockMovements } from '../../api/stockMovements';
import { formatDate, getErrorMessage, Notice, PageHeader } from '../ResourcePage';

function StockMovementList() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    getStockMovements().then(({ data }) => active && setItems(Array.isArray(data) ? data : []))
      .catch((requestError) => active && setError(getErrorMessage(requestError)))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, []);

  return <>
    <PageHeader eyebrow="Stockroom" title="Stock movements" subtitle="A ledger of inbound, outbound and adjustment activity." />
    <section className="panel"><Notice>{error}</Notice>
      {loading ? <div className="loading">Loading movements…</div> : items.length === 0 ? <div className="empty-state"><strong>No movements recorded</strong>Receiving purchases and stock adjustments will appear here.</div> : (
        <div className="table-wrap"><table><thead><tr><th>Date</th><th>Product</th><th>Movement</th><th>Quantity</th><th>Reference</th><th>Note</th></tr></thead>
          <tbody>{items.map((movement) => <tr key={movement.id}><td>{formatDate(movement.movementAt)}</td><td><strong>{movement.productName || `Product #${movement.productId}`}</strong></td>
            <td><span className={`badge badge-${String(movement.movementType).toLowerCase()}`}>{movement.movementType}</span></td><td>{movement.quantity}</td><td>{movement.referenceNo || '—'}</td><td>{movement.note || '—'}</td></tr>)}</tbody>
        </table></div>
      )}
    </section>
  </>;
}

export default StockMovementList;
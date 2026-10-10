import React, { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { getStockMovements, approveStockMovement, rejectStockMovement } from '../../api/stockMovements';
import { formatDate, getErrorMessage, Notice, PageHeader } from '../ResourcePage';

function StockMovementList() {
  const [movements, setMovements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [refresh, setRefresh] = useState(0);
  const role = localStorage.getItem('role');

  useEffect(() => {
    let active = true;
    getStockMovements().then(({ data }) => {
      if (active) { setMovements(data); setLoading(false); }
    }).catch(err => {
      if (active) { setError(getErrorMessage(err)); setLoading(false); }
    });
    return () => { active = false; };
  }, [refresh]);

  const handleApprove = async (id) => {
    try { await approveStockMovement(id); toast.success('Approved successfully'); setRefresh(r => r + 1); } 
    catch (err) { setError(getErrorMessage(err)); }
  };
  const handleReject = async (id) => {
    try { await rejectStockMovement(id); toast.success('Rejected successfully'); setRefresh(r => r + 1); } 
    catch (err) { setError(getErrorMessage(err)); }
  };

  return <>
    <PageHeader title="Stock movements" subtitle="History of all inventory changes" />
    <section className="panel"><Notice>{error}</Notice>
      {loading ? <div className="loading">Loading…</div> : <div className="table-wrap">
        <table className="data-table"><thead><tr>
          <th>Date</th><th>Product</th><th>Type</th><th>Quantity</th><th>Reference</th><th>Status</th>
          {role === 'OWNER' && <th>Action</th>}
        </tr></thead><tbody>
          {movements.length === 0 ? <tr><td colSpan="7" className="empty-state">No movements</td></tr> :
            movements.map(m => (
              <tr key={m.id}>
                <td>{formatDate(m.movementAt)}</td>
                <td>{m.productName}</td>
                <td><span className={`badge badge-${m.movementType.toLowerCase()}`}>{m.movementType}</span></td>
                <td>{m.movementType === 'OUT' ? '-' : '+'}{m.quantity}</td>
                <td>{m.referenceNo || '-'}</td>
                <td><span className={`badge badge-${(m.status || 'APPROVED').toLowerCase()}`}>{m.status || 'APPROVED'}</span></td>
                {role === 'OWNER' && <td>
                   {m.status === 'PENDING' && <>
                      <button className="btn btn-sm btn-primary" style={{marginRight: 8}} onClick={() => handleApprove(m.id)}>Approve</button>
                      <button className="btn btn-sm btn-secondary" onClick={() => handleReject(m.id)}>Reject</button>
                   </>}
                </td>}
              </tr>
            ))}
        </tbody></table>
      </div>}
    </section>
  </>;
}

export default StockMovementList;

import React, { useCallback, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { getInventoryByProduct, updateInventory } from '../../api/inventory';
import { getProductsAdmin } from '../../api/products';
import { formatMoney, getErrorMessage, Notice, PageHeader } from '../ResourcePage';

function InventoryPage() {
  const [items, setItems] = useState([]);
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState(null);
  const [quantity, setQuantity] = useState('');
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [refresh, setRefresh] = useState(0);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await getProductsAdmin({ page: 0, size: 100, sortBy: 'name', direction: 'asc' });
      const products = data.content || [];
      const stockResults = await Promise.allSettled(products.map((product) => getInventoryByProduct(product.id)));
      setItems(products.map((product, index) => ({ ...product, stock: stockResults[index].status === 'fulfilled' ? stockResults[index].value.data : null })));
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load, refresh]);

  const openAdjust = (item) => {
    setSelected(item);
    setQuantity(String(item.stock?.quantity ?? 0));
    setReason('');
  };

  const saveAdjustment = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      await updateInventory(selected.id, { quantity: Number(quantity), reason: reason.trim() || null });
      toast.success('Inventory updated');
      setSelected(null);
      setRefresh((value) => value + 1);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  const filtered = items.filter((item) => `${item.name} ${item.sku}`.toLowerCase().includes(query.toLowerCase()));

  return <>
    <PageHeader eyebrow="Stockroom" title="Inventory" subtitle="Review on-hand quantities and correct stock counts." />
    <section className="panel">
      <div className="toolbar"><input className="form-control search-input" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search products" aria-label="Search products" />
        <span className="page-subtitle">{items.length} products tracked</span></div>
      <Notice>{error}</Notice>
      {loading ? <div className="loading">Loading inventory…</div> : filtered.length === 0 ? <div className="empty-state"><strong>No inventory records</strong>Products will appear when available.</div> : (
        <div className="table-wrap"><table><thead><tr><th>SKU</th><th>Product</th><th>On hand</th><th>Reserved</th><th>Available</th><th>Min. stock</th><th>Price</th><th>Action</th></tr></thead>
          <tbody>{filtered.map((item) => {
            const stock = item.stock;
            const low = stock && Number(stock.quantity) <= Number(item.minimumStock || 0);
            return <tr key={item.id}><td>{item.sku}</td><td><strong>{item.name}</strong></td>
              <td className={low ? 'stock-low' : ''}>{stock?.quantity ?? '—'}</td><td>{stock?.reservedQuantity ?? '—'}</td>
              <td>{stock?.availableQuantity ?? '—'}</td><td>{item.minimumStock ?? '—'}</td><td>{formatMoney(item.price)}</td>
              <td><button className="btn btn-secondary btn-small" disabled={!stock} onClick={() => openAdjust(item)}>Adjust</button></td></tr>;
          })}</tbody>
        </table></div>
      )}
    </section>
    {selected && <div className="modal-overlay" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && setSelected(null)}>
      <form className="modal panel" onSubmit={saveAdjustment}>
        <div className="panel-title"><h2>Adjust stock</h2><button type="button" className="btn btn-secondary btn-small" onClick={() => setSelected(null)} aria-label="Close">Close</button></div>
        <p className="page-subtitle">{selected.name} · {selected.sku}</p>
        <div className="form-group"><label htmlFor="stock-count">New on-hand quantity</label><input id="stock-count" className="form-control" type="number" min={selected.stock?.reservedQuantity || 0} step="1" required value={quantity} onChange={(event) => setQuantity(event.target.value)} /></div>
        <div className="form-group"><label htmlFor="stock-reason">Reason</label><input id="stock-reason" className="form-control" value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Count correction, damage…" /></div>
        <div className="form-actions"><button type="button" className="btn btn-secondary" onClick={() => setSelected(null)}>Cancel</button><button className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save adjustment'}</button></div>
      </form>
    </div>}
  </>;
}

export default InventoryPage;
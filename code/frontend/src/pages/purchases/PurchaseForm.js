import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { createPurchase } from '../../api/purchases';
import { getProductsAdmin } from '../../api/products';
import { getSuppliers } from '../../api/suppliers';
import { formatMoney, getErrorMessage, Notice, PageHeader } from '../ResourcePage';

function PurchaseForm() {
  const navigate = useNavigate();
  const [suppliers, setSuppliers] = useState([]);
  const [products, setProducts] = useState([]);
  const [supplierId, setSupplierId] = useState('');
  const [lines, setLines] = useState([{ productId: '', quantity: 1, unitCost: '' }]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([getSuppliers(), getProductsAdmin({ page: 0, size: 100, sortBy: 'name', direction: 'asc' })])
      .then(([supplierResponse, productResponse]) => {
        setSuppliers(supplierResponse.data || []);
        setProducts(productResponse.data.content || []);
      }).catch((requestError) => setError(getErrorMessage(requestError))).finally(() => setLoading(false));
  }, []);

  const updateLine = (index, field, value) => setLines((current) => current.map((line, lineIndex) => {
    if (lineIndex !== index) return line;
    if (field === 'productId') {
      const product = products.find((item) => String(item.id) === value);
      return { ...line, productId: value, unitCost: product?.costPrice ?? '' };
    }
    return { ...line, [field]: value };
  }));

  const total = lines.reduce((sum, line) => sum + Number(line.quantity || 0) * Number(line.unitCost || 0), 0);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createPurchase({ supplierId: Number(supplierId), items: lines.map((line) => ({ productId: Number(line.productId), quantity: Number(line.quantity), unitCost: Number(line.unitCost) })) });
      toast.success('Purchase order created');
      navigate('/purchases');
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  return <>
    <PageHeader eyebrow="Purchases" title="New purchase order" subtitle="Choose a supplier and list the items being ordered." />
    <section className="panel"><Notice>{error}</Notice>{loading ? <div className="loading">Loading suppliers and products…</div> : (
      <form onSubmit={submit}>
        <div className="form-group"><label htmlFor="supplier">Supplier <span className="required">*</span></label><select id="supplier" className="form-control" required value={supplierId} onChange={(event) => setSupplierId(event.target.value)}>
          <option value="">Select supplier</option>{suppliers.map((supplier) => <option value={supplier.id} key={supplier.id}>{supplier.name}</option>)}
        </select></div>
        <div className="line-items"><div className="panel-title"><h2>Items</h2><button type="button" className="btn btn-secondary btn-small" onClick={() => setLines((current) => [...current, { productId: '', quantity: 1, unitCost: '' }])}>＋ Add line</button></div>
          {lines.map((line, index) => <div className="line-item" key={index}>
            <div className="form-group"><label>Product</label><select className="form-control" required value={line.productId} onChange={(event) => updateLine(index, 'productId', event.target.value)}><option value="">Select product</option>{products.map((product) => <option key={product.id} value={product.id}>{product.sku} · {product.name}</option>)}</select></div>
            <div className="form-group"><label>Quantity</label><input className="form-control" type="number" min="1" step="1" required value={line.quantity} onChange={(event) => updateLine(index, 'quantity', event.target.value)} /></div>
            <div className="form-group"><label>Unit cost</label><input className="form-control" type="number" min="0.01" step="0.01" required value={line.unitCost} onChange={(event) => updateLine(index, 'unitCost', event.target.value)} /></div>
            <button type="button" className="btn btn-danger btn-small" disabled={lines.length === 1} onClick={() => setLines((current) => current.filter((_, itemIndex) => itemIndex !== index))} aria-label="Remove line">Remove</button>
          </div>)}
          <div className="total-row">Estimated total&nbsp; {formatMoney(total)}</div>
        </div>
        <div className="form-actions"><Link to="/purchases" className="btn btn-secondary">Cancel</Link><button className="btn btn-primary" disabled={saving || suppliers.length === 0 || products.length === 0}>{saving ? 'Creating…' : 'Create purchase'}</button></div>
      </form>
    )}</section>
  </>;
}

export default PurchaseForm;
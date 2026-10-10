import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getCustomers } from '../../api/customers';
import { createOrder } from '../../api/orders';
import { getProducts } from '../../api/products';
import { formatMoney, getErrorMessage, Notice, PageHeader } from '../ResourcePage';

function OrderForm() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [customerId, setCustomerId] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('CASH');
  const [lines, setLines] = useState([{ productId: '', quantity: 1 }]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([getCustomers(), getProducts({ page: 0, size: 100, sortBy: 'name', direction: 'asc' })])
      .then(([customerResponse, productResponse]) => {
        setCustomers(customerResponse.data || []);
        setProducts(productResponse.data.content || []);
      }).catch((requestError) => setError(getErrorMessage(requestError))).finally(() => setLoading(false));
  }, []);

  const updateLine = (index, field, value) => setLines((current) => current.map((line, lineIndex) => lineIndex === index ? { ...line, [field]: value } : line));
  const total = lines.reduce((sum, line) => {
    const product = products.find((item) => String(item.id) === String(line.productId));
    return sum + Number(product?.price || 0) * Number(line.quantity || 0);
  }, 0);
  
  const selectedCustomer = customers.find(c => String(c.id) === String(customerId));
  const isMember = selectedCustomer?.member;
  const totalQuantity = lines.reduce((sum, line) => sum + Number(line.quantity || 0), 0);
  
  // Apply frontend discount preview matching backend logic
  let finalTotal = total;
  if (isMember) {
      finalTotal = total * 0.9;
  } else if (totalQuantity >= 10) {
      finalTotal = total * 0.95;
  }

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    const payload = {
      customerId: customerId ? Number(customerId) : null,
      paymentMethod,
      items: lines.map((line) => ({ productId: Number(line.productId), quantity: Number(line.quantity) })),
    };
    try {
      const response = await createOrder(payload);
      toast.success('Sales order created');
      navigate('/orders', { state: { receiptOrder: response.data } });
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  return <>
    <PageHeader eyebrow="Sales orders" title="New sales order" subtitle="Add the customer and products for this order." />
    <section className="panel"><Notice>{error}</Notice>{loading ? <div className="loading">Loading customers and products…</div> : (
      <form onSubmit={submit}>
        <div className="form-grid">
          <div className="form-group"><label htmlFor="customer">Customer</label><select id="customer" className="form-control" value={customerId} onChange={(event) => setCustomerId(event.target.value)}><option value="">Walk-in customer</option>{customers.map((customer) => <option key={customer.id} value={customer.id}>{customer.name} · {customer.phone}{customer.member ? ' ⭐ Member' : ''}</option>)}</select></div>
          <div className="form-group"><label htmlFor="payment">Payment method</label><select id="payment" className="form-control" value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value)}><option value="CASH">Cash</option><option value="TRANSFER">Transfer</option><option value="CARD">Card</option></select></div>
        </div>
        <div className="line-items"><div className="panel-title"><h2>Items</h2><button type="button" className="btn btn-secondary btn-small" onClick={() => setLines((current) => [...current, { productId: '', quantity: 1 }])}>＋ Add line</button></div>
          {lines.map((line, index) => <div className="line-item" key={index}>
            <div className="form-group"><label>Product</label><select className="form-control" required value={line.productId} onChange={(event) => updateLine(index, 'productId', event.target.value)}><option value="">Select product</option>{products.map((product) => <option key={product.id} value={product.id}>{product.sku} · {product.name} · {formatMoney(product.price)}</option>)}</select></div>
            <div className="form-group"><label>Quantity</label><input className="form-control" type="number" min="1" step="1" required value={line.quantity} onChange={(event) => updateLine(index, 'quantity', event.target.value)} /></div>
            <div className="form-group"><label>Unit price</label><input className="form-control" readOnly value={formatMoney(products.find((item) => String(item.id) === String(line.productId))?.price)} /></div>
            <button type="button" className="btn btn-danger btn-small" disabled={lines.length === 1} onClick={() => setLines((current) => current.filter((_, itemIndex) => itemIndex !== index))} aria-label="Remove line">Remove</button>
          </div>)}
          <div className="total-row">Estimated total&nbsp; {formatMoney(finalTotal)}</div>
        </div>
        <div className="form-actions"><Link to="/orders" className="btn btn-secondary">Cancel</Link><button className="btn btn-primary" disabled={saving || products.length === 0}>{saving ? 'Creating…' : 'Create order'}</button></div>
      </form>
    )}</section>
  </>;
}

export default OrderForm;
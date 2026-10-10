import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getCustomers } from '../../api/customers';
import { createOrder } from '../../api/orders';
import { getProducts } from '../../api/products';
import { formatMoney, getErrorMessage, Notice, PageHeader } from '../ResourcePage';

function OrderForm() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [customerSearch, setCustomerSearch] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('CASH');
  const [lines, setLines] = useState([]);
  const [productSearch, setProductSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const productInputRef = useRef(null);

  useEffect(() => {
    Promise.all([getCustomers(), getProducts({ page: 0, size: 500, sortBy: 'name', direction: 'asc' })])
      .then(([customerResponse, productResponse]) => {
        setCustomers(customerResponse.data || []);
        setProducts(productResponse.data.content || []);
      }).catch((requestError) => setError(getErrorMessage(requestError))).finally(() => setLoading(false));
  }, []);

  // Customer resolution
  const selectedCustomer = customers.find(c => 
    c.phone === customerSearch || 
    String(c.id) === customerSearch || 
    c.name.toLowerCase() === customerSearch.toLowerCase()
  );
  const isMember = selectedCustomer?.member;

  // Cart logic
  const handleAddProduct = (e) => {
    e.preventDefault();
    if (!productSearch.trim()) return;
    
    // Find exact SKU, or exact Name
    const foundProduct = products.find(p => 
      p.sku.toLowerCase() === productSearch.toLowerCase().trim() ||
      p.name.toLowerCase() === productSearch.toLowerCase().trim()
    );

    if (foundProduct) {
      setLines(current => {
        const existing = current.findIndex(line => line.productId === foundProduct.id);
        if (existing >= 0) {
          const newLines = [...current];
          newLines[existing] = { ...newLines[existing], quantity: Number(newLines[existing].quantity) + 1 };
          return newLines;
        }
        return [{ productId: foundProduct.id, quantity: 1, product: foundProduct }, ...current];
      });
      setProductSearch('');
      if (productInputRef.current) productInputRef.current.focus();
    } else {
      toast.error('Product not found! Please check the SKU or name.');
    }
  };

  const updateQuantity = (productId, newQty) => {
    if (newQty < 1) return;
    setLines(current => current.map(line => line.productId === productId ? { ...line, quantity: Number(newQty) } : line));
  };

  const removeLine = (productId) => {
    setLines(current => current.filter(line => line.productId !== productId));
  };

  // Totals
  const total = lines.reduce((sum, line) => sum + Number(line.product.price) * line.quantity, 0);
  const totalQuantity = lines.reduce((sum, line) => sum + line.quantity, 0);
  let finalTotal = total;
  let discountLabel = '';
  if (isMember) {
      finalTotal = total * 0.9;
      discountLabel = '10% Member Discount';
  } else if (totalQuantity >= 10) {
      finalTotal = total * 0.95;
      discountLabel = '5% Bulk Discount (10+ items)';
  }

  const submit = async (event) => {
    event.preventDefault();
    if (lines.length === 0) {
      setError('Please add at least one product to the cart.');
      return;
    }
    setSaving(true);
    setError('');
    const payload = {
      customerId: selectedCustomer ? selectedCustomer.id : null,
      paymentMethod,
      items: lines.map((line) => ({ productId: line.productId, quantity: line.quantity })),
    };
    try {
      await createOrder(payload);
      toast.success('Sales order completed');
      navigate('/orders');
    } catch (requestError) {
      setError(getErrorMessage(requestError));
      setSaving(false);
    }
  };

  return <>
    <PageHeader eyebrow="Sales orders" title="Point of Sale (POS)" subtitle="Fast checkout for walk-in and member customers." />
    <section className="panel"><Notice>{error}</Notice>{loading ? <div className="loading">Loading POS system…</div> : (
      <div className="pos-grid" style={{ display: 'flex', gap: '24px', alignItems: 'flex-start' }}>
        
        {/* Left Side: Cart & Scanner */}
        <div style={{ flex: 2 }}>
          {/* Scanner Input */}
          <form onSubmit={handleAddProduct} style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
            <input 
              ref={productInputRef}
              type="text" 
              className="form-control" 
              style={{ flex: 1, padding: '12px', fontSize: '1.1rem' }}
              placeholder="Scan Barcode (SKU) or type Product Name and press Enter..." 
              value={productSearch} 
              onChange={e => setProductSearch(e.target.value)}
              list="product-suggestions"
              autoFocus
            />
            <datalist id="product-suggestions">
              {products.map(p => <option key={p.id} value={p.sku}>{p.name} - {formatMoney(p.price)}</option>)}
            </datalist>
            <button type="submit" className="btn btn-secondary">Add</button>
          </form>

          {/* Cart Table */}
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr><th>Item</th><th style={{width: '100px'}}>Qty</th><th>Price</th><th>Total</th><th></th></tr>
              </thead>
              <tbody>
                {lines.length === 0 ? <tr><td colSpan="5" className="empty-state">Cart is empty. Scan an item to begin.</td></tr> : 
                  lines.map(line => (
                    <tr key={line.productId}>
                      <td><strong>{line.product.name}</strong><br/><small style={{color:'var(--gray-600)'}}>{line.product.sku}</small></td>
                      <td>
                        <input type="number" min="1" className="form-control" style={{width: '70px', padding: '4px'}}
                          value={line.quantity} onChange={(e) => updateQuantity(line.productId, e.target.value)} />
                      </td>
                      <td>{formatMoney(line.product.price)}</td>
                      <td>{formatMoney(line.product.price * line.quantity)}</td>
                      <td style={{textAlign: 'right'}}><button type="button" className="btn btn-sm btn-danger" onClick={() => removeLine(line.productId)}>✕</button></td>
                    </tr>
                  ))
                }
              </tbody>
            </table>
          </div>
        </div>

        {/* Right Side: Checkout Panel */}
        <div className="panel" style={{ flex: 1, background: 'var(--gray-50)', border: '1px solid var(--gray-200)', marginTop: 0 }}>
          <h2 style={{marginTop: 0, marginBottom: '16px'}}>Checkout</h2>
          
          <div className="form-group">
            <label>Customer (Phone or Name)</label>
            <input 
              type="text" 
              className="form-control" 
              placeholder="Leave blank for Walk-in..." 
              value={customerSearch}
              onChange={e => setCustomerSearch(e.target.value)}
              list="customer-suggestions"
            />
            <datalist id="customer-suggestions">
              {customers.map(c => <option key={c.id} value={c.phone}>{c.name}</option>)}
            </datalist>
            <div style={{ marginTop: '8px', fontSize: '0.9rem', color: selectedCustomer ? 'var(--blue)' : 'var(--gray-600)' }}>
              {selectedCustomer ? 
                <strong>✓ Found: {selectedCustomer.name} {selectedCustomer.member ? '⭐ (Member)' : ''}</strong> 
                : 'Walk-in / General Customer'}
            </div>
          </div>

          <div className="form-group" style={{marginTop: '16px'}}>
            <label>Payment Method</label>
            <select className="form-control" value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)}>
              <option value="CASH">Cash (เงินสด)</option>
              <option value="TRANSFER">Transfer (โอนเงิน)</option>
              <option value="CARD">Credit Card (บัตรเครดิต)</option>
            </select>
          </div>

          <hr style={{margin: '24px 0', borderColor: 'var(--gray-200)'}} />

          <div style={{display: 'flex', justifyContent: 'space-between', marginBottom: '8px'}}>
            <span>Subtotal:</span>
            <span>{formatMoney(total)}</span>
          </div>
          {discountLabel && <div style={{display: 'flex', justifyContent: 'space-between', color: 'var(--green)', marginBottom: '8px'}}>
            <span>{discountLabel}:</span>
            <span>-{formatMoney(total - finalTotal)}</span>
          </div>}
          <div style={{display: 'flex', justifyContent: 'space-between', fontSize: '1.4rem', fontWeight: 'bold', marginTop: '16px', borderTop: '2px solid var(--gray-800)', paddingTop: '16px'}}>
            <span>Total:</span>
            <span>{formatMoney(finalTotal)}</span>
          </div>

          <button 
            type="button" 
            className="btn btn-primary" 
            style={{width: '100%', marginTop: '24px', padding: '16px', fontSize: '1.1rem'}}
            disabled={saving || lines.length === 0}
            onClick={submit}
          >
            {saving ? 'Processing...' : 'Pay & Complete Order'}
          </button>
        </div>

      </div>
    )}</section>
  </>;
}

export default OrderForm;

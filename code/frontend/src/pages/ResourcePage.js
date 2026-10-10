import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import * as categoryApi from '../api/categories';
import * as customerApi from '../api/customers';
import * as productApi from '../api/products';
import * as supplierApi from '../api/suppliers';

export const resourceConfig = {
  categories: {
    label: 'Categories', singular: 'Category',
    api: { list: categoryApi.getCategories, get: categoryApi.getCategory, create: categoryApi.createCategory, update: categoryApi.updateCategory, delete: categoryApi.deleteCategory },
    fields: [
      { name: 'name', label: 'Category name', required: true, maxLength: 100 },
      { name: 'description', label: 'Description', type: 'textarea', maxLength: 255, wide: true },
    ],
    columns: [['name', 'Category'], ['description', 'Description']],
  },
  suppliers: {
    label: 'Suppliers', singular: 'Supplier',
    api: { list: supplierApi.getSuppliers, get: supplierApi.getSupplier, create: supplierApi.createSupplier, update: supplierApi.updateSupplier, delete: supplierApi.deleteSupplier },
    fields: [
      { name: 'name', label: 'Supplier name', required: true, maxLength: 100 },
      { name: 'phone', label: 'Phone', maxLength: 20 },
      { name: 'email', label: 'Email', type: 'email', maxLength: 150 },
      { name: 'address', label: 'Address', type: 'textarea', maxLength: 255, wide: true },
    ],
    columns: [['name', 'Supplier'], ['phone', 'Phone'], ['email', 'Email'], ['address', 'Address']],
  },
  customers: {
    label: 'Customers', singular: 'Customer',
    api: { list: customerApi.getCustomers, get: customerApi.getCustomer, create: customerApi.createCustomer, update: customerApi.updateCustomer, delete: customerApi.deleteCustomer },
    fields: [
      { name: 'name', label: 'Full name', required: true, maxLength: 150 },
      { name: 'phone', label: 'Phone', required: true, maxLength: 20 },
      { name: 'email', label: 'Email', type: 'email', maxLength: 150 },
      { name: 'member', label: 'Membership', type: 'checkbox' },
      { name: 'address', label: 'Address', type: 'textarea', maxLength: 500, wide: true },
    ],
    columns: [['name', 'Customer'], ['phone', 'Phone'], ['email', 'Email'], ['member', 'Member', (value) => value ? 'Yes' : 'No']],
  },
  products: {
    label: 'Products', singular: 'Product',
    api: { 
      list: localStorage.getItem('role') === 'CASHIER' ? productApi.getProducts : productApi.getProductsAdmin, 
      get: localStorage.getItem('role') === 'CASHIER' ? productApi.getProduct : productApi.getProductAdmin, 
      create: productApi.createProduct, 
      update: productApi.updateProduct, 
      delete: productApi.deleteProduct 
    },
    readOnly: localStorage.getItem('role') === 'CASHIER',
    paginated: true,
    fields: [
      { name: 'sku', label: 'SKU', required: true, maxLength: 100 },
      { name: 'name', label: 'Product name', required: true, maxLength: 150 },
      { name: 'unit', label: 'Unit', required: true, maxLength: 50 },
      { name: 'price', label: 'Selling price', type: 'number', required: true, min: 0.01, step: '0.01' },
      { name: 'costPrice', label: 'Cost price', type: 'number', required: true, min: 0.01, step: '0.01' },
      { name: 'minimumStock', label: 'Minimum stock', type: 'number', required: true, min: 1, step: '1' },
      { name: 'categoryId', label: 'Category', type: 'select', required: true, loadOptions: categoryApi.getCategories },
      { name: 'supplierId', label: 'Supplier', type: 'select', required: true, loadOptions: supplierApi.getSuppliers },
      { name: 'description', label: 'Description', type: 'textarea', maxLength: 500, wide: true },
    ],
    columns: [['sku', 'SKU'], ['name', 'Product'], ['price', 'Price', (value) => formatMoney(value)], ['quantity', 'In Stock']],
  },
};

export function formatMoney(value) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'THB', maximumFractionDigits: 2 }).format(Number(value || 0));
}

export function formatDate(value) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

export function PageHeader({ eyebrow, title, subtitle, action }) {
  return (
    <header className="page-header">
      <div>
        {eyebrow && <p className="eyebrow">{eyebrow}</p>}
        <h1>{title}</h1>
        {subtitle && <p className="page-subtitle">{subtitle}</p>}
      </div>
      {action}
    </header>
  );
}

export function Notice({ children, type = 'error' }) {
  return children ? <div className={`notice ${type === 'error' ? 'notice-error' : `notice-${type}`}`} role="alert">{children}</div> : null;
}

export function getErrorMessage(error) {
  const data = error?.response?.data;
  if (typeof data === 'string') return data;
  return data?.message || data?.error || error?.message || 'Something went wrong. Please try again.';
}

function unwrapList(data) {
  if (Array.isArray(data)) return data;
  return data?.content || data?.items || [];
}

function ResourceListPage({ configKey }) {
  const config = resourceConfig[configKey];
  const [rows, setRows] = useState([]);
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(0);
  const [pageCount, setPageCount] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [refresh, setRefresh] = useState(0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    const params = config.paginated ? { page, size: 10, sortBy: 'id', direction: 'desc', keyword: query || undefined } : undefined;
    config.api.list(params).then(({ data }) => {
      if (!active) return;
      setRows(unwrapList(data));
      setPageCount(Math.max(1, data?.totalPages || 1));
    }).catch((requestError) => {
      if (active) setError(getErrorMessage(requestError));
    }).finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [config, page, query, refresh]);

  const visibleRows = config.paginated || !query.trim()
    ? rows
    : rows.filter((row) => Object.values(row).some((value) => String(value ?? '').toLowerCase().includes(query.toLowerCase())));

  const remove = async (row) => {
    if (!window.confirm(`Delete ${config.singular.toLowerCase()} "${row.name || row.sku || row.id}"?`)) return;
    try {
      await config.api.delete(row.id);
      toast.success(`${config.singular} deleted`);
      setRefresh((value) => value + 1);
    } catch (requestError) {
      toast.error(getErrorMessage(requestError));
    }
  };

  return (
    <>
      <PageHeader eyebrow="Catalog" title={config.label} subtitle={`Manage ${config.label.toLowerCase()} records.`}
        action={<Link className="btn btn-primary" to={`/${configKey}/new`}>＋ Add {config.singular}</Link>} />
      <section className="panel">
        <div className="toolbar">
          <input className="form-control search-input" value={query} placeholder={`Search ${config.label.toLowerCase()}`}
            onChange={(event) => { setQuery(event.target.value); setPage(0); }} aria-label={`Search ${config.label}`} />
          <span className="page-subtitle">{config.paginated ? `${rows.length} shown` : `${visibleRows.length} records`}</span>
        </div>
        <Notice>{error}</Notice>
        {loading ? <div className="loading">Loading {config.label.toLowerCase()}…</div> : visibleRows.length === 0 ? (
          <div className="empty-state"><strong>No {config.label.toLowerCase()} found</strong>{query ? 'Try a different search.' : 'Create a record to get started.'}</div>
        ) : (
          <div className="table-wrap"><table>
            <thead><tr>{config.columns.map(([, label]) => <th key={label}>{label}</th>)}{!config.readOnly && <th>Actions</th>}</tr></thead>
            <tbody>{visibleRows.map((row) => (
              <tr key={row.id}>
                {config.columns.map(([key, label, render]) => <td key={label}>{render ? render(row[key], row) : (row[key] ?? '—')}</td>)}
                {!config.readOnly && <td><div className="row-actions">
                  <Link className="btn btn-secondary btn-small" to={`/${configKey}/${row.id}/edit`} aria-label={`Edit ${row.name || row.sku}`}>Edit</Link>
                  <button className="btn btn-danger btn-small" onClick={() => remove(row)}>Delete</button>
                </div></td>}
              </tr>
            ))}</tbody>
          </table></div>
        )}
        {config.paginated && pageCount > 1 && <div className="pagination">
          <button className="btn btn-secondary btn-small" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>Previous</button>
          <span>Page {page + 1} of {pageCount}</span>
          <button className="btn btn-secondary btn-small" disabled={page + 1 >= pageCount} onClick={() => setPage((value) => value + 1)}>Next</button>
        </div>}
      </section>
    </>
  );
}

export function ResourceFormPage({ configKey }) {
  const config = resourceConfig[configKey];
  const { id } = useParams();
  const navigate = useNavigate();
  const editing = Boolean(id);
  const [values, setValues] = useState({});
  const [options, setOptions] = useState({});
  const [loading, setLoading] = useState(editing);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    const optionFields = config.fields.filter((field) => field.loadOptions);
    const requests = optionFields.map((field) => field.loadOptions());
    if (editing) requests.push(config.api.get(id));
    Promise.all(requests).then((results) => {
      if (!active) return;
      const nextOptions = {};
      optionFields.forEach((field, index) => { nextOptions[field.name] = unwrapList(results[index].data); });
      setOptions(nextOptions);
      if (editing) setValues(results[results.length - 1].data);
    }).catch((requestError) => active && setError(getErrorMessage(requestError)))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [config, editing, id]);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    const payload = { ...values };
    config.fields.filter((field) => field.type === 'select' || field.type === 'number').forEach((field) => {
      if (payload[field.name] !== '' && payload[field.name] != null) payload[field.name] = Number(payload[field.name]);
    });
    try {
      if (editing) await config.api.update(id, payload);
      else await config.api.create(payload);
      toast.success(`${config.singular} ${editing ? 'updated' : 'created'}`);
      navigate(`/${configKey}`);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <PageHeader eyebrow={config.label} title={`${editing ? 'Edit' : 'New'} ${config.singular}`} subtitle="Keep the record details accurate and up to date." />
      <section className="panel">
        <Notice>{error}</Notice>
        {loading ? <div className="loading">Loading record…</div> : (
          <form onSubmit={submit}>
            <div className="form-grid">{config.fields.map((field) => (
              <div className={`form-group ${field.wide ? 'span-2' : ''}`} key={field.name}>
                {field.type === 'checkbox' ? (
                  <label><input type="checkbox" checked={Boolean(values[field.name])} onChange={(event) => setValues({ ...values, [field.name]: event.target.checked })} /> {field.label}</label>
                ) : <>
                  <label htmlFor={field.name}>{field.label}{field.required && <span className="required"> *</span>}</label>
                  {field.type === 'textarea' ? <textarea id={field.name} className="form-control" maxLength={field.maxLength} value={values[field.name] || ''}
                    onChange={(event) => setValues({ ...values, [field.name]: event.target.value })} /> : field.type === 'select' ? (
                    <select id={field.name} className="form-control" required={field.required} value={values[field.name] || ''}
                      onChange={(event) => setValues({ ...values, [field.name]: event.target.value })}>
                      <option value="">Select {field.label.toLowerCase()}</option>
                      {(options[field.name] || []).map((option) => <option key={option.id} value={option.id}>{option.name}</option>)}
                    </select>
                  ) : <input id={field.name} className="form-control" type={field.type || 'text'} required={field.required}
                    min={field.min} step={field.step} maxLength={field.maxLength} value={values[field.name] ?? ''}
                    onChange={(event) => setValues({ ...values, [field.name]: event.target.value })} />}
                </>}
              </div>
            ))}</div>
            <div className="form-actions"><Link className="btn btn-secondary" to={`/${configKey}`}>Cancel</Link>
              <button className="btn btn-primary" type="submit" disabled={saving}>{saving ? 'Saving…' : editing ? 'Save changes' : `Create ${config.singular}`}</button>
            </div>
          </form>
        )}
      </section>
    </>
  );
}

export default ResourceListPage;



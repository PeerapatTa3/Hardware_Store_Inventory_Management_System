import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { AuthProvider, useAuth } from './context/AuthContext';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import Layout from './components/Layout';
import CategoryList from './pages/categories/CategoryList';
import CategoryForm from './pages/categories/CategoryForm';
import SupplierList from './pages/suppliers/SupplierList';
import SupplierForm from './pages/suppliers/SupplierForm';
import CustomerList from './pages/customers/CustomerList';
import CustomerForm from './pages/customers/CustomerForm';
import ProductList from './pages/products/ProductList';
import ProductForm from './pages/products/ProductForm';
import InventoryPage from './pages/inventory/InventoryPage';
import StockMovementList from './pages/stock-movements/StockMovementList';
import PurchaseList from './pages/purchases/PurchaseList';
import PurchaseForm from './pages/purchases/PurchaseForm';
import PurchaseDetail from './pages/purchases/PurchaseDetail';
import OrderList from './pages/orders/OrderList';
import OrderForm from './pages/orders/OrderForm';
import OrderDetail from './pages/orders/OrderDetail';
import './App.css';

function PrivateRoute({ children }) {
  const { user } = useAuth();
  const token = localStorage.getItem('token');
  return user || token ? children : <Navigate to="/login" />;
}

function RoleRoute({ roles, children }) {
  const { user } = useAuth();
  const role = user?.role || localStorage.getItem('role');
  if (!role || !roles.includes(role)) {
    return <Navigate to="/" />;
  }
  return children;
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/"
            element={
              <PrivateRoute>
                <Layout />
              </PrivateRoute>
            }
          >
            <Route index element={<DashboardPage />} />
            
            {/* Categories & Suppliers: OWNER, STOCK_MANAGER */}
            <Route path="categories" element={<RoleRoute roles={['OWNER', 'STOCK_MANAGER']}><CategoryList /></RoleRoute>} />
            <Route path="categories/new" element={<RoleRoute roles={['OWNER', 'STOCK_MANAGER']}><CategoryForm /></RoleRoute>} />
            <Route path="categories/:id/edit" element={<RoleRoute roles={['OWNER', 'STOCK_MANAGER']}><CategoryForm /></RoleRoute>} />
            
            <Route path="suppliers" element={<RoleRoute roles={['OWNER', 'STOCK_MANAGER']}><SupplierList /></RoleRoute>} />
            <Route path="suppliers/new" element={<RoleRoute roles={['OWNER', 'STOCK_MANAGER']}><SupplierForm /></RoleRoute>} />
            <Route path="suppliers/:id/edit" element={<RoleRoute roles={['OWNER', 'STOCK_MANAGER']}><SupplierForm /></RoleRoute>} />
            
            {/* Customers: OWNER, CASHIER */}
            <Route path="customers" element={<RoleRoute roles={['OWNER', 'CASHIER']}><CustomerList /></RoleRoute>} />
            <Route path="customers/new" element={<RoleRoute roles={['OWNER', 'CASHIER']}><CustomerForm /></RoleRoute>} />
            <Route path="customers/:id/edit" element={<RoleRoute roles={['OWNER', 'CASHIER']}><CustomerForm /></RoleRoute>} />
            
            {/* Products: All roles for view, OWNER/STOCK_MANAGER for edit */}
            <Route path="products" element={<ProductList />} />
            <Route path="products/new" element={<RoleRoute roles={['OWNER', 'STOCK_MANAGER']}><ProductForm /></RoleRoute>} />
            <Route path="products/:id/edit" element={<RoleRoute roles={['OWNER', 'STOCK_MANAGER']}><ProductForm /></RoleRoute>} />
            
            {/* Inventory & Stock: OWNER, STOCK_MANAGER */}
            <Route path="inventory" element={<RoleRoute roles={['OWNER', 'STOCK_MANAGER']}><InventoryPage /></RoleRoute>} />
            <Route path="stock-movements" element={<RoleRoute roles={['OWNER', 'STOCK_MANAGER']}><StockMovementList /></RoleRoute>} />
            <Route path="purchases" element={<RoleRoute roles={['OWNER', 'STOCK_MANAGER']}><PurchaseList /></RoleRoute>} />
            <Route path="purchases/new" element={<RoleRoute roles={['OWNER', 'STOCK_MANAGER']}><PurchaseForm /></RoleRoute>} />
            <Route path="purchases/:id" element={<RoleRoute roles={['OWNER', 'STOCK_MANAGER']}><PurchaseDetail /></RoleRoute>} />
            
            {/* Orders: OWNER, CASHIER */}
            <Route path="orders" element={<RoleRoute roles={['OWNER', 'CASHIER']}><OrderList /></RoleRoute>} />
            <Route path="orders/new" element={<RoleRoute roles={['OWNER', 'CASHIER']}><OrderForm /></RoleRoute>} />
            <Route path="orders/:id" element={<RoleRoute roles={['OWNER', 'CASHIER']}><OrderDetail /></RoleRoute>} />
          </Route>
        </Routes>
      </BrowserRouter>
      <ToastContainer position="top-right" autoClose={3000} />
    </AuthProvider>
  );
}

export default App;
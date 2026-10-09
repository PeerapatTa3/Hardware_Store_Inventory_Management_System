# Frontend

## Overview

The project frontend is a React single-page application in `code/frontend/`. It uses React Router for navigation, Axios for REST calls, and the existing Spring Boot API as its source of truth. The application includes authenticated routes, catalog maintenance, stock operations, purchasing and sales-order workflows.

## Run and build

Prerequisites: Node.js 18 or later, npm, and a running backend API.

```powershell
cd code/frontend
npm install
$env:REACT_APP_API_URL="http://localhost:8080"
npm start
```

The development server runs at `http://localhost:3000`. `REACT_APP_API_URL` is the backend origin, without `/api`; if omitted, the application uses `http://localhost:8080`. Configure backend CORS to allow the frontend origin.

Create a production build with:

```powershell
npm run build
```

Authentication credentials are managed by the backend. The frontend stores the returned bearer token and attaches it to API calls; an unauthorized response clears the session and redirects to sign in.

## Screen and API mapping

| Screen | Main operations | REST API |
|---|---|---|
| Overview | Summary of products, available stock, low-stock items, purchases and sales orders; recent order list | Products, inventory, purchases, orders, suppliers, customers |
| Categories | List, search, create, edit and delete | `/api/v1/categories` |
| Suppliers | List, search, create, edit and delete | `/api/v1/suppliers` |
| Customers | List, search, create, edit and delete | `/api/v1/customers` |
| Products | Paginated list, search, create, edit and delete | `/api/v1/products` and the authenticated `/admin` routes |
| Inventory | View on-hand/reserved/available quantities and submit an adjustment | `/api/v1/inventory/products/{productId}` |
| Stock movements | Review movement type, quantity, reference and timestamp | `/api/v1/stock-movements` |
| Purchases | Create purchase orders, view details and receive a pending purchase | `/api/v1/purchases` and `/api/v1/purchases/{id}/receive` |
| Sales orders | Create and view orders; change status using allowed transitions | `/api/v1/orders` and `/api/v1/orders/{id}/status` |
| Sign in | Authenticate and establish a browser session | `/api/auth/login` |

## Main workflows

- A purchase order contains a supplier and one or more product lines. Receiving a pending purchase calls the backend receive endpoint; the backend is responsible for updating inventory and recording the corresponding stock movement.
- Inventory adjustment submits an absolute non-negative stock quantity and an optional reason. The UI prevents adjustments below the current reserved quantity.
- A sales order contains one or more products, quantities, and optional customer/shipping information. Its status controls follow the backend state transitions: `PENDING` to `CONFIRMED` or `CANCELLED`, `CONFIRMED` to `SHIPPED` or `CANCELLED`, and `SHIPPED` to `COMPLETED`.
- Product listing uses server pagination, sorting and keyword search. Other collection screens search the returned collection in the browser because their current API endpoints return lists.

## Implementation structure

| Path | Responsibility |
|---|---|
| `src/App.js` | Route definitions and authenticated route boundary |
| `src/components/Layout.js` | Responsive navigation shell and sign-out action |
| `src/context/AuthContext.js` | Login state and local-storage session |
| `src/api/` | Axios client and endpoint-specific request functions |
| `src/pages/ResourcePage.js` | Shared catalog CRUD screens, field definitions and formatting helpers |
| `src/pages/` | Dashboard, authentication, inventory and transaction views |
| `src/App.css` | Shared visual system and responsive layouts |

## Assignment coverage

This frontend addresses the worksheet's React requirement and connects the UI to the available REST API. It provides usable CRUD forms for multiple resources, displays API validation/server errors, and uses the backend for authentication and transaction state changes. Backend requirements such as deployment, API documentation, database design, server-side validation and testing remain the responsibility of their respective project components; this document does not claim those project-wide requirements are complete.
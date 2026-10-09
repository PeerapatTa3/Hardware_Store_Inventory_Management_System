# Hardware Store Frontend

React single-page application for the Hardware Store Inventory Management System. It uses the Spring Boot REST API for authentication, catalog maintenance, inventory operations, purchases, and sales orders.

## Requirements

- Node.js 18 or later and npm
- Backend API running and reachable from the browser

## Run locally

```powershell
cd code/frontend
npm install
$env:REACT_APP_API_URL="http://localhost:8080"
npm start
```

The development server opens at `http://localhost:3000`. Set `REACT_APP_API_URL` to the backend origin without an `/api` suffix when the API runs elsewhere. The backend must allow the frontend origin through CORS.

## Build

```powershell
npm run build
```

## Authentication

Sign in with a user configured by the backend. The access token and user details are stored in browser local storage and the token is attached to API requests. A `401` response clears the session and returns the user to the sign-in screen.

## Screens

- Overview: product, stock, low-stock, purchase and order summaries with recent sales orders.
- Catalog: create, read, update, delete and search categories, suppliers, customers and products.
- Inventory: inspect on-hand, reserved and available quantities and submit a stock adjustment.
- Stock movements: review inbound, outbound and adjustment records.
- Purchases: create purchase orders, inspect line items and receive a pending purchase into inventory.
- Sales orders: create orders, inspect line items and advance orders through valid backend statuses.

Product catalog requests use the backend's paginated and sortable product endpoint. Other lists use the collection endpoints currently exposed by the backend.

## Configuration

The API origin is read from `REACT_APP_API_URL`; when unset it defaults to `http://localhost:8080`. API request modules are in `src/api/`, shared authentication state is in `src/context/`, and route views are in `src/pages/`.

For the screen-to-endpoint mapping and the assignment criteria covered by this frontend, see [doc/เอกสารที่จำเป็นชั่วคราว/frontend.md](../../doc/เอกสารที่จำเป็นชั่วคราว/frontend.md).
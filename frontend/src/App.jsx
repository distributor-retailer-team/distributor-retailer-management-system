
import { useEffect, useState } from "react";
import "./App.css";

const API = "http://localhost:8080/api/products";

export default function App() {
  const [username, setUsername] = useState("apiuser");
  const [password, setPassword] = useState("");
  const [authenticated, setAuthenticated] = useState(false);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [editingId, setEditingId] = useState(null);
  const [search, setSearch] = useState("");
  const [form, setForm] = useState({
    name: "",
    sku: "",
    price: "",
    stockQuantity: "",
    minimumStockLevel: ""
  });

  const headers = {
    "Content-Type": "application/json",
    Authorization: "Basic " + btoa(username + ":" + password)
  };

  async function request(url, options = {}) {
    const response = await fetch(url, {
      ...options,
      headers: { ...headers, ...options.headers }
    });

    if (response.status === 401 || response.status === 403) {
      throw new Error("Login failed. Check your API username and password.");
    }
    if (!response.ok) {
      throw new Error("Request failed: " + response.status);
    }
    if (response.status === 204) return null;
    return response.json();
  }

  async function loadProducts() {
    setLoading(true);
    setError("");
    try {
      const data = await request(API);
      setProducts(data);
      setAuthenticated(true);
    } catch (e) {
      setError(e.message);
      setAuthenticated(false);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (authenticated) loadProducts();
  }, [authenticated]);

  function updateField(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function saveProduct(e) {
    e.preventDefault();
    setError("");

    const payload = {
      name: form.name.trim(),
      sku: form.sku.trim(),
      price: Number(form.price),
      stockQuantity: Number(form.stockQuantity),
      minimumStockLevel: Number(form.minimumStockLevel)
    };

    if (!payload.name || !payload.sku) {
      setError("Please enter product name and SKU.");
      return;
    }

    try {
      if (editingId !== null) {
        await request(`${API}/${editingId}`, {
          method: "PUT",
          body: JSON.stringify(payload)
        });
      } else {
        await request(API, {
          method: "POST",
          body: JSON.stringify(payload)
        });
      }

      setForm({
        name: "",
        sku: "",
        price: "",
        stockQuantity: "",
        minimumStockLevel: ""
      });
      setEditingId(null);
      await loadProducts();
    } catch (e) {
      setError(e.message);
    }
  }

  function editProduct(product) {
    setEditingId(product.id);
    setForm({
      name: product.name ?? "",
      sku: product.sku ?? "",
      price: product.price ?? "",
      stockQuantity: product.stockQuantity ?? "",
      minimumStockLevel: product.minimumStockLevel ?? ""
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function deleteProduct(id) {
    if (!window.confirm("Delete this product?")) return;

    try {
      await request(`${API}/${id}`, { method: "DELETE" });
      await loadProducts();
    } catch (e) {
      setError(e.message);
    }
  }

  const filteredProducts = products.filter((p) =>
    `${p.name} ${p.sku}`.toLowerCase().includes(search.toLowerCase())
  );

  if (!authenticated) {
    return (
      <main className="app-shell login-shell">
        <form
          className="login-card"
          onSubmit={(e) => {
            e.preventDefault();
            setAuthenticated(true);
          }}
        >
          <div className="brand-mark">DR</div>
          <h1>Distributor-Retailer</h1>
          <p>Management System · Admin Login</p>

          <label>API Username</label>
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />

          <label>API Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          {error && <p className="error-message">{error}</p>}
          <button className="primary-button" type="submit">
            Connect to Backend
          </button>
          <small>Your credentials are not saved in the source code.</small>
        </form>
      </main>
    );
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">DR</span>
          <span>TradeBridge</span>
        </div>
        <p className="nav-heading">WORKSPACE</p>
        <div className="nav-item active">▦ &nbsp; Dashboard</div>
        <div className="nav-item">▣ &nbsp; Products</div>
        <div className="nav-item">♙ &nbsp; Distributors</div>
        <div className="nav-item">♧ &nbsp; Retailers</div>
        <div className="nav-item">⇄ &nbsp; Orders</div>
        <div className="sidebar-bottom">Inventory Management</div>
      </aside>

      <section className="main-content">
        <header className="topbar">
          <div>
            <h2>Product Management</h2>
            <p>Manage your inventory and stock levels.</p>
          </div>
          <button className="secondary-button" onClick={loadProducts}>
            ↻ Refresh
          </button>
        </header>

        {error && <div className="error-banner">{error}</div>}

        <section className="stats-grid">
          <article className="stat-card">
            <span>Total Products</span>
            <strong>{products.length}</strong>
          </article>
          <article className="stat-card">
            <span>Total Units in Stock</span>
            <strong>
              {products.reduce((sum, p) => sum + Number(p.stockQuantity || 0), 0)}
            </strong>
          </article>
          <article className="stat-card">
            <span>Low Stock Items</span>
            <strong>
              {products.filter(
                (p) =>
                  Number(p.stockQuantity) <= Number(p.minimumStockLevel)
              ).length}
            </strong>
          </article>
        </section>

        <section className="panel">
          <h3>{editingId !== null ? "Edit Product" : "Add New Product"}</h3>
          <form className="product-form" onSubmit={saveProduct}>
            <input name="name" placeholder="Product name" value={form.name} onChange={updateField} required />
            <input name="sku" placeholder="SKU" value={form.sku} onChange={updateField} required />
            <input name="price" type="number" min="0" step="0.01" placeholder="Price (₹)" value={form.price} onChange={updateField} required />
            <input name="stockQuantity" type="number" min="0" placeholder="Stock quantity" value={form.stockQuantity} onChange={updateField} required />
            <input name="minimumStockLevel" type="number" min="0" placeholder="Minimum stock" value={form.minimumStockLevel} onChange={updateField} required />
            <button className="primary-button" type="submit">
              {editingId !== null ? "Save Changes" : "+ Add Product"}
            </button>
            {editingId !== null && (
              <button
                className="secondary-button"
                type="button"
                onClick={() => {
                  setEditingId(null);
                  setForm({ name: "", sku: "", price: "", stockQuantity: "", minimumStockLevel: "" });
                }}
              >
                Cancel
              </button>
            )}
          </form>
        </section>

        <section className="panel">
          <div className="table-heading">
            <div>
              <h3>Inventory</h3>
              <p>Products stored in your database</p>
            </div>
            <input
              className="search-input"
              placeholder="Search name or SKU..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          {loading ? (
            <p>Loading products...</p>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>SKU</th>
                    <th>Price</th>
                    <th>Stock</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredProducts.map((p) => (
                    <tr key={p.id}>
                      <td>{p.name}</td>
                      <td>{p.sku}</td>
                      <td>₹{Number(p.price).toLocaleString("en-IN")}</td>
                      <td>{p.stockQuantity}</td>
                      <td>
                        <span className={
                          Number(p.stockQuantity) <= Number(p.minimumStockLevel)
                            ? "status low"
                            : "status in-stock"
                        }>
                          {Number(p.stockQuantity) <= Number(p.minimumStockLevel)
                            ? "Low Stock"
                            : "In Stock"}
                        </span>
                      </td>
                      <td className="actions">
                        <button className="edit-button" onClick={() => editProduct(p)}>Edit</button>
                        <button className="delete-button" onClick={() => deleteProduct(p.id)}>Delete</button>
                      </td>
                    </tr>
                  ))}
                  {filteredProducts.length === 0 && (
                    <tr>
                      <td colSpan="6" className="empty-state">
                        {search ? "No matching products found." : "No products yet. Add your first product above."}
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </section>
        <p className="footer-note">TradeBridge · Distributor-Retailer Management System</p>
      </section>
    </main>
  );
}

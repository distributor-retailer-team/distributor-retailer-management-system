import { useEffect, useState } from "react";
import "./App.css";

const PRODUCTS_API = "http://localhost:8080/api/products";
const DISTRIBUTORS_API = "http://localhost:8080/api/distributors";

export default function App() {
  const [username, setUsername] = useState("apiuser");
  const [password, setPassword] = useState("");
  const [authenticated, setAuthenticated] = useState(false);
  const [page, setPage] = useState("Dashboard");

  const [products, setProducts] = useState([]);
  const [distributors, setDistributors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [search, setSearch] = useState("");
  const [editingProductId, setEditingProductId] = useState(null);
  const [editingDistributorId, setEditingDistributorId] = useState(null);

  const emptyProduct = {
    name: "",
    sku: "",
    price: "",
    stockQuantity: "",
    minimumStockLevel: ""
  };
  const emptyDistributor = {
    name: "",
    companyName: "",
    email: "",
    phone: "",
    address: ""
  };

  const [productForm, setProductForm] = useState(emptyProduct);
  const [distributorForm, setDistributorForm] = useState(emptyDistributor);

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
      let message = "Request failed: " + response.status;
      try {
        const body = await response.json();
        if (body.message) message = body.message;
        else if (body.error) message = body.error;
      } catch {
        // Keep the status-based message.
      }
      throw new Error(message);
    }
    if (response.status === 204) return null;
    return response.json();
  }

  async function loadProducts() {
    setLoading(true);
    setError("");
    try {
      const data = await request(PRODUCTS_API);
      setProducts(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function loadDistributors() {
    setLoading(true);
    setError("");
    try {
      const url = search.trim()
        ? `${DISTRIBUTORS_API}?search=${encodeURIComponent(search.trim())}`
        : DISTRIBUTORS_API;
      const data = await request(url);
      setDistributors(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function connectToBackend(e) {
    e.preventDefault();
    setError("");
    try {
      const data = await request(PRODUCTS_API);
      setProducts(Array.isArray(data) ? data : []);
      setAuthenticated(true);
      setPage("Dashboard");
    } catch (e) {
      setError(e.message);
    }
  }

  useEffect(() => {
    if (!authenticated) return;
    if (page === "Products" || page === "Dashboard") loadProducts();
    if (page === "Distributors") loadDistributors();
  }, [authenticated, page]);

  function updateProductField(e) {
    setProductForm({ ...productForm, [e.target.name]: e.target.value });
  }

  function updateDistributorField(e) {
    setDistributorForm({ ...distributorForm, [e.target.name]: e.target.value });
  }

  async function saveProduct(e) {
    e.preventDefault();
    setError("");
    setNotice("");

    const payload = {
      name: productForm.name.trim(),
      sku: productForm.sku.trim(),
      price: Number(productForm.price),
      stockQuantity: Number(productForm.stockQuantity),
      minimumStockLevel: Number(productForm.minimumStockLevel)
    };

    if (!payload.name || !payload.sku) {
      setError("Please enter product name and SKU.");
      return;
    }

    try {
      if (editingProductId !== null) {
        await request(`${PRODUCTS_API}/${editingProductId}`, {
          method: "PUT",
          body: JSON.stringify(payload)
        });
      } else {
        await request(PRODUCTS_API, {
          method: "POST",
          body: JSON.stringify(payload)
        });
      }
      setProductForm(emptyProduct);
      setEditingProductId(null);
      setNotice("Product saved successfully.");
      await loadProducts();
    } catch (e) {
      setError(e.message);
    }
  }

  function editProduct(product) {
    setEditingProductId(product.id);
    setProductForm({
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
      await request(`${PRODUCTS_API}/${id}`, { method: "DELETE" });
      setNotice("Product deleted.");
      await loadProducts();
    } catch (e) {
      setError(e.message);
    }
  }

  async function saveDistributor(e) {
    e.preventDefault();
    setError("");
    setNotice("");

    const payload = {
      name: distributorForm.name.trim(),
      companyName: distributorForm.companyName.trim(),
      email: distributorForm.email.trim(),
      phone: distributorForm.phone.trim(),
      address: distributorForm.address.trim()
    };

    if (!payload.name || !payload.companyName || !payload.email) {
      setError("Please enter distributor name, company name and email.");
      return;
    }

    try {
      if (editingDistributorId !== null) {
        await request(`${DISTRIBUTORS_API}/${editingDistributorId}`, {
          method: "PUT",
          body: JSON.stringify(payload)
        });
      } else {
        await request(DISTRIBUTORS_API, {
          method: "POST",
          body: JSON.stringify(payload)
        });
      }
      setDistributorForm(emptyDistributor);
      setEditingDistributorId(null);
      setNotice("Distributor saved successfully.");
      await loadDistributors();
    } catch (e) {
      setError(e.message);
    }
  }

  function editDistributor(distributor) {
    setEditingDistributorId(distributor.id);
    setDistributorForm({
      name: distributor.name ?? "",
      companyName: distributor.companyName ?? "",
      email: distributor.email ?? "",
      phone: distributor.phone ?? "",
      address: distributor.address ?? ""
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function deleteDistributor(id) {
    if (!window.confirm("Delete this distributor?")) return;
    setError("");
    setNotice("");
    try {
      await request(`${DISTRIBUTORS_API}/${id}`, { method: "DELETE" });
      setNotice("Distributor deleted.");
      await loadDistributors();
    } catch (e) {
      setError(e.message);
    }
  }

  const filteredProducts = products.filter((p) =>
    `${p.name ?? ""} ${p.sku ?? ""}`.toLowerCase().includes(search.toLowerCase())
  );

  if (!authenticated) {
    return (
      <main className="app-shell login-shell">
        <form className="login-card" onSubmit={connectToBackend}>
          <div className="brand-mark">DR</div>
          <h1>Distributor-Retailer</h1>
          <p>Management System · Admin Login</p>
          <label>API Username</label>
          <input value={username} onChange={(e) => setUsername(e.target.value)} required />
          <label>API Password</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          {error && <p className="error-message">{error}</p>}
          <button className="primary-button" type="submit">Connect to Backend</button>
          <small>Your credentials are not saved in the source code.</small>
        </form>
      </main>
    );
  }

  const navItems = [
    ["Dashboard", "▦"],
    ["Products", "▣"],
    ["Distributors", "♙"],
    ["Retailers", "♧"],
    ["Orders", "⇄"]
  ];

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">DR</span>
          <span>TradeBridge</span>
        </div>
        <p className="nav-heading">WORKSPACE</p>
        {navItems.map(([label, symbol]) => (
          <button
            key={label}
            type="button"
            className={`nav-item ${page === label ? "active" : ""}`}
            onClick={() => {
              setPage(label);
              setSearch("");
              setError("");
              setNotice("");
            }}
          >
            {symbol} &nbsp; {label}
          </button>
        ))}
        <div className="sidebar-bottom">Inventory Management</div>
      </aside>

      <section className="main-content">
        <header className="topbar">
          <div>
            <h2>{page === "Dashboard" ? "Product Management" : page === "Products" ? "Product Management" : page === "Distributors" ? "Distributor Management" : page}</h2>
            <p>
              {page === "Distributors"
                ? "Manage your distributors and company details."
                : page === "Products" || page === "Dashboard"
                ? "Manage your inventory and stock levels."
                : `${page} management will be added next.`}
            </p>
          </div>
          <button
            className="secondary-button"
            type="button"
            onClick={() => page === "Distributors" ? loadDistributors() : loadProducts()}
          >
            ↻ Refresh
          </button>
        </header>

        {error && <div className="error-banner">{error}</div>}
        {notice && <div className="success-banner">{notice}</div>}

        {(page === "Dashboard" || page === "Products") && (
          <>
            <section className="stats-grid">
              <article className="stat-card">
                <span>Total Products</span>
                <strong>{products.length}</strong>
              </article>
              <article className="stat-card">
                <span>Total Units in Stock</span>
                <strong>{products.reduce((sum, p) => sum + Number(p.stockQuantity || 0), 0)}</strong>
              </article>
              <article className="stat-card">
                <span>Low Stock Items</span>
                <strong>{products.filter((p) => Number(p.stockQuantity) <= Number(p.minimumStockLevel)).length}</strong>
              </article>
            </section>

            <section className="panel">
              <h3>{editingProductId !== null ? "Edit Product" : "Add New Product"}</h3>
              <form className="product-form" onSubmit={saveProduct}>
                <input name="name" placeholder="Product name" value={productForm.name} onChange={updateProductField} required />
                <input name="sku" placeholder="SKU" value={productForm.sku} onChange={updateProductField} required />
                <input name="price" type="number" min="0" step="0.01" placeholder="Price (₹)" value={productForm.price} onChange={updateProductField} required />
                <input name="stockQuantity" type="number" min="0" placeholder="Stock quantity" value={productForm.stockQuantity} onChange={updateProductField} required />
                <input name="minimumStockLevel" type="number" min="0" placeholder="Minimum stock" value={productForm.minimumStockLevel} onChange={updateProductField} required />
                <button className="primary-button" type="submit">{editingProductId !== null ? "Save Changes" : "+ Add Product"}</button>
                {editingProductId !== null && (
                  <button className="secondary-button" type="button" onClick={() => { setEditingProductId(null); setProductForm(emptyProduct); }}>Cancel</button>
                )}
              </form>
            </section>

            <section className="panel">
              <div className="table-heading">
                <div><h3>Inventory</h3><p>Products stored in your database</p></div>
                <input className="search-input" placeholder="Search name or SKU..." value={search} onChange={(e) => setSearch(e.target.value)} />
              </div>
              {loading ? <p>Loading products...</p> : (
                <div className="table-wrap">
                  <table>
                    <thead><tr><th>Product</th><th>SKU</th><th>Price</th><th>Stock</th><th>Status</th><th>Actions</th></tr></thead>
                    <tbody>
                      {filteredProducts.map((p) => (
                        <tr key={p.id}>
                          <td>{p.name}</td><td>{p.sku}</td><td>₹{Number(p.price).toLocaleString("en-IN")}</td><td>{p.stockQuantity}</td>
                          <td><span className={Number(p.stockQuantity) <= Number(p.minimumStockLevel) ? "status low" : "status in-stock"}>{Number(p.stockQuantity) <= Number(p.minimumStockLevel) ? "Low Stock" : "In Stock"}</span></td>
                          <td className="actions"><button className="edit-button" onClick={() => editProduct(p)}>Edit</button><button className="delete-button" onClick={() => deleteProduct(p.id)}>Delete</button></td>
                        </tr>
                      ))}
                      {filteredProducts.length === 0 && <tr><td colSpan="6" className="empty-state">{search ? "No matching products found." : "No products yet. Add your first product above."}</td></tr>}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          </>
        )}

        {page === "Distributors" && (
          <>
            <section className="stats-grid">
              <article className="stat-card"><span>Total Distributors</span><strong>{distributors.length}</strong></article>
            </section>
            <section className="panel">
              <h3>{editingDistributorId !== null ? "Edit Distributor" : "Add New Distributor"}</h3>
              <form className="product-form" onSubmit={saveDistributor}>
                <input name="name" placeholder="Distributor name" value={distributorForm.name} onChange={updateDistributorField} required />
                <input name="companyName" placeholder="Company name" value={distributorForm.companyName} onChange={updateDistributorField} required />
                <input name="email" type="email" placeholder="Email address" value={distributorForm.email} onChange={updateDistributorField} required />
                <input name="phone" placeholder="Phone number" value={distributorForm.phone} onChange={updateDistributorField} />
                <input name="address" placeholder="Address" value={distributorForm.address} onChange={updateDistributorField} />
                <button className="primary-button" type="submit">{editingDistributorId !== null ? "Save Changes" : "+ Add Distributor"}</button>
                {editingDistributorId !== null && <button className="secondary-button" type="button" onClick={() => { setEditingDistributorId(null); setDistributorForm(emptyDistributor); }}>Cancel</button>}
              </form>
            </section>
            <section className="panel">
              <div className="table-heading">
                <div><h3>Distributors</h3><p>Distributor details stored in your database</p></div>
                <input className="search-input" placeholder="Search distributors..." value={search} onChange={(e) => { setSearch(e.target.value); }} onKeyDown={(e) => { if (e.key === "Enter") loadDistributors(); }} />
              </div>
              <div className="table-wrap">
                {loading ? <p>Loading distributors...</p> : (
                  <table>
                    <thead><tr><th>Name</th><th>Company</th><th>Email</th><th>Phone</th><th>Address</th><th>Actions</th></tr></thead>
                    <tbody>
                      {distributors.map((d) => (
                        <tr key={d.id}>
                          <td>{d.name}</td><td>{d.companyName}</td><td>{d.email}</td><td>{d.phone || "—"}</td><td>{d.address || "—"}</td>
                          <td className="actions"><button className="edit-button" onClick={() => editDistributor(d)}>Edit</button><button className="delete-button" onClick={() => deleteDistributor(d.id)}>Delete</button></td>
                        </tr>
                      ))}
                      {distributors.length === 0 && <tr><td colSpan="6" className="empty-state">{search ? "No matching distributors found." : "No distributors yet. Add your first distributor above."}</td></tr>}
                    </tbody>
                  </table>
                )}
              </div>
            </section>
          </>
        )}

        {page !== "Dashboard" && page !== "Products" && page !== "Distributors" && (
          <section className="panel"><h3>{page}</h3><p>This section will be implemented next.</p></section>
        )}
        <p className="footer-note">TradeBridge · Distributor-Retailer Management System</p>
      </section>
    </main>
  );
}

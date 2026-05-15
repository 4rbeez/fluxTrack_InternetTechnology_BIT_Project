// =============================================================
// fluxTrack - products.js
// Drives the Product Overview page:
//   - fetches products and partners from the backend
//   - renders the table (with computed Status from quantity)
//   - search + filter
//   - +/- quantity adjustment (PUT /product/{id})
//   - "Add new product" modal (POST /product/add)
// =============================================================

requireAuth();

let allProducts = [];
const partnerLookup = {};   // { partnerID: partnerName }

// -------------------------------------------------------------
// Data loading
// -------------------------------------------------------------
async function loadPartners() {
    try {
        const res = await authFetch('/partner/');
        if (!res || !res.ok) return;
        const partners = await res.json();
        partners.forEach(p => { partnerLookup[p.partnerID] = p.partnerName; });
    } catch (err) {
        console.error('Failed to load partners', err);
    }
}

async function loadProducts() {
    const tbody = document.getElementById('products-tbody');
    try {
        const res = await authFetch('/product/');
        if (!res) return;
        if (!res.ok) {
            tbody.innerHTML = `<tr><td colspan="8" class="table-empty">Failed to load products (HTTP ${res.status})</td></tr>`;
            return;
        }
        allProducts = await res.json();
        applySearchFilter();
    } catch (err) {
        console.error(err);
        tbody.innerHTML = `<tr><td colspan="8" class="table-empty">Failed to load products</td></tr>`;
    }
}

// -------------------------------------------------------------
// Rendering
// -------------------------------------------------------------
function renderProducts(products) {
    const tbody = document.getElementById('products-tbody');
    if (!products.length) {
        tbody.innerHTML = `<tr><td colspan="8" class="table-empty">No products found</td></tr>`;
        return;
    }
    tbody.innerHTML = products.map(p => {
        const qty = p.productQuantity ?? 0;
        const inStock = qty > 0;
        const partnerName = partnerLookup[p.productPartnerID] ?? '—';
        const price = (p.productPrice ?? 0).toFixed(2);
        return `
            <tr>
                <td><input type="checkbox" data-id="${p.productID}"></td>
                <td>${escapeHtml(p.productSKU || '')}</td>
                <td>${escapeHtml(p.productName || '')}</td>
                <td>${escapeHtml(partnerName)}</td>
                <td>${price}</td>
                <td>${qty}</td>
                <td>
                    <button class="qty-btn" data-id="${p.productID}" data-delta="1" aria-label="Increase quantity">+</button>
                    <button class="qty-btn" data-id="${p.productID}" data-delta="-1" aria-label="Decrease quantity" ${qty <= 0 ? 'disabled' : ''}>-</button>
                </td>
                <td>
                    <span class="status-pill ${inStock ? 'in-stock' : 'out-of-stock'}">
                        ${inStock ? 'In Stock' : 'Out of Stock'}
                    </span>
                </td>
            </tr>`;
    }).join('');
}

function escapeHtml(str) {
    return String(str)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;');
}

// -------------------------------------------------------------
// Search & filter (client-side, on already-loaded products)
// -------------------------------------------------------------
function applySearchFilter() {
    const term = document.getElementById('search-input').value.trim().toLowerCase();
    const filter = document.getElementById('filter-select').value;
    const filtered = allProducts.filter(p => {
        const matchesTerm = !term ||
            (p.productName && p.productName.toLowerCase().includes(term)) ||
            (p.productSKU && p.productSKU.toLowerCase().includes(term));
        const qty = p.productQuantity ?? 0;
        const matchesFilter =
            !filter ||
            (filter === 'instock' && qty > 0) ||
            (filter === 'outofstock' && qty <= 0);
        return matchesTerm && matchesFilter;
    });
    renderProducts(filtered);
}

document.getElementById('search-btn').addEventListener('click', applySearchFilter);
document.getElementById('search-input').addEventListener('keyup', (e) => {
    if (e.key === 'Enter') applySearchFilter();
});
document.getElementById('search-input').addEventListener('input', applySearchFilter);
document.getElementById('filter-select').addEventListener('change', applySearchFilter);

// -------------------------------------------------------------
// +/- quantity buttons (event delegation on tbody)
// -------------------------------------------------------------
document.getElementById('products-tbody').addEventListener('click', async (e) => {
    const btn = e.target.closest('.qty-btn');
    if (!btn || btn.disabled) return;

    const id = btn.dataset.id;
    const delta = parseInt(btn.dataset.delta, 10);
    const product = allProducts.find(p => String(p.productID) === id);
    if (!product) return;

    const newQty = Math.max(0, (product.productQuantity ?? 0) + delta);
    if (newQty === product.productQuantity) return;

    // Build the full Product body for PUT
    const updated = {
        productID: product.productID,
        productName: product.productName,
        productSKU: product.productSKU,
        productPrice: product.productPrice,
        productQuantity: newQty,
        productPartnerID: product.productPartnerID,
    };

    btn.disabled = true;
    const res = await authFetch(`/product/${id}`, {
        method: 'PUT',
        body: JSON.stringify(updated),
    });
    btn.disabled = false;

    if (res && res.ok) {
        product.productQuantity = newQty;
        applySearchFilter();
    } else {
        console.error('Failed to update quantity', res && res.status);
    }
});

// -------------------------------------------------------------
// New Product modal
// -------------------------------------------------------------
const modal = document.getElementById('product-modal');
const openBtn = document.getElementById('open-new-product');
const cancelBtn = document.getElementById('cancel-modal');
const productForm = document.getElementById('product-form');

openBtn.addEventListener('click', () => {
    modal.classList.remove('hidden');
});

cancelBtn.addEventListener('click', closeModal);
modal.addEventListener('click', (e) => {
    if (e.target === modal) closeModal();
});

function closeModal() {
    modal.classList.add('hidden');
    productForm.reset();
    document.getElementById('form-error').textContent = '';
}

productForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const errorDiv = document.getElementById('form-error');
    errorDiv.textContent = '';

    const newProduct = {
        productName: document.getElementById('productName').value.trim(),
        productSKU: document.getElementById('productSKU').value.trim(),
        productPrice: parseFloat(document.getElementById('productPrice').value),
        productQuantity: parseInt(document.getElementById('productQuantity').value, 10),
    };

    if (!newProduct.productName || !newProduct.productSKU) {
        errorDiv.textContent = 'Product Name and SKU are required.';
        return;
    }
    if (isNaN(newProduct.productPrice) || isNaN(newProduct.productQuantity)) {
        errorDiv.textContent = 'Price and Quantity must be numbers.';
        return;
    }

    const res = await authFetch('/product/add', {
        method: 'POST',
        body: JSON.stringify(newProduct),
    });

    if (res && res.ok) {
        closeModal();
        await loadProducts();
    } else {
        errorDiv.textContent = `Could not create product${res ? ` (HTTP ${res.status})` : ''}. Check that all fields are valid.`;
    }
});

// -------------------------------------------------------------
// Initial load
// -------------------------------------------------------------
(async () => {
    await loadPartners();
    await loadProducts();
})();
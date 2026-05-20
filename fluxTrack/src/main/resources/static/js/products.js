// =============================================================
// fluxTrack - products.js
// Drives the Product Overview page:
//   - fetches products and partners from the backend
//   - renders the table (with computed Status from quantity)
//   - search + filter
//   - +/- quantity adjustment (PUT /product/{id})
//   - "Add new product" modal (POST /product/add)
//   - Edit modal reuses the same form; submit goes to PUT /product/{id}
//   - Admin-only partner picker in the modal
//   - Per-partner placeholder text in the modal (wine vs. board games)
//   - Row checkboxes with select-all + bulk delete
//   - Delete with confirmation (single + bulk)
// =============================================================

requireAuth();

let allProducts = [];
const partnerLookup = {};   // { partnerID: partnerName }
const isAdmin = getUser() === 'admin';

// Set of productIDs (as strings) currently selected via checkbox.
// Tracked separately from the DOM so the selection survives re-renders
// triggered by search/filter changes.
const selectedProductIds = new Set();

// Pending delete state. Always an array; single-row delete is a 1-element
// array, bulk delete is a multi-element array. Drives the confirm modal.
let pendingProductDeleteIds = [];

// When set, the modal is in edit mode and submit will PUT instead of POST.
let editingProduct = null;

// -------------------------------------------------------------
// Per-partner placeholder text for the "Add new product" modal.
// Wylaade sells wine; Drachehöhli sells board games. Showing
// partner-appropriate examples makes the form feel native to
// whichever shop is using it. Admin sees a neutral set until
// they pick a partner from the dropdown.
// -------------------------------------------------------------
const USERNAME_TO_PARTNER_ID = {
    'wylaade': 1,
    'drachehoehli': 2,
};

const PARTNER_PLACEHOLDERS = {
    1: { // Wylaade — wine
        name:     'E.g. Baselbieter Kerner 2022',
        sku:      'E.g. 00083',
        price:    'E.g. 26.80',
        quantity: 'E.g. 34',
    },
    2: { // Drachehöhli — board games
        name:     'E.g. Catan – Seafarers Expansion',
        sku:      'E.g. 00187',
        price:    'E.g. 49.90',
        quantity: 'E.g. 12',
    },
};

const GENERIC_PLACEHOLDERS = {
    name:     'E.g. Product name',
    sku:      'E.g. 00100',
    price:    'E.g. 19.90',
    quantity: 'E.g. 20',
};

function applyPlaceholders(partnerId) {
    const set = PARTNER_PLACEHOLDERS[partnerId] || GENERIC_PLACEHOLDERS;
    document.getElementById('productName').placeholder     = set.name;
    document.getElementById('productSKU').placeholder      = set.sku;
    document.getElementById('productPrice').placeholder    = set.price;
    document.getElementById('productQuantity').placeholder = set.quantity;
}

// -------------------------------------------------------------
// Data loading
// -------------------------------------------------------------
async function loadPartners() {
    try {
        const res = await authFetch('/partner/');
        if (!res || !res.ok) return;
        const partners = await res.json();
        partners.forEach(p => { partnerLookup[p.partnerID] = p.partnerName; });

        if (isAdmin) {
            const select = document.getElementById('productPartnerID');
            const group = document.getElementById('partner-picker-group');
            partners.forEach(p => {
                const opt = document.createElement('option');
                opt.value = p.partnerID;
                opt.textContent = p.partnerName;
                select.appendChild(opt);
            });
            group.classList.remove('hidden');
        }
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
            tbody.innerHTML = `<tr><td colspan="9" class="table-empty">Failed to load products (HTTP ${res.status})</td></tr>`;
            return;
        }
        allProducts = await res.json();
        // Drop selections for products that no longer exist (e.g. after a delete)
        const stillExisting = new Set(allProducts.map(p => String(p.productID)));
        Array.from(selectedProductIds).forEach(id => {
            if (!stillExisting.has(id)) selectedProductIds.delete(id);
        });
        applySearchFilter();
    } catch (err) {
        console.error(err);
        tbody.innerHTML = `<tr><td colspan="9" class="table-empty">Failed to load products</td></tr>`;
    }
}

// -------------------------------------------------------------
// Rendering
// -------------------------------------------------------------
function renderProducts(products) {
    const tbody = document.getElementById('products-tbody');
    if (!products.length) {
        tbody.innerHTML = `<tr><td colspan="9" class="table-empty">No products found</td></tr>`;
        updateBulkActionBar();
        return;
    }
    tbody.innerHTML = products.map(p => {
        const qty = p.productQuantity ?? 0;
        const inStock = qty > 0;
        const partnerName = partnerLookup[p.productPartnerID] ?? '—';
        const price = (p.productPrice ?? 0).toFixed(2);
        const isChecked = selectedProductIds.has(String(p.productID));
        return `
            <tr>
                <td><input type="checkbox" data-id="${p.productID}" ${isChecked ? 'checked' : ''}></td>
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
                <td>
                    <button class="row-action product-edit-btn" data-id="${p.productID}" title="Edit">✏️</button>
                    <button class="row-action product-delete-btn" data-id="${p.productID}" title="Delete">🗑</button>
                </td>
            </tr>`;
    }).join('');
    updateBulkActionBar();
}

function escapeHtml(str) {
    return String(str)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;');
}

// -------------------------------------------------------------
// Search & filter
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
// Row actions: edit + delete + quantity buttons (event delegation)
// -------------------------------------------------------------
document.getElementById('products-tbody').addEventListener('click', async (e) => {
    const editBtn = e.target.closest('.product-edit-btn');
    if (editBtn) {
        const product = allProducts.find(p => String(p.productID) === editBtn.dataset.id);
        if (product) openEditModal(product);
        return;
    }

    const deleteBtn = e.target.closest('.product-delete-btn');
    if (deleteBtn) {
        openProductDeleteModal(deleteBtn.dataset.id);
        return;
    }

    const btn = e.target.closest('.qty-btn');
    if (!btn || btn.disabled) return;

    const id = btn.dataset.id;
    const delta = parseInt(btn.dataset.delta, 10);
    const product = allProducts.find(p => String(p.productID) === id);
    if (!product) return;

    btn.disabled = true;

    if (delta < 0) {
        // Decrement = record a sale (creates an Order, decrements stock atomically)
        const res = await authFetch('/order/sale', {
            method: 'POST',
            body: JSON.stringify({ productID: product.productID, quantity: 1 }),
        });
        btn.disabled = false;
        if (res && res.ok) {
            product.productQuantity = Math.max(0, (product.productQuantity ?? 0) - 1);
            applySearchFilter();
        } else {
            console.error('Failed to record sale', res && res.status);
        }
        return;
    }

    // Increment = restock (no order recorded, pure inventory PUT)
    const newQty = (product.productQuantity ?? 0) + delta;
    const updated = {
        productID: product.productID,
        productName: product.productName,
        productSKU: product.productSKU,
        productPrice: product.productPrice,
        productQuantity: newQty,
        productPartnerID: product.productPartnerID,
    };
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
// Checkbox handling: select-all + per-row selection
// -------------------------------------------------------------
document.getElementById('select-all').addEventListener('change', (e) => {
    const checked = e.target.checked;
    const visibleCheckboxes = document.querySelectorAll('#products-tbody input[type="checkbox"]');
    visibleCheckboxes.forEach(cb => {
        cb.checked = checked;
        if (checked) {
            selectedProductIds.add(cb.dataset.id);
        } else {
            selectedProductIds.delete(cb.dataset.id);
        }
    });
    updateBulkActionBar();
});

document.getElementById('products-tbody').addEventListener('change', (e) => {
    const cb = e.target.closest('input[type="checkbox"]');
    if (!cb) return;
    if (cb.checked) {
        selectedProductIds.add(cb.dataset.id);
    } else {
        selectedProductIds.delete(cb.dataset.id);
    }
    updateBulkActionBar();
});

// Reflects the count + drives the visibility of the bulk-action-bar.
// Also computes the tri-state (checked / unchecked / indeterminate) for select-all
// based on what's currently visible.
function updateBulkActionBar() {
    const visibleCheckboxes = document.querySelectorAll('#products-tbody input[type="checkbox"]');
    const checkedCount = Array.from(visibleCheckboxes).filter(cb => cb.checked).length;
    const totalVisible = visibleCheckboxes.length;

    const bar = document.getElementById('bulk-action-bar');
    const countLabel = document.getElementById('bulk-action-count');
    if (checkedCount > 0) {
        bar.classList.remove('hidden');
        countLabel.textContent = `${checkedCount} selected`;
    } else {
        bar.classList.add('hidden');
    }

    const selectAll = document.getElementById('select-all');
    if (totalVisible === 0 || checkedCount === 0) {
        selectAll.checked = false;
        selectAll.indeterminate = false;
    } else if (checkedCount === totalVisible) {
        selectAll.checked = true;
        selectAll.indeterminate = false;
    } else {
        selectAll.checked = false;
        selectAll.indeterminate = true;
    }
}

document.getElementById('bulk-delete-btn').addEventListener('click', () => {
    const ids = Array.from(selectedProductIds);
    if (ids.length === 0) return;
    openBulkDeleteModal(ids);
});

// -------------------------------------------------------------
// New/Edit Product modal
// -------------------------------------------------------------
const modal = document.getElementById('product-modal');
const openBtn = document.getElementById('open-new-product');
const cancelBtn = document.getElementById('cancel-modal');
const productForm = document.getElementById('product-form');
const modalTitleEl = document.getElementById('product-modal-title');

openBtn.addEventListener('click', () => {
    editingProduct = null;
    modalTitleEl.textContent = 'Add new product';
    modal.classList.remove('hidden');
    // Match the placeholders to whoever is logged in.
    // Admin starts generic; placeholders update when they pick a partner below.
    if (isAdmin) {
        applyPlaceholders(null);
    } else {
        applyPlaceholders(USERNAME_TO_PARTNER_ID[getUser()]);
    }
});

function openEditModal(product) {
    editingProduct = product;
    modalTitleEl.textContent = 'Edit product';
    document.getElementById('productName').value     = product.productName ?? '';
    document.getElementById('productSKU').value      = product.productSKU ?? '';
    document.getElementById('productPrice').value    = product.productPrice ?? '';
    document.getElementById('productQuantity').value = product.productQuantity ?? 0;
    if (isAdmin) {
        document.getElementById('productPartnerID').value = product.productPartnerID ?? '';
        applyPlaceholders(product.productPartnerID);
    } else {
        applyPlaceholders(USERNAME_TO_PARTNER_ID[getUser()]);
    }
    document.getElementById('form-error').textContent = '';
    modal.classList.remove('hidden');
}

cancelBtn.addEventListener('click', closeModal);
modal.addEventListener('click', (e) => {
    if (e.target === modal) closeModal();
});

// Admin only: as they pick a partner, re-skin the placeholders to match.
// Harmless for non-admin users (the picker is hidden and never fires change).
document.getElementById('productPartnerID').addEventListener('change', (e) => {
    const raw = e.target.value;
    applyPlaceholders(raw ? parseInt(raw, 10) : null);
});

function closeModal() {
    modal.classList.add('hidden');
    productForm.reset();
    document.getElementById('form-error').textContent = '';
    editingProduct = null;
    modalTitleEl.textContent = 'Add new product';
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

    if (isAdmin) {
        const partnerIdRaw = document.getElementById('productPartnerID').value;
        if (!partnerIdRaw) {
            errorDiv.textContent = 'Please select a partner.';
            return;
        }
        newProduct.productPartnerID = parseInt(partnerIdRaw, 10);
    } else if (editingProduct) {
        // Non-admin editing: the partner picker is hidden, so carry the
        // original partnerID forward — the backend expects it on PUT.
        newProduct.productPartnerID = editingProduct.productPartnerID;
    }

    if (!newProduct.productName || !newProduct.productSKU) {
        errorDiv.textContent = 'Product Name and SKU are required.';
        return;
    }
    if (isNaN(newProduct.productPrice) || isNaN(newProduct.productQuantity)) {
        errorDiv.textContent = 'Price and Quantity must be numbers.';
        return;
    }
    if (newProduct.productPrice < 0){
        errorDiv.textContent = 'Price cannot be negative.';
        return;

    }

    let res;
    if (editingProduct) {
        newProduct.productID = editingProduct.productID;
        res = await authFetch(`/product/${editingProduct.productID}`, {
            method: 'PUT',
            body: JSON.stringify(newProduct),
        });
    } else {
        res = await authFetch('/product/add', {
            method: 'POST',
            body: JSON.stringify(newProduct),
        });
    }

    if (res && res.ok) {
        closeModal();
        await loadProducts();
    } else {
        const action = editingProduct ? 'update' : 'create';
        errorDiv.textContent = `Could not ${action} product${res ? ` (HTTP ${res.status})` : ''}. Check that all fields are valid.`;
    }
});

// -------------------------------------------------------------
// Delete Product modal (single + bulk)
// -------------------------------------------------------------
const deleteModal     = document.getElementById('product-delete-modal');
const deleteTitleEl   = document.getElementById('product-delete-title');
const deleteMessageEl = document.getElementById('product-delete-message');
const deleteErrorEl   = document.getElementById('product-delete-error');

document.getElementById('cancel-product-delete-btn').addEventListener('click', closeProductDeleteModal);
deleteModal.addEventListener('click', (e) => {
    if (e.target === deleteModal) closeProductDeleteModal();
});

function openProductDeleteModal(id) {
    const product = allProducts.find(p => String(p.productID) === String(id));
    if (!product) return;
    pendingProductDeleteIds = [String(id)];
    deleteTitleEl.textContent = 'Delete product?';
    deleteMessageEl.textContent = `Are you sure you want to delete "${product.productName}" (SKU ${product.productSKU})? This cannot be undone.`;
    deleteErrorEl.textContent = '';
    deleteModal.classList.remove('hidden');
}

function openBulkDeleteModal(ids) {
    pendingProductDeleteIds = ids.slice();
    const n = ids.length;
    deleteTitleEl.textContent = n === 1 ? 'Delete product?' : 'Delete products?';
    deleteMessageEl.textContent =
        `Are you sure you want to delete ${n} product${n === 1 ? '' : 's'}? This cannot be undone.`;
    deleteErrorEl.textContent = '';
    deleteModal.classList.remove('hidden');
}

function closeProductDeleteModal() {
    pendingProductDeleteIds = [];
    deleteModal.classList.add('hidden');
    deleteErrorEl.textContent = '';
}

document.getElementById('confirm-product-delete-btn').addEventListener('click', async () => {
    if (pendingProductDeleteIds.length === 0) return;

    // Fire all deletes in parallel. For a small inventory this is fine; for
    // a large bulk operation a dedicated batch endpoint would be better.
    const results = await Promise.all(
        pendingProductDeleteIds.map(id =>
            authFetch(`/product/${id}`, { method: 'DELETE' })
        )
    );

    const failed = results.filter(r => !r || !r.ok);
    if (failed.length === 0) {
        // Drop the now-deleted IDs from the selection set
        pendingProductDeleteIds.forEach(id => selectedProductIds.delete(id));
        closeProductDeleteModal();
        await loadProducts();
    } else {
        deleteErrorEl.textContent =
            `${failed.length} of ${pendingProductDeleteIds.length} product(s) could not be deleted.`;
    }
});

// -------------------------------------------------------------
// Initial load
// -------------------------------------------------------------
(async () => {
    await loadPartners();
    await loadProducts();

    // Pre-fill search from URL param (used when arriving from Dashboard)
    const urlParams = new URLSearchParams(window.location.search);
    const searchParam = urlParams.get('search');
    if (searchParam) {
        document.getElementById('search-input').value = searchParam;
        applySearchFilter();
    }
})();
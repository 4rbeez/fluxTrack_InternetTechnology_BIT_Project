// =============================================================
// fluxTrack - orders.js
// Order History view (UC 304):
//   - Partner: own orders only (server enforces via business rule)
//   - Admin: all orders, with partner filter
//   - Search by product name, filter by date range
//   - Sorted newest first
// =============================================================

requireAuth();

const isAdmin = getUser() === 'admin';
const partnerLookup = {};
let allOrders = [];

async function loadAll() {
    try {
        const [partnerRes, orderRes] = await Promise.all([
            authFetch('/partner/'),
            authFetch('/order/'),
        ]);

        if (partnerRes && partnerRes.ok) {
            const partners = await partnerRes.json();
            partners.forEach(p => { partnerLookup[p.partnerID] = p.partnerName; });

            if (isAdmin) {
                const select = document.getElementById('order-partner-filter');
                partners.forEach(p => {
                    const opt = document.createElement('option');
                    opt.value = p.partnerID;
                    opt.textContent = p.partnerName;
                    select.appendChild(opt);
                });
                document.getElementById('partner-filter-group').classList.remove('hidden');
                document.querySelectorAll('.th-partner').forEach(el => el.classList.remove('hidden'));
            }
        }

        if (orderRes && orderRes.ok) {
            allOrders = await orderRes.json();
            // Sort newest first
            allOrders.sort((a, b) => new Date(b.orderDate) - new Date(a.orderDate));
        }

        applyOrderFilters();
    } catch (err) {
        console.error('Order load failed', err);
        const tbody = document.getElementById('orders-tbody');
        tbody.innerHTML = `<tr><td colspan="5" class="table-empty">Failed to load orders</td></tr>`;
    }
}

function renderOrders(orders) {
    const tbody = document.getElementById('orders-tbody');
    const colspan = isAdmin ? 5 : 4;
    if (orders.length === 0) {
        tbody.innerHTML = `<tr><td colspan="${colspan}" class="table-empty">No orders found</td></tr>`;
        renderSummary([]);
        return;
    }
    tbody.innerHTML = orders.map(o => `
        <tr>
            <td>${formatDate(o.orderDate)}</td>
            <td>${escapeHtml(o.productName || '—')}</td>
            ${isAdmin ? `<td>${escapeHtml(partnerLookup[o.partnerID] || '—')}</td>` : ''}
            <td>${o.quantity}</td>
            <td>${(o.totalAmount ?? 0).toFixed(2)}</td>
        </tr>
    `).join('');
    renderSummary(orders);
}

function renderSummary(orders) {
    const totalRevenue = orders.reduce((sum, o) => sum + (o.totalAmount ?? 0), 0);
    const totalUnits = orders.reduce((sum, o) => sum + (o.quantity ?? 0), 0);
    document.getElementById('order-summary').innerHTML = orders.length === 0
        ? ''
        : `<strong>${orders.length}</strong> order${orders.length === 1 ? '' : 's'} &middot;
           <strong>${totalUnits}</strong> units &middot;
           <strong>CHF ${totalRevenue.toLocaleString('de-CH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</strong>`;
}

function applyOrderFilters() {
    const term = document.getElementById('order-search-input').value.trim().toLowerCase();
    const fromStr = document.getElementById('order-date-from').value;
    const toStr   = document.getElementById('order-date-to').value;
    const partnerFilter = isAdmin
        ? document.getElementById('order-partner-filter').value
        : '';

    const fromTime = fromStr ? new Date(fromStr + 'T00:00:00').getTime() : null;
    const toTime   = toStr   ? new Date(toStr   + 'T23:59:59').getTime() : null;

    const filtered = allOrders.filter(o => {
        if (term && !(o.productName && o.productName.toLowerCase().includes(term))) {
            return false;
        }
        const ts = new Date(o.orderDate).getTime();
        if (fromTime && ts < fromTime) return false;
        if (toTime   && ts > toTime)   return false;
        if (partnerFilter && String(o.partnerID) !== String(partnerFilter)) return false;
        return true;
    });

    renderOrders(filtered);
}

function formatDate(isoString) {
    if (!isoString) return '—';
    const d = new Date(isoString);
    if (isNaN(d.getTime())) return '—';
    const date = d.toLocaleDateString('de-CH', { day: '2-digit', month: '2-digit', year: 'numeric' });
    const time = d.toLocaleTimeString('de-CH', { hour: '2-digit', minute: '2-digit' });
    return `${date} ${time}`;
}

function escapeHtml(str) {
    return String(str)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;');
}

document.getElementById('order-search-input').addEventListener('input', applyOrderFilters);
document.getElementById('order-date-from').addEventListener('change', applyOrderFilters);
document.getElementById('order-date-to').addEventListener('change', applyOrderFilters);
document.getElementById('order-partner-filter').addEventListener('change', applyOrderFilters);
document.getElementById('clear-order-filters').addEventListener('click', () => {
    document.getElementById('order-search-input').value = '';
    document.getElementById('order-date-from').value = '';
    document.getElementById('order-date-to').value = '';
    document.getElementById('order-partner-filter').value = '';
    applyOrderFilters();
});

loadAll();

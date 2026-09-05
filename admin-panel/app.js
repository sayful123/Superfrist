// Super Frist VPN - Web Admin Control Center

const API_BASE = window.location.origin.includes(':3000') ? '/api' : 'http://localhost:3000/api';

// State
let authToken = localStorage.getItem('super_frist_admin_token') || null;
let currentTab = 'overview';
let activeCodesList = [];
let serversList = [
  { id: 'srv_uae_dubai', name: 'UAE - Dubai', country: 'UAE', flag: '🇦🇪', host: 'ae-dxb.vpn.superfrist.net', port: 51820, protocol: 'WireGuard', ping: 32, load: 45, status: 'ONLINE' },
  { id: 'srv_singapore', name: 'Singapore', country: 'Singapore', flag: '🇸🇬', host: 'sg-sin.vpn.superfrist.net', port: 51820, protocol: 'WireGuard', ping: 48, load: 58, status: 'ONLINE' },
  { id: 'srv_germany', name: 'Germany - Frankfurt', country: 'Germany', flag: '🇩🇪', host: 'de-fra.vpn.superfrist.net', port: 51820, protocol: 'WireGuard', ping: 65, load: 38, status: 'ONLINE' },
  { id: 'srv_netherlands', name: 'Netherlands - Amsterdam', country: 'Netherlands', flag: '🇳🇱', host: 'nl-ams.vpn.superfrist.net', port: 51820, protocol: 'WireGuard', ping: 68, load: 42, status: 'ONLINE' },
  { id: 'srv_usa', name: 'United States - New York', country: 'United States', flag: '🇺🇸', host: 'us-nyc.vpn.superfrist.net', port: 51820, protocol: 'WireGuard', ping: 110, load: 64, status: 'ONLINE' },
  { id: 'srv_uk', name: 'United Kingdom - London', country: 'United Kingdom', flag: '🇬🇧', host: 'uk-lon.vpn.superfrist.net', port: 51820, protocol: 'WireGuard', ping: 72, load: 51, status: 'ONLINE' }
];

let generatedBatch = [];

// DOM Elements
const loginModal = document.getElementById('login-modal');
const adminApp = document.getElementById('admin-app');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const logoutBtn = document.getElementById('logout-btn');

// Init
document.addEventListener('DOMContentLoaded', () => {
  setupNavigation();
  setupEventListeners();

  if (authToken) {
    showApp();
    loadDashboardData();
  } else {
    showLogin();
  }
});

function showLogin() {
  loginModal.style.display = 'flex';
  adminApp.style.display = 'none';
}

function showApp() {
  loginModal.style.display = 'none';
  adminApp.style.display = 'block';
  renderOverviewNodes();
  renderServersCards();
}

// Navigation Tabs
function setupNavigation() {
  document.querySelectorAll('.nav-item').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.nav-item').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));

      btn.classList.add('active');
      const tabId = btn.dataset.tab;
      currentTab = tabId;
      document.getElementById(`tab-${tabId}`).classList.add('active');

      if (tabId === 'codes') loadCodes();
      if (tabId === 'connections') loadConnections();
      if (tabId === 'audit') loadAuditLogs();
      if (tabId === 'overview') loadDashboardData();
    });
  });
}

// Event Listeners
function setupEventListeners() {
  // Login
  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    loginError.style.display = 'none';

    const email = document.getElementById('admin-email').value;
    const password = document.getElementById('admin-password').value;

    try {
      const res = await fetch(`${API_BASE}/admin/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });

      const data = await res.json();
      if (data.success) {
        authToken = data.data.token;
        localStorage.setItem('super_frist_admin_token', authToken);
        showApp();
        loadDashboardData();
      } else {
        // Fallback for standalone demo
        if (email === 'admin@superfrist.vpn' && password === 'Admin@2026!') {
          authToken = 'mock_jwt_token_admin_2026';
          localStorage.setItem('super_frist_admin_token', authToken);
          showApp();
          loadDashboardData();
        } else {
          loginError.textContent = data.error || 'Authentication failed.';
          loginError.style.display = 'block';
        }
      }
    } catch (err) {
      // Local fallback
      if (email === 'admin@superfrist.vpn' && password === 'Admin@2026!') {
        authToken = 'mock_jwt_token_admin_2026';
        localStorage.setItem('super_frist_admin_token', authToken);
        showApp();
        loadDashboardData();
      } else {
        loginError.textContent = 'Unable to reach backend API. Check if server is running.';
        loginError.style.display = 'block';
      }
    }
  });

  // Logout
  logoutBtn.addEventListener('click', () => {
    localStorage.removeItem('super_frist_admin_token');
    authToken = null;
    showLogin();
  });

  // Code Generator Form
  document.getElementById('generator-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const prefix = document.getElementById('gen-prefix').value.trim() || 'VPN';
    const count = parseInt(document.getElementById('gen-count').value) || 5;
    const validity = parseInt(document.getElementById('gen-validity').value) || 30;
    const notes = document.getElementById('gen-notes').value;

    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    const newCodes = [];

    for (let i = 0; i < count; i++) {
      const p1 = Array.from({ length: 4 }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
      const p2 = Array.from({ length: 4 }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
      const p3 = Array.from({ length: 4 }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
      const code = `${prefix}-${p1}-${p2}-${p3}`;

      newCodes.push({
        code,
        status: 'ACTIVE',
        validityDays: validity,
        createdDate: new Date().toLocaleDateString(),
        notes: notes || `Admin Generated (${validity} Days)`
      });
    }

    generatedBatch = newCodes;
    activeCodesList = [...newCodes, ...activeCodesList];

    // Display Output
    const outCard = document.getElementById('generated-output-card');
    outCard.style.display = 'block';
    document.getElementById('gen-batch-count').textContent = count;

    const chipsContainer = document.getElementById('generated-codes-list');
    chipsContainer.innerHTML = newCodes.map(c => `
      <div class="code-chip">
        <span class="code-font">${c.code}</span>
        <button class="btn btn-sm btn-outline" onclick="copyText('${c.code}')">Copy</button>
      </div>
    `).join('');

    // Update stats
    const unusedEl = document.getElementById('metric-unused-codes');
    unusedEl.textContent = parseInt(unusedEl.textContent) + count;
    const totalEl = document.getElementById('metric-total-codes');
    totalEl.textContent = parseInt(totalEl.textContent) + count;
  });

  // Copy All Generated
  document.getElementById('btn-copy-generated').addEventListener('click', () => {
    const text = generatedBatch.map(c => c.code).join('\n');
    copyText(text);
    alert(`Copied ${generatedBatch.length} codes to clipboard!`);
  });

  // Download Generated CSV
  document.getElementById('btn-download-generated').addEventListener('click', () => {
    let csv = 'Activation Code,Validity Days,Status,Notes\n';
    generatedBatch.forEach(c => {
      csv += `"${c.code}",${c.validityDays},"${c.status}","${c.notes}"\n`;
    });
    downloadCsv(csv, 'super_frist_batch_codes.csv');
  });

  // Export All CSV
  document.getElementById('btn-export-all-csv').addEventListener('click', () => {
    let csv = 'Code,Status,Validity Days,Device ID,Created At\n';
    activeCodesList.forEach(c => {
      csv += `"${c.code}","${c.status}",${c.validityDays},"${c.deviceId || ''}","${c.createdDate || ''}"\n`;
    });
    downloadCsv(csv, 'super_frist_all_codes.csv');
  });

  // Filter Pills
  document.querySelectorAll('.filter-pills .pill').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.filter-pills .pill').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      renderCodesTable(pill.dataset.filter, document.getElementById('filter-search').value);
    });
  });

  // Search Input
  document.getElementById('filter-search').addEventListener('input', (e) => {
    const activePill = document.querySelector('.filter-pills .pill.active');
    renderCodesTable(activePill ? activePill.dataset.filter : '', e.target.value);
  });
}

function copyText(text) {
  navigator.clipboard.writeText(text);
}

function downloadCsv(content, filename) {
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

// Initial Data Population
function loadDashboardData() {
  // Populate initial codes inventory if empty
  if (activeCodesList.length === 0) {
    activeCodesList = [
      { code: 'VPN-8F7K-29MX-QP4A', status: 'ACTIVE', validityDays: 30, deviceId: null, createdDate: '2026-09-01', expiryDate: null },
      { code: 'VPN-FAST-7777-UAE1', status: 'ACTIVE', validityDays: 60, deviceId: null, createdDate: '2026-09-02', expiryDate: null },
      { code: 'VPN-PRO1-9999-YEAR', status: 'ACTIVE', validityDays: 365, deviceId: null, createdDate: '2026-09-03', expiryDate: null },
      { code: 'VPN-EXPR-1111-TEST', status: 'EXPIRED', validityDays: 7, deviceId: 'DEV-USER-001', createdDate: '2026-08-20', expiryDate: '2026-08-27' },
      { code: 'VPN-DISA-2222-TEST', status: 'DISABLED', validityDays: 30, deviceId: null, createdDate: '2026-08-30', expiryDate: null },
      { code: 'VPN-USED-3333-TEST', status: 'USED', validityDays: 30, deviceId: 'DEV-OTHER-999', createdDate: '2026-08-29', expiryDate: '2026-09-29' }
    ];
  }

  document.getElementById('last-refresh-time').textContent = `Updated: ${new Date().toLocaleTimeString()}`;
}

function renderOverviewNodes() {
  const tbody = document.getElementById('overview-nodes-tbody');
  tbody.innerHTML = serversList.map(s => `
    <tr>
      <td><strong>${s.flag} ${s.name}</strong></td>
      <td><span class="badge badge-active">${s.protocol}</span></td>
      <td class="code-font">${s.host}:${s.port}</td>
      <td><span style="color:var(--emerald); font-weight:bold;">${s.ping} ms</span></td>
      <td>
        <div style="display:flex; align-items:center; gap:8px;">
          <div style="flex:1; height:6px; background:var(--border-color); border-radius:3px; overflow:hidden;">
            <div style="width:${s.load}%; height:100%; background:${s.load > 60 ? 'var(--amber)' : 'var(--cyan)'}"></div>
          </div>
          <span>${s.load}%</span>
        </div>
      </td>
      <td><span class="badge badge-used">${s.status}</span></td>
    </tr>
  `).join('');
}

function renderServersCards() {
  const grid = document.getElementById('servers-cards-grid');
  grid.innerHTML = serversList.map(s => `
    <div class="server-node-card">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
        <span class="flag">${s.flag}</span>
        <span class="badge badge-used">${s.status}</span>
      </div>
      <h4>${s.name}</h4>
      <div class="code-font" style="font-size:11px; margin-bottom:8px;">${s.host}:${s.port}</div>
      <div style="display:flex; justify-content:space-between; font-size:12px; color:var(--text-secondary); margin-bottom:12px;">
        <span>Latency: <strong style="color:var(--emerald)">${s.ping} ms</strong></span>
        <span>Load: <strong>${s.load}%</strong></span>
      </div>
      <div style="display:flex; gap:8px;">
        <button class="btn btn-sm btn-outline" style="flex:1;" onclick="toggleServerStatus('${s.id}')">${s.status === 'ONLINE' ? 'Disable' : 'Enable'}</button>
      </div>
    </div>
  `).join('');
}

function toggleServerStatus(srvId) {
  const s = serversList.find(x => x.id === srvId);
  if (s) {
    s.status = s.status === 'ONLINE' ? 'OFFLINE' : 'ONLINE';
    renderOverviewNodes();
    renderServersCards();
  }
}

function renderCodesTable(filter = '', search = '') {
  const tbody = document.getElementById('codes-table-body');
  const filtered = activeCodesList.filter(c => {
    const matchStatus = !filter || c.status === filter;
    const matchSearch = !search || c.code.toLowerCase().includes(search.toLowerCase()) || (c.deviceId && c.deviceId.toLowerCase().includes(search.toLowerCase()));
    return matchStatus && matchSearch;
  });

  tbody.innerHTML = filtered.map(c => `
    <tr>
      <td class="code-font">${c.code}</td>
      <td><span class="badge badge-${c.status.toLowerCase()}">${c.status}</span></td>
      <td>${c.validityDays} Days</td>
      <td style="font-size:12px; font-family:var(--font-mono);">${c.deviceId || '<span style="color:var(--text-muted)">Unbound</span>'}</td>
      <td style="font-size:12px;">${c.createdDate || '-'}</td>
      <td style="font-size:12px;">${c.expiryDate || '-'}</td>
      <td>
        <button class="btn btn-sm btn-outline" onclick="copyText('${c.code}')">Copy</button>
        <button class="btn btn-sm btn-outline" onclick="extendCode('${c.code}')">+30d</button>
        ${c.deviceId ? `<button class="btn btn-sm btn-outline" onclick="resetDeviceBinding('${c.code}')">Reset Device</button>` : ''}
        <button class="btn btn-sm btn-outline" onclick="toggleCodeDisabled('${c.code}')">${c.status === 'DISABLED' ? 'Enable' : 'Disable'}</button>
      </td>
    </tr>
  `).join('');
}

function extendCode(code) {
  const item = activeCodesList.find(c => c.code === code);
  if (item) {
    item.validityDays += 30;
    if (item.status === 'EXPIRED') item.status = 'USED';
    alert(`Extended ${code} by +30 days!`);
    renderCodesTable();
  }
}

function resetDeviceBinding(code) {
  const item = activeCodesList.find(c => c.code === code);
  if (item) {
    item.deviceId = null;
    alert(`Reset device binding for ${code}!`);
    renderCodesTable();
  }
}

function toggleCodeDisabled(code) {
  const item = activeCodesList.find(c => c.code === code);
  if (item) {
    item.status = item.status === 'DISABLED' ? 'ACTIVE' : 'DISABLED';
    renderCodesTable();
  }
}

function loadCodes() {
  const activePill = document.querySelector('.filter-pills .pill.active');
  renderCodesTable(activePill ? activePill.dataset.filter : '', document.getElementById('filter-search').value);
}

function loadConnections() {
  const tbody = document.getElementById('connections-table-body');
  const dummyConns = [
    { userId: 'USR-QP4A', deviceId: 'DEV-4820FE71A912', server: '🇦🇪 UAE - Dubai', ip: '10.8.0.2', connectedAt: '12m ago', protocol: 'WireGuard' },
    { userId: 'USR-UAE1', deviceId: 'DEV-8891CD201823', server: '🇸🇬 Singapore', ip: '10.8.0.3', connectedAt: '45m ago', protocol: 'WireGuard' },
    { userId: 'USR-YEAR', deviceId: 'DEV-1092AF829103', server: '🇩🇪 Germany - Frankfurt', ip: '10.8.0.4', connectedAt: '2h 10m ago', protocol: 'WireGuard' }
  ];

  tbody.innerHTML = dummyConns.map(c => `
    <tr>
      <td><strong>${c.userId}</strong></td>
      <td class="code-font" style="font-size:12px;">${c.deviceId}</td>
      <td>${c.server}</td>
      <td class="code-font">${c.ip}</td>
      <td>${c.connectedAt}</td>
      <td><span class="badge badge-active">${c.protocol}</span></td>
      <td><button class="btn btn-sm btn-outline" style="color:var(--rose)" onclick="alert('Session ${c.userId} disconnected.')">Disconnect</button></td>
    </tr>
  `).join('');
}

function loadAuditLogs() {
  const tbody = document.getElementById('audit-table-body');
  const logs = [
    { time: '2026-09-04 17:15:20', action: 'BATCH_GENERATE', target: '5 codes', user: 'admin@superfrist.vpn', details: 'Generated 5 activation codes with 30 days validity.' },
    { time: '2026-09-04 16:40:12', action: 'CODE_ACTIVATED', target: 'VPN-8F7K-29MX-QP4A', user: 'system', details: 'Bound to device DEV-4820FE71A912 (Android 15)' },
    { time: '2026-09-04 14:10:00', action: 'SYSTEM_INIT', target: 'Cluster', user: 'admin@superfrist.vpn', details: 'Initialized 6 production WireGuard gateways.' }
  ];

  tbody.innerHTML = logs.map(l => `
    <tr>
      <td style="font-size:12px; color:var(--text-secondary);">${l.time}</td>
      <td><strong>${l.action}</strong></td>
      <td class="code-font">${l.target}</td>
      <td>${l.user}</td>
      <td style="font-size:12px;">${l.details}</td>
    </tr>
  `).join('');
}

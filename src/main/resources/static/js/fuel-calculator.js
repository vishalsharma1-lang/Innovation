/* ============================================================
   SMART FUEL COST CALCULATOR — calculator.js
   ============================================================ */
'use strict';

// ── STATE ──────────────────────────────────────────────────
const state = {
  city: 'Delhi',
  period: 'month',   // day | month | year
  distUnit: 'km',
  distance: 1500,
  prices: {},
  calculated: false
};

// ── FUEL CONFIG ─────────────────────────────────────────────
const FUEL = {
  petrol: { label: 'Petrol', icon: 'fa-gas-pump', unit: 'L',   priceKey: 'petrol',      mileageDefault: 15,  mileageUnit: 'kmpl',   priceLabel: '₹/Litre' },
  diesel: { label: 'Diesel', icon: 'fa-gas-pump', unit: 'L',   priceKey: 'diesel',      mileageDefault: 18,  mileageUnit: 'kmpl',   priceLabel: '₹/Litre' },
  cng:    { label: 'CNG',    icon: 'fa-wind',     unit: 'Kg',  priceKey: 'cng',         mileageDefault: 22,  mileageUnit: 'km/kg',  priceLabel: '₹/Kg'   },
  ev:     { label: 'EV',     icon: 'fa-bolt',     unit: 'kWh', priceKey: 'electricity', mileageDefault: 6.5, mileageUnit: 'km/kWh', priceLabel: '₹/kWh'  }
};

let CITY_PRICES = {};

// ── LOAD FUEL PRICES ────────────────────────────────────────
async function loadPrices() {
  try {
    const r = await fetch('/data/fuel-prices.json');
    const data = await r.json();
    CITY_PRICES = data.cities;
  } catch {
    CITY_PRICES = { Delhi: { petrol: 94.72, diesel: 87.62, cng: 75.09, electricity: 8.0 } };
  }
  applyCity(state.city);
}

function applyCity(city) {
  state.city = city;
  state.prices = CITY_PRICES[city] || CITY_PRICES['Delhi'] || {};
  updateLiveBar();
  updateCardPrices();
  updateSidebarPrices();
}

// ── DOM ─────────────────────────────────────────────────────
const $ = id => document.getElementById(id);
const $$ = s => document.querySelectorAll(s);

function fmt(n, dec = 2) {
  if (n == null || isNaN(n)) return '—';
  return new Intl.NumberFormat('en-IN', { maximumFractionDigits: dec, minimumFractionDigits: dec }).format(n);
}
function fmtINR(n) {
  if (n == null || isNaN(n)) return '—';
  if (n >= 100000) return '₹' + fmt(n / 100000, 2) + 'L';
  return '₹' + fmt(n, 0);
}

// ── LIVE BAR ────────────────────────────────────────────────
function updateLiveBar() {
  const p = state.prices;
  $('lbCity').textContent    = state.city;
  $('lbPetrol').textContent  = p.petrol      ? '₹' + p.petrol.toFixed(2) + '/L'   : '—';
  $('lbDiesel').textContent  = p.diesel      ? '₹' + p.diesel.toFixed(2) + '/L'   : '—';
  $('lbCng').textContent     = p.cng         ? '₹' + p.cng.toFixed(2) + '/Kg'     : '—';
  $('lbEv').textContent      = p.electricity ? '₹' + p.electricity.toFixed(2) + '/kWh' : '—';
}

// ── CARD PRICES ─────────────────────────────────────────────
function updateCardPrices() {
  Object.entries(FUEL).forEach(([type, cfg]) => {
    const priceEl = $(`price_${type}`);
    if (priceEl && !priceEl.dataset.userSet) {
      const p = state.prices[cfg.priceKey];
      if (p) priceEl.value = p.toFixed(2);
    }
  });
}

// ── SIDEBAR PRICES ──────────────────────────────────────────
function updateSidebarPrices() {
  const p = state.prices;
  const sbCity = $('sbCity');
  if (sbCity) sbCity.textContent = state.city;
  ['petrol', 'diesel', 'cng', 'electricity'].forEach(k => {
    const el = $('sb_' + k);
    if (el && p[k]) {
      const units = { petrol: '/L', diesel: '/L', cng: '/Kg', electricity: '/kWh' };
      el.textContent = '₹' + p[k].toFixed(2) + units[k];
    }
  });
}

// ── PERIOD TOGGLE ────────────────────────────────────────────
function setPeriod(p) {
  state.period = p;
  $$('.period-btn').forEach(b => b.classList.toggle('active', b.dataset.period === p));
  updateDistanceLabel();
}
function setDistUnit(u) {
  state.distUnit = u;
  $$('.unit-btn').forEach(b => b.classList.toggle('active', b.dataset.unit === u));
  updateDistanceLabel();
}
function updateDistanceLabel() {
  const labels = { day: 'Daily', month: 'Monthly', year: 'Yearly' };
  const lbl = $('distLabel');
  if (lbl) lbl.textContent = `${labels[state.period]} Distance (${state.distUnit.toUpperCase()})`;
}

// ── DISTANCE SYNC (daily↔monthly↔yearly) ────────────────────
function getDistanceAsKmPerMonth() {
  let d = parseFloat($('distInput').value) || 0;
  if (state.distUnit === 'miles') d = d * 1.60934;
  if (state.period === 'day')   return d * 30;
  if (state.period === 'year')  return d / 12;
  return d; // month
}
function getDisplayDistance() {
  return parseFloat($('distInput').value) || 0;
}

// ── CALCULATE ────────────────────────────────────────────────
function calculate() {
  const kmPerMonth = getDistanceAsKmPerMonth();
  if (!kmPerMonth || kmPerMonth <= 0) {
    $('distInput').classList.add('error');
    $('distError').classList.add('show');
    return;
  }
  $('distInput').classList.remove('error');
  $('distError').classList.remove('show');

  const results = {};
  Object.entries(FUEL).forEach(([type, cfg]) => {
    const price   = parseFloat($(`price_${type}`).value) || state.prices[cfg.priceKey] || 0;
    const mileage = parseFloat($(`mileage_${type}`).value) || cfg.mileageDefault;
    if (!price || !mileage) return;
    const costPerKm     = price / mileage;
    const monthlyKm     = kmPerMonth;
    const monthlyCost   = costPerKm * monthlyKm;
    const dailyCost     = monthlyCost / 30;
    const yearlyCost    = monthlyCost * 12;
    results[type] = { costPerKm, dailyCost, monthlyCost, yearlyCost };
  });

  renderCardResults(results);
  renderSavingsBanner(results);
  renderCompTable(results, kmPerMonth);
  renderPriceGraph();
  state.calculated = true;
  trackUse();

  // Scroll to results
  $('resultsAnchor') && $('resultsAnchor').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// ── RENDER CARD RESULTS ──────────────────────────────────────
function renderCardResults(results) {
  Object.entries(FUEL).forEach(([type]) => {
    const r = results[type];
    const el = $(`result_${type}`);
    if (!el) return;
    if (!r) {
      el.innerHTML = '<div style="font-size:12px;color:var(--text-muted);padding:12px 16px">Enter price &amp; mileage to calculate</div>';
      return;
    }
    const display = getDisplayDisplay(r);
    el.innerHTML = `
      <div class="fc-result-row">
        <span class="fc-result-label"><i class="fas fa-clock"></i> Daily Cost</span>
        <span class="fc-result-value">₹${fmt(r.dailyCost, 2)}</span>
      </div>
      <div class="fc-result-row">
        <span class="fc-result-label"><i class="fas fa-calendar-alt"></i> Monthly Cost</span>
        <span class="fc-result-value ${type}">₹${fmt(r.monthlyCost, 0)}</span>
      </div>
      <div class="fc-result-row">
        <span class="fc-result-label"><i class="fas fa-calendar-check"></i> Yearly Cost</span>
        <span class="fc-result-value highlight ${type}">${fmtINR(r.yearlyCost)}</span>
      </div>
      <div class="fc-result-row" style="background:#f8f9fa;margin:0 -16px;padding:7px 16px;margin-top:4px">
        <span class="fc-result-label"><i class="fas fa-route"></i> Cost per km</span>
        <span class="fc-result-value" style="font-size:13px">₹${fmt(r.costPerKm, 2)}/km</span>
      </div>`;
  });

  // Mark cheapest
  const costs = Object.entries(results).map(([t, r]) => ({ type: t, cost: r.yearlyCost }));
  const cheapest = costs.sort((a, b) => a.cost - b.cost)[0];
  $$('.cheapest-tag').forEach(el => el.style.display = 'none');
  if (cheapest) {
    const tag = $(`cheapest_${cheapest.type}`);
    if (tag) tag.style.display = 'flex';
  }
}

function getDisplayDisplay(r) { return r; }

// ── SAVINGS BANNER ───────────────────────────────────────────
function renderSavingsBanner(results) {
  const banner = $('savingsBanner');
  if (!banner) return;
  const p = results.petrol;
  const c = results.cng;
  const e = results.ev;
  const d = results.diesel;

  const lines = [];
  if (p && c && c.yearlyCost < p.yearlyCost)
    lines.push(`CNG saves <strong>${fmtINR(p.yearlyCost - c.yearlyCost)}/year</strong> vs Petrol`);
  if (p && e && e.yearlyCost < p.yearlyCost)
    lines.push(`EV saves <strong>${fmtINR(p.yearlyCost - e.yearlyCost)}/year</strong> vs Petrol`);
  if (d && c && c.yearlyCost < d.yearlyCost)
    lines.push(`CNG saves <strong>${fmtINR(d.yearlyCost - c.yearlyCost)}/year</strong> vs Diesel`);

  if (lines.length) {
    banner.classList.add('show');
    $('savingsText').innerHTML = lines.join(' &nbsp;·&nbsp; ');
  } else {
    banner.classList.remove('show');
  }
}

// ── COMPARISON TABLE ─────────────────────────────────────────
function renderCompTable(results, kmPerMonth) {
  const rows = Object.entries(FUEL).map(([type, cfg]) => {
    const r = results[type];
    return { type, label: cfg.label, ...r };
  }).filter(r => r.yearlyCost).sort((a, b) => a.yearlyCost - b.yearlyCost);

  const tbody = $('compBody');
  if (!tbody) return;
  const cheapestYearly = rows[0]?.yearlyCost;
  tbody.innerHTML = rows.map((r, i) => {
    const isCheapest = i === 0;
    const saving = i > 0 ? r.yearlyCost - cheapestYearly : null;
    return `
      <tr class="${isCheapest ? 'cheapest' : ''}">
        <td>
          <span class="fuel-pill ${r.type}">
            <i class="fas ${FUEL[r.type].icon}"></i> ${r.label}
          </span>
          ${isCheapest ? '<span class="best-badge">✓ Cheapest</span>' : ''}
        </td>
        <td>₹${fmt(r.costPerKm, 2)}</td>
        <td>₹${fmt(r.dailyCost, 2)}</td>
        <td>₹${fmt(r.monthlyCost, 0)}</td>
        <td style="font-weight:700">${fmtINR(r.yearlyCost)}</td>
        <td style="color:${saving ? '#ef4444' : 'var(--green)'}; font-weight:600">
          ${saving ? '+' + fmtINR(saving) : '—'}
        </td>
      </tr>`;
  }).join('');

  const section = $('compSection');
  if (section) section.style.display = 'block';
}

// ── PRICE TREND GRAPH (SVG) ──────────────────────────────────
function renderPriceGraph() {
  const canvas = $('priceChart');
  if (!canvas) return;
  const graphSection = $('graphSection');
  if (graphSection) graphSection.style.display = 'block';

  // Generate last-10-days mock trend data around current prices
  const p = state.prices;
  const days = [];
  for (let i = 9; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    days.push(d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short' }));
  }
  const jitter = (base, spread) =>
    Array.from({ length: 10 }, (_, i) =>
      parseFloat((base + (Math.sin(i * 0.9) * spread)).toFixed(2)));

  const petrolData  = jitter(p.petrol || 94.72, 0.4);
  const dieselData  = jitter(p.diesel || 87.62, 0.35);
  const cngData     = jitter(p.cng    || 75.09, 0.2);

  drawLineChart(canvas, days, [
    { data: petrolData, color: '#0059A8', label: 'Petrol' },
    { data: dieselData, color: '#1e7e34', label: 'Diesel' },
    { data: cngData,    color: '#6f2da8', label: 'CNG' }
  ]);
}

function drawLineChart(canvas, labels, datasets) {
  const W = canvas.offsetWidth || 700, H = 180;
  const pad = { top: 10, right: 20, bottom: 28, left: 48 };
  const chartW = W - pad.left - pad.right;
  const chartH = H - pad.top - pad.bottom;

  const allValues = datasets.flatMap(d => d.data);
  const minV = Math.min(...allValues) - 0.5;
  const maxV = Math.max(...allValues) + 0.5;
  const scaleX = i => pad.left + (i / (labels.length - 1)) * chartW;
  const scaleY = v => pad.top + chartH - ((v - minV) / (maxV - minV)) * chartH;

  let svg = `<svg viewBox="0 0 ${W} ${H}" xmlns="http://www.w3.org/2000/svg" style="width:100%;height:${H}px">`;

  // Grid lines
  for (let i = 0; i <= 4; i++) {
    const y = pad.top + (i / 4) * chartH;
    const val = (maxV - (i / 4) * (maxV - minV)).toFixed(0);
    svg += `<line x1="${pad.left}" y1="${y}" x2="${W - pad.right}" y2="${y}" stroke="#e2e8f0" stroke-width="1"/>`;
    svg += `<text x="${pad.left - 6}" y="${y + 4}" text-anchor="end" font-size="9" fill="#94a3b8">${val}</text>`;
  }

  // X labels
  labels.forEach((lbl, i) => {
    if (i % 2 === 0) {
      svg += `<text x="${scaleX(i)}" y="${H - 4}" text-anchor="middle" font-size="9" fill="#94a3b8">${lbl}</text>`;
    }
  });

  // Lines + dots
  datasets.forEach(({ data, color }) => {
    const pts = data.map((v, i) => `${scaleX(i)},${scaleY(v)}`).join(' ');
    svg += `<polyline points="${pts}" fill="none" stroke="${color}" stroke-width="2" stroke-linejoin="round" stroke-linecap="round"/>`;
    data.forEach((v, i) => {
      if (i === data.length - 1) {
        svg += `<circle cx="${scaleX(i)}" cy="${scaleY(v)}" r="4" fill="${color}" stroke="#fff" stroke-width="1.5"/>`;
        svg += `<text x="${scaleX(i) - 4}" y="${scaleY(v) - 8}" font-size="9" fill="${color}" font-weight="700">₹${v}</text>`;
      }
    });
  });

  svg += '</svg>';
  canvas.innerHTML = svg;
}

// ── RESET ────────────────────────────────────────────────────
function resetAll() {
  $('distInput').value = state.period === 'month' ? 1500 : state.period === 'day' ? 50 : 18000;
  Object.entries(FUEL).forEach(([type, cfg]) => {
    const priceEl = $(`price_${type}`);
    const mileageEl = $(`mileage_${type}`);
    if (priceEl) { priceEl.value = ''; priceEl.dataset.userSet = ''; }
    if (mileageEl) mileageEl.value = cfg.mileageDefault;
  });
  updateCardPrices();
  $$('.cheapest-tag').forEach(el => el.style.display = 'none');
  $$('.fc-results').forEach(el => { el.innerHTML = '<div style="font-size:12px;color:var(--text-muted);padding:8px 0 4px">Press Calculate to see results</div>'; });
  const banner = $('savingsBanner'); if (banner) banner.classList.remove('show');
  const comp = $('compSection'); if (comp) comp.style.display = 'none';
  const graph = $('graphSection'); if (graph) graph.style.display = 'none';
  state.calculated = false;
}

// ── ANALYTICS ────────────────────────────────────────────────
function trackUse() {
  const uses = (parseInt(localStorage.getItem('fcc_uses') || '0')) + 1;
  localStorage.setItem('fcc_uses', uses);
  $('totalUses') && ($('totalUses').textContent = uses.toLocaleString('en-IN'));

  const cities = JSON.parse(localStorage.getItem('fcc_cities') || '{}');
  cities[state.city] = (cities[state.city] || 0) + 1;
  localStorage.setItem('fcc_cities', JSON.stringify(cities));
  const topCity = Object.entries(cities).sort((a,b) => b[1]-a[1])[0];
  $('topCity') && ($('topCity').textContent = topCity ? topCity[0] : '—');
}

function initAnalytics() {
  const uses = parseInt(localStorage.getItem('fcc_uses') || '0');
  $('totalUses') && ($('totalUses').textContent = uses.toLocaleString('en-IN'));
  const cities = JSON.parse(localStorage.getItem('fcc_cities') || '{}');
  const topCity = Object.entries(cities).sort((a,b) => b[1]-a[1])[0];
  $('topCity') && ($('topCity').textContent = topCity ? topCity[0] : '—');
}

// ── FAQ ──────────────────────────────────────────────────────
function initFAQ() {
  $$('.faq-item').forEach(item => {
    item.querySelector('.faq-q').addEventListener('click', () => item.classList.toggle('open'));
  });
}

// ── VEHICLE PREFILL (URL PARAMS) ─────────────────────────────
function handlePrefill() {
  const p = new URLSearchParams(window.location.search);
  const vehicle = p.get('vehicle'), fuel = p.get('fuel'), mileage = p.get('mileage');
  if (vehicle || fuel || mileage) {
    const el = $('vehiclePrefill');
    if (el) {
      el.querySelector('.vn').textContent = vehicle || 'Vehicle';
      el.classList.add('show');
    }
    if (fuel && FUEL[fuel.toLowerCase()]) {
      const mi = $(`mileage_${fuel.toLowerCase()}`);
      if (mi && mileage) { mi.value = mileage; }
    }
  }
}

// ── CITY SELECT ──────────────────────────────────────────────
function onCityChange(val) {
  applyCity(val);
}

// ── PRICE INPUT USER OVERRIDE ────────────────────────────────
function initPriceOverrideTracking() {
  Object.keys(FUEL).forEach(type => {
    const el = $(`price_${type}`);
    if (el) el.addEventListener('input', () => { el.dataset.userSet = '1'; });
  });
}

// ── INIT ─────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
  await loadPrices();

  // Period toggle
  $$('.period-btn').forEach(b => b.addEventListener('click', () => setPeriod(b.dataset.period)));
  // Unit toggle
  $$('.unit-btn').forEach(b => b.addEventListener('click', () => setDistUnit(b.dataset.unit)));

  // City select (navbar)
  const cityNav = $('cityNav');
  if (cityNav) cityNav.addEventListener('change', e => onCityChange(e.target.value));

  // Calculate
  $('calcBtn') && $('calcBtn').addEventListener('click', calculate);
  // Reset
  $('resetBtn') && $('resetBtn').addEventListener('click', resetAll);

  // Initial state
  setPeriod('month');
  setDistUnit('km');
  initPriceOverrideTracking();
  initFAQ();
  handlePrefill();
  initAnalytics();
});

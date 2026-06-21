/**
 * Website Frontend — website.js
 * CarDekho Deals UI interactions
 */

// Analytics event tracking
function trackEvent(eventName, data) {
    const payload = Object.assign({
        eventName: eventName,
        pageUrl: window.location.href
    }, data || {});
    fetch('/api/analytics/event', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        keepalive: true
    }).catch(function() {});
}

// Scroll to element with sticky-bar offset compensation
function scrollToId(id) {
    var el = document.getElementById(id);
    if (!el) return;
    var nav = document.getElementById('mainNav');
    var tabBar = document.getElementById('detailTabBar');
    var offset = (nav ? nav.offsetHeight : 68) + (tabBar ? tabBar.offsetHeight : 0) + 12;
    var y = el.getBoundingClientRect().top + window.scrollY - offset;
    window.scrollTo({ top: y, behavior: 'smooth' });
}

// Auto-track page_view on load and wire data-attribute click tracking
document.addEventListener('DOMContentLoaded', function() {
    trackEvent('page_view', {});

    document.querySelectorAll('.track-deal-click').forEach(function(el) {
        el.addEventListener('click', function() {
            trackEvent('deal_click', {
                dealId: el.dataset.dealId,
                dealName: el.dataset.dealName,
                vehicleId: el.dataset.vehicleId,
                vehicleName: el.dataset.vehicleName
            });
        });
    });

    document.querySelectorAll('.track-brand-click').forEach(function(el) {
        el.addEventListener('click', function() {
            trackEvent('brand_click', { vehicleName: el.dataset.brandName });
        });
    });
});

const ProgressiveDisclosure = {
    init() {
        document.querySelectorAll('[data-pd-section]').forEach(section => this.initSection(section));
    },

    initSection(section) {
        const limit = parseInt(section.dataset.pdLimit, 10) || 4;
        const items = section.querySelectorAll('[data-pd-item]');
        const toggle = section.querySelector('[data-pd-toggle]');
        if (!toggle || items.length <= limit) {
            if (toggle) toggle.style.display = 'none';
            return;
        }

        items.forEach((item, i) => {
            if (i >= limit) item.classList.add('pd-item-hidden');
        });

        const contentId = section.id || section.dataset.pdSection + '-content';
        if (!section.id) section.id = contentId;
        toggle.setAttribute('aria-controls', contentId);
        toggle.setAttribute('aria-expanded', 'false');

        toggle.addEventListener('click', (e) => {
            e.preventDefault();
            this.toggle(section, items, limit, toggle);
        });
    },

    toggle(section, items, limit, toggle) {
        const expanded = toggle.getAttribute('aria-expanded') === 'true';
        const labelMore = toggle.dataset.pdLabelMore || 'Read More';
        const labelLess = toggle.dataset.pdLabelLess || 'Show Less';
        const isGallery = section.dataset.pdSection === 'gallery';

        items.forEach((item, i) => {
            if (i >= limit) {
                item.classList.toggle('pd-item-hidden', expanded);
            }
        });

        toggle.setAttribute('aria-expanded', expanded ? 'false' : 'true');
        toggle.innerHTML = (expanded ? labelMore : labelLess) +
            ' <i class="fas fa-chevron-' + (expanded ? 'down' : 'up') + ' ms-1" style="font-size:10px"></i>';
    }
};

const VehicleListing = {
    init() {
        const grid = document.getElementById('vehicleGrid');
        const sortSelect = document.getElementById('vehicleSort');
        if (!grid) return;

        if (sortSelect) {
            sortSelect.addEventListener('change', () => this.sort(grid, sortSelect.value));
        }
    },

    sort(grid, mode) {
        const cards = Array.from(grid.querySelectorAll('[data-vehicle-card]'));
        cards.sort((a, b) => {
            const priceA = parseFloat(a.dataset.price) || 0;
            const priceB = parseFloat(b.dataset.price) || 0;
            const nameA = (a.dataset.name || '').toLowerCase();
            const nameB = (b.dataset.name || '').toLowerCase();
            switch (mode) {
                case 'price-asc': return priceA - priceB;
                case 'price-desc': return priceB - priceA;
                case 'name-asc': return nameA.localeCompare(nameB);
                case 'name-desc': return nameB.localeCompare(nameA);
                default: return 0;
            }
        });
        cards.forEach(card => grid.appendChild(card));
    }
};

const VehicleDetail = {
    init() {
        this.initTabs();
        this.initGallerySort();
        this.initDescription();
        this.initFaqSection();
        this.initFaqAnswers();
        this.initExpertReviews();
    },

    scrollToSection(el) {
        const tabBar = document.getElementById('detailTabBar');
        const nav = document.getElementById('mainNav');
        const offset = (nav ? nav.offsetHeight : 68) + (tabBar ? tabBar.offsetHeight : 48) + 8;
        const y = el.getBoundingClientRect().top + window.scrollY - offset;
        window.scrollTo({ top: y, behavior: 'smooth' });
    },

    initTabs() {
        const tabBar = document.getElementById('detailTabBar');
        if (!tabBar) return;

        tabBar.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                const href = link.getAttribute('href');
                if (href && href.startsWith('#')) {
                    e.preventDefault();
                    const target = document.querySelector(href);
                    if (target) {
                        this.scrollToSection(target);
                        tabBar.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
                        link.classList.add('active');
                    }
                }
            });
        });

        const sections = [];
        tabBar.querySelectorAll('.nav-link').forEach(link => {
            const id = link.getAttribute('href');
            if (id && id.startsWith('#')) {
                const el = document.querySelector(id);
                if (el) sections.push({ link, el });
            }
        });

        if (sections.length) {
            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const match = sections.find(s => s.el === entry.target);
                        if (match) {
                            tabBar.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
                            match.link.classList.add('active');
                        }
                    }
                });
            }, { rootMargin: '-40% 0px -50% 0px', threshold: 0 });

            sections.forEach(s => observer.observe(s.el));
        }
    },

    initGallerySort() {
        const grid = document.getElementById('galleryGrid');
        if (!grid) return;

        const imgs = Array.from(grid.querySelectorAll('.gallery-img'));
        const priority = ['exterior', 'vehicle'];
        imgs.sort((a, b) => {
            const catA = (a.dataset.category || '').toLowerCase();
            const catB = (b.dataset.category || '').toLowerCase();
            const priA = priority.includes(catA) ? 0 : 1;
            const priB = priority.includes(catB) ? 0 : 1;
            if (priA !== priB) return priA - priB;
            return (parseInt(a.dataset.order, 10) || 0) - (parseInt(b.dataset.order, 10) || 0);
        });
        imgs.forEach(img => grid.appendChild(img));
    },

    initDescription() {
        const text = document.getElementById('descText');
        const btn = document.getElementById('descToggle');
        if (!text || !btn) return;
        if (text.scrollHeight <= text.clientHeight + 5) {
            btn.style.display = 'none';
            return;
        }
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const expanded = text.style.maxHeight === 'none';
            text.style.maxHeight = expanded ? '7.5em' : 'none';
            btn.innerHTML = (expanded ? 'Read More' : 'Read Less') +
                ' <i class="fas fa-chevron-' + (expanded ? 'down' : 'up') + ' ms-1" style="font-size:10px"></i>';
        });
    },

    initFaqSection() {
        const items = document.querySelectorAll('.faq-accordion-item');
        const toggle = document.getElementById('faqSectionToggle');
        if (!toggle || items.length <= 5) return;

        items.forEach((el, i) => { if (i >= 5) el.classList.add('pd-item-hidden'); });
        toggle.style.display = 'inline-block';
        toggle.addEventListener('click', (e) => {
            e.preventDefault();
            const expanded = toggle.dataset.expanded === 'true';
            items.forEach((el, i) => {
                if (i >= 5) el.classList.toggle('pd-item-hidden', expanded);
            });
            toggle.dataset.expanded = expanded ? 'false' : 'true';
            toggle.innerHTML = (expanded ? 'Read More' : 'Show Less') +
                ' <i class="fas fa-chevron-' + (expanded ? 'down' : 'up') + ' ms-1" style="font-size:10px"></i>';
        });
    },

    initFaqAnswers() {
        document.querySelectorAll('.faq-answer-wrap').forEach(wrap => {
            const btn = wrap.nextElementSibling;
            if (!btn || !btn.classList.contains('faq-read-more')) return;
            if (wrap.scrollHeight > wrap.clientHeight + 5) {
                btn.style.display = 'inline-block';
                btn.addEventListener('click', (e) => {
                    e.preventDefault();
                    const expanded = wrap.style.maxHeight === 'none';
                    wrap.style.maxHeight = expanded ? '7.5em' : 'none';
                    btn.innerHTML = (expanded ? 'Read More' : 'Read Less') +
                        ' <i class="fas fa-chevron-' + (expanded ? 'down' : 'up') + ' ms-1" style="font-size:9px"></i>';
                });
            }
        });
    },

    initExpertReviews() {
        document.querySelectorAll('.expert-review-text').forEach(text => {
            const btn = text.nextElementSibling;
            if (!btn || !btn.classList.contains('expert-review-toggle')) return;
            if (text.scrollHeight > text.clientHeight + 5) {
                btn.style.display = 'inline-block';
                btn.addEventListener('click', (e) => {
                    e.preventDefault();
                    const expanded = text.style.maxHeight === 'none';
                    text.style.maxHeight = expanded ? '7.5em' : 'none';
                    btn.innerHTML = (expanded ? 'Read More' : 'Read Less') +
                        ' <i class="fas fa-chevron-' + (expanded ? 'down' : 'up') + ' ms-1" style="font-size:9px"></i>';
                });
            }
        });
    }
};

const HeroSearch = {
    _timer: null,
    _dropdown: null,

    init() {
        const form = document.getElementById('heroSearchForm');
        const input = document.getElementById('heroSearchInput');
        if (!form || !input) return;

        // Create dropdown
        this._dropdown = document.createElement('div');
        this._dropdown.id = 'heroSearchDropdown';
        this._dropdown.style.cssText = 'position:absolute;left:0;right:0;top:100%;background:#fff;border-radius:0 0 14px 14px;box-shadow:0 8px 32px rgba(0,0,0,.14);z-index:999;display:none;overflow:hidden;border:1px solid #e5e7eb;border-top:none;max-height:420px;overflow-y:auto';
        const wrap = input.closest('.input-group') || form;
        wrap.style.position = 'relative';
        wrap.appendChild(this._dropdown);

        input.addEventListener('input', () => {
            clearTimeout(this._timer);
            const q = input.value.trim();
            if (q.length < 2) { this._hide(); return; }
            this._timer = setTimeout(() => this._search(q), 220);
        });

        input.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') this._hide();
        });

        document.addEventListener('click', (e) => {
            if (!form.contains(e.target)) this._hide();
        });

        form.addEventListener('submit', (e) => {
            e.preventDefault();
            this._hide();
            const q = input.value.trim().toLowerCase();
            if (q) {
                trackEvent('search_query', { searchQuery: q });
                window.location.href = '/vehicles?search=' + encodeURIComponent(q);
            } else {
                window.location.href = '/vehicles';
            }
        });
    },

    async _search(q) {
        try {
            const [vRes, dRes] = await Promise.all([
                fetch('/api/vehicles?search=' + encodeURIComponent(q) + '&size=5').then(r => r.json()).catch(() => null),
                fetch('/api/deals').then(r => r.json()).catch(() => null)
            ]);

            const vehicles = (vRes && vRes.content) ? vRes.content : (Array.isArray(vRes) ? vRes : []);
            const ql = q.toLowerCase();
            const deals = Array.isArray(dRes)
                ? dRes.filter(d => d.vehicleName && d.vehicleName.toLowerCase().includes(ql)).slice(0, 4)
                : [];

            this._render(q, vehicles.slice(0, 5), deals);
        } catch (e) { this._hide(); }
    },

    _render(q, vehicles, deals) {
        if (!vehicles.length && !deals.length) { this._hide(); return; }
        const hl = (text) => {
            if (!text) return '';
            return text.replace(new RegExp('(' + q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + ')', 'gi'),
                '<strong style="color:var(--primary,#FF5A00)">$1</strong>');
        };

        let html = '';

        if (vehicles.length) {
            html += '<div style="padding:8px 14px 4px;font-size:11px;font-weight:700;color:#94a3b8;text-transform:uppercase;letter-spacing:.6px">Cars</div>';
            vehicles.forEach(v => {
                const slug = v.slug || '';
                const img = v.heroImage || v.thumbnailImage || '';
                html += `<a href="/vehicles/${slug}" style="display:flex;align-items:center;gap:12px;padding:10px 16px;text-decoration:none;color:#0f172a;border-bottom:1px solid #f1f5f9;transition:background .15s" onmouseover="this.style.background='#fff7ed'" onmouseout="this.style.background=''">
                    <div style="width:44px;height:36px;border-radius:6px;overflow:hidden;background:#f1f5f9;flex-shrink:0;display:flex;align-items:center;justify-content:center">
                        ${img ? `<img src="${img}" style="width:100%;height:100%;object-fit:cover" onerror="this.style.display='none'">` : '<i class="fas fa-car" style="color:#94a3b8;font-size:14px"></i>'}
                    </div>
                    <div style="flex:1;min-width:0">
                        <div style="font-size:13.5px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${hl(v.name)}</div>
                        <div style="font-size:11.5px;color:#64748b">${v.brand || ''} ${v.startingPrice ? '· ₹' + Number(v.startingPrice).toLocaleString('en-IN') : ''}</div>
                    </div>
                    <i class="fas fa-arrow-right" style="color:#d1d5db;font-size:11px"></i>
                </a>`;
            });
        }

        if (deals.length) {
            html += '<div style="padding:8px 14px 4px;font-size:11px;font-weight:700;color:#94a3b8;text-transform:uppercase;letter-spacing:.6px">Active Deals</div>';
            deals.forEach(d => {
                const slug = '';
                html += `<a href="/vehicles" style="display:flex;align-items:center;gap:12px;padding:10px 16px;text-decoration:none;color:#0f172a;border-bottom:1px solid #f1f5f9;transition:background .15s" onmouseover="this.style.background='#fff7ed'" onmouseout="this.style.background=''">
                    <div style="width:44px;height:36px;border-radius:6px;background:#fff7ed;flex-shrink:0;display:flex;align-items:center;justify-content:center">
                        <i class="fas fa-tags" style="color:#FF5A00;font-size:14px"></i>
                    </div>
                    <div style="flex:1;min-width:0">
                        <div style="font-size:13.5px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${hl(d.vehicleName)} <span style="background:#fff7ed;color:#FF5A00;font-size:10px;padding:2px 6px;border-radius:4px;font-weight:700;margin-left:4px">Deal</span></div>
                        <div style="font-size:11.5px;color:#059669;font-weight:600">Save ₹${Number(d.totalSavings || 0).toLocaleString('en-IN')}</div>
                    </div>
                    <i class="fas fa-arrow-right" style="color:#d1d5db;font-size:11px"></i>
                </a>`;
            });
        }

        html += `<a href="/vehicles?search=${encodeURIComponent(q)}" style="display:flex;align-items:center;justify-content:center;gap:8px;padding:11px;font-size:13px;font-weight:600;color:#FF5A00;text-decoration:none;background:#fff7ed">
            <i class="fas fa-search"></i> See all results for "${q}"
        </a>`;

        this._dropdown.innerHTML = html;
        this._dropdown.style.display = 'block';
    },

    _hide() {
        if (this._dropdown) this._dropdown.style.display = 'none';
    }
};

// ── Counter animation ───────────────────────────────────────────────────────
function animateCounter(el) {
    var target = parseInt(el.getAttribute('data-target')) || 0;
    if (target === 0) return;
    var duration = 1400;
    var start = performance.now();
    function tick(now) {
        var progress = Math.min((now - start) / duration, 1);
        var eased = 1 - Math.pow(1 - progress, 3);
        el.textContent = Math.round(eased * target);
        if (progress < 1) requestAnimationFrame(tick);
    }
    requestAnimationFrame(tick);
}

// ── Scroll-triggered animations ─────────────────────────────────────────────
function initScrollAnimations() {
    var countersDone = false;
    var animObserver = new IntersectionObserver(function(entries) {
        entries.forEach(function(e) {
            if (!e.isIntersecting) return;
            e.target.classList.add('is-visible');
            animObserver.unobserve(e.target);
        });
    }, { threshold: 0.1, rootMargin: '0px 0px -30px 0px' });

    var counterObserver = new IntersectionObserver(function(entries) {
        entries.forEach(function(e) {
            if (!e.isIntersecting || countersDone) return;
            countersDone = true;
            document.querySelectorAll('.counter-val[data-target]').forEach(animateCounter);
        });
    }, { threshold: 0.5 });

    document.querySelectorAll('.animate-on-scroll').forEach(function(el) {
        animObserver.observe(el);
    });

    // Observe stats bar for counters
    var statsBar = document.querySelector('.stats-bar');
    if (statsBar) counterObserver.observe(statsBar);

    // Also run counters if stats bar already in view on load
    if (statsBar) {
        var rect = statsBar.getBoundingClientRect();
        if (rect.top < window.innerHeight) {
            countersDone = true;
            document.querySelectorAll('.counter-val[data-target]').forEach(animateCounter);
        }
    }
}

// ── Hero animations run via CSS keyframes (no JS needed) ────────────────────
function initHeroAnimations() {}

// ── Skeleton loading for images ──────────────────────────────────────────────
function initSkeletonImages() {
    document.querySelectorAll('img[loading="lazy"]').forEach(function(img) {
        if (img.complete) return;
        img.style.opacity = '0';
        img.style.transition = 'opacity .3s ease';
        img.addEventListener('load', function() { img.style.opacity = '1'; });
        img.addEventListener('error', function() { img.style.opacity = '1'; });
    });
}

document.addEventListener('DOMContentLoaded', function () {
    initScrollAnimations();
    initHeroAnimations();
    initSkeletonImages();

    const nav = document.getElementById('mainNav');
    if (nav) {
        if (nav.classList.contains('nav-solid') || !document.querySelector('.hero-section')) {
            nav.classList.add('scrolled');
        }
        const topBar = document.querySelector('.top-bar');
        const topBarH = topBar ? topBar.offsetHeight : 33;
        const onScroll = () => {
            if (window.scrollY > topBarH) {
                nav.classList.add('scrolled');
                nav.style.top = '0';
            } else {
                nav.style.top = topBarH + 'px';
                if (!nav.classList.contains('nav-solid') && document.querySelector('.hero-section')) nav.classList.remove('scrolled');
            }
        };
        window.addEventListener('scroll', onScroll, { passive: true });
        onScroll();
    }

    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            const href = this.getAttribute('href');
            if (!href || href.length <= 1) return;
            const target = document.querySelector(href);
            if (target) {
                e.preventDefault();
                scrollToId(target.id || href.slice(1));
            }
        });
    });

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('is-visible');
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });

    document.querySelectorAll('.brand-card, .deal-card, .vehicle-card, .why-card, .service-card').forEach(el => {
        el.classList.add('animate-on-scroll');
        observer.observe(el);
    });

    ProgressiveDisclosure.init();
    VehicleListing.init();
    VehicleDetail.init();
    HeroSearch.init();

    // ── Home: Offers by Type filter pills ──
    const offerTabs = document.querySelectorAll('.obt-pill');
    const offerCards = document.querySelectorAll('#offerCardsGrid > [data-offer-types]');
    const offerNoResults = document.getElementById('offerNoResults');
    if (offerTabs.length) {
        offerTabs.forEach(tab => {
            tab.addEventListener('click', () => {
                offerTabs.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');
                const filter = tab.dataset.offer;
                let visible = 0;
                offerCards.forEach(card => {
                    const types = (card.dataset.offerTypes || '').toLowerCase();
                    const show = filter === 'all' || types.includes(filter);
                    card.style.display = show ? '' : 'none';
                    if (show) visible++;
                });
                if (offerNoResults) offerNoResults.style.display = visible === 0 ? '' : 'none';
            });
        });
    }

    // ── Home: Popular Cars strip scroll ──
    const pcdStrip = document.getElementById('pcdStrip');
    const pcdPrev  = document.getElementById('pcdPrev');
    const pcdNext  = document.getElementById('pcdNext');
    if (pcdStrip && pcdPrev && pcdNext) {
        const scroll = dir => pcdStrip.scrollBy({ left: dir * 640, behavior: 'smooth' });
        pcdPrev.addEventListener('click', () => scroll(-1));
        pcdNext.addEventListener('click', () => scroll(1));
        const sync = () => {
            pcdPrev.style.opacity = pcdStrip.scrollLeft < 10 ? '0.35' : '1';
            pcdNext.style.opacity = pcdStrip.scrollLeft + pcdStrip.clientWidth >= pcdStrip.scrollWidth - 10 ? '0.35' : '1';
        };
        pcdStrip.addEventListener('scroll', sync, { passive: true });
        sync();
    }

    // ── Vehicles: offer quick-filter chips ──
    const oqfChips = document.querySelectorAll('.oqf-chip');
    if (oqfChips.length) {
        oqfChips.forEach(chip => {
            chip.addEventListener('click', () => {
                oqfChips.forEach(c => c.classList.remove('active'));
                chip.classList.add('active');
                const filter = chip.dataset.dealFilter;
                document.querySelectorAll('#vehicleGrid [data-vehicle-card]').forEach(card => {
                    if (filter === 'all') { card.style.display = ''; return; }
                    const badge = card.querySelector('.vehicle-deal-badge');
                    const hasDeal = !!badge;
                    if (filter === 'has-deal') { card.style.display = hasDeal ? '' : 'none'; return; }
                    // For cash/exchange/corporate/finance we rely on badge presence for now
                    card.style.display = hasDeal ? '' : 'none';
                });
            });
        });
    }

    // ── Vehicles: sort by savings ──
    const sortSelect = document.getElementById('vehicleSort');
    if (sortSelect) {
        sortSelect.addEventListener('change', () => {
            if (sortSelect.value !== 'savings-desc') return;
            const grid = document.getElementById('vehicleGrid');
            if (!grid) return;
            const cards = [...grid.querySelectorAll('[data-vehicle-card]')];
            cards.sort((a, b) => {
                const sa = parseFloat((a.querySelector('.vehicle-deal-badge')?.textContent.replace(/[^\d]/g,'')) || 0);
                const sb = parseFloat((b.querySelector('.vehicle-deal-badge')?.textContent.replace(/[^\d]/g,'')) || 0);
                return sb - sa;
            });
            cards.forEach(c => grid.appendChild(c));
        });
    }
});

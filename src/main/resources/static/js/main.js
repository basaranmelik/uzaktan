// ============================================
// GUZEM — Main JavaScript
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    initNavbar();
    initScrollAnimations();
    initAccordions();
    setActiveNavLink();
});

// ---------- Navbar ----------
function initNavbar() {
    const hamburger = document.querySelector('.nav-hamburger');
    const navLinks = document.querySelector('.nav-links');

    if (hamburger && navLinks) {
        hamburger.addEventListener('click', () => {
            hamburger.classList.toggle('active');
            navLinks.classList.toggle('active');
        });

        // Close on link click (mobile)
        navLinks.querySelectorAll('a').forEach(link => {
            link.addEventListener('click', () => {
                hamburger.classList.remove('active');
                navLinks.classList.remove('active');
            });
        });
    }

    // Scroll effect
    window.addEventListener('scroll', () => {
        const navbar = document.querySelector('.navbar');
        if (navbar) {
            navbar.classList.toggle('scrolled', window.scrollY > 20);
        }
    });
}

// ---------- Active nav link ----------
function setActiveNavLink() {
    const path = window.location.pathname;
    const filename = path.split('/').pop() || 'index.html';

    document.querySelectorAll('.nav-links a:not(.nav-cta)').forEach(link => {
        const href = link.getAttribute('href');
        if (href === filename || (filename === '' && href === 'index.html') || (filename === 'index.html' && href === 'index.html')) {
            link.classList.add('active');
        }
    });
}

// ---------- Scroll Animations ----------
function initScrollAnimations() {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1, rootMargin: '0px 0px -50px 0px' });

    document.querySelectorAll('.fade-in, .fade-in-left, .fade-in-right').forEach(el => {
        observer.observe(el);
    });
}

// ---------- Accordion ----------
function initAccordions() {
    document.querySelectorAll('.accordion-header').forEach(header => {
        header.addEventListener('click', () => {
            const item = header.parentElement;
            const isActive = item.classList.contains('active');

            // Close all
            item.closest('.accordion-container')?.querySelectorAll('.accordion-item').forEach(i => {
                i.classList.remove('active');
            });

            // Toggle current
            if (!isActive) {
                item.classList.add('active');
            }
        });
    });
}

// ---------- Render Helpers ----------
function renderStars(rating) {
    const full = Math.floor(rating);
    const half = rating - full >= 0.5 ? 1 : 0;
    const empty = 5 - full - half;
    return '★'.repeat(full) + (half ? '½' : '') + '☆'.repeat(empty);
}

function renderCourseCard(course) {
    return `
    <a href="egitim-detay.html?id=${course.id}" class="card course-card fade-in">
      <div class="card-image">
        <img src="${course.image}" alt="${course.title}" loading="lazy">
        <span class="card-badge">${course.level}</span>
      </div>
      <div class="card-body">
        <span class="card-category">${course.category}</span>
        <h4 class="card-title">${course.title}</h4>
        <p class="card-desc">${course.description}</p>
        <div class="card-meta">
          <div class="meta-item">
            <span class="stars">${renderStars(course.rating)}</span>
            <span>${course.rating}</span>
          </div>
          <div class="meta-item">
            📚 ${course.lessons} Ders
          </div>
          <span class="card-price">${course.price}</span>
        </div>
      </div>
    </a>
  `;
}

// ---------- Page-specific init ----------

// Counter animation
function animateCounters() {
    const counters = document.querySelectorAll('[data-count]');
    const observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const el = entry.target;
                const target = parseInt(el.getAttribute('data-count'));
                const suffix = el.getAttribute('data-suffix') || '';
                const duration = 2000;
                const step = target / (duration / 16);
                let current = 0;

                const timer = setInterval(() => {
                    current += step;
                    if (current >= target) {
                        clearInterval(timer);
                        current = target;
                    }
                    el.textContent = Math.floor(current).toLocaleString('tr-TR') + suffix;
                }, 16);

                observer.unobserve(el);
            }
        });
    }, { threshold: 0.5 });

    counters.forEach(c => observer.observe(c));
}

// ---------- Mobile Dropdown ----------
function initMobileDropdown() {
  const trigger = document.querySelector('.nav-dropdown-trigger');
  const dropdown = document.querySelector('.nav-dropdown');
  if (!trigger || !dropdown) return;

  trigger.addEventListener('click', (e) => {
    if (window.innerWidth <= 768) {
      e.preventDefault();
      dropdown.classList.toggle('mobile-open');
    }
  });
}

document.addEventListener('DOMContentLoaded', () => {
  initMobileDropdown();
});
// ---------- Hero Slider ----------
function initHeroSlider() {
  const slides = document.querySelectorAll('.hero-slide');
  const dots   = document.querySelectorAll('.slider-dot');
  const prev   = document.getElementById('sliderPrev');
  const next   = document.getElementById('sliderNext');
  if (!slides.length) return;

  let current = 0;
  let timer;

  function goTo(idx) {
    slides[current].classList.remove('active');
    dots[current].classList.remove('active');
    current = (idx + slides.length) % slides.length;
    slides[current].classList.add('active');
    dots[current].classList.add('active');
  }

  function startAuto() {
    timer = setInterval(() => goTo(current + 1), 3500);
  }

  function resetAuto() {
    clearInterval(timer);
    startAuto();
  }

  prev.addEventListener('click', () => { goTo(current - 1); resetAuto(); });
  next.addEventListener('click', () => { goTo(current + 1); resetAuto(); });
  dots.forEach((dot, i) => dot.addEventListener('click', () => { goTo(i); resetAuto(); }));

  startAuto();
}

document.addEventListener('DOMContentLoaded', initHeroSlider);
// ---------- Egitimler Sidebar Filtre ----------
function initSidebarFilters() {
  if (!document.getElementById('courses-grid')) return;

  let activeSort   = 'default';
  let minPrice     = 0;
  let maxPrice     = 500;
  let minRating    = 0;
  let activeLevels = [];

  // Sıralama
  document.querySelectorAll('.sort-option').forEach(opt => {
    opt.addEventListener('click', () => {
      document.querySelectorAll('.sort-option').forEach(o => o.classList.remove('active'));
      opt.classList.add('active');
      activeSort = opt.dataset.sort;
      renderFiltered();
    });
  });

  // Fiyat range
  const minEl = document.getElementById('price-min');
  const maxEl = document.getElementById('price-max');
  const minLbl = document.getElementById('price-min-label');
  const maxLbl = document.getElementById('price-max-label');

  if (minEl && maxEl) {
    minEl.addEventListener('input', () => {
      minPrice = parseInt(minEl.value);
      if (minPrice > maxPrice) minPrice = maxPrice;
      minLbl.textContent = '₺' + minPrice;
      renderFiltered();
    });
    maxEl.addEventListener('input', () => {
      maxPrice = parseInt(maxEl.value);
      if (maxPrice < minPrice) maxPrice = minPrice;
      maxLbl.textContent = '₺' + maxPrice;
      renderFiltered();
    });
  }

  // Fiyat preset
  document.querySelectorAll('.price-preset').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.price-preset').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      minPrice = parseInt(btn.dataset.min);
      maxPrice = parseInt(btn.dataset.max);
      if (minEl) { minEl.value = minPrice; minLbl.textContent = '₺' + minPrice; }
      if (maxEl) { maxEl.value = maxPrice; maxLbl.textContent = '₺' + maxPrice; }
      renderFiltered();
    });
  });

  // Seviye
  document.querySelectorAll('.level-check').forEach(chk => {
    chk.addEventListener('change', () => {
      activeLevels = [...document.querySelectorAll('.level-check:checked')].map(c => c.value);
      renderFiltered();
    });
  });

  // Puan
  document.querySelectorAll('.rating-opt').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.rating-opt').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      minRating = parseFloat(btn.dataset.rating);
      renderFiltered();
    });
  });

  // Sıfırla
  document.getElementById('reset-filters')?.addEventListener('click', () => {
    activeSort = 'default'; minPrice = 0; maxPrice = 500; minRating = 0; activeLevels = [];
    document.querySelectorAll('.sort-option')[0]?.click();
    document.querySelectorAll('.price-preset')[0]?.click();
    document.querySelectorAll('.rating-opt')[0]?.click();
    document.querySelectorAll('.level-check').forEach(c => c.checked = false);
    if (minEl) { minEl.value = 0; minLbl.textContent = '₺0'; }
    if (maxEl) { maxEl.value = 500; maxLbl.textContent = '₺500'; }
    renderFiltered();
  });

  function getPrice(course) {
    return parseInt(course.price.replace(/[^\d]/g, ''));
  }

  function renderFiltered() {
    const grid = document.getElementById('courses-grid');
    const searchVal = document.getElementById('course-search')?.value?.toLowerCase() || '';

    let filtered = COURSES.filter(c => {
      const p = getPrice(c);
      const matchPrice  = p >= minPrice && p <= maxPrice;
      const matchRating = c.rating >= minRating;
      const matchLevel  = activeLevels.length === 0 || activeLevels.includes(c.level);
      const matchSearch = !searchVal || c.title.toLowerCase().includes(searchVal) || c.category.toLowerCase().includes(searchVal);
      return matchPrice && matchRating && matchLevel && matchSearch;
    });

    // Sıralama
    if (activeSort === 'az')         filtered.sort((a,b) => a.title.localeCompare(b.title, 'tr'));
    else if (activeSort === 'za')    filtered.sort((a,b) => b.title.localeCompare(a.title, 'tr'));
    else if (activeSort === 'price-asc')  filtered.sort((a,b) => getPrice(a) - getPrice(b));
    else if (activeSort === 'price-desc') filtered.sort((a,b) => getPrice(b) - getPrice(a));
    else if (activeSort === 'rating')     filtered.sort((a,b) => b.rating - a.rating);
    else if (activeSort === 'popular')    filtered.sort((a,b) => b.students - a.students);

    if (filtered.length === 0) {
      grid.innerHTML = '<div style="grid-column:1/-1;text-align:center;padding:3rem;color:var(--text-muted);">Filtreyle eşleşen eğitim bulunamadı.</div>';
    } else {
      grid.innerHTML = filtered.map(renderCourseCard).join('');
    }
    initScrollAnimations();
  }

  // Arama ile entegre
  document.getElementById('course-search')?.addEventListener('input', renderFiltered);

  renderFiltered();
}

document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById('courses-grid')) initSidebarFilters();
});

// ---------- Toast Notification System ----------
function showToast(message, type = 'info', duration = 4000) {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    let icon = 'bi-info-circle-fill';
    if (type === 'success') icon = 'bi-check-circle-fill';
    if (type === 'error') icon = 'bi-exclamation-octagon-fill';
    if (type === 'warning') icon = 'bi-exclamation-triangle-fill';

    toast.innerHTML = `
        <i class="bi ${icon}"></i>
        <span style="flex: 1;">${message}</span>
        <button class="toast-close" style="background: none; border: none; color: inherit; cursor: pointer; padding: 0; font-size: 1.2rem; opacity: 0.6;">×</button>
    `;

    const closeBtn = toast.querySelector('.toast-close');
    let timeoutId = null;
    let dismissed = false;

    const removeToast = () => {
        if (!dismissed) {
            dismissed = true;
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
            if (timeoutId) clearTimeout(timeoutId);
        }
    };

    closeBtn.addEventListener('click', removeToast);

    container.appendChild(toast);

    // Show
    setTimeout(() => toast.classList.add('show'), 10);

    // Auto-hide
    timeoutId = setTimeout(removeToast, duration);
}

// ---------- Video Locking Handler ----------
function initVideoLocking() {
    // Handle clicks on elements with .locked class
    document.addEventListener('click', (e) => {
        const lockedItem = e.target.closest('.locked');
        if (lockedItem) {
            e.preventDefault();
            e.stopPropagation();
            showToast('Lütfen önce sıradaki önceki videoları tamamlayın.', 'warning');
        }
    }, true);
}

// ---------- Global Toast Bridge ----------
function initGlobalToasts() {
    const navbar = document.getElementById('navbar');
    if (navbar) {
        const success = navbar.getAttribute('data-toast-success');
        const error = navbar.getAttribute('data-toast-error');

        if (success && success.trim() !== '') {
            showToast(success, 'success');
        }
        if (error && error.trim() !== '') {
            showToast(error, 'error');
        }
    }
}

// ============================================
// COURSE LIST PAGE — Filtering & Sorting
// ============================================
function initCourseListFilters() {
    const grid = document.getElementById('courses-grid');
    if (!grid) return;

    const searchInput = document.getElementById('course-search');
    const noResultsMsg = document.getElementById('no-results-msg');
    const resultCount = document.getElementById('result-count');
    const filterBadge = document.getElementById('active-filter-badge');

    if (!grid.querySelectorAll('.course-card').length) return; // Cards rendered server-side

    let allCards = Array.from(grid.querySelectorAll('.course-card'));
    let originalOrder = allCards.slice();
    let activeSort = 'default';
    let activeCategory = '';
    let minPrice = 0;
    let maxPrice = 200000;

    function getPrice(card) {
        return parseFloat(card.dataset.price) || 0;
    }

    function applyFilters() {
        const q = searchInput ? searchInput.value.trim().toLowerCase() : '';
        const isFiltered = q || minPrice > 0 || maxPrice < 200000 || activeSort !== 'default' || activeCategory;
        if (filterBadge) filterBadge.style.display = isFiltered ? 'inline-block' : 'none';

        let visible = allCards.filter(card => {
            const price = getPrice(card);
            const title = (card.dataset.title || '').toLowerCase();
            const cat = (card.dataset.category || '').toLowerCase();
            const matchPrice = price >= minPrice && price <= maxPrice;
            const matchSearch = !q || title.includes(q);
            const matchCategory = !activeCategory || cat === activeCategory;
            return matchPrice && matchSearch && matchCategory;
        });

        // Sorting
        if (activeSort === 'az')
            visible.sort((a, b) => (a.dataset.title || '').localeCompare(b.dataset.title || '', 'tr'));
        else if (activeSort === 'za')
            visible.sort((a, b) => (b.dataset.title || '').localeCompare(a.dataset.title || '', 'tr'));
        else if (activeSort === 'price-asc')
            visible.sort((a, b) => getPrice(a) - getPrice(b));
        else if (activeSort === 'price-desc')
            visible.sort((a, b) => getPrice(b) - getPrice(a));
        else
            visible.sort((a, b) => originalOrder.indexOf(a) - originalOrder.indexOf(b));

        // Update visibility
        allCards.forEach(c => c.classList.add('course-card-hidden'));
        visible.forEach(c => {
            c.classList.remove('course-card-hidden');
            grid.appendChild(c);
        });

        // Update count
        const countSpan = resultCount ? resultCount.querySelector('span:first-child') : null;
        if (countSpan) countSpan.textContent = visible.length;

        // Empty state
        if (noResultsMsg) noResultsMsg.style.display = visible.length === 0 ? 'block' : 'none';
        grid.style.display = visible.length === 0 ? 'none' : '';
    }

    // Category filtering
    document.querySelectorAll('.category-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.category-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            activeCategory = (btn.dataset.category || '').toLowerCase();
            applyFilters();
        });
    });

    // Sorting
    document.querySelectorAll('.sort-option').forEach(opt => {
        opt.addEventListener('click', () => {
            document.querySelectorAll('.sort-option').forEach(o => o.classList.remove('active'));
            opt.classList.add('active');
            activeSort = opt.dataset.sort;
            applyFilters();
        });
    });

    // Price range
    const minEl = document.getElementById('price-min');
    const maxEl = document.getElementById('price-max');
    const minLbl = document.getElementById('price-min-label');
    const maxLbl = document.getElementById('price-max-label');

    if (minEl && maxEl) {
        minEl.addEventListener('input', () => {
            minPrice = parseInt(minEl.value);
            if (minPrice > maxPrice) { minPrice = maxPrice; minEl.value = minPrice; }
            if (minLbl) minLbl.textContent = '₺' + minPrice.toLocaleString('tr-TR');
            applyFilters();
        });
        maxEl.addEventListener('input', () => {
            maxPrice = parseInt(maxEl.value);
            if (maxPrice < minPrice) { maxPrice = minPrice; maxEl.value = maxPrice; }
            if (maxLbl) maxLbl.textContent = '₺' + maxPrice.toLocaleString('tr-TR');
            applyFilters();
        });
    }

    // Price presets
    document.querySelectorAll('.price-preset').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.price-preset').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            minPrice = parseInt(btn.dataset.min);
            maxPrice = parseInt(btn.dataset.max);
            if (minEl) { minEl.value = minPrice; if (minLbl) minLbl.textContent = '₺' + minPrice.toLocaleString('tr-TR'); }
            if (maxEl) { maxEl.value = maxPrice; if (maxLbl) maxLbl.textContent = '₺' + maxPrice.toLocaleString('tr-TR'); }
            applyFilters();
        });
    });

    // Live search
    if (searchInput) searchInput.addEventListener('input', applyFilters);

    // Reset
    const resetBtn = document.getElementById('reset-filters');
    if (resetBtn) {
        resetBtn.addEventListener('click', () => {
            activeSort = 'default'; minPrice = 0; maxPrice = 200000; activeCategory = '';
            document.querySelectorAll('.category-btn').forEach(b => b.classList.remove('active'));
            const firstCat = document.querySelectorAll('.category-btn')[0];
            if (firstCat) firstCat.classList.add('active');
            document.querySelectorAll('.sort-option')[0]?.click();
            document.querySelectorAll('.price-preset')[0]?.click();
            if (minEl) { minEl.value = 0; if (minLbl) minLbl.textContent = '₺0'; }
            if (maxEl) { maxEl.value = 200000; if (maxLbl) maxLbl.textContent = '₺200.000'; }
            if (searchInput) searchInput.value = '';
            applyFilters();
        });
    }

    applyFilters();
}

// ============================================
// SSS PAGE — Accordion
// ============================================
function initSssAccordion() {
    const accordionHeaders = document.querySelectorAll('.accordion-header');
    if (!accordionHeaders.length) return;

    accordionHeaders.forEach(header => {
        header.addEventListener('click', () => {
            const item = header.parentElement;
            const body = header.nextElementSibling;
            if (!item || !body) return;

            // Close others
            document.querySelectorAll('.accordion-item').forEach(i => {
                if (i !== item) {
                    i.classList.remove('active');
                    const b = i.querySelector('.accordion-body');
                    if (b) b.style.maxHeight = null;
                }
            });

            // Toggle current
            item.classList.toggle('active');
            if (item.classList.contains('active')) {
                body.style.maxHeight = "500px";
            } else {
                body.style.maxHeight = null;
            }
        });
    });
}

// ============================================
// ADMIN — Course Videos (Reorder, Edit Modal, Upload)
// ============================================
function initAdminCourseVideos() {
    const videoList = document.getElementById('videoList');
    if (!videoList) return;

    // Check if Sortable is available
    if (typeof Sortable !== 'undefined') {
        const originalOrder = Array.from(videoList.querySelectorAll('.vl-row')).map(row => row.dataset.videoId);
        const saveOrderBar = document.getElementById('saveOrderBar');

        Sortable.create(videoList, {
            handle: '.vl-handle',
            animation: 150,
            onSort: function() {
                videoList.querySelectorAll('.vl-row').forEach(function(row, idx) {
                    const numEl = row.querySelector('.vl-num');
                    if (numEl) numEl.textContent = idx + 1;
                });
                const currentOrder = Array.from(videoList.querySelectorAll('.vl-row')).map(row => row.dataset.videoId);
                const changed = JSON.stringify(originalOrder) !== JSON.stringify(currentOrder);
                if (saveOrderBar) saveOrderBar.classList.toggle('visible', changed);
            }
        });

        const saveOrderBtn = document.getElementById('saveOrderBtn');
        if (saveOrderBtn) {
            saveOrderBtn.addEventListener('click', function() {
                const captureOrder = () => Array.from(videoList.querySelectorAll('.vl-row')).map(row => row.dataset.videoId);
                const ids = captureOrder().map(Number);
                const url = videoList.dataset.reorderUrl;
                this.disabled = true;
                this.innerHTML = '<i class="bi bi-hourglass-split"></i> Kaydediliyor...';
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

                fetch(url, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
                    body: JSON.stringify(ids)
                }).then(res => {
                    if (res.ok) {
                        showToast('Sıralama kaydedildi.', 'success');
                        if (saveOrderBar) saveOrderBar.classList.remove('visible');
                    } else {
                        showToast('Sıralama kaydedilemedi!', 'error');
                    }
                }).catch(() => {
                    showToast('Bağlantı hatası!', 'error');
                }).finally(() => {
                    this.disabled = false;
                    this.innerHTML = '<i class="bi bi-check-lg"></i> Sırayı Kaydet';
                });
            });
        }

        const cancelOrderBtn = document.getElementById('cancelOrderBtn');
        if (cancelOrderBtn) {
            cancelOrderBtn.addEventListener('click', () => {
                // Restore original order
                originalOrder.forEach(function(id) {
                    const row = videoList.querySelector('[data-video-id="' + id + '"]');
                    if (row) videoList.appendChild(row);
                });
                videoList.querySelectorAll('.vl-row').forEach(function(row, idx) {
                    const numEl = row.querySelector('.vl-num');
                    if (numEl) numEl.textContent = idx + 1;
                });
                if (saveOrderBar) saveOrderBar.classList.remove('visible');
            });
        }
    }

    // Edit modal - event delegation for edit buttons
    document.querySelectorAll('.edit-video-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const id = this.dataset.id;
            const title = this.dataset.title;
            const desc = this.dataset.description;
            const editTitle = document.getElementById('editTitle');
            const editDescription = document.getElementById('editDescription');
            const editForm = document.getElementById('editForm');
            const editModalBackdrop = document.getElementById('editModalBackdrop');

            if (editTitle) editTitle.value = title || '';
            if (editDescription) editDescription.value = desc || '';
            if (editForm) editForm.action = '/admin/videolar/' + id + '/duzenle';
            if (editModalBackdrop) editModalBackdrop.classList.add('open');
            if (editTitle) editTitle.focus();
        });
    });

    // Close modal handlers
    const editModalBackdrop = document.getElementById('editModalBackdrop');
    const closeBtn = document.querySelector('.vl-modal-close');
    const cancelBtn = document.querySelector('.cancel-modal-btn');

    function closeModal() {
        if (editModalBackdrop) editModalBackdrop.classList.remove('open');
    }

    if (closeBtn) closeBtn.addEventListener('click', closeModal);
    if (cancelBtn) cancelBtn.addEventListener('click', closeModal);
    if (editModalBackdrop) {
        editModalBackdrop.addEventListener('click', function(e) {
            if (e.target === this) closeModal();
        });
    }

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') closeModal();
    });
}

// ============================================
// ADMIN — Video Upload Preview
// ============================================
function initVideoUpload() {
    const uploadForm = document.getElementById('uploadForm');
    if (!uploadForm) return;

    const fileInput = document.getElementById('videoFiles');
    if (!fileInput) return;

    fileInput.addEventListener('change', function() {
        const preview = document.getElementById('filePreview');
        const list = document.getElementById('fileList');
        const btn = document.getElementById('uploadBtn');
        const warning = document.getElementById('limitWarning');
        const warningText = document.getElementById('limitWarningText');
        const files = Array.from(this.files);

        if (files.length === 0) {
            if (preview) preview.style.display = 'none';
            if (warning) warning.style.display = 'none';
            return;
        }

        const currentCount = parseInt(uploadForm.dataset.currentCount || '0');
        const moduleCount = parseInt(uploadForm.dataset.moduleCount || '0');
        const remaining = moduleCount > 0 ? moduleCount - currentCount : 999;

        // Limit check
        if (moduleCount > 0 && files.length > remaining) {
            if (warning) warning.style.display = 'block';
            if (warningText) warningText.textContent = 'En fazla ' + remaining + ' video daha ekleyebilirsiniz.';
            if (btn) btn.disabled = true;
        } else {
            if (warning) warning.style.display = 'none';
            if (btn) btn.disabled = false;
        }

        if (list) {
            list.innerHTML = files.map((f, i) => {
                const nameNoExt = f.name.includes('.') ? f.name.substring(0, f.name.lastIndexOf('.')) : f.name;
                const sizeMb = (f.size / (1024 * 1024)).toFixed(1);
                return `
                    <div style="padding:0.75rem 1rem;${i < files.length - 1 ? 'border-bottom:1px solid var(--gray-100);' : ''}">
                        <div style="display:flex;align-items:center;gap:0.75rem;font-size:0.875rem;margin-bottom:0.5rem;">
                            <span style="color:var(--text-muted);font-size:0.78rem;flex:1;min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${f.name.replace(/</g, '&lt;')}</span>
                            <span style="color:var(--text-muted);font-size:0.78rem;flex-shrink:0;">${sizeMb} MB</span>
                        </div>
                        <div style="display:flex;gap:0.6rem;align-items:center;">
                            <input type="text" name="titles" value="${nameNoExt.replace(/"/g, '&quot;')}" class="form-control"
                                   placeholder="Video başlığı girin" maxlength="200" required
                                   style="font-size:0.85rem;padding:0.4rem 0.65rem;flex:1;">
                        </div>
                    </div>`;
            }).join('');
        }

        if (btn) btn.innerHTML = '<i class="bi bi-cloud-upload"></i> Yükle (' + files.length + ' video)';
        if (preview) preview.style.display = 'block';
    });

    // Form validation
    uploadForm.addEventListener('submit', function(e) {
        const titleInputs = this.querySelectorAll('input[name="titles"]');
        for (let i = 0; i < titleInputs.length; i++) {
            if (!titleInputs[i].value.trim()) {
                e.preventDefault();
                titleInputs[i].focus();
                titleInputs[i].style.borderColor = '#c92a2a';
                return;
            }
        }
        const uploadBtn = document.getElementById('uploadBtn');
        const uploadHint = document.getElementById('uploadHint');
        if (uploadBtn) uploadBtn.disabled = true;
        if (uploadHint) uploadHint.style.display = 'inline';
    });
}

// ============================================
// NAVBAR — Panel Toggle, Cart, Notifications
// ============================================
function initNavbar() {
    // Panel toggle (notifications)
    const notifWrap = document.getElementById('notifWrap');
    if (notifWrap) {
        const notifBtn = notifWrap.querySelector('.nav-icon-btn');
        if (notifBtn) {
            notifBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                notifWrap.classList.toggle('open');
            });
        }

        document.addEventListener('click', (e) => {
            if (!notifWrap.contains(e.target)) {
                notifWrap.classList.remove('open');
            }
        });
    }

    // Cart item removal
    document.querySelectorAll('.nav-cart-remove').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            const courseId = btn.dataset.id;
            const panel = document.getElementById('cartPanel');
            if (!panel || !courseId) return;

            const csrf = panel.dataset.csrfToken;
            const header = panel.dataset.csrfHeader;

            fetch('/sepet/kaldir-ajax?courseId=' + courseId, {
                method: 'POST',
                headers: { [header]: csrf }
            }).then(res => res.json())
              .then(data => {
                const row = btn.closest('.nav-cart-item');
                if (row) row.remove();

                const badge = document.getElementById('cartBadge');
                if (badge) {
                    badge.textContent = data.count;
                    badge.style.display = data.count > 0 ? '' : 'none';
                }
                showToast('Eğitim sepetten kaldırıldı.', 'success');

                const countEl = panel.querySelector('.nav-panel-count');
                if (countEl) countEl.textContent = data.count + ' kurs';

                if (data.total !== undefined) {
                    const totalStr = '₺' + data.total.toLocaleString('tr-TR', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
                    const totalEl = panel.querySelector('.nav-panel-footer strong');
                    if (totalEl) totalEl.textContent = totalStr;
                }

                if (data.count === 0) {
                    const body = panel.querySelector('.nav-panel-body');
                    const footer = panel.querySelector('.nav-panel-footer');
                    if (body && footer) {
                        body.closest('div').innerHTML =
                            '<div style="padding:2rem;text-align:center;color:var(--text-muted);">' +
                            '<i class="bi bi-cart3" style="font-size:2rem;display:block;margin-bottom:0.5rem;color:var(--gray-300);"></i>' +
                            '<div style="font-size:0.85rem;">Sepetiniz boş</div></div>';
                        footer.style.display = 'none';
                    }
                }
              });
        });
    });

    // Notification removal
    document.querySelectorAll('.nav-notif-remove').forEach(btn => {
        btn.addEventListener('click', () => {
            const panel = document.getElementById('notifPanel');
            if (!panel) return;
            const row = btn.closest('.nav-notif-item');
            if (!row) return;
            const notifId = row.dataset.id;
            if (!notifId) return;

            const csrf = panel.dataset.csrfToken;
            const header = panel.dataset.csrfHeader || 'X-CSRF-TOKEN';

            fetch('/bildirimler/' + notifId + '/sil', {
                method: 'POST',
                headers: {
                    [header]: csrf,
                    'X-Requested-With': 'XMLHttpRequest'
                }
            }).then(res => {
                if (!res.ok) throw new Error('Bildirimi kapatma basarisiz');
                return res.json();
            }).then(data => {
                row.remove();
                const badge = document.getElementById('notifBadge');
                if (badge) {
                    badge.textContent = data.count;
                    badge.style.display = data.count > 0 ? '' : 'none';
                }
                const countEl = panel.querySelector('.nav-panel-count');
                if (countEl) countEl.textContent = data.count + ' yeni';

                const body = panel.querySelector('.nav-panel-body');
                if (body && body.querySelectorAll('.nav-notif-item').length === 0) {
                    body.innerHTML =
                        '<div style="padding:2rem;text-align:center;color:var(--text-muted);">' +
                        '<i class="bi bi-bell-slash" style="font-size:2rem;display:block;margin-bottom:0.5rem;color:var(--gray-300);"></i>' +
                        '<div style="font-size:0.85rem;">Yeni bildirim yok</div></div>';
                }
            }).catch(() => {
                showToast('Bildirim kapatilirken bir hata olustu.', 'error');
            });
        });
    });

    // Notification polling (30s)
    const notifBadge = document.getElementById('notifBadge');
    if (notifBadge) {
        function pollNotifCount() {
            fetch('/bildirimler/sayac', { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
                .then(r => r.ok ? r.json() : null)
                .then(data => {
                    if (!data) return;
                    notifBadge.textContent = data.count;
                    notifBadge.style.display = data.count > 0 ? '' : 'none';
                    const countEl = document.querySelector('#notifPanel .nav-panel-count');
                    if (countEl) countEl.textContent = data.count + ' yeni';
                }).catch(() => {});
        }
        setInterval(pollNotifCount, 30000);
    }

    // Navbar search sync
    const navSearchInput = document.getElementById('navSearchInput');
    if (navSearchInput) {
        navSearchInput.addEventListener('input', () => {
            const courseSearch = document.getElementById('course-search');
            if (courseSearch) {
                courseSearch.value = navSearchInput.value;
                courseSearch.dispatchEvent(new Event('input'));
            }
        });

        // Fill existing keyword on courses page
        const params = new URLSearchParams(window.location.search);
        const kw = params.get('keyword');
        if (kw && window.location.pathname === '/egitimler') {
            navSearchInput.value = kw;
        }
    }
}

// ============================================
// ADMIN — Users Page (Modal, Filter, Auto-submit)
// ============================================
function initAdminUsers() {
    // Role select auto-submit
    document.querySelectorAll('.role-select-auto-submit').forEach(select => {
        select.addEventListener('change', () => {
            select.closest('form')?.submit();
        });
    });

    // Confirm delete buttons
    document.querySelectorAll('.confirm-delete-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            if (!confirm('Bu kullanıcıyı silmek istediğinize emin misiniz?')) {
                e.preventDefault();
            }
        });
    });

    // Filter
    const filterInput = document.getElementById('user-filter');
    if (filterInput) {
        const rows = Array.from(document.querySelectorAll('.user-row'));
        const count = document.getElementById('filter-count');

        filterInput.addEventListener('input', () => {
            const q = filterInput.value.trim().toLowerCase();
            let visible = 0;
            rows.forEach(r => {
                const name = (r.querySelector('.row-fullname')?.textContent || '').toLowerCase();
                const email = (r.querySelector('.row-email')?.textContent || '').toLowerCase();
                const show = !q || name.includes(q) || email.includes(q);
                r.style.display = show ? '' : 'none';
                if (show) visible++;
            });
            if (count) count.textContent = q ? visible + ' sonuç' : '';
        });
    }

    // User modal
    const modalOverlay = document.getElementById('userModalOverlay');
    const userModal = document.getElementById('userModal');

    function closeModal() {
        if (modalOverlay) modalOverlay.style.display = 'none';
        if (userModal) userModal.style.display = 'none';
    }

    if (modalOverlay && userModal) {
        modalOverlay.addEventListener('click', closeModal);

        document.querySelectorAll('.close-user-modal-btn').forEach(btn => {
            btn.addEventListener('click', closeModal);
        });

        document.querySelectorAll('.show-user-modal-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.getElementById('modalFullName').textContent = btn.dataset.name || '-';
                document.getElementById('modalEmail').textContent = btn.dataset.email || '-';
                document.getElementById('modalPhone').textContent = btn.dataset.phone && btn.dataset.phone !== 'null' ? btn.dataset.phone : 'Belirtilmemiş';
                document.getElementById('modalBirthDate').textContent = btn.dataset.birth && btn.dataset.birth !== 'null' ? btn.dataset.birth : 'Belirtilmemiş';

                let locationStr = 'Belirtilmemiş';
                if (btn.dataset.city && btn.dataset.city !== 'null') {
                    locationStr = btn.dataset.city;
                    if (btn.dataset.district && btn.dataset.district !== 'null') locationStr += ' / ' + btn.dataset.district;
                }
                document.getElementById('modalLocation').textContent = locationStr;
                document.getElementById('modalAddress').textContent = btn.dataset.address && btn.dataset.address !== 'null' ? btn.dataset.address : 'Adres bilgisi yok';
                document.getElementById('modalZipcode').textContent = btn.dataset.zip && btn.dataset.zip !== 'null' ? btn.dataset.zip : 'Posta Kodu Yok';

                modalOverlay.style.display = 'block';
                userModal.style.display = 'block';
            });
        });
    }
}

// ============================================
// ADMIN — Generic Table Filters
// ============================================
function initAdminTableFilters() {
    // Generic filter input pattern: [id$="-filter"] with count [id$="-count"]
    const filterInputs = document.querySelectorAll('[id$="-filter"]');
    filterInputs.forEach(input => {
        const prefix = input.id.replace('-filter', '');
        const countEl = document.getElementById(prefix + '-count') || document.getElementById('filter-count');
        const rowSelector = '.' + prefix + '-row';
        const rows = Array.from(document.querySelectorAll(rowSelector));

        if (!rows.length) return;

        input.addEventListener('input', () => {
            const q = input.value.trim().toLowerCase();
            let visible = 0;
            rows.forEach(r => {
                const text = r.textContent.toLowerCase();
                const show = !q || text.includes(q);
                r.style.display = show ? '' : 'none';
                if (show) visible++;
            });
            if (countEl) countEl.textContent = q ? visible + ' sonuç' : '';
        });
    });
}

// ============================================
// ADMIN — Course Forms (Type Toggle, Schedule Days)
// ============================================
function initCourseForms() {
    const courseForm = document.getElementById('courseForm');
    if (!courseForm) return;

    // Course type field visibility toggle
    function updateTypeFields() {
        const selected = document.querySelector('input[name="courseType"]:checked')?.value
                         || document.querySelector('input[name="courseType"]')?.value
                         || 'ONLINE';

        document.querySelectorAll('[data-show-for]').forEach(el => {
            const types = el.dataset.showFor.split(',');
            el.style.display = types.includes(selected) ? '' : 'none';
        });

        // Update description label based on course type
        const descLabel = document.getElementById('desc-label') || document.getElementById('desc-label-edit');
        const descHint = document.getElementById('desc-hint') || document.getElementById('desc-hint-edit');
        const quotaInput = document.getElementById('quota-input');

        if (selected === 'REMOTE_FORMAL') {
            if (descLabel) descLabel.textContent = 'Açıklama';
            if (descHint) descHint.style.display = 'block';
            if (quotaInput) quotaInput.required = true;
        } else {
            if (descLabel) descLabel.textContent = 'Açıklama';
            if (descHint) descHint.style.display = 'none';
            if (quotaInput) quotaInput.required = (selected !== 'ONLINE');
        }
    }

    document.querySelectorAll('input[name="courseType"]').forEach(r => {
        r.addEventListener('change', updateTypeFields);
    });
    updateTypeFields();

    // Update scheduleDays hidden field when form is submitted
    courseForm.addEventListener('submit', () => {
        const days = Array.from(document.querySelectorAll('input[name="scheduleDaysArray"]:checked'))
            .map(cb => cb.value).join(',');
        const scheduleDaysInput = document.getElementById('scheduleDays');
        if (scheduleDaysInput) scheduleDaysInput.value = days;
    });
}

// ============================================
// ADMIN — Auto-submit Selects
// ============================================
function initAutoSubmitSelects() {
    document.querySelectorAll('.auto-submit-select').forEach(select => {
        select.addEventListener('change', () => {
            select.closest('form')?.submit();
        });
    });
}

// ============================================
// ADMIN — Confirm Delete Buttons
// ============================================
function initConfirmDeleteBtns() {
    document.querySelectorAll('.confirm-delete-btn').forEach(btn => {
        if (!btn.dataset.confirmMessage) {
            btn.dataset.confirmMessage = 'Bu öğeyi silmek istediğinize emin misiniz?';
        }
        btn.addEventListener('click', (e) => {
            if (!confirm(btn.dataset.confirmMessage)) {
                e.preventDefault();
            }
        });
    });
}

// ============================================
// AUTH — Phone Input Formatter
// ============================================
function initPhoneInput() {
    const phoneInput = document.getElementById('phoneNumber');
    if (!phoneInput) return;

    if (!phoneInput.value.startsWith('+90 ')) {
        phoneInput.value = '+90 ';
    }

    phoneInput.addEventListener('input', function() {
        let input = phoneInput.value.replace(/\D/g, '');
        if (input.length === 0) {
            phoneInput.value = '+90 ';
            return;
        }
        if (!input.startsWith('90')) {
            input = '90' + input;
        }
        if (input.length > 12) input = input.substring(0, 12);

        let formatted = '+90 ';
        if (input.length > 2) formatted += input.substring(2, 5);
        if (input.length > 5) formatted += ' ' + input.substring(5, 8);
        if (input.length > 8) formatted += ' ' + input.substring(8, 10);
        if (input.length > 10) formatted += ' ' + input.substring(10, 12);

        phoneInput.value = formatted;
    });

    phoneInput.addEventListener('keydown', function(e) {
        if (e.key === 'Backspace' && phoneInput.value.length <= 4) {
            e.preventDefault();
        }
    });
}

// ============================================
// EĞİTMEN — Question Search
// ============================================
function initQuestionSearch() {
    const searchInput = document.getElementById('questionSearch');
    if (!searchInput) return;

    searchInput.addEventListener('input', function() {
        const term = this.value.toLowerCase();
        const cards = document.querySelectorAll('.q-card');
        let visible = 0;
        cards.forEach(function(card) {
            const text = card.getAttribute('data-search')?.toLowerCase() || '';
            const match = text.includes(term);
            card.style.display = match ? '' : 'none';
            if (match) visible++;
        });
        const countEl = document.getElementById('visibleCount');
        if (countEl) countEl.textContent = visible;
    });
}

// ============================================
// EĞİTMEN — Question Form Preview
// ============================================
function initQuestionFormPreview() {
    const fields = { Q: 'inputQ', A: 'inputA', B: 'inputB', C: 'inputC', D: 'inputD', E: 'inputE' };
    const previewCard = document.getElementById('previewCard');
    const previewQ = document.getElementById('previewQ');
    const previewExpl = document.getElementById('previewExpl');
    const qCharCount = document.getElementById('qCharCount');
    const explCharCount = document.getElementById('explCharCount');
    const inputQ = document.getElementById('inputQ');
    const inputExpl = document.getElementById('inputExpl');

    if (!previewCard || !inputQ) return;

    function updatePreview() {
        const qText = inputQ.value.trim();
        if (!qText) {
            previewCard.classList.add('hidden');
            return;
        }
        previewCard.classList.remove('hidden');
        previewQ.textContent = qText;

        const selected = document.querySelector('input[name="correctOption"]:checked');
        const correctVal = selected ? selected.value : '';

        ['A', 'B', 'C', 'D', 'E'].forEach(function(letter) {
            const el = document.getElementById('preview' + letter);
            if (!el) return;
            const val = document.getElementById('input' + letter)?.value || '—';
            el.querySelector('span:last-child').textContent = val;
            el.classList.toggle('correct', correctVal === letter);
        });

        const explText = inputExpl?.value.trim();
        if (previewExpl) {
            if (explText) {
                previewExpl.classList.remove('hidden');
                previewExpl.querySelector('span').textContent = explText;
            } else {
                previewExpl.classList.add('hidden');
            }
        }
    }

    if (inputExpl) {
        inputExpl.addEventListener('input', function() {
            if (explCharCount) explCharCount.textContent = this.value.length;
            updatePreview();
        });
    }

    inputQ.addEventListener('input', function() {
        if (qCharCount) qCharCount.textContent = this.value.length;
        updatePreview();
    });

    Object.keys(fields).forEach(function(key) {
        const el = document.getElementById(fields[key]);
        if (el) el.addEventListener('input', updatePreview);
    });

    document.querySelectorAll('input[name="correctOption"]').forEach(function(radio) {
        radio.addEventListener('change', updatePreview);
    });

    if (qCharCount) qCharCount.textContent = inputQ.value.length;
    if (explCharCount && inputExpl) explCharCount.textContent = inputExpl.value.length;
    updatePreview();
}

// ============================================
// EĞİTMEN — Zoom Copy Join Link
// ============================================
function initZoomCopyLink() {
    document.querySelectorAll('.copy-join-link-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const path = btn.dataset.url;
            if (!path) return;
            const fullUrl = window.location.origin + path;
            navigator.clipboard.writeText(fullUrl).then(() => {
                const orig = btn.innerHTML;
                btn.innerHTML = '<i class="bi bi-check-lg"></i> Kopyalandı!';
                btn.style.borderColor = '#2b8a3e';
                btn.style.color = '#2b8a3e';
                setTimeout(() => {
                    btn.innerHTML = orig;
                    btn.style.borderColor = '';
                    btn.style.color = '';
                }, 2000);
            });
        });
    });
}

// ============================================
// Demo Alert Buttons
// ============================================
function initDemoAlerts() {
    document.querySelectorAll('.data-demo-alert').forEach(btn => {
        btn.addEventListener('click', function() {
            alert(btn.dataset.demoAlertMessage || 'Bu bir demo sayfasıdır.');
        });
    });
}

// ============================================
// Toggle Excel Panel
// ============================================
function initExcelPanelToggle() {
    document.querySelectorAll('.toggle-excel-panel-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const panel = document.getElementById('excelUploadPanel');
            if (panel) {
                panel.classList.toggle('sb-hidden');
            }
        });
    });
}

// ============================================
// Course List - Reset Filters
// ============================================
function initCourseListReset() {
    const resetBtn = document.getElementById('reset-filters-btn');
    if (resetBtn) {
        resetBtn.addEventListener('click', function() {
            const resetFilters = document.getElementById('reset-filters');
            if (resetFilters) {
                resetFilters.click();
            }
        });
    }
}

// ============================================
// Course Detail — Module Toggle
// ============================================
function initCourseModuleToggle() {
    document.querySelectorAll('.cm-accord-head').forEach(head => {
        head.addEventListener('click', function() {
            const index = this.dataset.moduleIndex;
            if (index === undefined) return;
            const body = document.getElementById('mbody-' + index);
            const chevron = document.getElementById('chevron-' + index);
            if (body) {
                const isOpen = body.style.display !== 'none';
                body.style.display = isOpen ? 'none' : '';
            }
            if (chevron) {
                const isOpen = body.style.display !== 'none';
                chevron.innerHTML = isOpen
                    ? '<i class="bi bi-chevron-down"></i>'
                    : '<i class="bi bi-chevron-up"></i>';
            }
        });
    });
}

// ============================================
// Video — Theater Mode Toggle
// ============================================
function initTheaterModeToggle() {
    document.querySelectorAll('.theater-mode-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const mainCol = document.getElementById('video-main-col');
            if (mainCol) {
                mainCol.classList.toggle('theater-mode');
            }
        });
    });
}

// ============================================
// Quiz — Exam Page
// ============================================
function initQuizExam() {
    const examData = document.getElementById('examData');
    if (!examData) return;

    const totalQuestions = parseInt(examData.dataset.totalQuestions) || 0;
    let answered = new Set();

    function markAnswered(radio) {
        const qnum = radio.dataset.qnum;
        answered.add(qnum);

        const card = document.getElementById('q' + qnum);
        if (card) card.classList.add('answered');

        const dot = document.getElementById('nav-' + qnum);
        if (dot) dot.classList.add('answered');

        const answeredCount = document.getElementById('answeredCount');
        if (answeredCount) answeredCount.textContent = answered.size;

        const progressFill = document.getElementById('progressFill');
        if (progressFill) {
            const pct = (answered.size / totalQuestions) * 100;
            progressFill.style.width = pct + '%';
        }
    }

    document.querySelectorAll('input[type="radio"]').forEach(radio => {
        radio.addEventListener('change', function() {
            markAnswered(this);
        });
    });

    const submitBtn = document.getElementById('submitBtn');
    if (submitBtn) {
        submitBtn.addEventListener('click', function(e) {
            if (answered.size < totalQuestions) {
                const unanswered = totalQuestions - answered.size;
                if (!confirm(unanswered + ' soru cevaplanmamış. Yine de göndermek istiyor musunuz?')) {
                    e.preventDefault();
                }
            }
        });
    }

    document.querySelectorAll('input[type="radio"]:checked').forEach(radio => {
        markAnswered(radio);
    });
}

// ============================================
// Course Form — Curriculum Builder
// ============================================
function initCurriculumBuilder() {
    const builder = document.getElementById('curriculum-builder');
    const jsonField = document.getElementById('manualCurriculumJson');
    if (!builder || !jsonField) return;

    function getModules() {
        const modules = [];
        builder.querySelectorAll('.cm-module').forEach(card => {
            const title = card.querySelector('.cm-title-input').value.trim();
            const description = card.querySelector('.cm-desc').value.trim();
            const topics = Array.from(card.querySelectorAll('.cm-topic-input'))
                .map(i => i.value.trim()).filter(v => v);
            if (title) modules.push({ title, description, topics });
        });
        return modules;
    }

    function addTopic(topicsDiv) {
        const row = document.createElement('div');
        row.className = 'cm-topic-row';
        row.innerHTML = `
            <input class="cm-topic-input" type="text" placeholder="Konu başlığı...">
            <button type="button" class="cm-topic-remove" title="Konuyu Sil"><i class="bi bi-x-lg"></i></button>
        `;
        row.querySelector('.cm-topic-remove').onclick = () => row.remove();
        topicsDiv.appendChild(row);
        return row.querySelector('.cm-topic-input');
    }

    function addModule(data = {}) {
        const idx = builder.querySelectorAll('.cm-module').length + 1;
        const card = document.createElement('div');
        card.className = 'cm-module';
        card.innerHTML = `
            <div class="cm-module-head">
                <div class="cm-module-num">${String(idx).padStart(2,'0')}</div>
                <input class="cm-title-input" type="text" placeholder="Modül başlığı..." value="${escHtml(data.title||'')}">
                <button type="button" class="cm-delete-module" title="Modülü Sil"><i class="bi bi-trash3"></i></button>
            </div>
            <div class="cm-module-body">
                <textarea class="cm-desc" rows="2" placeholder="Modül açıklaması (isteğe bağlı)...">${escHtml(data.description||'')}</textarea>
                <div class="cm-topics"></div>
                <button type="button" class="cm-add-topic"><i class="bi bi-plus-sm"></i> Konu Ekle</button>
            </div>
        `;
        card.querySelector('.cm-delete-module').onclick = () => {
            card.remove();
            renumberModules();
        };
        const topicsDiv = card.querySelector('.cm-topics');
        card.querySelector('.cm-add-topic').onclick = () => addTopic(topicsDiv).focus();
        (data.topics || []).forEach(t => { addTopic(topicsDiv).value = t; });
        if (!data.topics || !data.topics.length) addTopic(topicsDiv);
        builder.appendChild(card);
    }

    function renumberModules() {
        builder.querySelectorAll('.cm-module-num').forEach((n, i) => {
            n.textContent = String(i + 1).padStart(2, '0');
        });
    }

    function escHtml(str) {
        return String(str).replace(/&/g,'&amp;').replace(/"/g,'&quot;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
    }

    const addModuleBtn = document.getElementById('add-module-btn');
    if (addModuleBtn) {
        addModuleBtn.onclick = () => { addModule(); };
    }

    const courseForm = document.getElementById('courseForm');
    if (courseForm) {
        courseForm.addEventListener('submit', function() {
            jsonField.value = JSON.stringify(getModules());
        }, true);
    }

    const existingData = document.getElementById('existingCurriculumData');
    if (existingData) {
        const existing = existingData.textContent;
        if (existing && existing.trim().startsWith('[')) {
            try {
                JSON.parse(existing).forEach(m => addModule(m));
            } catch(e) { addModule(); }
        } else {
            addModule();
        }
    } else {
        addModule();
    }
}

// ============================================
// Course Form — Instructor Multi-Select
// ============================================
function initInstructorMultiSelect() {
    const wrap = document.getElementById('imsWrap');
    if (!wrap) return;

    const trigger = document.getElementById('imsTrigger');
    const tagsEl = document.getElementById('imsTags');
    const search = document.getElementById('imsSearch');
    const list = document.getElementById('imsList');
    let emptyEl = null;

    function getItems() { return Array.from(list.querySelectorAll('.ims-item')); }

    function renderTags() {
        const selected = getItems().filter(i => i.querySelector('input').checked);
        tagsEl.innerHTML = '';
        if (!selected.length) {
            tagsEl.innerHTML = '<span class="ims-ph">Eğitmen seçin...</span>';
            return;
        }
        selected.forEach(item => {
            const id = item.dataset.id;
            const name = item.dataset.name;
            const tag = document.createElement('span');
            tag.className = 'ims-tag';
            tag.innerHTML = name + '<button type="button" class="ims-tag-remove" aria-label="Kaldır"><i class="bi bi-x"></i></button>';
            tag.querySelector('.ims-tag-remove').addEventListener('click', e => {
                e.stopPropagation();
                const cb = list.querySelector('input[value="' + id + '"]');
                if (cb) { cb.checked = false; cb.closest('.ims-item').classList.remove('ims-selected'); }
                renderTags();
            });
            tagsEl.appendChild(tag);
        });
    }

    getItems().forEach(item => {
        item.addEventListener('click', e => {
            const cb = item.querySelector('input');
            if (e.target !== cb) cb.checked = !cb.checked;
            item.classList.toggle('ims-selected', cb.checked);
            renderTags();
        });
    });

    trigger.addEventListener('click', () => {
        wrap.classList.toggle('open');
        if (wrap.classList.contains('open')) { setTimeout(() => search.focus(), 50); }
    });

    trigger.addEventListener('keydown', e => {
        if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); wrap.classList.toggle('open'); }
        if (e.key === 'Escape') wrap.classList.remove('open');
    });

    search.addEventListener('input', () => {
        const q = search.value.toLowerCase();
        let any = false;
        getItems().forEach(item => {
            const match = item.dataset.name.toLowerCase().includes(q);
            item.style.display = match ? '' : 'none';
            if (match) any = true;
        });
        if (emptyEl) emptyEl.remove();
        if (!any) {
            emptyEl = document.createElement('div');
            emptyEl.className = 'ims-empty';
            emptyEl.textContent = 'Sonuç bulunamadı.';
            list.appendChild(emptyEl);
        } else { emptyEl = null; }
    });

    document.addEventListener('click', e => {
        if (!wrap.contains(e.target)) wrap.classList.remove('open');
    });

    renderTags();
}

// ============================================
// Contact Form — AJAX Submit
// ============================================
function initContactForm() {
    const form = document.getElementById('contact-form');
    const formSuccess = document.getElementById('formSuccess');
    if (!form) return;

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const btn = form.querySelector('.btn-submit');
        const originalText = btn.innerHTML;
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        const formData = {
            firstName: document.getElementById('firstName').value,
            lastName: document.getElementById('lastName').value,
            email: document.getElementById('email').value,
            phone: document.getElementById('phone').value,
            topic: document.getElementById('topic').value,
            message: document.getElementById('message').value
        };

        btn.textContent = 'Gönderiliyor...';
        btn.disabled = true;

        fetch('/iletisim', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(formData)
        })
        .then(response => response.json())
        .then(data => {
            btn.innerHTML = originalText;
            btn.disabled = false;

            if (data.success) {
                formSuccess.style.display = 'block';
                formSuccess.style.background = '#e6fcf5';
                formSuccess.style.color = '#0a7a5a';
                formSuccess.style.borderColor = '#20c997';
                formSuccess.innerHTML = '<i class="bi bi-check-circle-fill"></i> ' + data.message;
                form.reset();

                setTimeout(() => {
                    formSuccess.style.display = 'none';
                }, 6000);
            } else {
                throw new Error(data.message);
            }
        })
        .catch(error => {
            btn.innerHTML = originalText;
            btn.disabled = false;
            formSuccess.style.display = 'block';
            formSuccess.style.background = '#fff5f5';
            formSuccess.style.color = '#c92a2a';
            formSuccess.style.borderColor = '#ffa8a8';
            formSuccess.innerHTML = '<i class="bi bi-exclamation-triangle-fill"></i> Hata: ' + (error.message || 'Mesaj gönderilemedi.');
        });
    });
}

// ============================================
// Home Page — Animate Counters
// ============================================
function initHomeCounters() {
    if (typeof animateCounters === 'function') {
        animateCounters();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initVideoLocking();
    initGlobalToasts();
    initCourseListFilters();
    initSssAccordion();
    initAdminCourseVideos();
    initVideoUpload();
    initNavbar();
    initAdminUsers();
    initAdminTableFilters();
    initCourseForms();
    initAutoSubmitSelects();
    initConfirmDeleteBtns();
    initPhoneInput();
    initQuestionSearch();
    initQuestionFormPreview();
    initZoomCopyLink();
    initDemoAlerts();
    initExcelPanelToggle();
    initCourseListReset();
    initCourseModuleToggle();
    initTheaterModeToggle();
    initQuizExam();
    initCurriculumBuilder();
    initInstructorMultiSelect();
    initContactForm();
    initHomeCounters();
});


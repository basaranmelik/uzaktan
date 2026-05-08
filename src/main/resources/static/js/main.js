// ============================================
// GUZEM — Main JavaScript
// ============================================


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
        <button class="toast-close" style="background: none; border: none; color: inherit; cursor: pointer; padding: 0; font-size: 1.2rem; opacity: 0.5;">&times;</button>
    `;

    const closeBtn = toast.querySelector('.toast-close');
    let timeoutId = null;
    let dismissed = false;

    const removeToast = () => {
        if (!dismissed) {
            dismissed = true;
            toast.classList.remove('show');
            setTimeout(() => {
                if (toast.parentNode) toast.remove();
            }, 350);
            if (timeoutId) clearTimeout(timeoutId);
        }
    };

    closeBtn.addEventListener('click', removeToast);

    container.appendChild(toast);

    requestAnimationFrame(() => {
        toast.classList.add('show');
    });

    timeoutId = setTimeout(removeToast, duration);
}

function handleAjaxResponse(data) {
    if (!data) return;
    const msg = data.message || '';
    const type = data.type || (data.success ? 'success' : 'error');
    if (msg) showToast(msg, type);
}

async function ajaxPost(url, body, options = {}) {
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const csrf = csrfMeta ? csrfMeta.content : '';
    const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.content : 'X-CSRF-TOKEN';

    const headers = {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest',
        [csrfHeader]: csrf,
        ...(options.headers || {})
    };

    try {
        const res = await fetch(url, {
            method: 'POST',
            headers,
            body: JSON.stringify(body)
        });
        if (!res.ok) {
            const text = await res.text();
            let msg = text;
            try { const j = JSON.parse(text); msg = j.message || text; } catch (_) {}
            showToast(msg, 'error');
            return null;
        }
        const data = await res.json();
        handleAjaxResponse(data);
        if (options.onSuccess) options.onSuccess(data);
        return data;
    } catch (err) {
        showToast('Bağlantı hatası! Lütfen tekrar deneyin.', 'error');
        if (options.onError) options.onError(err);
        return null;
    }
}

async function ajaxFormSubmit(form, options = {}) {
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const csrf = csrfMeta ? csrfMeta.content : '';
    const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.content : 'X-CSRF-TOKEN';

    const formData = new FormData(form);

    try {
        const res = await fetch(form.action, {
            method: form.method || 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                [csrfHeader]: csrf,
                ...(options.headers || {})
            },
            body: formData
        });
        if (!res.ok) {
            const text = await res.text();
            let msg = text;
            try { const j = JSON.parse(text); msg = j.message || text; } catch (_) {}
            showToast(msg, 'error');
            return null;
        }
        const data = await res.json();
        handleAjaxResponse(data);
        if (options.onSuccess) options.onSuccess(data);
        return data;
    } catch (err) {
        showToast('Bağlantı hatası! Lütfen tekrar deneyin.', 'error');
        if (options.onError) options.onError(err);
        return null;
    }
}

// ---------- Flash Banner Auto-Dismiss ----------
function initFlashBanners() {
    document.querySelectorAll('.flash-banner[data-flash]').forEach(banner => {
        const closeBtn = banner.querySelector('.flash-close');
        let timeoutId = null;

        const dismiss = () => {
            if (banner.classList.contains('flash-dismissing')) return;
            banner.classList.add('flash-dismissing');
            if (timeoutId) clearTimeout(timeoutId);
            setTimeout(() => {
                if (banner.parentNode) banner.remove();
            }, 300);
        };

        if (closeBtn) {
            closeBtn.addEventListener('click', dismiss);
        }

        timeoutId = setTimeout(dismiss, 6000);
    });
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
// ============================================
// COURSE LIST PAGE — Filtering & Sorting
// ============================================
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
    // Hamburger menu (mobile)
    const hamburger = document.querySelector('.nav-hamburger');
    const navLinks = document.querySelector('.nav-links');
    if (hamburger && navLinks) {
        hamburger.addEventListener('click', () => {
            hamburger.classList.toggle('active');
            navLinks.classList.toggle('active');
        });
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
                document.getElementById('modalZoomEmail').textContent = btn.dataset.zoomemail || 'Tanımlanmamış';
                if (!btn.dataset.zoomemail) {
                    document.getElementById('modalZoomEmail').style.color = '#f08c00';
                    document.getElementById('modalZoomEmail').textContent = '⚠ Tanımlanmamış';
                }
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

        const isOnline = selected === 'ONLINE';
        const isFaceToFace = selected === 'FACE_TO_FACE';
        const isHybrid = selected === 'HYBRID';

        const descInput = document.getElementById('desc-input') || document.getElementById('desc-input-edit');
        const descLabel = document.getElementById('desc-label') || document.getElementById('desc-label-edit');
        const descHint = document.getElementById('desc-hint') || document.getElementById('desc-hint-edit');
        const quotaInput = document.getElementById('quota-input');
        const startDateInput = document.getElementById('startDate-input');
        const endDateInput = document.getElementById('endDate-input');
        const locationInput = document.getElementById('location-input');

        if (isHybrid) {
            if (descLabel) descLabel.textContent = 'Açıklama';
            if (descHint) descHint.style.display = 'block';
            if (descInput) descInput.removeAttribute('required');
            if (quotaInput) quotaInput.required = true;
        } else {
            if (descLabel) descLabel.textContent = 'Açıklama *';
            if (descHint) descHint.style.display = 'none';
            if (descInput) descInput.required = true;
            if (quotaInput) quotaInput.required = !isOnline;
        }

        if (startDateInput) startDateInput.required = !isOnline;
        if (endDateInput) endDateInput.required = !isOnline;

        if (locationInput) locationInput.required = isFaceToFace;
    }

    document.querySelectorAll('input[name="courseType"]').forEach(r => {
        r.addEventListener('change', updateTypeFields);
    });
    updateTypeFields();

    // Update scheduleDays hidden field and validate dates when form is submitted
    courseForm.addEventListener('submit', (e) => {
        const days = Array.from(document.querySelectorAll('input[name="scheduleDaysArray"]:checked'))
            .map(cb => cb.value).join(',');
        const scheduleDaysInput = document.getElementById('scheduleDays');
        if (scheduleDaysInput) scheduleDaysInput.value = days;

        // Validate startDate < endDate
        const startDateInput = document.getElementById('startDate-input');
        const endDateInput = document.getElementById('endDate-input');
        if (startDateInput && endDateInput && startDateInput.required && endDateInput.required) {
            const start = startDateInput.value;
            const end = endDateInput.value;
            if (start && end && start > end) {
                e.preventDefault();
                alert('Başlangıç tarihi, bitiş tarihinden sonra olamaz.');
                endDateInput.focus();
                return;
            }
        }
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
// EĞİTMEN — Question Bank (search + single-nav)
// ============================================
function initQuestionBank() {
    const qBankData = document.getElementById('qBankData');
    if (!qBankData) return;

    const totalQuestions = parseInt(qBankData.dataset.totalQuestions) || 0;
    if (totalQuestions === 0) return;

    let currentIndex = 0;
    let isSearching = false;
    let activeModuleFilter = '';
    const cards = document.querySelectorAll('#questionList .qb-card');
    const dots = document.querySelectorAll('#qbDots .qb-dot');
    const prevBtn = document.getElementById('qbPrevBtn');
    const nextBtn = document.getElementById('qbNextBtn');
    const counterBottom = document.getElementById('qbBottomCurrent');
    const counterTotal = document.getElementById('qbBottomTotal');
    const counterTop = document.getElementById('currentQ');
    const dotsScroll = document.getElementById('qbDotsScroll');
    const searchInput = document.getElementById('questionSearch');
    const searchClear = document.getElementById('qbSearchClear');
    const searchStatus = document.getElementById('qbSearchStatus');
    const moduleFilter = document.getElementById('qbModuleFilter');
    const totalBadge = document.querySelector('.qb-total-badge');

    function getVisibleCards() {
        const arr = [];
        cards.forEach((c, i) => {
            if (c.dataset.filtered !== 'false') arr.push({ card: c, index: i });
        });
        return arr;
    }

    function updateCounterUI(visibleCount, num) {
        if (counterBottom) counterBottom.textContent = num;
        if (counterTotal) counterTotal.textContent = visibleCount;
        if (counterTop) counterTop.textContent = num;
        if (totalBadge) totalBadge.textContent = visibleCount + ' soru';
        if (prevBtn) prevBtn.disabled = num <= 1;
        if (nextBtn) nextBtn.disabled = num >= visibleCount;
    }

    function showCardByIndex(index) {
        const visible = getVisibleCards();
        if (visible.length === 0) return;
        // Find the card with matching dataset.index
        const match = visible.find(v => parseInt(v.card.dataset.index) === index);
        if (!match) return;
        currentIndex = index;
        const num = visible.findIndex(v => v.card === match.card) + 1;
        cards.forEach(c => c.style.display = 'none');
        match.card.style.display = 'block';
        updateDots(index);
        updateCounterUI(visible.length, num);
    }

    function updateDots(activeDataIndex) {
        dots.forEach((dot, i) => {
            const idx = parseInt(dot.dataset.index);
            const active = idx === activeDataIndex;
            dot.classList.toggle('active', active);
            dot.setAttribute('aria-selected', active ? 'true' : 'false');
            if (active) {
                dot.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
            }
        });
    }

    function applyModuleFilter(moduleName) {
        activeModuleFilter = moduleName;
        isSearching = false;
        if (searchInput) searchInput.value = '';
        if (searchClear) searchClear.style.display = 'none';
        if (searchStatus) searchStatus.textContent = '';

        const lower = moduleName.toLowerCase();
        let firstIdx = -1;
        cards.forEach((card, i) => {
            const mod = (card.dataset.module || '').toLowerCase();
            const show = !moduleName || mod === lower;
            card.dataset.filtered = show ? 'true' : 'false';
            card.style.display = 'none';
            if (dots[i]) dots[i].style.display = show ? '' : 'none';
            if (show && firstIdx === -1) firstIdx = i;
        });
        if (firstIdx >= 0) showCardByIndex(firstIdx);
        updateCounterUI(getVisibleCards().length, firstIdx >= 0 ? 1 : 0);
    }

    function updateUI(index) {
        currentIndex = index;
        const visible = getVisibleCards();
        const pos = visible.findIndex(v => parseInt(v.card.dataset.index) === index);
        const num = pos >= 0 ? pos + 1 : 1;
        cards.forEach(c => c.style.display = 'none');
        if (pos >= 0) visible[pos].card.style.display = 'block';
        else if (visible.length > 0) visible[0].card.style.display = 'block';
        updateDots(index);
        updateCounterUI(visible.length, num);
    }

    function goTo(index) {
        const visible = getVisibleCards();
        if (visible.length === 0 || index < 0 || index >= cards.length) return;
        if (isSearching) clearSearch();
        // Find visible card at or after given index
        const after = visible.filter(v => parseInt(v.card.dataset.index) >= index);
        const target = after.length > 0 ? after[0] : visible[0];
        showCardByIndex(parseInt(target.card.dataset.index));
    }

    function goRelative(delta) {
        const visible = getVisibleCards();
        if (visible.length === 0) return;
        const curPos = visible.findIndex(v => parseInt(v.card.dataset.index) === currentIndex);
        const newPos = curPos + delta;
        if (newPos < 0 || newPos >= visible.length) return;
        showCardByIndex(parseInt(visible[newPos].card.dataset.index));
    }

    function clearSearch() {
        if (searchInput) searchInput.value = '';
        if (searchClear) searchClear.style.display = 'none';
        if (searchStatus) searchStatus.textContent = '';
        isSearching = false;
        applyModuleFilter(activeModuleFilter);
    }

    // Build module filter options
    if (moduleFilter) {
        const modulesMap = new Map();
        cards.forEach(card => {
            const mod = card.dataset.module;
            if (mod && mod !== 'null' && mod !== '') modulesMap.set(mod, (modulesMap.get(mod) || 0) + 1);
        });
        modulesMap.forEach((count, mod) => {
            const opt = document.createElement('option');
            opt.value = mod;
            opt.textContent = mod + ' (' + count + ')';
            moduleFilter.appendChild(opt);
        });
        moduleFilter.addEventListener('change', function() {
            applyModuleFilter(this.value);
        });
    }

    if (prevBtn) prevBtn.addEventListener('click', () => goRelative(-1));
    if (nextBtn) nextBtn.addEventListener('click', () => goRelative(1));

    dots.forEach(dot => {
        dot.addEventListener('click', function() {
            if (this.style.display === 'none') return;
            showCardByIndex(parseInt(this.dataset.index));
        });
    });

    document.addEventListener('keydown', function(e) {
        const visible = Array.from(cards).find(c => c.style.display === 'block');
        if (!visible) return;
        if (e.key === 'ArrowLeft') { e.preventDefault(); goRelative(-1); }
        if (e.key === 'ArrowRight') { e.preventDefault(); goRelative(1); }
    });

    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const term = this.value.trim();
            if (searchClear) searchClear.style.display = term ? '' : 'none';

            if (!term) {
                clearSearch();
                return;
            }

            isSearching = true;
            const lower = term.toLowerCase();
            let matchCount = 0;
            let firstMatch = -1;

            cards.forEach(function(card, i) {
                const text = card.getAttribute('data-search')?.toLowerCase() || '';
                const modOk = !activeModuleFilter || (card.dataset.module || '').toLowerCase() === activeModuleFilter.toLowerCase();
                const match = modOk && text.includes(lower);
                card.dataset.filtered = match ? 'true' : 'false';
                card.style.display = 'none';
                if (dots[i]) dots[i].style.display = match ? '' : 'none';
                if (match) {
                    matchCount++;
                    if (firstMatch === -1) firstMatch = i;
                }
            });

            if (searchStatus) {
                searchStatus.textContent = matchCount > 0
                    ? matchCount + ' soru bulundu'
                    : 'Sonuç bulunamadı';
            }
            if (totalBadge) totalBadge.textContent = matchCount + ' soru';

            if (firstMatch >= 0) showCardByIndex(firstMatch);
        });

        if (searchClear) {
            searchClear.addEventListener('click', function() {
                clearSearch();
                searchInput.focus();
            });
        }
    }

    // Initial - show first card
    if (cards.length > 0) {
        cards.forEach(c => c.style.display = 'none');
        cards[0].style.display = 'block';
    }
    updateCounterUI(cards.length, 1);
    updateDots(0);
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
                const expanded = panel.classList.toggle('hidden');
                panel.setAttribute('aria-hidden', expanded ? 'true' : 'false');
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
    let currentIndex = 0;

    const cards = document.querySelectorAll('.exam-q-card');
    const navDots = document.querySelectorAll('.exam-nav-dot');
    const submitArea = document.querySelector('.exam-submit-area');

    function showQuestion(index) {
        currentIndex = index;
        cards.forEach((card, i) => {
            card.style.display = i === index ? 'block' : 'none';
        });
        navDots.forEach((dot, i) => {
            dot.classList.toggle('active', i === index);
        });

        const counter = document.getElementById('currentQ');
        if (counter) counter.textContent = index + 1;

        const card = cards[index];
        if (card) {
            const prevBtn = card.querySelector('.prev-btn');
            const nextBtn = card.querySelector('.next-btn');
            const submitWrap = card.querySelector('.exam-submit-area');

            if (prevBtn) prevBtn.disabled = index === 0;
            if (nextBtn) nextBtn.style.display = index === totalQuestions - 1 ? 'none' : '';
            if (submitWrap) submitWrap.style.display = index === totalQuestions - 1 ? '' : 'none';
        }
    }

    function goTo(index) {
        if (index < 0 || index >= totalQuestions) return;
        showQuestion(index);
    }

    // Prev/next buttons
    document.querySelectorAll('.prev-btn').forEach(btn => {
        btn.addEventListener('click', () => goTo(currentIndex - 1));
    });
    document.querySelectorAll('.next-btn').forEach(btn => {
        btn.addEventListener('click', () => goTo(currentIndex + 1));
    });

    // Navigation dots
    navDots.forEach(dot => {
        dot.addEventListener('click', function() {
            const idx = parseInt(this.dataset.index);
            goTo(idx);
        });
    });

    // Keyboard navigation
    document.addEventListener('keydown', function(e) {
        if (!document.querySelector('.exam-q-card') || document.querySelector('.exam-q-card').style.display === 'none') {
            const visible = Array.from(document.querySelectorAll('.exam-q-card')).find(c => c.style.display !== 'none' || c.style.display === '');
            if (!visible) return;
        }
        if (e.key === 'ArrowLeft') goTo(currentIndex - 1);
        if (e.key === 'ArrowRight') goTo(currentIndex + 1);
    });

    function markAnswered(radio) {
        const qnum = radio.dataset.qnum;
        answered.add(qnum);

        const idx = parseInt(radio.dataset.index);
        if (!isNaN(idx)) {
            const card = cards[idx];
            if (card) card.classList.add('answered');
        }

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

    // Show first question
    showQuestion(0);
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
            const description = card.querySelector('.cm-desc-main') ? card.querySelector('.cm-desc-main').value.trim() : '';
            const topics = Array.from(card.querySelectorAll('.cm-topics .cm-topic-input'))
                .map(i => i.value.trim()).filter(v => v);
            const purpose = card.querySelector('.cm-purpose') ? card.querySelector('.cm-purpose').value.trim() : '';
            const durationRaw = card.querySelector('.cm-duration') ? card.querySelector('.cm-duration').value.trim() : '';
            const durationHours = durationRaw ? parseFloat(durationRaw) : null;
            const activities = Array.from(card.querySelectorAll('.cm-activities .cm-topic-input'))
                .map(i => i.value.trim()).filter(v => v);
            const outcomes = Array.from(card.querySelectorAll('.cm-outcomes .cm-topic-input'))
                .map(i => i.value.trim()).filter(v => v);
            if (title) modules.push({ title, description, topics, purpose, durationHours, activities, outcomes });
        });
        return modules;
    }

    function addListRow(container, placeholder, value) {
        const row = document.createElement('div');
        row.className = 'cm-topic-row';
        row.innerHTML = `<input class="cm-topic-input" type="text" placeholder="${escHtml(placeholder)}"><button type="button" class="cm-topic-remove" title="Sil"><i class="bi bi-x-lg"></i></button>`;
        row.querySelector('.cm-topic-remove').onclick = () => row.remove();
        if (value) row.querySelector('.cm-topic-input').value = value;
        container.appendChild(row);
        return row.querySelector('.cm-topic-input');
    }

    function addTopic(topicsDiv) {
        return addListRow(topicsDiv, 'Konu başlığı...');
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
                <div class="cm-field-group">
                    <label class="cm-field-label">Açıklama</label>
                    <textarea class="cm-desc cm-desc-main" rows="2" placeholder="Modül açıklaması (isteğe bağlı)...">${escHtml(data.description||'')}</textarea>
                </div>
                <div class="cm-field-group">
                    <label class="cm-field-label">Modül Amacı</label>
                    <textarea class="cm-desc cm-purpose" rows="2" placeholder="Bu modülün amacı...">${escHtml(data.purpose||'')}</textarea>
                </div>
                <div class="cm-field-group" style="display:flex;gap:1rem;align-items:flex-end;">
                    <div style="flex:1;">
                        <label class="cm-field-label">Süre (Saat)</label>
                        <input class="cm-topic-input cm-duration" type="number" step="0.5" min="0" placeholder="1.5" value="${escHtml(String(data.durationHours != null ? data.durationHours : ''))}" style="width:120px;">
                    </div>
                </div>
                <div class="cm-field-group">
                    <label class="cm-field-label">Konu Başlıkları</label>
                    <div class="cm-topics"></div>
                    <button type="button" class="cm-add-topic cm-add-list-btn"><i class="bi bi-plus-circle-fill"></i> Konu Ekle</button>
                </div>
                <div class="cm-field-group">
                    <label class="cm-field-label">Uygulama / Etkinlikler</label>
                    <div class="cm-activities cm-topics"></div>
                    <button type="button" class="cm-add-activity cm-add-list-btn"><i class="bi bi-plus-circle-fill"></i> Etkinlik Ekle</button>
                </div>
                <div class="cm-field-group">
                    <label class="cm-field-label">Modül Kazanımları</label>
                    <div class="cm-outcomes cm-topics"></div>
                    <button type="button" class="cm-add-outcome cm-add-list-btn"><i class="bi bi-plus-circle-fill"></i> Kazanım Ekle</button>
                </div>
            </div>
        `;
        card.querySelector('.cm-delete-module').onclick = () => { card.remove(); renumberModules(); };
        const topicsDiv = card.querySelector('.cm-topics');
        const activitiesDiv = card.querySelector('.cm-activities');
        const outcomesDiv = card.querySelector('.cm-outcomes');
        card.querySelector('.cm-add-topic').onclick = () => addListRow(topicsDiv, 'Konu başlığı...').focus();
        card.querySelector('.cm-add-activity').onclick = () => addListRow(activitiesDiv, 'Uygulama veya etkinlik...').focus();
        card.querySelector('.cm-add-outcome').onclick = () => addListRow(outcomesDiv, 'Modül kazanımı...').focus();
        (data.topics || []).forEach(t => addListRow(topicsDiv, 'Konu başlığı...', t));
        if (!data.topics || !data.topics.length) addListRow(topicsDiv, 'Konu başlığı...');
        (data.activities || []).forEach(a => addListRow(activitiesDiv, 'Uygulama veya etkinlik...', a));
        (data.outcomes || []).forEach(o => addListRow(outcomesDiv, 'Modül kazanımı...', o));
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
// UZEM Form — Dynamic List Builders
// ============================================
function initUzemForm() {
    const courseForm = document.getElementById('courseForm');
    if (!courseForm) return;

    // Check for UZEM hidden inputs
    const uzemFields = ['targetAudience', 'contentTopics', 'learningOutcomes', 'prerequisites'];
    const hasUzem = document.getElementById('targetAudienceJson');
    if (!hasUzem) return;

    function escStr(s) {
        return String(s || '').replace(/&/g,'&amp;').replace(/"/g,'&quot;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
    }

    // Generic string-list row adder
    window.addUzemItem = function(fieldId, placeholder, value) {
        const builder = document.getElementById(fieldId + '-builder');
        if (!builder) return;
        const row = document.createElement('div');
        row.className = 'uzem-item-row';
        row.innerHTML = '<input class="uzem-item-input" type="text" placeholder="' + escStr(placeholder) + '">' +
            '<button type="button" class="uzem-item-remove" title="Sil"><i class="bi bi-x-lg"></i></button>';
        if (value) row.querySelector('.uzem-item-input').value = value;
        row.querySelector('.uzem-item-remove').onclick = function() { row.remove(); };
        builder.appendChild(row);
    };

    // Assessment item row adder
    var assessBuilder = document.getElementById('assessmentItems-builder');
    window.addAssessmentItem = function(data) {
        data = data || {};
        var row = document.createElement('div');
        row.className = 'assess-row';
        row.innerHTML =
            '<input class="assess-input" type="text" placeholder="Quiz, Ödev..." value="' + escStr(data.type) + '">' +
            '<input class="assess-input" type="text" placeholder="Açıklama..." value="' + escStr(data.description) + '">' +
            '<input class="assess-input" type="number" min="0" max="100" placeholder="20" value="' + escStr(data.weight != null ? data.weight : '') + '">' +
            '<button type="button" class="uzem-item-remove" title="Sil"><i class="bi bi-x-lg"></i></button>';
        row.querySelector('.uzem-item-remove').onclick = function() { row.remove(); };
        if (assessBuilder) assessBuilder.appendChild(row);
    };

    // Serialize on form submit (runs before manualCurriculum serializer via capture)
    courseForm.addEventListener('submit', function() {
        uzemFields.forEach(function(id) {
            var jsonInput = document.getElementById(id + 'Json');
            if (!jsonInput) return;
            var items = Array.from(document.querySelectorAll('#' + id + '-builder .uzem-item-input'))
                .map(function(i) { return i.value.trim(); })
                .filter(function(v) { return v.length > 0; });
            jsonInput.value = JSON.stringify(items);
        });
        if (assessBuilder) {
            var aItems = Array.from(assessBuilder.querySelectorAll('.assess-row')).map(function(row) {
                var inputs = row.querySelectorAll('.assess-input');
                var wRaw = inputs[2].value.trim();
                return { type: inputs[0].value.trim(), description: inputs[1].value.trim(), weight: wRaw ? parseInt(wRaw, 10) : null };
            }).filter(function(a) { return a.type || a.description; });
            var aiJson = document.getElementById('assessmentItemsJson');
            if (aiJson) aiJson.value = JSON.stringify(aItems);
        }
    }, true);

    // Load existing data (edit form uses data-existing-* attributes on builder divs)
    uzemFields.forEach(function(id) {
        var builder = document.getElementById(id + '-builder');
        if (!builder) return;
        var existing = builder.dataset.existing;
        var placeholder = builder.dataset.placeholder || 'Girin...';
        if (existing && existing.trim().startsWith('[')) {
            try {
                var items = JSON.parse(existing);
                if (items.length > 0) {
                    items.forEach(function(v) { window.addUzemItem(id, placeholder, v); });
                    return;
                }
            } catch(e) {}
        }
        window.addUzemItem(id, placeholder);
    });

    if (assessBuilder) {
        var existingAI = assessBuilder.dataset.existing;
        if (existingAI && existingAI.trim().startsWith('[')) {
            try {
                var parsed = JSON.parse(existingAI);
                if (parsed.length > 0) {
                    parsed.forEach(function(a) { window.addAssessmentItem(a); });
                    return;
                }
            } catch(e) {}
        }
        window.addAssessmentItem();
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
                form.reset();
                showToast(data.message || 'Mesajınız başarıyla iletildi!', 'success');
            } else {
                showToast(data.message || 'Mesaj gönderilemedi.', 'error');
            }
        })
        .catch(error => {
            btn.innerHTML = originalText;
            btn.disabled = false;
            showToast('Bağlantı hatası! Lütfen daha sonra tekrar deneyin.', 'error');
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

// ============================================
// Course Detail — Course Info Toggle
// ============================================
function initCourseInfoToggle() {
    const toggleHeader = document.querySelector('[data-course-info-toggle]');
    if (!toggleHeader) return;

    toggleHeader.addEventListener('click', function() {
        const body = document.querySelector('[data-course-info-body]');
        const chevron = document.querySelector('[data-course-info-chevron]');

        if (body && body.style.display === 'none') {
            body.style.display = 'block';
            if (chevron) chevron.style.transform = 'rotate(180deg)';
            toggleHeader.style.borderRadius = '12px 12px 0 0';
        } else if (body) {
            body.style.display = 'none';
            if (chevron) chevron.style.transform = 'rotate(0deg)';
            toggleHeader.style.borderRadius = '12px';
        }
    });
}

// ============================================
// CSP-Safe Event Delegation (replaces inline handlers)
// ============================================
function initCspSafeHandlers() {
    // UZEM add-item buttons: data-uzem-field + data-uzem-placeholder
    document.querySelectorAll('[data-uzem-field]').forEach(btn => {
        btn.addEventListener('click', () => {
            const field = btn.dataset.uzemField;
            const placeholder = btn.dataset.uzemPlaceholder || '';
            if (window.addUzemItem) window.addUzemItem(field, placeholder);
        });
    });

    // Assessment item add button: data-action="addAssessmentItem"
    document.querySelectorAll('[data-action="addAssessmentItem"]').forEach(btn => {
        btn.addEventListener('click', () => {
            if (window.addAssessmentItem) window.addAssessmentItem();
        });
    });

    // Copy password button (instructors page)
    const copyPwdBtn = document.getElementById('copyPasswordBtn');
    if (copyPwdBtn) {
        copyPwdBtn.addEventListener('click', () => {
            const pwd = document.getElementById('teacher-pwd');
            if (pwd) {
                navigator.clipboard.writeText(pwd.textContent).then(() => {
                    copyPwdBtn.innerHTML = '<i class="bi bi-check-lg"></i>';
                    setTimeout(() => { copyPwdBtn.innerHTML = '<i class="bi bi-clipboard"></i>'; }, 2000);
                });
            }
        });
    }

    // Close credentials modal button
    const closeModalBtn = document.getElementById('closeCredentialsModalBtn');
    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', () => {
            const backdrop = document.getElementById('credentials-modal-backdrop');
            if (backdrop) backdrop.remove();
        });
    }

    // Go back button (course detail)
    const goBackBtn = document.getElementById('goBackBtn');
    if (goBackBtn) {
        goBackBtn.addEventListener('click', (e) => {
            e.preventDefault();
            history.back();
        });
    }

    // Prevent default on # links with role="button" (category filters)
    document.querySelectorAll('a[href="#"][role="button"]').forEach(link => {
        link.addEventListener('click', (e) => e.preventDefault());
    });
}

document.addEventListener('DOMContentLoaded', () => {
    initScrollAnimations();
    initAccordions();
    setActiveNavLink();
    initVideoLocking();
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
    initQuestionBank();
    initQuestionFormPreview();
    initZoomCopyLink();
    initDemoAlerts();
    initExcelPanelToggle();
    initCourseListReset();
    initCourseModuleToggle();
    initTheaterModeToggle();
    initQuizExam();
    initCurriculumBuilder();
    initUzemForm();
    initInstructorMultiSelect();
    initContactForm();
    initHomeCounters();
    initCourseInfoToggle();
    initCspSafeHandlers();
    initProfilePicturePreview();
    initPasswordChangeForm();
    initFlashBanners();
});

// ============================================
// Profile Picture Preview (egitmen/profilim)
// ============================================
function initProfilePicturePreview() {
    var input = document.getElementById('profilePicture');
    if (!input) return;
    input.addEventListener('change', function () {
        var file = this.files[0];
        if (!file) return;
        var reader = new FileReader();
        reader.onload = function (e) {
            var wrap = document.getElementById('avatar-preview');
            var existing = document.getElementById('avatar-img');
            var icon = document.getElementById('avatar-icon');
            if (icon) icon.style.display = 'none';
            if (existing) {
                existing.src = e.target.result;
            } else {
                var img = document.createElement('img');
                img.id = 'avatar-img';
                img.src = e.target.result;
                img.alt = 'Profil';
                wrap.appendChild(img);
            }
        };
        reader.readAsDataURL(file);
    });
}

// ============================================
// Password Change Form Validation (auth/sifre-degistir)
// ============================================
function initPasswordChangeForm() {
    var pwdInput = document.getElementById('newPassword');
    if (!pwdInput) return;
    var form = pwdInput.closest('form');
    if (!form) return;
    form.addEventListener('submit', function(e) {
        var pwd = document.getElementById('newPassword').value;
        var cpwd = document.getElementById('confirmPassword').value;
        var err = document.getElementById('client-error');
        var pattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
        if (!pattern.test(pwd)) {
            e.preventDefault();
            err.textContent = 'Şifre en az 8 karakter olmalı, büyük harf, küçük harf ve rakam içermelidir.';
            err.style.display = 'block';
            return;
        }
        if (pwd !== cpwd) {
            e.preventDefault();
            err.textContent = 'Şifreler eşleşmiyor.';
            err.style.display = 'block';
        }
    });
}


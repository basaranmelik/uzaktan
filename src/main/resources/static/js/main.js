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

document.addEventListener('DOMContentLoaded', () => {
    initVideoLocking();
    initGlobalToasts();
});

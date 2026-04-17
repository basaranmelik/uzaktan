(function () {
    // Veriyi HTML data attribute'larından oku (CSP unsafe-inline gerektirmez)
    var dataEl = document.getElementById('video-player-data');
    if (!dataEl) return;

    var videoId = parseInt(dataEl.dataset.videoId || '0', 10);
    var alreadyWatched = dataEl.dataset.alreadyWatched === 'true';

    // ── Plyr Init ──
    const videoElement = document.getElementById('player');
    const player = new Plyr('#player', {
        controls: ['play-large', 'play', 'progress', 'current-time', 'mute', 'volume', 'captions', 'settings', 'pip', 'airplay', 'fullscreen'],
        settings: ['quality', 'speed'],
        speed: { selected: 1, options: [0.5, 0.75, 1, 1.25, 1.5, 2] },
        keyboard: { focused: true, global: true },
        tooltips: { controls: true, seek: true }
    });

    // Move watermark inside Plyr container so it stays visible during fullscreen
    const watermark = document.querySelector('.video-watermark');
    const plyrContainer = document.querySelector('.plyr');
    if (watermark && plyrContainer) {
        plyrContainer.appendChild(watermark);
    }

    var marked = alreadyWatched;
    var markingInFlight = false;

    // CSRF Utility
    function getCsrf() {
        var meta = document.querySelector('meta[name="_csrf"]');
        var headerMeta = document.querySelector('meta[name="_csrf_header"]');
        var panel = document.querySelector('[data-csrf-token]');
        return {
            token: meta ? meta.getAttribute('content') : (panel ? panel.dataset.csrfToken : ''),
            header: headerMeta ? headerMeta.getAttribute('content') : (panel ? panel.dataset.csrfHeader : 'X-CSRF-TOKEN')
        };
    }

    function shouldMarkWatched(currentTime, duration) {
        return !marked && Number.isFinite(duration) && duration > 0 && currentTime / duration >= 0.8;
    }

    function checkWatchThreshold(currentTime, duration) {
        if (shouldMarkWatched(currentTime, duration)) {
            markAsWatched();
        }
    }

    // ── Watch Progress & Auto-Play ──
    player.on('timeupdate', () => {
        checkWatchThreshold(player.currentTime, player.duration);
    });

    player.on('ended', () => {
        if (marked) {
            handleAutoPlay();
        } else {
            markAsWatched().then(data => {
                if (data && data.success) {
                    handleAutoPlay();
                }
            });
        }
    });

    var thresholdPoller = window.setInterval(() => {
        if (marked || !videoElement) {
            if (marked) {
                window.clearInterval(thresholdPoller);
            }
            return;
        }
        checkWatchThreshold(videoElement.currentTime, videoElement.duration);
    }, 2000);

    function markAsWatched() {
        if (marked) return Promise.resolve({ success: true });
        if (markingInFlight) return Promise.resolve({ success: false });
        markingInFlight = true;

        const csrf = getCsrf();
        return fetch('/videolar/' + videoId + '/izlendi', {
            method: 'POST',
            headers: {
                [csrf.header]: csrf.token,
                'Content-Type': 'application/json'
            }
        })
            .then(r => {
                if (!r.ok) {
                    throw new Error('mark-watched-failed:' + r.status);
                }
                return r.json();
            })
            .then(data => {
                if (data.success) {
                    marked = true;
                    updateUIAsWatched();
                    return data;
                }
                throw new Error('mark-watched-unsuccessful');
            })
            .catch(() => {
                showToast('Video tamamlandı bilgisi kaydedilemedi. Lütfen tekrar deneyin.', 'warning');
                return { success: false };
            })
            .finally(() => {
                markingInFlight = false;
            });
    }

    function updateUIAsWatched() {
        var progress = document.getElementById('watch-progress');
        if (progress) {
            progress.className = 'complete-btn done';
            progress.style = '';
            progress.innerHTML = '<i class="bi bi-check-circle-fill"></i> Tamamlandı';
            progress.id = 'watch-status';
        }
        var playlistItem = document.querySelector('.playlist-item.active');
        if (playlistItem && !playlistItem.querySelector('.watch-check')) {
            var check = document.createElement('i');
            check.className = 'bi bi-check-circle-fill watch-check';
            playlistItem.appendChild(check);
        }
        var lockedBtn = document.getElementById('next-video-locked');
        if (lockedBtn) {
            var nextId = lockedBtn.getAttribute('data-id');
            var link = document.createElement('a');
            link.href = '/videolar/' + nextId;
            link.className = 'btn btn-primary';
            link.id = 'next-video-btn';
            link.innerHTML = 'Sonraki Video <i class="bi bi-arrow-right"></i>';
            lockedBtn.parentNode.replaceChild(link, lockedBtn);
        }
        var firstLocked = document.querySelector('.playlist-item.locked');
        if (firstLocked) {
            firstLocked.classList.remove('locked');
            var nextVideoId = firstLocked.getAttribute('data-video-id');
            firstLocked.href = '/videolar/' + nextVideoId;
            var lockIcon = firstLocked.querySelector('.lock-icon-overlay');
            if (lockIcon) lockIcon.remove();
        }

        // İlerleme çubuğunu güncelle
        var total = document.querySelectorAll('.playlist-item').length;
        var watched = document.querySelectorAll('.playlist-item .watch-check').length;
        if (total > 0) {
            var pct = Math.round(watched * 100 / total);
            var fill = document.querySelector('.izle-progress-fill');
            if (fill) fill.style.width = pct + '%';
            var label = document.querySelector('.izle-progress-info span:last-child');
            if (label) label.textContent = pct + '%';
        }
    }

    function handleAutoPlay() {
        const nextBtn = document.getElementById('next-video-btn');
        if (nextBtn) {
            showToast('Sonraki derse geçiliyor...', 'info');
            setTimeout(() => {
                window.location.href = nextBtn.href;
            }, 2000);
        }
    }

    // ── Authenticity Tracking ──
    var isPlaying = false;
    var playStart = null;
    var watchTimeDelta = 0;
    var hasSeeked = false;

    player.on('playing', () => {
        isPlaying = true;
        playStart = Date.now();
    });

    player.on('pause', () => {
        if (isPlaying && playStart) {
            watchTimeDelta += (Date.now() - playStart) / 1000;
        }
        isPlaying = false;
        playStart = null;
    });

    player.on('seeked', () => {
        hasSeeked = true;
        if (isPlaying && playStart) {
            watchTimeDelta += (Date.now() - playStart) / 1000;
            playStart = Date.now();
        }
    });

    function sendProgressPing() {
        var now = Date.now();
        var current = watchTimeDelta;
        if (isPlaying && playStart) {
            current += (now - playStart) / 1000;
        }
        var delta = Math.round(current);
        if (delta < 2) return;

        watchTimeDelta = 0;
        if (isPlaying) playStart = now;
        var seeked = hasSeeked;
        hasSeeked = false;

        var csrf = getCsrf();
        fetch('/videolar/' + videoId + '/progress', {
            method: 'POST',
            headers: { [csrf.header]: csrf.token, 'Content-Type': 'application/json' },
            body: JSON.stringify({
                watchTimeDelta: delta,
                currentPosition: Math.round(player.currentTime || 0),
                duration: Math.round(player.duration || 0),
                seeked: seeked
            })
        }).catch(() => {});
    }

    var progressPingTimer = setInterval(sendProgressPing, 15000);

    window.addEventListener('beforeunload', () => {
        sendProgressPing();
        clearInterval(progressPingTimer);
    });

    // ── Theater Mode ──
    window.toggleTheaterMode = function () {
        const layout = document.querySelector('.video-layout');
        const container = document.getElementById('player-container');
        layout.classList.toggle('theater');
        container.classList.toggle('theater');
        window.dispatchEvent(new Event('resize'));
    };

    // ── Additional Keybinds ──
    document.addEventListener('keydown', (e) => {
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;
        if (e.key.toLowerCase() === 't') {
            toggleTheaterMode();
        }
    });

})();

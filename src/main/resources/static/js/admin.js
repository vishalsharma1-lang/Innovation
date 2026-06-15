/**
 * CMS Admin Panel — admin.js
 */

// Auto-dismiss alerts after 5 seconds
document.addEventListener('DOMContentLoaded', function () {
    // Auto-dismiss alerts
    const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity .5s, transform .5s';
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-8px)';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    // Character counters
    document.querySelectorAll('.char-counter').forEach(counter => {
        const maxLen = parseInt(counter.getAttribute('data-max'));
        const countEl = counter.querySelector('.char-count');
        // Find the nearest preceding input/textarea
        const field = counter.previousElementSibling || counter.closest('.col-12').querySelector('input, textarea');
        if (field && countEl) {
            const update = () => {
                const len = field.value.length;
                countEl.textContent = len;
                counter.style.color = len > maxLen ? '#dc3545' : (len > maxLen * 0.85 ? '#fd7e14' : '#9ca3af');
            };
            field.addEventListener('input', update);
            update(); // init
        }
    });

    // Confirm delete buttons (extra safety)
    document.querySelectorAll('[data-confirm]').forEach(el => {
        el.addEventListener('click', function (e) {
            if (!confirm(this.getAttribute('data-confirm'))) {
                e.preventDefault();
            }
        });
    });
});

// Image preview before save (for file inputs)
function previewImage(input, previewId) {
    const file = input.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = function (e) {
        const preview = document.getElementById(previewId);
        if (preview) {
            preview.src = e.target.result;
            preview.style.display = 'block';
        }
    };
    reader.readAsDataURL(file);
}

// Toast notification
function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `alert alert-${type} position-fixed`;
    toast.style.cssText = 'bottom:24px;right:24px;z-index:9999;min-width:280px;border-radius:12px;box-shadow:0 8px 24px rgba(0,0,0,0.15)';
    toast.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'} me-2"></i>${message}`;
    document.body.appendChild(toast);
    setTimeout(() => {
        toast.style.transition = 'opacity .4s';
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 400);
    }, 3500);
}

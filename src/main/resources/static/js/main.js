// FraudGuard AI - Core Frontend Logic

document.addEventListener('DOMContentLoaded', () => {
    // 1. Theme Management (Dark/Light mode)
    const themeToggleBtn = document.getElementById('theme-toggle');
    const bodyElement = document.body;
    
    // Check local storage for preference
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'light') {
        bodyElement.classList.add('light-theme');
        if (themeToggleBtn) {
            themeToggleBtn.innerHTML = '<i class="fas fa-moon"></i>';
        }
    }

    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', () => {
            bodyElement.classList.toggle('light-theme');
            const isLight = bodyElement.classList.contains('light-theme');
            localStorage.setItem('theme', isLight ? 'light' : 'dark');
            themeToggleBtn.innerHTML = isLight ? '<i class="fas fa-moon"></i>' : '<i class="fas fa-sun"></i>';
        });
    }

    // 2. Real-Time Notification Bell Polling (every 10 seconds)
    const notificationBadge = document.getElementById('notification-badge');
    
    function updateUnreadNotificationCount() {
        if (!notificationBadge) return;
        
        fetch('/api/v1/notifications/unread-count')
            .then(response => {
                if (response.ok) return response.json();
                throw new Error('Network response not ok');
            })
            .then(count => {
                if (count > 0) {
                    notificationBadge.style.display = 'block';
                    notificationBadge.innerText = count > 9 ? '9+' : count;
                } else {
                    notificationBadge.style.display = 'none';
                }
            })
            .catch(error => {
                console.error('Error fetching unread notification count:', error);
            });
    }

    // Initial load and set interval
    if (notificationBadge) {
        updateUnreadNotificationCount();
        setInterval(updateUnreadNotificationCount, 10000);
    }
});

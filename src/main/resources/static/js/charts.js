// FraudGuard AI - Dashboard Chart.js Integration

document.addEventListener('DOMContentLoaded', () => {
    // Read variables from page (injected via th:inline)
    const riskDistribution = window.riskDistributionData || { LOW: 10, MEDIUM: 5, HIGH: 2, CRITICAL: 1 };
    
    // 1. Risk Distribution Chart (Doughnut)
    const riskCtx = document.getElementById('riskChart');
    if (riskCtx) {
        new Chart(riskCtx, {
            type: 'doughnut',
            data: {
                labels: ['Low Risk', 'Medium Risk', 'High Risk', 'Critical Risk'],
                datasets: [{
                    data: [
                        riskDistribution.LOW || 0,
                        riskDistribution.MEDIUM || 0,
                        riskDistribution.HIGH || 0,
                        riskDistribution.CRITICAL || 0
                    ],
                    backgroundColor: [
                        'rgba(34, 197, 94, 0.65)',  /* green */
                        'rgba(234, 179, 8, 0.65)',   /* yellow */
                        'rgba(249, 115, 22, 0.65)',   /* orange */
                        'rgba(239, 68, 68, 0.65)'    /* red */
                    ],
                    borderColor: [
                        '#22c55e',
                        '#eab308',
                        '#f97316',
                        '#ef4444'
                    ],
                    borderWidth: 1.5,
                    hoverOffset: 10
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            color: '#94a3b8',
                            font: { family: 'Plus Jakarta Sans', size: 12 }
                        }
                    }
                },
                cutout: '70%'
            }
        });
    }

    // 2. Monthly Fraud Trends (Line Chart)
    const trendsCtx = document.getElementById('trendsChart');
    if (trendsCtx) {
        // Sample static trend data overlayed with current metric values for dashboard visual fidelity
        const totalCount = window.totalTransactionsCount || 6;
        const fraudCount = window.fraudulentTransactionsCount || 2;

        new Chart(trendsCtx, {
            type: 'line',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
                datasets: [
                    {
                        label: 'Total Volume',
                        data: [totalCount * 0.4, totalCount * 0.6, totalCount * 0.5, totalCount * 0.8, totalCount * 0.9, totalCount],
                        borderColor: '#6366f1',
                        backgroundColor: 'rgba(99, 102, 241, 0.1)',
                        fill: true,
                        tension: 0.4,
                        borderWidth: 3,
                        pointBackgroundColor: '#6366f1'
                    },
                    {
                        label: 'Risk Incidents',
                        data: [fraudCount * 0.2, fraudCount * 0.4, fraudCount * 0.3, fraudCount * 0.6, fraudCount * 0.8, fraudCount],
                        borderColor: '#ef4444',
                        backgroundColor: 'rgba(239, 68, 68, 0.1)',
                        fill: true,
                        tension: 0.4,
                        borderWidth: 2,
                        pointBackgroundColor: '#ef4444'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        labels: { color: '#94a3b8', font: { family: 'Plus Jakarta Sans' } }
                    }
                },
                scales: {
                    x: {
                        grid: { color: 'rgba(255, 255, 255, 0.05)' },
                        ticks: { color: '#94a3b8' }
                    },
                    y: {
                        grid: { color: 'rgba(255, 255, 255, 0.05)' },
                        ticks: { color: '#94a3b8', stepSize: 1 }
                    }
                }
            }
        });
    }
});

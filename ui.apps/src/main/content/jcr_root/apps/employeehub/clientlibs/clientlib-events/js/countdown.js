(function () {
    'use strict';

    function decompose(target) {
        var now = new Date();
        if (isNaN(target.getTime()) || target <= now) {
            return { months: 0, weeks: 0, days: 0, hours: 0, minutes: 0, done: true };
        }
        // Count whole calendar months first, then break the remainder down.
        var months = 0;
        var cursor = new Date(now.getTime());
        while (true) {
            var next = new Date(cursor.getTime());
            next.setMonth(next.getMonth() + 1);
            if (next <= target) {
                cursor = next;
                months++;
            } else {
                break;
            }
        }
        var totalMinutes = Math.floor((target - cursor) / 60000);
        var days = Math.floor(totalMinutes / 1440);
        totalMinutes -= days * 1440;
        var weeks = Math.floor(days / 7);
        days -= weeks * 7;
        var hours = Math.floor(totalMinutes / 60);
        var minutes = totalMinutes - hours * 60;
        return { months: months, weeks: weeks, days: days, hours: hours, minutes: minutes, done: false };
    }

    function render(timer) {
        var target = new Date(timer.getAttribute('data-target'));
        var parts = decompose(target);
        Object.keys(parts).forEach(function (unit) {
            var el = timer.querySelector('[data-unit="' + unit + '"]');
            if (el) {
                el.textContent = parts[unit];
            }
        });
    }

    function init() {
        var timers = document.querySelectorAll('.eh-countdown__timer[data-target]');
        if (!timers.length) {
            return;
        }
        timers.forEach(render);
        setInterval(function () {
            timers.forEach(render);
        }, 1000);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();

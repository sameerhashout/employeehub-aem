(function () {
    'use strict';

    var ENDPOINT = '/bin/employeehub/poll';

    function renderResults(widget, data) {
        var total = data.total || 0;
        var results = data.results || {};
        widget.querySelectorAll('.eh-poll__option').forEach(function (btn) {
            var option = btn.getAttribute('data-option');
            var key = option.trim().toLowerCase().replace(/[^a-z0-9_-]/g, '-');
            var count = results[key] || 0;
            var pct = total > 0 ? Math.round((count / total) * 100) : 0;
            var fill = btn.querySelector('.eh-poll__bar-fill');
            var countEl = btn.querySelector('.eh-poll__count');
            if (fill) { fill.style.width = pct + '%'; }
            if (countEl) { countEl.textContent = count + ' (' + pct + '%)'; }
        });
        var totalEl = widget.querySelector('.eh-poll__total');
        if (totalEl) {
            totalEl.textContent = total + ' vote' + (total === 1 ? '' : 's') + ' so far';
        }
    }

    function load(widget) {
        var pollId = widget.getAttribute('data-poll-id');
        fetch(ENDPOINT + '?pollId=' + encodeURIComponent(pollId))
            .then(function (res) { return res.json(); })
            .then(function (data) { renderResults(widget, data); })
            .catch(function () { /* silent */ });
    }

    function vote(widget, option) {
        var pollId = widget.getAttribute('data-poll-id');
        var body = 'pollId=' + encodeURIComponent(pollId) + '&option=' + encodeURIComponent(option);
        fetch(ENDPOINT, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: body
        })
            .then(function (res) { return res.json(); })
            .then(function (data) { renderResults(widget, data); })
            .catch(function () { /* silent */ });
    }

    function init() {
        document.querySelectorAll('.eh-poll').forEach(function (widget) {
            widget.querySelectorAll('.eh-poll__option').forEach(function (btn) {
                btn.addEventListener('click', function () {
                    vote(widget, btn.getAttribute('data-option'));
                });
            });
            load(widget);
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();

(function() {
    'use strict';

    document.addEventListener('DOMContentLoaded', function() {
        var searchForms = document.querySelectorAll('.eh-search');
        searchForms.forEach(function(form) {
            var input = form.querySelector('.eh-search__input');
            var button = form.querySelector('.eh-search__btn');
            var resultsContainer = form.parentElement.querySelector('.eh-search__results');
            var endpoint = form.getAttribute('data-endpoint') || '/bin/employeehub/employee-search.json';
            var maxResults = form.getAttribute('data-max-results') || '20';

            if (!button || !input) return;

            button.addEventListener('click', function(e) {
                e.preventDefault();
                performSearch();
            });

            input.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    performSearch();
                }
            });

            function performSearch() {
                var query = input.value.trim();
                if (!query) return;

                resultsContainer.innerHTML = '<p class="eh-search__loading">Searching…</p>';
                var url = endpoint + '?q=' + encodeURIComponent(query) + '&limit=' + maxResults;
                fetch(url)
                    .then(function(response) { return response.json(); })
                    .then(function(data) { renderResults(data); })
                    .catch(function() {
                        resultsContainer.innerHTML = '<p class="eh-search__no-results">Search failed. Please try again.</p>';
                    });
            }

            function getInitials(name) {
                if (!name) return '?';
                var parts = name.trim().split(/\s+/);
                if (parts.length >= 2) {
                    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
                }
                return name[0].toUpperCase();
            }

            function avatarClass(dept) {
                if (!dept) return 'eh-avatar';
                var d = dept.toLowerCase();
                if (d.indexOf('human') >= 0 || d.indexOf('hr') >= 0) return 'eh-avatar eh-avatar--hr';
                if (d.indexOf('finance') >= 0) return 'eh-avatar eh-avatar--finance';
                return 'eh-avatar eh-avatar--it';
            }

            function renderResults(data) {
                if (!resultsContainer) return;
                resultsContainer.innerHTML = '';

                if (!data.success || !data.employees || data.employees.length === 0) {
                    resultsContainer.innerHTML = '<p class="eh-search__no-results">No employees found matching your search.</p>';
                    return;
                }

                var count = document.createElement('p');
                count.className = 'eh-search__count';
                count.textContent = data.employees.length + ' result' + (data.employees.length > 1 ? 's' : '') + ' found';
                resultsContainer.appendChild(count);

                data.employees.forEach(function(emp) {
                    var item = document.createElement('div');
                    item.className = 'eh-search__result-item';
                    var tags = (emp.tags || []).map(function(t) {
                        return '<span class="eh-tag">' + t + '</span>';
                    }).join('');
                    item.innerHTML =
                        '<div class="eh-search__result-inner">' +
                            '<div class="' + avatarClass(emp.department) + '">' + getInitials(emp.employeeName) + '</div>' +
                            '<div class="eh-search__result-body">' +
                                '<strong>' + emp.employeeName + '</strong>' +
                                '<span>' + emp.department + '</span>' +
                                '<small>' + emp.email + '</small>' +
                                (tags ? '<div class="eh-search__result-tags">' + tags + '</div>' : '') +
                            '</div>' +
                        '</div>';
                    resultsContainer.appendChild(item);
                });
            }
        });

        /* Highlight active nav link */
        var path = window.location.pathname;
        document.querySelectorAll('.eh-header__nav a').forEach(function(link) {
            if (path.indexOf(link.getAttribute('href').replace('.html', '')) >= 0) {
                link.classList.add('eh-nav--active');
            }
        });
    });
})();

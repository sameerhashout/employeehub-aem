(function () {
    'use strict';

    var ENDPOINT = '/bin/employeehub/weather';

    function fetchWeather(widget) {
        var input = widget.querySelector('.eh-weather__input');
        var result = widget.querySelector('.eh-weather__result');
        var city = input ? input.value.trim() : '';
        result.textContent = 'Loading...';

        fetch(ENDPOINT + '?city=' + encodeURIComponent(city), {
            headers: { 'Accept': 'application/json' }
        })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (!data.success) {
                    result.innerHTML = '<span class="eh-weather__error">' +
                        (data.message || 'Unable to fetch weather') + '</span>';
                    return;
                }
                result.innerHTML =
                    '<div class="eh-weather__city">' + data.city + '</div>' +
                    '<div class="eh-weather__temp">' + Math.round(data.temperature) + '&deg;C</div>' +
                    '<div class="eh-weather__desc">' + data.description + '</div>' +
                    '<div class="eh-weather__wind">Wind: ' + data.windspeed + ' km/h</div>';
            })
            .catch(function () {
                result.innerHTML = '<span class="eh-weather__error">Network error. Try again.</span>';
            });
    }

    function init() {
        document.querySelectorAll('.eh-weather').forEach(function (widget) {
            var btn = widget.querySelector('.eh-weather__btn');
            if (btn) {
                btn.addEventListener('click', function () { fetchWeather(widget); });
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();

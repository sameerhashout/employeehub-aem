(function () {
    'use strict';

    function setupCarousel(carousel) {
        var track = carousel.querySelector('.eh-events__track');
        var slides = carousel.querySelectorAll('.eh-events__slide');
        if (!track || slides.length === 0) {
            return;
        }
        var index = 0;

        function show(i) {
            index = (i + slides.length) % slides.length;
            slides.forEach(function (slide, slideIndex) {
                slide.classList.toggle('eh-events__slide--active', slideIndex === index);
            });
        }

        var prev = carousel.querySelector('.eh-events__nav--prev');
        var next = carousel.querySelector('.eh-events__nav--next');
        if (prev) {
            prev.addEventListener('click', function () { show(index - 1); });
        }
        if (next) {
            next.addEventListener('click', function () { show(index + 1); });
        }

        var timer;
        function startRotation() {
            timer = setInterval(function () { show(index + 1); }, 6000);
        }

        carousel.addEventListener('mouseenter', function () { clearInterval(timer); });
        carousel.addEventListener('mouseleave', function () { startRotation(); });

        show(0);
        startRotation();
    }

    function init() {
        document.querySelectorAll('.eh-events__carousel').forEach(setupCarousel);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();

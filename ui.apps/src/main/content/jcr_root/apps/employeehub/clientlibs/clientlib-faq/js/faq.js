(function() {
    'use strict';

    document.addEventListener('DOMContentLoaded', function() {
        var faqItems = document.querySelectorAll('.eh-faq__question');
        faqItems.forEach(function(question) {
            question.addEventListener('click', function() {
                var item = question.parentElement;
                var isActive = item.classList.contains('active');
                document.querySelectorAll('.eh-faq__item.active').forEach(function(el) {
                    el.classList.remove('active');
                });
                if (!isActive) {
                    item.classList.add('active');
                }
            });
        });
    });
})();

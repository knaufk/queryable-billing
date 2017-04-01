/**
 * Implements the "Charred Trail" pattern for fragments, see the book
 * "Presentation Patterns" by Neal Ford, Matthew McCullough, and Nate Schutta
 * (http://presentationpatterns.com/).
 *
 * See README.md for more information.
 *
 * MIT Licensed
 * See LICENSE for more information.
 */
(function () {
  function toArray(list) {
    if (!list) return [];
    return Array.prototype.slice.call(list);
  }

  function getIndex(fragment) {
    return parseInt(fragment.getAttribute('data-fragment-index'));
  }

  function focus(fragment) {
    var fragments = Reveal.getCurrentSlide().querySelectorAll('.fragment');
    toArray(fragments).forEach(function(fragment) {
      fragment.classList.remove('focus');
    });
    do {
      if (fragment.classList.contains('fragment')) {
        fragment.classList.add('focus');
      }
    } while(fragment = fragment.parentElement);
  }

  function fragmentShown(e) {
    focus(e.fragment);
  }

  function fragmentHidden(e) {
    var currentFragment = e.fragment;
    var currentIndex = getIndex(currentFragment);
    var fragments = Reveal.getCurrentSlide().querySelectorAll('.fragment');
    toArray(fragments).forEach(function(fragment) {
      if (getIndex(fragment) === currentIndex - 1) {
        focus(fragment);
      }
    });
  }

  Reveal.addEventListener('fragmentshown',  fragmentShown);
  Reveal.addEventListener('fragmenthidden', fragmentHidden);
}());


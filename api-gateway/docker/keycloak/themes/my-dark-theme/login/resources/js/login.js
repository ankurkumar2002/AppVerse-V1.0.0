(function() {
  const urlParams = new URLSearchParams(window.location.search);
  const mode = urlParams.get('mode');

  if (mode === 'developer') {
    document.body.classList.add('developer-theme');
  } else if (mode === 'user') {
    document.body.classList.add('user-theme');
  }
})();

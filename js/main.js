/* =========================================================
   ARSHAD — Backend Engineer
   Motion + interaction layer
   ========================================================= */

(function () {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  /* ---------- Lenis smooth scroll ---------- */
  let lenis = null;
  if (window.Lenis && !reduceMotion) {
    lenis = new Lenis({
      duration: 1.1,
      easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
      smoothWheel: true,
      wheelMultiplier: 1,
      touchMultiplier: 1.6,
    });
  }

  /* ---------- GSAP setup ---------- */
  if (window.gsap && window.ScrollTrigger) {
    gsap.registerPlugin(ScrollTrigger);

    if (lenis) {
      lenis.on('scroll', ScrollTrigger.update);
      gsap.ticker.add((time) => { lenis.raf(time * 1000); });
      gsap.ticker.lagSmoothing(0);
    }
  }

  /* ---------- Nav scroll state ---------- */
  const nav = document.getElementById('nav');
  const progress = document.getElementById('nav-progress');

  function onScroll() {
    if (window.scrollY > 24) nav.setAttribute('data-scroll', '');
    else nav.removeAttribute('data-scroll');

    const h = document.documentElement.scrollHeight - window.innerHeight;
    const p = h > 0 ? window.scrollY / h : 0;
    progress.style.transform = `scaleX(${p})`;
  }
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();

  /* ---------- Mobile menu ---------- */
  const toggle = document.getElementById('nav-toggle');
  const menu = document.getElementById('menu');
  let menuOpen = false;

  function setMenu(open) {
    menuOpen = open;
    toggle.setAttribute('aria-expanded', String(open));
    toggle.setAttribute('aria-label', open ? 'Close menu' : 'Open menu');
    menu.classList.toggle('is-open', open);
    menu.setAttribute('aria-hidden', String(!open));
    document.body.style.overflow = open ? 'hidden' : '';
  }
  toggle.addEventListener('click', () => setMenu(!menuOpen));
  menu.querySelectorAll('a').forEach((a) => a.addEventListener('click', () => setMenu(false)));
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && menuOpen) setMenu(false); });

  /* ---------- Smooth anchor scrolling (Lenis) ---------- */
  document.querySelectorAll('a[href^="#"]').forEach((a) => {
    a.addEventListener('click', (e) => {
      const id = a.getAttribute('href');
      if (id.length < 2) return;
      const target = document.querySelector(id);
      if (!target) return;
      e.preventDefault();
      if (lenis) lenis.scrollTo(target, { offset: -10, duration: 1.2 });
      else target.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth' });
    });
  });

  /* ---------- Hero entrance timeline ---------- */
  const heroEls = ['.hero__title', '.hero__eyebrow', '.hero__sub', '.hero__cta', '.hero__bento', '.hero__marquee'];

  if (!reduceMotion && window.gsap) {
    gsap.set(heroEls, { opacity: 0, y: 24 });
    const tl = gsap.timeline({ defaults: { ease: 'power3.out', duration: 0.9 } });
    tl.to('.hero__eyebrow', { opacity: 1, y: 0 }, 0)
      .to('.hero__title', { opacity: 1, y: 0, duration: 1.1 }, 0.05)
      .to('.hero__sub', { opacity: 1, y: 0 }, 0.25)
      .to('.hero__cta', { opacity: 1, y: 0 }, 0.4)
      .to('.hero__bento', { opacity: 1, y: 0, duration: 1 }, 0.3)
      .to('.hero__marquee', { opacity: 1, y: 0 }, 0.55);
  }

  /* ---------- Scroll reveal (generic) ---------- */
  if (!reduceMotion && window.gsap && window.ScrollTrigger) {
    ScrollTrigger.batch('[data-reveal]', {
      start: 'top 88%',
      onEnter: (els) => {
        gsap.to(els, {
          opacity: 1,
          y: 0,
          duration: 0.8,
          ease: 'power2.out',
          stagger: 0.08,
          overwrite: true,
        });
      },
      once: true,
    });
  } else {
    // no GSAP / reduced motion fallback — show everything
    document.querySelectorAll('[data-reveal]').forEach((el) => el.classList.add('is-in'));
  }

  /* ---------- Active section indicator ---------- */
  const sections = Array.from(document.querySelectorAll('[data-section]'));
  const navLinks = Array.from(document.querySelectorAll('.nav__links a'));
  const idToLink = {};
  navLinks.forEach((a) => { idToLink[a.getAttribute('href').slice(1)] = a; });

  const io = new IntersectionObserver(
    (entries) => {
      entries.forEach((en) => {
        if (en.isIntersecting) {
          const id = en.target.id;
          navLinks.forEach((a) => a.classList.remove('is-active'));
          if (idToLink[id]) idToLink[id].classList.add('is-active');
        }
      });
    },
    { rootMargin: '-45% 0px -50% 0px', threshold: 0 }
  );
  sections.forEach((s) => io.observe(s));

  /* ---------- Project visual hover motion ---------- */
  document.querySelectorAll('.proj__visual').forEach((el) => {
    const inner = el.querySelector('.mock');
    if (!inner || reduceMotion) return;
    el.addEventListener('mousemove', (e) => {
      const r = el.getBoundingClientRect();
      const x = (e.clientX - r.left) / r.width - 0.5;
      const y = (e.clientY - r.top) / r.height - 0.5;
      inner.style.transform = `translate(${x * 6}px, ${y * 6}px)`;
    });
    el.addEventListener('mouseleave', () => { inner.style.transform = ''; });
  });

  /* ---------- Loaded flag ---------- */
  document.documentElement.classList.add('is-ready');
})();

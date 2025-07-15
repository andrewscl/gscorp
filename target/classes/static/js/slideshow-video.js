export function setupSlideshow () {
    console.log("setupSlideshow activado");

    const slides = document.querySelectorAll('.slide');
    const prevButton = document.querySelector('.nav.prev');
    const nextButton = document.querySelector('.nav.next');
    const dots = document.querySelectorAll('.dot');
    let currentIndex = 0;
    let intervalId;

    function resetAnimations(slide) {
        const texts = slide.querySelectorAll('.slide-texts h1, .slide-texts h2, .slide-texts h3');

        texts.forEach(text => {
            // ⚠️ Clona el nodo para reiniciar completamente la animación
            const clone = text.cloneNode(true);
            text.parentNode.replaceChild(clone, text);
        });
    }

    function showSlide(index) {
        slides.forEach((slide, i) => {
            slide.classList.remove('active');
            dots[i].classList.remove('active');
        });

        const activeSlide = slides[index];
        activeSlide.classList.add('active');
        dots[index].classList.add('active');
        currentIndex = index;

        resetAnimations(activeSlide);
    }

    function nextSlide() {
        currentIndex = (currentIndex + 1) % slides.length;
        showSlide(currentIndex);
    }

    function prevSlide() {
        currentIndex = (currentIndex - 1 + slides.length) % slides.length;
        showSlide(currentIndex);
    }

    function goToSlide(index) {
        showSlide(index);
        restartInterval();
    }

    function startInterval() {
        intervalId = setInterval(nextSlide, 6000);
    }

    function restartInterval() {
        clearInterval(intervalId);
        startInterval();
    }

    nextButton?.addEventListener('click', () => {
        nextSlide();
        restartInterval();
    });

    prevButton?.addEventListener('click', () => {
        prevSlide();
        restartInterval();
    });

    dots.forEach((dot, index) => {
        dot.addEventListener('click', () => goToSlide(index));
    });

    showSlide(currentIndex);
    startInterval();

    window.setupSlideshow = setupSlideshow;

}

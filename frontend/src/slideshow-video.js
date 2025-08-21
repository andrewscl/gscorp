export function setupSlideshow () {
    console.log("setupSlideshow activado");

    const slides = document.querySelectorAll('.slide');
    const prevButton = document.querySelector('.nav.prev');
    const nextButton = document.querySelector('.nav.next');
    const dots = document.querySelectorAll('.dot');
    let currentIndex = 0;

    //Temporizador programable
    let timerId = null;
    function scheduleNext(ms = 6000){
        clearTimeout(timerId);
        timerId = setTimeout(nextSlide, ms);
    }

    //helper para controlar los videos
    function controlVideo(slide, action){
        const v = slide.querySelector('video');
        if (!v) return;

        if (action === 'reset'){
            try { v.pause(); } catch{}
            //Si aun no cargo metadata, esperamos para resetear el currentTime
            const resetTime = () => { try { v.currentTime = 0; } catch {} };
            if(v.readyState >= 1) resetTime();
            else v.addEventListener('loadedmetadata', resetTime, { once:true });
            return;
        }

        if (action === 'start') {
            const startPlayback = () => {
                try { v.pause(); } catch {}
                try { v.currentTime = 0; } catch {}
                const p = v.play();
                //Si el navegador bloquea autoplay, de todas formas programamos el siguiente
                if (p && typeof p.catch === 'function'){
                    p.catch(() => scheduleNext(6000));
                }
            };
            //Programa el siguiente cambio cuendo realmente comienza a reproducir
            const onPlaying = () => {
                v.removeEventListener('playing', onPlaying);
                const dwell = dwellFor(v, 6000);
                scheduleNext(dwell);
            };

            v.addEventListener('playing', onPlaying, { once:true });

            if(v.readyState >= 2) startPlayback();
            else v.addEventListener('canplay', startPlayback, { once:true });
        }
    }

    function resetAnimations(slide) {
        const texts = slide.querySelectorAll('.slide-texts h1, .slide-texts h2, .slide-texts h3');

        texts.forEach(text => {
            // ⚠️ Clona el nodo para reiniciar completamente la animación
            const clone = text.cloneNode(true);
            text.parentNode.replaceChild(clone, text);
        });
    }

    function showSlide(index) {
        clearTimeout(timerId);

        slides.forEach((slide, i) => {
            slide.classList.toggle('active', i === index);
            dots[i]?.classList.toggle('active', i === index);
            //Reset a los no activos
            if (i !== index) controlVideo(slide, 'reset');
        });

        const activeSlide = slides[index];
        currentIndex = index;
        resetAnimations(activeSlide);

        //Arranca el video del slide activo y agenda el proximo cambio cuando empiece
        controlVideo(activeSlide, 'start');
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
    }

    //Actualiza handlers
    nextButton?.addEventListener('click', () => {
        nextSlide();
    });

    prevButton?.addEventListener('click', () => {
        prevSlide();
    });

    dots.forEach((dot, index) => {
        dot.addEventListener('click', () => goToSlide(index));
    });

    //Determinar la duración del video
    function dwellFor(video, fallbackMs = 6000) {
    let ms = Number.isFinite(video.duration) && video.duration > 0
        ? video.duration * 1000
        : fallbackMs;
    // Margen de seguridad: evita que el pase caiga justo en el frame final
    // (útil con 25/30 fps donde un clip puede ser 6.04 s)
    ms = ms - 60; // ~2–3 frames
    return Math.max(100, ms);
    }

    showSlide(currentIndex);

    window.setupSlideshow = setupSlideshow;



}

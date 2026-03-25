(() => {
    const canvas = document.getElementById("starfield");
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    const stars = [];
    const STAR_COUNT = 340;

    function resize() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    }

    function random(min, max) {
        return Math.random() * (max - min) + min;
    }

    function createStars() {
        stars.length = 0;
        for (let i = 0; i < STAR_COUNT; i += 1) {
            stars.push({
                x: Math.random() * canvas.width,
                y: Math.random() * canvas.height,
                radius: random(0.35, 1.7),
                alpha: random(0.28, 0.95),
                velocity: random(0.01, 0.08),
                drift: random(-0.03, 0.03)
            });
        }
    }

    function draw() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        for (const s of stars) {
            s.y += s.velocity;
            s.x += s.drift;
            if (s.y > canvas.height) s.y = -2;
            if (s.x > canvas.width) s.x = 0;
            if (s.x < 0) s.x = canvas.width;

            const twinkle = 0.15 * Math.sin(Date.now() * 0.0015 + s.x * 0.01);
            const alpha = Math.max(0.08, Math.min(1, s.alpha + twinkle));

            ctx.beginPath();
            ctx.fillStyle = `rgba(235, 221, 255, ${alpha})`;
            ctx.shadowColor = "rgba(186, 128, 255, 0.75)";
            ctx.shadowBlur = 8;
            ctx.arc(s.x, s.y, s.radius, 0, Math.PI * 2);
            ctx.fill();
        }
        requestAnimationFrame(draw);
    }

    resize();
    createStars();
    draw();
    window.addEventListener("resize", () => {
        resize();
        createStars();
    });
})();

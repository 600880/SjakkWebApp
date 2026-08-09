import { state } from './state.js';

export function triggerFireworks() {
    if (!state.specialEffectsEnabled) return;
    const canvas = createOverlayCanvas();
    const ctx = canvas.getContext('2d');
    let particles = [];

    class Particle {
        constructor(x, y, color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.velocity = {
                x: (Math.random() - 0.5) * 8,
                y: (Math.random() - 0.5) * 8
            };
            this.alpha = 1;
            this.friction = 0.95;
            this.gravity = 0.2;
        }

        draw() {
            ctx.save();
            ctx.globalAlpha = this.alpha;
            ctx.beginPath();
            ctx.arc(this.x, this.y, 2, 0, Math.PI * 2, false);
            ctx.fillStyle = this.color;
            ctx.fill();
            ctx.restore();
        }

        update() {
            this.velocity.x *= this.friction;
            this.velocity.y *= this.friction;
            this.velocity.y += this.gravity;
            this.x += this.velocity.x;
            this.y += this.velocity.y;
            this.alpha -= 0.01;
        }
    }

    function createFirework(x, y) {
        const colors = ['#ff0000', '#ffa500', '#ffff00', '#00ff00', '#0000ff', '#4b0082', '#ee82ee'];
        const color = colors[Math.floor(Math.random() * colors.length)];
        for (let i = 0; i < 40; i++) {
            particles.push(new Particle(x, y, color));
        }
    }

    let frame = 0;
    function animate() {
        if (frame > 300 && particles.length === 0) {
            canvas.remove();
            return;
        }
        requestAnimationFrame(animate);
        ctx.fillStyle = 'rgba(0, 0, 0, 0.1)';
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        if (frame % 30 === 0 && frame < 200) {
            createFirework(Math.random() * canvas.width, Math.random() * canvas.height * 0.5);
        }

        particles.forEach((particle, index) => {
            if (particle.alpha <= 0) {
                particles.splice(index, 1);
            } else {
                particle.update();
                particle.draw();
            }
        });
        frame++;
    }

    animate();
}

export function triggerTomatoes() {
    if (!state.specialEffectsEnabled) return;
    const canvas = createOverlayCanvas();
    const ctx = canvas.getContext('2d');
    let tomatoes = [];

    class Tomato {
        constructor() {
            this.x = Math.random() * canvas.width;
            this.y = -50;
            this.size = 30 + Math.random() * 20;
            this.speed = 5 + Math.random() * 5;
            this.splatted = false;
            this.splatTimer = 0;
        }

        draw() {
            if (this.splatted) {
                ctx.fillStyle = 'rgba(255, 0, 0, 0.6)';
                ctx.beginPath();
                ctx.arc(this.x, this.y, this.size * 1.2, 0, Math.PI * 2);
                ctx.fill();
                // Add some smaller splats around
                for (let i = 0; i < 5; i++) {
                    ctx.beginPath();
                    ctx.arc(this.x + (Math.random()-0.5)*40, this.y + (Math.random()-0.5)*40, this.size * 0.3, 0, Math.PI * 2);
                    ctx.fill();
                }
            } else {
                ctx.font = `${this.size}px serif`;
                ctx.textAlign = 'center';
                ctx.fillText('🍅', this.x, this.y);
            }
        }

        update() {
            if (!this.splatted) {
                this.y += this.speed;
                if (this.y > canvas.height * 0.7 + Math.random() * 100) {
                    this.splatted = true;
                }
            } else {
                this.splatTimer++;
            }
        }
    }

    let frame = 0;
    function animate() {
        if (frame > 300 && tomatoes.length === 0) {
            canvas.remove();
            return;
        }
        requestAnimationFrame(animate);
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        if (frame % 20 === 0 && frame < 150) {
            tomatoes.push(new Tomato());
        }

        tomatoes.forEach((tomato, index) => {
            if (tomato.splatTimer > 100) {
                tomatoes.splice(index, 1);
            } else {
                tomato.update();
                tomato.draw();
            }
        });
        frame++;
    }

    animate();
}

function createOverlayCanvas() {
    const canvas = document.createElement('canvas');
    canvas.style.position = 'fixed';
    canvas.style.top = '0';
    canvas.style.left = '0';
    canvas.style.width = '100vw';
    canvas.style.height = '100vh';
    canvas.style.pointerEvents = 'none';
    canvas.style.zIndex = '10000';
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    document.body.appendChild(canvas);
    return canvas;
}

export function playCheckAlarm() {
    if (!state.specialEffectsEnabled) return;
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    
    const audioCtx = new AudioContext();
    
    // Play a smooth, warm warning chime instead of a harsh alarm
    function playNote(freq, startTime, duration = 0.18, volume = 0.08) {
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();

        osc.type = 'triangle';
        osc.frequency.setValueAtTime(freq, startTime);

        // Smooth attack and quick decay
        gain.gain.setValueAtTime(0, startTime);
        gain.gain.linearRampToValueAtTime(volume, startTime + 0.02);
        gain.gain.exponentialRampToValueAtTime(0.001, startTime + duration);

        osc.connect(gain);
        gain.connect(audioCtx.destination);

        osc.start(startTime);
        osc.stop(startTime + duration);
    }

    const now = audioCtx.currentTime;
    // Play two brief, subtle alert tones (E5 -> B4)
    playNote(659.25, now, 0.18, 0.07);
    playNote(493.88, now + 0.12, 0.22, 0.06);
}

export function playMoveSound(isCapture = false) {
    if (!state.specialEffectsEnabled) return;
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    
    const audioCtx = new AudioContext();
    const now = audioCtx.currentTime;

    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();

    if (isCapture) {
        // Slightly punchier wooden click for piece captures
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(320, now);
        osc.frequency.exponentialRampToValueAtTime(80, now + 0.05);

        gain.gain.setValueAtTime(0.12, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.05);
    } else {
        // Soft, subtle wooden thud for standard piece moves
        osc.type = 'sine';
        osc.frequency.setValueAtTime(240, now);
        osc.frequency.exponentialRampToValueAtTime(60, now + 0.04);

        gain.gain.setValueAtTime(0.09, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.04);
    }

    osc.connect(gain);
    gain.connect(audioCtx.destination);

    osc.start(now);
    osc.stop(now + 0.06);
}

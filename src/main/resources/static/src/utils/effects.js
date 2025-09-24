export function initializeEasterEgg() {
    const konamiCode = ['ArrowUp', 'ArrowUp', 'ArrowDown', 'ArrowDown', 'ArrowLeft', 'ArrowRight'];
    let sequence = [];
    let mouseX = window.innerWidth / 2;
    let mouseY = window.innerHeight / 2;

    document.addEventListener('mousemove', (e) => {
        mouseX = e.clientX;
        mouseY = e.clientY;
    });

    const triggerConfetti = (originX, originY) => {
        const confettiCount = 250;
        const colors = ['#f44336', '#e91e63', '#9c27b0', '#673ab7', '#3f51b5', '#2196f3', '#03a9f4', '#009688', '#4caf50', '#ffeb3b', '#ff9800'];

        for (let i = 0; i < confettiCount; i++) {
            const confetti = document.createElement('div');
            confetti.style.position = 'fixed';
            confetti.style.left = `${originX}px`;
            confetti.style.top = `${originY}px`;
            confetti.style.width = `${Math.random() * 12 + 6}px`;
            confetti.style.height = `${Math.random() * 12 + 6}px`;
            confetti.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
            confetti.style.opacity = '1';
            confetti.style.zIndex = '1000';
            confetti.style.transition = 'transform 1.5s ease-out, opacity 1.5s ease-out';
            confetti.style.transform = 'translate(-50%, -50%)';

            document.body.appendChild(confetti);

            const angle = Math.random() * 2 * Math.PI;
            const blastRadius = Math.random() * 400 + 200;
            const finalX = Math.cos(angle) * blastRadius;
            const finalY = Math.sin(angle) * blastRadius;
            const rotation = Math.random() * 1080 - 540;

            setTimeout(() => {
                confetti.style.transform = `translate(-50%, -50%) translate(${finalX}px, ${finalY}px) rotate(${rotation}deg)`;
                confetti.style.opacity = '0';
            }, 10);

            setTimeout(() => {
                confetti.remove();
            }, 1500);
        }
    };

    const triggerScreenShake = (duration) => {
        document.body.style.transition = 'none';
        const startTime = Date.now();
        const shakeInterval = setInterval(() => {
            const elapsedTime = Date.now() - startTime;
            if (elapsedTime > duration) {
                clearInterval(shakeInterval);
                document.body.style.transform = '';
                return;
            }
            const x = (Math.random() - 0.5) * 20;
            const y = (Math.random() - 0.5) * 20;
            document.body.style.transform = `translate(${x}px, ${y}px)`;
        }, 50);
    };

    const secretAction = () => {
        console.log('💥 MOUSE-PLOSION ACTIVATED! 💥');
        triggerScreenShake(300);
        triggerConfetti(mouseX, mouseY);
    };

    document.addEventListener('keyup', (e) => {
        sequence.push(e.key);
        sequence.splice(-konamiCode.length - 1, sequence.length - konamiCode.length);
        if (sequence.join('') === konamiCode.join('')) {
            secretAction();
        }
    });
}
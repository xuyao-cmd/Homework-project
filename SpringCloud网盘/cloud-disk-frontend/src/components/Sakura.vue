<template>
    <div class="sakura-container">
        <div
            v-for="petal in petals"
            :key="petal.id"
            class="sakura-petal"
            :style="petal.style"
        ></div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const petals = ref([])
let timer = null

const createPetal = () => {
    const id = Date.now() + Math.random()
    const left = Math.random() * 100
    const size = Math.random() * 12 + 6
    const duration = Math.random() * 10 + 8
    const delay = Math.random() * 5
    const opacity = Math.random() * 0.6 + 0.2
    const rotation = Math.random() * 360

    return {
        id,
        style: {
            left: `${left}%`,
            width: `${size}px`,
            height: `${size}px`,
            opacity,
            animationDuration: `${duration}s`,
            animationDelay: `${delay}s`,
            '--rotation': `${rotation}deg`
        }
    }
}

onMounted(() => {
    // 初始生成花瓣
    for (let i = 0; i < 30; i++) {
        petals.value.push(createPetal())
    }
    // 定期补充新花瓣
    timer = setInterval(() => {
        if (petals.value.length < 40) {
            petals.value.push(createPetal())
        }
        // 清理太旧的
        if (petals.value.length > 45) {
            petals.value = petals.value.slice(-40)
        }
    }, 2000)
})

onUnmounted(() => {
    clearInterval(timer)
})
</script>

<style scoped>
.sakura-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 9999;
    overflow: hidden;
}

.sakura-petal {
    position: absolute;
    top: -20px;
    background: linear-gradient(135deg, #ffc0cb 0%, #ffb7c5 30%, #ffa0b5 60%, #ff8da1 100%);
    border-radius: 50% 0 50% 0;
    animation: sakuraFall linear forwards;
    transform: rotate(var(--rotation));
}

@keyframes sakuraFall {
    0% {
        transform: translateY(-20px) rotate(0deg) scale(1);
        opacity: 0;
    }
    10% {
        opacity: 1;
    }
    50% {
        transform: translateY(50vh) rotate(180deg) scale(1.1);
    }
    80% {
        opacity: 0.8;
    }
    100% {
        transform: translateY(105vh) rotate(360deg) scale(0.3);
        opacity: 0;
    }
}
</style>

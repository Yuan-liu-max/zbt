<template>
  <div id="app">
    <router-view v-slot="{ Component, route }">
      <transition :name="String(route.meta.transition || 'fade-slide')" mode="out-in">
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>
  </div>
</template>

<script setup lang="ts">
// App root — page transitions + global layout
</script>

<style>
#app {
  min-height: 100vh;
  background: var(--bg-page);
  max-width: var(--max-width);
  margin: 0 auto;
  position: relative;
  overflow-x: hidden;
}

/* ======== Page Transitions ======== */

/* Fade + Slide (default) */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* Fade only (for tab switches) */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Slide up (for modals/popups) */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}
</style>

<template>
  <div id="app">
    <AppHeader v-if="showHeader" />
    <main>
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <AppFooter v-if="showFooter" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'

const route = useRoute()
// 登录、注册页面不显示头尾
const showHeader = computed(() => !['Login', 'Register'].includes(route.name))
const showFooter = computed(() => !['Login', 'Register'].includes(route.name))
</script>

<style lang="scss">
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.4s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.page-fade-enter-from {
  opacity: 0;
}

.page-fade-leave-to {
  opacity: 0;
}
</style>

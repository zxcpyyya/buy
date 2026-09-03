<template>
  <div class="category-page">
    <section class="page-hero">
      <div class="container-large">
        <h1 class="page-title">{{ category?.name || '分类' }}</h1>
        <p class="page-subtitle">{{ category?.description || '探索该分类下的精选商品' }}</p>
      </div>
    </section>
    
    <Products :category-id="Number(route.params.id)" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import Products from './Products.vue'

const route = useRoute()
const category = ref(null)

const fetchCategory = async () => {
  try {
    category.value = await request.get(`/category/${route.params.id}`)
  } catch (e) {
    console.error('获取分类失败', e)
  }
}

watch(() => route.params.id, fetchCategory)
onMounted(fetchCategory)
</script>

<style lang="scss" scoped>
.category-page {
  padding-top: 44px;
}

.page-hero {
  padding: 80px 0 48px;
  text-align: center;
  background: linear-gradient(180deg, #fbfbfd 0%, #fff 100%);
}

.page-title {
  font-size: 56px;
  font-weight: 600;
  letter-spacing: -0.015em;
  color: #1d1d1f;
  margin-bottom: 8px;
  line-height: 1.07;
  
  @media (max-width: 833px) {
    font-size: 40px;
  }
}

.page-subtitle {
  font-size: 21px;
  color: #6e6e73;
}
</style>

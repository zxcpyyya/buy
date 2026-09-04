import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMenuStore = defineStore('menu', () => {
  const menus = ref(JSON.parse(localStorage.getItem('admin_menus') || '[]'))
  const activeMenu = ref(localStorage.getItem('admin_active_menu') || '')

  function setMenus(newMenus) {
    menus.value = newMenus
    localStorage.setItem('admin_menus', JSON.stringify(newMenus))
  }

  function setActiveMenu(path) {
    activeMenu.value = path
    localStorage.setItem('admin_active_menu', path)
  }

  function clearMenus() {
    menus.value = []
    localStorage.removeItem('admin_menus')
    localStorage.removeItem('admin_active_menu')
  }

  return {
    menus,
    activeMenu,
    setMenus,
    setActiveMenu,
    clearMenus
  }
})

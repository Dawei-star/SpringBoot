import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
  state: () => ({ isDark: false }),
  actions: {
    setDark(val) {
      this.isDark = !!val
      const root = document.documentElement
      const body = document.body
      if (this.isDark) {
        root.setAttribute('data-theme', 'dark')
        root.classList.add('dark')
        body && body.classList.add('dark')
      } else {
        root.removeAttribute('data-theme')
        root.classList.remove('dark')
        body && body.classList.remove('dark')
      }
      localStorage.setItem('darkMode', String(this.isDark))
    },
    applyInitial() {
      const saved = localStorage.getItem('darkMode')
      const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
      this.setDark(saved !== null ? saved === 'true' : prefersDark)
      if (saved === null && window.matchMedia) {
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => this.setDark(e.matches))
      }
    },
    toggle() { this.setDark(!this.isDark) }
  }
})

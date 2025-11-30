import { defineStore } from 'pinia'

export const useTokenStore = defineStore('token', {
  state: () => ({ token: localStorage.getItem('token') || '' }),
  actions: {
    setToken(t) {
      this.token = t
      localStorage.setItem('token', t)
    },
    clear() {
      this.token = ''
      localStorage.removeItem('token')
    }
  }
})

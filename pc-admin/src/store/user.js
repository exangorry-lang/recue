import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    info: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    isSuperAdmin: (state) => state.info && state.info.roles && state.info.roles.includes('SUPER_ADMIN'),
    isLeader: (state) => state.info && state.info.roles && state.info.roles.includes('DEPT_LEADER')
  },
  actions: {
    setLogin(token, info) {
      this.token = token
      this.info = info
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(info))
    },
    logout() {
      this.token = ''
      this.info = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})

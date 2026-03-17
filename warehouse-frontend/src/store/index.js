import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const authCache = JSON.parse(sessionStorage.getItem('auth') || 'null')

export default new Vuex.Store({
  state: {
    auth: authCache,
    user: authCache ? authCache.user : null
  },
  getters: {
    token(state) {
      return state.auth ? state.auth.token : ''
    },
    isAdmin(state) {
      return !!(state.user && state.user.role === 2)
    }
  },
  mutations: {
    SET_AUTH(state, auth) {
      state.auth = auth
      state.user = auth ? auth.user : null
      sessionStorage.setItem('auth', JSON.stringify(auth))
    },
    CLEAR_AUTH(state) {
      state.auth = null
      state.user = null
      sessionStorage.removeItem('auth')
    }
  },
  actions: {
    login({ commit }, auth) {
      commit('SET_AUTH', auth)
    },
    logout({ commit }) {
      commit('CLEAR_AUTH')
    }
  }
})

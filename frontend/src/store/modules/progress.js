import { generateGetters } from 'utils'

const SET_PROGRESS = 'SET_PROGRESS'

const state = {
  progress: 0
}

const getters = generateGetters('progress')

let timeout

const actions = {
  setProgress({commit}, progress) {
    clearTimeout(timeout)
    commit(SET_PROGRESS, progress)
    if (progress !== 100) return
    timeout = setTimeout(() => {
      commit(SET_PROGRESS, 0)
    }, 600)
  }
}

const mutations = {
  [SET_PROGRESS](state, payload) {
    state.progress = payload
  }
}

export default {
  state,
  getters,
  actions,
  mutations
}

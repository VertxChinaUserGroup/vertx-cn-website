const INIT_STATE = {}

// eslint-disable-next-line
Object.assign(utils, INIT_STATE, {
  replaceRoute(route) {
    history.replaceState(null, null, route)
  }
})

const state = Object.assign({

}, INIT_STATE)

const getters = {

}

const actions = {

}

const mutations = {

}
export default {
  state,
  getters,
  actions,
  mutations
}

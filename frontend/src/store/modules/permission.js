const INIT_STATE = {}
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

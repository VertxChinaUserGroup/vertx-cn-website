import Vue from 'vue'
import VueRouter from 'vue-router'

import { dispatch, getters } from 'store'
import routes from './routes'

Vue.use(VueRouter)

const router = new VueRouter(routes)

router.beforeEach((to, from, next) => {
  next()
})

router.afterEach((to, from, next) => {
  Object.assign(router.app.$el, {
    scrollTop: 0,
    scrollLeft: 0
  })
})

export default router

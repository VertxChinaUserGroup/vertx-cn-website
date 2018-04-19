import 'styles/bootstrap'
import 'styles/app'
import Vue from 'vue'

import 'iview/dist/styles/iview.css'
import iView from 'iview'

// 注入 plugins 文件下的 plugins
import 'plugins'

import store, { dispatch, getters } from 'store'
import router from 'router'
import App from 'views/App'

import utils from 'utils'

Vue.use(iView)

Object.defineProperty(Vue.prototype, '$util', {
  value: utils,
  readable: true,
  writable: __DEV__
})

if (module.hot) module.hot.accept()

// eslint-disable-next-line no-new
new Vue({
  ...App,
  el: '#app',
  router,
  store
})

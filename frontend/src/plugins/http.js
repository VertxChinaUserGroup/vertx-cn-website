import Vue from 'vue'
import axios from 'axios'
import { Message } from 'iview'

import store from 'store'

const setProgress = progress => store.dispatch('setProgress', progress)

const PERMISSION_DENIED = () => Message.warning('您没有该资源的访问权限!')

const HANDLER = {
  401: PERMISSION_DENIED,
  404: () => Message.warning('未找到匹配的 url 请求!'),
  500: () => Message.warning('系统异常，请稍后重试！')
}

const service = axios.create({
  baseURL: __MOCK__ ? '' : BASE_URL,
  timeout: 10000
})

const requestInterceptor = config => {
  config.headers['X-Requested-With'] = 'XMLHttpRequest'
  config.headers['Content-Type'] = 'application/json'

  // token etc

  setProgress(50)

  return config
}

const responseInterceptor = response => {
  // 此处可根据后端自定义 code 来拦截
  const { data, code } = response

  setProgress(100)

  return Promise.resolve(data)
}

const errorInterceptor = error => {
  const { response } = error
  const { status } = response

  setProgress(0)

  HANDLER[status] && HANDLER[status]()

  return Promise.reject(error)
}

service.interceptors.request.use(requestInterceptor)

service.interceptors.response.use(responseInterceptor, errorInterceptor)

Vue.prototype.$http = service

export default service

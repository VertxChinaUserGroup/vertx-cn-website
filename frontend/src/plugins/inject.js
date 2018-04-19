import Vue from 'vue'

// 注入 plugins
const combine = function (context, modules, key) {
  modules[key.replace(/(^\.\/)|(\.(js|vue)$)/g, '')] = context(key)
  return modules
}

const HANDLER = {
  component: combine,
  directive: combine,
  filter: (context, modules, key) => Object.assign(modules, context(key))
}

export default (context, type) => {
  const values = context.keys().reduce((modules, key) => HANDLER[type](context, modules, key), {})
  Object.keys(values).forEach(key => Vue[type](key, values[key]))
}

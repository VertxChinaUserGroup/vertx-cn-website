const modulesContext = require.context('.', true, NON_INDEX_REGEX)

const routes = modulesContext.keys().reduce((modules, key) => modules.concat(modulesContext(key)), [])

routes.push({
  path: '*',
  redirect: '/404'
})

export default {
  mode: 'history',
  routes
}

// const contextRoute = require.context('../../views', true, /^\.\/((?!\/)[\s\S])+\/route\.js$/)

export default [{
  path: '/',
  name: 'home',
  component: () => import('views/Home'),
  meta: {
    bg: true
  }
}, {
  path: '/404',
  name: '404',
  component: () => import('components/Widgets/NotFound')
}]

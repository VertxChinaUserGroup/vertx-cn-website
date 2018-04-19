import path from 'path'
import {argv} from 'yargs'
import _debug from 'debug'

const debug = _debug('vertx:config:base')

const NODE_ENV = process.env.NODE_ENV || 'development'
debug('the node_env is:', NODE_ENV)

const __PROXY__ = !!argv.proxy
const config = {
  env: NODE_ENV,

  pkg: require('../../package.json'),

  // ----------------------------------
  // Project Structure
  // ----------------------------------
  path_base: path.resolve(__dirname, '../../'),
  dir_src: 'src',
  dir_dist: 'dist',
  dir_server: 'server',
  dir_test: 'test',

  // ----------------------------------
  // Server Configuration
  // you can change your server_host and server_port here
  // ----------------------------------
  server_host: 'localhost',
  server_port: process.env.PORT || 3000,

  // ----------------------------------
  // Compiler Configuration
  // ----------------------------------
  compiler_html_minify: false,
  compiler_css_modules: false,
  compiler_devtool: 'source-map',
  compiler_hash_type: 'hash',
  compiler_quiet: false,
  compiler_browsers: ['> 1% in CN'],
  compiler_stats: {
    colors: true,
    modules: false,
    children: false,
    chunks: false,
    chunkModules: false
  },
  compiler_alias: {
    vue: 'vue/dist/vue.common'
  },
  compiler_vendor: [
    'axios',
    'moment',
    'qs',
    'vue',
    'vue-router',
    'vuex',
    'iview'
  ]
}
// ------------------------------------
// Environment 全局环境变量配置
// ------------------------------------

const __MOCK__ = !!argv.mock

const __DEV__ = NODE_ENV === 'development'

// require.context regex
let NON_INDEX_REGEX = /^(?!.*[/\\](index)\.js).*\.(js|vue)$/.toString()

__DEV__ || (NON_INDEX_REGEX = NON_INDEX_REGEX.replace('index', 'index|test'))

config.globals = {
  'process.env.NODE_ENV': JSON.stringify(NODE_ENV),
  NODE_ENV,
  __DEV__,
  __PROXY__,
  __PROD__: NODE_ENV === 'production',
  __TEST__: NODE_ENV === 'test',
  __TESTING__: NODE_ENV === 'testing',
  __MOCK__,
  BASE_URL: JSON.stringify('/api'),
  BASE_PREFIX_URL: JSON.stringify(process.env.BASE_PREFIX) || ('"http://localhost:3001/"'),
  CONTEXT: JSON.stringify('/context'),
  IMG_PATH_PREFIX: (JSON.stringify((!__MOCK__ || !__DEV__) && process.env.IMG_PATH_PREFIX || 'http://baidu.com/')),
  NON_INDEX_REGEX
}

// ------------------------------------
// Validate Vendor Dependencies 检查 vendors 依赖
// ------------------------------------
config.compiler_vendor = config.compiler_vendor
  .filter(dep => ({...config.pkg.dependencies, ...config.compiler_alias}.hasOwnProperty(dep) ? true : debug(
    'Package "' + dep + '" was not found as an npm dependency in package.json; ' +
    'it won\'t be included in the webpack vendor bundle.\n' +
    'Consider removing it from compiler_vendor in "./config/_base.js"'
  )))

// ------------------------------------
// Utilities  path 配置
// ------------------------------------
config.paths = (() => {
  const resolve = path.resolve

  const base = (...args) =>
    resolve.apply(resolve, [config.path_base, ...args])

  return {
    base,
    src: base.bind(null, config.dir_src),
    dist: base.bind(null, config.dir_dist),
    server: base.bind(null, config.dir_server),
    test: base.bind(null, config.dir_test)
  }
})()

export default config

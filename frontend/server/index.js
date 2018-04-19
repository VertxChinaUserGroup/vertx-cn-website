import Koa from 'koa'
import logger from 'koa-logger'
import compress from 'koa-compress'
import proxy from 'koa-proxy2'
import _debug from 'debug'
import config from '../build/config'
import error from './error'
import history from './history'
import dev from './dev-tools'
import {argv} from 'yargs'

const proxies = require('koa-proxies')

const debug = _debug('vertx:server')

// Koa application is now a class and requires the new operator.
const app = new Koa()

app.use(history())

app.use(logger())
app.use(compress())

// handle error
app.use(error())

const __MOCK__ = !!argv.mock

if (!__MOCK__) {
  app.use(proxy({
    proxy_rules: [
      {
        proxy_location: /^\/api/,
        proxy_pass: 'http://localhost:8090'
      }
    ]
  }))
}
// ------------------------------------
// Apply Webpack DEV/HMR Middleware
// ------------------------------------
dev(app)

const args = [config.server_port, config.server_host]

export default app.listen(...args, err =>
  debug.apply(null, err ? [err] : ['Server is now running at http://%s:%s.', ...args.reverse()]))

require('babel-register')
require('babel-polyfill')

const debug = require('debug')('vertx:bin:compile')

debug('Create webpack compiler.')

require('webpack')(require('../webpack')).run((err, stats) => {
  const jsonStats = stats.toJson()

  debug('Webpack compile completed.')

  console.log(stats.toString({
    modules: false,
    children: false,
    chunks: false,
    chunkModules: false,
    colors: true
  }))

  if (err) {
    debug('Webpack compiler encountered a fatal error.', err)
    process.exit(1)
  } else if (jsonStats.errors.length > 0) {
    debug('Webpack compiler encountered errors.')
    console.error(jsonStats.errors)
    process.exit(1)
  } else if (jsonStats.warnings.length > 0) {
    debug('Webpack compiler encountered warnings.')
  } else {
    debug('No errors or warnings encountered.')
  }
})

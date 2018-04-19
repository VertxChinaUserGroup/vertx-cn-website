require('babel-register')

const debug = require('debug')('vertx:bin:clean')
const paths = require('../config').paths

debug('Clean files...')

require('del')([paths.dist('**'), paths.base('sync/**')], err => {
  if (err) {
    debug(err)
  } else {
    debug('Files cleaned.')
  }
})

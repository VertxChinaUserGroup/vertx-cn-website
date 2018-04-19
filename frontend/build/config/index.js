import fs from 'fs'
import _debug from 'debug'
import config, {env} from './_base'

const debug = _debug('vertx:config')
debug('Create configuration.')
debug(`Apply environment overrides for NODE_ENV "${env}".`)

// 检查 env 文件是否存在
const overridesFilename = `_${env}`
let hasOverridesFile
try {
  fs.lstatSync(`${__dirname}/${overridesFilename}.js`)
  hasOverridesFile = true
} catch (e) {
  // debug(e)
}

// export config to be merged
let overrides
if (hasOverridesFile) {
  overrides = require(`./${overridesFilename}`)(config)
} else {
  debug(`No configuration overrides found for NODE_ENV "${env}"`)
}

export default {...config, ...overrides}

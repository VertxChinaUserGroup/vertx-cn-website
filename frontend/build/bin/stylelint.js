const debug = require('debug')('vertx:stylelint')

require('stylelint').lint({
  files: ['css', 'less', 'sass', 'scss'].map(value => 'src/**/*\\.' + value),
  formatter: 'verbose'
}).then(result => result.errored && (debug('there are some errors occurred!%s', result.output) || process.exit(1)))

require('stylint')('src', require('../../.stylintrc')).create()

const modules = require.context('../..', true, /\/mock\.js$/)

modules.keys().forEach(key => modules(key))

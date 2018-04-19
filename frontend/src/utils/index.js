const modulesContext = require.context('.', false, NON_INDEX_REGEX)

const utils = modulesContext.keys().reduce((modules, key) => Object.assign(modules, modulesContext(key)), {})

__PROD__ || (window.utils = utils)

export default utils

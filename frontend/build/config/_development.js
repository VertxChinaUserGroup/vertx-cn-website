// We use an explicit public path when the assets are served by webpack
// to fix this issue:
// http://stackoverflow.com/questions/34133808/webpack-ots-parsing-error-loading-fonts/34133809#34133809
export default config => ({
  compiler_devtool: 'eval',
  // compiler_public_path: '/'
  compiler_public_path: `http://${config.server_host || 'localhost'}:${config.server_port}/`
})

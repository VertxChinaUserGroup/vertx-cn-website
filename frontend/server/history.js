import history from 'connect-history-api-fallback'

export default options => async({req, res}, next) => {
  history(options)(req, res, () => {
  })
  await next()
}

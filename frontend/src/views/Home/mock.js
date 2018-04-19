import { mock } from 'mockjs'

mock(/\/get-home-data$/, () => {
  return mock({
    username: '@cname(4, 5)'
  })
})

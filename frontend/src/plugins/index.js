import './components'
import './directives'
import './filters'
import './http'

if (__MOCK__) {
  require('./mock')
  __DEV__ || require('utils').warn('Notice: you are using mock server!')
}

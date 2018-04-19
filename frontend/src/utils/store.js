import { isArray } from './base'

export const generateGetters = keys => {
  isArray(keys) || (keys = [keys])
  const getters = {}
  keys.forEach(key => (getters[key] = state => state[key]))
  return getters
}

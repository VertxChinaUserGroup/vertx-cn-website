export const urlParam = param => {
  const a = new RegExp('(\\?|&)' + param + '=([^&\\?]*)').exec(location.search)
  if (!a) return ''
  return RegExp.$2
}

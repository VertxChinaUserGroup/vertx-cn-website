<template lang="pug">
  div(:class="$style.loading")
    div(:class="$style.loadingContent",
    :style="{width: '60px',height: '60px', backgroundImage: `url(${loading})`}")
</template>
<script>
  export default {
    name: 'loading',
    data() {
      return {
        loading: require('./loading.gif')
      }
    }
  }
</script>
<style lang="stylus" module>
  .loading
    fixed(left)
    top 50%
    size 100%
    text-align center
    transform translate3d(0, -50%, 0)
    background-color rgba(0, 0, 0, .25)
    z-index 10000

  .loading-content
    middleCenter()
    border-radius 10px
    background-color $stress-color
    background-size cover
    background-position center
    background-repeat no-repeat
</style>

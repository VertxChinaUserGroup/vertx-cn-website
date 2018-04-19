<template lang="pug">
  #app(:class="$style.container")
    img(:src="img")
    router-view(transition="fade")

    loading(v-if="progress", :progress="progress")
</template>
<script>
  import { mapGetters } from 'vuex'
  import Loading from 'components/Widgets/Loading'

  export default {
    name: 'app',
    computed: {
      ...mapGetters(['progress'])
    },
    data() {
      return {
        img: require('assets/logo.png')
      }
    },
    components: {
      Loading
    }
  }
</script>
<style lang="stylus" module>
  .container
    font-family 'Avenir', Helvetica, Arial, sans-serif
    -webkit-font-smoothing antialiased
    -moz-osx-font-smoothing grayscale
    text-align center
    color #2c3e50
    margin-top: 60px
</style>

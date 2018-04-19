<template lang="pug">
  div(:class="$style.container")
    h3 vertx china
    h3 Hello! your name is {{ username }} now.
    h3 You can refresh your web page to change your name with mock
    Button(type='primary' @click='test') http
</template>
<script>
  import { testHttp } from '@/api'
  export default {
    data() {
      return {
        username: ''
      }
    },
    async created() {
      const data = (await this.$http.post('/get-home-data'))
      Object.assign(this, data)
    },
    methods: {
      test() {
        testHttp()
      }
    }
  }
</script>
<style lang="stylus" module>
  .container
    h1, h2
      font-weight normal

    ul
      list-style-type none
      padding 0

    li
      display inline-block
      margin 0 10px

    a
      color #42b983

</style>

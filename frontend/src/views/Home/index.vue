<template lang="pug">
  div(:class="$style.container")
    h3 vertx china
    h3 Hello! your name is {{ username }} now.
    Button(type='primary' @click='mock') mock
    Button(type='primary' @click='http') 404
</template>
<script>
  import { testHttp } from '@/api'
  export default {
    data() {
      return {
        username: ''
      }
    },
    methods: {
      http() {
        testHttp()
      },
      async mock() {
        const data = (await this.$http.post('/get-home-data'))
        Object.assign(this, data)
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

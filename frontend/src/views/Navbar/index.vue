<template lang="pug">
  nav(:class="$style.navbar")
    div(:class="$style.navbarlayoutCeiling")
      div(:class="$style.navbarCeilingMain")
        a(href="") 登录
        a(href="") 注册
        a(href="") 更多
      div(:class="$style.navbarHeader") vertx china
      div(:class="$style.navbarItems")
        a(href="") 首页
        a(href="") 话题
        a(href="") 精华
</template>

<script>
export default {
  name: 'Navbar',
  data() {
    return {
      logo: require('@/assets/logo.png')
    }
  }
}
</script>

<style lang="stylus" module>
  .navbar
    background #464c5b
    position fixed
    width 100%
    top 0
    color #fff
    height 52px
    line-height 52px
    overflow hidden

  .navbar-ceiling
    overflow hidden

  .navbar-header
    float left
    margin-left 15px

  .navbar-logo
    width 40px
    height 40px

  .navbar-ceiling-main
    float right
    margin-right 15px
    a
      margin 0 5px

  .navbar-items
    margin-left 120px
    float left
    a
      margin 0 10px
</style>

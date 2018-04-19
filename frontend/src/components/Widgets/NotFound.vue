<template lang="pug">
  hi-empty(:text="`未找到对应路由，请确认后重试！`", :img="require('./404.png')")
</template>
<script>
  export default {
    name: 'not-found'
  }
</script>

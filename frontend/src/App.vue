<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  overflow-y: scroll;
}

#app {
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  font-size: 20px !important;
  min-height: 100vh;
}

body {
  font-size: 20px !important;
  min-height: 100vh;
  overflow-y: scroll;
}

.el-menu-item,
.el-button,
.el-input__inner,
.el-form-item__label,
.el-table td,
.el-table th,
.el-dialog__title,
.el-tag {
  font-size: 20px !important;
}
</style>
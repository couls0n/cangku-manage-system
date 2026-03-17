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
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  margin: 0;
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #f4f7fb;
  color: #18212f;
}

body {
  min-width: 1280px;
}

.page-shell {
  display: grid;
  gap: 18px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 22px 26px;
  background: linear-gradient(135deg, #083b66, #0f766e);
  color: #fff;
  border-radius: 24px;
  box-shadow: 0 16px 32px rgba(8, 59, 102, 0.18);
}

.page-header h2 {
  margin: 0 0 8px;
  font-size: 28px;
}

.page-header p {
  margin: 0;
  color: rgba(255, 255, 255, 0.78);
}

.data-card {
  border-radius: 22px !important;
  border: 1px solid rgba(15, 23, 42, 0.06) !important;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.05) !important;
}

.panel-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.panel-toolbar .left-tools,
.panel-toolbar .right-tools {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(244, 247, 251, 0.88));
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.metric-card .label {
  color: #6b7280;
  font-size: 13px;
}

.metric-card .value {
  margin-top: 12px;
  font-size: 30px;
  font-weight: 700;
}

.soft-tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(15, 118, 110, 0.1);
  color: #0f766e;
  font-size: 12px;
  font-weight: 600;
}

.muted-text {
  color: #6b7280;
}
</style>

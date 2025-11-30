# Vue 3 + Vite

This template should help get you started developing with Vue 3 in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about IDE Support for Vue in the [Vue Docs Scaling up Guide](https://vuejs.org/guide/scaling-up/tooling.html#ide-support).
# Big Event Frontend (fronted-front)

## 开发
- 安装：`npm i`
- 启动：`npm run dev`

## 代码质量
- Lint：`npm run lint`，自动修复：`npm run lint:fix`
- 格式化：`npm run format`

## 测试
- 单元测试：`npm run test`（使用 Vitest）

## 结构说明
- `src/components`：通用组件
- `src/views`：页面视图
- `src/utils`：通用工具（如日期格式化）
- `src/stores`：Pinia 仓库（如主题）

## 主题与暗色
- 暗色变量：`element-plus/theme-chalk/dark/css-vars.css`
- 切换控制：`src/stores/theme.js` + `src/components/ThemeSwitch.vue`

## 注意
- 本仓库作为主前端；另一个 `big-event-frontend` 为旧版示例。如需合并，请以此项目为主。

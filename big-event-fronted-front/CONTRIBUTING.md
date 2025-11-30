## 开发流程

1. Fork/创建分支：`feature/<name>`
2. 运行：`npm run dev`
3. 代码检查：`npm run lint` 与 `npm run format`
4. 编写单测：`tests/unit`，运行 `npm run test`
5. 提交 PR：描述改动与影响范围

## 代码规范
- 使用 Composition API 与 `<script setup>`
- 组件命名使用帕斯卡命名，如 `ArticleCard.vue`
- 通用逻辑放入 `src/utils` 或 `src/stores`

## UI规范
- 统一使用 Element Plus 组件与全局变量（`styles/enhancements.scss`）
- 支持暗色主题与无障碍（ARIA 标签、键盘可访问）

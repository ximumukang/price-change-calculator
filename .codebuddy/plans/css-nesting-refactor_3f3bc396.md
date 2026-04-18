---
name: css-nesting-refactor
overview: 将5个Vue组件的 `<style scoped>` CSS从平铺选择器写法改为CSS嵌套写法
todos:
  - id: convert-register
    content: 转换 Register.vue CSS为嵌套写法
    status: completed
  - id: convert-login
    content: 转换 Login.vue CSS为嵌套写法
    status: completed
  - id: convert-dashboard
    content: 转换 Dashboard.vue CSS为嵌套写法
    status: completed
  - id: convert-recovery
    content: 转换 RecoveryCalculator.vue CSS为嵌套写法
    status: completed
  - id: convert-compound
    content: 转换 CompoundCalculator.vue CSS为嵌套写法
    status: completed
---

## 核心需求

将5个Vue组件中 `<style scoped>` 内的CSS样式从平铺选择器写法改为CSS嵌套写法，保持视觉效果完全不变。

## 转换规则

- `.parent .child {}` 改为 `.parent { .child {} }`
- `.parent:hover {}` 改为 `.parent { &:hover {} }`
- `.parent:last-child {}` 改为 `.parent { &:last-child {} }`
- `.parent :deep(.el-xxx) {}` 改为 `.parent { :deep(.el-xxx) {} }`
- `@keyframes` 和 `@media` 保持顶层位置不变
- 独立的顶层选择器（无父子关系）保持顶层不变

## 技术栈

- CSS Nesting（Vite 8 原生支持，无需额外配置）
- Vue 3 SFC `<style scoped>`

## 实现方案

逐文件将平铺CSS转换为嵌套写法。核心转换逻辑：

1. 识别选择器间的父子关系，将子选择器嵌套进父选择器
2. 伪类/伪元素用 `&` 引用父选择器
3. `:deep()` 穿透选择器保持不变，嵌套进对应父级
4. `@keyframes`/`@media` 保留在顶层
5. 同级选择器（如 `.bg-shape-1`、`.bg-shape-2`、`.bg-shape-3` 都继承 `.bg-shape` 的基础样式）分别保持顶层，仅在存在明确父子关系时嵌套

## 注意事项

- 嵌套深度不宜过深，最多2-3层，避免可读性下降
- 重复的类名（如 `.decline-val` 在RecoveryCalculator中出现在多处）保持顶层定义，不强制嵌套
- `.footer a` 这类选择器改为 `.footer { a {} }`
- `@media` 内的选择器也采用嵌套写法
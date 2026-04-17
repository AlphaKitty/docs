# 技能操作结构化编辑设计

**目标**

将技能列表中的删除、编辑、技能配置改为真实能力，并让编辑支持结构化字段更新。

**范围**

- 仅修改 `expert-link-backend` 与 `expert-link-frontend`
- `SkillList` 中的删除改为真实删除
- 编辑与技能配置统一走独立编辑页
- 本轮不扩展批量删除或复杂权限控制

**设计**

1. 后端
   - 将 `PUT /api/skills/{id}` 升级为结构化 DTO 更新接口
   - 支持更新：
     - `name`
     - `category`
     - `description`
     - `domainId`
     - `demandLevel`
     - `enabled`
     - `tags`
     - `relatedSkillIds`
     - `documentation`
   - 保持事务性，确保标签、相关技能、文档更新一致

2. 前端
   - `SkillList.vue`
     - `删除` 真实调用删除接口
     - 成功后刷新列表
     - `编辑` 和 `技能配置` 统一跳转到编辑页
   - `AddSkill.vue`
     - 改造成“新增 / 编辑双模式”
     - 编辑模式根据路由参数加载技能详情
     - 表单回填结构化数据
     - 保存时调用 `updateSkill()`
   - 路由
     - 增加 `/skills/:id/edit`

3. 验证
   - 可编辑现有结构化技能
   - 更新标签、相关技能、文档后详情页可见变化
   - 列表中删除技能后立即消失

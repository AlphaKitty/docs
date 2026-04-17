# 新增技能全结构化联动设计

**目标**

打通“新增技能 -> 列表可见 -> 详情可见”完整链路，并将 `tags`、`relatedSkills`、`documentation` 从前端表单字段升级为后端真实结构化数据。

**范围**

- 只修改 `expert-link-frontend` 与 `expert-link-backend`
- `expert-link-frontend-simple` 仅作为原型参考，不做实现修改
- 本轮完成新增技能闭环，不扩展为完整技能编辑系统

**数据模型**

1. `Tag`
   - 独立实体
   - 核心字段：`id`、`name`
   - 与 `Skill` 为多对多关系
   - 创建技能时允许前端直接传字符串数组，后端负责复用或新建标签
   - 标签名写入前需要做 `trim + lower-case compare` 归一化，避免 `Java` / ` java ` / `JAVA` 重复

2. `Skill.relatedSkills`
   - `Skill` 与 `Skill` 自关联多对多
   - 用于表达“当前技能关联哪些其他技能”
   - 创建时由前端传 `relatedSkillIds`
   - 后端负责过滤不存在的技能和非法自关联
   - 接口响应中只返回浅层 related skill DTO，避免递归嵌套整个 `Skill`

3. `SkillDocumentation`
   - 与 `Skill` 一对一
   - 至少包含 `content`
   - 可选包含 `url`
   - 用于支撑当前新增页中的“技能文档”字段

**接口设计**

- 创建接口统一为 `POST /api/skills`
- 请求体使用 DTO，包含：
  - `name`
  - `category`
  - `description`
  - `domainId`
  - `demandLevel`
  - `enabled`
  - `tags`
  - `relatedSkillIds`
  - `documentation`
- 创建流程：
  1. 校验技能名称唯一
  2. 校验领域存在
  3. 解析标签：同名复用，不存在则创建
  4. 解析相关技能：过滤不存在项，排除自己
  5. 创建 `Skill`
  6. 关联标签
  7. 关联相关技能
  8. 创建文档
  9. 返回完整技能详情
 - 整个创建流程必须在一个事务中完成，避免部分写入成功

**数据库策略**

- 使用 JPA 建表能力或项目现有 schema 更新机制创建：
  - `tags`
  - `skill_documentation`
  - `skill_tags`
  - `skill_related_skills`
- 需要明确唯一约束和外键：
  - `tags.name` 唯一
  - `skill_documentation.skill_id` 唯一
  - 关联表外键指向 `skills` / `tags`
- 关联表命名和约束需要稳定，避免后续环境不一致

**前端设计**

1. `AddSkill.vue`
   - 保留现有页面结构
   - 新增真实领域下拉
   - 相关技能改为真实技能列表
   - 保存时映射为新的创建 DTO
   - 创建成功后统一跳转到技能详情页，保证验证路径固定

2. `SkillList.vue`
   - 继续使用真实接口
   - 新增成功后回列表页应看到新技能

3. `SkillDetail.vue`
   - 优先读取后端真实 `tags`、`relatedSkills`、`documentation`
   - 缺失时再走现有降级逻辑

**验证**

- 可以从新增技能页成功创建一个技能
- 创建请求可同时提交领域、标签、相关技能、文档
- 创建成功后跳转到详情页
- 详情页可见真实标签、相关技能、文档
- 再返回列表页时可见该技能
- 本轮修改不引入新的已编辑文件诊断错误

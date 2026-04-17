# 技能领域真实联动设计

**目标**

将前端 `SkillList` 和 `SkillDetail` 从静态 mock 页面改为真实接口页面，同时保留现有顶部分类卡片 UI，并让分类卡片来自后端 `domains + skills` 数据聚合。

**范围**

- 本轮前端唯一改动目标为 `expert-link-frontend`
- `expert-link-frontend-simple` 仅作为原型参考，不进行实现修改
- 列表页接入真实 `skills` 分页接口
- 详情页接入真实 `skill` 详情接口
- 分类卡片接入真实 `domains` 数据，并基于技能列表聚合展示
- 维持现有页面结构与交互入口，避免本轮扩大到完整技能 CRUD 重构

**设计**

1. `SkillList.vue`
   - 保留现有页面结构
   - 使用 `SkillService.getSkills()` 获取真实技能列表
   - 新增领域查询服务，获取真实 `domains`
   - 通过适配函数将后端 `skills` / `domains` 映射为页面展示模型
   - 顶部分类卡片按领域展示：
     - `name` 来自 domain name
     - `description` 来自 domain description
     - `expertCount` / `projectCount` 优先使用 domain 自带字段
     - `topSkills` 来自该领域下技能名的前几项
     - `matchRate` 为前端降级展示字段，本轮使用可解释的聚合占位值
   - 表格保留搜索和分类筛选，但筛选逻辑改为真实字段

2. `SkillDetail.vue`
   - 使用 `SkillService.getSkillById()` 获取真实详情
   - 使用 `SkillService.getSkills()` 获取同分类或同领域技能，作为“相关技能”和“同分类技能”数据源
   - 缺失的后端字段采用“真实优先，缺失降级”：
     - `tags` 使用 `name/category/domain` 等组合构造
     - `documentation` 缺失时展示“暂无文档”
     - `creator` 缺失时展示“系统”
     - `matchRate` 使用降级统计值
   - 保留现有编辑/配置按钮，但仍然只做提示，不扩展新功能

3. 前端服务层
   - 复用已有 `SkillService`
   - 增加 `DomainService`
   - 修正 `skill.service.ts` 中与当前后端不一致的接口定义，避免后续列表页和详情页取值错误
   - 领域和技能的关联优先使用后端返回的 `skill.domain.id`，若不存在再降级到 `domainId` / `domain.name` 类字段推断

4. 数据映射原则
   - 不直接让页面消费后端原始对象
   - 在视图文件附近放置轻量 adapter，统一处理：
     - 分类枚举映射
     - `demandLevel` 枚举转显示等级
     - 缺省字段兜底
     - domain-skill 关联聚合

5. 验证
   - 先跑定向前端类型检查或构建检查，确认未引入新的错误
   - 再通过真实接口返回和页面逻辑核对：
     - 技能列表能显示真实数据
     - 技能详情能显示真实数据
     - 分类卡片能反映真实领域和技能关系
     - 页面加载中、空数据、接口异常时有可接受的降级展示

**非目标**

- 不在本轮实现完整技能编辑/配置功能
- 不在本轮重构为新的 Pinia store
- 不在本轮调整后端接口设计

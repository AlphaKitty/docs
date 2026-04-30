/**
 * 评价打分页展示用文案（与设计文档「专家活动申请表」评价打分页一致）
 */
import { BASE_SCORE_BY_ITEM_SCOPE } from './engagement-form-config'

/** 积分提示：显示在打分段落最上方 */
export const POINTS_EVAL_HINT_BY_ITEM: Record<string, string> = {
  技术评审:
    '作为领域专家，参加部门内或跨部门的项目/方案评审、GPS项目/投资评审等，主动识别本领域关键事项问题/风险，并提出有效建议。',
  人才评审: '作为评委参与人才标准评审或人才认证答辩等工作。',
  技术支持:
    '以技委会专家身份，参与跨BG/BU的技术攻关顺利攻克或跨BG专项顺利完成专项任务等，BG反馈满意度评分3分及以上。',
  '行业/技术洞察':
    '基于业务战略及行业趋势，识别当前行业/技术/产品/管理热点，输出行业/技术/产品洞察报告，并在内部评审、分享。',
  技术成果推广:
    '将技术预研、攻关、研究等成果进行萃取、包装和转化，形成案例并在相关领域内推广应用，如项目案例、GPS案例包装推广等。',
  人才标准建设: '作为编写成员主责或参与人才标准（岗位职责、任职标准、学习地图等）建设。',
  专业论文: '在行业专业期刊/网站/公众号上，以排名前3的作者身份发表专业论文/文章等。',
  知识产权: '以排名前3的身份获得知识产权。',
  项目经验沉淀: '定期做好项目复盘，形成有价值的 lesson learn。',
  知识库建设:
    '及时总结和转化典型问题，形成相应的案例分析或操作指导，并对相关知识（Checklist/Design guide/失效模式）进行更新和关联，典型问题经评审在知识库发布。',
  流程制度建设: '参与或主导流程、制度、管理规范等标准的建设工作，并经过公司评审发布。',
  课程开发: '主导专业类或通用类课程开发，课程经人力资源本部或一级部门内部认证备案。',
  担任导师: '担任员工导师，包括歌尔之翼、内部调岗或社招员工。',
  担任讲师: '担任内部讲师，在内部分享知识、经验，单次培训人数不得少于10人，有签到表/纪要。',
  '行业/业务交流':
    '作为领域专家代表在行业/业务领域内技术交流/访谈拜访/客户来访等活动中发表课题、分享经验等。',
}

/** 积分标准：显示在积分提示下方（累计/计量规则说明） */
export const POINTS_EVAL_STANDARD_BY_ITEM: Record<string, string> = {
  技术评审: '①每参与跨BG评审4次可获得2分\n②每参与BG内评审4次可获得1分',
  人才评审: '1次1分',
  技术支持: '①跨BG专项2分\n②BG内专项1分',
  '行业/技术洞察': '1个1分',
  技术成果推广: '①跨BG应用3分\n②BG内应用2分\n③BU内应用1分',
  人才标准建设:
    '①作为编写组长主责人才标准的编写工作2分\n②作为编写成员参与岗位标准的编写工作1分',
  专业论文:
    '①国家级核心期刊发表1篇 3分\n②省市级刊物发表1篇 2分\n③公司内刊物发表1篇 1分',
  知识产权:
    '①以排名前3的身份获得发明专利1次2分\n②以排名前3的身份实用新型专利1次1分',
  项目经验沉淀: '有价值的 lesson learn 1次1个1分',
  知识库建设: '典型问题经评审在知识库发布，每3个问题可获得1分',
  流程制度建设:
    '①主责一级部门/业务领域内制度或流程建设/个2分\n②作为核心成员参与一级部门/业务领域内制度或流程建设/个1分',
  课程开发:
    '①主责公司级/专业线课程开发/门2分\n②作为核心成员参与公司级/专业线课程开发或主导一级部门内课程开发/门1分',
  担任导师:
    '①担任14级及以上员工导师，辅导员工顺利转正，且员工评价A/B+1人3分\n②担任14级及以上员工导师，辅导员工顺利转正，且员工评价B；或担任14级以下员工导师，辅导员工顺利转正，且员工评价A/B+1人2分\n③担任14级以下员工导师，辅导员工顺利转正，且员工评价B1人1分',
  担任讲师:
    '①进行公司级/专业线级培训授课，单次培训人次不低于50人1次3分\n②进行一级部门内部培训授课，单次培训人次不低于30人1次2分\n③担任二级部门内部培训授课，单次培训人次不低于10人1次1分',
  '行业/业务交流':
    '①参与行业交流会，并作为公司代表分享发言1次2分\n②外部公司来访/到访外部公司/客户来访作为代表进行分享交流1次1分',
}

const CIRCLED_NUMS = ['①', '②', '③', '④', '⑤', '⑥']

/** 本单「贡献范围 → 标准分」与系统 BASE_SCORE 一致，便于对照 */
export function formatContributionScopeScoreLines(pointsItem: string): string {
  const map = BASE_SCORE_BY_ITEM_SCOPE[pointsItem]
  if (!map || Object.keys(map).length === 0) return ''
  return Object.entries(map)
    .map(([scope, score], i) => `${CIRCLED_NUMS[i] ?? `${i + 1}.`}${scope}=${score}`)
    .join('\n')
}

export type PointsEvalRulesDisplay = {
  hint: string
  standard: string
  scopeLines: string
}

export function getPointsEvalRulesDisplay(pointsItem: string): PointsEvalRulesDisplay | null {
  const hint = POINTS_EVAL_HINT_BY_ITEM[pointsItem]?.trim() || ''
  const standard = POINTS_EVAL_STANDARD_BY_ITEM[pointsItem]?.trim() || ''
  const scopeLines = formatContributionScopeScoreLines(pointsItem).trim()
  if (!hint && !standard && !scopeLines) return null
  return { hint, standard, scopeLines }
}

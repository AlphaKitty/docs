import * as echarts from "echarts"
import type { ECharts, EChartsOption } from "echarts"

const DEFAULT_EXPERT_GROWTH = [
  { month: "1月", experts: 120, growth: 5 },
  { month: "2月", experts: 125, growth: 4.2 },
  { month: "3月", experts: 132, growth: 5.6 },
  { month: "4月", experts: 142, growth: 7.6 },
  { month: "5月", experts: 148, growth: 4.2 },
  { month: "6月", experts: 156, growth: 5.4 },
  { month: "7月", experts: 162, growth: 3.8 },
  { month: "8月", experts: 170, growth: 4.9 },
  { month: "9月", experts: 178, growth: 4.7 },
  { month: "10月", experts: 185, growth: 3.9 },
  { month: "11月", experts: 192, growth: 3.8 },
  { month: "12月", experts: 200, growth: 4.2 }
]

const DEFAULT_PROJECT_STATUS = [
  { value: 12, name: "规划中", color: "#909399" },
  { value: 20, name: "进行中", color: "#409EFF" },
  { value: 10, name: "已完成", color: "#67C23A" },
  { value: 4, name: "已暂停", color: "#E6A23C" },
  { value: 2, name: "已取消", color: "#F56C6C" }
]

const DEFAULT_SKILL_HEAT = [
  { name: "Vue.js", value: 98, category: "前端开发" },
  { name: "React", value: 92, category: "前端开发" },
  { name: "Spring Boot", value: 95, category: "后端开发" },
  { name: "Python", value: 88, category: "后端开发" },
  { name: "Docker", value: 82, category: "DevOps" },
  { name: "Kubernetes", value: 78, category: "DevOps" },
  { name: "MySQL", value: 85, category: "数据库" },
  { name: "Redis", value: 80, category: "数据库" },
  { name: "UI设计", value: 72, category: "设计" },
  { name: "产品管理", value: 68, category: "管理" }
]

const DEFAULT_REVENUE_TREND = [
  { month: "1月", revenue: 180000, cost: 120000 },
  { month: "2月", revenue: 210000, cost: 135000 },
  { month: "3月", revenue: 240000, cost: 150000 },
  { month: "4月", revenue: 285000, cost: 165000 },
  { month: "5月", revenue: 320000, cost: 180000 },
  { month: "6月", revenue: 350000, cost: 195000 },
  { month: "7月", revenue: 380000, cost: 210000 },
  { month: "8月", revenue: 410000, cost: 225000 }
]

export type ExpertGrowthRow = { month: string; experts: number; growth: number }
export type ProjectStatusRow = { value: number; name: string; color: string }
export type SkillHeatRow = { name: string; value: number; category: string }
export type RevenueTrendRow = { month: string; revenue: number; cost: number }

export function getExpertGrowthChartOption(data?: ExpertGrowthRow[]): EChartsOption {
  const rows = data ?? DEFAULT_EXPERT_GROWTH
  return {
    tooltip: { trigger: "axis" },
    legend: { data: ["专家数量", "增长率(%)"], bottom: 0 },
    grid: { left: "3%", right: "4%", bottom: "12%", containLabel: true },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: rows.map((r) => r.month)
    },
    yAxis: [
      { type: "value", name: "人数", splitLine: { lineStyle: { type: "dashed" } } },
      { type: "value", name: "增长率", splitLine: { show: false }, axisLabel: { formatter: "{value}%" } }
    ],
    series: [
      {
        name: "专家数量",
        type: "line",
        smooth: true,
        data: rows.map((r) => r.experts),
        itemStyle: { color: "#409EFF" },
        areaStyle: { color: "rgba(64, 158, 255, 0.15)" }
      },
      {
        name: "增长率(%)",
        type: "line",
        yAxisIndex: 1,
        smooth: true,
        data: rows.map((r) => r.growth),
        itemStyle: { color: "#67C23A" }
      }
    ]
  }
}

export function getProjectStatusChartOption(data?: ProjectStatusRow[]): EChartsOption {
  const rows = data ?? DEFAULT_PROJECT_STATUS
  return {
    tooltip: { trigger: "item", formatter: "{b}: {c} ({d}%)" },
    legend: { bottom: 0 },
    series: [
      {
        name: "项目状态",
        type: "pie",
        radius: ["40%", "65%"],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 4, borderColor: "#fff", borderWidth: 2 },
        label: { show: true },
        data: rows.map((r) => ({ value: r.value, name: r.name, itemStyle: { color: r.color } }))
      }
    ]
  }
}

export function getSkillHeatChartOption(data?: SkillHeatRow[]): EChartsOption {
  const rows = data ?? DEFAULT_SKILL_HEAT
  return {
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" },
      formatter: (params: unknown) => {
        const p = Array.isArray(params) ? params[0] : params
        if (!p || typeof p !== "object" || !("name" in p) || !("value" in p)) return ""
        const row = rows.find((r) => r.name === (p as { name: string }).name)
        const cat = row?.category ?? ""
        return `${(p as { name: string }).name}<br/>热度: ${(p as { value: number }).value}${cat ? `<br/>分类: ${cat}` : ""}`
      }
    },
    grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
    xAxis: {
      type: "category",
      data: rows.map((r) => r.name),
      axisLabel: { rotate: 30, interval: 0, fontSize: 11 }
    },
    yAxis: { type: "value", name: "热度", splitLine: { lineStyle: { type: "dashed" } } },
    series: [
      {
        name: "技能热度",
        type: "bar",
        data: rows.map((r) => r.value),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "#79bbff" },
            { offset: 1, color: "#409EFF" }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  }
}

export function getRevenueTrendChartOption(data?: RevenueTrendRow[]): EChartsOption {
  const rows = data ?? DEFAULT_REVENUE_TREND
  return {
    tooltip: { trigger: "axis" },
    legend: { data: ["收入", "成本"], bottom: 0 },
    grid: { left: "3%", right: "4%", bottom: "12%", containLabel: true },
    xAxis: { type: "category", boundaryGap: false, data: rows.map((r) => r.month) },
    yAxis: {
      type: "value",
      name: "金额(元)",
      axisLabel: { formatter: (v: number) => `¥${(v / 10000).toFixed(0)}万` },
      splitLine: { lineStyle: { type: "dashed" } }
    },
    series: [
      {
        name: "收入",
        type: "line",
        smooth: true,
        areaStyle: { color: "rgba(64, 158, 255, 0.25)" },
        data: rows.map((r) => r.revenue),
        itemStyle: { color: "#409EFF" }
      },
      {
        name: "成本",
        type: "line",
        smooth: true,
        areaStyle: { color: "rgba(230, 162, 60, 0.2)" },
        data: rows.map((r) => r.cost),
        itemStyle: { color: "#E6A23C" }
      }
    ]
  }
}

export interface RadarSkillCategory {
  name: string
  level: string
  skills: { name: string; level: string }[]
}

function levelToScore(level: string): number {
  switch (level) {
    case "精通":
      return 100
    case "熟练":
      return 75
    case "了解":
      return 50
    default:
      return 25
  }
}

/** 与专家详情页雷达图一致的聚合逻辑，便于复用或后续替换内联配置 */
export function getExpertSkillRadarOption(categories: RadarSkillCategory[]): EChartsOption {
  const indicator = categories.map((c) => ({ name: c.name, max: 100 }))
  const values = categories.map((c) => {
    const scores = c.skills.map((s) => levelToScore(s.level))
    return scores.reduce((a, b) => a + b, 0) / scores.length
  })
  return {
    tooltip: { trigger: "item" },
    radar: {
      indicator,
      shape: "circle",
      splitNumber: 5,
      axisName: { color: "#606266", fontSize: 12 },
      splitLine: {
        lineStyle: { color: ["#e4e7ed", "#dcdfe6", "#c0c4cc", "#b1b3b8", "#a6a9ad"] }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ["rgba(250, 250, 250, 0.8)", "rgba(245, 245, 245, 0.8)", "rgba(240, 240, 240, 0.8)"]
        }
      },
      axisLine: { lineStyle: { color: "#dcdfe6" } }
    },
    series: [
      {
        name: "技能分布",
        type: "radar",
        data: [
          {
            value: values,
            name: "技能评分",
            symbol: "circle",
            symbolSize: 8,
            lineStyle: { width: 3, color: "#409EFF" },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "rgba(64, 158, 255, 0.8)" },
                { offset: 1, color: "rgba(64, 158, 255, 0.2)" }
              ])
            },
            itemStyle: { color: "#409EFF", borderColor: "#fff", borderWidth: 2 }
          }
        ]
      }
    ]
  }
}

export function initChart(el: HTMLElement, option: EChartsOption): ECharts {
  const chart = echarts.init(el)
  chart.setOption(option)
  return chart
}

export function resizeChart(chart: ECharts): void {
  chart.resize()
}

export function disposeChart(chart: ECharts): void {
  chart.dispose()
}

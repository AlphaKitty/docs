<template>
  <div class="expert-profile" v-loading="loading">
    <div class="page-header">
      <div class="left">
        <el-button text @click="goBack">返回</el-button>
        <h2>专家详情</h2>
      </div>
    </div>

    <el-card v-if="expert" class="profile-card">
      <div class="profile-main">
        <el-avatar :size="88" :src="expert.avatar" />
        <div class="profile-info">
          <div class="name-row">
            <span class="name">{{ expert.name }}</span>
            <el-tag :type="getStatusType(expert.status)">{{ getStatusText(expert.status) }}</el-tag>
          </div>
          <div class="meta">{{ expert.title || '未填写职位' }} · {{ expert.company || '未填写公司' }}</div>
          <div class="meta">经验：{{ expert.experience }} 年 · 评分：{{ Number(expert.rating || 0).toFixed(1) }}</div>
        </div>
      </div>
    </el-card>

    <el-row v-if="expert" :gutter="16">
      <el-col :span="16">
        <el-card>
          <template #header><span>技能与领域</span></template>
          <div class="section">
            <div class="label">技能</div>
            <div>
              <el-tag v-for="skill in expert.skills" :key="skill" class="tag">{{ skill }}</el-tag>
              <span v-if="expert.skills.length === 0" class="empty-text">暂无技能数据</span>
            </div>
          </div>
          <div class="section">
            <div class="label">领域</div>
            <div>
              <el-tag
                v-for="domain in expert.domains || []"
                :key="domain"
                class="tag"
                type="info"
              >
                {{ domain }}
              </el-tag>
              <span v-if="!(expert.domains || []).length" class="empty-text">未分配领域</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span>联系信息</span></template>
          <div class="contact-item">
            <div class="contact-label">邮箱</div>
            <div>{{ expert.email || '-' }}</div>
          </div>
          <div class="contact-item">
            <div class="contact-label">电话</div>
            <div>{{ expert.phone || '-' }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && !expert" description="未找到该专家" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useExpertStore } from '@/stores/expert'

const route = useRoute()
const router = useRouter()
const expertStore = useExpertStore()
const loading = ref(false)

const expertId = computed(() => Number(route.params.id || 0))
const expert = computed(() => expertStore.experts.find((item) => item.id === expertId.value))

const getStatusType = (status: string) => {
  switch (status) {
    case 'available':
      return 'success'
    case 'busy':
      return 'warning'
    case 'unavailable':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'available':
      return '可用'
    case 'busy':
      return '忙碌'
    case 'unavailable':
      return '不可用'
    default:
      return '未知'
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(async () => {
  if (expert.value || !expertId.value) return
  loading.value = true
  try {
    await expertStore.fetchExperts()
  } catch {
    ElMessage.error('加载专家详情失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.expert-profile {
  padding: 20px;
}

.page-header {
  margin-bottom: 16px;
}

.left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.left h2 {
  margin: 0;
  color: #303133;
}

.profile-card {
  margin-bottom: 16px;
}

.profile-main {
  display: flex;
  align-items: center;
  gap: 16px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.meta {
  margin-top: 6px;
  color: #606266;
}

.section {
  margin-bottom: 14px;
}

.label {
  margin-bottom: 8px;
  font-size: 13px;
  color: #909399;
}

.tag {
  margin-right: 6px;
  margin-bottom: 6px;
}

.empty-text {
  color: #909399;
  font-size: 13px;
}

.contact-item {
  margin-bottom: 12px;
}

.contact-label {
  margin-bottom: 4px;
  color: #909399;
  font-size: 13px;
}
</style>

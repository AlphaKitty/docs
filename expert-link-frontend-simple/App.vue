<template>
  <div id="app">
    <el-container class="app-container">
      <!-- 顶部导航栏 -->
      <el-header class="app-header">
        <div class="header-left">
          <h1 class="app-title">Expert Link 专家管理系统</h1>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" :src="userAvatar" />
              <span class="user-name">{{ userName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item>设置</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <!-- 侧边菜单栏 -->
        <el-aside width="200px" class="app-sidebar">
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            router
          >
            <el-menu-item index="/">
              <el-icon><House /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            
            <el-sub-menu index="experts">
              <template #title>
                <el-icon><User /></el-icon>
                <span>专家管理</span>
              </template>
              <el-menu-item index="/experts">专家列表</el-menu-item>
              <el-menu-item index="/experts/add">添加专家</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="projects">
              <template #title>
                <el-icon><Document /></el-icon>
                <span>项目管理</span>
              </template>
              <el-menu-item index="/projects">项目列表</el-menu-item>
              <el-menu-item index="/projects/add">新建项目</el-menu-item>
            </el-sub-menu>
            
            <el-menu-item index="/skills">
              <el-icon><Collection /></el-icon>
              <span>技能领域</span>
            </el-menu-item>
            
            <el-menu-item index="/analysis">
              <el-icon><DataAnalysis /></el-icon>
              <span>统计分析</span>
            </el-menu-item>
            
            <el-menu-item index="/settings">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主内容区域 -->
        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  House,
  User,
  Document,
  Collection,
  DataAnalysis,
  Setting
} from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const userName = computed(() => userStore.userInfo.name)
const userAvatar = computed(() => userStore.userInfo.avatar)
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #409EFF 0%, #337ecc 100%);
  color: white;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left .app-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.header-right .user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: white;
}

.user-name {
  margin-left: 8px;
  font-size: 14px;
}

.app-sidebar {
  background-color: #f8f9fa;
  border-right: 1px solid #e4e7ed;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.app-main {
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}
</style>
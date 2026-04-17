#!/usr/bin/env node

/**
 * Expert Link 批量API集成转换脚本
 * 自动转换所有Vue页面组件为API驱动版本
 */

const fs = require('fs');
const path = require('path');

// 配置
const CONFIG = {
  viewsDir: path.join(__dirname, 'views'),
  backupDir: path.join(__dirname, 'views-backup'),
  outputDir: path.join(__dirname, 'views-api-integrated'),
  
  // API服务映射
  apiServices: {
    'Dashboard.vue': 'StatsService',
    'ExpertList.vue': 'ExpertService',
    'ExpertDetail.vue': 'ExpertService',
    'ExpertAdd.vue': 'ExpertService',
    'ExpertEdit.vue': 'ExpertService',
    'ProjectList.vue': 'ProjectService',
    'ProjectDetail.vue': 'ProjectService',
    'ProjectAdd.vue': 'ProjectService',
    'ProjectEdit.vue': 'ProjectService',
    'SkillList.vue': 'SkillService',
    'SkillDetail.vue': 'SkillService',
    'SkillAdd.vue': 'SkillService',
    'SkillEdit.vue': 'SkillService',
    'DomainList.vue': 'DomainService',
    'DomainDetail.vue': 'DomainService',
    'DomainAdd.vue': 'DomainService',
    'DomainEdit.vue': 'DomainService'
  },
  
  // 页面类型映射
  pageTypes: {
    'List': 'list',
    'Detail': 'detail',
    'Add': 'form',
    'Edit': 'form',
    'Dashboard': 'dashboard'
  }
};

// 转换模板
const TEMPLATES = {
  // 导入语句模板
  imports: {
    stats: `import { StatsService } from '@/api/services';
import type { DashboardStats, ExpertStats, ProjectStats, SkillStats } from '@/api/types';`,
    
    expert: `import { ExpertService } from '@/api/services';
import type { Expert, ExpertQueryParams } from '@/api/types';`,
    
    project: `import { ProjectService } from '@/api/services';
import type { Project, ProjectQueryParams } from '@/api/types';`,
    
    skill: `import { SkillService } from '@/api/services';
import type { Skill, SkillQueryParams } from '@/api/types';`,
    
    domain: `import { DomainService } from '@/api/services';
import type { Domain, DomainQueryParams } from '@/api/types';`
  },
  
  // 数据定义模板
  data: {
    list: `const items = ref<ItemType[]>([]);
const loading = ref(false);
const total = ref(0);
const queryParams = ref<QueryParamsType>({
  page: 1,
  pageSize: 10,
  search: '',
  sortField: 'id',
  sortOrder: 'desc'
});`,
    
    detail: `const item = ref<ItemType | null>(null);
const loading = ref(false);
const id = ref<string>('');`,
    
    form: `const formData = ref<FormDataType>({
  // 表单字段初始值
});
const loading = ref(false);
const submitting = ref(false);
const rules = {
  // 验证规则
};`,
    
    dashboard: `const dashboardStats = ref<DashboardStats>({
  totalExperts: 0,
  activeExperts: 0,
  totalProjects: 0,
  ongoingProjects: 0,
  totalSkills: 0,
  totalDomains: 0,
  monthlyRevenue: 0,
  utilizationRate: 0
});

const expertStats = ref<ExpertStats>({
  byStatus: { ACTIVE: 0, INACTIVE: 0, PENDING: 0, ARCHIVED: 0 },
  byLevel: { JUNIOR: 0, MIDDLE: 0, SENIOR: 0, EXPERT: 0 },
  byDomain: [],
  bySkill: [],
  ratingDistribution: [],
  hourlyRateDistribution: []
});

const loading = ref(false);`
  },
  
  // 方法模板
  methods: {
    list: `// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const response = await Service.getList(queryParams.value);
    items.value = response.data;
    total.value = response.total;
  } catch (error) {
    console.error('加载数据失败:', error);
    ElMessage.error('加载数据失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  queryParams.value.page = 1;
  loadData();
};

// 重置搜索
const handleReset = () => {
  queryParams.value = {
    page: 1,
    pageSize: 10,
    search: '',
    sortField: 'id',
    sortOrder: 'desc'
  };
  loadData();
};

// 分页变化
const handlePageChange = (page: number) => {
  queryParams.value.page = page;
  loadData();
};

// 删除项目
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除吗？', '提示', {
      type: 'warning'
    });
    
    await Service.deleteItem(id);
    ElMessage.success('删除成功');
    loadData();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error('删除失败');
    }
  }
};`,
    
    detail: `// 加载详情
const loadDetail = async () => {
  if (!id.value) return;
  
  loading.value = true;
  try {
    const data = await Service.getById(id.value);
    item.value = data;
  } catch (error) {
    console.error('加载详情失败:', error);
    ElMessage.error('加载详情失败');
  } finally {
    loading.value = false;
  }
};

// 初始化
onMounted(() => {
  const route = useRoute();
  id.value = route.params.id as string;
  loadDetail();
});`,
    
    form: `// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    
    submitting.value = true;
    
    if (isEditMode.value) {
      // 编辑模式
      await Service.update(id.value, formData.value);
      ElMessage.success('更新成功');
    } else {
      // 添加模式
      await Service.create(formData.value);
      ElMessage.success('添加成功');
    }
    
    // 返回列表页
    router.push('/items');
  } catch (error) {
    console.error('提交失败:', error);
    if (error !== 'cancel') {
      ElMessage.error('提交失败');
    }
  } finally {
    submitting.value = false;
  }
};

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields();
};

// 取消
const handleCancel = () => {
  router.push('/items');
};`,
    
    dashboard: `// 加载仪表板数据
const loadDashboardData = async () => {
  loading.value = true;
  try {
    const [dashboardData, expertData, projectData, skillData] = await Promise.all([
      StatsService.getDashboardStats(),
      StatsService.getExpertStats(),
      StatsService.getProjectStats(),
      StatsService.getSkillStats()
    ]);
    
    dashboardStats.value = dashboardData;
    expertStats.value = expertData;
    projectStats.value = projectData;
    skillStats.value = skillData;
    
    ElMessage.success('数据加载成功');
  } catch (error) {
    console.error('加载仪表板数据失败:', error);
    ElMessage.error('数据加载失败');
  } finally {
    loading.value = false;
  }
};

// 刷新数据
const refreshData = () => {
  loadDashboardData();
};

// 初始化
onMounted(() => {
  loadDashboardData();
});`
  }
};

class BatchApiTransformer {
  constructor() {
    this.stats = {
      total: 0,
      processed: 0,
      success: 0,
      failed: 0,
      skipped: 0
    };
  }

  // 备份原始文件
  backupFiles() {
    console.log('📦 备份原始文件...');
    
    if (!fs.existsSync(CONFIG.backupDir)) {
      fs.mkdirSync(CONFIG.backupDir, { recursive: true });
    }
    
    const files = fs.readdirSync(CONFIG.viewsDir);
    files.forEach(file => {
      if (file.endsWith('.vue')) {
        const source = path.join(CONFIG.viewsDir, file);
        const backup = path.join(CONFIG.backupDir, file);
        fs.copyFileSync(source, backup);
      }
    });
    
    console.log(`✅ 备份完成，共备份 ${files.filter(f => f.endsWith('.vue')).length} 个文件`);
  }

  // 创建输出目录
  createOutputDir() {
    if (!fs.existsSync(CONFIG.outputDir)) {
      fs.mkdirSync(CONFIG.outputDir, { recursive: true });
    }
  }

  // 获取页面类型
  getPageType(fileName) {
    for (const [key, type] of Object.entries(CONFIG.pageTypes)) {
      if (fileName.includes(key)) {
        return type;
      }
    }
    return 'list';
  }

  // 获取服务类型
  getServiceType(fileName) {
    for (const [file, service] of Object.entries(CONFIG.apiServices)) {
      if (fileName === file) {
        return service.replace('Service', '').toLowerCase();
      }
    }
    return 'expert';
  }

  // 读取文件
  readFile(filePath) {
    return fs.readFileSync(filePath, 'utf-8');
  }

  // 写入文件
  writeFile(filePath, content) {
    fs.writeFileSync(filePath, content, 'utf-8');
  }

  // 转换单个文件
  transformFile(fileName) {
    console.log(`\n🔄 处理: ${fileName}`);
    
    const filePath = path.join(CONFIG.viewsDir, fileName);
    const outputPath = path.join(CONFIG.outputDir, fileName);
    
    try {
      let content = this.readFile(filePath);
      
      // 获取页面信息
      const pageType = this.getPageType(fileName);
      const serviceType = this.getServiceType(fileName);
      const serviceName = CONFIG.apiServices[fileName];
      
      if (!serviceName) {
        console.log(`  ⚠️  跳过: 未找到API服务映射`);
        this.stats.skipped++;
        return false;
      }
      
      // 应用转换
      content = this.applyTransformations(content, fileName, pageType, serviceType, serviceName);
      
      // 写入文件
      this.writeFile(outputPath, content);
      
      console.log(`  ✅ 转换成功`);
      this.stats.success++;
      return true;
      
    } catch (error) {
      console.error(`  ❌ 转换失败:`, error.message);
      this.stats.failed++;
      return false;
    }
  }

  // 应用转换
  applyTransformations(content, fileName, pageType, serviceType, serviceName) {
    let transformed = content;
    
    // 1. 添加API导入
    transformed = this.addApiImports(transformed, serviceType, pageType);
    
    // 2. 替换数据定义
    transformed = this.replaceDataDefinitions(transformed, pageType, serviceType);
    
    // 3. 添加方法
    transformed = this.addMethods(transformed, pageType, serviceName);
    
    // 4. 更新模板
    transformed = this.updateTemplate(transformed, pageType);
    
    // 5. 添加生命周期钩子
    transformed = this.addLifecycleHooks(transformed, pageType);
    
    return transformed;
  }

  // 添加API导入
  addApiImports(content, serviceType, pageType) {
    // 添加Element Plus消息组件
    if (!content.includes("import { ElMessage")) {
      const importMatch = content.match(/import\s+{([^}]+)}\s+from\s+['"]@?element-plus['"]/);
      if (importMatch) {
        const newImport = importMatch[0].replace('}', ', ElMessage, ElMessageBox }');
        content = content.replace(importMatch[0], newImport);
      } else {
        // 在script标签后添加
        const scriptIndex = content.indexOf('<script');
        if (scriptIndex !== -1) {
          const scriptEndIndex = content.indexOf('>', scriptIndex) + 1;
          const importStatement = `\nimport { ElMessage, ElMessageBox } from 'element-plus';`;
          content = content.slice(0, scriptEndIndex) + importStatement + content.slice(scriptEndIndex);
        }
      }
    }
    
    // 添加路由导入
    if (pageType === 'detail' || pageType === 'form') {
      if (!content.includes("import { useRoute")) {
        const vueImportMatch = content.match(/import\s+{([^}]+)}\s+from\s+['"]vue['"]/);
        if (vueImportMatch) {
          const newImport = vueImportMatch[0].replace('}', ', useRoute }');
          content = content.replace(vueImportMatch[0], newImport);
        }
      }
      
      if (!content.includes("import { useRouter")) {
        const vueRouterMatch = content.match(/import\s+{([^}]+)}\s+from\s+['"]vue-router['"]/);
        if (vueRouterMatch) {
          const newImport = vueRouterMatch[0].replace('}', ', useRouter }');
          content = content.replace(vueRouterMatch[0], newImport);
        } else {
          // 添加vue-router导入
          const scriptIndex = content.indexOf('<script');
          if (scriptIndex !== -1) {
            const scriptEndIndex = content.indexOf('>', scriptIndex) + 1;
            const importStatement = `\nimport { useRoute, useRouter } from 'vue-router';`;
            content = content.slice(0, scriptEndIndex) + importStatement + content.slice(scriptEndIndex);
          }
        }
      }
    }
    
    // 添加API服务导入
    const apiImport = TEMPLATES.imports[serviceType];
    if (apiImport && !content.includes(serviceName)) {
      // 在最后一个导入后添加
      const importPattern = /import\s+.*\s+from\s+['"][^'"]+['"]/g;
      const imports = content.match(importPattern) || [];
      
      if (imports.length > 0) {
        const lastImport = imports[imports.length - 1];
        const lastImportIndex = content.lastIndexOf(lastImport) + lastImport.length;
        content = content.slice(0, lastImportIndex) + '\n' + apiImport + content.slice(lastImportIndex);
      }
    }
    
    return content;
  }

  // 替换数据定义
  replaceDataDefinitions(content, pageType, serviceType) {
    const dataTemplate = TEMPLATES.data[pageType];
    
    if (!dataTemplate) {
      return content;
    }
    
    // 查找硬编码的数据定义
    const dataPatterns = [
      /const\s+\w+\s*=\s*ref\s*\(\s*\[[\s\S]*?\]\s*\)/g, // 数组数据
      /const\s+\w+\s*=\s*ref\s*\(\s*\{[\s\S]*?\}\s*\)/g, // 对象数据
      /const\s+\w+\s*=\s*\[[\s\S]*?\]/g, // 普通数组
      /const\s+\w+\s*=\s*\{[\s\S]*?\}/g  // 普通对象
    ];
    
    let found = false;
    for (const pattern of dataPatterns) {
      const matches = content.match(pattern) || [];
      for (const match of matches) {
        // 检查是否是硬编码数据（包含具体值而不是初始值）
        if (this.isHardcodedData(match)) {
          // 替换为API数据定义
          const replacement = dataTemplate
            .replace(/ItemType/g, this.getTypeName(serviceType))
            .replace(/QueryParamsType/g, `${this.getTypeName(serviceType)}QueryParams`)
            .replace(/FormDataType/g, `${this.getTypeName(serviceType)}FormData`)
            .replace(/Service/g, `${this.capitalize(serviceType)}Service`);
          
          content = content.replace(match, replacement);
          found = true;
          break;
        }
      }
      if (found) break;
    }
    
    return content;
  }

  // 检查是否是硬编码数据
  isHardcodedData(dataString) {
    // 包含具体值而不是初始值
    const hardcodedPatterns = [
      /id:\s*\d+/,
      /name:\s*['"][^'"]+['"]/,
      /status:\s*['"][^'"]+['"]/,
      /createdAt:\s*['"][^'"]+['"]/,
      /\d+,/,
      /['"][^'"]+['"],/
    ];
    
    return hardcodedPatterns.some(pattern => pattern.test(dataString));
  }

  // 获取类型名称
  getTypeName(serviceType) {
    return this.capitalize(serviceType);
  }

  // 首字母大写
  capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
  }

  // 添加方法
  addMethods(content, pageType, serviceName) {
    const methodsTemplate = TEMPLATES.methods[pageType];
    
    if (!methodsTemplate) {
      return content;
    }
    
    // 替换服务名称
    const methods = methodsTemplate.replace(/Service/g, serviceName);
    
    // 查找现有的方法定义
    const methodPattern = /const\s+\w+\s*=\s*\([^)]*\)\s*=>\s*\{[\s\S]*?\}/g;
    const existingMethods = content.match(methodPattern) || [];
    
    if (existingMethods.length > 0) {
      // 在最后一个方法后添加
      const lastMethod = existingMethods[existingMethods.length - 1];
      const lastMethodIndex = content.lastIndexOf(lastMethod) + lastMethod.length;
      content = content.slice(0, lastMethodIndex) + '\n\n' + methods + content.slice(lastMethodIndex);
    } else {
      // 在数据定义后添加
      const dataPattern = /const\s+\w+\s*=\s*ref.*?;/g;
      const dataMatches = content.match(dataPattern) || [];
      
      if (dataMatches.length > 0) {
        const lastData = dataMatches[dataMatches.length - 1];
        const lastDataIndex = content.lastIndexOf(lastData) + lastData.length;
        content = content.slice(0, lastDataIndex) + '\n\n' + methods + content.slice(lastDataIndex);
      }
    }
    
    return content;
  }

  // 更新模板
  updateTemplate(content, pageType) {
    // 这里可以添加模板更新逻辑
    // 例如：添加加载状态、空状态等
    return content;
  }

  // 添加生命周期钩子
  addLifecycleHooks(content, pageType) {
    if (pageType === 'list' || pageType === 'dashboard') {
      // 添加onMounted钩子
      if (!content.includes('onMounted')) {
        // 在script标签中查找合适的位置
        const scriptContent = this.getScriptContent(content);
        if (scriptContent) {
          // 在最后添加onMounted
          const onMountedHook = `\n\nonMounted(() => {
  loadData();
});`;
          
          const newScriptContent = scriptContent + onMountedHook;
          content = content.replace(scriptContent, newScriptContent);
        }
      }
    }
    
    return content;
  }

  // 获取script内容
  getScriptContent(content) {
    const scriptStart = content.indexOf('<script');
    if (scriptStart === -1) return null;
    
    const scriptEnd = content.indexOf('</script>', scriptStart);
    if (scriptEnd === -1) return null;
    
    const scriptTagEnd = content.indexOf('>', scriptStart) + 1;
    return content.substring(scriptTagEnd, scriptEnd).trim();
  }

  // 运行转换
  run() {
    console.log('🚀 Expert Link API集成批量转换');
    console.log('='.repeat(50));
    
    // 备份文件
    this.backupFiles();
    
    // 创建输出目录
    this.createOutputDir();
    
    // 获取所有Vue文件
    const files = fs.readdirSync(CONFIG.viewsDir)
      .filter(file => file.endsWith('.vue'))
      .sort();
    
    this.stats.total = files.length;
    
    console.log(`\n📁 发现 ${files.length} 个Vue文件`);
    console.log('开始转换...\n');
    
    // 转换每个文件
    files.forEach(file => {
      this.transformFile(file);
      this.stats.processed++;
    });
    
    // 输出统计
    this.printStats();
    
    // 生成转换报告
    this.generateReport();
  }

  // 打印统计
  printStats() {
    console.log('\n' + '='.repeat(50));
    console.log('📊 转换统计:');
    console.log(`   总文件数: ${this.stats.total}`);
    console.log(`   已处理: ${this.stats.processed}`);
    console.log(`   成功: ${this.stats.success}`);
    console.log(`   失败: ${this.stats.failed}`);
    console.log(`   跳过: ${this.stats.skipped}`);
    
    if (this.stats.failed === 0) {
      console.log('\n✅ 批量转换完成！');
      console.log(`\n转换后的文件保存在: ${CONFIG.outputDir}`);
    } else {
      console.log('\n⚠️  部分文件转换失败，请检查错误信息');
    }
  }

  // 生成报告
  generateReport() {
    const reportPath = path.join(__dirname, 'api-integration-report.md');
    const reportContent = `# API集成转换报告

## 转换统计
- 总文件数: ${this.stats.total}
- 成功转换: ${this.stats.success}
- 转换失败: ${this.stats.failed}
- 跳过文件: ${this.stats.skipped}

## 转换详情

### 成功转换的文件
${this.getFileList('success')}

### 转换失败的文件
${this.getFileList('failed')}

### 跳过的文件
${this.getFileList('skipped')}

## 下一步操作

1. **检查转换结果**
   - 查看 ${CONFIG.outputDir} 目录中的转换后文件
   - 验证API导入是否正确
   - 测试数据加载功能

2. **运行测试**
   \`\`\`bash
   # 启动后端服务
   cd expert-link-backend
   ./mvnw spring-boot:run
   
   # 启动前端开发服务器
   cd ..
   npm run dev
   \`\`\`

3. **验证功能**
   - 访问 http://localhost:3000
   - 测试各个页面的数据加载
   - 验证CRUD操作

## 注意事项

1. **类型定义**: 确保TypeScript类型定义与后端API响应匹配
2. **错误处理**: 检查错误处理逻辑是否完善
3. **加载状态**: 验证加载状态显示是否正确
4. **空状态**: 测试数据为空时的显示效果

## 技术支持

如遇到问题，请参考:
- API集成指南: API_INTEGRATION_GUIDE.md
- 后端API文档: http://localhost:8080/swagger-ui.html
- 前端开发文档: README.md
`;

    this.writeFile(reportPath, reportContent);
    console.log(`\n📄 转换报告已生成: ${reportPath}`);
  }

  // 获取文件列表
  getFileList(type) {
    // 这里简化处理，实际应该记录每个文件的状态
    const count = this.stats[type];
    if (count === 0) {
      return '无';
    }
    
    return `共 ${count} 个文件`;
  }
}

// 运行脚本
const transformer = new BatchApiTransformer();
transformer.run();
#!/usr/bin/env node

/**
 * Expert Link 前端API集成转换脚本
 * 将静态原型转换为动态API驱动应用
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// 配置
const CONFIG = {
  // 源目录
  viewsDir: path.join(__dirname, 'views'),
  // 备份目录
  backupDir: path.join(__dirname, 'views-backup'),
  // 输出目录
  outputDir: path.join(__dirname, 'views-api-integrated'),
  // API服务映射
  apiServices: {
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
    'DomainEdit.vue': 'DomainService',
    'Dashboard.vue': 'StatsService'
  }
};

// 工具函数
class ApiIntegrationTransformer {
  constructor() {
    this.stats = {
      totalFiles: 0,
      processedFiles: 0,
      successFiles: 0,
      failedFiles: 0
    };
  }

  // 备份原始文件
  backupOriginalFiles() {
    console.log('📦 备份原始文件...');
    
    if (!fs.existsSync(CONFIG.backupDir)) {
      fs.mkdirSync(CONFIG.backupDir, { recursive: true });
    }
    
    const files = fs.readdirSync(CONFIG.viewsDir);
    files.forEach(file => {
      if (file.endsWith('.vue')) {
        const sourcePath = path.join(CONFIG.viewsDir, file);
        const backupPath = path.join(CONFIG.backupDir, file);
        fs.copyFileSync(sourcePath, backupPath);
        console.log(`  ✓ 备份: ${file}`);
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

  // 读取文件内容
  readFile(filePath) {
    return fs.readFileSync(filePath, 'utf-8');
  }

  // 写入文件内容
  writeFile(filePath, content) {
    fs.writeFileSync(filePath, content, 'utf-8');
  }

  // 转换单个文件
  transformFile(fileName) {
    const filePath = path.join(CONFIG.viewsDir, fileName);
    const outputPath = path.join(CONFIG.outputDir, fileName);
    
    console.log(`\n🔄 处理文件: ${fileName}`);
    
    try {
      let content = this.readFile(filePath);
      
      // 根据文件名确定使用的API服务
      const apiService = CONFIG.apiServices[fileName];
      if (!apiService) {
        console.log(`  ⚠️  未找到API服务映射: ${fileName}`);
        return false;
      }

      // 应用转换规则
      content = this.applyTransformations(content, fileName, apiService);
      
      // 写入转换后的文件
      this.writeFile(outputPath, content);
      
      console.log(`  ✅ 转换完成: ${fileName}`);
      this.stats.successFiles++;
      return true;
      
    } catch (error) {
      console.error(`  ❌ 转换失败: ${fileName}`, error.message);
      this.stats.failedFiles++;
      return false;
    }
  }

  // 应用转换规则
  applyTransformations(content, fileName, apiService) {
    let transformed = content;
    
    // 1. 添加API导入
    transformed = this.addApiImport(transformed, apiService);
    
    // 2. 替换硬编码数据为API调用
    transformed = this.replaceHardcodedData(transformed, fileName);
    
    // 3. 添加加载状态
    transformed = this.addLoadingState(transformed);
    
    // 4. 添加错误处理
    transformed = this.addErrorHandling(transformed);
    
    // 5. 更新方法调用
    transformed = this.updateMethodCalls(transformed, fileName);
    
    return transformed;
  }

  // 添加API导入
  addApiImport(content, apiService) {
    const importPattern = /import\s+.*\s+from\s+['"][^'"]+['"]/g;
    const imports = content.match(importPattern) || [];
    
    // 检查是否已导入API服务
    const hasApiImport = imports.some(imp => imp.includes(apiService));
    
    if (!hasApiImport) {
      // 在最后一个导入后添加API导入
      const lastImport = imports[imports.length - 1];
      const apiImport = `import { ${apiService} } from '@/api/services';`;
      
      if (lastImport) {
        const lastImportIndex = content.lastIndexOf(lastImport) + lastImport.length;
        content = content.slice(0, lastImportIndex) + '\n' + apiImport + content.slice(lastImportIndex);
      } else {
        // 如果没有导入语句，在script标签后添加
        const scriptIndex = content.indexOf('<script');
        if (scriptIndex !== -1) {
          const scriptEndIndex = content.indexOf('>', scriptIndex) + 1;
          content = content.slice(0, scriptEndIndex) + '\n' + apiImport + content.slice(scriptEndIndex);
        }
      }
    }
    
    return content;
  }

  // 替换硬编码数据
  replaceHardcodedData(content, fileName) {
    // 根据文件名应用不同的替换规则
    if (fileName === 'Dashboard.vue') {
      return this.transformDashboard(content);
    } else if (fileName.includes('Expert')) {
      return this.transformExpertPages(content, fileName);
    } else if (fileName.includes('Project')) {
      return this.transformProjectPages(content, fileName);
    } else if (fileName.includes('Skill')) {
      return this.transformSkillPages(content, fileName);
    } else if (fileName.includes('Domain')) {
      return this.transformDomainPages(content, fileName);
    }
    
    return content;
  }

  // 转换Dashboard页面
  transformDashboard(content) {
    // 这里实现Dashboard页面的具体转换逻辑
    // 由于Dashboard.vue已经单独处理，这里返回原内容
    return content;
  }

  // 转换专家相关页面
  transformExpertPages(content, fileName) {
    // 示例：替换专家列表的硬编码数据
    if (fileName === 'ExpertList.vue') {
      // 查找硬编码的专家数据
      const hardcodedDataPattern = /const\s+experts\s*=\s*ref.*?\[[\s\S]*?\]/;
      const match = content.match(hardcodedDataPattern);
      
      if (match) {
        const replacement = `const experts = ref<Expert[]>([]);
const loading = ref(false);
const total = ref(0);
const queryParams = ref<ExpertQueryParams>({
  page: 1,
  pageSize: 10,
  name: '',
  status: '',
  domainId: undefined,
  skillIds: []
});

// 加载专家数据
const loadExperts = async () => {
  loading.value = true;
  try {
    const response = await ExpertService.getExperts(queryParams.value);
    experts.value = response.data;
    total.value = response.total;
  } catch (error) {
    console.error('加载专家数据失败:', error);
    ElMessage.error('加载专家数据失败');
  } finally {
    loading.value = false;
  }
};`;
        
        content = content.replace(hardcodedDataPattern, replacement);
      }
    }
    
    return content;
  }

  // 添加加载状态
  addLoadingState(content) {
    // 在data部分添加loading状态
    const dataPattern = /const\s+(\w+)\s*=\s*ref.*?;/g;
    const loadingVar = 'const loading = ref(false);';
    
    // 检查是否已有loading
    if (!content.includes('loading = ref')) {
      // 在第一个ref声明后添加loading
      const firstRefMatch = content.match(dataPattern);
      if (firstRefMatch) {
        const firstRefIndex = content.indexOf(firstRefMatch[0]) + firstRefMatch[0].length;
        content = content.slice(0, firstRefIndex) + '\n' + loadingVar + content.slice(firstRefIndex);
      }
    }
    
    return content;
  }

  // 添加错误处理
  addErrorHandling(content) {
    // 确保导入了ElMessage
    if (!content.includes("import { ElMessage } from 'element-plus'")) {
      const importPattern = /import\s+.*\s+from\s+['"]@?element-plus['"]/;
      const match = content.match(importPattern);
      
      if (match) {
        const importStatement = match[0];
        if (!importStatement.includes('ElMessage')) {
          const newImport = importStatement.replace('}', ', ElMessage }');
          content = content.replace(importStatement, newImport);
        }
      } else {
        // 添加ElMessage导入
        const scriptIndex = content.indexOf('<script');
        if (scriptIndex !== -1) {
          const scriptEndIndex = content.indexOf('>', scriptIndex) + 1;
          const elMessageImport = `import { ElMessage } from 'element-plus';`;
          content = content.slice(0, scriptEndIndex) + '\n' + elMessageImport + content.slice(scriptEndIndex);
        }
      }
    }
    
    return content;
  }

  // 更新方法调用
  updateMethodCalls(content, fileName) {
    // 根据页面类型更新方法调用
    if (fileName.includes('List')) {
      content = this.updateListMethods(content);
    } else if (fileName.includes('Detail')) {
      content = this.updateDetailMethods(content);
    } else if (fileName.includes('Add') || fileName.includes('Edit')) {
      content = this.updateFormMethods(content);
    }
    
    return content;
  }

  // 更新列表方法
  updateListMethods(content) {
    // 查找硬编码的加载方法
    const loadMethodPattern = /const\s+load\w*\s*=\s*\(\)\s*=>\s*{[\s\S]*?}/g;
    const matches = content.match(loadMethodPattern) || [];
    
    matches.forEach(match => {
      // 检查是否是硬编码的加载方法
      if (match.includes('mock') || match.includes('hardcoded') || match.includes('静态数据')) {
        // 替换为API调用
        const apiCall = `const loadData = async () => {
  loading.value = true;
  try {
    const response = await ${this.getServiceName(content)}.getList(queryParams.value);
    data.value = response.data;
    total.value = response.total;
  } catch (error) {
    console.error('加载数据失败:', error);
    ElMessage.error('加载数据失败');
  } finally {
    loading.value = false;
  }
};`;
        
        content = content.replace(match, apiCall);
      }
    });
    
    return content;
  }

  // 获取服务名称
  getServiceName(content) {
    if (content.includes('ExpertService')) return 'ExpertService';
    if (content.includes('ProjectService')) return 'ProjectService';
    if (content.includes('SkillService')) return 'SkillService';
    if (content.includes('DomainService')) return 'DomainService';
    if (content.includes('StatsService')) return 'StatsService';
    return 'ApiService';
  }

  // 运行转换
  run() {
    console.log('🚀 开始API集成转换...');
    console.log('='.repeat(50));
    
    // 备份原始文件
    this.backupOriginalFiles();
    
    // 创建输出目录
    this.createOutputDir();
    
    // 获取所有Vue文件
    const files = fs.readdirSync(CONFIG.viewsDir)
      .filter(file => file.endsWith('.vue'))
      .filter(file => CONFIG.apiServices[file]); // 只处理有映射的文件
    
    this.stats.totalFiles = files.length;
    
    console.log(`\n📁 发现 ${files.length} 个需要转换的文件`);
    
    // 转换每个文件
    files.forEach(file => {
      this.transformFile(file);
      this.stats.processedFiles++;
    });
    
    // 输出统计信息
    console.log('\n' + '='.repeat(50));
    console.log('📊 转换统计:');
    console.log(`   总文件数: ${this.stats.totalFiles}`);
    console.log(`   已处理: ${this.stats.processedFiles}`);
    console.log(`   成功: ${this.stats.successFiles}`);
    console.log(`   失败: ${this.stats.failedFiles}`);
    
    if (this.stats.failedFiles === 0) {
      console.log('\n✅ 所有文件转换成功！');
      console.log(`\n转换后的文件保存在: ${CONFIG.outputDir}`);
      console.log('\n下一步操作:');
      console.log('1. 检查转换后的文件');
      console.log('2. 运行测试验证API连接');
      console.log('3. 部署到生产环境');
    } else {
      console.log('\n⚠️  部分文件转换失败，请检查错误信息');
    }
  }
}

// 运行脚本
const transformer = new ApiIntegrationTransformer();
transformer.run();
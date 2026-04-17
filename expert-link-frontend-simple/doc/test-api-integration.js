#!/usr/bin/env node

/**
 * Expert Link API集成验证测试脚本
 * 验证转换后的文件是否符合API集成要求
 */

const fs = require('fs');
const path = require('path');

// 配置
const CONFIG = {
  sourceDir: path.join(__dirname, 'views'),
  convertedDir: path.join(__dirname, 'views-api-integrated'),
  
  // 验证规则
  validationRules: {
    requiredImports: [
      'import.*from.*@/api/services',
      'import.*ElMessage',
      'import.*ref.*from.*vue'
    ],
    
    requiredVariables: [
      'loading.*ref',
      'const.*ref.*\\[\\]', // 数组数据
      'const.*ref\\(\\{.*\\}\\)' // 对象数据
    ],
    
    requiredMethods: [
      'async.*load',
      'try.*catch',
      'ElMessage\\.(success|error|warning)'
    ],
    
    forbiddenPatterns: [
      'const.*=.*\\[\\s*\\{.*id:\\s*\\d+', // 硬编码ID
      'const.*=.*\\[\\s*\\{.*name:\\s*[\'"]', // 硬编码名称
      'mockData',
      'hardcoded',
      '静态数据'
    ]
  }
};

class ApiIntegrationValidator {
  constructor() {
    this.results = {
      totalFiles: 0,
      validatedFiles: 0,
      passedFiles: 0,
      failedFiles: 0,
      details: []
    };
  }

  // 读取文件
  readFile(filePath) {
    try {
      return fs.readFileSync(filePath, 'utf-8');
    } catch (error) {
      console.error(`无法读取文件: ${filePath}`, error.message);
      return null;
    }
  }

  // 验证单个文件
  validateFile(fileName) {
    console.log(`\n🔍 验证: ${fileName}`);
    
    const sourcePath = path.join(CONFIG.sourceDir, fileName);
    const convertedPath = path.join(CONFIG.convertedDir, fileName);
    
    // 检查文件是否存在
    if (!fs.existsSync(convertedPath)) {
      console.log(`  ❌ 转换后的文件不存在: ${convertedPath}`);
      this.results.details.push({
        file: fileName,
        status: 'failed',
        errors: ['转换后的文件不存在']
      });
      this.results.failedFiles++;
      return false;
    }
    
    const sourceContent = this.readFile(sourcePath);
    const convertedContent = this.readFile(convertedPath);
    
    if (!sourceContent || !convertedContent) {
      console.log(`  ❌ 无法读取文件内容`);
      this.results.failedFiles++;
      return false;
    }
    
    // 执行验证
    const validationResult = this.performValidation(fileName, sourceContent, convertedContent);
    
    // 记录结果
    this.results.details.push(validationResult);
    
    if (validationResult.status === 'passed') {
      console.log(`  ✅ 验证通过`);
      this.results.passedFiles++;
    } else {
      console.log(`  ❌ 验证失败`);
      this.results.failedFiles++;
    }
    
    return validationResult.status === 'passed';
  }

  // 执行验证
  performValidation(fileName, sourceContent, convertedContent) {
    const result = {
      file: fileName,
      status: 'passed',
      warnings: [],
      errors: []
    };
    
    // 1. 检查是否移除了硬编码数据
    const hardcodedRemoved = this.checkHardcodedRemoved(sourceContent, convertedContent);
    if (!hardcodedRemoved) {
      result.warnings.push('可能仍包含硬编码数据');
    }
    
    // 2. 检查必需的导入
    const importIssues = this.checkRequiredImports(convertedContent);
    if (importIssues.length > 0) {
      result.errors.push(...importIssues);
    }
    
    // 3. 检查必需的变量
    const variableIssues = this.checkRequiredVariables(convertedContent);
    if (variableIssues.length > 0) {
      result.errors.push(...variableIssues);
    }
    
    // 4. 检查必需的方法
    const methodIssues = this.checkRequiredMethods(convertedContent);
    if (methodIssues.length > 0) {
      result.warnings.push(...methodIssues);
    }
    
    // 5. 检查禁止的模式
    const forbiddenIssues = this.checkForbiddenPatterns(convertedContent);
    if (forbiddenIssues.length > 0) {
      result.errors.push(...forbiddenIssues);
    }
    
    // 6. 检查API调用
    const apiCallIssues = this.checkApiCalls(convertedContent);
    if (apiCallIssues.length > 0) {
      result.warnings.push(...apiCallIssues);
    }
    
    // 更新状态
    if (result.errors.length > 0) {
      result.status = 'failed';
    } else if (result.warnings.length > 0) {
      result.status = 'warning';
    }
    
    return result;
  }

  // 检查是否移除了硬编码数据
  checkHardcodedRemoved(sourceContent, convertedContent) {
    // 检查源文件中的硬编码模式
    const hardcodedPatterns = [
      /const\s+\w+\s*=\s*\[[\s\S]*?\{[\s\S]*?id:\s*\d+[\s\S]*?\}[\s\S]*?\]/,
      /const\s+\w+\s*=\s*\{[\s\S]*?id:\s*\d+[\s\S]*?\}/,
      /mockData/,
      /hardcoded/
    ];
    
    // 如果源文件有硬编码数据，但转换后的文件没有，则通过
    const sourceHasHardcoded = hardcodedPatterns.some(pattern => pattern.test(sourceContent));
    const convertedHasHardcoded = hardcodedPatterns.some(pattern => pattern.test(convertedContent));
    
    return !sourceHasHardcoded || !convertedHasHardcoded;
  }

  // 检查必需的导入
  checkRequiredImports(content) {
    const issues = [];
    
    CONFIG.validationRules.requiredImports.forEach(pattern => {
      const regex = new RegExp(pattern);
      if (!regex.test(content)) {
        issues.push(`缺少必需的导入: ${pattern}`);
      }
    });
    
    return issues;
  }

  // 检查必需的变量
  checkRequiredVariables(content) {
    const issues = [];
    
    CONFIG.validationRules.requiredVariables.forEach(pattern => {
      const regex = new RegExp(pattern);
      if (!regex.test(content)) {
        issues.push(`缺少必需的变量: ${pattern}`);
      }
    });
    
    return issues;
  }

  // 检查必需的方法
  checkRequiredMethods(content) {
    const issues = [];
    
    CONFIG.validationRules.requiredMethods.forEach(pattern => {
      const regex = new RegExp(pattern);
      if (!regex.test(content)) {
        issues.push(`可能缺少必需的方法: ${pattern}`);
      }
    });
    
    return issues;
  }

  // 检查禁止的模式
  checkForbiddenPatterns(content) {
    const issues = [];
    
    CONFIG.validationRules.forbiddenPatterns.forEach(pattern => {
      const regex = new RegExp(pattern);
      if (regex.test(content)) {
        issues.push(`包含禁止的模式: ${pattern}`);
      }
    });
    
    return issues;
  }

  // 检查API调用
  checkApiCalls(content) {
    const issues = [];
    
    // 检查是否有API服务调用
    const apiCallPatterns = [
      /await\s+\w+Service\.\w+/,
      /\.get\(/,
      /\.post\(/,
      /\.put\(/,
      /\.delete\(/
    ];
    
    const hasApiCalls = apiCallPatterns.some(pattern => {
      const regex = new RegExp(pattern);
      return regex.test(content);
    });
    
    if (!hasApiCalls) {
      issues.push('未检测到API调用，可能仍使用硬编码数据');
    }
    
    return issues;
  }

  // 运行验证
  run() {
    console.log('🔬 Expert Link API集成验证测试');
    console.log('='.repeat(50));
    
    // 检查目录是否存在
    if (!fs.existsSync(CONFIG.convertedDir)) {
      console.log('❌ 转换后的目录不存在，请先运行转换脚本');
      return;
    }
    
    // 获取所有Vue文件
    const files = fs.readdirSync(CONFIG.sourceDir)
      .filter(file => file.endsWith('.vue'))
      .sort();
    
    this.results.totalFiles = files.length;
    
    console.log(`\n📁 发现 ${files.length} 个Vue文件`);
    console.log('开始验证...\n');
    
    // 验证每个文件
    files.forEach(file => {
      this.validateFile(file);
      this.results.validatedFiles++;
    });
    
    // 输出结果
    this.printResults();
    
    // 生成验证报告
    this.generateReport();
  }

  // 打印结果
  printResults() {
    console.log('\n' + '='.repeat(50));
    console.log('📊 验证结果:');
    console.log(`   总文件数: ${this.results.totalFiles}`);
    console.log(`   已验证: ${this.results.validatedFiles}`);
    console.log(`   通过: ${this.results.passedFiles}`);
    console.log(`   失败: ${this.results.failedFiles}`);
    
    // 显示详细结果
    console.log('\n📋 详细结果:');
    this.results.details.forEach(detail => {
      const icon = detail.status === 'passed' ? '✅' : 
                  detail.status === 'warning' ? '⚠️ ' : '❌';
      console.log(`  ${icon} ${detail.file}`);
      
      if (detail.errors.length > 0) {
        detail.errors.forEach(error => {
          console.log(`    ❌ ${error}`);
        });
      }
      
      if (detail.warnings.length > 0) {
        detail.warnings.forEach(warning => {
          console.log(`    ⚠️  ${warning}`);
        });
      }
    });
    
    // 总结
    if (this.results.failedFiles === 0) {
      console.log('\n🎉 所有文件验证通过！API集成转换成功。');
    } else {
      console.log('\n⚠️  部分文件验证失败，请检查并修复问题。');
    }
  }

  // 生成报告
  generateReport() {
    const reportPath = path.join(__dirname, 'api-validation-report.md');
    
    let reportContent = `# API集成验证报告

## 验证统计
- 总文件数: ${this.results.totalFiles}
- 已验证文件: ${this.results.validatedFiles}
- 通过验证: ${this.results.passedFiles}
- 验证失败: ${this.results.failedFiles}

## 详细验证结果

`;
    
    // 按状态分组
    const passedFiles = this.results.details.filter(d => d.status === 'passed');
    const warningFiles = this.results.details.filter(d => d.status === 'warning');
    const failedFiles = this.results.details.filter(d => d.status === 'failed');
    
    // 通过的文件
    reportContent += `### ✅ 通过验证的文件 (${passedFiles.length})\n\n`;
    passedFiles.forEach(file => {
      reportContent += `- ${file.file}\n`;
    });
    
    // 有警告的文件
    if (warningFiles.length > 0) {
      reportContent += `\n### ⚠️ 有警告的文件 (${warningFiles.length})\n\n`;
      warningFiles.forEach(file => {
        reportContent += `#### ${file.file}\n`;
        file.warnings.forEach(warning => {
          reportContent += `- ${warning}\n`;
        });
        reportContent += '\n';
      });
    }
    
    // 失败的文件
    if (failedFiles.length > 0) {
      reportContent += `\n### ❌ 验证失败的文件 (${failedFiles.length})\n\n`;
      failedFiles.forEach(file => {
        reportContent += `#### ${file.file}\n`;
        file.errors.forEach(error => {
          reportContent += `- ${error}\n`;
        });
        if (file.warnings.length > 0) {
          file.warnings.forEach(warning => {
            reportContent += `- ⚠️ ${warning}\n`;
          });
        }
        reportContent += '\n';
      });
    }
    
    // 建议
    reportContent += `## 建议和下一步操作

### 对于验证通过的文件
1. 进行功能测试，验证API调用是否正常工作
2. 测试错误处理机制
3. 验证加载状态显示

### 对于有警告的文件
1. 检查警告内容，确认是否需要修复
2. 验证API调用是否完整
3. 测试边界情况

### 对于验证失败的文件
1. 根据错误信息进行修复
2. 重新运行验证测试
3. 确保移除了所有硬编码数据

### 整体测试步骤
\`\`\`bash
# 1. 启动后端服务
cd expert-link-backend
./mvnw spring-boot:run

# 2. 启动前端开发服务器
cd ..
npm run dev

# 3. 访问应用进行测试
open http://localhost:3000
\`\`\`

### 测试要点
1. **数据加载**: 验证每个页面都能正确加载数据
2. **CRUD操作**: 测试添加、编辑、删除功能
3. **错误处理**: 测试网络错误、服务器错误等情况
4. **用户体验**: 验证加载状态、空状态、错误提示等

## 技术支持

如需帮助，请参考:
- API集成指南: API_INTEGRATION_GUIDE.md
- 转换脚本: batch-api-integration.js
- 后端API文档: http://localhost:8080/swagger-ui.html
`;

    fs.writeFileSync(reportPath, reportContent, 'utf-8');
    console.log(`\n📄 验证报告已生成: ${reportPath}`);
  }
}

// 运行验证
const validator = new ApiIntegrationValidator();
validator.run();
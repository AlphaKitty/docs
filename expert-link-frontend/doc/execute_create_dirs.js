const fs = require('fs')
const path = require('path')

// 创建目录结构
const dirs = [
  'src/assets',
  'src/components/common',
  'src/components/layout',
  'src/components/business',
  'src/composables',
  'src/router',
  'src/stores',
  'src/types',
  'src/utils',
  'src/views/dashboard',
  'src/views/experts',
  'src/views/projects',
  'src/views/skills',
  'src/views/analysis',
  'src/views/settings',
  'src/views/auth'
]

dirs.forEach(dir => {
  const fullPath = path.join(__dirname, dir)
  if (!fs.existsSync(fullPath)) {
    fs.mkdirSync(fullPath, { recursive: true })
    console.log(`创建目录: ${dir}`)
  }
})

// 创建必要的文件
const files = [
  'src/App.vue',
  'src/main.ts',
  '.env',
  '.env.development',
  '.env.production'
]

files.forEach(file => {
  const fullPath = path.join(__dirname, file)
  if (!fs.existsSync(fullPath)) {
    fs.writeFileSync(fullPath, '')
    console.log(`创建文件: ${file}`)
  }
})

console.log('目录结构创建完成')
const fs = require('fs')
const path = require('path')

const baseDir = '/mnt/user-data/workspace/src'

const directories = [
  'api',
  'api/types',
  'api/services',
  'api/mocks',
  'api/utils'
]

function createDirectories() {
  console.log('开始创建API目录结构...')
  
  // 确保src目录存在
  if (!fs.existsSync(baseDir)) {
    fs.mkdirSync(baseDir, { recursive: true })
    console.log(`创建目录: ${baseDir}`)
  }
  
  // 创建所有子目录
  directories.forEach(dir => {
    const fullPath = path.join(baseDir, dir)
    if (!fs.existsSync(fullPath)) {
      fs.mkdirSync(fullPath, { recursive: true })
      console.log(`创建目录: ${fullPath}`)
    } else {
      console.log(`目录已存在: ${fullPath}`)
    }
  })
  
  console.log('API目录结构创建完成！')
}

createDirectories()
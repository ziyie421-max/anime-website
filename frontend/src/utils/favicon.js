/**
 * 动态favicon工具
 * 根据主题变化动态生成不同颜色的favicon
 */

// 创建带主题色彩的favicon
function createThemedFavicon(isDark = false) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'

    img.onload = () => {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      const size = 64 // 回到64x64像素，平衡大小和性能

      canvas.width = size
      canvas.height = size

      // 根据主题设置图标颜色
      const primaryColor = isDark ? '#EA7A99' : '#4EABE6' // 粉色主题 : 蓝色主题

      // 让图标完全填满整个画布，无边距
      ctx.drawImage(img, 0, 0, size, size)

      // 获取图像数据
      const imageData = ctx.getImageData(0, 0, size, size)
      const data = imageData.data

      // 解析主题颜色
      const color = hexToRgb(primaryColor)

      // 遍历每个像素，将非透明像素改为主题色
      for (let i = 0; i < data.length; i += 4) {
        const alpha = data[i + 3] // 透明度
        if (alpha > 0) { // 如果不是完全透明
          // 保持原始透明度，但改变颜色为主题色
          data[i] = color.r     // Red
          data[i + 1] = color.g // Green
          data[i + 2] = color.b // Blue
          // data[i + 3] 保持原始透明度
        }
      }

      // 清空画布并绘制新的图像数据
      ctx.clearRect(0, 0, size, size)
      ctx.putImageData(imageData, 0, 0)

      resolve(canvas)
    }

    img.onerror = () => {
      // 如果图片加载失败，创建简单的文字图标
      const canvas = createSimpleFavicon(isDark)
      resolve(canvas)
    }

    // 尝试加载原始图标
    img.src = '/title-icon.png'
  })
}

// 将十六进制颜色转换为RGB
function hexToRgb(hex) {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result ? {
    r: parseInt(result[1], 16),
    g: parseInt(result[2], 16),
    b: parseInt(result[3], 16)
  } : { r: 0, g: 0, b: 0 }
}

// 创建简单的文字favicon作为降级方案
function createSimpleFavicon(isDark = false) {
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  const size = 64 // 与主favicon保持相同的64x64尺寸

  canvas.width = size
  canvas.height = size

  // 根据主题设置图标颜色（透明背景）
  const primaryColor = isDark ? '#EA7A99' : '#4EABE6' // 粉色主题 : 蓝色主题

  // 不绘制背景，保持透明

  // 绘制完全填满画布的圆形图标
  ctx.beginPath()
  ctx.arc(size / 2, size / 2, size / 2, 0, 2 * Math.PI) // 完全填满，无边距
  ctx.fillStyle = primaryColor
  ctx.fill()

  // 绘制字母 "A" (Anime的首字母) - 适配64x64尺寸
  ctx.fillStyle = '#ffffff'
  ctx.font = 'bold 56px Arial' // 64x64画布中的合适字体
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText('A', size / 2, size / 2)

  return canvas
}

// 更新favicon
export async function updateFavicon(isDark = false) {
  try {
    // 移除现有的favicon
    const existingFavicon = document.querySelector('link[rel="icon"]')
    if (existingFavicon) {
      existingFavicon.remove()
    }

    // 创建主题化的favicon
    const canvas = await createThemedFavicon(isDark)
    const dataURL = canvas.toDataURL('image/png')

    // 创建新的link元素
    const link = document.createElement('link')
    link.rel = 'icon'
    link.type = 'image/png'
    link.href = dataURL

    // 添加到head
    document.head.appendChild(link)

    console.log('🎨 Favicon已更新为', isDark ? '暗色粉色主题' : '蓝白色主题')
  } catch (error) {
    console.error('❌ 更新favicon失败:', error)
    // 降级到静态图标
    fallbackToStaticIcon()
  }
}

// 降级到静态图标
function fallbackToStaticIcon() {
  const existingFavicon = document.querySelector('link[rel="icon"]')
  if (existingFavicon) {
    existingFavicon.remove()
  }

  const link = document.createElement('link')
  link.rel = 'icon'
  link.type = 'image/png'
  link.href = '/title-icon.png'

  document.head.appendChild(link)
}

// 初始化favicon
export async function initFavicon() {
  // 检查当前主题 - 主题类是添加到body上的
  const isDark = document.body.classList.contains('theme-dark')
  await updateFavicon(isDark)
}

// 监听主题变化
export function watchThemeChange() {
  // 使用MutationObserver监听body的class变化
  const observer = new MutationObserver(async (mutations) => {
    for (const mutation of mutations) {
      if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
        const isDark = document.body.classList.contains('theme-dark')
        await updateFavicon(isDark)
        break // 只需要处理一次
      }
    }
  })

  // 开始观察body元素
  observer.observe(document.body, {
    attributes: true,
    attributeFilter: ['class']
  })

  return observer
}

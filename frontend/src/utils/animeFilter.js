/**
 * 动漫内容筛选工具
 * 用于过滤掉电影解说等无关内容，只保留真正的动漫内容
 */

/**
 * 检查标签是否包含动漫相关关键词
 * @param {string} tag - 标签文本
 * @returns {boolean} - 是否为动漫相关标签
 */
function isAnimeRelatedTag(tag) {
  if (!tag || typeof tag !== 'string') {
    return false
  }
  
  const lowerTag = tag.toLowerCase()
  
  // 动漫相关关键词
  const animeKeywords = [
    '动漫', '动画', '番剧', '预告片', '预告', 'pv', 'trailer',
    'anime', 'animation', '漫画', 'manga', '二次元',
    '日漫', '国漫', '美漫', '韩漫', '新番', '完结番',
    'ova', 'oad', '剧场版', '电影版', '特别篇'
  ]
  
  return animeKeywords.some(keyword => lowerTag.includes(keyword))
}

/**
 * 检查标题是否包含动漫相关关键词
 * @param {string} title - 标题文本
 * @returns {boolean} - 是否为动漫相关标题
 */
function isAnimeRelatedTitle(title) {
  if (!title || typeof title !== 'string') {
    return false
  }

  const lowerTitle = title.toLowerCase()

  // 动漫相关标题关键词（扩展更多常见词汇）
  const animeTitleKeywords = [
    '动漫', '动画', '番剧', '预告片', '预告', 'pv', 'trailer',
    'anime', 'animation', '漫画', 'manga', '二次元',
    '日漫', '国漫', '美漫', '韩漫', '新番', '完结番',
    'ova', 'oad', '剧场版', '电影版', '特别篇',
    '第一季', '第二季', '第三季', '第四季', '第五季', '第六季',
    '第1季', '第2季', '第3季', '第4季', '第5季', '第6季',
    'season', 's1', 's2', 's3', 's4', 's5', 's6',
    // 添加更多常见的动漫相关词汇
    '魔法', '忍者', '海贼', '死神', '火影', '龙珠', '柯南',
    '宫崎骏', '新海诚', '吉卜力', 'studio', '工作室',
    '声优', 'cv', '配音', '原创', 'original',
    '漫改', '轻小说', '小说改编', 'light novel'
  ]

  return animeTitleKeywords.some(keyword => lowerTitle.includes(keyword))
}



/**
 * 筛选动漫项目，过滤掉无关内容
 * @param {Object} animeItem - 动漫项目对象
 * @returns {boolean} - 是否应该保留该项目
 */
export function shouldKeepAnimeItem(animeItem) {
  if (!animeItem) {
    return false
  }

  const title = animeItem.vod_name || animeItem.vodName || ''
  const tags = animeItem.vod_tag || animeItem.vodTag || ''
  const content = animeItem.vod_content || animeItem.vodContent || ''
  const remarks = animeItem.vod_remarks || animeItem.vodRemarks || ''

  // 优先检查是否明确包含无关内容关键词
  // 只有当标题中明确包含这些关键词时才过滤
  const titleLower = title.toLowerCase()

  // 明确的无关内容关键词（只检查标题，更精确）
  // 注意：这些关键词必须非常精确，避免误杀正常内容
  const definiteIrrelevantKeywords = [
    '电影解说', '影视解说', '解说电影', '解说影视',
    '说电影', '说影视', '电影推荐', '影视推荐',
    '电影剪辑', '影视剪辑', '电影合集', '影视合集',
    '电影盘点', '影视盘点'
    // 移除了 '真人电影', '真人版电影' 避免误杀动漫改编的真人版
  ]

  // 检查标题是否明确包含无关内容
  const hasIrrelevantKeywords = definiteIrrelevantKeywords.some(keyword =>
    titleLower.includes(keyword)
  )

  if (hasIrrelevantKeywords) {
    console.log('🚫 过滤掉明确的无关内容:', title)
    return false
  }

  // 如果标题或标签包含动漫相关关键词，则保留
  const hasAnimeKeywords = isAnimeRelatedTitle(title) || isAnimeRelatedTag(tags)

  if (hasAnimeKeywords) {
    console.log('✅ 保留动漫内容:', title)
    return true
  }

  // 对于没有明确动漫关键词的内容，采用更宽松的策略
  // 只要不是明确的无关内容，就保留（避免误杀）
  console.log('✅ 保留内容（宽松策略）:', title)
  return true
}

/**
 * 批量筛选动漫列表
 * @param {Array} animeList - 动漫列表
 * @returns {Array} - 筛选后的动漫列表
 */
export function filterAnimeList(animeList) {
  if (!Array.isArray(animeList)) {
    return []
  }
  
  const originalCount = animeList.length
  const filteredList = animeList.filter(shouldKeepAnimeItem)
  const filteredCount = filteredList.length
  
  console.log(`🔍 动漫筛选完成: ${originalCount} -> ${filteredCount} (过滤掉 ${originalCount - filteredCount} 个无关项目)`)
  
  return filteredList
}

/**
 * 筛选API响应数据
 * @param {Object} response - API响应对象
 * @returns {Object} - 筛选后的响应对象
 */
export function filterAnimeResponse(response) {
  if (!response || !response.list || !Array.isArray(response.list)) {
    return response
  }
  
  const filteredList = filterAnimeList(response.list)
  
  return {
    ...response,
    list: filteredList,
    // 更新总数（如果原来有的话）
    total: response.total ? Math.max(0, response.total - (response.list.length - filteredList.length)) : filteredList.length
  }
}

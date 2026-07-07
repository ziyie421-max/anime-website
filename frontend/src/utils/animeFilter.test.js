/**
 * 动漫筛选功能测试
 * 用于验证筛选逻辑是否正确工作
 */

import { shouldKeepAnimeItem, filterAnimeList, filterAnimeResponse } from './animeFilter.js'

// 测试数据 - 更全面的测试用例
const testAnimeItems = [
  // 应该保留的动漫内容
  {
    vod_name: '鬼灭之刃',
    vod_tag: '动漫,日本动漫,热血',
    vod_content: '一部关于鬼杀队的动漫作品'
  },
  {
    vod_name: '进击的巨人 第四季',
    vod_tag: '动画,番剧',
    vod_content: '人类与巨人的战争故事'
  },
  {
    vod_name: '你的名字 预告片',
    vod_tag: '预告片,动漫电影',
    vod_content: '新海诚导演的动画电影预告'
  },
  {
    vod_name: '海贼王',
    vod_tag: '冒险,热血',
    vod_content: '路飞的冒险故事'
  },
  {
    vod_name: '某个没有明确标签的动漫',
    vod_tag: '剧情,冒险',
    vod_content: '一个普通的故事'
  },
  {
    vod_name: '攻壳机动队真人版',
    vod_tag: '科幻,动作',
    vod_content: '基于经典动漫改编的真人电影'
  },
  {
    vod_name: '钢之炼金术师',
    vod_tag: '奇幻,冒险',
    vod_content: '兄弟俩寻找贤者之石的故事'
  },
  {
    vod_name: '千与千寻',
    vod_tag: '宫崎骏,吉卜力',
    vod_content: '宫崎骏经典动画电影'
  },
  {
    vod_name: '火影忍者疾风传',
    vod_tag: '忍者,热血',
    vod_content: '鸣人成长的故事'
  },
  {
    vod_name: '某部普通电影',
    vod_tag: '剧情,爱情',
    vod_content: '一个普通的爱情故事'
  },

  // 应该过滤掉的内容（只有明确的解说类）
  {
    vod_name: '复仇者联盟电影解说',
    vod_tag: '电影解说,漫威',
    vod_content: '详细解说复仇者联盟电影剧情'
  },
  {
    vod_name: '说电影：蜘蛛侠',
    vod_tag: '影视解说,超级英雄',
    vod_content: '蜘蛛侠电影剧情解说'
  },
  {
    vod_name: '电影推荐：阿凡达',
    vod_tag: '电影推荐,科幻',
    vod_content: '推荐阿凡达这部科幻大片'
  },
  {
    vod_name: '影视剪辑：动作片合集',
    vod_tag: '剪辑,动作',
    vod_content: '各种动作片的精彩片段剪辑'
  }
]

// 运行测试
function runTests() {
  console.log('🧪 开始动漫筛选功能测试...\n')
  
  // 测试单个项目筛选
  console.log('📋 测试单个项目筛选:')
  testAnimeItems.forEach((item, index) => {
    const shouldKeep = shouldKeepAnimeItem(item)
    const status = shouldKeep ? '✅ 保留' : '❌ 过滤'
    console.log(`${index + 1}. ${status}: ${item.vod_name}`)
  })
  
  console.log('\n📋 测试批量筛选:')
  const filteredList = filterAnimeList(testAnimeItems)
  console.log(`原始数量: ${testAnimeItems.length}`)
  console.log(`筛选后数量: ${filteredList.length}`)
  console.log('筛选后的动漫:')
  filteredList.forEach((item, index) => {
    console.log(`${index + 1}. ${item.vod_name}`)
  })
  
  console.log('\n📋 测试API响应筛选:')
  const mockResponse = {
    code: 1,
    msg: 'success',
    list: testAnimeItems,
    total: testAnimeItems.length,
    pagecount: 1
  }
  
  const filteredResponse = filterAnimeResponse(mockResponse)
  console.log(`API响应筛选: ${mockResponse.list.length} -> ${filteredResponse.list.length}`)
  
  console.log('\n🎉 测试完成！')
}

// 如果直接运行此文件，执行测试
if (typeof window === 'undefined') {
  // Node.js 环境
  runTests()
} else {
  // 浏览器环境，将测试函数暴露到全局
  window.testAnimeFilter = runTests
}

export { runTests }

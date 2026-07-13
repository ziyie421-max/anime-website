// API服务 - 统一管理所有API调用
// 统一使用相对路径 /api：开发期走 Vite 代理，生产期前后端同源（前端打进后端 jar）
const getAPIBaseURL = () => '/api';

const API_BASE_URL = getAPIBaseURL();

/**
 * 通用的API请求函数
 */
async function apiRequest(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;

  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const config = { ...defaultOptions, ...options };

  try {
    const response = await fetch(url, config);

    if (!response.ok) {
      // 尝试解析后端返回的错误信息并透传，避免只显示 "HTTP error! status: 400"
      let message = `HTTP error! status: ${response.status}`;
      try {
        const errorData = await response.json();
        if (errorData && (errorData.message || errorData.error)) {
          message = errorData.message || errorData.error;
        }
      } catch (_) {
        // 响应体不是 JSON，保留默认 status 提示
      }
      throw new Error(message);
    }

    return await response.json();
  } catch (error) {
    console.error('API请求失败:', error);
    throw error;
  }
}

/**
 * 动漫相关API
 */
export const animeAPI = {
  // 获取所有动漫（分页）
  getAll: (page = 0, size = 12, sortBy = 'updatedAt', sortDir = 'desc') => {
    return apiRequest(`/anime?page=${page}&size=${size}&sort=${sortBy}&sortDir=${sortDir}`);
  },

  // 根据ID获取动漫详情
  getById: (id) => {
    return apiRequest(`/anime/${id}`);
  },

  // 获取热门动漫
  getPopular: (limit = 10) => {
    return apiRequest(`/anime/popular?limit=${limit}`);
  },

  // 获取最新动漫
  getLatest: (limit = 10) => {
    return apiRequest(`/anime/latest?limit=${limit}`);
  },

  // 获取推荐动漫
  getFeatured: () => {
    return apiRequest('/anime/featured');
  },

  // 根据状态获取动漫
  getByStatus: (status, page = 0, size = 12) => {
    return apiRequest(`/anime/status/${status}?page=${page}&size=${size}`);
  },

  // 根据分类获取动漫
  getByCategory: (categoryId, page = 0, size = 12) => {
    return apiRequest(`/anime/category/${categoryId}?page=${page}&size=${size}`);
  },

  // 搜索动漫 - 直接使用量子资源搜索
  search: (keyword, page = 0, size = 12) => {
    // 直接调用外部搜索API，使用量子资源
    return externalAPI.searchAnime(keyword, page + 1);
  },

  // 根据年份获取动漫
  getByYear: (year, page = 0, size = 12) => {
    return apiRequest(`/anime/year/${year}?page=${page}&size=${size}`);
  },

  // 获取动漫统计信息
  getStats: () => {
    return apiRequest('/anime/stats');
  },

  // 增加观看次数
  incrementViewCount: (id) => {
    return apiRequest(`/anime/${id}/view`, { method: 'POST' });
  }
};

/**
 * 分类相关API
 */
export const categoryAPI = {
  // 获取所有分类
  getAll: () => {
    return apiRequest('/categories');
  },

  // 根据ID获取分类详情
  getById: (id) => {
    return apiRequest(`/categories/${id}`);
  },

  // 根据名称获取分类
  getByName: (name) => {
    return apiRequest(`/categories/name/${encodeURIComponent(name)}`);
  },

  // 获取热门分类
  getPopular: (limit = 8) => {
    return apiRequest(`/categories/popular?limit=${limit}`);
  },

  // 获取分类统计信息
  getStats: () => {
    return apiRequest('/categories/stats');
  }
};

/**
 * 剧集相关API
 */
export const episodeAPI = {
  // 根据动漫ID获取剧集列表
  getByAnimeId: (animeId, publishedOnly = true) => {
    return apiRequest(`/episodes/anime/${animeId}?publishedOnly=${publishedOnly}`);
  },

  // 分页获取剧集列表
  getByAnimeIdPaged: (animeId, page = 0, size = 20, publishedOnly = true) => {
    return apiRequest(`/episodes/anime/${animeId}/page?page=${page}&size=${size}&publishedOnly=${publishedOnly}`);
  },

  // 根据ID获取剧集详情
  getById: (id) => {
    return apiRequest(`/episodes/${id}`);
  },

  // 根据动漫ID和剧集编号获取剧集
  getByAnimeIdAndNumber: (animeId, episodeNumber) => {
    return apiRequest(`/episodes/anime/${animeId}/episode/${episodeNumber}`);
  },

  // 获取热门剧集
  getPopular: (animeId, limit = 10) => {
    return apiRequest(`/episodes/anime/${animeId}/popular?limit=${limit}`);
  },

  // 获取最新剧集
  getLatest: (animeId, limit = 10) => {
    return apiRequest(`/episodes/anime/${animeId}/latest?limit=${limit}`);
  },

  // 搜索剧集
  search: (animeId, keyword) => {
    return apiRequest(`/episodes/anime/${animeId}/search?keyword=${encodeURIComponent(keyword)}`);
  },

  // 获取剧集导航信息
  getNavigation: (animeId, episodeNumber) => {
    return apiRequest(`/episodes/anime/${animeId}/episode/${episodeNumber}/navigation`);
  },

  // 增加观看次数
  incrementViewCount: (id) => {
    return apiRequest(`/episodes/${id}/view`, { method: 'POST' });
  },

  // 获取剧集统计信息
  getStats: (animeId) => {
    return apiRequest(`/episodes/anime/${animeId}/stats`);
  }
};

/**
 * 认证相关API
 */
export const authAPI = {
  // 用户登录
  login: (loginData) => {
    return apiRequest('/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(loginData)
    });
  },

  // 用户注册
  register: (registerData) => {
    return apiRequest('/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(registerData)
    });
  },

  // 刷新令牌
  refreshToken: (refreshToken) => {
    return apiRequest('/auth/refresh', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken })
    });
  },

  // 获取当前用户信息
  getCurrentUser: () => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      return Promise.reject(new Error('未登录'));
    }

    return apiRequest('/auth/me', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  },

  // 用户登出
  logout: () => {
    return apiRequest('/auth/logout', {
      method: 'POST'
    });
  },

  // 检查用户名是否可用
  checkUsername: (username) => {
    return apiRequest(`/auth/check-username?username=${encodeURIComponent(username)}`);
  },

  // 检查邮箱是否可用
  checkEmail: (email) => {
    return apiRequest(`/auth/check-email?email=${encodeURIComponent(email)}`);
  },

  // 发送邮箱验证码
  sendVerificationCode: (data) => {
    return apiRequest('/auth/send-code', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data)
    });
  },

  // 验证邮箱验证码
  verifyCode: (data) => {
    return apiRequest('/auth/verify-code', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data)
    });
  }
};

/**
 * 测试相关API
 */
export const testAPI = {
  // 健康检查
  health: () => {
    return apiRequest('/test/health');
  },

  // 简单测试
  hello: () => {
    return apiRequest('/test/hello');
  }
};

/**
 * 错误处理工具
 */
export const handleApiError = (error) => {
  console.error('API错误:', error);

  if (error.message.includes('Failed to fetch')) {
    return '网络连接失败，请检查网络连接';
  }

  if (error.message.includes('404')) {
    return '请求的资源不存在';
  }

  if (error.message.includes('500')) {
    return '服务器内部错误，请稍后重试';
  }

  return '请求失败，请稍后重试';
};

/**
 * 外部数据源API
 */
export const externalAPI = {
  // 获取外部动漫列表
  getAnimeList: (category = '日本动漫', page = 1, keyword = null) => {
    const params = new URLSearchParams({
      category,
      page: page.toString()
    });

    if (keyword) {
      params.append('keyword', keyword);
    }

    return apiRequest(`/external/anime/list?${params.toString()}`);
  },

  // 获取外部动漫详情
  getAnimeDetail: (vodId) => {
    return apiRequest(`/external/anime/detail/${vodId}`);
  },

  // 搜索外部动漫 - 直接调用量子资源API（和测试页面一样）
  searchAnime: async (keyword, page = 1) => {
    try {
      // 直接调用量子资源API，和测试页面完全一样的方式
      const apiUrl = `https://cj.lziapi.com/api.php/provide/vod/?ac=list&wd=${encodeURIComponent(keyword)}&pg=${page}`;
      console.log('直接调用量子资源API:', apiUrl);

      const response = await fetch(apiUrl, {
        method: 'GET',
        mode: 'cors', // 明确指定CORS模式
        headers: {
          'Accept': 'application/json, text/plain, */*'
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('量子资源API响应:', data);

      // 为每个动漫项添加源信息
      if (data.list && Array.isArray(data.list)) {
        data.list = data.list.map(item => ({
          ...item,
          sourceKey: 'lzzy',
          sourceName: '🎥┃量子┃资源'
        }));
      }

      return data;
    } catch (error) {
      console.error('调用量子资源API失败:', error);
      // 如果CORS失败，提供更详细的错误信息
      if (error.message.includes('CORS')) {
        console.error('CORS错误：可能需要使用代理或者API不支持跨域请求');
      }
      throw error;
    }
  },

  // 获取播放链接
  getPlayUrls: (vodId, source = 'lzzy') => {
    return apiRequest(`/external/anime/play/${vodId}?source=${encodeURIComponent(source)}`);
  },

  // 获取热门动漫（优先展示量子资源）
  getPopularAnime: (page = 1, limit = 9) => {
    const params = new URLSearchParams({
      page: page.toString(),
      limit: limit.toString()
    });

    return apiRequest(`/external/anime/popular?${params.toString()}`);
  },

  // 获取支持的分类列表
  getCategories: () => {
    return apiRequest('/external/categories');
  },

  // 获取支持的资源源列表
  getSources: () => {
    return apiRequest('/external/sources');
  },

  // 健康检查
  healthCheck: () => {
    return apiRequest('/external/health');
  }
};

export default {
  animeAPI,
  categoryAPI,
  episodeAPI,
  authAPI,
  externalAPI,
  testAPI,
  handleApiError
};

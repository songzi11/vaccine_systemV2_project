<script>
/**
 * App.vue — 应用入口
 * 职责：
 * 1. Pinia 初始化（在 createApp 中已完成）
 * 2. 路由守卫：未登录 → 登录页，已登录 → 按角色跳转
 * 3. Token 校验：onShow 时检查 Token 有效性
 */

import { getToken } from '@/utils/auth.js'

// 无需登录即可访问的页面（公开页面）
const PUBLIC_PAGES = [
  '/pages/auth/login',
  '/pages/auth/register',
  '/pages/auth/forgot-password',
  '/pages/vaccine/list',
  '/pages/vaccine/detail',
  '/pages/notice/list',
  '/pages/notice/detail',
  '/pages/common/404'
]

export default {
  onLaunch() {
    console.log('[App] Launch')
    // 应用启动时检查登录状态
    this.checkAuth()
  },

  onShow() {
    console.log('[App] Show')
    // 每次应用从后台恢复时检查 Token
    this.checkAuth()
  },

  onHide() {
    console.log('[App] Hide')
  },

  methods: {
    /**
     * 认证检查
     * - 无 Token 且当前页面不是公开页面 → 跳转登录页
     */
    checkAuth() {
      const token = getToken()
      if (!token) {
        // 获取当前页面路径
        const pages = getCurrentPages()
        // pages 为空说明应用刚启动，首页不是公开页面，直接跳转登录
        if (pages.length === 0) {
          uni.reLaunch({ url: '/pages/auth/login' })
          return
        }

        const currentPage = pages[pages.length - 1]
        const path = '/' + currentPage.route

        if (!PUBLIC_PAGES.includes(path)) {
          // 保存来源页面，登录后跳回
          uni.setStorageSync('loginRedirect', path)
          uni.reLaunch({ url: '/pages/auth/login' })
        }
      }
    }
  }
}
</script>

<style lang="scss">
@import '@/uni_modules/uni-scss/index.scss';

/* 全局基础样式 */
page {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 50%, #a5d6a7 100%);
  font-size: $font-size-base;
  color: $color-text-primary;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  line-height: 1.6;
}

/* 全局底部版权声明 */
page::after {
  content: '此app版权归宋子嘉所有';
  display: block;
  text-align: center;
  font-size: 22rpx;
  color: #999999;
  padding: 30rpx 0 calc(30rpx + env(safe-area-inset-bottom));
}

/* 安全区域底部占位（为 CustomTabBar 页面预留空间） */
.safe-area-bottom {
  padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
}

/* 卡片通用样式 */
.card {
  background-color: $color-bg-white;
  border-radius: $radius-md;
  padding: $spacing-lg;
  margin: $spacing-sm $spacing-md;
  box-shadow: $shadow-sm;
}

/* H5 PC端适配 */
/* #ifdef H5 */
@media screen and (min-width: 768px) {
  body {
    overflow-y: scroll;
  }
}
/* #endif */
</style>

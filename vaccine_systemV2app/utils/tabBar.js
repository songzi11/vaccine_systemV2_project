/**
 * TabBar 图标映射
 * 用于 CustomTabBar 组件的图标渲染
 * 使用 uni-icons 的图标名
 */

/**
 * 图标名称映射
 * key 对应 store/user.js 中 tabBarConfig 的 icon 字段
 */
export const TAB_ICONS = {
  home: 'home',
  calendar: 'calendar',
  person: 'person',
  chart: 'bars',
  settings: 'gear',
  list: 'list',
  document: 'paperplane',
  sound: 'sound',
  box: 'box',
  grid: 'grid'
}

/**
 * TabBar 未选中颜色
 */
export const TAB_COLOR = '#999999'

/**
 * TabBar 选中颜色
 */
export const TAB_ACTIVE_COLOR = '#07C160'

/**
 * TabBar 背景色
 */
export const TAB_BG_COLOR = '#FFFFFF'

/**
 * TabBar 高度（rpx）
 */
export const TAB_HEIGHT = 100

/**
 * 判断指定页面路径是否为 TabBar 页面
 * @param {string} path - 页面路径（如 /pages/index/index）
 * @param {Array} tabBarList - 当前角色的 TabBar 配置
 * @returns {boolean}
 */
export function isTabBarPage(path, tabBarList = []) {
  return tabBarList.some(tab => tab.path === path)
}

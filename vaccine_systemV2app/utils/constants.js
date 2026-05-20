/**
 * 共享常量定义
 * 来源：共享内核（DOMAIN-CROSS-CUTTING-001）
 * 包含：预约状态、批次状态、用户状态、性别、证件类型、错误码、角色编码、权限编码
 */

// ==================== 预约状态 ====================
export const APPOINTMENT_STATUS = {
  APPOINTED: 1,       // 已预约
  COMPLETED: 2,       // 已完成
  CANCELLED: 3,       // 已取消
  EXPIRED: 4,         // 已过期
  SIGNED_IN: 6,       // 已签到
  PRECHECK_PASS: 7,   // 预检通过
  PRECHECK_FAIL: 9,   // 预检不通过
  OBSERVING: 10       // 留观中
}

export const APPOINTMENT_STATUS_TEXT = {
  [APPOINTMENT_STATUS.APPOINTED]: '待签到',
  [APPOINTMENT_STATUS.COMPLETED]: '已完成',
  [APPOINTMENT_STATUS.CANCELLED]: '已取消',
  [APPOINTMENT_STATUS.EXPIRED]: '已过期',
  [APPOINTMENT_STATUS.SIGNED_IN]: '已签到',
  [APPOINTMENT_STATUS.PRECHECK_PASS]: '预检通过',
  [APPOINTMENT_STATUS.PRECHECK_FAIL]: '预检不通过',
  [APPOINTMENT_STATUS.OBSERVING]: '留观中'
}

export const APPOINTMENT_STATUS_COLOR = {
  [APPOINTMENT_STATUS.APPOINTED]: '#1989FA',
  [APPOINTMENT_STATUS.COMPLETED]: '#07C160',
  [APPOINTMENT_STATUS.CANCELLED]: '#999999',
  [APPOINTMENT_STATUS.EXPIRED]: '#999999',
  [APPOINTMENT_STATUS.SIGNED_IN]: '#FF9900',
  [APPOINTMENT_STATUS.PRECHECK_PASS]: '#FF9900',
  [APPOINTMENT_STATUS.PRECHECK_FAIL]: '#EE0A24',
  [APPOINTMENT_STATUS.OBSERVING]: '#FF9900'
}

// 进行中状态组
export const IN_PROGRESS_STATUSES = [1, 6, 7, 10]
// 终态组
export const TERMINAL_STATUSES = [2, 3, 4, 9]

// ==================== 批次状态 ====================
export const BATCH_STATUS = {
  NORMAL: 0,
  NEAR_EXPIRY: 1,
  EXPIRED: 2,
  DISPOSED: 3
}

export const BATCH_STATUS_TEXT = {
  [BATCH_STATUS.NORMAL]: '正常',
  [BATCH_STATUS.NEAR_EXPIRY]: '即将过期',
  [BATCH_STATUS.EXPIRED]: '已过期',
  [BATCH_STATUS.DISPOSED]: '已销毁'
}

export const BATCH_STATUS_COLOR = {
  [BATCH_STATUS.NORMAL]: '#07C160',
  [BATCH_STATUS.NEAR_EXPIRY]: '#FF9900',
  [BATCH_STATUS.EXPIRED]: '#EE0A24',
  [BATCH_STATUS.DISPOSED]: '#999999'
}

// ==================== 用户状态 ====================
export const USER_STATUS = {
  NORMAL: 0,
  DISABLED: 1,
  CANCELLED: 2
}

// ==================== 性别 ====================
export const GENDER = {
  UNKNOWN: 0,
  MALE: 1,
  FEMALE: 2
}

export const GENDER_TEXT = {
  [GENDER.UNKNOWN]: '未知',
  [GENDER.MALE]: '男',
  [GENDER.FEMALE]: '女'
}

// ==================== 证件类型 ====================
export const ID_CARD_TYPE = {
  ID_CARD: 1,
  PASSPORT: 2,
  OTHER: 3
}

export const ID_CARD_TYPE_TEXT = {
  [ID_CARD_TYPE.ID_CARD]: '身份证',
  [ID_CARD_TYPE.PASSPORT]: '护照',
  [ID_CARD_TYPE.OTHER]: '其他'
}

// ==================== 疫苗类型 ====================
export const VACCINE_CATEGORY = {
  CLASS_I: 'CLASS_I',    // 一类疫苗（免费）
  CLASS_II: 'CLASS_II'   // 二类疫苗（自费）
}

export const VACCINE_CATEGORY_TEXT = {
  [VACCINE_CATEGORY.CLASS_I]: '一类疫苗',
  [VACCINE_CATEGORY.CLASS_II]: '二类疫苗'
}

// ==================== 窗口职能类型 ====================
export const WINDOW_FUNCTION_TYPE = {
  SIGNIN: 'SIGNIN',
  PRECHECK: 'PRECHECK',
  REGISTER: 'REGISTER',
  VACCINATE: 'VACCINATE',
  OBSERVE: 'OBSERVE'
}

// ==================== 公告状态 ====================
export const NOTICE_STATUS = {
  PENDING: 0,
  PUBLISHED: 1,
  TAKEN_DOWN: 2,
  REJECTED: 3,
  EXPIRED: 4
}

export const NOTICE_STATUS_TEXT = {
  [NOTICE_STATUS.PENDING]: '待审批',
  [NOTICE_STATUS.PUBLISHED]: '已发布',
  [NOTICE_STATUS.TAKEN_DOWN]: '已下架',
  [NOTICE_STATUS.REJECTED]: '已拒绝',
  [NOTICE_STATUS.EXPIRED]: '已过期'
}

export const NOTICE_STATUS_COLOR = {
  [NOTICE_STATUS.PENDING]: '#FF9900',
  [NOTICE_STATUS.PUBLISHED]: '#07C160',
  [NOTICE_STATUS.TAKEN_DOWN]: '#999999',
  [NOTICE_STATUS.REJECTED]: '#EE0A24',
  [NOTICE_STATUS.EXPIRED]: '#999999'
}

// ==================== 公告类型 ====================
export const NOTICE_TYPE = {
  SYSTEM: 'SYSTEM',
  INTERNAL: 'INTERNAL'
}

export const NOTICE_TYPE_TEXT = {
  [NOTICE_TYPE.SYSTEM]: '系统公告',
  [NOTICE_TYPE.INTERNAL]: '内部公告'
}

// ==================== 角色编码 ====================
export const ROLE_CODES = {
  USER: 'USER',
  DOCTOR_SIGNIN: 'DOCTOR_SIGNIN',
  DOCTOR_PRECHECK: 'DOCTOR_PRECHECK',
  DOCTOR_REGISTER: 'DOCTOR_REGISTER',
  DOCTOR_VACCINATE: 'DOCTOR_VACCINATE',
  DOCTOR_OBSERVE: 'DOCTOR_OBSERVE',
  DOCTOR_STOCK: 'DOCTOR_STOCK',
  DOCTOR_BUSINESS_ADMIN: 'DOCTOR_BUSINESS_ADMIN',
  SUPER_ADMIN: 'SUPER_ADMIN'
}

// 角色分组
export const ROLE_GROUPS = {
  USER: 'USER',
  DOCTOR: 'DOCTOR',
  ADMIN: 'ADMIN'
}

// 流程医生角色集合（签到/预检/接种/留观）
export const FLOW_DOCTOR_ROLES = [
  ROLE_CODES.DOCTOR_SIGNIN,
  ROLE_CODES.DOCTOR_PRECHECK,
  ROLE_CODES.DOCTOR_REGISTER,
  ROLE_CODES.DOCTOR_VACCINATE,
  ROLE_CODES.DOCTOR_OBSERVE
]

// 管理员角色集合
export const ADMIN_ROLES = [
  ROLE_CODES.DOCTOR_BUSINESS_ADMIN,
  ROLE_CODES.SUPER_ADMIN
]

// ==================== 错误码 ====================
export const ERROR_CODES = {
  // 系统级 1000-1999
  UNAUTHORIZED: 1001,
  FORBIDDEN: 1002,
  BAD_REQUEST: 1003,
  NOT_FOUND: 1004,
  CONFLICT: 1005,
  TOO_MANY_REQUESTS: 1006,
  INTERNAL_ERROR: 1007,
  USER_FROZEN: 1011,
  NO_SHOW_FROZEN: 1012,

  // 预约模块 2000-2999
  APPOINT_CHILD_NOT_FOUND: 2001,
  APPOINT_SLOT_FULL: 2005,
  APPOINT_DUPLICATE: 2006,
  APPOINT_CANCEL_FORBIDDEN: 2007,
  APPOINT_NOT_FOUND: 2008,
  APPOINT_EXPIRED: 2009,

  // 流程模块 3000-3999
  SIGNIN_STATUS_INVALID: 3001,
  SIGNIN_IDCARD_MISMATCH: 3002,
  PRECHECK_STATUS_INVALID: 3003,
  OBSERVE_STATUS_INVALID: 3005,
  OBSERVE_TIME_INSUFFICIENT: 3006,
  ADVERSE_NOT_REPORTED: 3009,

  // 库存模块 4000-4999
  STOCK_TRANSFER_SAME_LOCATION: 4001,
  STOCK_TRANSFER_INSUFFICIENT: 4002,
  STOCK_BATCH_DISPOSED: 4003,
  STOCK_DISPOSE_EXCEED: 4004,
  STOCK_DISPOSE_REASON_EMPTY: 4005,
  STOCK_BATCH_NOT_FOUND: 4006,

  // 接种模块 6000-6999
  VACCINATE_STATUS_INVALID: 6001,
  VACCINATE_STOCK_INSUFFICIENT: 6002,
  VACCINATE_BATCH_EXPIRED: 6003,

  // 用户模块 7000-7999
  USER_PHONE_DUPLICATE: 7001,
  USER_LOGIN_FAILED: 7002,
  USER_NOT_FOUND: 7003,
  SMS_CODE_INVALID: 7007,
  LOGIN_LOCKED: 7008,
  CHILD_COUNT_EXCEEDED: 7011,
  CHILD_HAS_APPOINTMENT: 7012,
  CANNOT_MODIFY_SYSADMIN: 7013,

  // 管理模块 8000-8999
  SCHEDULE_CONFLICT: 8001,
  SCHEDULE_NOT_FOUND: 8002,
  WINDOW_CODE_DUPLICATE: 8003,
  WINDOW_IN_USE: 8004,
  NOTICE_NOT_FOUND: 8005,
  VACCINE_NOT_FOUND: 8006,
  ROLE_CODE_DUPLICATE: 8008,
  ROLE_NOT_FOUND: 8009,
  ROLE_SYSTEM_PROTECTED: 8010,
  ROLE_IN_USE: 8011
}

// ==================== 注射部位 ====================
export const INJECTION_SITES = [
  { value: 'LEFT_UPPER_ARM', label: '左上臂' },
  { value: 'RIGHT_UPPER_ARM', label: '右上臂' },
  { value: 'LEFT_BUTTOCK', label: '左臀' },
  { value: 'RIGHT_BUTTOCK', label: '右臀' }
]

// ==================== 健康状况 ====================
export const HEALTH_STATUS = [
  { value: 'GOOD', label: '良好' },
  { value: 'GENERAL', label: '一般' },
  { value: 'POOR', label: '较差' }
]

// ==================== 不良反应类型 ====================
export const ADVERSE_REACTION_TYPES = [
  { value: 'LOCAL_REACTION', label: '局部反应' },
  { value: 'ALLERGIC_REACTION', label: '过敏反应' },
  { value: 'FEVER', label: '发热' },
  { value: 'OTHER', label: '其他' }
]

// ==================== 严重程度 ====================
export const SEVERITY_LEVELS = [
  { value: 'MILD', label: '轻度' },
  { value: 'MODERATE', label: '中度' },
  { value: 'SEVERE', label: '重度' }
]

// ==================== 处理结果 ====================
export const HANDLE_RESULTS = [
  { value: 'RECOVERED', label: '好转' },
  { value: 'IMPROVED', label: '未好转' },
  { value: 'REFERRED', label: '转诊' }
]

// ==================== 轮询间隔（毫秒） ====================
export const POLLING_INTERVAL = {
  QUEUE: 30000,
  OBSERVE_QUEUE: 5000,
  OBSERVE_DETAIL: 1000,
  CALLING: 10000,
  HOME: 60000
}

/** 留观最短时间（分钟） */
export const OBSERVE_MIN_DURATION = 30

/** 叫号超时（分钟） */
export const CALL_TIMEOUT_MIN = 5

/** 最大过号次数 */
export const MAX_SKIP_COUNT = 3

/** 体温阈值 */
export const TEMPERATURE_THRESHOLD = 37.3

// ==================== 业务常量 ====================

/** 最大儿童数 */
export const MAX_CHILDREN = 5

/** 预约最大提前天数 */
export const MAX_APPOINTMENT_DAYS = 7

/** 留观最短时间（毫秒） */
export const MIN_OBSERVE_DURATION = 30 * 60 * 1000

/** 短信验证码倒计时（秒） */
export const SMS_COOLDOWN = 60

/** 叫号超时（毫秒） */
export const CALL_TIMEOUT = 5 * 60 * 1000

/** 叫号过号上限 */
export const MAX_MISS_COUNT = 3

/** 爽约冻结天数 */
export const NO_SHOW_FREEZE_DAYS = 7

// ==================== 管理员端常量 ====================

// 排班状态
export const SCHEDULE_STATUS = { NORMAL: 0, ON_LEAVE: 1, CANCELLED: 2 }
export const SCHEDULE_STATUS_TEXT = { 0: '正常', 1: '请假', 2: '已取消' }
export const SCHEDULE_STATUS_COLOR = { 0: '#07C160', 1: '#999999', 2: '#FF9900' }

// 窗口状态
export const WINDOW_STATUS = { OPEN: 0, CLOSED: 1 }
export const WINDOW_STATUS_TEXT = { 0: '开放', 1: '关闭' }
export const WINDOW_STATUS_COLOR = { 0: '#07C160', 1: '#999999' }

// 窗口职能类型
export const WINDOW_FUNCTION_TYPES = [
  { value: 'SIGNIN', label: '签到', color: '#1989FA' },
  { value: 'PRECHECK', label: '预检', color: '#07C160' },
  { value: 'REGISTER', label: '登记', color: '#FF9900' },
  { value: 'VACCINATE', label: '接种', color: '#EE0A24' },
  { value: 'OBSERVE', label: '留观', color: '#7232DD' }
]

// 疫苗管理状态
export const VACCINE_SHELF_STATUS = { ON_SHELF: 0, OFF_SHELF: 1 }
export const VACCINE_SHELF_STATUS_TEXT = { 0: '上架', 1: '下架' }
export const VACCINE_SHELF_STATUS_COLOR = { 0: '#07C160', 1: '#999999' }

// 公告管理状态
export const NOTICE_ADMIN_STATUS = { PENDING: 0, PUBLISHED: 1, TAKEN_DOWN: 2, REJECTED: 3, EXPIRED: 4 }
export const NOTICE_ADMIN_STATUS_TEXT = { 0: '待审批', 1: '已发布', 2: '已下架', 3: '已拒绝', 4: '已过期' }
export const NOTICE_ADMIN_STATUS_COLOR = { 0: '#FF9900', 1: '#07C160', 2: '#999999', 3: '#EE0A24', 4: '#999999' }

// 权限分组
export const PERMISSION_GROUPS = [
  { label: '排班权限', permissions: [
    { value: 'doctor.schedule.create', label: '创建排班' },
    { value: 'doctor.schedule.edit', label: '修改排班' },
    { value: 'doctor.schedule.view', label: '查看排班' },
    { value: 'doctor.schedule.delete', label: '删除排班' }
  ]},
  { label: '窗口权限', permissions: [
    { value: 'window.manage', label: '窗口管理' },
    { value: 'window.service.manage', label: '窗口服务配置' }
  ]},
  { label: '疫苗权限', permissions: [
    { value: 'vaccine.catalog.manage', label: '疫苗目录管理' },
    { value: 'vaccine.catalog.view', label: '疫苗目录查看' }
  ]},
  { label: '公告权限', permissions: [
    { value: 'notice.manage', label: '公告管理' },
    { value: 'notice.view', label: '公告查看' },
    { value: 'notice.audit', label: '公告审批' },
    { value: 'notice.feedback', label: '公告反馈查看' }
  ]},
  { label: '统计权限', permissions: [
    { value: 'stats.view', label: '统计查看' }
  ]},
  { label: '用户权限', permissions: [
    { value: 'user.manage', label: '用户管理' },
    { value: 'user.freeze', label: '用户冻结/解冻' }
  ]}
]

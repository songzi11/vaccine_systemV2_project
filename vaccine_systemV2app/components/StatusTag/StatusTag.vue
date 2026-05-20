<template>
  <view class="status-tag" :style="{ backgroundColor: bgColor, color: textColor }">
    <text class="status-tag-text">{{ displayText }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import {
  APPOINTMENT_STATUS_TEXT, APPOINTMENT_STATUS_COLOR,
  BATCH_STATUS_TEXT, BATCH_STATUS_COLOR,
  NOTICE_STATUS_TEXT, NOTICE_STATUS_COLOR,
  SCHEDULE_STATUS_TEXT, SCHEDULE_STATUS_COLOR
} from '@/utils/constants.js'

const props = defineProps({
  /** 状态码（数字或字符串） */
  status: { type: [Number, String], required: true },
  /** 文字映射表 */
  textMap: { type: Object, default: null },
  /** 颜色映射表 */
  colorMap: { type: Object, default: null },
  /** 预设类型：appointment / batch / notice / schedule */
  preset: { type: String, default: '' }
})

const colorPresets = {
  appointment: { text: APPOINTMENT_STATUS_TEXT, color: APPOINTMENT_STATUS_COLOR },
  batch: { text: BATCH_STATUS_TEXT, color: BATCH_STATUS_COLOR },
  notice: { text: NOTICE_STATUS_TEXT, color: NOTICE_STATUS_COLOR },
  schedule: { text: SCHEDULE_STATUS_TEXT, color: SCHEDULE_STATUS_COLOR }
}

const displayText = computed(() => {
  if (props.textMap) return props.textMap[props.status] || String(props.status)
  if (props.preset && colorPresets[props.preset]) {
    return colorPresets[props.preset].text[props.status] || String(props.status)
  }
  return String(props.status)
})

const bgColor = computed(() => {
  if (props.colorMap) return props.colorMap[props.status] || '#999999'
  if (props.preset && colorPresets[props.preset]) {
    return colorPresets[props.preset].color[props.status] || '#999999'
  }
  return '#999999'
})

const textColor = computed(() => '#FFFFFF')
</script>

<style lang="scss" scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 4rpx 16rpx;
  border-radius: $radius-sm;
}

.status-tag-text {
  font-size: $font-size-xs;
  line-height: 1;
}
</style>

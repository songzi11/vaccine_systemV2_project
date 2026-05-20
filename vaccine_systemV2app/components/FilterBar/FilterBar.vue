<template>
  <view class="filter-bar">
    <view v-for="item in dropdowns" :key="item.key" class="filter-dropdown">
      <picker :range="item.options" range-key="label" :value="getSelectedIndex(item)" @change="onPickerChange(item.key, $event)">
        <view class="picker-trigger">
          <text class="picker-text">{{ getSelectedLabel(item) }}</text>
          <uni-icons type="bottom" :size="12" color="#999" />
        </view>
      </picker>
    </view>
    <view v-if="showSearch" class="filter-search">
      <uni-easyinput v-model="internalKeyword" :placeholder="searchPlaceholder" @confirm="onSearch" @clear="onSearch" />
    </view>
    <view v-if="showReset" class="filter-reset" @tap="$emit('reset')">
      <uni-icons type="refreshempty" :size="16" color="#999" />
      <text class="reset-text">重置</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  dropdowns: { type: Array, default: () => [] },
  keyword: { type: String, default: '' },
  searchPlaceholder: { type: String, default: '搜索' },
  showSearch: { type: Boolean, default: true },
  showReset: { type: Boolean, default: true }
})

const emit = defineEmits(['update:keyword', 'filter-change', 'reset', 'search'])

const internalKeyword = ref(props.keyword)

function getSelectedIndex(item) {
  const selected = item.selected || ''
  const idx = item.options.findIndex(o => o.value === selected)
  return idx >= 0 ? idx : 0
}

function getSelectedLabel(item) {
  const selected = item.selected || ''
  const found = item.options.find(o => o.value === selected)
  return found ? found.label : item.options[0]?.label || '全部'
}

function onPickerChange(key, event) {
  const idx = event.detail.value
  const item = props.dropdowns.find(d => d.key === key)
  if (item) emit('filter-change', { key, value: item.options[idx]?.value || '' })
}

function onSearch() {
  emit('search', internalKeyword.value)
}
</script>

<style lang="scss" scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background: $color-bg-white;
  border-radius: $radius-lg;
  margin-bottom: $spacing-md;
  box-shadow: $shadow-card;
  flex-wrap: wrap;
}
.filter-dropdown { flex-shrink: 0; }
.picker-trigger {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx $spacing-sm;
  background: $color-bg-grey;
  border-radius: $radius-sm;
}
.picker-text { font-size: $font-size-sm; color: $color-text-secondary; }
.filter-search { flex: 1; min-width: 200rpx; }
.filter-reset {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx $spacing-sm;
  flex-shrink: 0;
}
.reset-text { font-size: $font-size-xs; color: $color-text-placeholder; }
</style>

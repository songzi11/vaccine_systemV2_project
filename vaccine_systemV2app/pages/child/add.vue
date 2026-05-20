<template>
  <view class="child-add-page">
    <uni-nav-bar :title="isEdit ? '编辑儿童' : '添加儿童'" :border="false">
      <template #right>
        <text class="nav-save" @tap="handleSave">保存</text>
      </template>
    </uni-nav-bar>

    <view class="form-section">
      <uni-forms ref="formRef" :modelValue="form" :rules="rules" validate-trigger="bind">
        <!-- 基本信息 -->
        <view class="form-group">
          <text class="group-title">基本信息</text>

          <uni-forms-item label="姓名" name="name" required>
            <uni-easyinput v-model="form.name" placeholder="请输入儿童姓名" maxlength="50" />
          </uni-forms-item>

          <uni-forms-item label="性别" name="gender" required>
            <uni-data-checkbox v-model="form.gender" :localdata="genderOptions" mode="button" />
          </uni-forms-item>

          <uni-forms-item label="出生日期" name="birthDate" required>
            <uni-datetime-picker type="date" v-model="form.birthDate" :end="todayStr" />
          </uni-forms-item>
        </view>

        <!-- 证件信息 -->
        <view class="form-group">
          <text class="group-title">证件信息</text>

          <uni-forms-item label="证件类型" name="idCardType">
            <uni-data-checkbox v-model="form.idCardType" :localdata="idCardTypeOptions" mode="button" />
          </uni-forms-item>

          <uni-forms-item label="证件号码" name="idCardNo" required>
            <uni-easyinput v-model="form.idCardNo" placeholder="请输入证件号码" />
          </uni-forms-item>
        </view>
      </uni-forms>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useChild } from '@/hooks/useChild.js'
import { useChildStore } from '@/store/child.js'
import { ID_CARD_REGEX } from '@/utils/validate.js'

const { addChild, updateChild } = useChild()
const childStore = useChildStore()

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  name: '',
  gender: 1,
  birthDate: '',
  idCardType: 1,
  idCardNo: ''
})

const editId = computed(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.$page?.options || currentPage.options || {}
  return options.id || null
})

const isEdit = computed(() => !!editId.value)

const todayStr = new Date().toISOString().split('T')[0]

const genderOptions = [
  { text: '男', value: 1 },
  { text: '女', value: 2 }
]

const idCardTypeOptions = [
  { text: '身份证', value: 1 },
  { text: '护照', value: 2 },
  { text: '其他', value: 3 }
]

const rules = {
  name: { rules: [{ required: true, errorMessage: '请输入姓名' }, { pattern: /^[\u4e00-\u9fa5·]{2,50}$/, errorMessage: '姓名仅支持中文，2-50位' }] },
  gender: { rules: [{ required: true, errorMessage: '请选择性别' }] },
  birthDate: { rules: [{ required: true, errorMessage: '请选择出生日期' }] },
  idCardNo: {
    rules: [
      { required: true, errorMessage: '请输入证件号码' }
    ]
  }
}

onShow(() => {
  if (isEdit.value) {
    loadChildData()
  }
})

async function loadChildData() {
  const child = childStore.children.find(c => c.id === editId.value)
  if (child) {
    form.name = child.name || ''
    const genderMap = { '男': 1, '女': 2 }
    form.gender = genderMap[child.gender] || 1
    form.birthDate = child.birthDate || ''
    const idCardTypeMap = { '身份证': 1, '护照': 2, '其他': 3 }
    form.idCardType = idCardTypeMap[child.idCardType] || 1
    form.idCardNo = child.idCardNo || ''
  }
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateChild(editId.value, { ...form })
      uni.showToast({ title: '修改成功', icon: 'success' })
    } else {
      await addChild({ ...form })
      uni.showToast({ title: '添加成功', icon: 'success' })
    }
    setTimeout(() => uni.navigateBack(), 1500)
  } catch {
    // 错误已由 interceptor 处理
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.child-add-page {
  min-height: 100vh;
  background-color: $color-bg-page;
}

.nav-save {
  font-size: $font-size-base;
  color: $color-primary;
  margin-right: $spacing-md;
}

.form-section {
  margin: $spacing-md;
  background-color: $color-bg-white;
  border-radius: $radius-lg;
  overflow: hidden;
}

.form-group {
  padding: $spacing-md;
  border-bottom: 1rpx solid $color-border-light;

  &:last-child {
    border-bottom: none;
  }
}

.group-title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin-bottom: $spacing-md;
}
</style>

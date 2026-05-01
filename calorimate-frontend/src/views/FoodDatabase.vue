<template>
  <div class="food-database">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="search-bar">
            <el-input
              v-model="searchName"
              placeholder="搜索食物名称"
              clearable
              style="width: 240px"
              @clear="loadFoods"
              @keyup.enter="loadFoods"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="searchCategory" placeholder="全部分类" clearable style="width: 160px; margin-left: 12px" @change="loadFoods">
              <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
            </el-select>
            <el-button type="primary" style="margin-left: 12px" @click="loadFoods">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="foodList" stripe>
        <el-table-column prop="name" label="食物名称" width="120" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="calories" label="卡路里(kcal)" width="120" sortable />
        <el-table-column prop="protein" label="蛋白质(g)" width="110" />
        <el-table-column prop="fat" label="脂肪(g)" width="100" />
        <el-table-column prop="carbs" label="碳水(g)" width="100" />
        <el-table-column prop="unit" label="单位" width="80" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadFoods"
          @current-change="loadFoods"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'

const searchName = ref('')
const searchCategory = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const foodList = ref([])

const categories = ['主食', '肉类', '蛋类', '蔬菜', '水果', '饮品', '豆制品', '海鲜']

const loadFoods = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchName.value) params.name = searchName.value
    if (searchCategory.value) params.category = searchCategory.value

    const res = await request.get('/food/list', { params })
    foodList.value = res.data.records
    total.value = res.data.total
  } catch (e) {}
}

onMounted(() => {
  loadFoods()
})
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.search-bar {
  display: flex;
  align-items: center;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

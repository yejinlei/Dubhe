/*
* Copyright 2019-2020 Zheng Jie
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <cdOperation>
        <span slot="right">
          <!-- 搜索 -->
          <el-input v-model="query.blurry" clearable placeholder="输入名称或者描述搜索" style="width: 200px;" class="filter-item" @change="crud.toQuery" />
          <el-date-picker
            v-model="query.createTime"
            :default-time="['00:00:00','23:59:59']"
            type="daterange"
            range-separator=":"
            class="date-item"
            value-format="timestamp"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :picker-options="pickerOptions"
            @change="crud.toQuery"
          />
          <rrOperation />
        </span>
      </cdOperation>
    </div>
    <!-- 表单渲染 -->
    <BaseModal
      :before-close="crud.cancelCU"
      :visible="crud.status.cu > 0"
      :title="crud.status.title"
      :loading="crud.status.cu === 2"
      width="600px"
      @cancel="crud.cancelCU"
      @ok="crud.submitCU"
    >
      <el-form ref="form" :inline="true" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" style="width: 455px;" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="描述信息" prop="remark">
          <el-input v-model="form.remark" style="width: 455px;" rows="3" type="textarea" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
    </BaseModal>
    <el-row :gutter="15">
      <!--角色管理-->
      <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="17" style="margin-bottom: 10px;">
        <el-card class="box-card" shadow="never">
          <div slot="header" class="clearfix">
            <span class="role-span">角色列表</span>
          </div>
          <el-table
            ref="table"
            v-loading="crud.loading"
            :data="crud.data"
            :row-class-name="tableRowClassName"
            @selection-change="crud.selectionChangeHandler"
          >
            <el-table-column type="selection" width="40" :selectable="checkboxT" />
            <el-table-column prop="name" label="名称" />
            <el-table-column show-overflow-tooltip prop="remark" label="描述" />
            <el-table-column show-overflow-tooltip prop="createTime" label="创建时间" width="160">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="170" fixed="right">
              <template slot-scope="scope">
                <udOperation :data="scope.row" :disabled-dle="isDisabled(scope.row.id)" />
                <el-button v-if="false" type="text" style="margin-left: 10px;" @click="handleCurrentChange(scope.row, scope.$index)">菜单分配</el-button>
              </template>
            </el-table-column>
          </el-table>
          <!--分页组件-->
          <pagination />
        </el-card>
      </el-col>
      <!-- 菜单授权 -->
      <el-col v-show="currentId" :xs="24" :sm="24" :md="8" :lg="8" :xl="7">
        <el-card class="box-card" shadow="never">
          <div slot="header" class="clearfix">
            <el-tooltip class="item" effect="dark" content="选择指定角色分配菜单" placement="top">
              <span class="role-span">菜单分配</span>
            </el-tooltip>
            <el-button
              :loading="menuLoading"
              icon="el-icon-check"
              style="float: right; padding: 6px 9px;"
              type="primary"
              @click="saveMenu"
            >保存</el-button>
          </div>
          <el-tree
            ref="menu"
            :data="menus"
            :defaultCheckedKeys="defaultCheckedKeys"
            :props="defaultProps"
            show-checkbox
            node-key="id"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import CRUD, { presenter, header, form, crud } from '@crud/crud';
import rrOperation from '@crud/RR.operation';
import cdOperation from '@crud/CD.operation';
import udOperation from '@crud/UD.operation';
import pagination from '@crud/Pagination';
import { validateName } from '@/utils/validate';
import crudRoles, { editMenu, get, getMenusTree }  from '@/api/system/role';
import BaseModal from '@/components/BaseModal';
import datePickerMixin from '@/mixins/datePickerMixin';

const DASHBOARD_MENU_ID = 1; // 概览页菜单ID
const ADMIN_ROLE_ID = 1; // 管理员角色id
const REGISTER_ROLE_ID = 2; // 注册用户角色id

const defaultForm = { id: null, name: null, remark: null };
export default {
  name: 'Role',
  components: { BaseModal, pagination, cdOperation, rrOperation, udOperation },
  cruds() {
    return CRUD({ title: '角色', crudMethod: { ...crudRoles }});
  },
  mixins: [presenter(), header(), form(defaultForm), crud(), datePickerMixin],
  data() {
    return {
      defaultProps: { children: 'children', label: 'label' },
      currentIndex: null, currentId: 0, menuLoading: false,
      menus: [], defaultCheckedKeys: [DASHBOARD_MENU_ID], leafNodeIds: [],
      rules: {
        name: [
          { required: true, message: '请输入角色名称', trigger: 'blur' },
          { validator: validateName, trigger: 'blur' },
        ],
      },
    };
  },
  created() {
    this.getMenus();
  },
  methods: {
    [CRUD.HOOK.afterRefresh]() {
      this.currentIndex = null;
      this.currentId = 0;
      this.$refs.menu.setCheckedKeys(this.defaultCheckedKeys);
    },
    // 提交前做的操作
    [CRUD.HOOK.afterValidateCU]() {
      return true;
    },
    // 获取所有菜单
    getMenus() {
      getMenusTree().then(res => {
        res = res || [];
        for (let index = 0; index < res.length; index += 1) {
          const node = res[index];
          if (this.defaultCheckedKeys.includes(node.id)) {
            node.disabled = true;
            break;
          }
        }
        this.menus = res;
        this.checkLeafNode();
      });
    },
    checkLeafNode() {
      const checkNode = (node) => {
        node.forEach(item => {
          if (!item.children) {
            this.leafNodeIds.push(item.id);
          } else {
            checkNode(item.children);
          }
        });
      };
      this.leafNodeIds = [];
      checkNode(this.menus);
    },
    tableRowClassName({ rowIndex }) {
      return rowIndex === this.currentIndex ? 'highlight-row' : '';
    },
    // 触发单选
    handleCurrentChange(val, index) {
      if (val) {
        // 保存当前的角色index 和 id
        this.currentIndex = index;
        this.currentId = val.id;
        // 菜单数据需要特殊处理
        const idSet = new Set(this.defaultCheckedKeys);
        val.menus.forEach(menu => {
          if (this.leafNodeIds.includes(menu.id)) {
            idSet.add(menu.id);
          }
        });
        this.$refs.menu.setCheckedKeys([...idSet]);
      }
    },
    // 保存菜单
    saveMenu() {
      this.menuLoading = true;
      const role = { id: this.currentId, menus: [] };
      // 得到半选的父节点数据，保存起来
      this.$refs.menu.getHalfCheckedKeys().forEach((data) => {
        const menu = { id: data };
        role.menus.push(menu);
      });
      // 得到已选中的 key 值
      this.$refs.menu.getCheckedKeys().forEach((data) => {
        const menu = { id: data };
        role.menus.push(menu);
      });
      editMenu(role).then(() => {
        this.crud.notify('保存成功', CRUD.NOTIFICATION_TYPE.SUCCESS);
        this.menuLoading = false;
        this.update();
      }).catch(err => {
        this.menuLoading = false;
        this.$message({
          message: err.message,
          type: 'error',
        });
      });
    },
    // 改变数据
    update() {
      // 无刷新更新 表格数据
      get(this.currentId).then(res => {
        for (let i = 0; i < this.crud.data.length; i+=1) {
          if (res.id === this.crud.data[i].id) {
            this.crud.data[i] = res;
            break;
          }
        }
      });
    },
    isDisabled(id) {
      return id === ADMIN_ROLE_ID || id === REGISTER_ROLE_ID;
    },
    checkboxT(row) {
      return !this.isDisabled(row.id);
    },
  },
};
</script>

<style rel="stylesheet/scss" lang="scss">
  .role-span {
    font-size: 15px;
    font-weight: bold;
    color: #303133;
  }
</style>

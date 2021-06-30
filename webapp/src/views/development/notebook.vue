/** Copyright 2020 Tianshu AI Platform. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================
 */

<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <cdOperation linkType="custom" @to-add="toAdd">
        <span slot="left">
          <el-tooltip
            content="Notebook 将会在开启后四小时自动关闭，请及时保存您的代码"
            placement="top"
          >
            <i class="el-icon-warning-outline primary f18" />
          </el-tooltip>
        </span>
        <span slot="right">
          <!-- 搜索 -->
          <el-input
            id="queryName"
            v-model="localQuery.noteBookName"
            clearable
            placeholder="请输入名称"
            class="filter-item"
            style="width: 200px;"
            @clear="crud.toQuery"
            @keyup.enter.native="crud.toQuery"
          />
          <rrOperation @resetQuery="onResetQuery" />
        </span>
      </cdOperation>
    </div>
    <create-dialog ref="create" @on-add="onAdded" />
    <!--右边侧边栏-->
    <el-drawer :visible.sync="drawer" :with-header="false" size="33%">
      <notebook-detail :itemObj="selectedItemObj" :notebookStatus="notebookStatus" />
    </el-drawer>
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="statusOptions.length === 0 || crud.loading"
      :data="crud.data"
      highlight-current-row
      @row-click="onRowClick"
    >
      <el-table-column prop="noteBookName" label="名称" />
      <el-table-column prop="cpuNum" label="规格">
        <template slot-scope="scope">
          <span>{{ scope.row.cpuNum }} CPU {{ scope.row.gpuNum }} GPU </span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="status" label="状态" width="150">
        <template #header>
          <dropdown-header
            title="状态"
            :list="notebookStatusList"
            :filtered="Boolean(localQuery.status) || localQuery.status === 0"
            @command="filterByStatus"
          />
        </template>
        <template slot-scope="scope">
          <el-tag
            v-if="!(scope.row.status == 0 && !scope.row.url)"
            :type="getTagType(scope.row.status)"
            effect="plain"
            >{{ notebookStatus[scope.row.status] }}
          </el-tag>
          <el-tag
            v-if="scope.row.status == 0 && !scope.row.url"
            :type="getTagType(3)"
            effect="plain"
            >{{ notebookStatus[3] }}
          </el-tag>
          <msg-popover
            :status-detail="scope.row.statusDetail"
            :show="showMessage(scope.row.status)"
          />
        </template>
      </el-table-column>
      <el-table-column show-overflow-tooltip prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.status === 1"
            :id="`start_` + scope.$index"
            type="text"
            @click.stop="doStart(scope.row)"
            >启动</el-button
          >
          <el-button
            v-if="scope.row.status === 1"
            :id="`delete_` + scope.$index"
            type="text"
            @click.stop="doDelete(scope.row)"
            >删除</el-button
          >
          <el-button
            v-if="scope.row.status === 0 && scope.row.url"
            :id="`open_` + scope.$index"
            type="text"
            @click.stop="doOpen(scope.row)"
          >
            打开<IconFont type="externallink" />
          </el-button>
          <el-button
            v-if="scope.row.status === 0 && scope.row.url"
            :id="`stop_` + scope.$index"
            type="text"
            @click.stop="doStop(scope.row)"
            >停止</el-button
          >
          <el-button
            v-if="
              ((scope.row.status === 0 && scope.row.url) || scope.row.status === 1) &&
                !scope.row.algorithmId
            "
            :id="`save_` + scope.$index"
            type="text"
            @click.stop="doSave(scope.row)"
            >保存算法</el-button
          >
          <i
            v-if="
              [3, 4, 5].includes(scope.row.status) || (scope.row.status === 0 && !scope.row.url)
            "
            class="el-icon-loading"
          />
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <pagination />
  </div>
</template>

<script>
// eslint-disable-next-line import/no-extraneous-dependencies
import { debounce } from 'throttle-debounce';

import notebookApi, { detail, getStatus, start, stop, open } from '@/api/development/notebook';
import { add as addAlgorithm } from '@/api/algorithm/algorithm';
import DropdownHeader from '@/components/DropdownHeader';
import MsgPopover from '@/components/MsgPopover';
import CRUD, { presenter, header, crud } from '@crud/crud';
import rrOperation from '@crud/RR.operation';
import cdOperation from '@crud/CD.operation';
import pagination from '@crud/Pagination';
import CreateDialog from './components/CreateDialog';
import NotebookDetail from './components/NotebookDetail';

const defaultQuery = {
  noteBookName: null,
  status: null,
};

export default {
  name: 'Notebook',
  components: {
    pagination,
    rrOperation,
    cdOperation,
    DropdownHeader,
    CreateDialog,
    NotebookDetail,
    MsgPopover,
  },
  cruds() {
    return CRUD({
      title: 'Notebook',
      crudMethod: { ...notebookApi },
      optShow: {
        del: false,
      },
      props: {
        optText: {
          add: '创建Notebook',
        },
      },
      queryOnPresenterCreated: false, // created 时不请求数据
    });
  },
  mixins: [presenter(), header(), crud()],
  data() {
    return {
      statusOptions: [],
      notebookStatus: {},
      drawer: false,
      selectedItemObj: {},
      pollingCount: 0,
      keepPoll: true,
      ct: null,
      localQuery: { ...defaultQuery },
    };
  },
  computed: {
    notebookStatusList() {
      return [{ label: '全部', value: null }].concat(
        this.statusOptions.map((status) => {
          return {
            label: status.statusName,
            value: status.statusCode,
          };
        })
      );
    },
  },
  mounted() {
    this.crud.msg.del = '正在删除';
    this.pollingCount = 0;
    if (this.$route.params?.noteBookName) {
      this.query.noteBookName = this.$route.params.noteBookName;
    }
    this.refetch = debounce(1000, this.crud.refresh);
    this.detailRefetch = debounce(2000, this.polling);
    this.getNotebookStatus();
  },
  beforeDestroy() {
    this.ct && clearTimeout(this.ct);
    this.keepPoll = false;
  },
  methods: {
    [CRUD.HOOK.afterRefresh]() {
      this.checkStatus();
    },
    getNotebookStatus() {
      getStatus().then((res) => {
        this.statusOptions = res;
        res.forEach((item) => {
          this.notebookStatus[item.statusCode] = item.statusName;
        });
        this.crud.refresh();
      });
    },
    filterByStatus(status) {
      this.localQuery.status = status;
      this.crud.toQuery();
    },
    checkStatus() {
      if (
        this.crud.data.some(
          (item) => [3, 4, 5].includes(item.status) || (item.status === 0 && !item.url)
        )
      ) {
        this.detailRefetch();
      }
    },
    async polling() {
      const idList = this.checkPollingIds();
      if (!this.keepPoll || !idList.length) {
        return;
      }
      const res = await detail(idList);
      this.pollingCount += 1;
      for (const item of res) {
        if (![0, 3, 4, 5].includes(item.status) || (item.status === 0 && item.url)) {
          // 如果变成了运行中的状态，需要判断url是否有值
          const ele = this.crud.data.find((d) => {
            return d.id === item.id;
          });
          ele.status = item.status;
          ele.updateTime = item.updateTime;
          ele.url = item.url;
          // 当变成运行中且有url时，自动打开url
          if (item.status === 0 && item.url) {
            window.open(item.url, '_blank');
          }
        }
      }
      if (res.length < idList.length) {
        this.crud.refresh();
        return;
      }
      if (
        this.crud.data.some((item) => [3, 4, 5].includes(item.status)) ||
        this.crud.data.some((item) => item.status === 0 && !item.url)
      ) {
        this.ct = setTimeout(() => {
          if (this.pollingCount < 200) {
            // 400s超时，超时不作提示
            this.detailRefetch();
          }
        }, 2000);
      }
    },
    checkPollingIds() {
      // 得到需要轮询的id，状态345，状态0但是没有url
      const idList = [];
      for (const item of this.crud.data) {
        if ([3, 4, 5].includes(item.status) || (item.status === 0 && !item.url)) {
          idList.push(item.id);
        }
      }
      return idList;
    },
    getTagType(status) {
      // 0运行，1停止, 2删除, 3启动中，4停止中，5删除中，6运行异常（暂未启用）
      switch (status) {
        case 0:
          return 'success';
        case 1:
          return 'danger';
        case 2:
          return 'danger';
        case 3:
          return 'info';
        case 4:
          return 'info';
        case 5:
          return 'info';
        case 6:
          return 'danger';
        default:
          return '';
      }
    },
    [CRUD.HOOK.beforeRefresh]() {
      this.crud.query = { ...this.localQuery };
    },
    toAdd() {
      this.$refs.create.showThis();
    },
    onResetQuery() {
      this.localQuery = { ...defaultQuery };
    },
    onAdded() {
      this.crud.toQuery();
    },
    // selection
    selectInit(item) {
      return ![3, 4, 5].includes(item.status);
    },
    // detail
    onRowClick(row) {
      this.selectedItemObj = row;
      this.drawer = true;
    },
    // start
    doStart(row) {
      start({ noteBookId: row.id }).then(() => {
        this.crud.refresh();
      });
    },
    // stop
    doStop(row) {
      this.$confirm('此操作将停止该环境, 是否继续?', '请确认').then(async () => {
        await stop({ noteBookId: row.id });
        this.crud.refresh();
      });
    },
    // open
    doOpen(row) {
      open(row.id).then((res) => {
        window.open(res);
        this.crud.refresh();
      });
    },
    async doSave(row) {
      const param = {
        codeDir: row.k8sPvcPath,
        algorithmName: row.noteBookName,
        noteBookId: row.id,
      };
      await addAlgorithm(param);
      this.crud.refresh();
      this.$message({
        message: '保存成功',
        type: 'success',
      });
    },
    doDelete(row) {
      this.$confirm('此操作将删除该环境, 是否继续?', '请确认').then(async () => {
        this.crud.doDelete(row);
      });
    },

    showMessage(status) {
      return [3, 6].includes(status);
    },
  },
};
</script>

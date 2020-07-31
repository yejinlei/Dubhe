/** Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

export default {
  name: 'DatasetAction',
  functional: true,
  props: {
    showPublish: Function,
    openUploadDialog: Function,
    goDetail: Function,
    getAutoAnnotateStatus: Function,
    autoAnnotate: Function,
    gotoVersion: Function,
    reAnnotation: Function,
    dataEnhance: Function,
  },
  render(h, { data, props }) {
    const { showPublish, openUploadDialog, goDetail, autoAnnotate, gotoVersion, reAnnotation, dataEnhance } = props;
    const columnProps = {
      ...data,
      scopedSlots: {
        header: () => {
          return (
            <span>
              <span>操作</span>
              <el-tooltip effect='dark' placement='top' style={{ marginLeft: '10px' }}>
                <div slot='content'>如果数据集操作没有更新，<br/>可能是后台算法在执行其他任务，<br/>请耐心等待或稍后重试</div>
                <i class='el-icon-question'/>
              </el-tooltip>
            </span>
          );
        },
        default: ({ row }) => {
          const btnProps = {
            props: {
              type: 'text',
              disabled: row.disabledAction,
            },
            style: {
              marginLeft: '0px',
              marginRight: '10px',
            },
          };

          // 查看标注按钮在 自动标注中(2) 未采样(5) 采样中(7) 数据增强中(8)时不显示, 此外，类型为视频时，自动标注完成(3)也不可查看(此时下游会进行目标跟踪)
          let showCheckButton = ![2, 5, 7, 8].includes(row.status);
          if (row.dataType === 1 && row.status === 3) {
            showCheckButton = false;
          }
          // 查看标注按钮
          const checkButton = (
            <el-button {...btnProps} onClick={() => goDetail(row)}>
              查看标注
            </el-button>
          );

          // 自动标注按钮在 自动标注中(2) 自动标注完成(3) 标注完成(4) 未采样(5) 目标跟踪完成(6) 采样中(7) 数据增强中(8)时不显示
          let showAutoButton = ![2, 3, 4, 5, 6, 7, 8].includes(row.status);
          // 自动标注按钮
          const autoButton = (
            <el-button {...btnProps} onClick={() => autoAnnotate(row)}>
              自动标注
            </el-button>
          );

          const showPublishEventProps = {
            on: {
              'onConfirm': () => showPublish(row),
            },
          };

          const reAnnotationEventProps = {
            on: {
              'onConfirm': () => reAnnotation(row),
            },
          };

          let showPublishButton = false;
          let publishButton = null;
          // 带有确认提示的发布按钮
          const publishConfirmButton = (
            <el-popconfirm
              popper-class='reannotate-popconfirm'
              placement='bottom'
              title='提示：自动标注已完成，尚未进行人工确认，是否发布？'
              width='200'
              {...showPublishEventProps}
            >
              <el-button slot='reference' {...btnProps}>发布</el-button>
            </el-popconfirm>
          );
          // 进入对话框的发布按钮
          const publishDialogButton = (
            <el-button {...btnProps} onClick={() => showPublish(row)}>
              发布
            </el-button>
          );

          // 当类型为视频时,状态为标注完成(4)目标跟踪完成(6)显示发布按钮,其余状态不显示发布按钮
          // 当类型为图片时,状态为自动标注完成(3)显示有弹窗确认的发布按钮,为标注完成(4)显示发布按钮,其余状态不显示发布按钮
          if (row.dataType === 1) {
            if ([4, 6].includes(row.status)) {
              showPublishButton = true;
              publishButton = publishDialogButton;
            }
          } else if (row.status === 3) {
            showPublishButton = true;
            publishButton = publishConfirmButton;
          } else if (row.status === 4) {
            showPublishButton = true;
            publishButton = publishDialogButton;
          }

          let showUploadButton = false;
          // 导入按钮
          const uploadButton = (
            <el-button {...btnProps} onClick={() => openUploadDialog(row)}>
              导入
            </el-button>
          );
          // 类型为视频时，当状态为未采样(5)时才可导入，其余状态不可导入
          // 类型为图片时，自动标注中(2) 数据增强中(8)不可导入，其余状态均可导入
          if (row.dataType === 1) {
            if (row.status === 5) {
              showUploadButton = true;
            }
          } else if (![2, 8].includes(row.status)) {
            showUploadButton = true;
          }

          // 当标注完成(4)目标跟踪完成(6)，以及非视频的自动标注完成(3)时显示重新自动标注按钮 (若为视频此时下游会进行目标跟踪)
          let showReAutoButton = [4, 6].includes(row.status) || (row.status === 3 && row.dataType === 0);
          // 重新自动标注按钮
          const reAutoButton = (
            <el-popconfirm
              popper-class='reannotate-popconfirm'
              placement='top-end'
              title='提示：确认清除现有标注并重新自动标注？'
              width='200'
              {...reAnnotationEventProps}
            >
              <el-button slot='reference' {...btnProps} >
                重新自动标注
              </el-button>
            </el-popconfirm>
          );

          // 展示数据增强入口
          // 当数据类型为图片,并且状态为自动标注完成(3) 标注完成(4)展示数据增强入口
          let showAugmentButton = row.dataType === 0 && [3, 4].includes(row.status);
          // 数据增强按钮
          const augmentButton = (
            <el-button {...btnProps} onClick={() => dataEnhance(row)}>
              数据增强
            </el-button>
          );

          // 有当前版本且状态不为自动标注中(2) 数据增强中(8)
          let showVersionButton = (row.currentVersionName && ![2, 8].includes(row.status));
          // 历史版本按钮
          const versionButton = (
            <el-button {...btnProps} onClick={() => gotoVersion(row)}>
              历史版本
            </el-button>
          );

          // 预置数据集只具备查看标注,历史版本功能。 
          if (row.type === 2) {
            showPublishButton = false;
            showUploadButton = false;
            showCheckButton = true;
            showAutoButton = false;
            showReAutoButton = false;
            showVersionButton = true;
            showAugmentButton = false;
          };
          // 导入的自定义数据集只允许删除操作
          if (row.import) {
            showPublishButton = false;
            showUploadButton = false;
            showCheckButton = false;
            showAutoButton = false;
            showReAutoButton = false;
            showVersionButton = false;
            showAugmentButton = false;
          };
          // 统计需要显示的按钮个数
          const buttonCount = (arr) => {
            let count = 0;
            arr.forEach(
              (item) => { if (item) count+=1; });
            return count;
          };
          const leftButtonArr = [showPublishButton, showUploadButton, showCheckButton, showAutoButton, showReAutoButton];
          const rightButtonArr = [showVersionButton, showAugmentButton];
          const leftButtonCount = buttonCount(leftButtonArr);
          const rightButtonCount = buttonCount(rightButtonArr);

          let moreButton = null;
          // 先判断是否有更多按钮
          // 当前左侧按钮不足三个时,不显示更多按钮
          if (rightButtonCount > 0) {
            if (leftButtonCount < 3) {
              moreButton = (
                <span>
                  {showVersionButton && versionButton}
                  {showAugmentButton && augmentButton}
                </span>
              );
            } else {
              moreButton = (
                <el-dropdown placement='bottom'>
                  <el-button {...btnProps}>
                    更多<i class='el-icon-arrow-down el-icon--right'></i>
                  </el-button>
                  <el-dropdown-menu slot='dropdown'>
                    <el-dropdown-item>
                      {showVersionButton && versionButton}
                    </el-dropdown-item>
                    <el-dropdown-item key='dataEnhance'>
                      {showAugmentButton && augmentButton}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              );
            }
          }

          return (
            <span>
              {showPublishButton && publishButton}
              {showUploadButton && uploadButton}
              {showCheckButton && checkButton}
              {showAutoButton && autoButton}
              {showReAutoButton && reAutoButton}
              {moreButton}
            </span>
          );
        },
      },
    };

    return h('el-table-column', columnProps);
  },
};

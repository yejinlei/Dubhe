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
 
import { statusCodeMap, dataTypeCodeMap, annotationCodeMap, isPublishDataset } from '../util';

export default {
  name: 'DatasetAction',
  functional: true,
  props: {
    showPublish: Function,
    uploadDataFile: Function,
    goDetail: Function,
    getAutoAnnotateStatus: Function,
    autoAnnotate: Function,
    gotoVersion: Function,
    reAnnotation: Function,
    track: Function,
    dataEnhance: Function,
    topDataset: Function,
    editDataset: Function,
    checkImport: Function, // 查询外部数据集导入状态
  },
  render(h, { data, props }) {
    const { showPublish, uploadDataFile, goDetail, autoAnnotate, gotoVersion, reAnnotation, track, dataEnhance, topDataset, editDataset, checkImport } = props;
    const columnProps = {
      ...data,
      scopedSlots: {
        header: () => {
          return (
            <span>
              <span>操作</span>
              <el-tooltip effect='dark' placement='top' class="ml-10">
                <div slot='content'>如果文本数据集关联的不是预置标签组，<br/>自动标注按钮可能无法使用</div>
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

          // 查看标注按钮在 自动标注中 未采样 采样中 采样失败 目标跟踪中 数据增强中 目标跟踪失败 时不显示, 此外，类型为视频时，自动标注完成也不可查看(此时下游会进行目标跟踪)
          let showCheckButton = !['AUTO_ANNOTATING', 'UNSAMPLED', 'SAMPLING', 'SAMPLE_FAILED', 'TRACKING', 'ENHANCING', 'TRACK_FAILED'].includes(statusCodeMap[row.status]);
          if (row.dataType === dataTypeCodeMap.VIDEO && statusCodeMap[row.status] === 'AUTO_ANNOTATED') {
            showCheckButton = false;
          }
          // 查看标注按钮根据版本发布的状态决定是否置灰加提示
          const checkButton = isPublishDataset(row) ? (
            <el-tooltip content="当前版本生成中，请稍后刷新" placement="top">
              <el-button {...btnProps}>
                <span style="color: #666; cursor: auto">查看标注</span>
              </el-button>
            </el-tooltip>
          ) : (
            <el-button {...btnProps} onClick={() => goDetail(row)}>
              查看标注
            </el-button>
          );

          // 自动标注按钮只在 未标注 标注中 时显示
          let showAutoButton = ['UNANNOTATED', 'ANNOTATING'].includes(statusCodeMap[row.status]);
          // 如果是文本分类，只有使用了预置标签组的数据集可以进行自动标注，autoAnnotation字段
          if(row.dataType === dataTypeCodeMap.TEXT && !row.autoAnnotation) {
            showAutoButton = false;
          }
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
          
          // 当类型为文本时,不显示发布按钮
          // 当类型为视频时,状态为标注完成、目标跟踪完成时显示发布按钮,其余状态不显示发布按钮
          // 当类型为图片时,状态为自动标注完成时显示有弹窗确认的发布按钮,为标注完成时显示发布按钮,其余状态不显示发布按钮
          if (row.dataType === dataTypeCodeMap.VIDEO) {
            if (['ANNOTATED', 'TRACK_SUCCEED'].includes(statusCodeMap[row.status])) {
              showPublishButton = true;
              publishButton = publishDialogButton;
            }
          } else if (row.dataType === dataTypeCodeMap.IMAGE) {
            if (statusCodeMap[row.status] === 'AUTO_ANNOTATED') {
              showPublishButton = true;
              publishButton = publishConfirmButton;
            } else if (statusCodeMap[row.status] === 'ANNOTATED') {
              showPublishButton = true;
              publishButton = publishDialogButton;
            }
          }

          let showUploadButton = false;
          // 导入按钮
          const uploadButton = (
            <el-button {...btnProps} onClick={() => uploadDataFile(row)}>
              导入
            </el-button>
          );
          // 类型为视频时，当状态为未采样时才可导入，其余状态不可导入
          // 类型为图片时，自动标注中、数据增强中 目标跟踪失败 不可导入，其余状态均可导入
          if (row.dataType === dataTypeCodeMap.VIDEO) {
            if (statusCodeMap[row.status] === 'UNSAMPLED') {
              showUploadButton = true;
            }
          } else if (!['AUTO_ANNOTATING', 'ENHANCING', 'TRACK_FAILED'].includes(statusCodeMap[row.status])) {
            showUploadButton = true;
          }

          // 当标注完成、目标跟踪完成，以及非视频的自动标注完成时显示重新自动标注按钮 (若为视频此时下游会进行目标跟踪)
          let showReAutoButton = ['ANNOTATED', 'TRACK_SUCCEED'].includes(statusCodeMap[row.status]) || (statusCodeMap[row.status] === 'AUTO_ANNOTATED' && row.dataType === dataTypeCodeMap.IMAGE);
          // 文本不能重新自动标注
          if (row.dataType === dataTypeCodeMap.TEXT) { 
            showReAutoButton = false; 
          }
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
          
          // 当目标跟踪标注类型的数据集状态为自动标注完成 标注完成时，显示目标跟踪按钮
          let showTrackButton = row.annotateType === annotationCodeMap.TRACK && ['AUTO_ANNOTATED','ANNOTATED'].includes(statusCodeMap[row.status]);
          // 目标跟踪按钮
          const trackButton = (
            <el-button {...btnProps} onClick={() => track(row, false)}>
              目标跟踪
            </el-button>
          );

          // 当目标跟踪失败时，显示重新目标跟踪按钮
          let showReTrackButton = ['TRACK_FAILED', 'TRACK_SUCCEED'].includes(statusCodeMap[row.status]);
          // 重新目标跟踪按钮
          const reTrackButton = (
            <el-button {...btnProps} onClick={() => track(row, true)}>
              重新目标跟踪
            </el-button>
          );

          // 展示数据增强入口
          // 当数据类型为图片,并且状态为自动标注完成、标注完成展示数据增强入口
          let showAugmentButton = row.dataType === dataTypeCodeMap.IMAGE && ['AUTO_ANNOTATED', 'ANNOTATED'].includes(statusCodeMap[row.status]);
          // 数据增强按钮
          const augmentButton = (
            <el-button {...btnProps} onClick={() => dataEnhance(row)}>
              数据增强
            </el-button>
          );

          // 有当前版本且状态不为自动标注中、数据增强中、目标跟踪中，导入中
          let showVersionButton = (row.currentVersionName && !['AUTO_ANNOTATING', 'ENHANCING', 'TRACKING', 'IMPORTING'].includes(statusCodeMap[row.status]));
          // 历史版本按钮
          const versionButton = (
            <el-button {...btnProps} onClick={() => gotoVersion(row)}>
              历史版本
            </el-button>
          );
          
          let showTopButton = true;
          // 置顶按钮总会显示
          const topButton = (
            <el-button {...btnProps} onClick={() => topDataset(row)}>
              {row.top ? '取消置顶' : '置顶'}
            </el-button>
          );

          let showEditButton = true;
          // 修改按钮总会显示
          const editButton = (
            <el-button {...btnProps} onClick={() => editDataset(row)}>
              修改
            </el-button>
          );

          // 导入外部数据集
          const showImportButton = row.import === true && ['UNANNOTATED'].includes(statusCodeMap[row.status]);

          // 外部导入数据集
          const importDatasetButton = showImportButton ? (
            <a {...btnProps} onClick={() => checkImport(row)} href="http://docs.dubhe.ai/docs/module/dataset/import-dataset" target="_blank" class="primary">
              导入本地数据集&nbsp;
              <IconFont type="externallink" />
            </a>
          ) : null;
          
          // 数据集版本发布或者切换中 只允许置顶 修改 历史版本，查看标注置灰
          if (isPublishDataset(row)) {
            showPublishButton = false;
            showUploadButton = false;
            showCheckButton = true;
            showAutoButton = false;
            showReAutoButton = false;
            showTrackButton = false;
            showReTrackButton = false;
            showVersionButton = true;
            showAugmentButton = false;
            showTopButton = true;
            showEditButton = true;
          };

          // 预置数据集只具备查看标注,历史版本功能。 
          if (row.type === 2) {
            showPublishButton = false;
            showUploadButton = false;
            showCheckButton = true;
            showAutoButton = false;
            showReAutoButton = false;
            showTrackButton = false;
            showReTrackButton = false;
            showVersionButton = true;
            showAugmentButton = false;
            showTopButton = false;
            showEditButton = false;
          };
          // 导入的自定义数据集只允许删除 置顶 修改操作
          if (row.import) {
            showUploadButton = false;
            showAutoButton = false;
            showReAutoButton = false;
            showTrackButton = false;
            showReTrackButton = false;
            showAugmentButton = false;
            // 导入完成才可以查看标注
            showCheckButton = (statusCodeMap[row.status] === 'ANNOTATED');
          };
          // 统计需要显示的按钮个数
          const buttonCount = (arr) => {
            let count = 0;
            arr.forEach(
              (item) => { if (item) count+=1; });
            return count;
          };
          const leftButtonArr = [showPublishButton, showUploadButton, showCheckButton, showAutoButton, showReAutoButton, showTrackButton];
          const rightButtonArr = [showVersionButton, showAugmentButton, showTopButton, showEditButton, showReTrackButton];
          const leftButtonCount = buttonCount(leftButtonArr);
          const rightButtonCount = buttonCount(rightButtonArr);

          let moreButton = null;
          // 先判断是否有更多按钮
          // 当前左侧按钮不足三个时,不显示更多按钮
          if (rightButtonCount > 0) {
            if (leftButtonCount < 3) {
              moreButton = (
                <span>
                  {showReTrackButton && reTrackButton}                  
                  {showVersionButton && versionButton}
                  {showAugmentButton && augmentButton}
                  {showTopButton && topButton}
                  {showEditButton && editButton}
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
                      {showReTrackButton && reTrackButton}
                    </el-dropdown-item>
                    <el-dropdown-item>
                      {showVersionButton && versionButton}
                    </el-dropdown-item>
                    <el-dropdown-item key='dataEnhance'>
                      {showAugmentButton && augmentButton}
                    </el-dropdown-item>                    
                    <el-dropdown-item key='top'>
                      {showTopButton && topButton}
                    </el-dropdown-item>
                    <el-dropdown-item key='edit'>
                      {showEditButton && editButton}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              );
            }
          }

          return (
            <span>
              { importDatasetButton }
              {showPublishButton && publishButton}
              {showUploadButton && uploadButton}
              {showCheckButton && checkButton}
              {showAutoButton && autoButton}
              {showTrackButton && trackButton}
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

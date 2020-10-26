/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
 * =============================================================
 */

package org.dubhe.service.convert;

import org.dubhe.base.MagicNumConstant;
import org.dubhe.enums.BizEnum;
import org.dubhe.k8s.domain.bo.PtJupyterResourceBO;
import org.dubhe.domain.entity.NoteBook;
import org.dubhe.utils.K8sNameTool;

/**
 * @description PtJupyterResourceBO 转换器
 * @date 2020-04-30
 */
public class PtJupyterResourceConvert {

    private PtJupyterResourceConvert(){

    }

    /**
     *  NoteBook 转换为 PtJupyterResourceBO
     *
     * @param noteBook
     * @param k8sNameTool
     * @param notebookDelayDeleteTime
     * @return PtJupyterResourceBO
     */
    public static PtJupyterResourceBO toPtJupyterResourceBo(NoteBook noteBook, K8sNameTool k8sNameTool, Integer notebookDelayDeleteTime){
        if (noteBook == null){
            return null;
        }
        PtJupyterResourceBO bo = new PtJupyterResourceBO();
        bo.setNamespace(noteBook.getK8sNamespace())
                .setName(noteBook.getK8sResourceName())
                .setCpuNum(noteBook.getCpuNum()* MagicNumConstant.ONE_THOUSAND)
                .setGpuNum(noteBook.getGpuNum()< MagicNumConstant.ONE?null:noteBook.getGpuNum())
                .setMemNum(noteBook.getMemNum()* MagicNumConstant.ONE_THOUSAND_TWENTY_FOUR)
                .setImage(noteBook.getK8sImageName())
                .setWorkspaceDir(k8sNameTool.getAbsoluteNfsPath(noteBook.getK8sPvcPath()))
                .setWorkspaceMountPath(noteBook.getK8sMountPath())
                // request和limit先一致
                .setWorkspaceRequest(noteBook.getDiskMemNum()*MagicNumConstant.ONE_THOUSAND_TWENTY_FOUR+"Mi")
                .setWorkspaceLimit(noteBook.getDiskMemNum()*MagicNumConstant.ONE_THOUSAND_TWENTY_FOUR+"Mi")
                .setBusinessLabel(k8sNameTool.getPodLabel(BizEnum.NOTEBOOK))
                .setDatasetDir(k8sNameTool.getAbsoluteNfsPath(noteBook.getDataSourcePath()))
                .setDatasetMountPath(k8sNameTool.getDatasetPath())
                .setDatasetReadOnly(true)
                .setDelayDeleteTime(notebookDelayDeleteTime)
        ;
        return bo;
    }

}

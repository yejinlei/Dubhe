/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
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

package org.dubhe.optimize.enums;

/**
 * @description 蒸馏算法命令行枚举
 * @date 2021-01-05
 */
public enum DistillCommandEnum {

    /**
     * 软标签蒸馏
     */
    KD("KD", "cd /usr/local/algorithm && bash run_train_teacher.sh %s %s %s %s %s && bash run_train_student_kd.sh %s %s %s %s %s"),
    /**
     * 软标签蒸馏，将BERT蒸馏到BiLSTM
     */
    BILSTM("Distilled-BiLSTM", "cd /usr/local/algorithm && bash run_train_teacher.sh %s %s %s %s %s && bash run_train_student_distilled_lstm.sh %s %s %s %s %s"),
    /**
     * 软标签蒸馏+层与层蒸馏
     */
    PKD("BERT-PKD", "cd /usr/local/algorithm && bash run_train_teacher.sh %s %s %s %s %s && bash run_train_student_bert_pkd.sh %s %s %s %s %s"),
    /**
     * 软标签蒸馏+层与层蒸馏+注意力蒸馏
     */
    TINY("TinyBERT", "cd /usr/local/algorithm && bash run_train_teacher.sh %s %s %s %s %s && bash run_train_student_tinybert.sh %s %s %s %s %s"),
    /**
     * 依照概率替换原有的BERT模块和Theseus的模块组成新的模型来训练
     */
    THESEUS("BERT-Theseus", "cd /usr/local/algorithm && bash run_train_teacher.sh %s %s %s %s %s && bash run_train_theseus.sh %s %s %s %s %s");


    /**
     * 算法名
     */
    private String name;

    /**
     * 命令行
     */
    private String command;

    DistillCommandEnum(String name, String command) {
        this.name = name;
        this.command = command;
    }

    /**
     * 根据名称获取命令行
     * @param name 蒸馏算法名称
     * @return 返回命令行
     */
    public static String getCommandByName(String name) {
        for (DistillCommandEnum commandEnum : DistillCommandEnum.values()) {
            if (commandEnum.getName().equals(name)) {
                return commandEnum.getCommand();
            }
        }
        return "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}

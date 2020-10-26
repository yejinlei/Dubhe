package org.dubhe.data.machine.utils.identify.setting;

import org.dubhe.data.machine.enums.DataStateEnum;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @description 状态判断设置类
 * @date 2020-09-24
 */
@Component
public class StateIdentifySetting {

    /**
     * 回退状态（自动标注失败/目标跟踪失败）使用
     */
    public static final Set<DataStateEnum> ROLL_BACK_FOR_STATE = new HashSet<DataStateEnum>() {{
        //自动标注中
        add(DataStateEnum.AUTOMATIC_LABELING_STATE);
        //目标跟踪中
        add(DataStateEnum.TARGET_FOLLOW_STATE);
    }};


    /**
     * 数据集状态需要使用文件状态去判断的
     */
    public static final Set<DataStateEnum> NEED_FILE_STATE_DO_IDENTIFY = new HashSet<DataStateEnum>() {{
        //未标注
        add(DataStateEnum.NOT_ANNOTATION_STATE);
        //手动标注中
        add(DataStateEnum.MANUAL_ANNOTATION_STATE);
        //自动标注完成
        add(DataStateEnum.AUTO_TAG_COMPLETE_STATE);
        //标注完成
        add(DataStateEnum.ANNOTATION_COMPLETE_STATE);
        //目标跟踪完成
        add(DataStateEnum.TARGET_COMPLETE_STATE);
    }};

}

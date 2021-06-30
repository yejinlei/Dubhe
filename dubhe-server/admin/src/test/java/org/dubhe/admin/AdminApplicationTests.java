package org.dubhe.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.admin.domain.dto.DictCreateDTO;
import org.dubhe.admin.domain.dto.DictDetailDTO;
import org.dubhe.admin.domain.dto.DictDetailQueryDTO;
import org.dubhe.admin.domain.entity.DictDetail;
import org.dubhe.admin.rest.DictController;
import org.dubhe.admin.service.DictDetailService;
import org.dubhe.biz.base.utils.DateUtil;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @description Admin启动类单元测试
 * @date: 2020-12-02
 */
@SpringBootTest
public class AdminApplicationTests {

    @Autowired
    private DictController dictController;
    @Autowired
    private DictDetailService dictDetailService;

    /**
     * 字典分页查询
     */
    @Test
    public void demo01() {

        Page<DictDetail> page = new Page<>();
        page.setCurrent(1);
        page.setSize(5);
        DictDetailQueryDTO dictQueryDTO = new DictDetailQueryDTO();
        dictQueryDTO.setDictId(3L);
        List<DictDetailDTO> dictDetailDTOS = dictDetailService.queryAll(dictQueryDTO);
        System.out.println(dictDetailDTOS.size());

    }

    /**
     * 字典创建
     */
    @Test
    public void demo02() {
        DictCreateDTO dict = new DictCreateDTO();
        dict.setName("测试");
        dict.setCreateTime(DateUtil.getCurrentTimestamp());
        dict.setRemark("观点");
        List<DictDetail> dictDetails = new ArrayList<>();
        DictDetail dictDetail = new DictDetail();
        dictDetail.setCreateTime(DateUtil.getCurrentTimestamp());
        dictDetail.setId(2222L);
        dictDetail.setLabel("开发");
        dictDetail.setSort("9");
        dictDetail.setValue("1");
        dictDetails.add(dictDetail);
        dict.setDictDetails(dictDetails);
        DataResponseBody dataResponseBody = dictController.create(dict);
        System.out.println(dataResponseBody.getData());

    }

}

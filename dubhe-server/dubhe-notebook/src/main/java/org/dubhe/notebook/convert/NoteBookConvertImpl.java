package org.dubhe.notebook.convert;

import org.dubhe.biz.base.dto.UserDTO;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.apache.commons.collections4.CollectionUtils;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.cloud.authconfig.service.AdminClient;
import org.dubhe.notebook.domain.entity.NoteBook;
import org.dubhe.notebook.domain.vo.NoteBookVO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class NoteBookConvertImpl implements NoteBookConvert {
    @Resource
    private AdminClient adminClient;

    @Resource
    private UserContextService  userContextService;

    @Override
    public NoteBook toEntity(NoteBookVO dto) {
        if (dto == null) {
            return null;
        }

        NoteBook noteBook = new NoteBook();

        noteBook.setCreateUserId(dto.getCreateUserId());
        noteBook.setUpdateUserId(dto.getUpdateUserId());
        if (dto.getCreateTime() != null) {
            noteBook.setCreateTime(new Timestamp(dto.getCreateTime().getTime()));
        }
        if (dto.getUpdateTime() != null) {
            noteBook.setUpdateTime(new Timestamp(dto.getUpdateTime().getTime()));
        }
        noteBook.setId(dto.getId());
        noteBook.setOriginUserId(dto.getOriginUserId());
        noteBook.setName(dto.getName());
        noteBook.setNoteBookName(dto.getNoteBookName());
        noteBook.setDescription(dto.getDescription());
        noteBook.setUrl(dto.getUrl());
        noteBook.setTotalRunMin(dto.getTotalRunMin());
        noteBook.setCpuNum(dto.getCpuNum());
        noteBook.setGpuNum(dto.getGpuNum());
        noteBook.setMemNum(dto.getMemNum());
        noteBook.setDiskMemNum(dto.getDiskMemNum());
        noteBook.setStatus(dto.getStatus());
        noteBook.setStatusDetail(dto.getStatusDetail());
        noteBook.setK8sStatusCode(dto.getK8sStatusCode());
        noteBook.setK8sStatusInfo(dto.getK8sStatusInfo());
        noteBook.setK8sNamespace(dto.getK8sNamespace());
        noteBook.setK8sResourceName(dto.getK8sResourceName());
        noteBook.setK8sImageName(dto.getK8sImageName());
        noteBook.setK8sPvcPath(dto.getK8sPvcPath());
        noteBook.setDataSourceName(dto.getDataSourceName());
        noteBook.setDataSourcePath(dto.getDataSourcePath());
        noteBook.setAlgorithmId(dto.getAlgorithmId());

        return noteBook;
    }

    @Override
    public NoteBookVO toDto(NoteBook entity) {
        if (entity == null) {
            return null;
        }

        NoteBookVO noteBookVO = new NoteBookVO();

        noteBookVO.setId(entity.getId());
        noteBookVO.setName(entity.getName());
        noteBookVO.setNoteBookName(entity.getNoteBookName());
        noteBookVO.setDescription(entity.getDescription());
        noteBookVO.setUrl(entity.getUrl());
        noteBookVO.setTotalRunMin(entity.getTotalRunMin());
        noteBookVO.setCpuNum(entity.getCpuNum());
        noteBookVO.setGpuNum(entity.getGpuNum());
        noteBookVO.setMemNum(entity.getMemNum());
        noteBookVO.setDiskMemNum(entity.getDiskMemNum());
        noteBookVO.setStatus(entity.getStatus());
        noteBookVO.setStatusDetail(entity.getStatusDetail());
        noteBookVO.setK8sStatusCode(entity.getK8sStatusCode());
        noteBookVO.setK8sStatusInfo(entity.getK8sStatusInfo());
        noteBookVO.setK8sNamespace(entity.getK8sNamespace());
        noteBookVO.setK8sResourceName(entity.getK8sResourceName());
        noteBookVO.setK8sImageName(entity.getK8sImageName());
        noteBookVO.setK8sPvcPath(entity.getK8sPvcPath());
        noteBookVO.setCreateTime(entity.getCreateTime());
        noteBookVO.setCreateUserId(entity.getCreateUserId());
        noteBookVO.setUpdateTime(entity.getUpdateTime());
        noteBookVO.setUpdateUserId(entity.getUpdateUserId());
        noteBookVO.setDataSourceName(entity.getDataSourceName());
        noteBookVO.setDataSourcePath(entity.getDataSourcePath());
        noteBookVO.setAlgorithmId(entity.getAlgorithmId());
        noteBookVO.setOriginUserId(entity.getOriginUserId());
        return noteBookVO;
    }

    @Override
    public List<NoteBook> toEntity(List<NoteBookVO> dtoList) {
        if (dtoList == null) {
            return null;
        }

        List<NoteBook> list = new ArrayList<NoteBook>(dtoList.size());
        for (NoteBookVO noteBookVO : dtoList) {
            list.add(toEntity(noteBookVO));
        }

        return list;
    }

    @Override
    public List<NoteBookVO> toDto(List<NoteBook> entityList) {
        if (entityList == null) {
            return null;
        }
        Map<Long, String> idUserNameMap = new HashMap<>();
        List<Long> userIds = entityList.stream().map(NoteBook::getCreateUserId).filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(userIds)) {
            DataResponseBody<List<UserDTO>> result = adminClient.getUserList(userIds);
            if (result.getData() != null) {
                idUserNameMap = result.getData().stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getUsername, (o, n) -> n));
            }
        }
        Map<Long, String> finalIdUserNameMap = idUserNameMap;
        List<NoteBookVO> list = new ArrayList<NoteBookVO>(entityList.size());
        for (NoteBook noteBook : entityList) {
            NoteBookVO noteBookVO = toDto(noteBook);
            if (BaseService.isAdmin(userContextService.getCurUser()) && noteBook.getCreateUserId() != null) {
                noteBookVO.setCreateUserName(finalIdUserNameMap.getOrDefault(noteBook.getCreateUserId(), null));
            }
            list.add(noteBookVO);
        }

        return list;
    }
}

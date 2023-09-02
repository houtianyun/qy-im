package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.TemplateGroup;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.TemplateCharacterMapper;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.ITemplateGroupService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.SelectableTemplateCharacterVO;
import xyz.qy.implatform.vo.TemplateCharacterVO;
import xyz.qy.implatform.vo.TemplateGroupVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-12 16:35
 **/
@Service
public class TemplateCharacterServiceImpl extends ServiceImpl<TemplateCharacterMapper, TemplateCharacter> implements ITemplateCharacterService {
    @Autowired
    private IGroupService groupService;

    @Autowired
    private ITemplateGroupService templateGroupService;

    @Autowired
    private IGroupMemberService groupMemberService;

    @Override
    public List<TemplateCharacterVO> findTemplateCharactersByGroupId(Long templateGroupId) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getId();
        LambdaQueryWrapper<TemplateCharacter> queryWrapper = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNull(templateGroupId)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "模板群聊id为空");
        }

        queryWrapper.eq(TemplateCharacter::getTemplateGroupId, templateGroupId);
        queryWrapper.eq(TemplateCharacter::getDeleted, Constant.NO);

        List<TemplateCharacter> templateCharacterList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(templateCharacterList)) {
            return Collections.emptyList();
        }

        List<TemplateCharacterVO> templateCharacterVOS = BeanUtils.copyProperties(templateCharacterList, TemplateCharacterVO.class);
        for (TemplateCharacterVO templateCharacterVO : templateCharacterVOS) {
            if (String.valueOf(userId).equals(templateCharacterVO.getCreateBy())) {
                templateCharacterVO.setIsOwner(true);
            }
        }
        return templateCharacterVOS;
    }

    @Override
    public List<TemplateCharacterVO> findSelectableTemplateCharacter(SelectableTemplateCharacterVO vo) {
        Group group = groupService.GetById(vo.getGroupId());
        if (ObjectUtil.isNull(group) || group.getDeleted()) {
            throw new GlobalException("群聊不存在或已被解散");
        }
        // 不是模板群聊
        if (group.getIsTemplate() != Constant.YES) {
            throw new GlobalException("该群聊不是模板群聊");
        }
        /*if (!vo.getTemplateGroupId().equals(group.getTemplateGroupId())) {
            throw new GlobalException("模板群聊参数错误");
        }*/
        TemplateGroup templateGroup = templateGroupService.getById(group.getTemplateGroupId());
        if (ObjectUtil.isNull(templateGroup) || Constant.DELETED == templateGroup.getDeleted()) {
            throw new GlobalException("模板群聊不存在或已被删除");
        }

        // 查询模板群聊下的所有模板人物
        List<TemplateCharacterVO> characterVOList = findTemplateCharactersByGroupId(vo.getTemplateGroupId());
        if (CollectionUtils.isEmpty(characterVOList)) {
            throw new GlobalException("数据异常");
        }
        // 查询模板群聊下已被用户选择的模板人物
        List<GroupMember> memberList = groupMemberService.findByGroupId(vo.getGroupId());
        if (CollectionUtils.isEmpty(memberList)) {
            throw new GlobalException("数据异常");
        }
        // 筛选出已被用户选择的模板人物
        Set<Long> templateCharacterIdSet = memberList.stream().filter(item -> !item.getQuit())
                .map(GroupMember::getTemplateCharacterId).collect(Collectors.toSet());
        characterVOList.forEach(item -> {
            if (templateCharacterIdSet.contains(item.getId())) {
                item.setSelectable(Boolean.FALSE);
                item.setChoosed(Boolean.TRUE);
            }
        });

        return characterVOList;
    }

    @Override
    @Transactional
    public void addOrModifyTemplateCharacters(TemplateGroupVO templateGroupVO) {
        UserSession session = SessionContext.getSession();

        if (ObjectUtil.isNull(templateGroupVO.getId())) {
            throw new GlobalException("模板群聊参数缺失");
        }
        TemplateGroup templateGroup = templateGroupService.getById(templateGroupVO.getId());
        if (ObjectUtil.isNull(templateGroup) || Constant.YES == templateGroup.getDeleted()) {
            throw new GlobalException("模板群聊不存在或已被删除");
        }
        if (ReviewEnum.REVIEWING.getCode().equals(templateGroup.getStatus())) {
            throw new GlobalException("数据正在审核中，不能修改");
        }
        List<TemplateCharacterVO> templateCharacterVOList = templateGroupVO.getTemplateCharacterVOList();
        if (CollectionUtils.isEmpty(templateCharacterVOList)) {
            throw new GlobalException("模板人物数据为空");
        }
        if (!session.getId().equals(Constant.ADMIN_USER_ID)
                && templateGroupVO.getTemplateCharacterVOList().size() > Constant.USER_MAX_TEMPLATE_CHARACTER_NUM) {
            throw new GlobalException("每个模板群聊最多只能创建" + Constant.USER_MAX_TEMPLATE_CHARACTER_NUM + "个模板人物");
        }

        List<TemplateCharacter> templateCharacters = BeanUtils.copyProperties(templateCharacterVOList, TemplateCharacter.class);
        templateCharacters.forEach(item -> {
            item.setTemplateGroupId(templateGroup.getId());
            item.setCreateBy(session.getId().toString());
            item.setUpdateBy(session.getId().toString());
            item.setStatus(ReviewEnum.TO_BE_REVIEW.getCode());
        });
        templateGroup.setStatus(ReviewEnum.TO_BE_REVIEW.getCode());
        templateGroup.setUpdateBy(session.getId().toString());
        templateGroup.setCount(templateCharacters.size());

        templateGroupService.updateById(templateGroup);
        this.saveOrUpdateBatch(templateCharacters);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserSession session = SessionContext.getSession();
        TemplateCharacter templateCharacter = baseMapper.selectById(id);
        if (ObjectUtil.isNull(templateCharacter)) {
            throw new GlobalException("当前模板人物不存在");
        }
        if (session.getId().toString().equals(templateCharacter.getCreateBy())) {
            throw new GlobalException("只有创建人才能删除");
        }
        Long templateGroupId = templateCharacter.getTemplateGroupId();
        TemplateGroup templateGroup = templateGroupService.getById(templateGroupId);
        templateGroup.setUpdateBy(session.getId().toString());

        templateCharacter.setDeleted(Constant.YES_STR);
        templateCharacter.setUpdateBy(session.getId().toString());
        baseMapper.updateById(templateCharacter);

        int count = this.countUserTemplateCharacter(templateGroupId);
        templateGroup.setCount(count);
        templateGroupService.updateById(templateGroup);
    }

    @Override
    public int countUserTemplateCharacter(Long templateGroupId) {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<TemplateCharacter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateCharacter::getDeleted, Constant.NO);
        queryWrapper.eq(TemplateCharacter::getCreateBy, session.getId());
        queryWrapper.eq(TemplateCharacter::getTemplateGroupId, templateGroupId);
        return this.count(queryWrapper);
    }
}

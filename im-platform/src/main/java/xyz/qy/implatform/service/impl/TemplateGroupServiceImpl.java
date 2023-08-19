package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.TemplateGroup;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.AuditResultEnum;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.TemplateGroupMapper;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.ITemplateGroupService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.ReviewVO;
import xyz.qy.implatform.vo.TemplateGroupVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-12 11:13
 **/
@Slf4j
@Service
public class TemplateGroupServiceImpl extends ServiceImpl<TemplateGroupMapper, TemplateGroup> implements ITemplateGroupService {
    @Autowired
    private ITemplateCharacterService templateCharacterService;

    @Autowired
    private IUserService userService;

    @Override
    public void addOrModify(TemplateGroupVO vo) {
        UserSession session = SessionContext.getSession();
        // 新增判断用户已创建的模板群聊数量是否大于10
        if (ObjectUtil.isNull(vo.getId()) && !session.getId().equals(Constant.ADMIN_USER_ID)) {
            int count = this.countUserTemplateGroup();
            if (count >= Constant.USER_MAX_TEMPLATE_GROUP_NUM) {
                throw new GlobalException("每位用户最多只能创建" + Constant.USER_MAX_TEMPLATE_GROUP_NUM + "个模板群聊");
            }
        }
        Long userId = session.getId();
        if (StringUtils.isBlank(vo.getGroupName())) {
            throw new GlobalException("模板群聊名称为空");
        }
        if (StringUtils.isBlank(vo.getAvatar())) {
            throw new GlobalException("模板群聊头像为空");
        }

        TemplateGroup templateGroup = BeanUtils.copyProperties(vo, TemplateGroup.class);
        assert templateGroup != null;
        if (ObjectUtil.isNull(templateGroup.getId())) {
            templateGroup.setCreateBy(userId.toString());
        }
        templateGroup.setUpdateBy(userId.toString());
        templateGroup.setStatus(ReviewEnum.TO_BE_REVIEW.getCode());
        this.saveOrUpdate(templateGroup);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getId();
        TemplateGroup templateGroup = baseMapper.selectById(id);
        if (ObjectUtil.isNull(templateGroup)) {
            throw new GlobalException("当前模板群聊已被删除");
        }
        templateGroup.setDeleted(Constant.YES);
        templateGroup.setUpdateBy(userId.toString());
        baseMapper.updateById(templateGroup);

        LambdaQueryWrapper<TemplateCharacter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateCharacter::getTemplateGroupId, templateGroup.getId());
        List<TemplateCharacter> templateCharacters = templateCharacterService.list(queryWrapper);
        if (CollectionUtils.isEmpty(templateCharacters)) {
            return;
        }
        templateCharacters.forEach(item -> {
            item.setDeleted(Constant.YES_STR);
            item.setUpdateBy(userId.toString());
        });
        templateCharacterService.updateBatchById(templateCharacters);
    }

    @Override
    public List<TemplateGroupVO> findTemplateGroups(TemplateGroupVO templateGroupVO) {
        LambdaQueryWrapper<TemplateGroup> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(templateGroupVO.getGroupName())) {
            queryWrapper.like(TemplateGroup::getGroupName, templateGroupVO.getGroupName());
        }
        queryWrapper.eq(TemplateGroup::getDeleted, Constant.NO);
        queryWrapper.eq(TemplateGroup::getStatus, ReviewEnum.REVIEWED.getCode());
        List<TemplateGroup> templateGroupList = this.list(queryWrapper);

        return BeanUtils.copyProperties(templateGroupList, TemplateGroupVO.class);
    }

    @Override
    public List<TemplateGroupVO> findMyTemplateGroups() {
        UserSession session = SessionContext.getSession();
        Long userId = session.getId();
        LambdaQueryWrapper<TemplateGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateGroup::getCreateBy, userId);
        queryWrapper.eq(TemplateGroup::getDeleted, Constant.NO);
        List<TemplateGroup> templateGroupList = this.list(queryWrapper);
        List<TemplateGroupVO> templateGroupVOS = BeanUtils.copyProperties(templateGroupList, TemplateGroupVO.class);
        templateGroupVOS.forEach(item -> item.setCreator(session.getUserName()));
        return templateGroupVOS;
    }

    @Override
    public int countUserTemplateGroup() {
        UserSession session = SessionContext.getSession();
        Long userId = session.getId();
        LambdaQueryWrapper<TemplateGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateGroup::getCreateBy, userId);
        queryWrapper.eq(TemplateGroup::getDeleted, Constant.NO);
        return this.count(queryWrapper);
    }

    @Transactional
    @Override
    public void submitForApproval(Long id) {
        UserSession session = SessionContext.getSession();
        TemplateGroup templateGroup = baseMapper.selectById(id);
        if (ObjectUtil.isNull(templateGroup)) {
            throw new GlobalException("模板群聊不存在");
        }
        if (ReviewEnum.REVIEWING.getCode().equals(templateGroup.getStatus())) {
            throw new GlobalException("数据已在审核中，请勿重复提交");
        }
        if (!session.getId().toString().equals(templateGroup.getCreateBy())) {
            throw new GlobalException("您不是创建人");
        }
        templateGroup.setStatus(ReviewEnum.REVIEWING.getCode());
        templateGroup.setUpdateBy(session.getId().toString());

        LambdaQueryWrapper<TemplateCharacter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateCharacter::getTemplateGroupId, templateGroup.getId());
        queryWrapper.eq(TemplateCharacter::getDeleted, Constant.NO);

        List<TemplateCharacter> templateCharacters = templateCharacterService.list(queryWrapper);
        if (CollectionUtils.isEmpty(templateCharacters)) {
            throw new GlobalException("请为该模板群聊上传模板人物信息");
        }
        templateCharacters.forEach(item -> {
            item.setStatus(ReviewEnum.REVIEWING.getCode());
            item.setUpdateBy(session.getId().toString());
        });
        baseMapper.updateById(templateGroup);
        templateCharacterService.updateBatchById(templateCharacters);
    }

    @Transactional
    @Override
    public void withdrawalOfApproval(Long id) {
        UserSession session = SessionContext.getSession();
        TemplateGroup templateGroup = baseMapper.selectById(id);
        if (ObjectUtil.isNull(templateGroup)) {
            throw new GlobalException("模板群聊不存在");
        }
        if (!ReviewEnum.REVIEWING.getCode().equals(templateGroup.getStatus())) {
            throw new GlobalException("当前数据不是审核中的状态");
        }
        if (!session.getId().toString().equals(templateGroup.getCreateBy())) {
            throw new GlobalException("您不是创建人");
        }
        templateGroup.setStatus(ReviewEnum.TO_BE_REVIEW.getCode());
        templateGroup.setUpdateBy(session.getId().toString());

        LambdaQueryWrapper<TemplateCharacter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateCharacter::getTemplateGroupId, templateGroup.getId());
        queryWrapper.eq(TemplateCharacter::getDeleted, Constant.NO);

        baseMapper.updateById(templateGroup);
        List<TemplateCharacter> templateCharacters = templateCharacterService.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(templateCharacters)) {
            templateCharacters.forEach(item -> {
                item.setStatus(ReviewEnum.TO_BE_REVIEW.getCode());
                item.setUpdateBy(session.getId().toString());
            });
            templateCharacterService.updateBatchById(templateCharacters);
        }
    }

    @Override
    public List<TemplateGroupVO> findReviewingTemplateGroups() {
        UserSession session = SessionContext.getSession();
        Long userid = session.getId();
        if (!userid.equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("当前只有管理员才能审批");
        }
        LambdaQueryWrapper<TemplateGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateGroup::getDeleted, Constant.NO);
        queryWrapper.eq(TemplateGroup::getStatus, ReviewEnum.REVIEWING.getCode());
        List<TemplateGroup> templateGroups = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(templateGroups)) {
            return Collections.emptyList();
        }
        List<String> userIds = templateGroups.stream().map(TemplateGroup::getCreateBy).collect(Collectors.toList());

        List<User> userList = userService.listByIds(userIds);
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity(), (key1, key2) -> key2));

        List<TemplateGroupVO> templateGroupVOS = BeanUtils.copyProperties(templateGroups, TemplateGroupVO.class);
        for (TemplateGroupVO templateGroupVO : templateGroupVOS) {
            if (userMap.containsKey(Long.parseLong(templateGroupVO.getCreateBy()))) {
                templateGroupVO.setCreator(userMap.get(Long.parseLong(templateGroupVO.getCreateBy()))
                        .getUserName());
            }
        }
        return templateGroupVOS;
    }

    @Transactional
    @Override
    public void submitAuditConclusion(ReviewVO reviewVO) {
        UserSession session = SessionContext.getSession();
        Long userid = session.getId();
        if (!userid.equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("当前只有管理员才能审批");
        }
        Long templateGroupId = reviewVO.getTemplateGroupId();
        TemplateGroup templateGroup = baseMapper.selectById(templateGroupId);
        if (!ReviewEnum.REVIEWING.getCode().equals(templateGroup.getStatus())) {
            throw new GlobalException("当前模板群聊状态不是审核中");
        }

        LambdaQueryWrapper<TemplateCharacter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateCharacter::getTemplateGroupId, templateGroupId);
        queryWrapper.eq(TemplateCharacter::getDeleted, Constant.NO);
        List<TemplateCharacter> templateCharacterList = templateCharacterService.list(queryWrapper);

        String status = AuditResultEnum.PASS.getCode().equals(reviewVO.getComments())
                ? ReviewEnum.REVIEWED.getCode() : ReviewEnum.NO_PASS.getCode();
        templateGroup.setStatus(status);
        templateGroup.setUpdateBy(userid.toString());
        templateCharacterList.forEach(item ->{
            item.setStatus(status);
            item.setUpdateBy(userid.toString());
        });

        baseMapper.updateById(templateGroup);
        templateCharacterService.updateBatchById(templateCharacterList);
    }
}

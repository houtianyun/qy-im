package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.qy.imclient.annotation.Lock;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.entity.Friend;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.TemplateGroup;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.GroupMapper;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.ITemplateGroupService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.CommonGroupVO;
import xyz.qy.implatform.vo.GroupInviteVO;
import xyz.qy.implatform.vo.GroupMemberVO;
import xyz.qy.implatform.vo.GroupVO;
import xyz.qy.implatform.vo.SwitchTemplateGroupVO;
import xyz.qy.implatform.vo.TemplateCharacterInviteVO;
import xyz.qy.implatform.vo.TemplateGroupCreateVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@CacheConfig(cacheNames = RedisKey.IM_CACHE_GROUP)
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements IGroupService {
    @Autowired
    private IUserService userService;

    @Autowired
    private IGroupMemberService groupMemberService;

    @Autowired
    private IFriendService friendsService;

    @Autowired
    private ITemplateGroupService templateGroupService;

    @Autowired
    private ITemplateCharacterService templateCharacterService;

    /**
     * 创建新群聊
     *
     * @Param groupName 群聊名称
     * @return
     **/
    @Transactional
    @Override
    public GroupVO createGroup(String groupName) {
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getId());
        // 保存群组数据
        Group group = new Group();
        group.setName(groupName);
        group.setOwnerId(user.getId());
        group.setHeadImage(user.getHeadImage());
        group.setHeadImageThumb(user.getHeadImageThumb());
        this.save(group);
        // 把群主加入群
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(group.getId());
        groupMember.setUserId(user.getId());
        groupMember.setAliasName(user.getNickName());
        groupMember.setRemark(groupName);
        groupMember.setHeadImage(user.getHeadImageThumb());
        groupMemberService.save(groupMember);
        GroupVO vo = BeanUtils.copyProperties(group, GroupVO.class);
        assert vo != null;
        vo.setAliasName(user.getNickName());
        vo.setRemark(groupName);
        log.info("创建群聊，群聊id:{},群聊名称:{}",group.getId(),group.getName());
        return vo;
    }

    /**
     * 修改群聊信息
     * 
     * @Param  GroupVO 群聊信息
     * @return
     **/
    @CacheEvict(key = "#vo.getId()")
    @Transactional
    @Override
    public GroupVO modifyGroup(GroupVO vo) {
        UserSession session = SessionContext.getSession();
        // 校验是不是群主，只有群主能改信息
        Group group = this.getById(vo.getId());
        // 群主有权修改群基本信息
        if(group.getOwnerId().equals(session.getId())){
            group = BeanUtils.copyProperties(vo,Group.class);
            this.updateById(group);
        }
        // 更新成员信息
        GroupMember member = groupMemberService.findByGroupAndUserId(vo.getId(),session.getId());
        if(member == null){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"您不是群聊的成员");
        }
        member.setAliasName(StringUtils.isEmpty(vo.getAliasName())?session.getNickName():vo.getAliasName());
        member.setRemark(StringUtils.isEmpty(vo.getRemark())? Objects.requireNonNull(group).getName():vo.getRemark());
        groupMemberService.updateById(member);
        assert group != null;
        log.info("修改群聊，群聊id:{},群聊名称:{}",group.getId(),group.getName());
        return vo;
    }

    /**
     * 删除群聊
     * 
     * @Param groupId 群聊id
     * @return
     **/
    @Transactional
    @CacheEvict(key = "#groupId")
    @Override
    public void deleteGroup(Long groupId) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(groupId);
        if(!group.getOwnerId().equals(session.getId())){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"只有群主才有权限解除群聊");
        }
        // 逻辑删除群数据
        group.setDeleted(true);
        this.updateById(group);
        // 删除成员数据
        groupMemberService.removeByGroupId(groupId);
        log.info("删除群聊，群聊id:{},群聊名称:{}",group.getId(),group.getName());
    }

    /**
     * 退出群聊
     *
     * @param groupId 群聊id
     * @return
     */
    @Override
    public void quitGroup(Long groupId) {
        Long userId = SessionContext.getSession().getId();
        Group group = this.getById(groupId);
        if(group.getOwnerId().equals(userId)){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"您是群主，不可退出群聊");
        }
        // 删除群聊成员
        groupMemberService.removeByGroupAndUserId(groupId,userId);
        log.info("退出群聊，群聊id:{},群聊名称:{},用户id:{}",group.getId(),group.getName(),userId);
    }

    /**
     * 将用户踢出群聊
     *
     * @param groupId 群聊id
     * @param userId 用户id
     * @return
     */
    @Override
    public void kickGroup(Long groupId, Long userId) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(groupId);
        if(!group.getOwnerId().equals(session.getId())){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"您不是群主，没有权限踢人");
        }
        if(userId.equals(session.getId())){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"亲，不能自己踢自己哟");
        }
        // 删除群聊成员
        groupMemberService.removeByGroupAndUserId(groupId,userId);
        log.info("踢出群聊，群聊id:{},群聊名称:{},用户id:{}",group.getId(),group.getName(),userId);
    }

    @Override
    public GroupVO findById(Long groupId) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(groupId);
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId,session.getId());
        if(member == null){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"您未加入群聊");
        }
        GroupVO vo = BeanUtils.copyProperties(group,GroupVO.class);
        assert vo != null;
        vo.setAliasName(member.getAliasName());
        vo.setRemark(member.getRemark());
        vo.setIsTemplateCharacter(member.getIsTemplate());
        vo.setTemplateCharacterId(member.getTemplateCharacterId());
        return  vo;
    }

    /**
     * 根据id查找群聊，并进行缓存
     *
     * @param groupId 群聊id
     * @return
     */
    @Cacheable(key = "#groupId")
    @Override
    public  Group GetById(Long groupId){
        Group group = super.getById(groupId);
        if(group == null){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"群组不存在");
        }
        if(group.getDeleted()){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"群组已解散");
        }
        return group;
    }

    /**
     * 查询当前用户的所有群聊
     *
     * @return
     **/
    @Override
    public List<GroupVO> findGroups() {
        UserSession session = SessionContext.getSession();
        // 查询当前用户的群id列表
        List<GroupMember> groupMembers = groupMemberService.findByUserId(session.getId());
        if(groupMembers.isEmpty()){
            return Collections.emptyList();
        }
        // 群id
        List<Long> ids = groupMembers.stream().map((GroupMember::getGroupId)).collect(Collectors.toList());
        QueryWrapper<Group> groupWrapper = new QueryWrapper();
        groupWrapper.lambda().in(Group::getId, ids);
        // 查询用户群聊信息
        List<Group> groups = this.list(groupWrapper);
        // 转vo
        List<GroupVO> vos = groups.stream().map(g -> {
            GroupVO vo = BeanUtils.copyProperties(g, GroupVO.class);
            GroupMember member = groupMembers.stream().filter(m -> g.getId().equals(m.getGroupId())).findFirst().get();
            assert vo != null;
            vo.setAliasName(member.getAliasName());
            vo.setRemark(member.getRemark());
            vo.setIsTemplateCharacter(member.getIsTemplate());
            vo.setTemplateCharacterId(member.getTemplateCharacterId());
            return vo;
        }).collect(Collectors.toList());
        return vos;
    }

    /**
     * 邀请好友进群
     *
     * @Param GroupInviteVO  群id、好友id列表
     * @return
     **/
    @Override
    @Lock(prefix = "im:group:member:modify", key = "#vo.getGroupId()")
    public void invite(GroupInviteVO vo) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(vo.getGroupId());
        if(group == null || group.getDeleted()){
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        if (!group.getIsTemplate().equals(vo.getIsTemplate())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊参数有误");
        }
        // 群聊人数校验
        List<GroupMember> members = groupMemberService.findByGroupId(vo.getGroupId());
        long size = members.stream().filter(m->!m.getQuit()).count();
        if(vo.getFriendIds().size() + size > Constant.MAX_GROUP_MEMBER){
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊人数不能大于"+Constant.MAX_GROUP_MEMBER+"人");
        }
        // 模板群聊人数不能超过模板群所有角色人物数量
        if(Constant.YES == group.getIsTemplate()) {
            // 查询模板群所有角色人物数量
            int characterCount = templateCharacterService.count(new LambdaQueryWrapper<TemplateCharacter>()
                    .eq(TemplateCharacter::getTemplateGroupId, group.getTemplateGroupId()));
            if (vo.getFriendIds().size() + size > characterCount) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前模板群聊人数不能大于" + characterCount + "人");
            }
            List<Long> templateCharacterIds = vo.getCharacterInviteVOList().stream()
                    .map(TemplateCharacterInviteVO::getTemplateCharacterId)
                    .collect(Collectors.toList());

            // 判断当前模板群聊是否包含所选模板人物
            List<TemplateCharacter> characterList = templateCharacterService.lambdaQuery()
                    .eq(TemplateCharacter::getTemplateGroupId, group.getTemplateGroupId())
                    .list();
            List<Long> collect = characterList.stream().map(TemplateCharacter::getId).collect(Collectors.toList());
            if (!collect.containsAll(templateCharacterIds)) {
                throw new GlobalException("当前模板群聊类型已改变，请重新选择");
            }

            // 判断新的群用户的模板角色人物是否已存在
            List<GroupMember> groupMemberList = groupMemberService.lambdaQuery()
                    .eq(GroupMember::getGroupId, group.getId())
                    .eq(GroupMember::getQuit, false)
                    .in(GroupMember::getTemplateCharacterId, templateCharacterIds).list();
            if (CollectionUtils.isNotEmpty(groupMemberList)) {
                List<TemplateCharacter> templateCharacters = templateCharacterService.listByIds(groupMemberList.stream()
                        .map(GroupMember::getTemplateCharacterId)
                        .collect(Collectors.toList()));
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前群聊已存在角色人物是"+templateCharacters.stream()
                        .map(TemplateCharacter::getName).collect(Collectors.joining(","))+"的用户");
            }
        }

        // 找出好友信息
        List<Friend> friends = friendsService.findFriendByUserId(session.getId());
        List<Friend> friendsList = vo.getFriendIds().stream().map(id ->
                friends.stream().filter(f -> f.getFriendId().equals(id)).findFirst().get()).collect(Collectors.toList());
        if (friendsList.size() != vo.getFriendIds().size()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "部分用户不是您的好友，邀请失败");
        }
        List<GroupMember> groupMembers = null;
        // 不是模板群聊
        if (Constant.NO == vo.getIsTemplate()) {
            // 批量保存成员数据
            groupMembers = friendsList.stream()
                    .map(f -> {
                        Optional<GroupMember> optional =  members.stream().filter(m-> m.getUserId().equals(f.getFriendId())).findFirst();
                        GroupMember groupMember = optional.orElseGet(GroupMember::new);
                        groupMember.setGroupId(vo.getGroupId());
                        groupMember.setUserId(f.getFriendId());
                        groupMember.setAliasName(f.getFriendNickName());
                        groupMember.setRemark(group.getName());
                        groupMember.setHeadImage(f.getFriendHeadImage());
                        groupMember.setCreatedTime(new Date());
                        groupMember.setQuit(false);
                        groupMember.setCharacterAvatarId(null);
                        groupMember.setAvatarAlias(null);
                        return groupMember;
                    }).collect(Collectors.toList());
        } else if (Constant.YES == vo.getIsTemplate()){
            groupMembers = vo.getCharacterInviteVOList().stream().map(f -> {
                Optional<GroupMember> optional =  members.stream().filter(m-> m.getUserId().equals(f.getFriendId())).findFirst();
                GroupMember groupMember = optional.orElseGet(GroupMember::new);
                groupMember.setGroupId(vo.getGroupId());
                groupMember.setUserId(f.getFriendId());
                groupMember.setAliasName(f.getTemplateCharacterName());
                groupMember.setRemark(group.getName());
                groupMember.setHeadImage(f.getTemplateCharacterAvatar());
                groupMember.setCreatedTime(new Date());
                groupMember.setIsTemplate(vo.getIsTemplate());
                groupMember.setTemplateCharacterId(f.getTemplateCharacterId());
                groupMember.setQuit(false);
                groupMember.setCharacterAvatarId(null);
                groupMember.setAvatarAlias(null);
                return groupMember;
            }).collect(Collectors.toList());
        }

        if(CollectionUtils.isNotEmpty(groupMembers)) {
            groupMemberService.saveOrUpdateBatch(group.getId(),groupMembers);
        }
        log.info("邀请进入群聊，群聊id:{},群聊名称:{},被邀请用户id:{}",group.getId(),group.getName(),vo.getFriendIds());
    }

    /**
     * 查询群成员
     *
     * @Param groupId 群聊id
     * @return List<GroupMemberVO>
     **/
    @Override
    public List<GroupMemberVO> findGroupMembers(Long groupId) {
        List<GroupMember> members = groupMemberService.findByGroupId(groupId);
        List<Long> userIds = members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
        List<User> userList = userService.listByIds(userIds);
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        List<GroupMemberVO> vos = members.stream().map(m->{
            GroupMemberVO vo = BeanUtils.copyProperties(m,GroupMemberVO.class);
            vo.setNickName(userMap.get(vo.getUserId()).getNickName());
            return  vo;
        }).collect(Collectors.toList());
        return vos;
    }

    @Override
    public GroupVO createTemplateGroup(TemplateGroupCreateVO templateGroupCreateVO) {
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getId());
        // 保存群组数据
        Group group = new Group();
        group.setName(templateGroupCreateVO.getTemplateGroupName());
        group.setOwnerId(user.getId());
        group.setHeadImage(templateGroupCreateVO.getTemplateGroupAvatar());
        group.setHeadImageThumb(templateGroupCreateVO.getTemplateGroupAvatar());
        group.setIsTemplate(Constant.YES);
        group.setTemplateGroupId(templateGroupCreateVO.getTemplateGroupId());
        this.save(group);
        // 把群主加入群
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(group.getId());
        groupMember.setUserId(user.getId());
        groupMember.setAliasName(templateGroupCreateVO.getTemplateCharacterName());
        groupMember.setRemark(group.getName());
        groupMember.setHeadImage(templateGroupCreateVO.getTemplateCharacterAvatar());
        groupMember.setIsTemplate(Constant.YES);
        groupMember.setTemplateCharacterId(templateGroupCreateVO.getTemplateCharacterId());
        groupMemberService.save(groupMember);
        GroupVO vo = BeanUtils.copyProperties(group, GroupVO.class);
        vo.setAliasName(groupMember.getAliasName());
        vo.setRemark(group.getName());
        log.info("创建群聊，群聊id:{},群聊名称:{}",group.getId(),group.getName());
        return vo;
    }

    @CacheEvict(key = "#vo.getGroupId()")
    @Lock(prefix = "im:group:member:modify", key = "#vo.getGroupId()")
    @Transactional
    @Override
    public GroupVO switchTemplateGroup(SwitchTemplateGroupVO vo) {
        // 判断是否群主
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getId());
        Group group = baseMapper.selectById(vo.getGroupId());
        if (!user.getId().equals(group.getOwnerId())) {
            throw new GlobalException("您不是群主");
        }
        // 判断上次切换时间与当前时间间隔
        Date switchTime = group.getSwitchTime();
        if (switchTime != null) {
            long interval = (new Date().getTime() - switchTime.getTime()) / 1000;
            if (interval < Constant.SWITCH_INTERVAL) {
                throw new GlobalException("距离上次切换群聊类型未超过30分钟");
            }
        }

        // 判断当前群聊里的模板人物是否重复
        List<GroupMemberVO> groupMembers = vo.getGroupMembers().stream()
                .filter(item -> !item.getQuit()).collect(Collectors.toList());
        List<Long> characterIds = groupMembers.stream()
                .map(GroupMemberVO::getTemplateCharacterId)
                .distinct()
                .collect(Collectors.toList());
        if (characterIds.size() != groupMembers.size()) {
            throw new GlobalException("当前模板群聊存在模板人物重复，请重新选择");
        }
        TemplateGroup templateGroup = templateGroupService.getById(vo.getTemplateGroupId());
        group.setTemplateGroupId(templateGroup.getId());
        group.setName(templateGroup.getGroupName());
        group.setHeadImage(templateGroup.getAvatar());
        group.setHeadImageThumb(templateGroup.getAvatar());
        group.setIsTemplate(Constant.YES);
        group.setSwitchTime(new Date());
        group.setRemark(templateGroup.getGroupName());
        baseMapper.updateById(group);

        Map<Long, GroupMemberVO> groupMemberMap = groupMembers.stream().collect(Collectors.toMap(GroupMemberVO::getUserId, Function.identity()));

        List<GroupMember> noQuitGroupMembers = groupMemberService.findNoQuitGroupMembers(vo.getGroupId());
        for (GroupMember groupMember : noQuitGroupMembers) {
            if (!groupMemberMap.containsKey(groupMember.getUserId())) {
                throw new GlobalException("有群聊成员未配置模板人物");
            }
            GroupMemberVO groupMemberVO = groupMemberMap.get(groupMember.getUserId());
            groupMember.setAliasName(groupMemberVO.getTemplateCharacterName());
            groupMember.setTemplateCharacterId(groupMemberVO.getTemplateCharacterId());
            groupMember.setHeadImage(groupMemberVO.getTemplateCharacterAvatar());
            groupMember.setRemark(templateGroup.getGroupName());
            groupMember.setIsTemplate(Constant.YES);
            groupMember.setCharacterAvatarId(null);
            groupMember.setAvatarAlias(null);
        }
        groupMemberService.saveOrUpdateBatch(group.getId(), noQuitGroupMembers);
        GroupVO groupVO = BeanUtils.copyProperties(group, GroupVO.class);
        groupVO.setAliasName(groupMemberMap.get(user.getId()).getTemplateCharacterName());
        return groupVO;
    }

    @CacheEvict(key = "#vo.getGroupId()")
    @Lock(prefix = "im:group:member:modify", key = "#vo.getGroupId()")
    @Transactional
    @Override
    public GroupVO switchCommonGroup(CommonGroupVO vo) {
        // 判断是否群主
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getId());
        Group group = baseMapper.selectById(vo.getGroupId());
        if (!user.getId().equals(group.getOwnerId())) {
            throw new GlobalException("您不是群主");
        }
        // 判断上次切换时间与当前时间间隔
        Date switchTime = group.getSwitchTime();
        if (switchTime != null) {
            long interval = (new Date().getTime() - switchTime.getTime()) / 1000;
            if (interval < Constant.SWITCH_INTERVAL) {
                throw new GlobalException("距离上次切换群聊类型未超过30分钟");
            }
        }
        // 查询群聊所有用户
        List<GroupMember> groupMemberList = groupMemberService.findByGroupId(vo.getGroupId());
        List<Long> userIds = groupMemberList.stream().map(GroupMember::getUserId).collect(Collectors.toList());

        // 查询所有用户信息
        List<User> userList = userService.listByIds(userIds);
        // 根据用户id分组
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        // 设置群用户信息
        for (GroupMember groupMember : groupMemberList) {
            groupMember.setIsTemplate(Constant.NO);
            groupMember.setTemplateCharacterId(0L);
            groupMember.setRemark(vo.getName());
            groupMember.setCharacterAvatarId(null);
            groupMember.setAvatarAlias(null);
            if (userMap.containsKey(groupMember.getUserId())) {
                User userInfo = userMap.get(groupMember.getUserId());
                groupMember.setHeadImage(userInfo.getHeadImage());
                groupMember.setAliasName(userInfo.getNickName());
            }
        }

        Map<Long, GroupMember> groupMemberMap = groupMemberList.stream().collect(Collectors.toMap(GroupMember::getUserId, Function.identity()));

        // 更新群用户信息
        groupMemberService.saveOrUpdateBatch(vo.getGroupId(), groupMemberList);

        // 设置群聊信息
        group.setIsTemplate(Constant.NO);
        group.setTemplateGroupId(null);
        group.setName(vo.getName());
        group.setRemark(vo.getName());
        if (StringUtils.isNotBlank(vo.getAvatar())) {
            group.setHeadImage(vo.getAvatar());
            group.setHeadImageThumb(vo.getAvatar());
        }
        group.setSwitchTime(new Date());
        // 更新群信息
        baseMapper.updateById(group);
        GroupVO groupVO = BeanUtils.copyProperties(group, GroupVO.class);
        groupVO.setAliasName(groupMemberMap.get(user.getId()).getAliasName());
        return groupVO;
    }

    @CacheEvict(key = Constant.COMMON_GROUP_ID + "")
    @Lock(prefix = "im:group:member:modify", key = Constant.COMMON_GROUP_ID + "")
    @Transactional
    @Override
    public GroupMember addToCommonGroup(User user) {
        Group group = baseMapper.selectById(Constant.COMMON_GROUP_ID);
        if (ObjectUtil.isNull(group)) {
            return null;

        }
        GroupMember groupMember = new GroupMember();
        // 是模板群聊
        if (Constant.YES == group.getIsTemplate()) {
            // 查询存在当前模板群聊的模板角色id
            List<GroupMember> groupMembers = groupMemberService.findNoQuitGroupMembers(group.getId());
            // 过滤得到所有已存在的模板角色id
            List<Long> templateCharacterIds = groupMembers.stream()
                    .filter(item -> Constant.YES == item.getIsTemplate())
                    .map(GroupMember::getTemplateCharacterId)
                    .collect(Collectors.toList());
            // 查询当前模板群聊还未分配的模板角色
            List<TemplateCharacter> templateCharacters = templateCharacterService.lambdaQuery()
                    .eq(TemplateCharacter::getTemplateGroupId, group.getTemplateGroupId())
                    .eq(TemplateCharacter::getDeleted, Constant.NO)
                    .notIn(TemplateCharacter::getId, templateCharacterIds)
                    .list();
            if (CollectionUtils.isEmpty(templateCharacters)) {
                return null;
            }
            // 不为空取第一个模板角色分配当前注册用户
            TemplateCharacter templateCharacter = templateCharacters.get(0);

            groupMember.setGroupId(group.getId());
            groupMember.setUserId(user.getId());
            groupMember.setAliasName(templateCharacter.getName());
            groupMember.setHeadImage(templateCharacter.getAvatar());
            groupMember.setIsTemplate(Constant.YES);
            groupMember.setTemplateCharacterId(templateCharacter.getId());
            groupMember.setRemark(group.getName());
        } else {
            groupMember.setGroupId(group.getId());
            groupMember.setUserId(user.getId());
            groupMember.setAliasName(user.getNickName());
            groupMember.setHeadImage(user.getHeadImage());
            groupMember.setIsTemplate(Constant.NO);
            groupMember.setRemark(group.getName());
        }
        groupMemberService.saveOrUpdateBatch(group.getId(), Collections.singletonList(groupMember));
        return groupMember;
    }
}

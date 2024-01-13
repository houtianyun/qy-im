package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import xyz.qy.imclient.IMClient;
import xyz.qy.imcommon.contant.Constant;
import xyz.qy.imcommon.model.GroupMessageInfo;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.GroupMessage;
import xyz.qy.implatform.entity.GroupMsgReadPosition;
import xyz.qy.implatform.enums.MessageStatus;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.GroupMessageMapper;
import xyz.qy.implatform.mapper.GroupMsgReadPositionMapper;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IGroupMessageService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.GroupMessageVO;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupMessageServiceImpl extends ServiceImpl<GroupMessageMapper, GroupMessage> implements IGroupMessageService {
    @Autowired
    private IGroupService groupService;

    @Autowired
    private IGroupMemberService groupMemberService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private IMClient imClient;

    @Resource
    private GroupMsgReadPositionMapper groupMsgReadPositionMapper;

    /**
     * 发送群聊消息(与mysql所有交换都要进行缓存)
     *
     * @param vo
     * @return 群聊id
     */
    @Override
    public Long sendMessage(GroupMessageVO vo) {
        Long userId = SessionContext.getSession().getUserId();
        Group group = groupService.getById(vo.getGroupId());
        if (group == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        if (group.getDeleted()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊已解散");
        }
        // 判断是否在群里
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(group.getId());
        if (!userIds.contains(userId)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法发送消息");
        }
        // 保存消息
        GroupMessage msg = BeanUtils.copyProperties(vo, GroupMessage.class);
        msg.setSendId(userId);
        msg.setSendTime(new Date());
        this.save(msg);
        // 不用发给自己
        userIds = userIds.stream().filter(id -> !userId.equals(id)).collect(Collectors.toList());
        // 群发
        GroupMessageInfo msgInfo = BeanUtils.copyProperties(msg, GroupMessageInfo.class);
        imClient.sendGroupMessage(userIds, msgInfo);
        log.info("发送群聊消息，发送id:{},群聊id:{},内容:{}", userId, vo.getGroupId(), vo.getContent());
        return msg.getId();
    }

    @Override
    public void sendGroupMessage(GroupMessageVO vo, Long sendUserId) {
        Group group = groupService.getById(vo.getGroupId());
        if (group == null) {
            return;
        }
        if (group.getDeleted()) {
            return;
        }
        // 判断是否在群里
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(group.getId());
        if (!userIds.contains(sendUserId)) {
            return;
        }
        // 保存消息
        GroupMessage msg = BeanUtils.copyProperties(vo, GroupMessage.class);
        msg.setSendId(sendUserId);
        msg.setSendTime(new Date());
        this.save(msg);
        // 不用发给自己
        //userIds = userIds.stream().filter(id-> !sendUserId.equals(id)).collect(Collectors.toList());
        // 群发
        GroupMessageInfo msgInfo = BeanUtils.copyProperties(msg, GroupMessageInfo.class);
        imClient.sendGroupMessage(userIds, msgInfo);
        log.info("发送群聊消息，发送id:{},群聊id:{},内容:{}", sendUserId, vo.getGroupId(), vo.getContent());
    }

    /**
     * 撤回消息
     *
     * @param id 消息id
     */
    @Override
    public void recallMessage(Long id) {
        Long userId = SessionContext.getSession().getUserId();
        GroupMessage msg = this.getById(id);
        if (msg == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息不存在");
        }
        if (!msg.getSendId().equals(userId)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "这条消息不是由您发送,无法撤回");
        }
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > Constant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息已发送超过5分钟，无法撤回");
        }
        // 判断是否在群里
        GroupMember member = groupMemberService.findByGroupAndUserId(msg.getGroupId(), userId);
        if (member == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法撤回消息");
        }
        // 修改数据库
        msg.setStatus(MessageStatus.RECALL.code());
        this.updateById(msg);
        // 群发
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(msg.getGroupId());
        // 不用发给自己
        userIds = userIds.stream().filter(uid -> userId.equals(uid)).collect(Collectors.toList());
        GroupMessageInfo msgInfo = BeanUtils.copyProperties(msg, GroupMessageInfo.class);
        msgInfo.setType(MessageType.TIP.code());
        String content = String.format("'%s'撤回了一条消息", member.getAliasName());
        msgInfo.setContent(content);
        msgInfo.setSendTime(new Date());
        imClient.sendGroupMessage(userIds, msgInfo);
        log.info("撤回群聊消息，发送id:{},群聊id:{},内容:{}", userId, msg.getGroupId(), msg.getContent());
    }

    /**
     * 异步拉取群聊消息，通过websocket异步推送
     *
     * @return
     */
    @Override
    public void pullUnreadMessage() {
        Long userId = SessionContext.getSession().getUserId();
        List<Long> recvIds = new LinkedList();
        recvIds.add(userId);
        List<GroupMember> members = groupMemberService.findByUserId(userId);
        List<GroupMessageInfo> allGroupMessageInfo = new LinkedList<>();
        for (GroupMember member : members) {
            // 获取群聊已读的最大消息id，只推送未读消息
            Integer maxReadedId = getGroupMsgReadId(member.getGroupId(), userId);
            QueryWrapper<GroupMessage> wrapper = new QueryWrapper();
            wrapper.lambda().eq(GroupMessage::getGroupId, member.getGroupId())
                    .ge(GroupMessage::getSendTime, member.getCreatedTime())
                    .ne(GroupMessage::getSendId, userId)
                    .ne(GroupMessage::getStatus, MessageStatus.RECALL.code());
            if (maxReadedId != null) {
                wrapper.lambda().gt(GroupMessage::getId, maxReadedId);
            }
            wrapper.last("limit 100");
            List<GroupMessage> messages = this.list(wrapper);
            if (messages.isEmpty()) {
                continue;
            }
            // 组装消息，准备推送
            List<GroupMessageInfo> messageInfos = messages.stream().map(m ->
                    BeanUtils.copyProperties(m, GroupMessageInfo.class)).collect(Collectors.toList());
            allGroupMessageInfo.addAll(messageInfos);
            log.info("拉取未读群聊消息，用户id:{},群聊id:{},数量:{}", userId, member.getGroupId(), messageInfos.size());
        }
        if (CollectionUtils.isEmpty(allGroupMessageInfo)) {
            return;
        }
        // 发送消息
        GroupMessageInfo[] infoArr = allGroupMessageInfo.toArray(new GroupMessageInfo[0]);
        // 不止一条未读群聊消息
        if (infoArr.length > 1) {
            for (int i = 0; i < infoArr.length; i++) {
                // 最后一条消息发送完才播放提示音
                if (i != infoArr.length - 1) {
                    infoArr[i].setPlayAudio(false);
                }
            }
        }
        imClient.sendGroupMessage(Collections.singletonList(userId), infoArr);
    }

    private Integer getGroupMsgReadId(Long groupId, Long userId) {
        String key = RedisKey.IM_GROUP_READED_POSITION + groupId + ":" + userId;
        Integer maxReadedId = (Integer) redisTemplate.opsForValue().get(key);
        if (ObjectUtil.isNull(maxReadedId)) {
            GroupMsgReadPosition groupMsgReadPosition = groupMsgReadPositionMapper.selectOne(new LambdaQueryWrapper<GroupMsgReadPosition>()
                    .eq(GroupMsgReadPosition::getGroupId, groupId)
                    .eq(GroupMsgReadPosition::getUserId, userId));
            if (ObjectUtil.isNotNull(groupMsgReadPosition)) {
                maxReadedId = groupMsgReadPosition.getGroupMsgId().intValue();
            }
        }
        return maxReadedId;
    }

    /**
     * 拉取历史聊天记录
     *
     * @param groupId 群聊id
     * @param page    页码
     * @param size    页码大小
     * @return 聊天记录列表
     */
    @Override
    public List<GroupMessageInfo> findHistoryMessage(Long groupId, Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        Long userId = SessionContext.getSession().getUserId();
        Long stIdx = (page - 1) * size;
        // 群聊成员信息
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, userId);
        if (member == null || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊中");
        }
        // 查询聊天记录，只查询加入群聊时间之后的消息
        QueryWrapper<GroupMessage> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMessage::getGroupId, groupId)
                .ge(GroupMessage::getSendTime, member.getCreatedTime())
                .ne(GroupMessage::getStatus, MessageStatus.RECALL.code())
                .orderByDesc(GroupMessage::getId)
                .last("limit " + stIdx + "," + size);

        List<GroupMessage> messages = this.list(wrapper);
        List<GroupMessageInfo> messageInfos = messages.stream().map(m ->
                BeanUtils.copyProperties(m, GroupMessageInfo.class)).collect(Collectors.toList());
        log.info("拉取群聊记录，用户id:{},群聊id:{}，数量:{}", userId, groupId, messageInfos.size());
        return messageInfos;
    }
}

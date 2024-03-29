package xyz.qy.implatform.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import xyz.qy.imclient.IMClient;
import xyz.qy.imcommon.contant.IMConstant;
import xyz.qy.imcommon.enums.IMTerminalType;
import xyz.qy.imcommon.model.IMGroupMessage;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.GroupMessageDTO;
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
import xyz.qy.implatform.service.IGroupMsgReadPositionService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.DateTimeUtils;
import xyz.qy.implatform.vo.GroupMessageVO;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    @Resource
    private IGroupMsgReadPositionService groupMsgReadPositionService;

    /**
     * 发送群聊消息(高并发接口，查询mysql接口都要进行缓存)
     *
     * @param dto 群聊消息
     * @return 群聊id
     */
    @Override
    public Long sendMessage(GroupMessageDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = groupService.getById(dto.getGroupId());
        if (Objects.isNull(group)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        if (group.getDeleted()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊已解散");
        }
        // 是否在群聊里面
        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getGroupId(), session.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法发送消息");
        }
        // 判断是否在群里
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(group.getId());
        // 不用发给自己
        userIds = userIds.stream().filter(id -> !session.getUserId().equals(id)).collect(Collectors.toList());
        // 保存消息
        GroupMessage msg = BeanUtils.copyProperties(dto, GroupMessage.class);
        msg.setSendId(session.getUserId());
        msg.setSendTime(new Date());
        msg.setSendNickName(member.getAliasName());
        if (CollUtil.isNotEmpty(dto.getAtUserIds())) {
            msg.setAtUserIds(StrUtil.join(",", dto.getAtUserIds()));
        }
        this.save(msg);
        // 不用发给自己
        userIds = userIds.stream().filter(id -> !session.getUserId().equals(id)).collect(Collectors.toList());
        // 群发
        GroupMessageVO msgInfo = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        msgInfo.setAtUserIds(dto.getAtUserIds());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setSendResult(false);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);
        log.info("发送群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), dto.getGroupId(), dto.getContent());
        return msg.getId();
    }

    @Override
    public void sendGroupMessage(GroupMessageDTO dto, Long sendUserId) {
        Group group = groupService.getById(dto.getGroupId());
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
        GroupMessage msg = BeanUtils.copyProperties(dto, GroupMessage.class);
        msg.setSendId(sendUserId);
        msg.setSendTime(new Date());
        if (CollUtil.isNotEmpty(dto.getAtUserIds())) {
            msg.setAtUserIds(StrUtil.join(",", dto.getAtUserIds()));
        }
        this.save(msg);
        // 不用发给自己
        //userIds = userIds.stream().filter(id-> !sendUserId.equals(id)).collect(Collectors.toList());
        // 群发
        GroupMessageVO msgInfo = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(sendUserId, IMTerminalType.WEB.code()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);
        log.info("发送群聊消息，发送id:{},群聊id:{},内容:{}", sendUserId, dto.getGroupId(), dto.getContent());
    }

    /**
     * 撤回消息
     *
     * @param id 消息id
     */
    @Override
    public void recallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        GroupMessage msg = this.getById(id);
        if (Objects.isNull(msg)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息不存在");
        }
        if (!msg.getSendId().equals(session.getUserId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "这条消息不是由您发送,无法撤回");
        }
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > IMConstant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息已发送超过5分钟，无法撤回");
        }
        // 判断是否在群里
        GroupMember member = groupMemberService.findByGroupAndUserId(msg.getGroupId(), session.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法撤回消息");
        }
        // 修改数据库
        msg.setStatus(MessageStatus.RECALL.code());
        this.updateById(msg);
        // 群发
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(msg.getGroupId());
        // 不用发给自己
        userIds = userIds.stream().filter(uid -> !session.getUserId().equals(uid)).collect(Collectors.toList());
        GroupMessageVO msgInfo = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        msgInfo.setType(MessageType.RECALL.code());
        String content = String.format("'%s'撤回了一条消息", member.getAliasName());
        msgInfo.setContent(content);
        msgInfo.setSendTime(new Date());

        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendGroupMessage(sendMessage);

        // 推给自己其他终端
        msgInfo.setContent("你撤回了一条消息");
        sendMessage.setSendToSelf(true);
        sendMessage.setRecvIds(Collections.emptyList());
        sendMessage.setRecvTerminals(Collections.emptyList());
        imClient.sendGroupMessage(sendMessage);
        log.info("撤回群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), msg.getGroupId(), msg.getContent());
    }

    /**
     * 异步拉取群聊消息，通过websocket异步推送
     *
     * @return
     */
    @Override
    public void pullUnreadMessage() {
        UserSession session = SessionContext.getSession();
        List<Long> recvIds = new LinkedList();
        recvIds.add(session.getUserId());
        List<GroupMember> members = groupMemberService.findByUserId(session.getUserId());
        for (GroupMember member : members) {
            // 获取群聊已读的最大消息id，只推送未读消息
            Integer maxReadedId = getGroupMsgReadId(member.getGroupId(), session.getUserId());
            LambdaQueryWrapper<GroupMessage> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(GroupMessage::getGroupId, member.getGroupId())
                    .ge(GroupMessage::getSendTime, member.getCreatedTime())
                    .ne(GroupMessage::getSendId, session.getUserId())
                    .ne(GroupMessage::getStatus, MessageStatus.RECALL.code());
            if (maxReadedId != null) {
                wrapper.gt(GroupMessage::getId, maxReadedId);
            }
            wrapper.last("limit 100");
            List<GroupMessage> messages = this.list(wrapper);
            if (messages.isEmpty()) {
                continue;
            }

            // 推送
            for (GroupMessage message : messages) {
                GroupMessageVO msgInfo = BeanUtils.copyProperties(message, GroupMessageVO.class);
                IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
                sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
                // 只推给自己当前终端
                sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
                sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
                sendMessage.setData(msgInfo);
                imClient.sendGroupMessage(sendMessage);
            }
            // 发送消息
            log.info("拉取未读群聊消息，用户id:{},群聊id:{},数量:{}", session.getUserId(), member.getGroupId(), messages.size());
        }
    }

    private Integer getGroupMsgReadId(Long groupId, Long userId) {
        String key = String.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId.toString(), userId.toString());
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
     * 拉取消息，只能拉取最近1个月的消息，一次拉取100条
     *
     * @param minId 消息起始id
     * @return 聊天消息列表
     */
    @Override
    public List<GroupMessageVO> loadMessage(Long minId) {
        UserSession session = SessionContext.getSession();
        List<GroupMember> members = groupMemberService.findByUserId(session.getUserId());
        if (CollectionUtil.isEmpty(members)) {
            return new ArrayList<>();
        }
        Map<Long, GroupMember> groupMemberMap = CollStreamUtil.toIdentityMap(members, GroupMember::getGroupId);
        Set<Long> ids = groupMemberMap.keySet();
        // 只能拉取最近1个月的
        Date minDate = DateTimeUtils.addMonths(new Date(), -1);
        LambdaQueryWrapper<GroupMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.gt(GroupMessage::getId, minId)
                .gt(GroupMessage::getSendTime, minDate)
                .in(GroupMessage::getGroupId, ids)
                .ne(GroupMessage::getStatus, MessageStatus.RECALL.code())
                .orderByAsc(GroupMessage::getId)
                .last("limit 100");

        List<GroupMessage> messages = this.list(wrapper);
        // 转成vo
        List<GroupMessageVO> vos = messages.stream()
                .filter(m -> {
                    //排除加群之前的消息
                    GroupMember member = groupMemberMap.get(m.getGroupId());
                    return Objects.nonNull(member) && DateUtil.compare(member.getCreatedTime(), m.getSendTime()) <= 0;
                })
                .map(m -> {
                    GroupMessageVO vo = BeanUtils.copyProperties(m, GroupMessageVO.class);
                    // 被@用户列表
                    if (StringUtils.isNotBlank(m.getAtUserIds()) && Objects.nonNull(vo)) {
                        List<String> atIds = Splitter.on(",").trimResults().splitToList(m.getAtUserIds());
                        vo.setAtUserIds(atIds.stream().map(Long::parseLong).collect(Collectors.toList()));
                    }
                    return vo;
                })
        .collect(Collectors.toList());
        // 消息状态,数据库没有存群聊的消息状态，需要从redis取
        List<String> keys = ids.stream()
                .map(id -> String.join(":", RedisKey.IM_GROUP_READED_POSITION, id.toString(), session.getUserId().toString()))
                .collect(Collectors.toList());
        List<Object> sendPos = redisTemplate.opsForValue().multiGet(keys);
        int idx = 0;
        for (Long id : ids) {
            Object o = sendPos.get(idx);
            if (Objects.isNull(o)) {
                o = groupMsgReadPositionService.getGroupMsgReadId(id, session.getUserId());
            }
            Integer sendMaxId = Objects.isNull(o) ? -1 : (Integer) o;
            vos.stream().filter(vo -> vo.getGroupId().equals(id)).forEach(vo -> {
                if (vo.getId() <= sendMaxId) {
                    // 已读
                    vo.setStatus(MessageStatus.READED.code());
                } else {
                    // 未推送
                    vo.setStatus(MessageStatus.UNSEND.code());
                }
            });
            idx++;
        }
        return vos;
    }

    /**
     * 消息已读,同步其他终端，清空未读数量
     *
     * @param groupId 群聊
     */
    @Override
    public void readedMessage(Long groupId) {
        UserSession session = SessionContext.getSession();
        // 取出最后的消息id
        LambdaQueryWrapper<GroupMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(GroupMessage::getGroupId, groupId)
                .orderByDesc(GroupMessage::getId)
                .last("limit 1")
                .select(GroupMessage::getId);
        GroupMessage message = this.getOne(wrapper);
        if (Objects.isNull(message)) {
            return;
        }
        // 推送消息给自己的其他终端
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.READED.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setSendId(session.getUserId());
        msgInfo.setGroupId(groupId);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setSendToSelf(true);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(true);
        imClient.sendGroupMessage(sendMessage);

        // 记录已读位置
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId, session.getUserId());
        redisTemplate.opsForValue().set(key, message.getId());
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
    public List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        Long userId = SessionContext.getSession().getUserId();
        long stIdx = (page - 1) * size;
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
        List<GroupMessageVO> messageInfos = messages.stream().map(m -> BeanUtils.copyProperties(m, GroupMessageVO.class)).collect(Collectors.toList());
        log.info("拉取群聊记录，用户id:{},群聊id:{}，数量:{}", userId, groupId, messageInfos.size());
        return messageInfos;
    }
}

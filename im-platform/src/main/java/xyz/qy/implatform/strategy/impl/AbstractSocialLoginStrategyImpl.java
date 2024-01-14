package xyz.qy.implatform.strategy.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.dto.GroupMessageDTO;
import xyz.qy.implatform.dto.PrivateMessageDTO;
import xyz.qy.implatform.dto.SocialTokenDTO;
import xyz.qy.implatform.dto.SocialUserInfoDTO;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.LoginTypeEnum;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.mapper.UserMapper;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.service.IGroupMessageService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.service.IPrivateMessageService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.strategy.SocialLoginStrategy;
import xyz.qy.implatform.util.CommonUtils;
import xyz.qy.implatform.util.IpUtils;
import xyz.qy.implatform.util.JwtUtil;
import xyz.qy.implatform.util.SysStringUtils;
import xyz.qy.implatform.vo.GroupMessageVO;
import xyz.qy.implatform.vo.IpGeoInfoVO;
import xyz.qy.implatform.vo.LoginVO;
import xyz.qy.implatform.vo.PrivateMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@Service
public abstract class AbstractSocialLoginStrategyImpl implements SocialLoginStrategy {
    @Resource
    private UserMapper userMapper;

    @Autowired
    private IFriendService friendService;

    @Autowired
    private IGroupService groupService;

    @Resource
    private HttpServletRequest request;

    @Autowired
    private IGroupMessageService groupMessageService;

    @Autowired
    private IPrivateMessageService privateMessageService;

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginVO login(String data) {
        // 获取第三方token信息
        SocialTokenDTO socialToken = getSocialToken(data);
        // 判断是否已注册
        User user = getUser(socialToken);
        // 获取用户ip信息
        String ipAddress = IpUtils.getIpAddress(request);
        String ipSource = IpUtils.getIpSource(ipAddress);
        if (ObjectUtil.isNull(user)) {
            user = saveUser(socialToken, ipAddress, ipSource);
        } else {
            user = updateUser(user, ipAddress, ipSource);
        }
        return jwtUtil.createToken(user, 0);
    }

    /**
     * 获取用户账号
     *
     * @return 用户账号
     */
    private User getUser(SocialTokenDTO socialTokenDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (LoginTypeEnum.QQ.getType().equals(socialTokenDTO.getLoginType())) {
            wrapper.eq(User::getQqOpenId, socialTokenDTO.getOpenId());
        }
        return userMapper.selectOne(wrapper);
    }

    /**
     * 获取第三方token信息
     *
     * @param data 数据
     * @return {@link SocialTokenDTO} 第三方token信息
     */
    public abstract SocialTokenDTO getSocialToken(String data);

    /**
     * 获取第三方用户信息
     *
     * @param socialTokenDTO 第三方token信息
     * @return {@link SocialUserInfoDTO} 第三方用户信息
     */
    public abstract SocialUserInfoDTO getSocialUserInfo(SocialTokenDTO socialTokenDTO);

    private User saveUser(SocialTokenDTO socialToken, String ipAddress, String ipSource) {
        // 获取第三方用户信息
        SocialUserInfoDTO socialUserInfo = getSocialUserInfo(socialToken);
        User user = new User();
        user.setUserName(userService.generateRandomUsername());
        user.setNickName(socialUserInfo.getNickname());
        user.setHeadImage(socialUserInfo.getAvatar());
        user.setHeadImageThumb(socialUserInfo.getAvatar());
        user.setPassword(passwordEncoder.encode(SysStringUtils.makeRandomPassword()));
        user.setIpAddress(ipAddress);
        user.setIpSource(ipSource);
        user.setLoginType(socialToken.getLoginType());
        if (LoginTypeEnum.QQ.getType().equals(socialToken.getLoginType())) {
            user.setQqOpenId(socialToken.getOpenId());
            user.setQqAccessToken(socialToken.getAccessToken());
        }
        IpGeoInfoVO ipGeoInfo = IpUtils.getIpGeoInfo(ipAddress);
        if (ObjectUtil.isNotNull(ipGeoInfo)) {
            user.setProvince(StringUtils.isBlank(ipGeoInfo.getPro()) ? ipSource : ipGeoInfo.getPro());
            user.setCity(ipGeoInfo.getCity());
        }
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.insert(user);
        try {
            GroupMember groupMember = groupService.addToCommonGroup(user);
            if (ObjectUtil.isNotNull(groupMember)) {
                GroupMessageDTO groupMessageVO = CommonUtils.buildGroupMessageVO(Constant.COMMON_GROUP_ID, CommonUtils.buildWelcomeMessage(user, groupMember), MessageType.TEXT.code());
                groupMessageService.sendGroupMessage(groupMessageVO, Constant.ADMIN_USER_ID);
            }
            if (!user.getId().equals(Constant.ADMIN_USER_ID)) {
                friendService.addFriend(user.getId(), Constant.ADMIN_USER_ID);
                PrivateMessageDTO privateMessageVO = CommonUtils.buildPrivateMessageVO(user.getId(), Constant.ADMIN_WELCOME_MSG, MessageType.TEXT.code());
                privateMessageService.sendPrivateMessage(privateMessageVO, Constant.ADMIN_USER_ID);
            }
        } catch (Exception e) {
            log.error("error:{}", e.getMessage());
        }
        return user;
    }

    private User updateUser(User user, String ipAddress, String ipSource) {
        user.setIpAddress(ipAddress);
        user.setIpSource(ipSource);
        IpGeoInfoVO ipGeoInfo = IpUtils.getIpGeoInfo(ipAddress);
        if (ObjectUtil.isNotNull(ipGeoInfo)) {
            user.setProvince(StringUtils.isBlank(ipGeoInfo.getPro()) ? ipSource : ipGeoInfo.getPro());
            user.setCity(ipGeoInfo.getCity());
        }
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }
}

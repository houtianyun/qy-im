package xyz.qy.implatform.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.qy.imclient.IMClient;
import xyz.qy.imcommon.contant.RedisKey;
import xyz.qy.implatform.config.JwtProperties;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.dto.LoginDTO;
import xyz.qy.implatform.dto.RegisterDTO;
import xyz.qy.implatform.entity.Friend;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.FilePathEnum;
import xyz.qy.implatform.enums.LoginTypeEnum;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.UserMapper;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IGroupMessageService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.service.IPrivateMessageService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.strategy.context.UploadStrategyContext;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.CommonUtils;
import xyz.qy.implatform.util.FileUtils;
import xyz.qy.implatform.util.ImageUtil;
import xyz.qy.implatform.util.IpUtils;
import xyz.qy.implatform.util.JwtUtil;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.util.SysStringUtils;
import xyz.qy.implatform.vo.GroupMessageVO;
import xyz.qy.implatform.vo.IpGeoInfoVO;
import xyz.qy.implatform.vo.LoginVO;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.PasswordVO;
import xyz.qy.implatform.vo.PrivateMessageVO;
import xyz.qy.implatform.vo.UploadImageVO;
import xyz.qy.implatform.vo.UserVO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IGroupMemberService groupMemberService;

    @Autowired
    private IFriendService friendService;

    @Autowired
    private IGroupService groupService;

    @Autowired
    private UploadStrategyContext uploadStrategyContext;

    @Autowired
    private RedisCache redisCache;

    @Resource
    private HttpServletRequest request;

    @Autowired
    private IGroupMessageService groupMessageService;

    @Autowired
    private IPrivateMessageService privateMessageService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private IMClient imClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 验证码校验
        validateCaptcha(dto.getCode(), dto.getUuid());
        User user = findUserByName(dto.getUserName());
        if (null == user) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "用户不存在");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new GlobalException(ResultCode.PASSWOR_ERROR);
        }
        recordLoginInfo(user);
        redisCache.deleteObject(RedisKey.CAPTCHA_CODE_KEY + dto.getUuid());
        // 生成token
        return jwtUtil.createToken(user, dto.getTerminal());
    }

    private void recordLoginInfo(User user) {
        String ipAddress = IpUtils.getIpAddress(request);
        String ipSource = IpUtils.getIpSource(ipAddress);
        user.setIpAddress(ipAddress);
        user.setIpSource(ipSource);
        user.setLoginType(LoginTypeEnum.USERNAME.getType());
        IpGeoInfoVO ipGeoInfo = IpUtils.getIpGeoInfo(ipAddress);
        if (ObjectUtil.isNotNull(ipGeoInfo)) {
            user.setProvince(StringUtils.isBlank(ipGeoInfo.getPro()) ? ipSource : ipGeoInfo.getPro());
            user.setCity(ipGeoInfo.getCity());
        }
        user.setLastLoginTime(LocalDateTime.now());
        baseMapper.updateById(user);
    }

    /**
     * 用refreshToken换取新 token
     *
     * @param refreshToken
     * @return
     */
    @Override
    public LoginVO refreshToken(String refreshToken) {
        //验证 token
        if(JwtUtil.checkSign(refreshToken, jwtProperties.getRefreshTokenSecret())){
            throw new GlobalException("refreshToken无效或已过期");
        }
        String strJson = JwtUtil.getInfo(refreshToken);
        Long userId = JwtUtil.getUserId(refreshToken);
        String accessToken = JwtUtil.sign(userId,strJson,jwtProperties.getAccessTokenExpireIn(),jwtProperties.getAccessTokenSecret());
        String newRefreshToken = JwtUtil.sign(userId,strJson,jwtProperties.getAccessTokenExpireIn(),jwtProperties.getAccessTokenSecret());
        LoginVO vo =new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(newRefreshToken);
        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        return vo;
    }

    /**
     * 用户注册
     *
     * @param vo 注册vo
     * @return
     */
    @Override
    public void register(RegisterDTO vo) {
        // 验证码校验
        validateCaptcha(vo.getCode(), vo.getUuid());
        validateSpecialChar(vo);
        if (!Validator.isGeneral(vo.getUserName())) {
            throw new GlobalException("用户名只能包含数字，字母，下划线");
        }
        User user = findUserByName(vo.getUserName());
        if (null != user) {
            throw new GlobalException(ResultCode.USERNAME_ALREADY_REGISTER);
        }
        user = BeanUtils.copyProperties(vo, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        try (InputStream inputStream = ImageUtil.getRandomAvatar()) {
            if (inputStream != null) {
                MultipartFile multipartFile = FileUtils.inputStreamToMultipartFile(System.currentTimeMillis() + ".png", inputStream);
                UploadImageVO uploadImageVO = uploadStrategyContext.executeUploadImageStrategy(multipartFile, FilePathEnum.AVATAR.getPath());
                user.setHeadImageThumb(uploadImageVO.getOriginUrl());
                user.setHeadImage(uploadImageVO.getOriginUrl());
            }
        } catch (Exception e) {
            log.error("get avatar error:{}", e.getMessage());
        }

        String ipAddress = IpUtils.getIpAddress(request);
        String ipSource = IpUtils.getIpSource(ipAddress);
        user.setIpAddress(ipAddress);
        user.setIpSource(ipSource);
        IpGeoInfoVO ipGeoInfo = IpUtils.getIpGeoInfo(ipAddress);
        if (ObjectUtil.isNotNull(ipGeoInfo)) {
            user.setProvince(StringUtils.isBlank(ipGeoInfo.getPro()) ? ipSource : ipGeoInfo.getPro());
            user.setCity(ipGeoInfo.getCity());
        }
        this.save(user);
        redisCache.deleteObject(RedisKey.CAPTCHA_CODE_KEY + vo.getUuid());
        try {
            GroupMember groupMember = groupService.addToCommonGroup(user);
            if (ObjectUtil.isNotNull(groupMember)) {
                GroupMessageVO groupMessageVO = CommonUtils.buildGroupMessageVO(Constant.COMMON_GROUP_ID, CommonUtils.buildWelcomeMessage(user, groupMember), MessageType.TEXT.code());
                groupMessageService.sendGroupMessage(groupMessageVO, Constant.ADMIN_USER_ID);
            }
            if (!user.getId().equals(Constant.ADMIN_USER_ID)) {
                friendService.addFriend(user.getId(), Constant.ADMIN_USER_ID);
                PrivateMessageVO privateMessageVO = CommonUtils.buildPrivateMessageVO(user.getId(), Constant.ADMIN_WELCOME_MSG, MessageType.TEXT.code());
                privateMessageService.sendPrivateMessage(privateMessageVO, Constant.ADMIN_USER_ID);
            }
        } catch (Exception e) {
            log.error("error:{}", e.getMessage());
        }
        log.info("注册用户，用户id:{},用户名:{},昵称:{}", user.getId(), vo.getUserName(), vo.getNickName());
    }

    private void validateCaptcha(String code, String uuid) {
        String verifyKey = xyz.qy.implatform.contant.RedisKey.CAPTCHA_CODE_KEY + uuid;
        String captcha = redisCache.getCacheObject(verifyKey);
        if (StringUtils.isBlank(captcha)) {
            throw new GlobalException(ResultCode.VERITY_CODE_NOT_EXIST, "验证码已过期");
        }

        if (!code.equals(captcha)) {
            throw new GlobalException(ResultCode.VERITY_CODE_ERROR, "验证码错误");
        }
    }

    private void validateSpecialChar(RegisterDTO vo) {
        if (SysStringUtils.checkSpecialChar(vo.getUserName())) {
            throw new GlobalException("用户名不能包含特殊字符");
        }
        if (SysStringUtils.checkSpecialChar(vo.getNickName())) {
            throw new GlobalException("昵称不能包含特殊字符");
        }
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return
     */
    @Override
    public User findUserByName(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(User::getUserName, username);
        return this.getOne(queryWrapper);
    }

    /**
     * 更新用户信息，好友昵称和群聊昵称等冗余信息也会更新
     *
     * @param vo 用户信息vo
     * @return
     */
    @Transactional
    @Override
    public void update(UserVO vo) {
        UserSession session = SessionContext.getSession();
        if (!session.getUserId().equals(vo.getId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "不允许修改其他用户的信息!");
        }
        User user = this.getById(vo.getId());
        if (null == user) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "用户不存在");
        }
        // 更新好友昵称和头像
        if (!user.getNickName().equals(vo.getNickName()) || !user.getHeadImageThumb().equals(vo.getHeadImageThumb())) {
            QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Friend::getFriendId, session.getUserId());
            List<Friend> friends = friendService.list(queryWrapper);
            for (Friend friend : friends) {
                friend.setFriendNickName(vo.getNickName());
                friend.setFriendHeadImage(vo.getHeadImageThumb());
            }
            friendService.updateBatchById(friends);
        }
        // 更新群聊中的头像
        if (!user.getHeadImageThumb().equals(vo.getHeadImageThumb())) {
            List<GroupMember> members = groupMemberService.findByUserId(session.getUserId());
            for (GroupMember member : members) {
                // 模板群聊不能修改用户聊天头像
                if (Constant.YES == member.getIsTemplate()) {
                    continue;
                }
                member.setHeadImage(vo.getHeadImageThumb());
            }
            groupMemberService.updateBatchById(members);
        }
        // 更新用户信息
        user.setNickName(vo.getNickName());
        user.setSex(vo.getSex());
        user.setSignature(vo.getSignature());
        user.setHeadImage(vo.getHeadImage());
        user.setHeadImageThumb(vo.getHeadImageThumb());
        this.updateById(user);
        log.info("用户信息更新，用户:{}}", user.toString());
    }

    /**
     * 根据用户昵称查询用户，最多返回20条数据
     *
     * @param nickname 用户昵称
     * @return
     */
    @Override
    public PageResultVO findUserByNickName(String nickname) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .ne(User::getId, userId)
                .and(StringUtils.isNotBlank(nickname),
                        qw -> qw.like(User::getNickName, nickname)
                                .or()
                                .like(User::getUserName, nickname))
                .orderByDesc(User::getCreateTime);
        Page<User> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), queryWrapper);
        List<User> users = page.getRecords();
        if (CollectionUtils.isEmpty(users)) {
            return PageResultVO.builder().data(Collections.EMPTY_LIST).build();
        }
        List<UserVO> vos = users.stream().map(u -> {
            UserVO vo = BeanUtils.copyProperties(u, UserVO.class);
            vo.setOnline(imClient.isOnline(u.getId()));
            return vo;
        }).collect(Collectors.toList());
        return PageResultVO.builder().data(vos).total(page.getTotal()).build();
    }

    /**
     * 判断用户是否在线，返回在线的用户id列表
     *
     * @param userIds 用户id，多个用‘,’分割
     * @return
     */
    @Override
    public List<Long> checkOnline(String userIds) {
        String[] idArr = userIds.split(",");
        List<Long> onlineIds = new LinkedList<>();
        for (String userId : idArr) {
            if (imClient.isOnline(Long.parseLong(userId))) {
                onlineIds.add(Long.parseLong(userId));
            }
        }
        return onlineIds;
    }

    @Override
    public String generateRandomUsername() {
        boolean flag = true;
        String username = null;
        int count = 0;
        while (flag) {
            username = RandomUtil.randomString(6);
            count++;
            List<User> users = baseMapper.selectList(new LambdaQueryWrapper<User>()
                    .eq(User::getUserName, username));
            if (CollectionUtils.isEmpty(users)) {
                break;
            }
            if (count > 20) {
                username = UUID.randomUUID().toString(true);
                flag = false;
            }
        }
        return username;
    }

    @Override
    public void modifyPassword(PasswordVO passwordVO) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        User user = baseMapper.selectById(userId);
        if (!passwordEncoder.matches(passwordVO.getOldPassword(), user.getPassword())) {
            throw new GlobalException("旧" + ResultCode.PASSWOR_ERROR);
        }
        user.setPassword(passwordEncoder.encode(passwordVO.getNewPassWord()));
        baseMapper.updateById(user);
    }
}

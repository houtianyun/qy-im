package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import xyz.qy.implatform.entity.Visitor;
import xyz.qy.implatform.mapper.VisitorMapper;
import xyz.qy.implatform.service.IVisitorService;
import xyz.qy.implatform.util.IpUtils;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.vo.IpGeoInfoVO;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

import static xyz.qy.implatform.contant.RedisKey.UNIQUE_VISITOR;

/**
 * @description: 访客信息
 * @author: Polaris
 * @create: 2023-04-29 09:40
 **/
@Slf4j
@Service
public class VisitorServiceImpl extends ServiceImpl<VisitorMapper, Visitor> implements IVisitorService {
    @Resource
    private HttpServletRequest request;

    @Autowired
    private RedisCache redisCache;

    @Override
    public void report() {
        // 获取ip
        String ipAddress = IpUtils.getIpAddress(request);
        if (StringUtils.isBlank(ipAddress)) {
            log.info("获取到空IP {}", LocalDateTime.now());
            return;
        }
        // 获取访问设备
        UserAgent userAgent = IpUtils.getUserAgent(request);
        Browser browser = userAgent.getBrowser();
        OperatingSystem operatingSystem = userAgent.getOperatingSystem();
        // 生成唯一用户标识
        String uuid = ipAddress + browser.getName() + operatingSystem.getName();
        String md5 = DigestUtils.md5DigestAsHex(uuid.getBytes());
        // 判断是否访问
        if (!redisCache.sIsMember(UNIQUE_VISITOR, md5)) {
            Visitor visitor = new Visitor();
            // 统计游客地域分布
            IpGeoInfoVO ipGeoInfoVO = IpUtils.getIpGeoInfo(ipAddress);
            if (ipGeoInfoVO != null) {
                BeanUtils.copyProperties(ipGeoInfoVO, visitor);
            }
            visitor.setIp(ipAddress);
            visitor.setBrowser(browser.getName());
            visitor.setOperatingSystem(operatingSystem.getName());
            baseMapper.insert(visitor);
            // 保存唯一标识
            redisCache.sAdd(UNIQUE_VISITOR, md5);
        }
    }
}

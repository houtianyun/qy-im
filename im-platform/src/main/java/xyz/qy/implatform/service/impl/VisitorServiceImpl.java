package xyz.qy.implatform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import xyz.qy.implatform.entity.Visitor;
import xyz.qy.implatform.mapper.VisitorMapper;
import xyz.qy.implatform.service.IVisitorService;
import xyz.qy.implatform.util.IpUtils;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.util.SpringContextUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Objects;

import static xyz.qy.implatform.contant.RedisKey.UNIQUE_VISITOR;

/**
 * @description: 访客信息
 * @author: Polaris
 * @create: 2023-04-29 09:40
 **/
@Slf4j
@Service
public class VisitorServiceImpl extends ServiceImpl<VisitorMapper, Visitor> implements IVisitorService {
    private static final int OK_CODE = 0;

    @Value("${tencent.location.key}")
    private String key;

    @Value("${tencent.location.sk}")
    private String sk;

    private static final String DEV_PROFILE = "dev";

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
            Visitor visitor = null;
            String activeProfile = SpringContextUtil.getActiveProfile();
            if (!DEV_PROFILE.equals(activeProfile)) {
                visitor = getVisitorInfoByIp(ipAddress);
            }
            if (Objects.isNull(visitor)) {
                visitor = new Visitor();
            }
            visitor.setIp(ipAddress);
            visitor.setAddr(IpUtils.getIpSource(ipAddress));
            visitor.setBrowser(browser.getName());
            visitor.setOperatingSystem(operatingSystem.getName());
            baseMapper.insert(visitor);
            // 保存唯一标识
            redisCache.sAdd(UNIQUE_VISITOR, md5);
        }
    }

    @Override
    public Visitor getVisitorInfoByIp(String ip) {
        Visitor visitor = null;
        try {
            String sign = IpUtils.getSign(ip, key, sk);
            JSONObject jsonObject = IpUtils.getIpLocationInfoByTencent(ip, key, sign);
            if (CollectionUtil.isNotEmpty(jsonObject) &&
                    jsonObject.getInteger("status").equals(OK_CODE)) {
                JSONObject resultJson = jsonObject.getJSONObject("result");
                if (CollectionUtil.isEmpty(resultJson)) {
                    return null;
                }
                JSONObject adInfo = resultJson.getJSONObject("ad_info");
                if (CollectionUtil.isEmpty(adInfo)) {
                    return null;
                }
                visitor = new Visitor();
                String nation = adInfo.getString("nation");
                String province = adInfo.getString("province");
                String city = adInfo.getString("city");
                visitor.setIp(ip);
                visitor.setNation(nation);
                visitor.setPro(province);
                visitor.setCity(city);
                visitor.setLocationInfo(resultJson.toJSONString());
            }
        } catch (Exception e) {
            log.error("getVisitorInfo error:{}", e.getMessage());
        }
        return visitor;
    }
}

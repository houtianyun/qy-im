package xyz.qy.implatform.util;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;
import xyz.qy.implatform.vo.IpGeoInfoVO;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * ip工具类
 *
 * @author 11921
 */
@SuppressWarnings("all")
@Slf4j
public class IpUtils {
    private static final String WHOIS_IP = "http://whois.pconline.com.cn/ipJson.jsp?ip=%s&json=true";

    /**
     * 腾讯位置服务
     */
    private static final String TENCENT_LOCATION_SERVICE = "https://apis.map.qq.com/ws/location/v1/ip?ip=%s&key=%s&sig=%s";

    /**
     * 百度位置服务
     */
    private static final String BAIDU_LOCATION_SERVICE = "http://opendata.baidu.com/api.php?query=%s&co=&resource_id=6006&oe=utf8";

    /**
     * 参与加密路径
     */
    private static final String ENCRYPTION_PATH = "/ws/location/v1/ip?ip=%s&key=%s%s";

    /**
     * 获取用户ip地址
     *
     * @param request 请求
     * @return ip地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if ("127.0.0.1".equals(ipAddress)) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) {
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }

    /**
     * 解析ip地址
     *
     * @param ipAddress ip地址
     * @return 解析后的ip地址
     */
    public static String getIpSource(String ipAddress) {
        try {
            URL url = new URL(String.format(BAIDU_LOCATION_SERVICE, ipAddress));
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), "utf-8"));
            String line = null;
            StringBuffer result = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            Map map = JSON.parseObject(result.toString(), Map.class);
            List<Map<String, String>> data = (List) map.get("data");
            return data.get(0).get("location");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取访问设备
     *
     * @param request 请求
     * @return {@link UserAgent} 访问设备
     */
    public static UserAgent getUserAgent(HttpServletRequest request) {
        return UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
    }

    /**
     * 查询IP属地信息
     *
     * @param ipAddress
     * @return IpGeoInfoVO
     */
    public static IpGeoInfoVO getIpGeoInfo(String ipAddress) {
        IpGeoInfoVO ipGeoInfoVO = null;
        try {
            String api = String.format(WHOIS_IP, ipAddress);
            String result = HttpUtil.get(api);
            if (StringUtils.isNotBlank(result)) {
                ipGeoInfoVO = JSONObject.parseObject(result, IpGeoInfoVO.class);
            }
        } catch (Exception e) {
            log.error("getIpGeoInfo error:{}", e.getMessage());
        } finally {
            return ipGeoInfoVO;
        }
    }

    /**
     * 从腾讯位置服务根据IP获取地址信息
     *
     * @param ip   IP地址
     * @param key  key
     * @param sign 签名密钥
     * @return 位置信息
     */
    public static JSONObject getIpLocationInfoByTencent(String ip, String key, String sign) {
        try {
            String url = String.format(TENCENT_LOCATION_SERVICE, ip, key, sign);
            String result = HttpUtil.get(url);
            log.info("tencent location result:{}", result);
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("tencent location error:{}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取加密内容签名
     *
     * @param ip  ip
     * @param key key
     * @param sk  密钥
     * @return 签名
     */
    public static String getSign(String ip, String key, String sk) {
        return DigestUtils.md5DigestAsHex(String.format(ENCRYPTION_PATH, ip, key, sk).getBytes(StandardCharsets.UTF_8));
    }
}

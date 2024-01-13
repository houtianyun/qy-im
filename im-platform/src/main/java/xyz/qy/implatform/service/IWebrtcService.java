package xyz.qy.implatform.service;

import org.springframework.web.bind.annotation.RequestBody;
import xyz.qy.implatform.config.ICEServer;

import java.util.List;

/**
 * @description: 通信服务
 * @author: HouTianYun
 * @create: 2024-01-13 15:33
 **/
public interface IWebrtcService {
    void call(Long uid, String offer);

    void accept( Long uid,@RequestBody String answer);

    void reject( Long uid);

    void cancel( Long uid);

    void failed( Long uid, String reason);

    void leave( Long uid) ;

    void candidate( Long uid, String candidate);

    List<ICEServer> getIceServers();
}

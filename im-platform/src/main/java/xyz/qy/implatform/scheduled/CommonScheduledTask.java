package xyz.qy.implatform.scheduled;

import xyz.qy.implatform.service.IMediaMaterialService;
import xyz.qy.implatform.service.IMusicService;
import xyz.qy.implatform.util.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.qy.implatform.contant.RedisKey;

/**
 * @description: 公共定时任务
 * @author: Polaris
 * @create: 2023-04-29 10:19
 **/
@Component
public class CommonScheduledTask {
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IMediaMaterialService mediaMaterialService;

    @Autowired
    private IMusicService musicService;

    @Scheduled(cron = "0 1 0 * * ?", zone = "Asia/Shanghai")
    public void clear() {
        // 清空redis访客记录
        redisCache.deleteObject(RedisKey.UNIQUE_VISITOR);
    }

    @Scheduled(cron = "0 0 6 1/2 * ?", zone = "Asia/Shanghai")
    public void generateRandomSort() {
        mediaMaterialService.generateRandomSort();
    }

    @Scheduled(cron = "0 0 3 * * ?", zone = "Asia/Shanghai")
    public void crawlMusic() {
        musicService.crawlMusic(null);
    }
}

package xyz.qy.imclient.task;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractPullMessageTask {
    private int threadNum = 8;

    //private ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
    ThreadPoolExecutor executorService = new ThreadPoolExecutor(threadNum, threadNum,5, TimeUnit.SECONDS,new ArrayBlockingQueue<>(10),new ThreadPoolExecutor.AbortPolicy());

    @PostConstruct
    public void init(){
        // 初始化定时器
        for(int i=0;i<threadNum;i++){
            executorService.execute(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    try{
                        pullMessage();
                    }catch (Exception e){
                        log.error("任务调度异常",e);
                        Thread.sleep(200);
                    }
                    if(!executorService.isShutdown()){
                        executorService.execute(this);
                    }
                }
            });
        }
    }

    @PreDestroy
    public void destroy(){
        log.info("{}线程任务关闭",this.getClass().getSimpleName());
        executorService.shutdown();
    }

    public abstract void pullMessage();
}

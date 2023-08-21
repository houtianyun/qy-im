package xyz.qy.imserver.task;

import xyz.qy.imserver.netty.IMServerGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public  abstract class AbstractPullMessageTask{
    private int threadNum = 1;

    private ExecutorService executorService;

    @Autowired
    private IMServerGroup serverGroup;

    public  AbstractPullMessageTask(){
        this.threadNum = 1;
    }

    public  AbstractPullMessageTask(int threadNum){
        this.threadNum = threadNum;
    }

    @PostConstruct
    public void init(){
        // 初始化定时器
        executorService = new ThreadPoolExecutor(threadNum, threadNum,0, TimeUnit.SECONDS,new LinkedBlockingQueue<>(20),new ThreadPoolExecutor.AbortPolicy());
        for(int i=0;i<threadNum;i++){
            executorService.execute(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    try{
                        if(serverGroup.isReady()){
                            pullMessage();
                        }
                        Thread.sleep(2000);
                    }catch (Exception e){
                        log.error("任务调度异常",e);
                        Thread.sleep(2000);
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

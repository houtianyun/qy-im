package xyz.qy.imserver.netty.processor;

import xyz.qy.imcommon.enums.IMCmdType;
import xyz.qy.imserver.util.SpringContextHolder;

public class ProcessorFactory {
    public static AbstractMessageProcessor createProcessor(IMCmdType cmd){
        AbstractMessageProcessor processor = null;
        switch (cmd){
            case LOGIN:
                processor = (AbstractMessageProcessor) SpringContextHolder.getApplicationContext().getBean(LoginProcessor.class);
                break;
            case HEART_BEAT:
                processor = (AbstractMessageProcessor) SpringContextHolder.getApplicationContext().getBean(HeartbeatProcessor.class);
                break;
            case PRIVATE_MESSAGE:
                processor = (AbstractMessageProcessor)SpringContextHolder.getApplicationContext().getBean(PrivateMessageProcessor.class);
                break;
            case GROUP_MESSAGE:
                processor = (AbstractMessageProcessor)SpringContextHolder.getApplicationContext().getBean(GroupMessageProcessor.class);
                break;
            default:
                break;
        }
        return processor;
    }
}

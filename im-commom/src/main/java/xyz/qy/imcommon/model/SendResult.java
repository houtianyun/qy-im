package xyz.qy.imcommon.model;

import lombok.Data;
import xyz.qy.imcommon.enums.IMSendCode;

@Data
public class SendResult<T> {
    /**
     * 接收者id
     */
    private Long recvId;

    /**
     * 发送状态
     */
    private IMSendCode code;

    /**
     * 消息体(透传)
     */
    private T messageInfo;
}

package xyz.qy.imcommon.model;

import lombok.Data;

@Data
public class SendResult<T> {
    /**
     * 接收者id
     */
    private Long recvId;

    /**
     * 接收者终端类型 IMTerminalType
     */
    private Integer recvTerminal;

    /**
     * 发送状态 IMCmdType
     */
    private Integer code;

    /**
     *  消息内容
     */
    private T data;

}

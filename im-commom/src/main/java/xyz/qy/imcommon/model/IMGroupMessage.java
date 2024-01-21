package xyz.qy.imcommon.model;

import lombok.Data;
import xyz.qy.imcommon.enums.IMTerminalType;

import java.util.Collections;
import java.util.List;

/**
 * @description:
 * @author: HouTianYun
 * @create: 2024-01-14 08:40
 **/
@Data
public class IMGroupMessage<T> {

    /**
     * 发送方
     */
    private IMUserInfo sender;

    /**
     * 接收者id列表(群成员列表,为空则不会推送)
     */
    private List<Long> recvIds  = Collections.EMPTY_LIST;


    /**
     * 接收者终端类型,默认全部
     */
    private List<Integer> recvTerminals = IMTerminalType.codes();

    /**
     * 是否发送给自己的其他终端,默认true
     */
    private Boolean sendToSelf = true;

    /**
     * 是否需要回推发送结果,默认true
     */
    private Boolean sendResult = true;

    /**
     *  消息内容
     */
    private T data;
}

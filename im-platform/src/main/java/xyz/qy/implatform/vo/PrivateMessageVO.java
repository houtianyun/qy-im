package xyz.qy.implatform.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import xyz.qy.imcommon.serializer.DateToLongSerializer;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@ApiModel("私聊消息VO")
public class PrivateMessageVO {
    /**
     * 消息id
     */
    private long id;

    /**
     * 发送者id
     */
    private Long sendId;

    /**
     * 接收者id
     */
    private Long recvId;

    /**
     * 发送内容
     */
    private String content;

    /**
     * 消息内容类型 IMCmdType
     */
    private Integer type;

    /**
     * 发送时间
     */
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;
}

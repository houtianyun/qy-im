package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @description: 说说查询 DTO
 * @author: HouTianYun
 * @create: 2023-11-20 20:09
 **/
@Data
public class TalkQueryDTO {
    @ApiModelProperty(value = "可见范围")
    private Integer scope;

    @ApiModelProperty(value = "当前用户id")
    private Long ownerId;

    @ApiModelProperty(value = "好友id")
    private List<Long> friendIds;

    @ApiModelProperty(value = "群友id")
    private List<Long> groupMemberIds;
}

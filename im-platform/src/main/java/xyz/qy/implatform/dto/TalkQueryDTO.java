package xyz.qy.implatform.dto;

import lombok.Data;

import java.util.List;

/**
 * @description: 说说查询 DTO
 * @author: HouTianYun
 * @create: 2023-11-20 20:09
 **/
@Data
public class TalkQueryDTO {

    private Integer scope;

    /**
     * 当前用户id
     */
    private Long ownerId;

    /**
     * 好友id
     */
    private List<Long> friendIds;

    /**
     * 群友id
     */
    private List<Long> groupMemberIds;
}

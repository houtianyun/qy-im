package xyz.qy.implatform.vo;

import lombok.Data;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-18 17:19
 **/
@Data
public class TemplateGroupCreateVO {
    /**
     * 模板群聊id
     */
    private Long templateGroupId;

    /**
     * 模板群聊头像
     */
    private String templateGroupAvatar;

    /**
     * 模板群聊名称
     */
    private String templateGroupName;

    /**
     * 模板人物id
     */
    private Long templateCharacterId;

    /**
     * 模板人物id
     */
    private String templateCharacterAvatar;

    /**
     * 模板人物名称
     */
    private String templateCharacterName;
}

package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 群成员
 *
 * @author Polaris
 * @since 2022-10-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_group_member")
public class GroupMember extends Model<GroupMember> {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群id
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     *  群内显示名称
     */
    @TableField("alias_name")
    private String aliasName;

    /**
     *  头像
     */
    @TableField("head_image")
    private String headImage;

    /**
     * 是否模板人物
     */
    @TableField("is_template")
    private Integer isTemplate;

    /**
     * 模板人物id
     */
    @TableField("template_character_id")
    private Long templateCharacterId;

    /**
     * 模板人物头像id
     */
    @TableField(value = "character_avatar_id", fill = FieldFill.INSERT_UPDATE)
    private Long characterAvatarId;

    /**
     * 头像别名
     */
    @TableField(value = "avatar_alias", fill = FieldFill.INSERT_UPDATE)
    private String avatarAlias;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 是否已离开群聊
     */
    @TableField("quit")
    private Boolean quit;


    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;

    @TableField("switch_time")
    private Date switchTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}

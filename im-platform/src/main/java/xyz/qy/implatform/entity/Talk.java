package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description: 说说
 * @author: HouTianYun
 * @create: 2023-11-19 21:29
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_talk")
public class Talk extends BaseEntity{
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 图片URL
     */
    @TableField("img_url")
    private String imgUrl;

    /**
     * 视频URL
     */
    @TableField("video_url")
    private String videoUrl;

    /**
     * 公布范围
     */
    @TableField("scope")
    private Integer scope;

    /**
     * 发布地址
     */
    @TableField("address")
    private String address;
}

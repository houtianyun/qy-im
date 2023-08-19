package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-12 16:25
 **/
@Data
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 是否删除：0-否；1-是 */
    @TableField("deleted")
    private String deleted;

    /** 创建者 */
    @TableField("create_by")
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private Date createTime;

    /** 更新者 */
    @TableField("update_by")
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private Date updateTime;
}

package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("用户登录VO")
public class LoginDTO {

    @NotEmpty(message="用户名不可为空")
    @ApiModelProperty(value = "用户名")
    private String userName;

    @NotEmpty(message="用户密码不可为空")
    @ApiModelProperty(value = "用户密码")
    private String password;

    @Length(max = 4,message = "验证码大于4字符")
    @NotEmpty(message="验证码不可为空")
    @ApiModelProperty(value = "验证码")
    private String code;

    @NotEmpty(message="参数异常")
    @ApiModelProperty(value = "验证码缓存记录字符串")
    private String uuid;

}

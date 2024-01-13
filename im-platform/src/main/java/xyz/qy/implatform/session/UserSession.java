package xyz.qy.implatform.session;

import lombok.Data;
import xyz.qy.imcommon.model.IMSessionInfo;

@Data
public class UserSession extends IMSessionInfo {
    private String userName;
    private String nickName;
}

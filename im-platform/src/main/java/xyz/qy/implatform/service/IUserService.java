package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.LoginDTO;
import xyz.qy.implatform.dto.RegisterDTO;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.vo.LoginVO;
import xyz.qy.implatform.vo.UserVO;

import java.util.List;


public interface IUserService extends IService<User> {

    LoginVO login(LoginDTO dto);

    LoginVO refreshToken(String refreshToken);

    void register(RegisterDTO registerDTO);

    User findUserByName(String username);

    void update(UserVO vo);

    List<UserVO> findUserByNickName(String nickname);

    List<Long> checkOnline(String userIds);

    /**
     * 生成随机用户名
     *
     * @return 用户名
     */
    String generateRandomUsername();
}

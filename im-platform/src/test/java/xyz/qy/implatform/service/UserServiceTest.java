package xyz.qy.implatform.service;

import xyz.qy.implatform.util.SysStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-06-10 09:14
 **/
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {
    @Resource
    private IUserService userService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Test
    public void generateRandomUsernameTest() {
        String username = userService.generateRandomUsername();
        log.info("username=" + username);
    }

    @Test
    public void makeRandomPassword() {
        String password = SysStringUtils.makeRandomPassword();
        String encode = passwordEncoder.encode(password);
        log.info("encode=" + encode);
    }
}

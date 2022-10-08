package com.jeequan.jeepay.service.impl;


import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.mgr.bootstrap.JeepayMgrApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = JeepayMgrApplication.class)
@Slf4j
public class SysUserAuthServiceTest {


  @Autowired
  SysUserAuthService sysUserAuthService;

  @Test
 public void resetAuthInfo() {

    sysUserAuthService.resetAuthInfo(100002l, null, null,
        "123456", CS.SYS_TYPE.MGR);
  }
}

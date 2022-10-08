package com.jeequan.jeepay.service.impl;


import com.jeequan.jeepay.mch.bootstrap.JeepayMchApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = JeepayMchApplication.class)
@Slf4j
public class SysUserAuthServiceTest {


  @Autowired
  SysUserAuthService sysUserAuthService;


  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  public void resetAuthInfo() {

    System.out.println(passwordEncoder.encode("123456"));
  }
}

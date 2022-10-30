package com.jeequan.jeepay.pay.ctrl.weixin;

import com.jeequan.jeepay.core.model.ApiRes;
import java.io.IOException;
import java.util.Base64;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts.OAuth2Scope;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/weixin")
@Slf4j
@Controller
public class WeixinController {


  @Autowired
  private WxMpService wxMpService;

  @Autowired
  private WxMpConfigStorage wxMpConfigStorage;

  @Value("${wx.mp.redirect}")
  private String wxRedirect;


  @GetMapping("/msg")
  public void msg(String timestamp, String nonce, String signature, String echostr,
      HttpServletResponse response) throws IOException {

    log.info("wxMpConfigStorage.getSecret()：",
        wxMpConfigStorage.getSecret());

    log.info("timestamp:{},nonce:{},signature：{},echostr:{}", timestamp, nonce, signature, echostr);

    Boolean success = wxMpService.checkSignature(timestamp, nonce, signature);

    log.info("签名验证结果：{}", success);

    response.getWriter().println(echostr);


  }


  @SneakyThrows
  @GetMapping("/login")
  public String login(String code, String service, HttpServletResponse response) {

    service = new String(Base64.getDecoder().decode(service));

    WxOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.getOAuth2Service().getAccessToken(code);

    String openId = wxMpOAuth2AccessToken.getOpenId();

    service += "?openId=" + openId;

    return "redirect:" + service;
  }


  @GetMapping("/authorize")
  public String authorize(String service) {

    String url =
        wxMpService
            .getOAuth2Service()
            .buildAuthorizationUrl(
                wxRedirect + "/weixin/login?service=" + service,
                OAuth2Scope.SNSAPI_BASE,
                null);

    return "redirect:" + url;
  }


  @SneakyThrows
  @GetMapping("/createJsapiSignature")
  @ResponseBody
  public ApiRes createJsapiSignature(String url) {

    return ApiRes.ok(wxMpService.createJsapiSignature(url));
  }


}

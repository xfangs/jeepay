package com.jeequan.jeepay.pay.channel.wxGlobalPay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyV3Result;
import com.jeequan.jeepay.core.constants.CS.IF_CODE;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.aliGlobalPay.AliGlobalPayNormalMchParams;
import com.jeequan.jeepay.core.model.params.wxGlobalPay.WXGlobalPayNormalMchParams;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WxGlobalPayChannelNoticeService extends AbstractChannelNoticeService {

  @Autowired
  private PayOrderService payOrderService;

  @Override
  public String getIfCode() {
    return IF_CODE.WX_GLOBAL_PAY;
  }

  @Override
  public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId,
      NoticeTypeEnum noticeTypeEnum) {
    try {

      // 获取订单信息
      PayOrder payOrder = payOrderService.getById(urlOrderId);
      if (payOrder == null) {
        throw new BizException("订单不存在");
      }

      //获取支付参数 (缓存数据) 和 商户信息
      MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(
          payOrder.getMchNo(), payOrder.getAppId());
      if (mchAppConfigContext == null) {
        throw new BizException("获取商户信息失败");
      }

      // 验签 && 获取订单回调数据
      JSONObject jsonObject = parseOrderNotifyV3Result(request,
          mchAppConfigContext);

      return MutablePair.of(jsonObject.getString("out_trade_no"), jsonObject);

    } catch (Exception e) {
      log.error("error", e);
      throw ResponseException.buildText("ERROR");
    }
  }

  @Override
  public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayOrder payOrder,
      MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {

    try {

      JSONObject jsonParams = (JSONObject) params;
      ChannelRetMsg channelResult = new ChannelRetMsg();
      channelResult.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认支付中

      String channelState = jsonParams.getString("trade_state");
      log.info("channelState:" + channelState);
      if ("SUCCESS".equals(channelState)) {
        channelResult.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
        payOrder.setState(PayOrder.STATE_ING);
      } else if ("CLOSED".equals(channelState)
          || "REVOKED".equals(channelState)
          || "PAYERROR".equals(channelState)) {  //CLOSED—已关闭， REVOKED—已撤销, PAYERROR--支付失败
        channelResult.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL); //支付失败
      }
      JSONObject resJSON = new JSONObject();
      resJSON.put("code", "SUCCESS");
      resJSON.put("message", "成功");

      ResponseEntity okResponse = jsonResp(resJSON);
      channelResult.setResponseEntity(okResponse); //响应数据

      return channelResult;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw ResponseException.buildText("ERROR");
    }


  }


  /**
   * V3校验通知签名
   *
   * @param request             请求信息
   * @param mchAppConfigContext 商户配置
   * @return true:校验通过 false:校验不通过
   */
  private JSONObject parseOrderNotifyV3Result(
      HttpServletRequest request, MchAppConfigContext mchAppConfigContext) throws Exception {

    String timestamp = request.getHeader("Wechatpay-Timestamp");
    String nonce = request.getHeader("Wechatpay-Nonce");
    String serial = request.getHeader("Wechatpay-Serial");
    String signature = request.getHeader("Wechatpay-Signature");

    // 获取加密信息
    String params = getReqParamFromBody();

    log.info("head：{timestamp:{},nonce:{},serial:{},signature:{}}", timestamp, nonce, serial,
        signature);

    log.info("body:{}", params);

    WXGlobalPayNormalMchParams normalMchParams = mchAppConfigContext.getNormalMchParamsByIfCode(
        IF_CODE.WX_GLOBAL_PAY,
        AliGlobalPayNormalMchParams.class);

    String merchantId = normalMchParams.getMerchantId(); // 商户号
    String merchantSerialNumber = normalMchParams.getMerchantSerialNumber(); // 商户证书序列号
    String apiV3Key = normalMchParams.getApiV3Key(); // apiV3密钥
    String wechatPaySerial = serial; // 平台证书序列号
    Verifier verifier; // 验签器
    CertificatesManager certificatesManager; // 平台证书管理器

    PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(normalMchParams.getMerchantPrivateKey());
    // 获取证书管理器实例
    certificatesManager = CertificatesManager.getInstance();
    // 向证书管理器增加需要自动更新平台证书的商户信息
    certificatesManager.putMerchant(merchantId, new WechatPay2Credentials(merchantId,
            new PrivateKeySigner(merchantSerialNumber, merchantPrivateKey)),
        apiV3Key.getBytes(StandardCharsets.UTF_8));
    // 从证书管理器中获取verifier
    verifier = certificatesManager.getVerifier(merchantId);

    // 构建request，传入必要参数
    NotificationRequest _request = new NotificationRequest.Builder().withSerialNumber(
            wechatPaySerial)
        .withNonce(nonce)
        .withTimestamp(timestamp)
        .withSignature(signature)
        .withBody(params)
        .build();
    NotificationHandler handler = new NotificationHandler(verifier,
        apiV3Key.getBytes(StandardCharsets.UTF_8));
    // 验签和解析请求体
    Notification notification = handler.parse(_request);

    return JSON.parseObject(notification.getDecryptData());


  }

  /**
   * V3接口验证微信支付通知参数
   *
   * @return
   */
  public void verifyWxPayParams(WxPayOrderNotifyV3Result.DecryptNotifyResult result,
      PayOrder payOrder) {

    try {
      // 核对金额
      Integer total_fee = result.getAmount().getTotal();        // 总金额
      long wxPayAmt = new BigDecimal(total_fee).longValue();
      long dbPayAmt = payOrder.getAmount().longValue();
      if (dbPayAmt != wxPayAmt) {
        throw ResponseException.buildText("AMOUNT ERROR");
      }
    } catch (Exception e) {
      throw ResponseException.buildText("ERROR");
    }
  }
}

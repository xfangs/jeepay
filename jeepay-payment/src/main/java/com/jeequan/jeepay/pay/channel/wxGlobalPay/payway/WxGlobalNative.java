package com.jeequan.jeepay.pay.channel.wxGlobalPay.payway;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jeequan.jeepay.core.constants.CS.IF_CODE;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.params.wxGlobalPay.WXGlobalPayNormalMchParams;
import com.jeequan.jeepay.pay.channel.wxGlobalPay.WxGlobalPayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg.ChannelState;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.WxH5OrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WxGlobalNative extends WxGlobalPayPaymentService {


  @Override
  public String getIfCode() {
    return IF_CODE.WX_GLOBAL_PAY;
  }

  @Override
  public boolean isSupport(String wayCode) {
    return super.isSupport(wayCode);
  }

  @Override
  public String preCheck(UnifiedOrderRQ bizRQ, PayOrder payOrder) {
    return super.preCheck(bizRQ, payOrder);
  }

  @Override
  public AbstractRS pay(UnifiedOrderRQ bizRQ, PayOrder payOrder,
      MchAppConfigContext mchAppConfigContext) throws Exception {

    WXGlobalPayNormalMchParams normalMchParams = mchAppConfigContext.getNormalMchParamsByIfCode(
        IF_CODE.WX_GLOBAL_PAY,
        WXGlobalPayNormalMchParams.class);

    PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(normalMchParams.getMerchantPrivateKey());
    CertificatesManager certificatesManager = CertificatesManager.getInstance();

    String apiV3Key = normalMchParams.getApiV3Key();

    String merchantSerialNumber = normalMchParams.getMerchantSerialNumber();

    certificatesManager.putMerchant(normalMchParams.getMerchantId(),
        new WechatPay2Credentials(normalMchParams.getMerchantId(),
            new PrivateKeySigner(merchantSerialNumber, merchantPrivateKey)),
        apiV3Key.getBytes(StandardCharsets.UTF_8));
    Verifier verifier = certificatesManager.getVerifier(normalMchParams.getMerchantId());
    WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
        .withMerchant(normalMchParams.getMerchantId(), merchantSerialNumber, merchantPrivateKey)
        .withValidator(new WechatPay2Validator(verifier));
    CloseableHttpClient httpClient = builder.build();

    JSONObject req = new JSONObject();
    req.set("sp_appid", normalMchParams.getAppId());
    req.set("sp_mchid", normalMchParams.getMerchantId());
    req.set("sub_mchid", normalMchParams.getSubMchid());
    req.set("out_trade_no", payOrder.getPayOrderId());
    req.set("merchant_category_code", "4111");
    req.set("notify_url", getNotifyUrl() + "/" + payOrder.getPayOrderId());
    req.set("trade_type", "NATIVE");

    JSONObject amount = new JSONObject();
    amount.set("total", bizRQ.getAmount());
    amount.set("currency", bizRQ.getCurrency());

    req.set("amount", amount);
    req.set("description", payOrder.getSubject());

    HttpPost httpPost = new HttpPost("https://apihk.mch.weixin.qq.com/v3/global/transactions/mweb");
    httpPost.addHeader("Accept", "application/json");
    httpPost.addHeader("Content-type", "application/json; charset=utf-8");
    httpPost.setEntity(new StringEntity(req.toString(), Charset.forName("UTF-8")));
    CloseableHttpResponse response = httpClient.execute(httpPost);

    int status = response.getStatusLine().getStatusCode();

    String result = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
        .lines().parallel().collect(Collectors.joining(System.lineSeparator()));

    WxH5OrderRS res = ApiResBuilder.buildSuccess(WxH5OrderRS.class);

    ChannelRetMsg channelRetMsg = new ChannelRetMsg();
    res.setChannelRetMsg(channelRetMsg);
    channelRetMsg.setChannelState(ChannelState.WAITING);

    if (status != 200) {
      log.error(StrUtil.format("微信二维码支付下单失败：{code:{},message：{}}", status, result));
      channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
      channelRetMsg.setChannelErrMsg(StrUtil.format("微信下单失败,{}", result));
      channelRetMsg.setChannelErrCode(status + "");

      return res;
    }

    JSONObject resObj = JSONUtil.parseObj(result);

    String codeUrl = resObj.getStr("code_url");

    res.setCodeImgUrl(sysConfigService.getDBApplicationConfig().genScanImgUrl(codeUrl));

    return res;


  }
}

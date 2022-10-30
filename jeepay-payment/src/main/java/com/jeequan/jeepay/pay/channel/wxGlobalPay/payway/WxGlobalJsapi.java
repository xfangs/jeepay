package com.jeequan.jeepay.pay.channel.wxGlobalPay.payway;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
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
import com.jeequan.jeepay.pay.rqrs.payorder.payway.WxJsapiOrderRS;
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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WxGlobalJsapi extends WxGlobalPayPaymentService {


  @Override
  public String getIfCode() {
    return IF_CODE.WX_GLOBAL_PAY;
  }

  @Override
  public boolean isSupport(String wayCode) {
    return super.isSupport(wayCode);
  }

  @Override
  public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {

    return null;
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
    req.set("out_trade_no",
        payOrder.getPayOrderId());
    req.set("merchant_category_code", "4111");
    req.set("notify_url", getNotifyUrl() + "/" + payOrder.getPayOrderId());
    req.set("trade_type", "JSAPI");

    JSONObject amount = new JSONObject();
    amount.set("total", bizRQ.getAmount());
    amount.set("currency", bizRQ.getCurrency());
    req.set("amount", amount);

    JSONObject payer = new JSONObject();

    JSONObject channelExtra = JSONUtil.parseObj(bizRQ.getChannelExtra());

    payer.set("sp_openid", channelExtra.getStr("openId"));

    req.set("description", payOrder.getSubject());
    req.set("payer", payer);

    HttpPost httpPost = new HttpPost(
        "https://apihk.mch.weixin.qq.com/v3/global/transactions/jsapi");
    httpPost.addHeader("Accept", "application/json");
    httpPost.addHeader("Content-type", "application/json; charset=utf-8");
    httpPost.setEntity(new StringEntity(req.toString(), Charset.forName("UTF-8")));
    CloseableHttpResponse response = httpClient.execute(httpPost);

    int status = response.getStatusLine().getStatusCode();

    String result = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
        .lines().parallel().collect(Collectors.joining(System.lineSeparator()));

    WxJsapiOrderRS res = ApiResBuilder.buildSuccess(WxJsapiOrderRS.class);

    ChannelRetMsg channelRetMsg = new ChannelRetMsg();
    res.setChannelRetMsg(channelRetMsg);
    channelRetMsg.setChannelState(ChannelState.WAITING);

    if (status != 200) {
      log.error(StrUtil.format("微信公众号支付下单失败：{code:{},message：{}}", status, result));
      channelRetMsg.setChannelState(ChannelState.CONFIRM_FAIL);
      channelRetMsg.setChannelErrMsg(StrUtil.format("微信下单失败,{}", result));
      channelRetMsg.setChannelErrCode(status + "");

      return res;
    }

    JSONObject resObj = JSONUtil.parseObj(result);

    String prepayId = resObj.getStr("prepay_id");

    JSONObject payInfo = new JSONObject();
    payInfo.set("appId", normalMchParams.getAppId());
    payInfo.set("timeStamp", System.currentTimeMillis() / 1000 + "");
    payInfo.set("nonceStr", RandomUtil.randomString(20));
    payInfo.set("package", "prepay_id=" + prepayId);
    payInfo.set("signType", "RSA");

    List<String> strings = new LinkedList<>();

    strings.add(payInfo.getStr("appId") + "\n");

    strings.add(payInfo.getStr("timeStamp") + "\n");

    strings.add(payInfo.getStr("nonceStr") + "\n");

    strings.add(payInfo.getStr("package") + "\n");

    StringBuilder sb = new StringBuilder();

    for (String string : strings) {

      sb.append(string);

    }

    Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA);

    sign.setPrivateKey(merchantPrivateKey);

    byte[] signData = sign.sign(sb.toString().getBytes(StandardCharsets.UTF_8));

    String paySign = Base64.encode(signData);

    payInfo.set("paySign", paySign);

    res.setPayInfo(payInfo.toString());

    return res;


  }
}

package com.jeequan.jeepay.pay.channel.aliGlobalPay.payway;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alipay.global.api.AlipayClient;
import com.alipay.global.api.DefaultAlipayClient;
import com.alipay.global.api.exception.AlipayApiException;
import com.alipay.global.api.model.ResultStatusType;
import com.alipay.global.api.model.ams.Amount;
import com.alipay.global.api.model.ams.Env;
import com.alipay.global.api.model.ams.Merchant;
import com.alipay.global.api.model.ams.Order;
import com.alipay.global.api.model.ams.OsType;
import com.alipay.global.api.model.ams.PaymentMethod;
import com.alipay.global.api.model.ams.ProductCodeType;
import com.alipay.global.api.model.ams.SettlementStrategy;
import com.alipay.global.api.model.ams.TerminalType;
import com.alipay.global.api.model.ams.WalletPaymentMethodType;
import com.alipay.global.api.request.ams.pay.AlipayPayRequest;
import com.alipay.global.api.response.ams.pay.AlipayPayResponse;
import com.jeequan.jeepay.core.constants.CS.IF_CODE;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.params.aliGlobalPay.AliGlobalPayNormalMchParams;
import com.jeequan.jeepay.pay.channel.alipay.AlipayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg.ChannelState;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliWapOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AliGlobalApp extends AlipayPaymentService {

  @Override
  public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
    return null;
  }

  @Override
  public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder,
      MchAppConfigContext mchAppConfigContext) throws AlipayApiException {

    AliGlobalPayNormalMchParams normalMchParams = mchAppConfigContext.getNormalMchParamsByIfCode(
        IF_CODE.ALI_GLOBAL_PAY,
        AliGlobalPayNormalMchParams.class);

    JSONObject channelExtra = JSONUtil.parseObj(rq.getChannelExtra());

    AlipayClient defaultAlipayClient = new DefaultAlipayClient(normalMchParams.getGatewayUrl(),
        normalMchParams.getPrivateKey(), normalMchParams.getAlipayPublicKey());

    AlipayPayRequest alipayPayRequest = new AlipayPayRequest();
    alipayPayRequest.setClientId(normalMchParams.getAppId());
    alipayPayRequest.setPath(
        StrUtil.equals(normalMchParams.getSandbox() + "", "1") ? "/ams/sandbox/api/v1/payments/pay"
            : "/ams/api/v1/payments/pay");
    alipayPayRequest.setProductCode(ProductCodeType.CASHIER_PAYMENT);
    alipayPayRequest.setPaymentRequestId(payOrder.getPayOrderId());

    Amount paymentAmount = new Amount();
    paymentAmount.setCurrency(rq.getCurrency());
    paymentAmount.setValue(rq.getAmount() + "");
    alipayPayRequest.setPaymentAmount(paymentAmount);

    Order order = new Order();
    order.setReferenceOrderId(payOrder.getPayOrderId());
    order.setOrderDescription(
        rq.getSubject());

    Amount orderAmount = new Amount();
    orderAmount.setCurrency(rq.getCurrency());
    orderAmount.setValue(paymentAmount.getValue());
    order.setOrderAmount(orderAmount);

    Env env = new Env();
    env.setTerminalType(TerminalType.APP);
    order.setEnv(env);

    String osType = channelExtra.getStr("osType");

    env.setOsType(OsType.valueOf(osType));

    alipayPayRequest.setOrder(order);

    PaymentMethod paymentMethod = new PaymentMethod();
    paymentMethod.setPaymentMethodType(WalletPaymentMethodType.ALIPAY_CN.name());
    alipayPayRequest.setPaymentMethod(paymentMethod);

    SettlementStrategy settlementStrategy = new SettlementStrategy();
    settlementStrategy.setSettlementCurrency(payOrder.getCurrency());
    alipayPayRequest.setSettlementStrategy(settlementStrategy);
    alipayPayRequest.setPaymentNotifyUrl(getNotifyUrl());
    alipayPayRequest.setPaymentRedirectUrl(getReturnUrl());

    Merchant merchant = new Merchant();

    merchant.setReferenceMerchantId("M210930189");

    alipayPayRequest.setMerchant(merchant);

    AlipayPayResponse alipayPayResponse = defaultAlipayClient.execute(alipayPayRequest);

    ResultStatusType resultStatusType = alipayPayResponse.getResult().getResultStatus();

    AliWapOrderRS res = ApiResBuilder.buildSuccess(AliWapOrderRS.class);

    ChannelRetMsg channelRetMsg = new ChannelRetMsg();
    res.setChannelRetMsg(channelRetMsg);
    channelRetMsg.setChannelState(ChannelState.WAITING);

    if (resultStatusType.name().equals("F")) {
      log.error(StrUtil.format("支付宝app下单失败：{code:{},message：{}}", resultStatusType.name(),
          alipayPayResponse.getResult().getResultMessage()));
      channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
      channelRetMsg.setChannelErrMsg(
          StrUtil.format("支付宝app下单失败,{}", alipayPayResponse.getResult().getResultMessage()));
      channelRetMsg.setChannelErrCode(alipayPayResponse.getResult().getResultCode());

      return res;
    }

    String redirectUrl = alipayPayResponse.getNormalUrl();

    res.setPayUrl(redirectUrl);

    return res;
  }


  protected String getNotifyUrl() {

    String url = sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/notify/"
        + getIfCode();

    log.info("回调地址：" + url);

    return url;
  }

  protected String getReturnUrl() {
    return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/return/"
        + getIfCode();
  }

  @Override
  public String getIfCode() {
    return IF_CODE.ALI_GLOBAL_PAY;
  }
}

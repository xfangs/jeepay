package com.jeequan.jeepay.pay.channel.aliGlobalPay.payway;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alipay.global.api.AlipayClient;
import com.alipay.global.api.DefaultAlipayClient;
import com.alipay.global.api.exception.AlipayApiException;
import com.alipay.global.api.model.ams.Amount;
import com.alipay.global.api.model.ams.InStorePaymentScenario;
import com.alipay.global.api.model.ams.Merchant;
import com.alipay.global.api.model.ams.Order;
import com.alipay.global.api.model.ams.PaymentFactor;
import com.alipay.global.api.model.ams.PaymentMethod;
import com.alipay.global.api.model.ams.ProductCodeType;
import com.alipay.global.api.model.ams.Store;
import com.alipay.global.api.model.ams.WalletPaymentMethodType;
import com.alipay.global.api.request.ams.pay.AlipayPayRequest;
import com.alipay.global.api.response.ams.pay.AlipayPayResponse;
import com.jeequan.jeepay.core.constants.CS.IF_CODE;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.params.aliGlobalPay.AliGlobalPayNormalMchParams;
import com.jeequan.jeepay.pay.channel.alipay.AlipayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliWapOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AliGlobalPayCode extends AlipayPaymentService {

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

    AlipayClient defaultAlipayClient = new DefaultAlipayClient(normalMchParams.getGatewayUrl(),
        normalMchParams.getPrivateKey(), normalMchParams.getAlipayPublicKey());

    AlipayPayRequest alipayPayRequest = new AlipayPayRequest();
    alipayPayRequest.setProductCode(ProductCodeType.IN_STORE_PAYMENT);
    alipayPayRequest.setPaymentNotifyUrl(getNotifyUrl());
    alipayPayRequest.setPaymentRequestId(payOrder.getPayOrderId());
    alipayPayRequest.setClientId(normalMchParams.getAppId());
    alipayPayRequest.setPath(
        StrUtil.equals(normalMchParams.getSandbox() + "", "1") ? "/ams/sandbox/api/v1/payments/pay"
            : "");

    PaymentFactor paymentFactor = new PaymentFactor();
    paymentFactor.setInStorePaymentScenario(InStorePaymentScenario.PaymentCode);
    alipayPayRequest.setPaymentFactor(paymentFactor);

    Order order = new Order();
    alipayPayRequest.setOrder(order);
    order.setReferenceOrderId(payOrder.getPayOrderId());
    order.setOrderDescription(
        rq.getSubject());

    Amount orderAmount = new Amount();
    orderAmount.setCurrency(rq.getCurrency());
    orderAmount.setValue(rq.getAmount() + "");
    order.setOrderAmount(orderAmount);

    Merchant merchant = new Merchant();
    merchant.setReferenceMerchantId(mchAppConfigContext.getMchNo());
    merchant.setMerchantName(mchAppConfigContext.getMchInfo().getMchName());
    merchant.setMerchantMCC("4816");
    order.setMerchant(merchant);
    Store store = new Store();
    store.setReferenceStoreId(mchAppConfigContext.getMchNo());
    store.setStoreName(mchAppConfigContext.getMchInfo().getMchName());
    store.setStoreMCC("4816");
    merchant.setStore(store);

    Amount paymentAmount = new Amount();
    paymentAmount.setCurrency(rq.getCurrency());
    paymentAmount.setValue(rq.getAmount() + "");
    alipayPayRequest.setPaymentAmount(paymentAmount);

    alipayPayRequest.setClientId(normalMchParams.getAppId());
    alipayPayRequest.setPath(
        StrUtil.equals(normalMchParams.getSandbox() + "", "1") ? "/ams/sandbox/api/v1/payments/pay"
            : "");
    alipayPayRequest.setProductCode(ProductCodeType.IN_STORE_PAYMENT);
    alipayPayRequest.setPaymentRequestId(payOrder.getPayOrderId());

    PaymentMethod paymentMethod = new PaymentMethod();
    paymentMethod.setPaymentMethodType(WalletPaymentMethodType.ALIPAY_CN.name());
    alipayPayRequest.setPaymentMethod(paymentMethod);

    AlipayPayResponse alipayPayResponse = defaultAlipayClient.execute(alipayPayRequest);

    JSONObject paymentActionForm = JSONUtil.parseObj(alipayPayResponse.getPaymentActionForm());

    String redirectUrl = paymentActionForm.getStr("redirectUrl");

    AliWapOrderRS res = ApiResBuilder.buildSuccess(AliWapOrderRS.class);

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

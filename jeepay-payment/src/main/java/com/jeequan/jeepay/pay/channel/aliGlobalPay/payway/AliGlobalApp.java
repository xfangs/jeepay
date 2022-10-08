package com.jeequan.jeepay.pay.channel.aliGlobalPay.payway;

import com.alibaba.fastjson.JSONObject;
import com.alipay.global.api.AlipayClient;
import com.alipay.global.api.DefaultAlipayClient;
import com.alipay.global.api.exception.AlipayApiException;
import com.alipay.global.api.model.ams.Amount;
import com.alipay.global.api.model.ams.BusinessType;
import com.alipay.global.api.model.ams.ChinaExtraTransInfo;
import com.alipay.global.api.model.ams.Env;
import com.alipay.global.api.model.ams.Merchant;
import com.alipay.global.api.model.ams.Order;
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
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import org.springframework.stereotype.Service;

@Service
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

    AlipayClient defaultAlipayClient = new DefaultAlipayClient(normalMchParams.getGatewayUrl(),
        normalMchParams.getPrivateKey(), normalMchParams.getAlipayPublicKey());

    AlipayPayRequest alipayPayRequest = new AlipayPayRequest();
    alipayPayRequest.setClientId(normalMchParams.getAppId());
    alipayPayRequest.setPath("/ams/api/v1/payments/pay");
    alipayPayRequest.setProductCode(ProductCodeType.CASHIER_PAYMENT);
    alipayPayRequest.setPaymentRequestId("pay_test_99");

    Amount paymentAmount = new Amount();
    paymentAmount.setCurrency("USD");
    paymentAmount.setValue("30000");
    alipayPayRequest.setPaymentAmount(paymentAmount);

    Order order = new Order();
    order.setReferenceOrderId("102775765075669");
    order.setOrderDescription(
        "Mi Band 3 Wrist Strap Metal Screwless Stainless Steel For Xiaomi Mi Band 3");

    ChinaExtraTransInfo chinaExtraTransInfo = new ChinaExtraTransInfo();
    chinaExtraTransInfo.setBusinessType(BusinessType.HOTEL);
    chinaExtraTransInfo.setHotelName("hotelName");
    chinaExtraTransInfo.setCheckinTime("2020-06-26T10:00:00+08:00");
    chinaExtraTransInfo.setCheckoutTime("2020-06-26T10:00:00+08:00");
    JSONObject extendInfo = new JSONObject();
    extendInfo.put("chinaExtraTransInfo", chinaExtraTransInfo);
    order.setExtendInfo(extendInfo.toJSONString());

    Merchant merchant = new Merchant();
    merchant.setMerchantMCC("testMcc");
    merchant.setReferenceMerchantId("referenceMerchantId");
    order.setMerchant(merchant);

    Amount orderAmount = new Amount();
    orderAmount.setCurrency("USD");
    orderAmount.setValue("30000");
    order.setOrderAmount(orderAmount);

    Env env = new Env();
    env.setTerminalType(TerminalType.WEB);
    order.setEnv(env);

    alipayPayRequest.setOrder(order);

    PaymentMethod paymentMethod = new PaymentMethod();
    paymentMethod.setPaymentMethodType(WalletPaymentMethodType.ALIPAY_CN.name());
    alipayPayRequest.setPaymentMethod(paymentMethod);

    alipayPayRequest.setPaymentNotifyUrl("https://global.alipay.com/notify");
    alipayPayRequest.setPaymentRedirectUrl("https://global.alipay.com?param1=v1");

    SettlementStrategy settlementStrategy = new SettlementStrategy();
    settlementStrategy.setSettlementCurrency("USD");
    alipayPayRequest.setSettlementStrategy(settlementStrategy);

    AlipayPayResponse alipayPayResponse = defaultAlipayClient.execute(alipayPayRequest);

    return null;
  }
}

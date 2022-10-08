package com;

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
import com.alipay.global.api.model.ams.OsType;
import com.alipay.global.api.model.ams.PaymentMethod;
import com.alipay.global.api.model.ams.ProductCodeType;
import com.alipay.global.api.model.ams.SettlementStrategy;
import com.alipay.global.api.model.ams.TerminalType;
import com.alipay.global.api.model.ams.WalletPaymentMethodType;
import com.alipay.global.api.request.ams.pay.AlipayPayRequest;
import com.alipay.global.api.response.ams.pay.AlipayPayResponse;

public class Main {

  public static void main(String[] args) throws AlipayApiException {

    String merchantPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQChBkluTdcP32Lu11s0fFBIg/1eCiSM4Ym9Je5tra8gpVDPMPrG39/Fam2dBsGCKN2am+IZqIWMxwz3Q6TGiwM7RmPj6YaOfPHDaRyu/hlnAyxMzsdSzyzHBLIeY6hpsdeKtcxbKlsGzsF41xgCNgCXq9/QjROcDy4OVmFIZ9CSQsDVE0w3YjIL5ve4oD8cSvI6xOR8nBR8suNm7mK8BiBr6HGzSJ0Jon8toJ+idF+YS+m4GCYOoQQctS8a/gXz3IDRdyTRxFRYkI1zrJbbp1aOA9T5eWMN162JEI6RKlytX8yXx3XB3f9VwykmX3keLSEj2efPlgfOPrXV4IAqOiHfAgMBAAECggEAc8gEdraro7MY/OmGn/ee9nVJchvS6iWll4a1qNFQ8iVMNJ5gQy1oRhfflx/rdf6SUQAzFAXzeSUK8qQFz+jWuwFDA/a/FKdMYxiqUj1M4KAMc3HfKnDjHnsG5Aj+aHlCpW9Q8GBFMWDrBkuK7NQNmwEvnlJCPl0/3XlI2/oho3gG6XVGWPuEGkot/i6QISU0Y1IBzikHPK/RJ4CepTWVav46LX0uZnhJpseOs/tkSdYV9sDkhPb00pEgZluAEFmzfi5UvyPlnBo1rXch0afliAKRvzLqQwZLjMrpVl0cGwuESlR6HP/4xpkkuk4j5qpHuVQ2gUQwzbL2LluepOp00QKBgQD9YmkUZUwYojRKhQuXhGGwKmgyCdGg+1HZSDJrPi7qxu2kQP6/Z0F6MM059p9citdV+fXLzmkFiPwgdzmP0jhYpAmWvmcNQKl3XN8m5IZ1mSeplh71njBGTjcBXOhbazzORtfR0GSxDJIsyOVIYvBMqjfZEZ+omY4utCNoNxWFRwKBgQCir87GZy3Zn9v+zW8f57g0TOEHZDi1evZ/FP6tnVv77k+MzjpOswPo3j6cthf+itI908BR6Dw+by788XFy/JvJQoYObc16dsXjj/NHfP6U7u1WealKN4xr/8zri4ZI2+tKjtd8FO0tw2im2/F9K0R84dSacwsNqyz3qosR+1yqqQKBgD+niV5mVEeb+CcAZXka+K+Y97QaY19dw6IiUQhABulUMD8jVNwgxII94FC/dCl7d71Rnj4lDJ0nXK+LRBqtZRpfm0kTbDAYHnquCiFrJ5xDbYNdA0oRA2+mFotxG65bslrf0TgUcjdIQTCfB3q34EZiPMV7d/CTIvT4rCxyKiXhAoGBAJ2maGXzDodZVkKwqQLt9Z8Y8OfMwvd6VOwJWFK9rqmP4h68qdwhtaQv2dTa0J2lwN6RGElHFzoZXBtZjWq0R/LcODQ7S2dlOZavpDyeb8W7Utr9woNdGQJ/PAD1kAeCtZvmmAJx9PTn673mXTnCd/fcj72rxgZU3pqR9XpTbxUhAoGAOaRZFs5UZkYJlLjqAfQ+zoXsu2Jx8bDMBRz2BI2d3fw0hNKb4DT6b+Va+jpq/i2Ob+j9/oKuZ7Af7NawHj8P/Lc1C3FIFg0js4FB16gh+KM00LnsDpZl5as4ZGoKHdkoVNih713vY3FDTATFXij146SJlFucpggIvqLsDsZo0vg=";

    String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjRYT2nPTaoWqz3BXCNfYG3hzd+nlxrEPlx8xVe986v3P0W5MBwrW4rKHUVbAlYqKMd0uqURdY5i+rtKLNaETGUwPS8yyK7E31t7k3gX+1CbCTM2ksR6Qdz1NGfdlT9Ixv8NYgQEB+5F8YBahnWDQL2EB2tN1nRLghQXnSyTQtg6jOFkO/YDR9v93r5UQ+zcnw3MtoLtJsycrRd2hhma3gIudj/SrohroWi7Y0tSXsWMGhinx78gHiaFAwApzoZtBdhwPZnLb5zL8P9ETSkX5KQJNxDXEtIgOTKGuhhAL0qI6iGQlr9EXGMW0gXo1ozpMetMTN0JOqm13o3YgVMyvDwIDAQAB";

    AlipayClient defaultAlipayClient = new DefaultAlipayClient("https://open-sea.alipay.com",
        merchantPrivateKey, alipayPublicKey);

    AlipayPayRequest alipayPayRequest = new AlipayPayRequest();
    alipayPayRequest.setClientId("SANDBOX_5Y378J2YC65Y03023");
    alipayPayRequest.setPath("/ams/sandbox/api/v1/payments/pay");
    alipayPayRequest.setProductCode(ProductCodeType.CASHIER_PAYMENT);
    alipayPayRequest.setPaymentRequestId("pay_test_99");

    Amount paymentAmount = new Amount();
    paymentAmount.setCurrency("PHP");
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
    env.setTerminalType(TerminalType.APP);
    env.setOsType(OsType.IOS);
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

    System.out.println("11");


  }

}

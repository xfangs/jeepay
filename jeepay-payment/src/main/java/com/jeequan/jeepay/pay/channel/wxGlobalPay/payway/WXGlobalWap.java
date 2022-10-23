package com.jeequan.jeepay.pay.channel.wxGlobalPay.payway;

import com.jeequan.jeepay.core.constants.CS.IF_CODE;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.channel.wxGlobalPay.WxGlobalPayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WXGlobalWap extends WxGlobalPayPaymentService {


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

    return null;


  }
}

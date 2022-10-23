package com.jeequan.jeepay.pay.channel.wxGlobalPay;

import com.jeequan.jeepay.core.constants.CS.IF_CODE;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.util.PaywayUtil;
import org.springframework.stereotype.Service;

@Service
public class WxGlobalPayPaymentService extends AbstractPaymentService {

  @Override
  public String getIfCode() {
    return IF_CODE.WX_GLOBAL_PAY;
  }

  @Override
  public boolean isSupport(String wayCode) {
    return true;
  }

  @Override
  public String preCheck(UnifiedOrderRQ bizRQ, PayOrder payOrder) {
    return null;
  }

  @Override
  public AbstractRS pay(UnifiedOrderRQ bizRQ, PayOrder payOrder,
      MchAppConfigContext mchAppConfigContext) throws Exception {
    return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode())
        .pay(bizRQ, payOrder, mchAppConfigContext);
  }
}

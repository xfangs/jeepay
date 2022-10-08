package com.jeequan.jeepay.pay.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS.PAY_DATA_TYPE;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import lombok.Data;


@Data
public class AliGlobalAppOrderRS extends UnifiedOrderRS {


  private String payData;

  @Override
  public String buildPayDataType() {
    return PAY_DATA_TYPE.ALI_GLOBAL_APP;
  }

  @Override
  public String buildPayData() {
    return payData;
  }

}

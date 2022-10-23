package com.jeequan.jeepay.core.model.params.wxGlobalPay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.StringKit;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class WXGlobalPayNormalMchParams extends NormalMchParams {


  private String apiV3Key;

  private String merchantId;

  private String appId;

  private String subMchid;

  private String merchantPrivateKey;

  private String merchantSerialNumber;

  private String wechatPaySerial;


  @Override
  public String deSenData() {

    WXGlobalPayNormalMchParams mchParams = this;
    if (StringUtils.isNotBlank(this.merchantPrivateKey)) {
      mchParams.setMerchantPrivateKey(StringKit.str2Star(this.merchantPrivateKey, 4, 4, 6));
    }
    return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
  }

}

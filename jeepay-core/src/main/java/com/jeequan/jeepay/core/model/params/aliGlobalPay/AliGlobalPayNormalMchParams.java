package com.jeequan.jeepay.core.model.params.aliGlobalPay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.StringKit;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class AliGlobalPayNormalMchParams extends NormalMchParams {


  /**
   * 是否沙箱环境
   */
  private Integer sandbox;


  private String gatewayUrl;

  private String appId;

  private String privateKey;

  private String alipayPublicKey;


  @Override
  public String deSenData() {

    AliGlobalPayNormalMchParams mchParams = this;
    if (StringUtils.isNotBlank(this.privateKey)) {
      mchParams.setPrivateKey(StringKit.str2Star(this.privateKey, 4, 4, 6));
    }
    if (StringUtils.isNotBlank(this.alipayPublicKey)) {
      mchParams.setAlipayPublicKey(StringKit.str2Star(this.alipayPublicKey, 6, 6, 6));
    }
    return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
  }

}

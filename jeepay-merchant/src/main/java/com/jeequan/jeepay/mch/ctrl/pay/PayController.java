package com.jeequan.jeepay.mch.ctrl.pay;


import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.DBApplicationConfig;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.model.PayOrderCreateReqModel;
import com.jeequan.jeepay.request.PayOrderCreateRequest;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchPayPassageService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pay")
@Slf4j
public class PayController extends CommonCtrl {

  @Autowired
  private MchAppService mchAppService;
  @Autowired
  private MchPayPassageService mchPayPassageService;
  @Autowired
  private SysConfigService sysConfigService;

  /**
   * 调起下单接口
   **/
  @PostMapping("/payOrders")
  public ApiRes doPay() {

    log.info("正在进行下单...");

    //获取请求参数
    String appId = getValStringRequired("appId");
    Long amount = getRequiredAmountL("amount");
    String mchOrderNo = getValStringRequired("mchOrderNo");
    String wayCode = getValStringRequired("wayCode");

    Byte divisionMode = getValByteRequired("divisionMode");
    String orderTitle = getValStringRequired("orderTitle");
    String currency = getValStringRequired("currency");
    String openId = getValStringRequired("openId");

    if (StringUtils.isEmpty(orderTitle)) {
      throw new BizException("订单标题不能为空");
    }

    // 前端明确了支付参数的类型 payDataType
    String payDataType = getValString("payDataType");
    String authCode = getValString("authCode");

    MchApp mchApp = mchAppService.getById(appId);
    if (mchApp == null || mchApp.getState() != CS.PUB_USABLE || !mchApp.getAppId().equals(appId)) {
      throw new BizException("商户应用不存在或不可用");
    }

    PayOrderCreateRequest request = new PayOrderCreateRequest();
    PayOrderCreateReqModel model = new PayOrderCreateReqModel();
    request.setBizModel(model);

    model.setMchNo(getCurrentMchNo()); // 商户号
    model.setAppId(appId);
    model.setMchOrderNo(mchOrderNo);
    model.setCurrency(currency);
    model.setWayCode(wayCode);
    model.setAmount(amount);

    if (StringUtils.isBlank(currency)) {
      switch (wayCode) {
        case "pp_pc":
          model.setCurrency("USD");
          break;
        default:
          model.setCurrency("CNY");
          break;
      }
    } else {
      model.setCurrency(currency);
    }

    model.setClientIp(getClientIp());
    model.setSubject(orderTitle);
    model.setBody(orderTitle);

    DBApplicationConfig dbApplicationConfig = sysConfigService.getDBApplicationConfig();

    model.setNotifyUrl(
        dbApplicationConfig.getMchSiteUrl() + "/api/anon/payNotify/payOrder"); //回调地址
    model.setReturnUrl(dbApplicationConfig.getPaySiteUrl());
    model.setDivisionMode(divisionMode); //分账模式

    //设置扩展参数
    JSONObject extParams = new JSONObject();
    if (StringUtils.isNotEmpty(payDataType)) {
      extParams.put("payDataType", payDataType.trim());
    }
    if (StringUtils.isNotEmpty(authCode)) {
      extParams.put("authCode", authCode.trim());
    }

    if (StringUtils.isNotBlank(openId)) {
      extParams.put("openId", openId);
    }

    model.setChannelExtra(extParams.toString());

    JeepayClient jeepayClient = new JeepayClient(dbApplicationConfig.getPaySiteUrl(),
        mchApp.getAppSecret());

    try {
      PayOrderCreateResponse response = jeepayClient.execute(request);
      if (response.getCode() != 0) {
        throw new BizException(response.getMsg());
      }

      return ApiRes.ok(response.get());
    } catch (JeepayException e) {
      logger.error(e.getMessage(), e);
      throw new BizException(e.getMessage());
    }
  }

}

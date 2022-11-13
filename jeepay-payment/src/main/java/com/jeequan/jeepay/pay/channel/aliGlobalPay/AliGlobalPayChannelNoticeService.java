/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.pay.channel.aliGlobalPay;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.global.api.AlipayClient;
import com.alipay.global.api.DefaultAlipayClient;
import com.alipay.global.api.request.ams.pay.AlipayPayCancelRequest;
import com.alipay.global.api.response.ams.pay.AlipayPayCancelResponse;
import com.alipay.global.api.tools.SignatureTool;
import com.jeequan.jeepay.core.constants.CS.IF_CODE;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.aliGlobalPay.AliGlobalPayNormalMchParams;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.exception.ChannelException;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/*
 * 支付宝 回调接口实现类
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:20
 */
@Service
@Slf4j
public class AliGlobalPayChannelNoticeService extends AbstractChannelNoticeService {


  public static void main(String[] args) throws Exception {

    AlipayClient defaultAlipayClient = new DefaultAlipayClient("https://open-sea.alipay.com",
        "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");

    final AlipayPayCancelRequest alipayPayCancelRequest = new AlipayPayCancelRequest();
    alipayPayCancelRequest.setClientId("SANDBOX_5Y378J2YC65Y03023");
    alipayPayCancelRequest.setPath("/ams/sandbox/api/v1/payments/cancel");
    alipayPayCancelRequest.setPaymentRequestId("P1588567922842841090");

    AlipayPayCancelResponse execute = defaultAlipayClient.execute(
        alipayPayCancelRequest);

    System.out.println("22");


  }


  @Override
  public String getIfCode() {
    return IF_CODE.ALI_GLOBAL_PAY;
  }

  @Override
  public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId,
      NoticeTypeEnum noticeTypeEnum) {

    try {

      JSONObject params = getReqParamJSON();

      log.info("支付回调参数：" + params.toJSONString());

      String payOrderId = params.getString("paymentRequestId");

      log.info("支付回调订单号：" + payOrderId);

      return MutablePair.of(payOrderId, params);

    } catch (Exception e) {
      log.error("error", e);
      throw ResponseException.buildText("ERROR");
    }
  }

  @Override
  public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayOrder payOrder,
      MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
    try {

      JSONObject res = new JSONObject();
      JSONObject _res = new JSONObject();
      _res.put("resultCode", "SUCCESS");
      _res.put("resultStatus", "S");
      _res.put("resultMessage", "success");
      res.put("result", _res);

      ResponseEntity okResponse = jsonResp(res);

      AliGlobalPayNormalMchParams normalMchParams = mchAppConfigContext.getNormalMchParamsByIfCode(
          IF_CODE.ALI_GLOBAL_PAY,
          AliGlobalPayNormalMchParams.class);

      String path = StrUtil.equals(normalMchParams.getSandbox() + "", "1")
          ? "/ams/sandbox/api/v1/payments/pay"
          : "/ams/api/v1/payments/pay";

      ChannelRetMsg result = new ChannelRetMsg();
      result.setResponseEntity(okResponse); //响应数据

      result.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认支付中

      JSONObject jsonParams = (JSONObject) params;

      String signature = request.getHeader("signature");

      signature = signature.split(",")[2].split("=")[1];

      String requestTime = request.getHeader("request-time");

      String clientId = request.getHeader("client-id");

      JSONObject rspBody = new JSONObject();
      rspBody.put("result", jsonParams.getJSONObject("result"));

      log.info("path:" + path);
      log.info("clientId:" + clientId);
      log.info("requestTime:" + requestTime);
      log.info("rspBody:" + rspBody.toJSONString());
      log.info("signature:" + signature);
      log.info("AlipayPublicKey:" + normalMchParams.getAlipayPublicKey());

      boolean verifyResult = SignatureTool.verify("POST", path, clientId, requestTime,
          rspBody.toJSONString(),
          signature, normalMchParams.getAlipayPublicKey());

      log.info("verifyResult:" + verifyResult);

      result.setResponseEntity(okResponse); //响应数据

      result.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认支付中
      result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);

      return result;

    } catch (Exception e) {
      log.error("error", e);
      throw ChannelException.sysError(e.getMessage());
    }
  }

}

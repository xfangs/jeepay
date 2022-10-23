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

    String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjRYT2nPTaoWqz3BXCNfYG3hzd+nlxrEPlx8xVe986v3P0W5MBwrW4rKHUVbAlYqKMd0uqURdY5i+rtKLNaETGUwPS8yyK7E31t7k3gX+1CbCTM2ksR6Qdz1NGfdlT9Ixv8NYgQEB+5F8YBahnWDQL2EB2tN1nRLghQXnSyTQtg6jOFkO/YDR9v93r5UQ+zcnw3MtoLtJsycrRd2hhma3gIudj/SrohroWi7Y0tSXsWMGhinx78gHiaFAwApzoZtBdhwPZnLb5zL8P9ETSkX5KQJNxDXEtIgOTKGuhhAL0qI6iGQlr9EXGMW0gXo1ozpMetMTN0JOqm13o3YgVMyvDwIDAQAB";

    String json = "{\"notifyType\":\"PAYMENT_RESULT\",\"result\":{\"resultCode\":\"SUCCESS\",\"resultStatus\":\"S\",\"resultMessage\":\"success\"},\"paymentRequestId\":\"20200101234567890444\",\"paymentId\":\"20200101234567890132\",\"paymentAmount\":{\"value\":\"8000\",\"currency\":\"EUR\"},\"actualPaymentAmount\":{\"value\":\"8000\",\"currency\":\"EUR\"},\"paymentCreateTime\":\"2020-01-01T12:01:00+08:30\",\"paymentTime\":\"2020-01-01T12:01:01+08:30\"}";

    String clientId = "SANDBOX_5Y378J2YC65Y03023";

    String requestTime = "2022-10-17T15:50:56Z";

    String signature = "PWDE9ZZeG927c9pvRgacEIc5u66GnzRDKvJ89Yh1%2B2maVPg9031ErlcLeb%2FOYhSyHN0YQmb%2Fdylq1DjQkc6TWVS2D%2BrFOr%2BMZS8ibqYY8%2F0%2B9bXcj7Auu7Hekc7L%2BBwi7%2BxHQiAPrk7HaDbUkuwIEqluS9Brk%2F%2FD86Jp%2FbMa42GMZFWETUGgREktZ01rPhS%2B5W2Nfqm%2BXYNdeyRfke4HkFyYGqLvJ%2BcyxS%2B7gos91sRt8CeOAfKen6SXJam4XWUNCYVnKrkrS0CoVNoobdvCo54H%2FGDPtoa%2F%2BJLSc6inWDvLCbzav21X%2BYg596AqPYZzSrdpvLD0zZ0F6fngHuo%2FZw%3D%3D";

    String path = "/ams/sandbox/api/v1/payments/pay";

    boolean verifyResult = SignatureTool.verify("POST", path, clientId, requestTime,
        json,
        signature, publicKey);

    System.out.println(verifyResult);


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

      ResponseEntity okResponse = textResp("SUCCESS");

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

      //验签失败
      if (!verifyResult) {
        throw ChannelException.sysError("签名验证失败");
      }

      if ("SUCCESS".equals(jsonParams.getJSONObject("result").getString("resultCode"))) {

        result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
      }

      return result;

    } catch (Exception e) {
      log.error("error", e);
      throw ChannelException.sysError(e.getMessage());
    }
  }

}

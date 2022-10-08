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

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS.IF_CODE;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
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

      ChannelRetMsg result = new ChannelRetMsg();
      result.setResponseEntity(okResponse); //响应数据

      result.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认支付中

      JSONObject jsonParams = (JSONObject) params;

      if ("SUCCESS".equals(jsonParams.getJSONObject("result").getString("resultCode"))) {

        result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
      }

      return result;

    } catch (Exception e) {
      log.error("error", e);
      throw ResponseException.buildText("ERROR");
    }
  }

}

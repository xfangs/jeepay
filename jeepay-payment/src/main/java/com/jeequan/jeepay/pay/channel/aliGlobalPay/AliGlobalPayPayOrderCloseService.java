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

import com.jeequan.jeepay.core.constants.CS.IF_CODE;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.channel.IPayOrderCloseService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 支付宝 关闭订单接口实现类
 *
 * @author xiaoyu
 * @site https://www.jeequan.com
 * @date 2022/1/25 11:17
 */
@Service
public class AliGlobalPayPayOrderCloseService implements IPayOrderCloseService {

  @Autowired
  private ConfigContextQueryService configContextQueryService;

  @Override
  public String getIfCode() {
    return IF_CODE.ALI_GLOBAL_PAY;
  }

  @Override
  public ChannelRetMsg close(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

    return null;
  }


}

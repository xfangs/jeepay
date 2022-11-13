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
package com.jeequan.jeepay.mch.ctrl.payconfig;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeequan.jeepay.core.entity.PayWay;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchPayPassageService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.PayWayService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付方式配置类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@RestController
@RequestMapping("api/payWays")
public class PayWayController extends CommonCtrl {

  @Autowired
  PayWayService payWayService;
  @Autowired
  MchPayPassageService mchPayPassageService;
  @Autowired
  PayOrderService payOrderService;

  /**
   * @Author: ZhuXiao
   * @Description: list
   * @Date: 15:52 2021/4/27
   */

  @GetMapping
  public ApiRes list() {

    List<PayWay> payWayList = payWayService.list(new QueryWrapper<PayWay>()
        .inSql("way_code", StrUtil.format(
            "select way_code from t_mch_pay_passage where mch_no = '{}' and if_code = '{}'",
            getCurrentMchNo(), getValStringRequired("ifCode")))
    );

    return ApiRes.ok(payWayList);
  }

}

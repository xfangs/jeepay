package com.jeequan.jeepay.mch.ctrl.payconfig;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeequan.jeepay.core.entity.PayInterfaceDefine;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PayInterfaceDefineService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/payPassage")
public class PayPassageController extends CommonCtrl {

  @Autowired
  private PayInterfaceDefineService payInterfaceDefineService;

  @GetMapping
  public ApiRes list() {

    List<PayInterfaceDefine> payInterfaceDefineList = payInterfaceDefineService.list(
        new QueryWrapper<PayInterfaceDefine>()
            .select("if_code", "if_name", "app_icon")
            .inSql("if_code",
                StrUtil.format("select if_code from t_mch_pay_passage where mch_no = '{}'",
                    getCurrentMchNo()))
    );


    return ApiRes.ok(payInterfaceDefineList);

  }


}

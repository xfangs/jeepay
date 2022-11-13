package com.jeequan.jeepay.mch.ctrl.dict;

import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.DictItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/anon/dict")
public class DictController extends CommonCtrl {

  @Autowired
  private DictItemService dictItemService;


  @GetMapping("/getDictText/{code}/{value}")
  public ApiRes getDictText(@PathVariable("code") String code,
      @PathVariable("value") String value) {

    String dictText = dictItemService.getDictText(code, value);

    return ApiRes.ok(dictText);
  }


  @GetMapping("/getDictItems/{code}")
  public ApiRes getDictItems(@PathVariable("code") String code) {

    return ApiRes.ok(dictItemService.getDictItems(code));

  }


}

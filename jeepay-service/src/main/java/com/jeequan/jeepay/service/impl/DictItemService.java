package com.jeequan.jeepay.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.entity.Dict;
import com.jeequan.jeepay.core.entity.DictItem;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.mapper.DictItemMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DictItemService extends ServiceImpl<DictItemMapper, DictItem> {

  @Autowired
  private DictService dictService;


  public String getDictText(String code, String value) {

    String key = StrUtil.format("dict_{}_{}", code, value);

    String text = RedisUtil.getString(key);

    if (text == null) {

      text = getBaseMapper().getDictText(code, value);
      RedisUtil.setString(key, text);
    }

    return text;
  }


  public List<DictItem> getDictItems(String code) {

    String key = StrUtil.format("dict_{}", code);

    List<DictItem> dictItemList = RedisUtil.getObject(key, ArrayList.class);

    if (dictItemList == null) {

      Dict dict = dictService.getOne(new QueryWrapper<Dict>().eq("dict_code", code));

      if (dict == null) {
        throw new BizException(StrUtil.format("code：{} 不存在"));
      }

      dictItemList = list(new QueryWrapper<DictItem>()
          .eq("dict_id", dict.getId())
      );

      RedisUtil.set(key,dictItemList);
    }

    return dictItemList;


  }


}

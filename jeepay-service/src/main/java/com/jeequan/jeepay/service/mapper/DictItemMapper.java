package com.jeequan.jeepay.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jeequan.jeepay.core.entity.DictItem;
import org.apache.ibatis.annotations.Param;

public interface DictItemMapper extends BaseMapper<DictItem> {

  String getDictText(@Param("code") String code, @Param("value") String value);


}

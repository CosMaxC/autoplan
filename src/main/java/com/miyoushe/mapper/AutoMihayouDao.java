package com.miyoushe.mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.miyoushe.model.AutoMihayou;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AutoMihayouDao extends BaseMapper<AutoMihayou> {

    List<AutoMihayou> selectMine(Integer userid);

    AutoMihayou selectBystuid(String suid);

    AutoMihayou selectByGenshinUid(String genshinUid);
    AutoMihayou selectOneByUserIdAndSuid(@Param("userId")Integer userId,@Param("suid")String suid);


}
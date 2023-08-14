package com.ttpfx.service.impl;

import com.ttpfx.entity.User;
import com.ttpfx.service.UserService;
import com.ttpfx.service.UserTypeService;
import lombok.extern.slf4j.Slf4j;
import org.h2.util.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserTypeServiceImpl implements UserTypeService {


    @Resource
    private StringRedisTemplate redisUserTypeTemplate;
    @Resource
    private UserService userService;
    @Override
    public int updateUserType(User user) {

        // 保存用户信息到Redis
        redisUserTypeTemplate.opsForValue().set("USER_TYPE_"+user.getUsername(), user.getUserType().toString(),3600, TimeUnit.SECONDS);
        //插入db
        log.info("redis  user type update ok");

        User updateUser = new User();
        updateUser.setUserType(user.getUserType());
        updateUser.setUsername(user.getUsername());
        int updateNum = userService.updateUserType(updateUser);
        return updateNum;
    }

    @Override
    public Integer getUserType(String usename) {
        String userTypeStr = redisUserTypeTemplate.opsForValue().get("USER_TYPE_"+usename);
        if(StringUtils.isNullOrEmpty(userTypeStr)){
            User user = userService.queryByName(usename);
            userTypeStr = user.getUserType().toString();
        }
        return Integer.valueOf(userTypeStr);

    }
}

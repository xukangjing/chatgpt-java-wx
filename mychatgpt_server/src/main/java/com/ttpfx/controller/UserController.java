package com.ttpfx.controller;
import com.alibaba.fastjson.JSON;
import com.mysql.cj.util.StringUtils;
import com.ttpfx.entity.User;
import com.ttpfx.service.UserService;
import com.ttpfx.service.UserTypeService;
import com.ttpfx.utils.R;
import com.ttpfx.vo.param.UserLoginRequest;
import com.ttpfx.vo.param.UserRegisterRequest;
import com.ttpfx.vo.param.UserUpdateRequest;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private RedisTemplate<String, User> redisTemplate;
    @Resource
    private UserService userService;
    private static Long currentUserId = 0L;

    public static ConcurrentHashMap<String, User> loginUser = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Long> loginUserKey = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Integer> loginUserTypeKey = new ConcurrentHashMap<>();
    @Resource
    private UserTypeService userTypeService;




    @PostMapping("/register")
    public R register(@Validated @RequestBody UserRegisterRequest request) {
        log.info("进入注册==》");
        // 非空校验和长度限制
        if (request.getUsername() == null || request.getPassword() == null ||
                request.getUsername().isEmpty() || request.getPassword().isEmpty()) {
//            return ResponseEntity.badRequest().body("用户名和密码不能为空");
            return R.fail("用户名和密码不能为空");

        }
        if (request.getUsername().length() > 20 || request.getPassword().length() > 20) {
//            return ResponseEntity.badRequest().body("用户名和密码长度不能超过20个字符");
            return R.fail("用户名和密码长度不能超过20个字符");

        }

        // 检查用户名是否已存在
        if (redisTemplate.opsForHash().hasKey("users", request.getUsername())) {
//            return ResponseEntity.badRequest().body("用户名已存在");
            log.info("redis check user repeat");
            return R.fail("用户名已存在");

        }

        // 保存用户信息到Redis
        // 保存用户信息到Redis
        Long userId = generateUserId(); // 生成用户ID，可以根据实际情况自行实现
        User user = new User(userId, request.getUsername(), request.getPassword(), LocalDateTime.now(),0);
        String userJson = JSON.toJSONString(user);
        redisTemplate.opsForHash().put("users", user.getUsername(), userJson);
//        return ResponseEntity.ok("注册成功");
        //插入db
        log.info("redis  user insert ok");

        int insertNum = userService.addUser(user);
        log.info("mysql  user insert ok,insertNum={}",insertNum);

        return R.ok("注册成功");

    }
    private synchronized Long generateUserId() {
        // 自增当前最大的ID值
        currentUserId++;
        return currentUserId;
    }
    @PostMapping("/login")
    public R login(@Validated @RequestBody UserLoginRequest request) {
        log.info("登录开始==》");

        // 非空校验和长度限制
        if (request.getUsername() == null || request.getPassword() == null ||
                request.getUsername().isEmpty() || request.getPassword().isEmpty()) {
//            return ResponseEntity.badRequest().body("用户名和密码不能为空");
            return R.fail("用户名和密码不能为空");
        }
        if (request.getUsername().length() > 20 || request.getPassword().length() > 20) {
//            return ResponseEntity.badRequest().body("用户名和密码长度不能超过20个字符");
            return R.fail("用户名和密码长度不能超过20个字符");

        }

        // 根据用户名从Redis中获取用户信息
//        String storedUserStr = redisTemplate.opsForHash().get("users", request.getUsername());
        String userJson = (String) redisTemplate.opsForHash().get("users", request.getUsername());
        log.info("login user from redis,userJson={}",userJson);

        User storedUser =new User();
        if (userJson != null) {
            storedUser = JSON.parseObject(userJson, User.class);
            // 现在 user 就是您从 Redis 中获取的 User 对象了
        }
        if (storedUser.getUsername() == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名不存在");
            //查询db
            storedUser = userService.queryByName(request.getUsername());
            log.info("login user from db,storedUser={}",JSON.toJSONString(storedUser));

            if(Objects.isNull(storedUser)){
                return R.fail("用户名不存在");
            }

        }

        // 检查密码是否正确
        if (!request.getPassword().equals(storedUser.getPassword())) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("密码错误");
            return R.fail("密码错误");

        }
        String username = request.getUsername();
        loginUser.put(username, storedUser);
        loginUserKey.put(username, System.currentTimeMillis());
        Integer userType = getUserTypeByName(username);
        loginUserTypeKey.put(username,userType);
        return R.ok(String.valueOf(loginUserKey.get(username)),userType);

//        return ResponseEntity.ok(String.valueOf(loginUserKey.get(username)));
    }
    @RequestMapping("/logout")
    public R logout(String username) {
        loginUser.remove(username);
        loginUserKey.remove(username);
        return R.ok();
    }

    @RequestMapping("/checkUserKey")
    public R checkUserKey(String username, Long key){
        if (StringUtils.isNullOrEmpty(username)){
            return R.fail("请重新登录");
        }
//        if (StringUtils.isNullOrEmpty(username) || key == null)return R.fail("请重新登录");
//        if (!Objects.equals(loginUserKey.get(username), key)){
//            return R.fail("用户在其他地方登录！！！");
//        }
        Integer userType = loginUserTypeKey.get(username) == null ? 0 : loginUserTypeKey.get(username);

        return R.ok(userType.toString());
    }

    @RequestMapping("/loginUser")
    public R loginUser(){
        return R.ok("success",loginUser.keySet());
    }

    @RequestMapping("/users")
    public List<String> getAllUsers() {
        // 从Redis的哈希结构中获取所有用户信息
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        Map<String, String> userMap = hashOperations.entries("users");
        return new ArrayList<>(userMap.values());
    }


    @PostMapping("/updateUserType")
    public R updateUserType(@Validated @RequestBody UserUpdateRequest request) {
        log.info("进入用户类型更新==》");
        // 非空校验和长度限制
        if (request.getUsername() == null || request.getUserType() == null ||
                request.getUsername().isEmpty() ) {
            return R.fail("用户名和用户类型不能为空");
        }
        User user = new User();
        user.setUserType(request.getUserType());
        user.setUsername(request.getUsername());
        int updateNum = userTypeService.updateUserType(user);
        log.info("mysql  user type update ok,updateNum={}",updateNum);
        return R.ok("更新成功");

    }

    private Integer getUserTypeByName(String key) {

        Integer userType = userTypeService.getUserType(key);
        return  userType;
    }
}

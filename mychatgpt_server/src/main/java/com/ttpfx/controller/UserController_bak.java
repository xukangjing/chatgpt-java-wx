//package com.ttpfx.controller;
//
//import com.ttpfx.entity.User;
//import com.ttpfx.service.UserService;
//import com.ttpfx.utils.R;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.HashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.web.bind.annotation.*;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * @author ttpfx
// * @date 2023/3/29
// */
//@Slf4j
//@RestController
//@RequestMapping("/user")
//public class UserController_bak {
//
//    @Resource
//    private UserService userService;
////    @Autowired
////    private UserRepository userRepository;
//    @Resource
//    private RedisTemplate<String, User> redisTemplate;
//    private final String USER_HASH_KEY = "users"; // Redis哈希结构的键名
//    public static ConcurrentHashMap<String, User> loginUser = new ConcurrentHashMap<>();
//
//    public static ConcurrentHashMap<String, Long> loginUserKey = new ConcurrentHashMap<>();
//    @RequestMapping("/login")
//    public R login(String username, String password) {
//        if (username == null) return R.fail("必须填写用户名");
//
//
//        User user = userService.queryByName(username);
//        if (user == null) return R.fail("用户名不存在");
//        String targetPassword = user.getPassword();
//        if (targetPassword == null) return R.fail("用户密码异常");
//        if (!targetPassword.equals(password)) return R.fail("密码错误");
//
//        loginUser.put(username, user);
//        loginUserKey.put(username, System.currentTimeMillis());
//        return R.ok(String.valueOf(loginUserKey.get(username)));
//    }
//
//    @RequestMapping("/logout")
//    public R logout(String username) {
//        loginUser.remove(username);
//        loginUserKey.remove(username);
//        return R.ok();
//    }
//
//    @RequestMapping("/checkUserKey")
//    public R checkUserKey(String username, Long key){
//        if (username==null || key == null)return R.fail("用户校验异常");
//        if (!Objects.equals(loginUserKey.get(username), key)){
//            return R.fail("用户在其他地方登录！！！");
//        }
//        return R.ok();
//    }
//
//    @RequestMapping("/loginUser")
//    public R loginUser(){
//        return R.ok("success",loginUser.keySet());
//    }
//
//
//
//
//
//    @PostMapping("/register")
//    public R register(@RequestBody User user) {
//        // 从Redis的哈希结构中检查用户名是否已经存在
//        HashOperations<String, Long, User> hashOperations = redisTemplate.opsForHash();
//        User existingUser = hashOperations.get(USER_HASH_KEY, user.getId());
//        if (existingUser != null) {
//            log.info("user:{} aready exist",user.getUsername());
//            return R.fail("用户名已存在");
//        }
//
//        // 保存用户信息到Redis的哈希结构中
//        hashOperations.put(USER_HASH_KEY, user.getId(), user);
//
//        return  R.ok("注册成功");
//    }
//
//    @GetMapping("/users")
//    public List<User> getAllUsers() {
//        // 从Redis的哈希结构中获取所有用户信息
//        HashOperations<String, Long, User> hashOperations = redisTemplate.opsForHash();
//        Map<Long, User> userMap = hashOperations.entries(USER_HASH_KEY);
//        List<User> userList =  new ArrayList<>(userMap.values());
//        log.info("all users");
//        return userList;
//    }
//}

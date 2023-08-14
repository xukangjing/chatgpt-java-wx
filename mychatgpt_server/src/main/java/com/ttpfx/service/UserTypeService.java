package com.ttpfx.service;

import com.ttpfx.entity.User;

public interface UserTypeService {
    int updateUserType(User user);
    Integer getUserType(String usename);
}

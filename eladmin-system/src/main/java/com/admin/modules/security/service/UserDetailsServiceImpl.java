/*
 *  Copyright 2019-2020 Fang Jin Biao
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.admin.modules.security.service;

import com.admin.exception.BadRequestException;
import com.admin.modules.system.controller.dto.AdminJwtUserDto;
import com.admin.modules.system.controller.dto.UserDto;
import com.admin.modules.system.service.IDataService;
import com.admin.modules.system.service.IRoleService;
import com.admin.modules.system.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Zheng Jie
 * @date 2018-11-22
 */
@Service("userDetailsService")
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final IUserService userService;

    private final IRoleService roleService;

    private final IDataService dataService;

    @Override
    public AdminJwtUserDto loadUserByUsername(String username) {
        UserDto user = userService.findByName(username);
        if (user == null) {
            throw new BadRequestException("账号不存在");
        } else {
            if (!user.getEnabled()) {
                throw new BadRequestException("账号未激活");
            }
            return new AdminJwtUserDto(user, dataService.getDeptIds(user),
                    (List<GrantedAuthority>) roleService.mapToGrantedAuthorities(user));
        }
    }
}

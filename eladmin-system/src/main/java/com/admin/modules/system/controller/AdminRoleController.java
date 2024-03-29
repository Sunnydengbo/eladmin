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
package com.admin.modules.system.controller;

import cn.hutool.core.lang.Dict;
import com.admin.annotation.Log;
import com.admin.exception.BadRequestException;
import com.admin.modules.system.controller.dto.RoleDto;
import com.admin.modules.system.controller.dto.RoleQueryCriteria;
import com.admin.modules.system.controller.dto.RoleSmallDto;
import com.admin.modules.system.model.RoleModel;
import com.admin.modules.system.service.IRoleService;
import com.admin.utils.SecurityUtils;
import com.admin.utils.Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author adyfang
 * @date 2020年5月2日
 */
@Api(tags = "系统：角色管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class AdminRoleController {
	private static final String ENTITY_NAME = "role";
	private final IRoleService roleService;

	@ApiOperation("获取单个role")
	@GetMapping(value = "/{id}")
	@PreAuthorize("@el.check('roles:list')")
	public ResponseEntity<Object> getRoles(@PathVariable Long id) {
		return new ResponseEntity<>(roleService.findById(id), HttpStatus.OK);
	}

	@Log("导出角色数据")
	@ApiOperation("导出角色数据")
	@GetMapping(value = "/download")
	@PreAuthorize("@el.check('role:list')")
	public void download(HttpServletResponse response, RoleQueryCriteria criteria) throws IOException {
		roleService.download(roleService.queryAll(criteria), response);
	}

	@ApiOperation("返回全部的角色")
	@GetMapping(value = "/all")
	@PreAuthorize("@el.check('roles:list','user:add','user:edit')")
	public ResponseEntity<Object> getAll() {
		return new ResponseEntity<>(roleService.queryAll(), HttpStatus.OK);
	}

	@Log("查询角色")
	@ApiOperation("查询角色")
	@GetMapping
	@PreAuthorize("@el.check('roles:list')")
	public ResponseEntity<Object> getRoles(RoleQueryCriteria criteria, Pageable pageable) {
		return new ResponseEntity<>(roleService.queryAll(criteria, Utils.convertPage(pageable)), HttpStatus.OK);
	}

	@ApiOperation("获取用户级别")
	@GetMapping(value = "/level")
	public ResponseEntity<Object> getLevel() {
		return new ResponseEntity<>(Dict.create().set("level", getLevels(null)), HttpStatus.OK);
	}

	@Log("新增角色")
	@ApiOperation("新增角色")
	@PostMapping
	@PreAuthorize("@el.check('roles:add')")
	public ResponseEntity<Object> create(@Validated @RequestBody RoleModel resources) {
		if (resources.getId() != null) {
			throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
		}
		getLevels(resources.getLevel());
		roleService.create(resources);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Log("修改角色")
	@ApiOperation("修改角色")
	@PutMapping
	@PreAuthorize("@el.check('roles:edit')")
	public ResponseEntity<Object> update(@Validated(RoleModel.Update.class) @RequestBody RoleModel resources) {
		getLevels(resources.getLevel());
		roleService.update(resources);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Log("修改角色菜单")
	@ApiOperation("修改角色菜单")
	@PutMapping(value = "/menu")
	@PreAuthorize("@el.check('roles:edit')")
	public ResponseEntity<Object> updateMenu(@RequestBody RoleModel resources) {
		RoleDto role = roleService.findById(resources.getId());
		getLevels(role.getLevel());
		roleService.updateMenu(resources, role);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Log("删除角色")
	@ApiOperation("删除角色")
	@DeleteMapping
	@PreAuthorize("@el.check('roles:del')")
	public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
		for (Long id : ids) {
			RoleDto role = roleService.findById(id);
			getLevels(role.getLevel());
		}
		// 验证是否被用户关联
		roleService.verification(ids);
		roleService.removeByIds(ids);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * 获取用户的角色级别
	 *
	 * @return /
	 */
	private int getLevels(Integer level) {
		List<Integer> levels = roleService.findByUsersId(SecurityUtils.getCurrentUserId()).stream()
				.map(RoleSmallDto::getLevel).collect(Collectors.toList());
		int min = Collections.min(levels);
		if (level != null) {
			if (level < min) {
				throw new BadRequestException("权限不足，你的角色级别：" + min + "，低于操作的角色级别：" + level);
			}
		}
		return min;
	}
}

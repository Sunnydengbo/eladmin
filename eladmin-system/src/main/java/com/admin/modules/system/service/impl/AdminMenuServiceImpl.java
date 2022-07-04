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
package com.admin.modules.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.admin.exception.BadRequestException;
import com.admin.exception.EntityExistException;
import com.admin.modules.system.controller.dto.MenuDto;
import com.admin.modules.system.controller.dto.MenuQueryCriteria;
import com.admin.modules.system.controller.dto.RoleSmallDto;
import com.admin.modules.system.mapper.IMenuMapper;
import com.admin.modules.system.mapper.IUserMapper;
import com.admin.modules.system.model.MenuModel;
import com.admin.modules.system.model.UserModel;
import com.admin.modules.system.model.vo.MenuMetaVo;
import com.admin.modules.system.model.vo.MenuVo;
import com.admin.modules.system.service.IMenuService;
import com.admin.modules.system.service.IRoleService;
import com.admin.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author adyfang
 * @date 2020年5月2日
 */
@RequiredArgsConstructor
@Service
@CacheConfig(cacheNames = "menu")
public class AdminMenuServiceImpl extends ServiceImpl<IMenuMapper, MenuModel> implements IMenuService {
	private final Mapper mapper;

	private final IRoleService roleService;

	private final IUserMapper userMapper;

	private final RedisUtils redisUtils;

	@Override
	public List<MenuDto> queryAll(MenuQueryCriteria criteria, Boolean isQuery) {
		List<MenuDto> list = DozerUtils.mapList(mapper, this.list(buildWrapper(criteria, isQuery)), MenuDto.class);
		return list;
	}

	private QueryWrapper<MenuModel> buildWrapper(MenuQueryCriteria criteria, Boolean isQuery) {
		QueryWrapper<MenuModel> query = null;
		if (null != criteria) {
			query = new QueryWrapper<MenuModel>();
			if (isQuery) {
				criteria.setPidIsNull(true);
			}
			boolean notEmpty = null != criteria.getPid() || StringUtils.isNotEmpty(criteria.getBlurry())
					|| CollectionUtils.isNotEmpty(criteria.getCreateTime());
			if (isQuery && notEmpty) {
				criteria.setPidIsNull(null);
			}
			boolean haveTime = CollectionUtils.isNotEmpty(criteria.getCreateTime())
					&& criteria.getCreateTime().size() >= 2;
			Timestamp start = haveTime ? criteria.getCreateTime().get(0) : null;
			Timestamp end = haveTime ? criteria.getCreateTime().get(1) : null;
			boolean isBlurry = StringUtils.isNotEmpty(criteria.getBlurry());
			query.lambda().eq(null != criteria.getPid(), MenuModel::getPid, criteria.getPid())
					.like(isBlurry, MenuModel::getTitle, criteria.getBlurry())
					.like(isBlurry, MenuModel::getComponent, criteria.getBlurry())
					.like(isBlurry, MenuModel::getPermission, criteria.getBlurry())
					.between(haveTime, MenuModel::getCreateTime, start, end)
					.isNull(isQuery && null != criteria.getPidIsNull() && criteria.getPidIsNull(), MenuModel::getPid)
					.orderByAsc(MenuModel::getMenuSort);
		}
		return query;
	}

	@Override
	@Cacheable(key = "'id:' + #p0")
	public MenuDto findById(long id) {
		MenuModel menu = Optional.ofNullable(this.getById(id)).orElseGet(MenuModel::new);
		ValidationUtil.isNull(menu.getId(), "Menu", "id", id);
		return mapper.map(menu, MenuDto.class);
	}

	/**
	 * 用户角色改变时需清理缓存
	 *
	 * @param currentUserId /
	 * @return /
	 */
	@Override
	@Cacheable(key = "'user:' + #p0")
	public List<MenuDto> findByUser(Long currentUserId) {
		List<RoleSmallDto> roles = roleService.findByUsersId(currentUserId);
		Set<Long> roleIds = roles.stream().map(RoleSmallDto::getId).collect(Collectors.toSet());
		LinkedHashSet<MenuModel> menus = this.baseMapper.selectLinkRole(roleIds, 2L);
		return menus.stream().map(menu -> mapper.map(menu, MenuDto.class)).collect(Collectors.toList());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void create(MenuModel resources) {
		QueryWrapper<MenuModel> query = new QueryWrapper<MenuModel>();
		query.lambda().eq(MenuModel::getTitle, resources.getTitle());
		if (this.getOne(query) != null) {
			throw new EntityExistException(MenuModel.class, "title", resources.getTitle());
		}
		if (StringUtils.isNotBlank(resources.getComponentName())) {
			QueryWrapper<MenuModel> query2 = new QueryWrapper<MenuModel>();
			query2.lambda().eq(MenuModel::getComponentName, resources.getComponentName());
			if (this.getOne(query2) != null) {
				throw new EntityExistException(MenuModel.class, "组件名称", resources.getComponentName());
			}
		}
		if (resources.getIFrame()) {
			String http = "http://", https = "https://";
			if (!(resources.getPath().toLowerCase().startsWith(http)
					|| resources.getPath().toLowerCase().startsWith(https))) {
				throw new BadRequestException("外链必须以http://或者https://开头");
			}
		}
		resources.setSubCount(resources.getSubCount() == null ? 0 : resources.getSubCount());
		resources.setMenuSort(resources.getMenuSort() == null ? 999 : resources.getMenuSort());
		this.save(resources);

		// 计算子节点数目
		if (resources.getPid() != null) {
			// 清理缓存
			updateSubCnt(resources.getPid());
		}
		redisUtils.del("menu::pid:" + (resources.getPid() == null ? 0 : resources.getPid()));
		List<String> keys = redisUtils.scan("menu::user:*");
		keys.forEach(item -> redisUtils.del(item));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(MenuModel resources) {
		if (resources.getId().equals(resources.getPid())) {
			throw new BadRequestException("上级不能为自己");
		}
		MenuModel menu = Optional.ofNullable(this.getById(resources.getId())).orElseGet(MenuModel::new);
		// 记录旧的父节点ID
		Long pid = menu.getPid();
		ValidationUtil.isNull(menu.getId(), "Permission", "id", resources.getId());

		if (resources.getIFrame()) {
			String http = "http://", https = "https://";
			if (!(resources.getPath().toLowerCase().startsWith(http)
					|| resources.getPath().toLowerCase().startsWith(https))) {
				throw new BadRequestException("外链必须以http://或者https://开头");
			}
		}
		QueryWrapper<MenuModel> query = new QueryWrapper<MenuModel>();
		query.lambda().eq(MenuModel::getTitle, resources.getTitle());
		MenuModel menu1 = this.getOne(query);

		if (menu1 != null && !menu1.getId().equals(menu.getId())) {
			throw new EntityExistException(MenuModel.class, "name", resources.getTitle());
		}

		if (resources.getPid().equals(0L)) {
			resources.setPid(null);
		}

		if (StringUtils.isNotBlank(resources.getComponentName())) {
			QueryWrapper<MenuModel> query2 = new QueryWrapper<MenuModel>();
			query2.lambda().eq(MenuModel::getComponentName, resources.getComponentName());
			menu1 = this.getOne(query2);
			if (menu1 != null && !menu1.getId().equals(menu.getId())) {
				throw new EntityExistException(MenuModel.class, "componentName", resources.getComponentName());
			}
		}

		// 类型从菜单或按钮变更为目录，清空路径和权限
		if (menu.getType() != MenuType.FOLDER.getValue() && resources.getType() == MenuType.FOLDER.getValue()) {
			menu.setComponent(null).setPermission(null).setComponentName(null);
		} else {
			menu.setComponent(resources.getComponent()).setPermission(resources.getPermission())
					.setComponentName(resources.getComponentName());
		}
		menu.setTitle(resources.getTitle());
		menu.setPath(resources.getPath());
		menu.setIcon(resources.getIcon());
		menu.setIFrame(resources.getIFrame());
		menu.setPid(resources.getPid());
		menu.setMenuSort(resources.getMenuSort());
		menu.setCache(resources.getCache());
		menu.setHidden(resources.getHidden());
		menu.setType(resources.getType());
		this.saveOrUpdate(menu);

		// 计算子节点数目
		if (resources.getPid() == null) {
			updateSubCnt(pid);
		} else {
			pid = resources.getPid();
			updateSubCnt(resources.getPid());
		}
		// 清理缓存
		delCaches(resources.getId(), pid);
	}

	@Override
	public Set<MenuModel> getDeleteMenus(List<MenuModel> menuList, Set<MenuModel> menuSet) {
		// 递归找出待删除的菜单
		for (MenuModel menu1 : menuList) {
			menuSet.add(menu1);
			QueryWrapper<MenuModel> query = new QueryWrapper<MenuModel>();
			query.lambda().eq(MenuModel::getPid, menu1.getId());
			List<MenuModel> menus = this.list(query);
			if (menus != null && menus.size() != 0) {
				getDeleteMenus(menus, menuSet);
			}
		}
		return menuSet;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delete(Set<MenuModel> menuSet) {
		for (MenuModel menu : menuSet) {
			// 清理缓存
			delCaches(menu.getId(), menu.getPid());
			roleService.untiedMenu(menu.getId());
			this.removeById(menu.getId());
			if (menu.getPid() != null) {
				updateSubCnt(menu.getPid());
			}
		}
	}

	@Override
	@Cacheable(key = "'tree'")
	public Object getMenuTree(Long pid) {
		Map<Long, List<MenuModel>> allMap = this.list().stream().collect(Collectors.groupingBy(MenuModel::getPid));
		return buildMenuTree(allMap, pid);
	}

	private Object buildMenuTree(Map<Long, List<MenuModel>> allMap, Long pid) {
		List<Map<String, Object>> list = new LinkedList<>();
		allMap.get(pid).forEach(menu -> {
			if (menu != null) {
				List<MenuModel> menuList = allMap.get(menu.getId());
				Map<String, Object> map = new HashMap<>(16);
				map.put("id", menu.getId());
				map.put("label", menu.getTitle());
				if (menuList != null && menuList.size() != 0) {
					map.put("children", getMenuTree(menu.getId()));
				}
				list.add(map);
			}
		});
		return list;
	}

	@Override
	@Cacheable(key = "'pid:' + #p0")
	public List<MenuDto> getMenus(Long pid) {
		List<MenuModel> menus;
		if (pid != null && !pid.equals(0L)) {
			QueryWrapper<MenuModel> query = new QueryWrapper<MenuModel>();
			query.lambda().eq(MenuModel::getPid, pid);
			menus = this.list(query);
		} else {
			QueryWrapper<MenuModel> query = new QueryWrapper<MenuModel>();
			query.lambda().isNull(MenuModel::getPid);
			menus = this.list(query);
		}
		return DozerUtils.mapList(mapper, menus, MenuDto.class);
	}

	@Override
	public List<MenuDto> getSuperior(MenuDto menuDto, List<MenuModel> menus) {
		QueryWrapper<MenuModel> query = new QueryWrapper<MenuModel>();
		if (menuDto.getPid() == null) {
			query.lambda().isNull(MenuModel::getPid);
			menus.addAll(this.list(query));
			return DozerUtils.mapList(mapper, menus, MenuDto.class);
		}
		query.lambda().eq(MenuModel::getPid, menuDto.getPid());
		menus.addAll(this.list(query));
		return getSuperior(findById(menuDto.getPid()), menus);
	}

	@Override
	public List<MenuDto> buildTree(List<MenuDto> menuDtos) {
		List<MenuDto> trees = new ArrayList<>();
		Set<Long> ids = new HashSet<>();
		for (MenuDto menuDTO : menuDtos) {
			if (null == menuDTO.getPid()) {
				trees.add(menuDTO);
			}
			for (MenuDto it : menuDtos) {
				if (it.getPid() != null && it.getPid().equals(menuDTO.getId())) {
					if (menuDTO.getChildren() == null) {
						menuDTO.setChildren(new ArrayList<>());
					}
					menuDTO.getChildren().add(it);
					ids.add(it.getId());
				}
			}
		}
		if (trees.size() == 0) {
			trees = menuDtos.stream().filter(s -> !ids.contains(s.getId())).collect(Collectors.toList());
		}
		return trees;
	}

	@Override
	public List<MenuVo> buildMenus(List<MenuDto> menuDtos) {
		List<MenuVo> list = new LinkedList<>();
		menuDtos.forEach(menuDTO -> {
			if (menuDTO != null) {
				List<MenuDto> menuDtoList = menuDTO.getChildren();
				MenuVo menuVo = new MenuVo();
				menuVo.setName(ObjectUtil.isNotEmpty(menuDTO.getComponentName()) ? menuDTO.getComponentName()
						: menuDTO.getTitle());
				// 一级目录需要加斜杠，不然会报警告
				menuVo.setPath(menuDTO.getPid() == null ? "/" + menuDTO.getPath() : menuDTO.getPath());
				menuVo.setHidden(menuDTO.getHidden());
				// 如果不是外链
				if (!menuDTO.getIFrame()) {
					if (menuDTO.getPid() == null) {
						menuVo.setComponent(
								StrUtil.isEmpty(menuDTO.getComponent()) ? "Layout" : menuDTO.getComponent());
					} else if (!StrUtil.isEmpty(menuDTO.getComponent())) {
						menuVo.setComponent(menuDTO.getComponent());
					}
				}
				menuVo.setMeta(new MenuMetaVo(menuDTO.getTitle(), menuDTO.getIcon(), !menuDTO.getCache()));
				if (menuDtoList != null && menuDtoList.size() != 0) {
					menuVo.setAlwaysShow(true);
					menuVo.setRedirect("noredirect");
					menuVo.setChildren(buildMenus(menuDtoList));
					// 处理是一级菜单并且没有子菜单的情况
				} else if (menuDTO.getPid() == null) {
					MenuVo menuVo1 = new MenuVo();
					menuVo1.setMeta(menuVo.getMeta());
					// 非外链
					if (!menuDTO.getIFrame()) {
						menuVo1.setPath("index");
						menuVo1.setName(menuVo.getName());
						menuVo1.setComponent(menuVo.getComponent());
					} else {
						menuVo1.setPath(menuDTO.getPath());
					}
					menuVo.setName(null);
					menuVo.setMeta(null);
					menuVo.setComponent("Layout");
					List<MenuVo> list1 = new ArrayList<>();
					list1.add(menuVo1);
					menuVo.setChildren(list1);
				}
				list.add(menuVo);
			}
		});
		return list;
	}

	@Override
	public MenuModel findOne(Long id) {
		MenuModel menu = Optional.ofNullable(this.getById(id)).orElseGet(MenuModel::new);
		ValidationUtil.isNull(menu.getId(), "Menu", "id", id);
		return menu;
	}

	@Override
	public void download(List<MenuDto> menuDtos, HttpServletResponse response) throws IOException {
		List<Map<String, Object>> list = new ArrayList<>(menuDtos.size());
		for (MenuDto menuDTO : menuDtos) {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("菜单标题", menuDTO.getTitle());
			map.put("菜单类型", menuDTO.getType() == null ? "目录" : menuDTO.getType() == 1 ? "菜单" : "按钮");
			map.put("权限标识", menuDTO.getPermission());
			map.put("外链菜单", menuDTO.getIFrame() ? "是" : "否");
			map.put("菜单可见", menuDTO.getHidden() ? "否" : "是");
			map.put("是否缓存", menuDTO.getCache() ? "是" : "否");
			map.put("创建日期", menuDTO.getCreateTime());
			list.add(map);
		}
		FileUtil.downloadExcel(list, response);
	}

	private void updateSubCnt(Long menuId) {
		QueryWrapper<MenuModel> query = new QueryWrapper<MenuModel>();
		query.lambda().eq(MenuModel::getPid, menuId);
		int count = this.count(query);

		UpdateWrapper<MenuModel> update = new UpdateWrapper<MenuModel>();
		update.lambda().eq(MenuModel::getId, menuId);
		MenuModel menu = new MenuModel();
		menu.setSubCount(count);
		this.update(menu, update);
	}

	/**
	 * 清理缓存
	 *
	 * @param id  菜单ID
	 * @param pid 菜单父级ID
	 */
	public void delCaches(Long id, Long pid) {
		List<UserModel> users = userMapper.findByMenuId(id);
		redisUtils.del("menu::id:" + id);
		redisUtils.delByKeys("menu::user:", users.stream().map(UserModel::getId).collect(Collectors.toSet()));
		redisUtils.del("menu::pid:" + (pid == null ? 0 : pid));
	}
}

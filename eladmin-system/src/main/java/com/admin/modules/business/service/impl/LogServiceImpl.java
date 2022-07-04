
package com.admin.modules.business.service.impl;

import com.admin.modules.business.mapper.LogMapper;
import com.admin.modules.business.model.LogModel;
import com.admin.modules.business.service.ILogService;
import com.admin.modules.business.service.dto.LogDto;
import com.admin.modules.business.service.dto.criteria.LogQueryCriteria;
import com.admin.utils.DozerUtils;
import com.admin.utils.FileUtil;
import com.admin.utils.PageUtil;
import com.admin.utils.ValidationUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.dozer.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
* @description 服务实现
* @author sunny
* @date 2022-07-04
**/
@Service
@RequiredArgsConstructor
public class LogServiceImpl extends ServiceImpl<LogMapper, LogModel> implements ILogService {

    // dozer做Model与DTO的转换
    private final Mapper mapper;

    @Override
    public Map< String, Object> queryAll(LogQueryCriteria criteria, Page< LogModel> pageable){
        Page< LogModel> page = this.page(pageable, buildWrapper(criteria));
        List< LogDto> dtoList = DozerUtils.mapList(mapper, page.getRecords(), LogDto.class);
        return PageUtil.toPage(dtoList, page.getTotal());
    }

    private QueryWrapper< LogModel> buildWrapper( LogQueryCriteria criteria) {
        QueryWrapper< LogModel> query = null;
        if (null != criteria) {
            query = new QueryWrapper< >();
        }
        return query;
    }

    @Override
    public List< LogDto> queryAll(LogQueryCriteria criteria){
        List< LogModel> logs = this.list(buildWrapper(criteria));
        return DozerUtils.mapList(mapper, logs, LogDto.class);
    }

    @Override
    @Transactional
    public LogDto findById(Long id) {
        LogModel log = Optional.ofNullable(this.getById(id)).orElseGet(LogModel::new);
        ValidationUtil.isNull(log.getId(),"Log","id",id);
        return mapper.map(log, LogDto.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(LogModel resources) {
        return this.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(LogModel resources) {
        LogModel log = Optional.ofNullable(this.getById(resources.getId())).orElseGet(LogModel::new);
        ValidationUtil.isNull(log.getId(), "Log", "id", resources.getId());
        log = mapper.map(resources, LogModel.class);
        this.saveOrUpdate(log);
    }

    @Override
    public void deleteAll(Long[ ] ids) {
        this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public void download(List< LogDto> all, HttpServletResponse response) throws IOException {
        List< Map< String, Object>> list = new ArrayList<>();
        for (LogDto log : all) {
            Map< String,Object> map = new LinkedHashMap<>();
            map.put("操作内容", log.getContent());
            map.put("操作时间", log.getTime());
            map.put("操作人", log.getUser());
            map.put("ip", log.getIp());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}

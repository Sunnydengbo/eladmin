
package com.admin.modules.business.service;

import com.admin.modules.business.model.LogModel;
import com.admin.modules.business.service.dto.LogDto;
import com.admin.modules.business.service.dto.criteria.LogQueryCriteria;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
* @description 服务接口
* @author sunny
* @date 2022-07-04
**/
public interface ILogService extends IService< LogModel>{

    /**
    * 查询数据分页
    * @param criteria 条件
    * @param pageable 分页参数
    */
    Map< String,Object> queryAll(LogQueryCriteria criteria, Page<LogModel> pageable);

    /**
    * 查询所有数据不分页
    * @param criteria 条件参数
    * @return List< LogDto>
    */
    List< LogDto> queryAll(LogQueryCriteria criteria);

    /**
     * 根据ID查询
     * @param id ID
     * @return LogDto
     */
    LogDto findById(Long id);

    /**
    * 创建
    * @param resources /
    * @return LogDto
    */
    Boolean create(LogModel resources);

    /**
    * 编辑
    * @param resources /
    */
    void update(LogModel resources);

    /**
    * 多选删除
    * @param ids /
    */
    void deleteAll(Long[] ids);

    /**
    * 导出数据
    * @param all 待导出的数据
    * @param response /
    * @throws IOException /
    */
    void download(List< LogDto> all, HttpServletResponse response) throws IOException;

}

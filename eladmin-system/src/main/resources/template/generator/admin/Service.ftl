
package ${package}.service;

import ${package}.model.${className}Model;
import ${package}.service.dto.${className}Dto;
import ${package}.service.dto.criteria.${className}QueryCriteria;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Map;
import java.util.List;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/**
* @description 服务接口
* @author ${author}
* @date ${date}
**/
public interface I${className}Service extends IService< ${className}Model>{

    /**
    * 查询数据分页
    * @param criteria 条件
    * @param pageable 分页参数
    */
    Map< String,Object> queryAll(${className}QueryCriteria criteria, Page< ${className}Model> page);

    /**
    * 查询所有数据不分页
    * @param criteria 条件参数
    * @return List< ${className}Dto>
    */
    List< ${className}Dto> queryAll(${className}QueryCriteria criteria);

    /**
     * 根据ID查询
     * @param ${pkChangeColName} ID
     * @return ${className}Dto
     */
    ${className}Dto findById(${pkColumnType} ${pkChangeColName});

    /**
    * 创建
    * @param resources /
    * @return ${className}Dto
    */
    ${className}Dto create(${className}Model resources);

    /**
    * 编辑
    * @param resources /
    */
    void update(${className}Model resources);

    /**
    * 多选删除
    * @param ids /
    */
    void deleteAll(${pkColumnType}[] ids);

    /**
    * 导出数据
    * @param all 待导出的数据
    * @param response /
    * @throws IOException /
    */
    void download(List< ${className}Dto> all, HttpServletResponse response) throws IOException;

}

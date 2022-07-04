
package com.admin.modules.business.controller;

import com.admin.annotation.Log;
import com.admin.modules.business.model.LogModel;
import com.admin.modules.business.service.ILogService;
import com.admin.modules.business.service.dto.criteria.LogQueryCriteria;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.*;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/**
* @author sunny
* @date 2022-07-04
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "/api/log管理")
@RequestMapping("/api/log")
public class LogController {

    private final ILogService logService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('log:list')")
    public void download(HttpServletResponse response, LogQueryCriteria criteria) throws IOException {
        logService.download(logService.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询/api/log")
    @ApiOperation("查询/api/log")
    @PreAuthorize("@el.check('log:list')")
    public ResponseEntity<?> query(LogQueryCriteria criteria, Page< LogModel> page){
        return new ResponseEntity<>(logService.queryAll(criteria,page),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增/api/log")
    @ApiOperation("新增/api/log")
    @PreAuthorize("@el.check('log:add')")
    public ResponseEntity<?> create(@Validated @RequestBody LogModel resources){
        return new ResponseEntity<>(logService.create(resources),HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改/api/log")
    @ApiOperation("修改/api/log")
    @PreAuthorize("@el.check('log:edit')")
    public ResponseEntity<?> update(@Validated @RequestBody LogModel resources){
        logService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除/api/log")
    @ApiOperation("删除/api/log")
    @PreAuthorize("@el.check('log:del')")
    @DeleteMapping
    public ResponseEntity<?> delete(@RequestBody Long[] ids) {
        logService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

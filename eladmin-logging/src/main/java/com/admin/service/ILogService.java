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
package com.admin.service;

import com.admin.model.LogModel;
import com.admin.service.dto.LogQueryCriteria;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.scheduling.annotation.Async;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author adyfang
 * @date 2020年5月3日
 */
public interface ILogService extends IService<LogModel> {

    @Async
    void save(String username, String browser, String ip, ProceedingJoinPoint joinPoint, LogModel log);

    Object findByErrDetail(Long id);

    void download(List<LogModel> logs, HttpServletResponse response) throws IOException;

    void delAllByError();

    void delAllByInfo();

    List<LogModel> queryAll(LogQueryCriteria criteria);

    @SuppressWarnings("rawtypes")
    Object queryAll(LogQueryCriteria criteria, IPage pageable);

    @SuppressWarnings("rawtypes")
    Object queryAllByUser(LogQueryCriteria criteria, IPage pageable);

}

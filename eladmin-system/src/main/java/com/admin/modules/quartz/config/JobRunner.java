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
package com.admin.modules.quartz.config;

import com.admin.modules.quartz.model.QuartzJobModel;
import com.admin.modules.quartz.service.IQuartzJobService;
import com.admin.modules.quartz.utils.QuartzManage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author adyfang
 * @date 2020年5月5日
 */
@Component
public class JobRunner implements ApplicationRunner {

    @Autowired
    private IQuartzJobService quartzJobService;

    @Autowired
    private QuartzManage quartzManage;

    /**
     * 项目启动时重新激活启用的定时任务
     *
     * @param applicationArguments /
     */
    @Override
    public void run(ApplicationArguments applicationArguments) {
        System.out.println("--------------------注入定时任务---------------------");
        QueryWrapper<QuartzJobModel> query = new QueryWrapper<QuartzJobModel>();
        query.lambda().eq(QuartzJobModel::getIsPause, false);
        List<QuartzJobModel> quartzJobs = quartzJobService.list(query);
        quartzJobs.forEach(quartzManage::addJob);
        System.out.println("--------------------定时任务注入完成---------------------");
    }
}

package com.sunny;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.admin.AppRun;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.admin.modules.system.service.IDeptService;

import com.admin.modules.system.controller.dto.DeptDto;
import com.admin.modules.system.controller.dto.DeptQueryCriteria;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@SpringBootTest(classes = AppRun.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class EladminSystemApplicationTests {

    @Autowired
    private IDeptService deptService;

    @SuppressWarnings("deprecation")
	@Test
    public void contextLoads() throws Exception {
        System.out.println("***********************************************");
        DeptQueryCriteria criteria = new DeptQueryCriteria();
        criteria.setEnabled(true);
        criteria.setName("ff");
        criteria.setPid(null);
        List<Timestamp> createTimes = new ArrayList<>();
        createTimes.add(new Timestamp(new Date(2019 - 1900, 1, 1).getTime()));
        createTimes.add(new Timestamp(new Date(2020 - 1900, 1, 1).getTime() + 100000));
        criteria.setCreateTime(createTimes);
        List<DeptDto> list = deptService.queryAll(criteria, true);
        System.out.println(list);
        System.out.println("***********************************************");
    }

    public static void main(String[] args) {
    }
}

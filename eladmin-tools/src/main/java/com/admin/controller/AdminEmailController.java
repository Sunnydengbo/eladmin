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
package com.admin.controller;

import com.admin.annotation.Log;
import com.admin.model.EmailConfigModel;
import com.admin.model.vo.EmailVo;
import com.admin.service.IEmailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author adyfang
 * @date 2020年5月4日
 */
@RestController
@RequestMapping("api/email")
@Api(tags = "工具：邮件管理")
public class AdminEmailController {

	@Autowired
	private IEmailService emailService;

	@GetMapping
	public ResponseEntity<Object> get() {
		return new ResponseEntity<>(emailService.find(), HttpStatus.OK);
	}

	@Log("配置邮件")
	@PutMapping
	@ApiOperation("配置邮件")
	public ResponseEntity<Object> emailConfig(@Validated @RequestBody EmailConfigModel emailConfig) throws Exception {
		emailService.config(emailConfig, emailService.find());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Log("发送邮件")
	@PostMapping
	@ApiOperation("发送邮件")
	public ResponseEntity<Object> send(@Validated @RequestBody EmailVo emailVo) {
		emailService.send(emailVo, emailService.find());
		return new ResponseEntity<>(HttpStatus.OK);
	}
}

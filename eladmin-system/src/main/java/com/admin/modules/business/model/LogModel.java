
package com.admin.modules.business.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.annotations.ApiModelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.*;
import java.io.Serializable;

/**
* @description t_log
* @author sunny
* @date 2022-07-04
**/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonIgnoreProperties(value = { "handler" })
@TableName("t_log")
public class LogModel extends Model< LogModel> implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @TableField(value = "id")
    @ApiModelProperty(value = "序号")
    private Long id;

    @TableField(value = "content")
    @ApiModelProperty(value = "操作内容")
    private String content;

    @TableField(value = "time")
    @ApiModelProperty(value = "操作时间")
    private String time;

    @TableField(value = "user")
    @ApiModelProperty(value = "操作人")
    private String user;

    @TableField(value = "ip")
    @ApiModelProperty(value = "ip")
    private String ip;

    @Override
    protected Serializable pkVal() {
        return id;
    }
}


package com.admin.modules.business.service.dto;

import lombok.Data;
import java.io.Serializable;

/**
* @description /
* @author sunny
* @date 2022-07-04
**/
@Data
public class LogDto implements Serializable {

    /** 序号 */
    private Long id;

    /** 操作内容 */
    private String content;

    /** 操作时间 */
    private String time;

    /** 操作人 */
    private String user;

    /** ip */
    private String ip;
}

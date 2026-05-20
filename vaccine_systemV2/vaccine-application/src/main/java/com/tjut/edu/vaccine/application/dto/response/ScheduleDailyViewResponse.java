package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

@Data
public class ScheduleDailyViewResponse {
    private Long doctorId;
    private String doctorName;
    private String roleName;
    private Long windowId;
    private String windowName;
    private String windowCode;
    /** 上午状态: 0=正常, 1=请假, 2=取消 */
    private int amStatus;
    /** 上午排班记录ID，null=默认无记录 */
    private Long amScheduleId;
    /** 下午状态: 0=正常, 1=请假, 2=取消 */
    private int pmStatus;
    /** 下午排班记录ID，null=默认无记录 */
    private Long pmScheduleId;
}

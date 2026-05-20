package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

@Data
public class WindowResponse {

    private Long id;
    private String windowCode;
    private String windowName;
    private String windowFunctionType;
    private Integer status;
    private Integer avgHandleTime;
    private Integer sortOrder;
    private Long doctorId;
    private String createTime;
    /** 当前值班医生 */
    private String doctorName;
}

package com.tjut.edu.vaccine.application.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class RoleResponse {

    private Long id;
    private String roleCode;
    private String roleName;
    private String roleGroup;
    private String description;
    private String status;
    private Boolean isSystem;
    private String createTime;
    private List<String> permissions;
    private Integer permissionCount;
    private Integer userCount;
}

package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoleUpdateRequest {

    @Size(max = 50, message = "角色名称最多50个字符")
    private String roleName;

    @Size(max = 50, message = "角色分组最多50个字符")
    private String roleGroup;

    @Size(max = 200, message = "描述最多200个字符")
    private String description;

    private Integer status;

    private List<String> permissions;
}

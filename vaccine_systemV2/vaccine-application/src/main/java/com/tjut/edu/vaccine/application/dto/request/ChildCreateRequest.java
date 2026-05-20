package com.tjut.edu.vaccine.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ChildCreateRequest {

    @NotBlank(message = "儿童姓名不能为空")
    @Size(max = 50, message = "儿童姓名最多50个字符")
    private String name;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的日期")
    private LocalDate birthDate;

    private Integer idCardType;

    @Size(max = 18, message = "证件号码最多18个字符")
    private String idCardNo;

    @Size(max = 100, message = "籍贯最多100个字符")
    private String nativePlace;

    @Size(max = 50, message = "民族最多50个字符")
    private String nation;

    @Size(max = 500, message = "既往病史最多500个字符")
    private String medicalHistory;

    @Size(max = 500, message = "过敏史最多500个字符")
    private String allergyHistory;
}

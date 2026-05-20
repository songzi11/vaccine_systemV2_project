package com.tjut.edu.vaccine.domain.vaccinate.aggregate;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 接种记录聚合根
 */
@Getter
@Setter
public class VaccinationRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long appointmentId;
    private String injectionId;
    private LocalDateTime injectionTime;
    private Long doctorId;
    private String injectionSite;
    private Long batchId;
    private String batchNo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public VaccinationRecord() {
    }

    private VaccinationRecord(Long appointmentId, Long doctorId, String injectionSite,
                              Long batchId, String batchNo, String injectionId) {
        if (appointmentId == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }
        if (injectionSite == null || injectionSite.isBlank()) {
            throw new IllegalArgumentException("接种部位不能为空");
        }
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.injectionSite = injectionSite;
        this.batchId = batchId;
        this.batchNo = batchNo;
        this.injectionId = injectionId;
        this.injectionTime = LocalDateTime.now();
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 创建接种记录工厂方法
     */
    public static VaccinationRecord create(Long appointmentId, Long doctorId,
                                           String injectionSite, Long batchId, String batchNo) {
        String injectionId = generateInjectionId(LocalDate.now());
        return new VaccinationRecord(appointmentId, doctorId, injectionSite, batchId, batchNo, injectionId);
    }

    /**
     * 生成接种编号 INJ+yyyyMMdd+4位序号
     */
    private static String generateInjectionId(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 4位序号由仓储层填充，此处使用占位符
        return "INJ" + dateStr + "0001";
    }

    /**
     * 设置接种编号（由仓储层调用）
     */
    public void setInjectionId(String injectionId) {
        this.injectionId = injectionId;
    }
}

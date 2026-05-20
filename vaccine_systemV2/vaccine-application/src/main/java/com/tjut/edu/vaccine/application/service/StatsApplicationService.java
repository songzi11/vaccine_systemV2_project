package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.common.enums.AppointmentStatus;
import com.tjut.edu.vaccine.domain.appointment.aggregate.Appointment;
import com.tjut.edu.vaccine.domain.appointment.repository.AppointmentRepository;
import com.tjut.edu.vaccine.domain.identity.aggregate.User;
import com.tjut.edu.vaccine.domain.identity.repository.UserRepository;
import com.tjut.edu.vaccine.domain.stock.aggregate.VaccineBatch;
import com.tjut.edu.vaccine.domain.stock.entity.HospitalVaccineStock;
import com.tjut.edu.vaccine.domain.stock.entity.StockAlertLog;
import com.tjut.edu.vaccine.domain.stock.repository.StockAlertLogRepository;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineBatchRepository;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineRepository;
import com.tjut.edu.vaccine.domain.observe.repository.AdverseReactionRepository;
import com.tjut.edu.vaccine.domain.stock.repository.VaccineStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsApplicationService {

    private final AppointmentRepository appointmentRepository;
    private final VaccineRepository vaccineRepository;
    private final VaccineStockRepository vaccineStockRepository;
    private final VaccineBatchRepository vaccineBatchRepository;
    private final StockAlertLogRepository stockAlertLogRepository;
    private final UserRepository userRepository;
    private final AdverseReactionRepository adverseReactionRepository;

    /**
     * 接种统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getVaccinationStats() {
        Map<String, Object> stats = new HashMap<>();

        LocalDate today = LocalDate.now();

        // 今日预约数
        List<Appointment> todayAppointments =
                appointmentRepository.findByDate(today);
        stats.put("todayAppointments", todayAppointments.size());

        // 今日已完成接种（状态=COMPLETED=2）
        List<Appointment> completedToday =
                appointmentRepository.findByStatuses(List.of(AppointmentStatus.COMPLETED.getCode()), today);
        stats.put("todayCompleted", completedToday.size());

        // 疫苗总数
        long vaccineCount = vaccineRepository.count(null, null);
        stats.put("totalVaccines", vaccineCount);

        // 总库存批次
        List<VaccineBatch> allBatches =
                vaccineBatchRepository.findAllNormal();
        stats.put("totalBatches", allBatches.size());

        // 总用户数
        List<User> allUsers = userRepository.findAll();
        stats.put("totalUsers", allUsers.size());

        // 不良反应总数
        stats.put("totalAdverseReactions", adverseReactionRepository.count());

        stats.put("date", today.toString());

        return stats;
    }

    /**
     * 库存统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStockStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总疫苗种类
        long vaccineCount = vaccineRepository.count(null, null);
        stats.put("totalVaccineTypes", vaccineCount);

        // 总批次数
        List<VaccineBatch> allBatches =
                vaccineBatchRepository.findAllNormal();
        stats.put("totalBatches", allBatches.size());

        // 有库存的批次
        List<HospitalVaccineStock> stockWithStock =
                vaccineStockRepository.findAllWithStock();
        stats.put("batchesWithStock", stockWithStock.size());
        int totalStock = stockWithStock.stream().mapToInt(HospitalVaccineStock::getTotalStock).sum();
        int availableStock = stockWithStock.stream().mapToInt(HospitalVaccineStock::getAvailableStock).sum();
        int lockedStock = stockWithStock.stream().mapToInt(HospitalVaccineStock::getLockedStock).sum();
        int usedStock = Math.max(0, totalStock - availableStock - lockedStock);
        stats.put("totalStock", totalStock);
        stats.put("availableStock", availableStock);
        stats.put("lockedStock", lockedStock);
        stats.put("usageRate", totalStock > 0 ? String.format("%.1f%%", usedStock * 100.0 / totalStock) : "0%");

        // 库存预警（未处理）
        List<StockAlertLog> unhandledAlerts =
                stockAlertLogRepository.findUnhandled(1, 100);
        stats.put("unhandledAlerts", unhandledAlerts.size());

        // 即将过期批次
        LocalDate warningDate = LocalDate.now().plusDays(30);
        List<VaccineBatch> nearExpiry =
                vaccineBatchRepository.findNearExpiry(warningDate);
        stats.put("nearExpiryBatches", nearExpiry.size());

        // 已过期批次
        List<VaccineBatch> expired =
                vaccineBatchRepository.findExpired(LocalDate.now());
        stats.put("expiredBatches", expired.size());

        return stats;
    }

    /**
     * 效率统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEfficiencyStats() {
        Map<String, Object> stats = new HashMap<>();

        LocalDate today = LocalDate.now();

        // 今日预约数
        List<Appointment> todayAppointments =
                appointmentRepository.findByDate(today);
        stats.put("todayAppointments", todayAppointments.size());

        // 今日完成数（COMPLETED=2）
        List<Appointment> completedToday =
                appointmentRepository.findByStatuses(List.of(AppointmentStatus.COMPLETED.getCode()), today);
        stats.put("todayCompleted", completedToday.size());

        // 完成率
        double completionRate = todayAppointments.isEmpty() ? 0.0 :
                (double) completedToday.size() / todayAppointments.size() * 100;
        stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);

        // 各状态分布
        Map<String, Integer> statusDistribution = new HashMap<>();
        statusDistribution.put("appointed", 0);
        statusDistribution.put("completed", 0);
        statusDistribution.put("cancelled", 0);
        statusDistribution.put("expired", 0);
        statusDistribution.put("signedIn", 0);
        statusDistribution.put("precheckPass", 0);
        statusDistribution.put("registered", 0);
        statusDistribution.put("precheckFail", 0);
        statusDistribution.put("observing", 0);

        for (Appointment appointment : todayAppointments) {
            int statusCode = appointment.getStatus();
            switch (statusCode) {
                case 1 -> statusDistribution.merge("appointed", 1, Integer::sum);
                case 2 -> statusDistribution.merge("completed", 1, Integer::sum);
                case 3 -> statusDistribution.merge("cancelled", 1, Integer::sum);
                case 4 -> statusDistribution.merge("expired", 1, Integer::sum);
                case 6 -> statusDistribution.merge("signedIn", 1, Integer::sum);
                case 7 -> statusDistribution.merge("precheckPass", 1, Integer::sum);
                case 8 -> statusDistribution.merge("registered", 1, Integer::sum);
                case 9 -> statusDistribution.merge("precheckFail", 1, Integer::sum);
                case 10 -> statusDistribution.merge("observing", 1, Integer::sum);
            }
        }
        stats.put("statusDistribution", statusDistribution);

        return stats;
    }

    /**
     * 异常统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAnomalyStats() {
        Map<String, Object> stats = new HashMap<>();

        // 库存预警数
        List<StockAlertLog> unhandledAlerts =
                stockAlertLogRepository.findUnhandled(1, 100);
        stats.put("unhandledAlerts", unhandledAlerts.size());

        // 即将过期批次
        LocalDate warningDate = LocalDate.now().plusDays(30);
        List<VaccineBatch> nearExpiry =
                vaccineBatchRepository.findNearExpiry(warningDate);
        stats.put("nearExpiryBatches", nearExpiry.size());

        // 已过期批次
        List<VaccineBatch> expired =
                vaccineBatchRepository.findExpired(LocalDate.now());
        stats.put("expiredBatches", expired.size());

        // 不良反应数
        long adverseCount = adverseReactionRepository.count();
        stats.put("adverseReactionCount", adverseCount);

        // 总异常数
        int totalAnomalies = unhandledAlerts.size() + nearExpiry.size() + expired.size() + (int) adverseCount;
        stats.put("totalAnomalies", totalAnomalies);

        return stats;
    }
}

-- 诊断查询：检查业务冲突的冗余数据

-- 1. 拥有多个 DOCTOR_* 角色的用户（按设计每个医生应只有一个流程角色）
SELECT u.id, u.real_name, GROUP_CONCAT(r.role_code ORDER BY r.role_code) AS roles
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
JOIN sys_role r ON ur.role_id = r.id
WHERE r.role_code LIKE 'DOCTOR_%'
  AND r.role_code != 'DOCTOR_STOCK'
GROUP BY u.id, u.real_name
HAVING COUNT(*) > 1;

-- 2. 窗口指向的医生没有对应角色（窗口-角色不匹配）
SELECT hw.id AS window_id, hw.window_name, hw.window_function_type,
       hw.doctor_id, u.real_name AS doctor_name,
       GROUP_CONCAT(r.role_code) AS actual_roles
FROM hospital_window hw
JOIN sys_user u ON hw.doctor_id = u.id
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
WHERE hw.doctor_id IS NOT NULL
GROUP BY hw.id, hw.window_name, hw.window_function_type, hw.doctor_id, u.real_name;

-- 3. 有医生角色但未分配到任何窗口的"悬浮"医生
SELECT u.id, u.real_name, r.role_code
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
JOIN sys_role r ON ur.role_id = r.id
WHERE r.role_code IN ('DOCTOR_SIGNIN','DOCTOR_PRECHECK','DOCTOR_REGISTER','DOCTOR_VACCINATE','DOCTOR_OBSERVE')
  AND NOT EXISTS (
    SELECT 1 FROM hospital_window hw WHERE hw.doctor_id = u.id
  );

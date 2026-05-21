-- V6: Ensure doctors assigned to windows have the correct roles
-- Root cause: Flyway baseline-version=2 causes V2 seed data to be skipped on new databases,
-- leaving sys_user_role empty even though hospital_window has doctor assignments.

-- PRECHECK window → role_id=3 (DOCTOR_PRECHECK)
INSERT IGNORE INTO sys_user_role (user_id, role_id, create_time)
SELECT hw.doctor_id, 3, NOW()
FROM hospital_window hw
WHERE hw.doctor_id IS NOT NULL
  AND hw.window_function_type = 'PRECHECK';

-- SIGNIN window → role_id=2 (DOCTOR_SIGNIN)
INSERT IGNORE INTO sys_user_role (user_id, role_id, create_time)
SELECT hw.doctor_id, 2, NOW()
FROM hospital_window hw
WHERE hw.doctor_id IS NOT NULL
  AND hw.window_function_type = 'SIGNIN';

-- REGISTER window → role_id=4 (DOCTOR_REGISTER)
INSERT IGNORE INTO sys_user_role (user_id, role_id, create_time)
SELECT hw.doctor_id, 4, NOW()
FROM hospital_window hw
WHERE hw.doctor_id IS NOT NULL
  AND hw.window_function_type = 'REGISTER';

-- VACCINATE window → role_id=5 (DOCTOR_VACCINATE)
INSERT IGNORE INTO sys_user_role (user_id, role_id, create_time)
SELECT hw.doctor_id, 5, NOW()
FROM hospital_window hw
WHERE hw.doctor_id IS NOT NULL
  AND hw.window_function_type = 'VACCINATE';

-- OBSERVE window → role_id=6 (DOCTOR_OBSERVE)
INSERT IGNORE INTO sys_user_role (user_id, role_id, create_time)
SELECT hw.doctor_id, 6, NOW()
FROM hospital_window hw
WHERE hw.doctor_id IS NOT NULL
  AND hw.window_function_type = 'OBSERVE';

"""错误码/权限码一致性检查器测试。"""
import unittest
from verify_consistency.models import (
    PrdDocument, ReqDocument, ErrorCode, PermissionCode, Severity,
)
from verify_consistency.checkers.error_permission import ErrorPermissionChecker


class TestErrorCodeChecker(unittest.TestCase):
    def setUp(self):
        self.checker = ErrorPermissionChecker()
        self.global_doc = ReqDocument(filename="REQ-GLOBAL", module="GLOBAL", is_global=True)
        self.global_doc.error_code_segments = {"1000-1999": "系统级", "2000-2999": "预约模块", "7000-7999": "用户模块"}
        self.global_doc.error_codes = [
            ErrorCode(id="", code=1001, message="未认证", source="REQ-GLOBAL", module="GLOBAL"),
            ErrorCode(id="", code=2001, message="儿童不存在", source="REQ-GLOBAL", module="GLOBAL"),
            ErrorCode(id="", code=7001, message="用户不存在", source="REQ-GLOBAL", module="GLOBAL"),
            ErrorCode(id="", code=7006, message="短信失败", source="REQ-GLOBAL", module="GLOBAL"),
        ]

    def test_module_codes_within_segment(self):
        module = ReqDocument(filename="REQ-USER", module="USER")
        module.error_codes = [ErrorCode(id="", code=7001, message="", source="REQ-USER", module="USER")]
        result = self.checker.check_error_codes(self.global_doc, module)
        self.assertEqual(len(result.issues), 0)

    def test_module_codes_outside_global(self):
        module = ReqDocument(filename="REQ-USER", module="USER")
        module.error_codes = [ErrorCode(id="", code=7007, message="", source="REQ-USER", module="USER")]
        result = self.checker.check_error_codes(self.global_doc, module)
        self.assertEqual(len(result.issues), 1)

    def test_prd_req_error_mapping(self):
        prd = PrdDocument(filename="PRD-USER", module="USER")
        prd.error_codes = [
            ErrorCode(id="E-USER-001", code=7001, message="", source="PRD-USER", module="USER"),
            ErrorCode(id="E-USER-002", code=7002, message="", source="PRD-USER", module="USER"),
        ]
        req = ReqDocument(filename="REQ-USER", module="USER")
        req.error_codes = [ErrorCode(id="", code=7001, message="", source="REQ-USER", module="USER")]
        result = self.checker.check_prd_req_error_mapping(prd, req)
        self.assertEqual(len(result.issues), 1)


class TestPermissionChecker(unittest.TestCase):
    def setUp(self):
        self.checker = ErrorPermissionChecker()
        self.global_doc = ReqDocument(filename="REQ-GLOBAL", module="GLOBAL", is_global=True)
        self.global_doc.permission_codes = [
            PermissionCode(code="appointment.book", name="预约接种", source="REQ-GLOBAL", module="GLOBAL"),
            PermissionCode(code="appointment.view.own", name="查看预约", source="REQ-GLOBAL", module="GLOBAL"),
        ]

    def test_prd_perm_in_global(self):
        prd = PrdDocument(filename="PRD-TEST", module="TEST")
        prd.permission_codes = [PermissionCode(code="appointment.book", name="预约接种", source="PRD-TEST", module="TEST")]
        result = self.checker.check_permissions(prd, self.global_doc)
        self.assertEqual(len(result.issues), 0)

    def test_prd_perm_not_in_global(self):
        prd = PrdDocument(filename="PRD-TEST", module="TEST")
        prd.permission_codes = [PermissionCode(code="magic.spell", name="魔法", source="PRD-TEST", module="TEST")]
        result = self.checker.check_permissions(prd, self.global_doc)
        self.assertEqual(len(result.issues), 1)


if __name__ == "__main__":
    unittest.main()

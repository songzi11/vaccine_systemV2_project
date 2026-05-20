"""状态机一致性检查器测试。"""
import unittest
from verify_consistency.models import ReqDocument, StatusDefinition, StatusTransition
from verify_consistency.checkers.state_machine import StateMachineChecker


class TestStateMachineChecker(unittest.TestCase):
    def setUp(self):
        self.checker = StateMachineChecker()
        self.global_doc = ReqDocument(filename="REQ-GLOBAL", module="GLOBAL", is_global=True)
        self.global_doc.status_definitions = [
            StatusDefinition(code=1, name="已预约", constant="APPOINTED", category="正常", window="-"),
            StatusDefinition(code=6, name="已签到", constant="SIGNED_IN", category="正常", window="SIGNIN"),
            StatusDefinition(code=7, name="预检通过", constant="PRECHECK_PASS", category="正常", window="PRECHECK"),
            StatusDefinition(code=2, name="已完成", constant="COMPLETED", category="正常", window="-"),
            StatusDefinition(code=3, name="已取消", constant="CANCELLED", category="异常", window="-"),
        ]
        self.global_doc.status_transitions = [
            StatusTransition(from_status=1, to_status=6, operation="签到", role="DOCTOR_SIGNIN", module="PRD-FLOW"),
            StatusTransition(from_status=6, to_status=7, operation="预检", role="DOCTOR_PRECHECK", module="PRD-FLOW"),
            StatusTransition(from_status=1, to_status=3, operation="取消", role="USER", module="PRD-APPOINTMENT"),
        ]

    def test_valid_status_codes(self):
        module = ReqDocument(filename="REQ-TEST", module="TEST")
        module.referenced_status_codes = [1, 3, 6]
        result = self.checker.check(self.global_doc, module)
        self.assertEqual(len(result.issues), 0)

    def test_invalid_status_code(self):
        module = ReqDocument(filename="REQ-TEST", module="TEST")
        module.referenced_status_codes = [1, 99]
        result = self.checker.check(self.global_doc, module)
        self.assertEqual(len(result.issues), 1)
        self.assertIn("99", result.issues[0].description)

    def test_no_global_doc(self):
        result = self.checker.run([ReqDocument(filename="REQ-TEST", module="TEST")])
        self.assertTrue(any("未找到" in line for line in result.summary_lines))


if __name__ == "__main__":
    unittest.main()

"""API 接口一致性检查器测试。"""
import unittest
from verify_consistency.models import PrdDocument, ReqDocument, ApiEndpoint
from verify_consistency.checkers.api_interface import ApiInterfaceChecker


class TestApiInterfaceChecker(unittest.TestCase):
    def setUp(self):
        self.checker = ApiInterfaceChecker()
        self.prd = PrdDocument(filename="PRD-TEST", module="TEST")
        self.prd.api_endpoints = [
            ApiEndpoint(path="/api/test/create", method="POST", feature_id="F-TEST-001", source="PRD-TEST", module="TEST"),
            ApiEndpoint(path="/api/test/{id}", method="GET", feature_id="F-TEST-002", source="PRD-TEST", module="TEST"),
            ApiEndpoint(path="/api/test/{id}/extra", method="GET", feature_id="F-TEST-003", source="PRD-TEST", module="TEST"),
        ]
        self.req = ReqDocument(filename="REQ-TEST", module="TEST")
        self.req.api_endpoints = [
            ApiEndpoint(path="/api/test/create", method="POST", feature_id="F-TEST-001", source="REQ-TEST", module="TEST"),
            ApiEndpoint(path="/api/test/{id}", method="GET", feature_id="F-TEST-002", source="REQ-TEST", module="TEST"),
        ]

    def test_uncovered_api(self):
        result = self.checker.check(self.prd, self.req)
        uncovered = [i for i in result.issues if "未覆盖" in i.description]
        self.assertEqual(len(uncovered), 1)

    def test_method_mismatch(self):
        req2 = ReqDocument(filename="REQ-TEST", module="TEST")
        req2.api_endpoints = [ApiEndpoint(path="/api/test/create", method="PUT", feature_id="F-TEST-001", source="REQ-TEST", module="TEST")]
        result = self.checker.check(self.prd, req2)
        conflicts = [i for i in result.issues if "方法不一致" in i.description]
        self.assertEqual(len(conflicts), 1)

    def test_coverage_rate(self):
        result = self.checker.run([self.prd], [self.req])
        self.assertTrue(any("66%" in line for line in result.summary_lines))


if __name__ == "__main__":
    unittest.main()

"""功能点覆盖检查器测试。"""
import unittest
from verify_consistency.models import (
    PrdDocument, ReqDocument, Feature, MatchMethod,
)
from verify_consistency.checkers.feature_coverage import FeatureCoverageChecker


class TestFeatureCoverageChecker(unittest.TestCase):
    def setUp(self):
        self.checker = FeatureCoverageChecker()
        self.prd = PrdDocument(filename="PRD-TEST", module="TEST")
        self.prd.features = [
            Feature(id="F-TEST-001", name="创建测试", source="PRD-TEST", module="TEST"),
            Feature(id="F-TEST-002", name="查询测试", source="PRD-TEST", module="TEST"),
            Feature(id="F-TEST-003", name="删除测试", source="PRD-TEST", module="TEST"),
        ]
        self.req = ReqDocument(filename="REQ-TEST", module="TEST")
        self.req.features = [
            Feature(id="F-TEST-001", name="创建测试", source="REQ-TEST", module="TEST"),
            Feature(id="F-TEST-003", name="删除测试", source="REQ-TEST", module="TEST"),
        ]

    def test_exact_match(self):
        mappings = self.checker.check(self.prd, self.req)
        covered = [m for m in mappings if m.status == "一致"]
        self.assertEqual(len(covered), 2)

    def test_prd_uncovered(self):
        mappings = self.checker.check(self.prd, self.req)
        uncovered = [m for m in mappings if m.status == "PRD未覆盖"]
        self.assertEqual(len(uncovered), 1)
        self.assertEqual(uncovered[0].prd_feature.id, "F-TEST-002")

    def test_req_no_source(self):
        req2 = ReqDocument(filename="REQ-TEST", module="TEST")
        req2.features = [
            Feature(id="F-TEST-001", name="创建测试", source="REQ-TEST", module="TEST"),
            Feature(id="F-TEST-999", name="多余功能", source="REQ-TEST", module="TEST"),
        ]
        mappings = self.checker.check(self.prd, req2)
        no_source = [m for m in mappings if m.status == "REQ无来源"]
        self.assertEqual(len(no_source), 1)

    def test_semantic_match(self):
        prd = PrdDocument(filename="PRD-ADMIN", module="ADMIN")
        prd.features = [Feature(id="F-ADMIN-001", name="用户管理", source="PRD-ADMIN", module="ADMIN")]
        req = ReqDocument(filename="REQ-ADMIN", module="ADMIN")
        req.features = [Feature(id="F-ADMIN-101", name="用户管理", source="REQ-ADMIN", module="ADMIN")]
        mappings = self.checker.check(prd, req)
        covered = [m for m in mappings if m.status == "一致"]
        self.assertEqual(len(covered), 1)
        self.assertEqual(covered[0].match_method, MatchMethod.SEMANTIC)

    def test_coverage_rate(self):
        result = self.checker.run([self.prd], [self.req])
        self.assertTrue(any("66%" in line for line in result.summary_lines))


if __name__ == "__main__":
    unittest.main()

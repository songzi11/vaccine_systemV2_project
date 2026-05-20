"""报告生成器测试。"""
import unittest
from verify_consistency.models import CheckResult, ConsistencyIssue, Severity
from verify_consistency.report import ReportGenerator


class TestReportGenerator(unittest.TestCase):
    def setUp(self):
        self.gen = ReportGenerator()
        self.result = CheckResult(
            dimension="1", dimension_name="功能点覆盖完整性",
            summary_lines=["覆盖率: 50%", "问题: 2 个"],
            issues=[
                ConsistencyIssue(dimension="1", severity=Severity.HIGH, module="TEST", description="F-TEST-002 未覆盖", location="PRD-TEST", suggestion="补充"),
                ConsistencyIssue(dimension="1", severity=Severity.LOW, module="TEST", description="F-TEST-999 无来源", location="REQ-TEST", suggestion="确认"),
            ],
        )

    def test_includes_summary(self):
        output = self.gen.generate([self.result], "2026-04-02")
        self.assertIn("功能点覆盖完整性", output)
        self.assertIn("50%", output)

    def test_includes_issues(self):
        output = self.gen.generate([self.result], "2026-04-02")
        self.assertIn("F-TEST-002 未覆盖", output)
        self.assertIn("F-TEST-999 无来源", output)

    def test_markdown_structure(self):
        output = self.gen.generate([self.result], "2026-04-02")
        self.assertIn("# PRD→REQ", output)
        self.assertIn("## 摘要", output)


if __name__ == "__main__":
    unittest.main()

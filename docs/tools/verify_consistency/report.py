"""Markdown 一致性报告生成器。"""
from datetime import datetime
from typing import List
from .models import CheckResult, ConsistencyIssue, Severity


class ReportGenerator:
    def generate(self, results: List[CheckResult], run_date: str = "") -> str:
        if not run_date:
            run_date = datetime.now().strftime("%Y-%m-%d %H:%M")
        lines = [
            "# PRD→REQ 一致性验证报告", "",
            f"> 生成时间: {run_date}", "",
            "## 摘要", "",
            "| 维度 | 结果 | 问题数 |",
            "|------|------|--------|",
        ]
        for r in results:
            issue_count = len(r.issues)
            high_count = sum(1 for i in r.issues if i.severity == Severity.HIGH)
            status = "通过" if issue_count == 0 else f"不通过 ({high_count} 高)"
            lines.append(f"| {r.dimension_name} | {status} | {issue_count} |")
        lines.append("")
        for r in results:
            lines.append(f"## {r.dimension}. {r.dimension_name}")
            lines.append("")
            if r.summary_lines:
                for sl in r.summary_lines:
                    lines.append(f"- {sl}")
                lines.append("")
            if r.issues:
                severity_order = {Severity.HIGH: 0, Severity.MEDIUM: 1, Severity.LOW: 2}
                sorted_issues = sorted(r.issues, key=lambda i: severity_order.get(i.severity, 99))
                lines.append("| 严重度 | 模块 | 问题描述 | 位置 | 修复建议 |")
                lines.append("|--------|------|----------|------|----------|")
                for issue in sorted_issues:
                    lines.append(f"| {issue.severity.value} | {issue.module} | {issue.description} | {issue.location} | {issue.suggestion} |")
                lines.append("")
            else:
                lines.append("无问题。")
                lines.append("")
        lines.append("---")
        lines.append("*报告由 verify_consistency.py 自动生成*")
        lines.append("")
        return "\n".join(lines)

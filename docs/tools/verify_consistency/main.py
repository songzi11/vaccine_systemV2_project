"""PRD→REQ 一致性验证脚本入口。

用法:
    python -m verify_consistency.main --prd-dir ../../02-PRD --req-dir ../../03-REQ --output ../../03-REQ/PRD-REQ-CONSISTENCY-REPORT.md
"""

import argparse
import sys
from pathlib import Path

from .prd_parser import PrdParser
from .req_parser import ReqParser
from .checkers.feature_coverage import FeatureCoverageChecker
from .checkers.state_machine import StateMachineChecker
from .checkers.error_permission import ErrorPermissionChecker
from .checkers.api_interface import ApiInterfaceChecker
from .checkers.data_model import DataModelChecker
from .report import ReportGenerator
from datetime import datetime


def main():
    parser = argparse.ArgumentParser(description="PRD→REQ 一致性验证工具")
    parser.add_argument("--prd-dir", required=True, type=Path, help="PRD 文档目录路径")
    parser.add_argument("--req-dir", required=True, type=Path, help="REQ 文档目录路径")
    parser.add_argument("--output", required=True, type=Path, help="输出报告文件路径")
    args = parser.parse_args()

    prd_dir = args.prd_dir
    req_dir = args.req_dir
    output = args.output

    if not prd_dir.is_dir():
        print(f"错误: PRD 目录不存在: {prd_dir}", file=sys.stderr)
        sys.exit(1)
    if not req_dir.is_dir():
        print(f"错误: REQ 目录不存在: {req_dir}", file=sys.stderr)
        sys.exit(1)

    prd_parser = PrdParser()
    req_parser = ReqParser()

    prd_docs = []
    for f in sorted(prd_dir.glob("PRD-*.md")):
        print(f"解析 PRD: {f.name}")
        prd_docs.append(prd_parser.parse(f))

    req_docs = []
    for f in sorted(req_dir.glob("REQ-*.md")):
        print(f"解析 REQ: {f.name}")
        req_docs.append(req_parser.parse(f))

    print(f"\n解析完成: {len(prd_docs)} 个 PRD, {len(req_docs)} 个 REQ")

    print("\n执行一致性检查...")
    results = []

    checkers = [
        ("功能点覆盖", FeatureCoverageChecker().run(prd_docs, req_docs)),
        ("状态机一致性", StateMachineChecker().run(req_docs)),
        ("错误码/权限码", ErrorPermissionChecker().run(prd_docs, req_docs)),
        ("API 接口一致性", ApiInterfaceChecker().run(prd_docs, req_docs)),
        ("数据模型一致性", DataModelChecker().run(prd_docs, req_docs)),
    ]

    for name, result in checkers:
        print(f"  {name}: {len(result.issues)} 个问题")
        results.append(result)

    report_gen = ReportGenerator()
    report = report_gen.generate(results, datetime.now().strftime("%Y-%m-%d %H:%M"))

    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(report, encoding="utf-8")
    print(f"\n报告已生成: {output}")

    total_high = sum(1 for r in results for i in r.issues if i.severity.value == "HIGH")
    if total_high > 0:
        print(f"发现 {total_high} 个高严重度问题")
        sys.exit(1)


if __name__ == "__main__":
    main()

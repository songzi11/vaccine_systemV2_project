"""维度5: 数据模型一致性检查。"""
from typing import List, Dict, Set
from ..models import PrdDocument, ReqDocument, TableField, ConsistencyIssue, Severity, CheckResult


class DataModelChecker:
    def check(self, prd: PrdDocument, req: ReqDocument) -> CheckResult:
        result = CheckResult(dimension="5", dimension_name="数据模型一致性")
        prd_tables = set(tf.table for tf in prd.table_fields if tf.field != "*")
        req_tables = set(tf.table for tf in req.table_fields)
        for table in sorted(req_tables - prd_tables):
            result.issues.append(ConsistencyIssue(
                dimension="5", severity=Severity.MEDIUM,
                module=prd.module,
                description=f"REQ 引用的表 {table} 在 PRD 字段定义中未定义",
                location=req.filename,
                suggestion=f"在 {prd.filename} §6 中补充表 {table} 的字段定义",
            ))
        for table in sorted(prd_tables - req_tables):
            result.issues.append(ConsistencyIssue(
                dimension="5", severity=Severity.LOW,
                module=prd.module,
                description=f"PRD 定义的表 {table} 在 REQ 中未被引用",
                location=prd.filename,
                suggestion=f"确认表 {table} 是否需要在此模块的 REQ 中使用",
            ))
        return result

    def run(self, prd_docs: List[PrdDocument], req_docs: List[ReqDocument]) -> CheckResult:
        result = CheckResult(dimension="5", dimension_name="数据模型一致性")
        prd_by_module = {d.module: d for d in prd_docs}
        req_by_module = {d.module: d for d in req_docs if not d.is_global}
        for module, prd in prd_by_module.items():
            req = req_by_module.get(module)
            if req:
                r = self.check(prd, req)
                result.issues.extend(r.issues)
        result.summary_lines = [f"数据模型问题: {len(result.issues)} 个"]
        return result

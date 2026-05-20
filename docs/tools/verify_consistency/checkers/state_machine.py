"""维度2: 状态机一致性检查。"""
from typing import List, Set
from ..models import ReqDocument, ConsistencyIssue, Severity, CheckResult


class StateMachineChecker:
    def check(self, global_doc: ReqDocument, module_doc: ReqDocument) -> CheckResult:
        result = CheckResult(dimension="2", dimension_name="状态机一致性")
        global_codes = {s.code for s in global_doc.status_definitions}
        module_codes = set(module_doc.referenced_status_codes)
        invalid = module_codes - global_codes
        for code in sorted(invalid):
            result.issues.append(ConsistencyIssue(
                dimension="2", severity=Severity.HIGH,
                module=module_doc.module,
                description=f"模块引用了 GLOBAL 中不存在的状态码: {code}",
                location=module_doc.filename,
                suggestion=f"检查状态码 {code} 是否应为 GLOBAL 中已定义的值",
            ))
        return result

    def run(self, req_docs: List[ReqDocument]) -> CheckResult:
        result = CheckResult(dimension="2", dimension_name="状态机一致性")
        global_doc = next((d for d in req_docs if d.is_global), None)
        if not global_doc:
            result.summary_lines.append("未找到 REQ-GLOBAL 文档，跳过状态机检查")
            return result
        global_codes = {s.code for s in global_doc.status_definitions}
        result.summary_lines.append(f"GLOBAL 定义状态码: {sorted(global_codes)}")
        module_docs = [d for d in req_docs if not d.is_global]
        for mod in module_docs:
            mod_result = self.check(global_doc, mod)
            result.issues.extend(mod_result.issues)
        if not result.issues:
            result.summary_lines.append("所有模块状态码引用合规")
        else:
            result.summary_lines.append(f"发现 {len(result.issues)} 个状态码问题")
        return result

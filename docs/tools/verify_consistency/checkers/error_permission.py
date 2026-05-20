"""维度3: 错误码/权限码一致性检查。"""
from typing import List, Dict
from ..models import (
    PrdDocument, ReqDocument, ErrorCode, PermissionCode,
    ConsistencyIssue, Severity, CheckResult,
)


class ErrorPermissionChecker:
    def check_error_codes(self, global_doc: ReqDocument, module_doc: ReqDocument) -> CheckResult:
        result = CheckResult(dimension="3", dimension_name="错误码/权限码一致性")
        global_codes = {e.code for e in global_doc.error_codes}
        module_codes = {e.code for e in module_doc.error_codes}
        outside = module_codes - global_codes
        for code in sorted(outside):
            result.issues.append(ConsistencyIssue(
                dimension="3", severity=Severity.HIGH,
                module=module_doc.module,
                description=f"错误码 {code} 在 {module_doc.filename} 中使用但 REQ-GLOBAL 未定义",
                location=module_doc.filename,
                suggestion=f"在 REQ-GLOBAL 错误码定义中补充 {code}",
            ))
        return result

    def check_prd_req_error_mapping(self, prd: PrdDocument, req: ReqDocument) -> CheckResult:
        result = CheckResult(dimension="3", dimension_name="错误码/权限码一致性")
        prd_codes = {e.code for e in prd.error_codes}
        req_codes = {e.code for e in req.error_codes}
        for code in sorted(prd_codes - req_codes):
            result.issues.append(ConsistencyIssue(
                dimension="3", severity=Severity.MEDIUM,
                module=prd.module,
                description=f"PRD 错误码 {code} 在 REQ 中未定义",
                location=prd.filename,
                suggestion=f"在 {req.filename} 中补充错误码 {code} 的处理",
            ))
        return result

    def check_permissions(self, prd: PrdDocument, global_doc: ReqDocument) -> CheckResult:
        result = CheckResult(dimension="3", dimension_name="错误码/权限码一致性")
        global_perms = {p.code for p in global_doc.permission_codes}
        for perm in prd.permission_codes:
            if perm.code not in global_perms:
                result.issues.append(ConsistencyIssue(
                    dimension="3", severity=Severity.HIGH,
                    module=prd.module,
                    description=f"PRD 权限码 {perm.code} 在 REQ-GLOBAL 权限矩阵中不存在",
                    location=prd.filename,
                    suggestion=f"在 REQ-GLOBAL §9.3 权限矩阵中补充 {perm.code}",
                ))
        return result

    def run(self, prd_docs: List[PrdDocument], req_docs: List[ReqDocument]) -> CheckResult:
        result = CheckResult(dimension="3", dimension_name="错误码/权限码一致性")
        global_doc = next((d for d in req_docs if d.is_global), None)
        if not global_doc:
            result.summary_lines.append("未找到 REQ-GLOBAL，跳过检查")
            return result
        prd_by_module = {d.module: d for d in prd_docs}
        req_by_module = {d.module: d for d in req_docs if not d.is_global}
        for module, req in req_by_module.items():
            r = self.check_error_codes(global_doc, req)
            result.issues.extend(r.issues)
        for module, prd in prd_by_module.items():
            req = req_by_module.get(module)
            if req:
                r = self.check_prd_req_error_mapping(prd, req)
                result.issues.extend(r.issues)
        for prd in prd_docs:
            r = self.check_permissions(prd, global_doc)
            result.issues.extend(r.issues)
        result.summary_lines.append(f"错误码/权限码问题: {len(result.issues)} 个")
        return result

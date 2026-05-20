"""维度4: API 接口一致性检查。"""
import re
from typing import List, Dict
from ..models import PrdDocument, ReqDocument, ApiEndpoint, ConsistencyIssue, Severity, CheckResult


class ApiInterfaceChecker:
    def check(self, prd: PrdDocument, req: ReqDocument) -> CheckResult:
        result = CheckResult(dimension="4", dimension_name="API 接口一致性")

        def normalize(path):
            return re.sub(r"\{[^}]+\}", "{param}", path)

        req_api_map = {}
        for api in req.api_endpoints:
            key = (normalize(api.path), api.method)
            req_api_map[key] = api

        for prd_api in prd.api_endpoints:
            key = (normalize(prd_api.path), prd_api.method)
            if key in req_api_map:
                pass  # matched
            else:
                path_matches = [req_api for (p, m), req_api in req_api_map.items() if p == normalize(prd_api.path)]
                if path_matches:
                    methods = [a.method for a in path_matches]
                    result.issues.append(ConsistencyIssue(
                        dimension="4", severity=Severity.HIGH,
                        module=prd.module,
                        description=f"API {prd_api.path} 方法不一致: PRD={prd_api.method}, REQ={methods}",
                        location=prd.filename,
                        suggestion="统一 HTTP 方法",
                    ))
                else:
                    result.issues.append(ConsistencyIssue(
                        dimension="4", severity=Severity.MEDIUM,
                        module=prd.module,
                        description=f"PRD API {prd_api.method} {prd_api.path} 在 REQ 中未覆盖",
                        location=prd.filename,
                        suggestion=f"在 {req.filename} 中补充该 API 的实现规格",
                    ))
        return result

    def run(self, prd_docs: List[PrdDocument], req_docs: List[ReqDocument]) -> CheckResult:
        result = CheckResult(dimension="4", dimension_name="API 接口一致性")
        prd_by_module = {d.module: d for d in prd_docs}
        req_by_module = {d.module: d for d in req_docs if not d.is_global}
        total_prd = 0
        total_covered = 0
        for module, prd in prd_by_module.items():
            req = req_by_module.get(module)
            if not req:
                total_prd += len(prd.api_endpoints)
                continue
            total_prd += len(prd.api_endpoints)
            r = self.check(prd, req)
            result.issues.extend(r.issues)
            covered = len(prd.api_endpoints) - sum(1 for i in r.issues if module == i.module and "未覆盖" in i.description)
            total_covered += covered
        rate = f"{total_covered * 100 // max(total_prd, 1)}%"
        result.summary_lines = [f"API 覆盖率: {rate} ({total_covered}/{total_prd})", f"API 问题: {len(result.issues)} 个"]
        return result

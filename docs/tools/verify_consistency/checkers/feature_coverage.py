"""维度1: 功能点覆盖完整性检查。"""

from typing import List, Dict

from ..models import (
    PrdDocument, ReqDocument, Feature, FeatureMapping,
    MatchMethod, ConsistencyIssue, Severity, CheckResult,
)


class FeatureCoverageChecker:
    """检查 PRD 功能点是否被 REQ 完整覆盖。"""

    def check(self, prd: PrdDocument, req: ReqDocument) -> List[FeatureMapping]:
        mappings = []
        req_by_id = {f.id: f for f in req.features}
        matched_req_ids = set()

        for pf in prd.features:
            if pf.id in req_by_id:
                mappings.append(FeatureMapping(
                    prd_feature=pf, req_feature=req_by_id[pf.id],
                    match_method=MatchMethod.EXACT, status="一致",
                ))
                matched_req_ids.add(pf.id)
            else:
                mappings.append(FeatureMapping(
                    prd_feature=pf, req_feature=None,
                    match_method=MatchMethod.MANUAL, status="PRD未覆盖",
                ))

        for rf in req.features:
            if rf.id not in matched_req_ids:
                semantic_match = self._semantic_match(rf, prd.features)
                if semantic_match:
                    mappings.append(FeatureMapping(
                        prd_feature=semantic_match, req_feature=rf,
                        match_method=MatchMethod.SEMANTIC, status="一致",
                    ))
                else:
                    mappings.append(FeatureMapping(
                        prd_feature=None, req_feature=rf,
                        match_method=MatchMethod.MANUAL, status="REQ无来源",
                    ))

        return mappings

    def _semantic_match(self, req_feature: Feature, prd_features: List[Feature]) -> Feature:
        req_name = req_feature.name.strip()
        for pf in prd_features:
            if pf.name.strip() == req_name:
                return pf
        return None

    def run(self, prd_docs: List[PrdDocument], req_docs: List[ReqDocument]) -> CheckResult:
        result = CheckResult(dimension="1", dimension_name="功能点覆盖完整性")
        prd_by_module = {d.module: d for d in prd_docs}
        req_by_module = {d.module: d for d in req_docs}
        all_mappings = []
        total_prd = 0
        total_covered = 0
        total_no_source = 0

        for module, prd in prd_by_module.items():
            req = req_by_module.get(module)
            if not req:
                for f in prd.features:
                    all_mappings.append(FeatureMapping(
                        prd_feature=f, req_feature=None,
                        match_method=MatchMethod.MANUAL, status="PRD未覆盖",
                    ))
                total_prd += len(prd.features)
                continue

            mappings = self.check(prd, req)
            all_mappings.extend(mappings)
            total_prd += len(prd.features)
            covered = sum(1 for m in mappings if m.status == "一致")
            total_covered += covered
            total_no_source += sum(1 for m in mappings if m.status == "REQ无来源")

        rate = f"{total_covered * 100 // max(total_prd, 1)}%"
        result.summary_lines = [
            f"功能覆盖率: {rate} ({total_covered}/{total_prd})",
            f"PRD 未覆盖功能: {total_prd - total_covered} 个",
            f"REQ 无来源功能: {total_no_source} 个",
        ]

        for m in all_mappings:
            if m.status == "PRD未覆盖":
                result.issues.append(ConsistencyIssue(
                    dimension="1", severity=Severity.MEDIUM,
                    module=m.prd_feature.module,
                    description=f"PRD 功能 {m.prd_feature.id} ({m.prd_feature.name}) 在 REQ 中未覆盖",
                    location=m.prd_feature.source,
                    suggestion="确认是否需要在 REQ 中补充该功能",
                ))
            elif m.status == "REQ无来源":
                result.issues.append(ConsistencyIssue(
                    dimension="1", severity=Severity.LOW,
                    module=m.req_feature.module,
                    description=f"REQ 功能 {m.req_feature.id} ({m.req_feature.name}) 无 PRD 来源",
                    location=m.req_feature.source,
                    suggestion="确认是否需要在 PRD 中补充该功能定义",
                ))

        return result

"""REQ Markdown 文档解析器。

从 REQ 文档中提取: 功能ID、状态机、错误码、权限码、API接口、SQL表引用。
"""

import re
from pathlib import Path
from typing import List, Dict, Tuple

from .models import (
    ReqDocument, Feature, ErrorCode, PermissionCode,
    ApiEndpoint, TableField, StatusDefinition, StatusTransition,
)


class ReqParser:
    """解析单个 REQ Markdown 文件。"""

    _FEATURE_ID_RE = re.compile(r"F-(\w+)-(\d+)")

    # 状态码枚举表格
    _STATUS_TABLE_HEADER = re.compile(r"\|\s*status\s*\|\s*状态名称\s*\|")
    _STATUS_ROW_RE = re.compile(r"\|\s*(\d+)\s*\|\s*(\S+)\s*\|\s*`?(\w+)`?\s*\|\s*(\S+)\s*\|\s*(\S+)\s*\|")

    # 流转矩阵
    _TRANSITION_HEADER = re.compile(r"\|\s*目标状态\s*\|\s*允许的前置状态\s*\|")
    _TRANSITION_ROW_RE = re.compile(
        r"\|\s*(\d+)（[^）]+）\s*\|\s*(\d+)（[^）]+）\s*\|\s*(.+?)\s*\|\s*(\S+)\s*\|\s*(\S+)\s*\|"
    )

    # 错误码分段
    _SEGMENT_HEADER = re.compile(r"\|\s*段范围\s*\|\s*所属模块\s*\|")
    _SEGMENT_ROW_RE = re.compile(r"\|\s*(\d{4})-(\d{4})\s*\|\s*(\S+)\s*\|")

    # 错误码表格 (4列和5列两种格式)
    _ERROR_TABLE_5COL = re.compile(r"\|\s*错误码\s*\|\s*常量名\s*\|\s*HTTP状态码\s*\|\s*描述\s*\|\s*触发场景\s*\|")
    _ERROR_TABLE_4COL = re.compile(r"\|\s*错误码\s*\|\s*常量名\s*\|\s*HTTP状态码\s*\|\s*描述\s*\|")
    _ERROR_CODE_ROW_RE = re.compile(r"\|\s*(\d{4})\s*\|")

    # 权限矩阵
    _PERM_MATRIX_HEADER = re.compile(r"\|\s*权限编码\s*\|\s*权限名称\s*\|")
    _PERM_CODE_RE = re.compile(r"([a-z]+\.[a-z]+(?:\.[a-z]+)?)")

    # 功能清单
    _FEATURE_TABLE_HEADER = re.compile(r"\|\s*编号\s*\|\s*功能名称\s*\|\s*API\s*\|")

    # API 路径
    _API_PATH_RE = re.compile(r"(/api/[^\s`|}]+)")
    _HTTP_METHOD_RE = re.compile(r"\b(GET|POST|PUT|DELETE|PATCH)\b")

    # SQL 表引用
    _SQL_TABLE_RE = re.compile(
        r"(?:FROM|INTO|UPDATE|JOIN)\s+(\w+)",
        re.IGNORECASE,
    )

    # 异常处理表格
    _EXCEPTION_TABLE_HEADER = re.compile(r"\|\s*异常场景\s*\|\s*错误码\s*\|\s*HTTP\s*\|")
    _EXCEPTION_CODE_RE = re.compile(r"\|\s*(\d{4})\s*\|")

    # 权限控制汇总
    _PERM_SUMMARY_HEADER = re.compile(r"\|\s*功能编号\s*\|\s*权限编码\s*\|")

    def parse(self, filepath: Path) -> ReqDocument:
        text = filepath.read_text(encoding="utf-8")
        stem = filepath.stem.upper()
        module = self._extract_module(stem)
        is_global = "GLOBAL" in stem

        doc = ReqDocument(
            filename=filepath.stem,
            module=module,
            is_global=is_global,
        )

        doc.features = self._parse_features(text, module)
        doc.error_codes = self._parse_error_codes(text, module)
        doc.permission_codes = self._parse_permissions(text, module)
        doc.api_endpoints = self._parse_api_endpoints(text, module)
        doc.table_fields = self._parse_sql_tables(text, module)

        if is_global:
            doc.status_definitions = self._parse_status_definitions(text)
            doc.status_transitions = self._parse_status_transitions(text)
            doc.error_code_segments = self._parse_error_segments(text)

        return doc

    def _extract_module(self, stem: str) -> str:
        m = re.match(r"REQ[-_]?(\w+)", stem, re.IGNORECASE)
        return m.group(1).upper() if m else stem.upper()

    # ---- 功能列表 ----

    def _parse_features(self, text: str, module: str) -> List[Feature]:
        features = []
        lines = text.split("\n")
        in_table = False

        for line in lines:
            if self._FEATURE_TABLE_HEADER.search(line):
                in_table = True
                continue
            if in_table and line.strip().startswith("|---"):
                continue
            if in_table and line.strip().startswith("|"):
                m = self._FEATURE_ID_RE.search(line)
                if m:
                    fid = m.group(0)
                    cells = [c.strip() for c in line.split("|")]
                    name = cells[2] if len(cells) > 2 else ""
                    name = re.sub(r"\s+", " ", name).strip()
                    features.append(Feature(
                        id=fid, name=name, source=f"REQ-{module}", module=module,
                    ))
            elif in_table and not line.strip().startswith("|"):
                in_table = False

        return features

    # ---- 状态机 ----

    def _parse_status_definitions(self, text: str) -> List[StatusDefinition]:
        defs = []
        lines = text.split("\n")
        in_table = False

        for line in lines:
            if self._STATUS_TABLE_HEADER.search(line):
                in_table = True
                continue
            if in_table and line.strip().startswith("|---"):
                continue
            if in_table and line.strip().startswith("|"):
                m = self._STATUS_ROW_RE.search(line)
                if m:
                    defs.append(StatusDefinition(
                        code=int(m.group(1)),
                        name=m.group(2),
                        constant=m.group(3),
                        category=m.group(4),
                        window=m.group(5),
                    ))
            elif in_table and not line.strip().startswith("|"):
                in_table = False

        return defs

    def _parse_status_transitions(self, text: str) -> List[StatusTransition]:
        transitions = []
        lines = text.split("\n")
        in_table = False

        for line in lines:
            if self._TRANSITION_HEADER.search(line):
                in_table = True
                continue
            if in_table and line.strip().startswith("|---"):
                continue
            if in_table and line.strip().startswith("|"):
                m = self._TRANSITION_ROW_RE.search(line)
                if m:
                    transitions.append(StatusTransition(
                        from_status=int(m.group(2)),
                        to_status=int(m.group(1)),
                        operation=m.group(3).strip(),
                        role=m.group(4).strip(),
                        module=m.group(5).strip(),
                    ))
            elif in_table and not line.strip().startswith("|"):
                in_table = False

        return transitions

    # ---- 错误码 ----

    def _parse_error_segments(self, text: str) -> Dict[str, str]:
        segments = {}
        lines = text.split("\n")
        in_table = False

        for line in lines:
            if self._SEGMENT_HEADER.search(line):
                in_table = True
                continue
            if in_table and line.strip().startswith("|---"):
                continue
            if in_table and line.strip().startswith("|"):
                m = self._SEGMENT_ROW_RE.search(line)
                if m:
                    key = f"{m.group(1)}-{m.group(2)}"
                    segments[key] = m.group(3).strip()
            elif in_table and not line.strip().startswith("|"):
                in_table = False

        return segments

    def _parse_error_codes(self, text: str, module: str) -> List[ErrorCode]:
        codes = []
        lines = text.split("\n")
        in_table = False

        for line in lines:
            if self._ERROR_TABLE_5COL.search(line) or self._ERROR_TABLE_4COL.search(line):
                in_table = True
                continue
            if in_table and line.strip().startswith("|---"):
                continue
            if in_table and line.strip().startswith("|"):
                m = self._ERROR_CODE_ROW_RE.search(line)
                if m:
                    codes.append(ErrorCode(
                        id="",
                        code=int(m.group(1)),
                        message="",
                        source=f"REQ-{module}",
                        module=module,
                    ))
            elif in_table and not line.strip().startswith("|"):
                in_table = False

        # 也从异常处理表格中提取（模块 REQ）
        in_table = False
        for line in lines:
            if self._EXCEPTION_TABLE_HEADER.search(line):
                in_table = True
                continue
            if in_table and line.strip().startswith("|---"):
                continue
            if in_table and line.strip().startswith("|"):
                m = self._EXCEPTION_CODE_RE.search(line)
                if m:
                    code_val = int(m.group(1))
                    if not any(ec.code == code_val for ec in codes):
                        codes.append(ErrorCode(
                            id="",
                            code=code_val,
                            message="",
                            source=f"REQ-{module}",
                            module=module,
                        ))
            elif in_table and not line.strip().startswith("|"):
                in_table = False

        return codes

    # ---- 权限码 ----

    def _parse_permissions(self, text: str, module: str) -> List[PermissionCode]:
        perms = []
        lines = text.split("\n")
        in_table = False

        for line in lines:
            if (self._PERM_MATRIX_HEADER.search(line) or
                    self._PERM_SUMMARY_HEADER.search(line)):
                in_table = True
                continue
            if in_table and line.strip().startswith("|---"):
                continue
            if in_table and line.strip().startswith("|"):
                m = self._PERM_CODE_RE.search(line)
                if m:
                    perm_code = m.group(1)
                    cells = [c.strip() for c in line.split("|")]
                    name = cells[2] if len(cells) > 2 else ""
                    perms.append(PermissionCode(
                        code=perm_code,
                        name=name,
                        source=f"REQ-{module}",
                        module=module,
                    ))
            elif in_table and not line.strip().startswith("|"):
                in_table = False

        return perms

    # ---- API 接口 ----

    def _parse_api_endpoints(self, text: str, module: str) -> List[ApiEndpoint]:
        apis = []
        lines = text.split("\n")
        in_table = False

        for line in lines:
            if self._FEATURE_TABLE_HEADER.search(line):
                in_table = True
                continue
            if in_table and line.strip().startswith("|---"):
                continue
            if in_table and line.strip().startswith("|"):
                fid_m = self._FEATURE_ID_RE.search(line)
                path_m = self._API_PATH_RE.search(line)
                method_m = self._HTTP_METHOD_RE.search(line)

                if path_m:
                    apis.append(ApiEndpoint(
                        path=path_m.group(1),
                        method=method_m.group(1) if method_m else "",
                        feature_id=fid_m.group(0) if fid_m else "",
                        source=f"REQ-{module}",
                        module=module,
                    ))
            elif in_table and not line.strip().startswith("|"):
                in_table = False

        # 也从权限汇总表中提取
        in_table = False
        for line in lines:
            if self._PERM_SUMMARY_HEADER.search(line):
                in_table = True
                continue
            if in_table and line.strip().startswith("|---"):
                continue
            if in_table and line.strip().startswith("|"):
                path_m = self._API_PATH_RE.search(line)
                method_m = self._HTTP_METHOD_RE.search(line)
                fid_m = self._FEATURE_ID_RE.search(line)
                if path_m and not any(a.path == path_m.group(1) for a in apis):
                    apis.append(ApiEndpoint(
                        path=path_m.group(1),
                        method=method_m.group(1) if method_m else "",
                        feature_id=fid_m.group(0) if fid_m else "",
                        source=f"REQ-{module}",
                        module=module,
                    ))
            elif in_table and not line.strip().startswith("|"):
                in_table = False

        return apis

    # ---- SQL 表引用 ----

    # 非表名的过滤词：SQL关键字、函数、短词、中文词
    _TABLE_FILTER = {
        "where", "now", "lock", "select", "from", "into", "update",
        "join", "set", "on", "and", "or", "not", "in", "is", "as",
        "if", "then", "else", "end", "case", "when", "exists",
        "current_timestamp", "available_stock", "summary",
    }

    def _parse_sql_tables(self, text: str, module: str) -> List[TableField]:
        fields = []
        tables_seen = set()

        for m in self._SQL_TABLE_RE.finditer(text):
            table = m.group(1).lower()
            # 过滤：包含非ASCII（中文）、过短、SQL关键字/函数
            if not table.isascii() or len(table) < 3:
                continue
            if table in self._TABLE_FILTER:
                continue
            if table not in tables_seen:
                tables_seen.add(table)
                fields.append(TableField(
                    table=table,
                    field="*",
                    field_type="",
                    required=False,
                    source=f"REQ-{module}",
                ))

        return fields

"""PRD Markdown 文档解析器。

从 PRD 文档中提取: 功能ID、错误码、权限码、API接口、数据库表字段。
"""

import re
from pathlib import Path
from typing import List

from .models import (
    PrdDocument, Feature, ErrorCode, PermissionCode,
    ApiEndpoint, TableField,
)


class PrdParser:
    """解析单个 PRD Markdown 文件。"""

    # 功能清单表格列头模式
    _FEATURE_TABLE_HEADER = re.compile(r"\|\s*编号\s*\|\s*功能名称\s*\|")
    _FEATURE_ID_RE = re.compile(r"F-(\w+)-(\d+)")

    # 数据库表字段表格
    _TABLE_HEADING_RE = re.compile(
        r"###\s+\d+\.\d+\s+\S+（(\w+)）字段"
    )
    _FIELD_TABLE_HEADER = re.compile(r"\|\s*字段名\s*\|\s*中文名\s*\|")

    # 权限码
    _PERM_TABLE_HEADER = re.compile(r"\|\s*权限编码\s*\|\s*权限名称\s*\|")
    _PERM_CODE_RE = re.compile(r"([a-z]+\.[a-z]+(?:\.[a-z]+)?)")

    # 异常场景
    _EXCEPTION_BLOCK_RE = re.compile(
        r"####\s+(E-(\w+)-(\d+))[:：](.+?)(?=\n####|\n##|\Z)",
        re.DOTALL,
    )
    _ERROR_CODE_RE = re.compile(r"\*\*错误码[：:]\*\*\s*(\d+)")
    _HTTP_STATUS_RE = re.compile(r"\*\*HTTP状态码[：:]\*\*\s*(\d+)")

    # API 接口
    _API_TABLE_HEADER = re.compile(r"\|\s*接口路径\s*\|\s*请求方法\s*\|")
    _API_PATH_RE = re.compile(r"(/api/[^\s|}]+)")
    _HTTP_METHOD_RE = re.compile(r"\b(GET|POST|PUT|DELETE|PATCH)\b")

    # 文档标题中的模块名
    _TITLE_MODULE_RE = re.compile(r"#\s+PRD-(\w+)")

    def parse(self, filepath: Path) -> PrdDocument:
        """解析 PRD 文件，返回结构化 PrdDocument。"""
        text = filepath.read_text(encoding="utf-8")
        module = self._extract_module(filepath.stem, text)
        doc = PrdDocument(filename=filepath.name, module=module)

        doc.features = self._parse_features(text, module)
        doc.error_codes = self._parse_error_codes(text, module)
        doc.permission_codes = self._parse_permissions(text, module)
        doc.api_endpoints = self._parse_api_endpoints(text, module)
        doc.table_fields = self._parse_table_fields(text, module)

        return doc

    def _extract_module(self, stem: str, text: str = "") -> str:
        """从文件名或文档标题提取模块名。

        优先从文件名提取（如 'PRD-APPOINTMENT' -> 'APPOINTMENT'），
        若文件名不匹配则从文档首行标题提取（如 '# PRD-APPOINTMENT ...' -> 'APPOINTMENT'）。
        """
        m = re.match(r"PRD-?(\w+)", stem, re.IGNORECASE)
        if m:
            return m.group(1).upper()
        # 回退到文档标题
        title_m = self._TITLE_MODULE_RE.search(text[:200])
        if title_m:
            return title_m.group(1).upper()
        return stem.upper()

    # ---- 功能列表 ----

    def _parse_features(self, text: str, module: str) -> List[Feature]:
        features = []
        lines = text.split("\n")
        in_table = False

        for i, line in enumerate(lines):
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
                        id=fid, name=name, source=f"PRD-{module}", module=module,
                    ))
            elif in_table and not line.strip().startswith("|"):
                in_table = False

        return features

    # ---- 错误码 ----

    def _parse_error_codes(self, text: str, module: str) -> List[ErrorCode]:
        codes = []
        for block_match in self._EXCEPTION_BLOCK_RE.finditer(text):
            eid = block_match.group(1)       # E-APPOINTMENT-001
            block_text = block_match.group(4)

            code_m = self._ERROR_CODE_RE.search(block_text)
            http_m = self._HTTP_STATUS_RE.search(block_text)

            if code_m:
                codes.append(ErrorCode(
                    id=eid,
                    code=int(code_m.group(1)),
                    message="",
                    source=f"PRD-{module}",
                    module=module,
                    http_status=int(http_m.group(1)) if http_m else 0,
                ))
        return codes

    # ---- 权限码 ----

    def _parse_permissions(self, text: str, module: str) -> List[PermissionCode]:
        perms = []
        lines = text.split("\n")
        in_table = False

        for line in lines:
            if self._PERM_TABLE_HEADER.search(line):
                in_table = True
                continue
            if in_table and line.strip().startswith("|---"):
                continue
            if in_table and line.strip().startswith("|"):
                cells = [c.strip() for c in line.split("|")]
                code_cell = cells[1] if len(cells) > 1 else ""
                name_cell = cells[2] if len(cells) > 2 else ""
                m = self._PERM_CODE_RE.search(code_cell)
                if m:
                    perms.append(PermissionCode(
                        code=m.group(1),
                        name=name_cell,
                        source=f"PRD-{module}",
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
            if self._API_TABLE_HEADER.search(line):
                in_table = True
                continue
            if in_table and line.strip().startswith("|---"):
                continue
            if in_table and line.strip().startswith("|"):
                path_m = self._API_PATH_RE.search(line)
                method_m = self._HTTP_METHOD_RE.search(line)
                fid_m = self._FEATURE_ID_RE.search(line)

                if path_m:
                    apis.append(ApiEndpoint(
                        path=path_m.group(1),
                        method=method_m.group(1) if method_m else "",
                        feature_id=fid_m.group(0) if fid_m else "",
                        source=f"PRD-{module}",
                        module=module,
                    ))
            elif in_table and not line.strip().startswith("|"):
                in_table = False

        return apis

    # ---- 数据库表字段 ----

    def _parse_table_fields(self, text: str, module: str) -> List[TableField]:
        fields = []
        lines = text.split("\n")
        current_table = None
        in_field_table = False

        for line in lines:
            table_m = self._TABLE_HEADING_RE.search(line)
            if table_m:
                current_table = table_m.group(1)
                in_field_table = False
                continue

            if current_table and self._FIELD_TABLE_HEADER.search(line):
                in_field_table = True
                continue

            if in_field_table and line.strip().startswith("|---"):
                continue

            if in_field_table and line.strip().startswith("|"):
                cells = [c.strip() for c in line.split("|")]
                if len(cells) > 5:
                    fname = cells[1]
                    ftype = cells[3]
                    req_str = cells[5]
                    required = req_str == "是"

                    if fname and not fname.startswith("---"):
                        fields.append(TableField(
                            table=current_table,
                            field=fname,
                            field_type=ftype,
                            required=required,
                            source=f"PRD-{module}",
                        ))
            elif in_field_table and not line.strip().startswith("|"):
                in_field_table = False
                current_table = None

        return fields

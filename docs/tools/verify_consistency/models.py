"""PRD→REQ 一致性验证的数据模型定义。"""

from dataclasses import dataclass, field
from enum import Enum
from typing import List, Dict, Optional, Tuple


class Severity(Enum):
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"


class MatchMethod(Enum):
    EXACT = "精确匹配"
    SEMANTIC = "语义匹配"
    MANUAL = "人工确认"


@dataclass
class Feature:
    id: str            # F-APPOINTMENT-001
    name: str          # 创建预约
    source: str        # PRD-APPOINTMENT / REQ-APPOINTMENT
    module: str        # APPOINTMENT
    description: str = ""


@dataclass
class ErrorCode:
    id: str            # E-APPOINTMENT-001 (PRD) 或 "" (REQ-only)
    code: int          # 2001
    message: str       # 儿童档案不存在
    source: str        # PRD-APPOINTMENT / REQ-APPOINTMENT
    module: str        # APPOINTMENT
    http_status: int = 0


@dataclass
class PermissionCode:
    code: str          # appointment.book
    name: str          # 预约接种
    source: str        # PRD-APPOINTMENT / REQ-GLOBAL
    module: str        # APPOINTMENT


@dataclass
class ApiEndpoint:
    path: str          # /api/user/appointment
    method: str        # POST
    feature_id: str    # F-APPOINTMENT-001
    source: str        # PRD-APPOINTMENT / REQ-APPOINTMENT
    module: str        # APPOINTMENT
    description: str = ""


@dataclass
class TableField:
    table: str         # appointment
    field: str         # user_id
    field_type: str    # bigint
    required: bool     # True
    source: str        # PRD-APPOINTMENT / REQ-APPOINTMENT
    default_value: str = ""


@dataclass
class StatusDefinition:
    code: int          # 1
    name: str          # 已预约
    constant: str      # APPOINTED
    category: str      # 正常 / 异常
    window: str        # SIGNIN / -


@dataclass
class StatusTransition:
    from_status: int   # 1
    to_status: int     # 6
    operation: str     # 执行签到
    role: str          # DOCTOR_SIGNIN
    module: str        # PRD-FLOW


@dataclass
class ConsistencyIssue:
    dimension: str     # 1/2/3/4/5
    severity: Severity
    module: str
    description: str
    location: str      # 文件名 + 节号
    suggestion: str    # 修复建议


@dataclass
class FeatureMapping:
    prd_feature: Optional[Feature]
    req_feature: Optional[Feature]
    match_method: MatchMethod
    status: str        # 一致 / PRD未覆盖 / REQ无来源


@dataclass
class PrdDocument:
    filename: str
    module: str
    features: List[Feature] = field(default_factory=list)
    error_codes: List[ErrorCode] = field(default_factory=list)
    permission_codes: List[PermissionCode] = field(default_factory=list)
    api_endpoints: List[ApiEndpoint] = field(default_factory=list)
    table_fields: List[TableField] = field(default_factory=list)


@dataclass
class ReqDocument:
    filename: str
    module: str
    is_global: bool = False
    features: List[Feature] = field(default_factory=list)
    error_codes: List[ErrorCode] = field(default_factory=list)
    permission_codes: List[PermissionCode] = field(default_factory=list)
    api_endpoints: List[ApiEndpoint] = field(default_factory=list)
    table_fields: List[TableField] = field(default_factory=list)
    status_definitions: List[StatusDefinition] = field(default_factory=list)
    status_transitions: List[StatusTransition] = field(default_factory=list)
    referenced_status_codes: List[int] = field(default_factory=list)
    error_code_segments: Dict[str, str] = field(default_factory=dict)


@dataclass
class CheckResult:
    dimension: str
    dimension_name: str
    issues: List[ConsistencyIssue] = field(default_factory=list)
    summary_lines: List[str] = field(default_factory=list)
    detail_tables: List[str] = field(default_factory=list)
    appendix_tables: List[str] = field(default_factory=list)

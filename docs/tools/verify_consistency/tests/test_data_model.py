"""数据模型一致性检查器测试。"""
import unittest
from verify_consistency.models import PrdDocument, ReqDocument, TableField
from verify_consistency.checkers.data_model import DataModelChecker


class TestDataModelChecker(unittest.TestCase):
    def setUp(self):
        self.checker = DataModelChecker()
        self.prd = PrdDocument(filename="PRD-TEST", module="TEST")
        self.prd.table_fields = [
            TableField(table="appointment", field="id", field_type="bigint", required=True, source="PRD-TEST"),
            TableField(table="appointment", field="user_id", field_type="bigint", required=True, source="PRD-TEST"),
            TableField(table="child_profile", field="id", field_type="bigint", required=True, source="PRD-TEST"),
        ]
        self.req = ReqDocument(filename="REQ-TEST", module="TEST")
        self.req.table_fields = [
            TableField(table="appointment", field="*", field_type="", required=False, source="REQ-TEST"),
            TableField(table="vaccine", field="*", field_type="", required=False, source="REQ-TEST"),
        ]

    def test_table_not_in_prd(self):
        result = self.checker.check(self.prd, self.req)
        no_source = [i for i in result.issues if "PRD 字段定义中未定义" in i.description]
        self.assertEqual(len(no_source), 1)
        self.assertIn("vaccine", no_source[0].description)

    def test_table_not_used_in_req(self):
        result = self.checker.check(self.prd, self.req)
        unused = [i for i in result.issues if "REQ 中未被引用" in i.description]
        self.assertEqual(len(unused), 1)
        self.assertIn("child_profile", unused[0].description)


if __name__ == "__main__":
    unittest.main()

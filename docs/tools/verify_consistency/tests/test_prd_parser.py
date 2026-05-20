"""PRD Parser 测试。"""

import unittest
from pathlib import Path
from verify_consistency.prd_parser import PrdParser

FIXTURES_DIR = Path(__file__).parent / "fixtures"


class TestPrdParser(unittest.TestCase):

    def setUp(self):
        self.parser = PrdParser()
        self.doc = self.parser.parse(FIXTURES_DIR / "sample_prd.md")

    def test_module_extraction(self):
        self.assertEqual(self.doc.module, "APPOINTMENT")
        self.assertEqual(self.doc.filename, "sample_prd.md")

    def test_feature_count(self):
        self.assertEqual(len(self.doc.features), 4)

    def test_feature_ids(self):
        ids = [f.id for f in self.doc.features]
        self.assertIn("F-APPOINTMENT-001", ids)
        self.assertIn("F-APPOINTMENT-002", ids)
        self.assertIn("F-APPOINTMENT-005", ids)
        self.assertIn("F-APPOINTMENT-007", ids)

    def test_feature_names(self):
        f001 = next(f for f in self.doc.features if f.id == "F-APPOINTMENT-001")
        self.assertEqual(f001.name, "创建预约")
        self.assertEqual(f001.module, "APPOINTMENT")

    def test_error_codes(self):
        self.assertEqual(len(self.doc.error_codes), 3)
        codes = sorted([e.code for e in self.doc.error_codes])
        self.assertEqual(codes, [1001, 2005, 5001])

    def test_error_code_business(self):
        e = next(ec for ec in self.doc.error_codes if ec.id == "E-APPOINTMENT-001")
        self.assertEqual(e.code, 1001)
        self.assertEqual(e.http_status, 403)

    def test_permission_codes(self):
        codes = [p.code for p in self.doc.permission_codes]
        self.assertIn("appointment.book", codes)
        self.assertIn("appointment.view.own", codes)

    def test_api_endpoints(self):
        self.assertEqual(len(self.doc.api_endpoints), 3)
        paths = [a.path for a in self.doc.api_endpoints]
        self.assertIn("/api/user/appointment", paths)

    def test_api_endpoint_method(self):
        api = next(a for a in self.doc.api_endpoints if a.feature_id == "F-APPOINTMENT-001")
        self.assertEqual(api.method, "POST")
        self.assertEqual(api.path, "/api/user/appointment")

    def test_table_fields(self):
        tables = set(tf.table for tf in self.doc.table_fields)
        self.assertIn("appointment", tables)
        fields = [tf.field for tf in self.doc.table_fields if tf.table == "appointment"]
        self.assertIn("user_id", fields)
        self.assertIn("status", fields)

    def test_table_field_required(self):
        f = next(tf for tf in self.doc.table_fields
                 if tf.table == "appointment" and tf.field == "user_id")
        self.assertTrue(f.required)


if __name__ == "__main__":
    unittest.main()

"""REQ Parser 测试。"""

import unittest
from pathlib import Path
from verify_consistency.req_parser import ReqParser

FIXTURES_DIR = Path(__file__).parent / "fixtures"


class TestReqGlobalParser(unittest.TestCase):

    def setUp(self):
        self.parser = ReqParser()
        self.doc = self.parser.parse(FIXTURES_DIR / "sample_req_global.md")

    def test_is_global(self):
        self.assertTrue(self.doc.is_global)

    def test_status_definitions(self):
        codes = [s.code for s in self.doc.status_definitions]
        self.assertEqual(len(codes), 9)
        self.assertIn(1, codes)
        self.assertIn(10, codes)
        self.assertIn(9, codes)

    def test_status_transition_count(self):
        self.assertEqual(len(self.doc.status_transitions), 8)

    def test_status_transition_example(self):
        t = self.doc.status_transitions[0]
        self.assertEqual(t.from_status, 1)
        self.assertEqual(t.to_status, 6)
        self.assertEqual(t.module, "PRD-FLOW")

    def test_error_code_segments(self):
        self.assertEqual(self.doc.error_code_segments["1000-1999"], "系统级")
        self.assertEqual(self.doc.error_code_segments["7000-7999"], "用户模块")

    def test_error_codes_global(self):
        codes = sorted([e.code for e in self.doc.error_codes])
        self.assertIn(1001, codes)
        self.assertIn(2001, codes)
        self.assertIn(7006, codes)

    def test_permission_codes_global(self):
        codes = [p.code for p in self.doc.permission_codes]
        self.assertIn("appointment.book", codes)
        self.assertIn("precheck.assess", codes)
        self.assertIn("stock.view", codes)


class TestReqModuleParser(unittest.TestCase):

    def setUp(self):
        self.parser = ReqParser()
        self.doc = self.parser.parse(FIXTURES_DIR / "req_appointment.md")

    def test_module(self):
        self.assertEqual(self.doc.module, "APPOINTMENT")
        self.assertFalse(self.doc.is_global)

    def test_features(self):
        self.assertEqual(len(self.doc.features), 4)
        ids = [f.id for f in self.doc.features]
        self.assertIn("F-APPOINTMENT-001", ids)
        self.assertIn("F-APPOINTMENT-013", ids)

    def test_error_codes(self):
        codes = sorted([e.code for e in self.doc.error_codes])
        self.assertIn(1001, codes)
        self.assertIn(2001, codes)
        self.assertIn(2005, codes)
        self.assertIn(2007, codes)

    def test_api_endpoints(self):
        apis = self.doc.api_endpoints
        self.assertTrue(any(a.path == "/api/user/appointment" and a.method == "POST" for a in apis))

    def test_sql_table_references(self):
        tables = set(tf.table for tf in self.doc.table_fields)
        self.assertIn("appointment", tables)

    def test_permission_codes(self):
        codes = [p.code for p in self.doc.permission_codes]
        self.assertIn("appointment.book", codes)
        self.assertIn("appointment.view.own", codes)


if __name__ == "__main__":
    unittest.main()

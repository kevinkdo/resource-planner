import unittest
import requests
import json

class TestTagTestCases(unittest.TestCase):
  def setUp(self):
      self.baseUrl = 'http://localhost:8080/'
      self. headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }

  def test_GetTags(self):
      tagUrl = 'api/tags'
      r = requests.get(self.baseUrl + tagUrl, headers=self.headers)
      decoded =  json.loads(r.text)
      assert decoded['is_error'] == False 

if __name__ == '__main__':
    unittest.main()

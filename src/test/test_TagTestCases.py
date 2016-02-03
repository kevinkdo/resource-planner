import unittest
import requests
import json

class TestTagTestCases(unittest.TestCase):
  def setUp(self):
      pass

  def test_GetTags(self):
      url = 'http://localhost:8080/api/tags'
      headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
      r = requests.get(url, headers=headers)
      decoded =  json.loads(r.text)
      assert decoded['is_error'] == False 

if __name__ == '__main__':
    unittest.main()

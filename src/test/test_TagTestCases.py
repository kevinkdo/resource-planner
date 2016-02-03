import unittest
import requests
import json

class TestTagTestCases(unittest.TestCase):
  def setUp(self):
      self.baseUrl = 'http://localhost:8080/'
      self.headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
      self.resetUrl = '/admin/init'
      self.tagUrl = 'api/tags'
      requests.get(self.baseUrl + self.resetUrl, headers = self.headers)

  def test_GetInitialTags(self):      
      r = requests.get(self.baseUrl + self.tagUrl, headers = self.headers)
      decoded =  json.loads(r.text)
      assert decoded['is_error'] == False 
      assert decoded['data'] == {'tags': []}
      assert decoded['error_msg'] == 'Successfully retrieved  tags'

  def test_GetTagsAfterPosting(self):
      resource = {"name":"some resource", "description":"some resource description", "tags":["tag1","tag2"]}
      postUrl = 'api/resources'
      r = requests.post(self.baseUrl + postUrl, data = resource, headers = self.headers)
      decoded = json.loads(r.text)
      print decoded

if __name__ == '__main__':
    unittest.main()

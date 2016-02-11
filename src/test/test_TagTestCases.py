import unittest
import json
import requests

class TagTestCases(unittest.TestCase):
  def setUp(self):
      self.baseUrl = 'https://localhost:443/'
      self.headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
      self.resetUrl = '/admin/init'
      self.tagUrl = 'api/tags'
      r = requests.get(self.baseUrl + self.resetUrl, headers = self.headers, verify = False)

  def test_GetInitialTags(self):      
      r = requests.get(self.baseUrl + self.tagUrl, headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False 
      assert decoded['data'] == {'tags': []}
      assert decoded['error_msg'] == 'Successfully retrieved tags'

  def test_GetTagsAfterPosting(self):
      resource = {"name":"some resource", "description":"some resource description", "tags":["tag1","tag2"]}
      postUrl = 'api/resources'
      r = requests.post(self.baseUrl + postUrl, data = json.dumps(resource), headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'description': u'some resource description', u'tags': [u'tag1', u'tag2'], u'name': u'some resource', u'resource_id': 1}
      assert decoded['error_msg'] == 'Successfully inserted resource'

if __name__ == '__main__':
    unittest.main()

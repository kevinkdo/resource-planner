import unittest
import json
import requests
import params

class TagTestCases(unittest.TestCase):
  def setUp(self): 
      r = requests.get(params.baseUrl + params.resetUrl, headers = params.headers, verify = False)

  def test_GetInitialTags(self):      
      r = requests.get(params.baseUrl + params.tagUrl, headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False 
      assert decoded['data'] == {'tags': []}
      assert decoded['error_msg'] == 'Successfully retrieved tags'

  def test_GetTagsAfterPosting(self):
      resource = {"name":"some resource", "description":"some resource description", "tags":["tag1","tag2"]}
      r = requests.post(params.baseUrl + params.resourcesUrl, data = json.dumps(resource), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'description': u'some resource description', u'tags': [u'tag1', u'tag2'], u'name': u'some resource', u'resource_id': 1}
      assert decoded['error_msg'] == 'Successfully inserted resource'

if __name__ == '__main__':
    unittest.main()

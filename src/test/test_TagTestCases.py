import unittest
import json
import requests
import params

#user id 1
adminHeaders =  {
  'Accept': 'application/json',
  "Authorization": "Bearer ",
  "Content-Type": "application/json" 
}

class TagTestCases(unittest.TestCase):
  def setUp(self): 
      r = requests.get(params.baseUrl + params.resetUrl, headers = params.headers, verify = False)
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithAdmin), headers = params.headers, verify = False)
      adminHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]

  def test_GetInitialTags(self):      
      r = requests.get(params.baseUrl + params.tagUrl, headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False 
      assert decoded['data'] == {'tags': []}
      assert decoded['error_msg'] == 'Successfully retrieved tags'

  def test_GetTagsAfterPosting(self):
      resource = {"name":"some resource", "description":"some resource description", "tags":["tag1","tag2"], "restricted": False, 'shared_count': 1, 'parent_id': 0}
      r = requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(resource), headers = adminHeaders, verify = False)
      r = requests.get(params.baseUrl + params.tagUrl, headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'tags': [u'tag1', u'tag2']}
      assert decoded['error_msg'] == 'Successfully retrieved tags'

if __name__ == '__main__':
    unittest.main()

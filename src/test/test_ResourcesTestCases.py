import unittest
import requests
import json

class AuthorizationTestCases(unittest.TestCase):
  def setUp(self):
      self.baseUrl = 'https://localhost:443/'
      self.headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
      self.resourceUrl = 'api/resources'
      self.resetUrl = '/admin/init'
      r = requests.get(self.baseUrl + self.resetUrl, headers = self.headers, verify = False)

  def test_CreateValidResourceNoTags(self):
      validResource = {'name':'some resource', 'description':'some resource description', 'tags':[]}
      r = requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource), headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'description': u'some resource description', u'tags': [], u'name': u'some resource', u'resource_id': 1}
      assert decoded['error_msg'] == 'Successfully inserted resource'

  def test_CreateValidResourceWithTags(self):
      validResource = {'name':'some resource', 'description':'some resource description', 'tags':['tag1', 'tag2']}
      r = requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource), headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'description': u'some resource description', u'tags': ['tag1', 'tag2'], u'name': u'some resource', u'resource_id': 1}
      assert decoded['error_msg'] == 'Successfully inserted resource'

  def test_CreateInvalidResourceNoName(self):
      validResource = {'name':'', 'description':'some resource description', 'tags':[]}
      r = requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource), headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource name required'

  def test_CreateInvalidResourceNoDescription(self):
      validResource = {'name':'some resource', 'description':'', 'tags':[]}
      r = requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource), headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'description': u'', u'tags': [], u'name': u'some resource', u'resource_id': 1}
      assert decoded['error_msg'] == 'Successfully inserted resource'

  def test_GetResourceWithValidID(self):
      validResource = {'name':'some resource', 'description':'some resource description', 'tags':['tag1', 'tag2']}
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource), headers = self.headers, verify = False)
      r = requests.get(self.baseUrl + self.resourceUrl + '/1', headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'description': u'some resource description', u'tags': ['tag1', 'tag2'], u'name': u'some resource', u'resource_id': 1}
      assert decoded['error_msg'] == 'Successfully retrieved resource'

  def test_GetResourceWithInvalidID(self):
      r = requests.get(self.baseUrl + self.resourceUrl + '/1', headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource does not exist'

  def test_GetResourcesWithValidQuery(self):
      validResource1 = {'name':'resource1', 'description':'resource description', 'tags':['tag1', 'tag2']}
      validResource2 = {'name':'resource2', 'description':'resource description', 'tags':['tag2']}
      validResource3 = {'name':'resource3', 'description':'resource description', 'tags':['tag2', 'tag3']}
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource1), headers = self.headers, verify = False)
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource2), headers = self.headers, verify = False)
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource3), headers = self.headers, verify = False)
      queryUrl = '/?required_tags=tag2,tag1&excluded_tags=tag3'
      r = requests.get(self.baseUrl + self.resourceUrl + queryUrl, headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'resources': [{u'description': u'resource description', u'tags': [u'tag1', u'tag2'], u'name': u'resource1', u'resource_id': 1}]}
      assert decoded['error_msg'] == 'Successfully retrieved resources'

  def test_GetResourcesWithQueryWithNonExistentTags(self):
      validResource1 = {'name':'resource1', 'description':'resource description', 'tags':['tag1', 'tag2']}
      validResource2 = {'name':'resource2', 'description':'resource description', 'tags':['tag2']}
      validResource3 = {'name':'resource3', 'description':'resource description', 'tags':['tag2', 'tag3']}
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource1), headers = self.headers, verify = False)
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource2), headers = self.headers, verify = False)
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource3), headers = self.headers, verify = False)
      queryUrl = '/?required_tags=tag4'
      r = requests.get(self.baseUrl + self.resourceUrl + queryUrl, headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'resources': []}
      assert decoded['error_msg'] == 'Successfully retrieved resources'

  def test_PutResourceWithValidIDAllFieldsUpdated(self):
      validResource1 = {'name':'resource1', 'description':'resource description', 'tags':['tag1', 'tag2']}
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource1), headers = self.headers, verify = False)
      update = {'name':'newname', 'description':'new description', 'tags':['tag3', 'tag4']}
      putUrl = '/1'
      r = requests.put(self.baseUrl + self.resourceUrl + putUrl, data = json.dumps(update), headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'description': u'new description', u'tags': [u'tag3', u'tag4'], u'name': u'newname', u'resource_id': 1}
      assert decoded['error_msg'] == 'Successfully updated resource'

  def test_PutResourceWithValidIDNoFieldsUpdated(self):
      validResource1 = {'name':'resource1', 'description':'resource description', 'tags':['tag1', 'tag2']}
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource1), headers = self.headers, verify = False)
      update = {'name':'resource1', 'description':'resource description', 'tags':['tag1', 'tag2']}
      putUrl = '/1'
      r = requests.put(self.baseUrl + self.resourceUrl + putUrl, data = json.dumps(update), headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'description': u'resource description', u'tags': [u'tag1', u'tag2'], u'name': u'resource1', u'resource_id': 1}
      assert decoded['error_msg'] == 'Successfully updated resource'

  def test_PutResourceWithInvalidID(self):
      validResource1 = {'name':'resource1', 'description':'resource description', 'tags':['tag1', 'tag2']}
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource1), headers = self.headers, verify = False)
      update = {'name':'resource1', 'description':'resource description', 'tags':['tag1', 'tag2']}
      putUrl = '/2'
      r = requests.put(self.baseUrl + self.resourceUrl + putUrl, data = json.dumps(update), headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource does not exist'

  def test_DeleteResourceWithValidID(self):
      validResource1 = {'name':'resource1', 'description':'resource description', 'tags':['tag1', 'tag2']}
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource1), headers = self.headers, verify = False)
      deleteUrl = '/1'
      r = requests.delete(self.baseUrl + self.resourceUrl + deleteUrl, headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'successfully deleted resource and all accompanying reservations'

  def test_DeleteResourceWithInvalidID(self):
      validResource1 = {'name':'resource1', 'description':'resource description', 'tags':['tag1', 'tag2']}
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource1), headers = self.headers, verify = False)
      deleteUrl = '/2'
      r = requests.delete(self.baseUrl + self.resourceUrl + deleteUrl, headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource does not exist'
      #change the error message if you try to delete a resource that's already deleted?

  def test_GetResourcesCanDeleteWithValidID(self):
      validResource1 = {'name':'resource1', 'description':'resource description', 'tags':['tag1', 'tag2']}
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource1), headers = self.headers, verify = False)
      canDeleteUrl = '/1/candelete'
      r = requests.get(self.baseUrl + self.resourceUrl + canDeleteUrl, headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'canDelete': True}
      assert decoded['error_msg'] == 'Successful retrieved canDelete status'
    
  def test_GetResourcesCanDeleteWithInvalidID(self):
      validResource1 = {'name':'resource1', 'description':'resource description', 'tags':['tag1', 'tag2']}
      requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource1), headers = self.headers, verify = False)
      canDeleteUrl = '/2/candelete'
      r = requests.get(self.baseUrl + self.resourceUrl + canDeleteUrl, headers = self.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'canDelete': True}
      assert decoded['error_msg'] == 'Successful retrieved canDelete status'
      #change the error message if you try to get candelete status of resource that doesn't exist

  def test_GetResourcesCanDeleteWithInvalidUserHeaders(self):
      userHeaders = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwidXNlcl9pZCI6MiwicGVybWlzc2lvbiI6MH0.5Br_NG0u_Kg2u11xyKqTGZAifyFHd19ca_HRRhu4j9Q", "Content-Type": "application/json" }
      canDeleteUrl = '/2/candelete'
      r = requests.get(self.baseUrl + self.resourceUrl + canDeleteUrl, headers = userHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      #print decoded


if __name__ == '__main__':
    unittest.main()

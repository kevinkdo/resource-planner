import unittest
import requests
import json
import params

#user id 1
adminHeaders =  {
  'Accept': 'application/json',
  "Authorization": "Bearer ",
  "Content-Type": "application/json" 
}
#userid 2
allHeaders = {
  'Accept': 'application/json',
  "Authorization": "Bearer ",
  "Content-Type": "application/json" 
}
#userid 3
noHeaders = {
  'Accept': 'application/json',
  "Authorization": "Bearer ",
  "Content-Type": "application/json" 
}

permissions = {
  "users":[{"user_id":2,"username":"all"}, {"user_id":3,"username":"none"}],
  "groups":[],
  "resources":[{"resource_id":1,"resource_name":"view"}, {"resource_id":2,"resource_name":"no"}],
  "system_permissions":{
    "user_permissions":[{"resource_p":True,"reservation_p":True,"user_p":True,"user_id":2}, {"resource_p":False,"reservation_p":False,"user_p":False,"user_id":3}],
    "group_permissions":[]
  },
  "resource_permissions":{
    "user_permissions":[{"resource_id":1,"permission_level":1,"user_id":2}, {"resource_id":2,"permission_level":0,"user_id":2}, {"resource_id":1,"permission_level":1,"user_id":3}, {"resource_id":2,"permission_level":0,"user_id":3}],
    "group_permissions":[]
  }
}
class ResourceTestCases(unittest.TestCase):
  def setUp(self):
      r = requests.get(params.baseUrl + params.resetUrl, headers = params.headers, verify = False)
      viewResource = {'name':'view', 'description':'', 'tags':[], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      noviewResource = {'name':'no', 'description':'', 'tags':[], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      r = requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(viewResource), headers = params.headers, verify = False)     
      r = requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(noviewResource), headers = params.headers, verify = False)     
      r = requests.post(params.baseUrl + params.userUrl, json.dumps(params.userWithAll), headers = params.headers, verify = False)
      r = requests.post(params.baseUrl + params.userUrl, json.dumps(params.userWithNone), headers = params.headers, verify = False)

      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithAdmin), headers = params.headers, verify = False)
      adminHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithAll), headers = params.headers, verify = False)
      allHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithNone), headers = params.headers, verify = False)
      noHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]

      r = requests.put(params.baseUrl + params.userUrl + "/1" + params.permissionsUrl, data = json.dumps(permissions), headers = adminHeaders, verify = False)   

  def test_CreateValidResourceNoTagsWithResourceManagementPermissions(self):
      resource = {'name':'test', 'description':'', 'tags':[], "restricted": False, 'shared_count': 1, 'parent_id': 0}
      r = requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(resource), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == u'Successfully inserted resource'

  def test_CreateValidResourceNoTagsWithoutResourceManagementPermissions(self):
      resource = {'name':'test', 'description':'', 'tags':[], "restricted": False, 'shared_count': 1, 'parent_id': 0 }
      r = requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(resource), headers = noHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == {u'name': u'test', u'tags': [], u'restricted': False, u'shared_count': 1, u'parent_id': 0, u'valid': True, u'validPut': True, u'description': u''}
      assert decoded['error_msg'] == 'You are not authorized.'

  def test_CreateInvalidResourceNoNameWithResourceManagementPermissions(self):
      resource = {'name':'', 'description':'', 'tags':[], "restricted": False, 'shared_count': 1, 'parent_id': 0}
      r = requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(resource), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource name required'

  def test_GetResourceWithValidIDWithResourceManagementPermissionsWithViewAccess(self):
      r = requests.get(params.baseUrl + params.resourceUrl + "/1", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': [], u'name': u'view'}
      assert decoded['error_msg'] == 'Successfully fetched resource'

  def test_GetResourceWithValidIDWithResourceManagementPermissionsWithoutViewAccess(self):
      r = requests.get(params.baseUrl + params.resourceUrl + "/2", headers = allHeaders, verify = False)
      decoded = r.json()   
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'description': u'', u'tags': [], u'resource_id': 2, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': [], u'name': u'no'}
      assert decoded['error_msg'] == 'Successfully fetched resource'

  def test_GetResourceWithValidIDWithoutResourceManagementPermissionsWithViewAccess(self):
      r = requests.get(params.baseUrl + params.resourceUrl + "/1", headers = noHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': False, u'parent_id': 0, u'children': [], u'name': u'view'}
      assert decoded['error_msg'] == 'Successfully fetched resource'

  def test_GetResourceWithValidIDWithoutResourceManagementPermissionsWithoutViewAccess(self):
      r = requests.get(params.baseUrl + params.resourceUrl + "/2", headers = noHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'description': u'Mystery', u'tags': [], u'resource_id': 0, u'restricted': False, u'shared_count': 0, u'can_view': False, u'can_reserve': False, u'parent_id': 0, u'children': [], u'name': u'Mystery'}
      assert decoded['error_msg'] == 'Successfully fetched resource'

  def test_GetResourceWithInvalidIDWithResourceManagementPermissionsWithViewAccess(self):
      r = requests.get(params.baseUrl + params.resourceUrl + "/10", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource does not exist'

  def test_GetResourcesWithValidQueryWithResourceManagementPermissionsWithViewAccess(self):
      validResource1 = {'name':'resource1', 'description':'resource description', 'tags':['tag1', 'tag2'], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      validResource2 = {'name':'resource2', 'description':'resource description', 'tags':['tag2'], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      validResource3 = {'name':'resource3', 'description':'resource description', 'tags':['tag2', 'tag3'], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(validResource1), headers = allHeaders, verify = False)
      requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(validResource2), headers = allHeaders, verify = False)
      requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(validResource3), headers = allHeaders, verify = False)
      queryUrl = '/?required_tags=tag2,tag1&excluded_tags=tag3'
      r = requests.get(params.baseUrl + params.resourceUrl + queryUrl, headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'resources': [{u'description': u'resource description', u'tags': [u'tag1', u'tag2'], u'resource_id': 3, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': None, u'name': u'resource1'}]}
      assert decoded['error_msg'] == 'Successfully retrieved resources'

  def test_GetResourcesWithQueryWithNonExistentTagsWithResourceManagementPermissionsWithViewAccess(self):
      validResource1 = {'name':'resource1', 'description':'resource description', 'tags':['tag1', 'tag2'], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      validResource2 = {'name':'resource2', 'description':'resource description', 'tags':['tag2'], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      validResource3 = {'name':'resource3', 'description':'resource description', 'tags':['tag2', 'tag3'], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(validResource1), headers = params.headers, verify = False)
      requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(validResource2), headers = params.headers, verify = False)
      requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(validResource3), headers = params.headers, verify = False)
      queryUrl = '/?required_tags=tag4'
      r = requests.get(params.baseUrl + params.resourceUrl + queryUrl, headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'resources': []}
      assert decoded['error_msg'] == 'Successfully retrieved resources'

  def test_PutResourceWithValidIDAllFieldsUpdatedWithResourceManagementPermissionsWithViewAccess(self):
      update = {'name':'newname', 'description':'new description', 'tags':['tag3', 'tag4'], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      r = requests.put(params.baseUrl + params.resourceUrl + '/1', data = json.dumps(update), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Successfully updated resource'

  def test_PutResourceWithValidIDAllFieldsUpdatedWithResourceManagementPermissionsWithoutViewAccess(self):
      update = {'name':'newname', 'description':'new description', 'tags':['tag3', 'tag4'], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      r = requests.put(params.baseUrl + params.resourceUrl + '/2', data = json.dumps(update), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] ==  'Successfully updated resource'

  def test_PutResourceWithValidIDAllFieldsUpdatedWithoutResourceManagementPermissionsWithViewAccess(self):
      update = {'name':'newname', 'description':'new description', 'tags':['tag3', 'tag4'], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      r = requests.put(params.baseUrl + params.resourceUrl + '/1', data = json.dumps(update), headers = noHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == {u'name': u'newname', u'tags': [u'tag3', u'tag4'], u'restricted': False, u'shared_count': 1, u'parent_id': 0, u'valid': True, u'validPut': True, u'description': u'new description'}
      assert decoded['error_msg'] == 'You are not authorized'

  def test_PutResourceWithValidIDAllFieldsUpdatedWithoutResourceManagementPermissionsWithoutViewAccess(self):
      update = {'name':'newname', 'description':'new description', 'tags':['tag3', 'tag4'], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      r = requests.put(params.baseUrl + params.resourceUrl + '/2', data = json.dumps(update), headers = noHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == {u'name': u'newname', u'tags': [u'tag3', u'tag4'], u'restricted': False, u'shared_count': 1, u'parent_id': 0, u'valid': True, u'validPut': True, u'description': u'new description'}
      assert decoded['error_msg'] == 'You are not authorized'

  def test_PutResourceWithInvalidIDWithResourceManagementPermissionsWithViewAccess(self):
      update = {'name':'newname', 'description':'new description', 'tags':['tag3', 'tag4'], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      r = requests.put(params.baseUrl + params.resourceUrl + '/10', data = json.dumps(update), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource does not exist'

  def test_DeleteResourceWithValidIDWithResourceManagementPermissionsWithViewAccess(self):
      r = requests.delete(params.baseUrl + params.resourceUrl + "/1", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'successfully deleted resource and all accompanying reservations'

  def test_DeleteResourceWithValidIDWithResourceManagementPermissionsWithoutViewAccess(self):
      r = requests.delete(params.baseUrl + params.resourceUrl + "/2", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'successfully deleted resource and all accompanying reservations'

  def test_DeleteResourceWithValidIDWithoutResourceManagementPermissionsWithViewAccess(self):
      r = requests.delete(params.baseUrl + params.resourceUrl + "/1", headers = noHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'You are not authorized'    

  def test_DeleteResourceWithValidIDWithoutResourceManagementPermissionsWithoutViewAccess(self):
      r = requests.delete(params.baseUrl + params.resourceUrl + "/2", headers = noHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'You are not authorized'

  def test_DeleteResourceWithInvalidIDWithResourceManagementPermissionsWithViewAccess(self):
      r = requests.delete(params.baseUrl + params.resourceUrl + "/10", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource does not exist'

  def test_GetResourcesCanDeleteWithValidIDWithResourceManagementPermissionsWithViewAccess(self):
      r = requests.get(params.baseUrl + params.resourceUrl + "/1/candelete", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'canDelete': True}
      assert decoded['error_msg'] == 'Successful retrieved canDelete status'

  def test_GetResourcesCanDeleteWithValidIDWithResourceManagementPermissionsWithoutViewAccess(self):
      r = requests.get(params.baseUrl + params.resourceUrl + "/2/candelete", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'canDelete': True}
      assert decoded['error_msg'] == 'Successful retrieved canDelete status'    

  def test_GetResourcesCanDeleteWithValidIDWithoutResourceManagementPermissionsWithViewAccess(self):
      r = requests.get(params.baseUrl + params.resourceUrl + "/1/candelete", headers = noHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'You are not authorized'

  def test_GetResourcesCanDeleteWithValidIDWithoutResourceManagementPermissionsWithoutViewAccess(self):
      r = requests.get(params.baseUrl + params.resourceUrl + "/2/candelete", headers = noHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'You are not authorized'

  def test_GetResourcesCanDeleteWithInvalidIDWithResourceManagementPermissionsWithViewAccess(self):
      r = requests.get(params.baseUrl + params.resourceUrl + "/10/candelete", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource does not exist'
      #change the error message if you try to get candelete status of resource that doesn't exist

  def test_GetResourceForest(self):
      r = requests.get(params.baseUrl + params.resourceUrl + "/forest", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'resources': [{u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': [], u'name': u'view'}, {u'description': u'', u'tags': [], u'resource_id': 2, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': [], u'name': u'no'}]}
      assert decoded['error_msg'] == 'Successfully retrieved resource forest'
 

if __name__ == '__main__':
    unittest.main()

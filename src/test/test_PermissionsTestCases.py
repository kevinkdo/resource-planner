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
allMPHeaders = {
  'Accept': 'application/json',
  "Authorization": "Bearer ",
  "Content-Type": "application/json" 
}
#userid 3
RSMPHeaders = {
  'Accept': 'application/json',
  "Authorization": "Bearer ",
  "Content-Type": "application/json" 
}

permissions = {
  "users":[{"user_id":2,"username":"all"}, {"user_id":3,"username":"resource"}],
  "groups":[],
  "resources":[],
  "system_permissions":{
    "user_permissions":[{"resource_p":True,"reservation_p":True,"user_p":True,"user_id":2}, {"resource_p":True,"reservation_p":False,"user_p":False,"user_id":3}],
    "group_permissions":[]
  },
  "resource_permissions":{
    "user_permissions":[],
    "group_permissions":[]
  }
}

class PermissionsTestCases(unittest.TestCase):
  def setUp(self):
    requests.get(params.baseUrl + params.resetUrl, headers = params.headers, verify = False)
    r = requests.post(params.baseUrl + params.userUrl, json.dumps(params.userWithAll), headers = params.headers, verify = False)
    r = requests.post(params.baseUrl + params.userUrl, json.dumps(params.userWithResource), headers = params.headers, verify = False)
    r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithAdmin), headers = params.headers, verify = False)
    adminHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]
    r = requests.put(params.baseUrl + params.userUrl + "/1" + params.permissionsUrl, data = json.dumps(permissions), headers = adminHeaders, verify = False)
    r = requests.get(params.baseUrl + params.userUrl + "/1" + params.permissionsUrl, headers = adminHeaders, verify = False)
    r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithAll), headers = params.headers, verify = False)
    allMPHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]
    r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithResource), headers = params.headers, verify = False)
    RSMPHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]

  def test_GetUserPermissionsWithValidIDWithUserManagementPermissions(self):
    r = requests.get(params.baseUrl + params.userUrl + "/2" + params.permissionsUrl, headers = allMPHeaders, verify = False)
    decoded = r.json()
    assert decoded['is_error'] == False
    assert decoded['data'] == {u'resource_permissions': {u'user_permissions': [], u'group_permissions': []}, u'resources': [], u'system_permissions': {u'user_permissions': [{u'user_p': True, u'resource_p': True, u'user_id': 2, u'reservation_p': True}, {u'user_p': False, u'resource_p': True, u'user_id': 3, u'reservation_p': False}], u'group_permissions': []}, u'users': [{u'username': u'all', u'user_id': 2}, {u'username': u'resource', u'user_id': 3}], u'groups': []}
    assert decoded['error_msg'] == u'Permissions retrieved'
  
  def test_GetUserPermissionsWithValidIDWithoutUserManagementPermissions(self):
    r = requests.get(params.baseUrl + params.userUrl + "/3" + params.permissionsUrl, headers = RSMPHeaders, verify = False)
    decoded = r.json()
    assert decoded['is_error'] == False
    assert decoded['data'] == {u'resource_permissions': {u'user_permissions': [], u'group_permissions': []}, u'resources': [], u'system_permissions': {u'user_permissions': [], u'group_permissions': []}, u'users': [{u'username': u'all', u'user_id': 2}, {u'username': u'resource', u'user_id': 3}], u'groups': []}
    assert decoded['error_msg'] == u'Permissions retrieved'

  def test_GetUserPermissionsWithInvalidIDWithUserManagementPermissions(self):
    r = requests.get(params.baseUrl + params.userUrl + "/4" + params.permissionsUrl, headers = allMPHeaders, verify = False)
    decoded = r.json()
    assert decoded['is_error'] == False
    assert decoded['data'] == None
    assert decoded['error_msg'] == 'Requester ID does not match URL parameter ID'

  def test_SetUserPermissionsWithValidIDWithUserManagementPermissions(self):
    r = requests.put(params.baseUrl + params.userUrl + "/2" + params.permissionsUrl, data = json.dumps(permissions), headers = allMPHeaders, verify = False)
    decoded = r.json()
    assert decoded['is_error'] == False
    assert decoded['data'] == None
    assert decoded['error_msg'] == u'Permissions updated'

  def test_SetUserPermissionsWithValidIDWithoutUserManagementPermissions(self):
    r = requests.put(params.baseUrl + params.userUrl + "/3" + params.permissionsUrl, data = json.dumps(permissions), headers = RSMPHeaders, verify = False)
    decoded = r.json()
    assert decoded['is_error'] == True
    assert decoded['data'] == None
    assert decoded['error_msg'] == 'Trying to edit system permissions without user_p'

  def test_SetUserPermissionsWithInvalidIDWithUserManagementPermissions(self):
    r = requests.put(params.baseUrl + params.userUrl + "/20" + params.permissionsUrl, data = json.dumps(permissions), headers = allMPHeaders, verify = False)
    decoded = r.json()
    assert decoded['is_error'] == False
    assert decoded['data'] == None
    assert decoded['error_msg'] == 'Requester ID does not match URL parameter ID'

if __name__ == '__main__':
    unittest.main()



import unittest
import requests
import json
import params
# user with ump 
# user with no permissions
# group with no permissions
# group with all permissions

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

#userid 4
nogroupsHeaders = {
  'Accept': 'application/json',
  "Authorization": "Bearer ",
  "Content-Type": "application/json" 
}

permissions = {
  "users":[{"user_id":2,"username":"all"}, {"user_id":3,"username":"resource"}, {"user_id":4, "username":"no"}],
  "groups":[{"group_id":1,"group_name":"allgroup"}, {"group_id":2,"group_name":"nogroup"}],
  "resources":[{"resource_id":1,"resource_name":"reserveaccess"}],
  "system_permissions":{
    "user_permissions":[{"resource_p":True,"reservation_p":True,"user_p":True,"user_id":2}, {"resource_p":False,"reservation_p":False,"user_p":False,"user_id":3}, {"resource_p":False,"reservation_p":False,"user_p":False,"user_id":4}],
    "group_permissions":[{"resource_p":True,"reservation_p":True,"user_p":True,"group_id":1}, {"resource_p":False,"reservation_p":False,"user_p":False,"group_id":2}]
  },
  "resource_permissions":{
    "user_permissions":[{"resource_id":1,"permission_level":2,"user_id":2}, {"resource_id":1,"permission_level":2,"user_id":3}, {"resource_id":1,"permission_level":2,"user_id":4}],
    "group_permissions":[{"resource_id":1,"permission_level":2,"group_id":1}, {"resource_id":1,"permission_level":2,"group_id":2}]
  }
}
class GroupsTestCases(unittest.TestCase):
  def setUp(self):
      requests.get(params.baseUrl + params.resetUrl, headers = params.headers, verify = False)  
      reserveAcessResource = {'name':'reserveaccess', 'description':'', 'tags':[], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      r = requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(reserveAcessResource), headers = params.headers, verify = False) 
      r = requests.post(params.baseUrl + params.userUrl, json.dumps(params.userWithAll), headers = params.headers, verify = False)
      r = requests.post(params.baseUrl + params.userUrl, json.dumps(params.userWithResource), headers = params.headers, verify = False)
      r = requests.post(params.baseUrl + params.userUrl, json.dumps(params.userWithNone), headers = params.headers, verify = False)

      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithAdmin), headers = params.headers, verify = False)
      adminHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithAll), headers = params.headers, verify = False)
      allHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithResource), headers = params.headers, verify = False)
      noHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithNone), headers = params.headers, verify = False)
      nogroupsHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]

      group1 = {"group_name":"allgroup", "user_ids":[2,3]}
      group2 = {"group_name":"nogroup", "user_ids":[2,3]}
      r = requests.post(params.baseUrl + params.groupUrl, data = json.dumps(group1), headers = adminHeaders, verify = False)
      r = requests.post(params.baseUrl + params.groupUrl, data = json.dumps(group2), headers = adminHeaders, verify = False)
      r = requests.put(params.baseUrl + params.userUrl + "/1" + params.permissionsUrl, data = json.dumps(permissions), headers = adminHeaders, verify = False)    

  def test_CreateValidGroupWithUserManagementPermissions(self):
      group = {"group_name":"test", "user_ids":[]} 
      r = requests.post(params.baseUrl + params.groupUrl, data = json.dumps(group), headers = adminHeaders, verify = False)
      decoded = r.json()
      assert decoded["is_error"] == False
      assert decoded["data"] == {u'user_ids': [], u'group_id': 3, u'group_name': u'test'}
      assert decoded["error_msg"] == 'Successfully created group.'

  def test_CreateValidGroupWithoutUserManagementPermissions(self):
      group = {"group_name":"test", "user_ids":[]} 
      r = requests.post(params.baseUrl + params.groupUrl, data = json.dumps(group), headers = nogroupsHeaders, verify = False)
      decoded = r.json()
      assert decoded["is_error"] == True
      assert decoded["data"] == {u'user_ids': [], u'valid': True, u'group_name': u'test'}
      assert decoded["error_msg"] == 'You are not authorized.'

  def test_GetGroupsWithUserManagementPermissions(self):
      r = requests.get(params.baseUrl + params.groupUrl, headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded["is_error"] == False
      assert decoded["data"] == [{u'group_id': 1, u'group_name': u'allgroup'}, {u'group_id': 2, u'group_name': u'nogroup'}]
      assert decoded["error_msg"] == 'Successfully retrieved groups'

  def test_GetGroupsWithoutUserManagementPermissions(self):
      r = requests.get(params.baseUrl + params.groupUrl, headers = nogroupsHeaders, verify = False)
      decoded = r.json()
      assert decoded["is_error"] == False
      assert decoded["data"] == [{u'group_id': 1, u'group_name': u'allgroup'}, {u'group_id': 2, u'group_name': u'nogroup'}]
      assert decoded["error_msg"] == 'Successfully retrieved groups' 

  def test_GetGroupsByValidIDWithUserManagementPermissions(self):
      r = requests.get(params.baseUrl + params.groupUrl + "/1", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded["is_error"] == False
      assert decoded["data"] == {u'user_ids': [2, 3], u'group_id': 1, u'group_name': u'allgroup'}
      assert decoded["error_msg"] == 'Successfully retrieved group' 

  def test_GetGroupsByInvalidIDWithUserManagementPermissions(self):
      r = requests.get(params.baseUrl + params.groupUrl + "/10", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded["is_error"] == True
      assert decoded["data"] == None
      assert decoded["error_msg"] == 'Group does not exist' 

  def test_PutGroupWithValidIDWithUserManagementPermissions(self):
      update = {"group_name":"all", "user_ids":[2]}
      r = requests.put(params.baseUrl + params.groupUrl + "/1", data = json.dumps(update), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded["is_error"] == False
      assert decoded["data"] == None
      assert decoded["error_msg"] == 'Successfully updated group'

  def test_PutGroupWithValidIDWithoutUserManagementPermissions(self):
      update = {"group_name":"all", "user_ids":[2]}
      r = requests.put(params.baseUrl + params.groupUrl + "/1", data = json.dumps(update), headers = nogroupsHeaders, verify = False)
      decoded = r.json()
      assert decoded["is_error"] == True
      assert decoded["data"] == {u'user_ids': [2], u'valid': True, u'group_name': u'all'}
      assert decoded["error_msg"] == 'You are not authorized'

  def test_DeleteGroupWithValidIDWithUserManagementPermissions(self):
      r = requests.delete(params.baseUrl + params.groupUrl + "/1", headers = allHeaders, verify = False)     
      decoded = r.json()
      assert decoded["is_error"] == False
      assert decoded["data"] == None
      assert decoded["error_msg"] == "Successfully deleted group"

  def test_DeleteGroupWithValidIDWithoutUserManagementPermissions(self):
      r = requests.delete(params.baseUrl + params.groupUrl + "/1", headers = nogroupsHeaders, verify = False)     
      decoded = r.json()
      assert decoded["is_error"] == True
      assert decoded["data"] == None
      assert decoded["error_msg"] == "You are not authorized"

  def test_DeleteGroupWithInvalidIDWithUserManagementPermissions(self):
      r = requests.delete(params.baseUrl + params.groupUrl + "/30", headers = allHeaders, verify = False)     
      decoded = r.json()
      assert decoded["is_error"] == True
      assert decoded["data"] == None
      assert decoded["error_msg"] == "Group does not exist"

  def test_UserHasLowerReservationManagementPermissionsThanGroup(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = noHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'description': u'none', u'title': u'none', u'user': {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}, u'complete': True, u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'resources': [{u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 0, u'can_view': False, u'can_reserve': False, u'parent_id': 0, u'children': None, u'name': u'reserveaccess'}], u'end_time': u'2011-08-06T11:00:00.000Z'}
      assert decoded['error_msg'] == 'Reservation inserted successfully'

  def test_UserHasHigherReservationManagementPermissionsThanGroup(self):
      reservation = {"title": "none", "description": "none",  "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'description': u'none', u'title': u'none', u'user': {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}, u'complete': True, u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'resources': [{u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 0, u'can_view': False, u'can_reserve': False, u'parent_id': 0, u'children': None, u'name': u'reserveaccess'}], u'end_time': u'2011-08-06T11:00:00.000Z'}
      assert decoded['error_msg'] == 'Reservation inserted successfully'

  def test_UserHasLowerResourceManagementPermissionsThanGroup(self):
      r = requests.delete(params.baseUrl + params.resourceUrl + "/1", headers = noHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'successfully deleted resource and all accompanying reservations'     

  def test_UserHasHigherResourceManagementPermissionsThanGroup(self):
      r = requests.delete(params.baseUrl + params.resourceUrl + "/1", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'successfully deleted resource and all accompanying reservations' 

  def test_UserHasLowerUserManagementPermissionsThanGroup(self):
      group = {"group_name":"test", "user_ids":[]} 
      r = requests.post(params.baseUrl + params.groupUrl, data = json.dumps(group), headers = noHeaders, verify = False)
      decoded = r.json()
      assert decoded["is_error"] == False
      assert decoded["data"] == {u'user_ids': [], u'group_id': 3, u'group_name': u'test'}
      assert decoded["error_msg"] == 'Successfully created group.'

  def test_UserHasHigherUserManagementPermissionsThanGroup(self):
      group = {"group_name":"test", "user_ids":[]} 
      r = requests.post(params.baseUrl + params.groupUrl, data = json.dumps(group), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded["is_error"] == False
      assert decoded["data"] == {u'user_ids': [], u'group_id': 3, u'group_name': u'test'}
      assert decoded["error_msg"] == 'Successfully created group.'

if __name__ == '__main__':
    unittest.main()

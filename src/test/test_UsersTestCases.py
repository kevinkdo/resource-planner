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

class UsersTestCases(unittest.TestCase):
  def setUp(self):
      r = requests.get(params.baseUrl + params.resetUrl, headers = params.headers, verify = False)
      r = requests.post(params.baseUrl + params.userUrl, json.dumps(params.userWithAll), headers = params.headers, verify = False)
      r = requests.post(params.baseUrl + params.userUrl, json.dumps(params.userWithResource), headers = params.headers, verify = False)
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithAdmin), headers = params.headers, verify = False)
      adminHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]

      r = requests.put(params.baseUrl + params.userUrl + "/1" + params.permissionsUrl, data = json.dumps(permissions), headers = adminHeaders, verify = False)
      r = requests.get(params.baseUrl + params.userUrl + "/1" + params.permissionsUrl, headers = adminHeaders, verify = False)
      
      
  def test_CreateValidUserAsAdmin(self): 
      newUser = {"email":"newuser@admin.com", "password":"password", "should_email":'false', "username":"newuser"}
      r = requests.post(params.baseUrl + params.userUrl, data = json.dumps(newUser), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False 
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Successfully registered.' 

  # def test_CreateValidUserNotAsAdmin(self):
  #     newUser = {"email":"newuser@admin.com", "password":"password", "should_email":'false', "username":"newuser"}
  #     validUser = {'email':'user13@admin.com', 'username':'someusername', 'password':'something'}
  #     r = requests.post(params.baseUrl + params.registerUrl, data = json.dumps(validUser), headers = params.headers, verify = False)
  #     r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(validUser), headers = params.headers, verify = False)
  #     nonAdminHeader = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwidXNlcl9pZCI6MiwicGVybWlzc2lvbiI6MH0.5Br_NG0u_Kg2u11xyKqTGZAifyFHd19ca_HRRhu4j9Q", "Content-Type": "application/json" }
  #     r = requests.post(params.baseUrl + params.userUrl, data = json.dumps(newUser), headers = nonAdminHeader, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == True
  #     assert decoded['data'] == None
  #     assert decoded['error_msg'] == 'You are not authorized'

  def test_CreateInvalidUserWithPreexistingUsername(self):
      newUser = {"email":"new@admin.com", "password":"password", "should_email":'false', "username":"admin"}
      r = requests.post(params.baseUrl + params.userUrl, data = json.dumps(newUser), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True 
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Username already exists' 

  def test_CreateInvalidUserWithPreexistingEmail(self):
      newUser = {"email":"admin@admin.com", "password":"password", "should_email":'false', "username":"new"}
      r = requests.post(params.baseUrl + params.userUrl, data = json.dumps(newUser), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True 
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Email already exists'

  #def test_GetUserWithQuery(self): not needed for current evolution

  def test_GetUserWithValidIDWithUserManagementPermissions(self):
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithAll), headers = params.headers, verify = False)
      allMPHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]
      r = requests.get(params.baseUrl + params.userUrl + "/2", headers = allMPHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}
      assert decoded['error_msg'] == u'Successfully retrieved user'

  def test_GetUserWithValidIDWithoutUserManagementPermissions(self):
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithResource), headers = params.headers, verify = False)
      RSMPHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]
      r = requests.get(params.baseUrl + params.userUrl + "/1", headers = RSMPHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True 
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'You are not authorized'

  def test_GetUserWithInvalidIDWithUserManagementPermissions(self):
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithAll), headers = params.headers, verify = False)
      allMPHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]
      r = requests.get(params.baseUrl + params.userUrl + "/20", headers = allMPHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'User not found'

  #def test_PutUserWithValidIDUpdateUsernameWithUniqueUsername(self): disabled
  #def test_PutUserWithValidIDUpdateUsernameWithPreexistingUsername(self): disabled
  #def test_PutUserWithValidIDUpdateNoFields(self): breaks if you try to update something other than should_email or leave it empty

  def test_PutUserWithValidIDUpdateShouldEmail(self):
      update = {'should_email': 'True'}
      r = requests.put(params.baseUrl + params.userUrl +'/1', data = json.dumps(update), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True}
      assert decoded['error_msg'] == 'Successfully updated email settings'    
  
  def test_PutUserWithInvalidID(self):
      update = {'should_email': 'True'}
      r = requests.put(params.baseUrl + params.userUrl +'/20', data = json.dumps(update), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'User not found'

  # def test_PutUserNotAsAdmin(self):
  #     update = {'should_email':'true'}
  #     validUser = {'email':'user13@admin.com', 'username':'someusername', 'password':'something'}
  #     r = requests.post(params.baseUrl + params.registerUrl, data = json.dumps(validUser), headers = params.headers, verify = False)
  #     r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(validUser), headers = params.headers, verify = False)
  #     nonAdminHeader = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwidXNlcl9pZCI6MiwicGVybWlzc2lvbiI6MH0.5Br_NG0u_Kg2u11xyKqTGZAifyFHd19ca_HRRhu4j9Q", "Content-Type": "application/json" }
  #     r = requests.put(params.baseUrl + params.userUrl + '/1', data = json.dumps(update), headers = nonAdminHeader, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == True
  #     assert decoded['data'] == None
  #     assert decoded['error_msg'] == 'You are not authorized'
        
  def test_DeleteUserWithValidID(self):
      validUser = {'email':'user13@admin.com', 'username':'someusername', 'password':'something'}
      r = requests.post(params.baseUrl + params.registerUrl, data = json.dumps(validUser), headers = params.headers, verify = False)
      r = requests.delete(params.baseUrl + params.userUrl + '/2', headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Successfully deleted user'

  def test_DeleteUserWithInvalidID(self): #not sure if desired behavior but can delete 'nonexisting' users (invalid user IDs)
      r = requests.delete(params.baseUrl + params.userUrl + '/2', headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Successfully deleted user' 
      
if __name__ == '__main__':
    unittest.main()

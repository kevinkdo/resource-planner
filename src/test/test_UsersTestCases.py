import unittest
import requests
import json
import params

class UsersTestCases(unittest.TestCase):
  def setUp(self):
      r = requests.get(params.baseUrl + params.resetUrl, headers = params.headers, verify = False)
      
  # def test_CreateValidUserAsAdmin(self): #not sure how to check if admin
  #     newUser = {"email":"newuser@admin.com", "password":"password", "should_email":'false', "username":"newuser"}
  #     r = requests.post(params.baseUrl + params.userUrl, data = json.dumps(newUser), headers = params.headers, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == False 
  #     assert decoded['data'] == {u'user_id': 2,  u'username': u'newuser', u'should_email': False, u'email': u'newuser@admin.com'}
  #     assert decoded['error_msg'] == 'Successfully registered.' 

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

  # def test_CreateInvalidUserWithPreexistingUsername(self):
  #     newUser = {"email":"new@admin.com", "password":"password", "should_email":'false', "username":"admin"}
  #     r = requests.post(params.baseUrl + params.userUrl, data = json.dumps(newUser), headers = params.headers, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == True 
  #     assert decoded['data'] == None
  #     assert decoded['error_msg'] == 'Username already exists' 

  # def test_CreateInvalidUserWithPreexistingEmail(self):
  #     newUser = {"email":"admin@admin.com", "password":"password", "should_email":'false', "username":"new"}
  #     r = requests.post(params.baseUrl + params.userUrl, data = json.dumps(newUser), headers = params.headers, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == True 
  #     assert decoded['data'] == None
  #     assert decoded['error_msg'] == 'Email already exists'

  def test_GetUserWithValidID(self):
      r = requests.get(params.baseUrl + params.userUrl + '/1', headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'username': u'admin', u'should_email': False, u'user_id': 1, u'email': u'admin@admin.com'}
      assert decoded['error_msg'] == 'Successfully retrieved user'

  def test_GetUserWithInvalidID(self):
      r = requests.get(params.baseUrl + params.userUrl + '/2', headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'User not found'   

  #def test_GetUserWithQuery(self): not needed for current evolution
  #def test_PutUserWithValidIDUpdateUsernameWithUniqueUsername(self): disabled
  #def test_PutUserWithValidIDUpdateUsernameWithPreexistingUsername(self): disabled
  #def test_PutUserWithValidIDUpdateNoFields(self): breaks if you try to update something other than should_email or leave it empty

  # def test_PutUserWithValidIDUpdateShouldEmail(self):
  #     update = {'should_email': 'True'}
  #     r = requests.put(params.baseUrl + params.userUrl +'/1', data = json.dumps(update), headers = params.headers, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == False
  #     assert decoded['data'] == {u'should_email': True}
  #     assert decoded['error_msg'] == 'Successfully updated email settings'    
  
  # def test_PutUserWithInvalidID(self):
  #     update = {'should_email': 'True'}
  #     r = requests.put(params.baseUrl + params.userUrl +'/2', data = json.dumps(update), headers = params.headers, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == True
  #     assert decoded['data'] == None
  #     assert decoded['error_msg'] == 'User not found'

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
        
  # def test_DeleteUserWithValidID(self):
  #     validUser = {'email':'user13@admin.com', 'username':'someusername', 'password':'something'}
  #     r = requests.post(params.baseUrl + params.registerUrl, data = json.dumps(validUser), headers = params.headers, verify = False)
  #     r = requests.delete(params.baseUrl + params.userUrl + '/2', headers = params.headers, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == False
  #     assert decoded['data'] == None
  #     assert decoded['error_msg'] == 'Successfully deleted user'

  # def test_DeleteUserWithInvalidID(self): #not sure if desired behavior but can delete 'nonexisting' users (invalid user IDs)
  #     r = requests.delete(params.baseUrl + params.userUrl + '/2', headers = params.headers, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == False
  #     assert decoded['data'] == None
  #     assert decoded['error_msg'] == 'Successfully deleted user' 
      
if __name__ == '__main__':
    unittest.main()

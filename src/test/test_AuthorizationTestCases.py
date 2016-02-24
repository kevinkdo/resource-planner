import unittest
import requests
import json
import params

class AuthorizationTestCases(unittest.TestCase):
  def setUp(self):
      r = requests.get(params.baseUrl + params.resetUrl, headers = params.headers, verify = False)

  def test_RegisterValidUser(self):
      validUser = {'email':'user13@admin.com', 'username':'someusername', 'password':'something', 'should_email':'False'}
      r = requests.post(params.baseUrl + params.registerUrl, data = json.dumps(validUser), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Successfully registered'

  def test_RegisterInValidUserWithEmailAlreadyTaken(self):
      validUser = {'email':'admin@admin.com', 'username':'someusername', 'password':'something', 'should_email':'False'}
      r = requests.post(params.baseUrl + params.registerUrl, data = json.dumps(validUser), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Email already exists'
  
  def test_RegisterInValidUserNoPassword(self):
      validUser = {'email':'user13@admin.com', 'username':'someusername', 'password':'', 'should_email':'False'}
      r = requests.post(params.baseUrl + params.registerUrl, data = json.dumps(validUser), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Password must be between 1 and 250 characters long'

  def test_LoginValidUser(self):
      userLogin = {'email':'admin@admin.com', 'username':'admin', 'password':'admin'}
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(userLogin), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {"token":'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI', u'userId': 1}
      assert decoded['error_msg'] == 'Successfully authenticated'

  def test_LoginInvalidUserWrongPassword(self):
      userLogin = {'email':'admin@admin.com', 'username':'admin', 'password':'wrongpassword'}
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(userLogin), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Failed to validate password'

  def test_LoginInvalidUserInvalidEmail(self):
      userLogin = {'email':'admin123@admin.com', 'username':'admin', 'password':'admin'}
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(userLogin), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {"token":'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI', u'userId': 1}
      assert decoded['error_msg'] == 'Successfully authenticated'

  def test_LoginInvalidUserInvalidUsername(self):
      userLogin = {'email':'admin@admin.com', 'username':'admin123', 'password':'admin'}
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(userLogin), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {"token":'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI', u'userId': 1}
      assert decoded['error_msg'] == 'Successfully authenticated'

if __name__ == '__main__':
    unittest.main()

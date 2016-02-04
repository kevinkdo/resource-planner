import unittest
import requests
import json

class AuthorizationTestCases(unittest.TestCase):
  def setUp(self):
      self.baseUrl = 'http://localhost:8080/'
      self.headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
      self.registerUrl = 'auth/register'
      self.loginUrl = 'auth/login'
      self.resetUrl = '/admin/init'
      r = requests.get(self.baseUrl + self.resetUrl, headers = self.headers)

  def test_RegisterValidUser(self):
      validUser = {'email':'user13@admin.com', 'username':'someusername', 'password':'something'}
      r = requests.post(self.baseUrl + self.registerUrl, data = json.dumps(validUser), headers = self.headers)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Successfully registered'

  def test_RegisterInValidUserWithEmailAlreadyTaken(self):
      validUser = {'email':'admin@admin.com', 'username':'someusername', 'password':'something'}
      r = requests.post(self.baseUrl + self.registerUrl, data = json.dumps(validUser), headers = self.headers)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Email already exists'
  
  def test_RegisterInValidUserNoPassword(self):
      validUser = {'email':'user13@admin.com', 'username':'someusername', 'password':''}
      r = requests.post(self.baseUrl + self.registerUrl, data = json.dumps(validUser), headers = self.headers)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'something about need a password'
      #update with jiawei's message about min password length

  def test_LoginValidUser(self):
      userLogin = {'email':'admin@admin.com', 'username':'admin', 'password':'admin'}
      r = requests.post(self.baseUrl + self.loginUrl, data = json.dumps(userLogin), headers = self.headers)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {"token":'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI', u'userId': 1}
      assert decoded['error_msg'] == 'Successfully authenticated'

  def test_LoginInvalidUserWrongPassword(self):
      userLogin = {'email':'admin@admin.com', 'username':'admin', 'password':'wrongpassword'}
      r = requests.post(self.baseUrl + self.loginUrl, data = json.dumps(userLogin), headers = self.headers)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Failed to validate password'

  def test_LoginInvalidUserInvalidEmail(self):
      userLogin = {'email':'admin123@admin.com', 'username':'admin', 'password':'admin'}
      r = requests.post(self.baseUrl + self.loginUrl, data = json.dumps(userLogin), headers = self.headers)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {"token":'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI', u'userId': 1}
      assert decoded['error_msg'] == 'Successfully authenticated'

  def test_LoginInvalidUserInvalidUsername(self):
      userLogin = {'email':'admin@admin.com', 'username':'admin123', 'password':'admin'}
      r = requests.post(self.baseUrl + self.loginUrl, data = json.dumps(userLogin), headers = self.headers)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {"token":'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI', u'userId': 1}
      assert decoded['error_msg'] == 'Successfully authenticated'

if __name__ == '__main__':
    unittest.main()

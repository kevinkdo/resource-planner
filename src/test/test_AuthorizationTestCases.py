import unittest
import requests
import json
import params

class AuthorizationTestCases(unittest.TestCase):
  def setUp(self):
      r = requests.get(params.baseUrl + params.resetUrl, headers = params.headers, verify = False)

  # def test_RegisterValidUser(self):
  #     validUser = {'email':'user13@admin.com', 'username':'someusername', 'password':'something', 'should_email':'False'}
  #     r = requests.post(params.baseUrl + params.registerUrl, data = json.dumps(validUser), headers = params.headers, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == False
  #     assert decoded['data'] == None
  #     assert decoded['error_msg'] == 'Successfully registered'

  # def test_RegisterInValidUserWithEmailAlreadyTaken(self):
  #     validUser = {'email':'admin@admin.com', 'username':'someusername', 'password':'something', 'should_email':'False'}
  #     r = requests.post(params.baseUrl + params.registerUrl, data = json.dumps(validUser), headers = params.headers, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == True
  #     assert decoded['data'] == None
  #     assert decoded['error_msg'] == 'Email already exists'
  
  # def test_RegisterInValidUserNoPassword(self):
  #     validUser = {'email':'user13@admin.com', 'username':'someusername', 'password':'', 'should_email':'False'}
  #     r = requests.post(params.baseUrl + params.registerUrl, data = json.dumps(validUser), headers = params.headers, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == True
  #     assert decoded['data'] == None
  #     assert decoded['error_msg'] == 'Password must be between 1 and 250 characters long'

  def test_LoginValidUser(self):
      userLogin = {'email':'admin@admin.com', 'username':'admin', 'password':'admin'}
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(userLogin), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'username': u'admin', u'token': u'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOjEsInN1cGVyX3AiOnRydWUsInJlc291cmNlX3AiOmZhbHNlLCJyZXNlcnZhdGlvbl9wIjpmYWxzZSwidXNlcl9wIjpmYWxzZX0.ftBgC8KfB5GyQy68I7HdQQZjUdBvkF-il93Z87BwKfg', u'userId': 1}
      assert decoded['error_msg'] == 'Successfully authenticated'

  def test_LoginInvalidUserWrongPassword(self):
      userLogin = {'email':'admin@admin.com', 'username':'admin', 'password':'wrongpassword'}
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(userLogin), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Failed to validate password'

  def test_LoginInvalidUserInvalidUsername(self):
      userLogin = {'email':'admin@admin.com', 'username':'admin123', 'password':'admin'}
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(userLogin), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Account does not exist'

if __name__ == '__main__':
    unittest.main()

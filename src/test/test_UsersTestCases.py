import unittest
import requests
import json

class UsersTestCases(unittest.TestCase):
  def setUp(self):
      self.baseUrl = 'http://localhost:8080/'
      self.headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
      self.userUrl = 'api/users'
      self.resetUrl = '/admin/init'
      r = requests.get(self.baseUrl + self.resetUrl, headers = self.headers)
      
  def test_CreateValidUserCheckAdmin(self):
      pass   
  def test_CreateInvaidUserWithPreexistingUsername(self):
      pass
  def test_GetUserWithValidID(self):
      pass  
  def test_GetUserWithInvalidID(self):
      pass    
  def test_GetUserWithQuery(self):
      pass    
  def test_PutUserWithValidIDUpdateUsernameWithUniqueUsername(self):
      pass  
  def test_PutUserWithValidIDUpdateUsernameWithPreexistingUsername(self):
      pass 
  def test_PutUserWithValidIDUpdateNoFields(self):
      pass    
  def test_PutUserWithInvalidID(self):
      pass    
  def test_PutUserAdminCheck(self):
      pass   
  def test_DeleteUserWithValidID(self):
      pass   
  def test_DeleteUserWithInvalidID(self):
      pass 
      
if __name__ == '__main__':
    unittest.main()

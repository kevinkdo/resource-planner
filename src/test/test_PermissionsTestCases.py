import unittest
import requests
import json
import params

class PermissionsTestCases(unittest.TestCase):
  def setUp(self):
      requests.get(params.baseUrl + params.resetUrl, headers = params.headers, verify = False)
   
  def test_GetUserPermissionsWithValidIDWithUserManagementPermissions(self):
		pass
  def test_SetUserPermissionsWithValidIDWithoutUserManagementPermissions(self):
     	pass
  def test_SetUserPermissionsWithInvalidIDWithUserManagementPermissions(self):
		pass
  def test_GetUserPermissionsWithValidIDWithUserManagementPermissions(self):
		pass
  def test_GetUserPermissionsWithValidIDWithoutUserManagementPermissions(self):
		pass
  def test_GetUserPermissionsWithInvalidIDWithUserManagementPermissions(self):
		pass

if __name__ == '__main__':
    unittest.main()


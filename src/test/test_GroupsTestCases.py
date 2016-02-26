import unittest
import requests
import json
import params

class GroupsTestCases(unittest.TestCase):
  def setUp(self):
      requests.get(params.baseUrl + params.resetUrl, headers = params.headers, verify = False)      

  def test_CreateValidGroupWithUserManagementPermissions(self):
      pass      
  def test_CreateValidGroupWithoutUserManagementPermissions(self):
      pass
  def test_GetGroupsWithUserManagementPermissions(self):
      pass
  def test_GetGroupsWithoutUserManagementPermissions(self):
      pass
  def test_GetGroupsByValidIDWithUserManagementPermissions(self):
      pass
  def test_GetGroupsByInvalidIDWithUserManagementPermissions(self):
      pass
  def test_PutGroupWithValidIDWithUserManagementPermissions(self):
      pass
  def test_PutGroupWithValidIDWithoutUserManagementPermissions(self):
      pass
  def test_PutGroupWithInvalidIDWithUserManagementPermissions(self):
      pass
  def test_DeleteGroupWithValidIDWithUserManagementPermissions(self):
      pass
  def test_DeleteGroupWithValidIDWithoutUserManagementPermissions(self):
      pass
  def test_DeleteGroupWithInvalidIDWithUserManagementPermissions(self):
      pass
  def test_UserHasLowerReservationManagementPermissionsThanGroup(self):
      pass
  def test_UserHasHigherReservationManagementPermissionsThanGroup(self):
      pass
  def test_UserHasLowerResourceManagementPermiissionsThanGroup(self):
      pass
  def test_UserHasHigherResourceManagementPermissionsThanGroup(self):
      pass
  def test_UserHasLowerUserManagementPermissionsThanGroup(self):
      pass
  def test_UserHasHigherUserManagementPermissionsThanGroup(self):
      pass

if __name__ == '__main__':
    unittest.main()

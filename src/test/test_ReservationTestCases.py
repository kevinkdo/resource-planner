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
allHeaders = {
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
  "resources":[{"resource_id":1,"resource_name":"reserveaccess"},{"resource_id":2,"resource_name":"noaccess"}],
  "system_permissions":{
    "user_permissions":[{"resource_p":True,"reservation_p":True,"user_p":True,"user_id":2}, {"resource_p":True,"reservation_p":False,"user_p":False,"user_id":3}],
    "group_permissions":[]
  },
  "resource_permissions":{
    "user_permissions":[{"resource_id":1,"permission_level":2,"user_id":2},{"resource_id":2,"permission_level":1,"user_id":2},{"resource_id":1,"permission_level":2,"user_id":3},{"resource_id":2,"permission_level":1,"user_id":3}],
    "group_permissions":[]
  }
}
#resource id:1 ==> reserve access
#resource id:2 ==> no reserve access
#user id:2 ==> ump
#user id:3 ==> no reserve mp
class ReservationTestCases(unittest.TestCase):
  def setUp(self):
      r = requests.get(params.baseUrl + params.resetUrl, headers = params.headers, verify = False)
      reserveAcessResource = {'name':'reserveaccess', 'description':'', 'tags':[], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      noReserveAccessResource = {'name':'noaccess', 'description':'', 'tags':[], 'restricted': False, 'shared_count': 1, 'parent_id': 0}
      r = requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(reserveAcessResource), headers = params.headers, verify = False)
      r = requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(noReserveAccessResource), headers = params.headers, verify = False)
      
      r = requests.post(params.baseUrl + params.userUrl, json.dumps(params.userWithAll), headers = params.headers, verify = False)
      r = requests.post(params.baseUrl + params.userUrl, json.dumps(params.userWithResource), headers = params.headers, verify = False)

      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithAdmin), headers = params.headers, verify = False)
      adminHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithAll), headers = params.headers, verify = False)
      allHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]
      r = requests.post(params.baseUrl + params.loginUrl, data = json.dumps(params.loginWithResource), headers = params.headers, verify = False)
      RSMPHeaders["Authorization"] = "Bearer " + r.json()["data"]["token"]

      r = requests.put(params.baseUrl + params.userUrl + "/1" + params.permissionsUrl, data = json.dumps(permissions), headers = adminHeaders, verify = False)

      #need user with reserve mp and reserve access
      # need user with reserve mp and without eserve access
      # need user without reserve mp and with reserve access
      # need uer without reserve mp and without reserve access

  def test_CreateValidReservationWithValidUserIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'description': u'none', u'title': u'none', u'user': {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}, u'complete': True, u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'resources': [{u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': [], u'name': u'reserveaccess'}], u'end_time': u'2011-08-06T11:00:00.000Z'}
      assert decoded['error_msg'] == 'Reservation inserted successfully'

  def test_CreateValidReservationWithValidUserIDWithReservationManagementPermissionsWithoutReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_id": [2], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Request is not valid'

  def test_CreateValidReservationWithValidUserIDWithoutReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "3", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = RSMPHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'description': u'none', u'title': u'none', u'user': {u'username': u'resource', u'should_email': False, u'user_id': 3, u'reservation_p': False, u'resource_p': True, u'user_p': False, u'email': u'resource@a.com'}, u'complete': True, u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'resources': [{u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': [], u'name': u'reserveaccess'}], u'end_time': u'2011-08-06T11:00:00.000Z'}
      assert decoded['error_msg'] == 'Reservation inserted successfully'

  def test_CreateValidReservationWithValidUserIDWithoutReservationManagementPermissionsWithoutReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "3", "resource_ids": [2], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = RSMPHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'You do not have reservation permission for resource with ID 2'

  # def test_CreateValidReservationWithInvalidUserIDWithReservationManagementPermissionsWithReserveAccess(self):
  #     reservation = {"title": "none", "description": "none", "user_id": "5", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
  #     r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
  #     decoded = r.json()      
  #     print decoded
  #     assert decoded['is_error'] == True
  #     assert decoded['data'] == None
  #     assert decoded['error_msg'] == 'User does not exist'

  def test_CreateInvalidReservationWithInvalidResourceIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [5], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource with id 5 does not exist'

  def test_CreateInvalidReservationInvalidTimeRangeWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2012-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Begin time is after end time'

  def test_GetReservationWithValidIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      r = requests.get(params.baseUrl + params.reserveUrl + "/1", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'description': u'none', u'title': u'none', u'user': {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}, u'complete': True, u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'resources': [{u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': [], u'name': u'reserveaccess'}], u'end_time': u'2011-08-06T11:00:00.000Z'}
      assert decoded['error_msg'] == 'Successfully retrieved reservation'

  def test_GetReservationWithValidIDWithoutReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      r = requests.get(params.baseUrl + params.reserveUrl + "/1", headers = RSMPHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'description': u'none', u'title': u'none', u'user': {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}, u'complete': True, u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'resources': [{u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': [], u'name': u'reserveaccess'}], u'end_time': u'2011-08-06T11:00:00.000Z'}
      assert decoded['error_msg'] == 'Successfully retrieved reservation'

  def test_GetReservationWithInvalidIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      r = requests.get(params.baseUrl + params.reserveUrl + "/10", headers = allHeaders, verify = False)
      decoded = r.json()   
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Reservation with that ID does not exist'

  def test_GetReservationWithValidQueryWithResourceAndUserListsWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      queryUrl = '/?start=2008-08-06T10:54:17.000Z&end=2015-08-06T10:54:17.000Z&user_ids=1,2&resource_ids=1'
      r = requests.get(params.baseUrl + params.reserveUrl + queryUrl, headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'reservations': [{u'should_email': True, u'description': u'none', u'title': u'none', u'user': {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}, u'complete': True, u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'resources': [{u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': [], u'name': u'reserveaccess'}], u'end_time': u'2011-08-06T11:00:00.000Z'}]}
      assert decoded['error_msg'] == 'Successfully retrieved reservations'

  def test_GetReservationWithQueryValidTimeRangeWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      queryUrl = '/?start=2008-08-06T10:54:17.000Z&end=2015-08-06T10:54:17.000Z'
      r = requests.get(params.baseUrl + params.reserveUrl + queryUrl, headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'reservations': [{u'should_email': True, u'description': u'none', u'title': u'none', u'user': {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}, u'complete': True, u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'resources': [{u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': [], u'name': u'reserveaccess'}], u'end_time': u'2011-08-06T11:00:00.000Z'}]}
      assert decoded['error_msg'] == 'Successfully retrieved reservations given no parameters'

  def test_GetReservationWtihQueryInvalidTimeRangeWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      queryUrl = '/?start=2015-08-06T10:54:17.000Z&end=2008-08-06T10:54:17.000Z'
      r = requests.get(params.baseUrl + params.reserveUrl + queryUrl, headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Request is not valid'

  def test_PutReservationWithValidIDUpdateAllFieldsWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      update = {"title": "none", "description": "none", "user_id":2,"resource_ids": [1],"begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T10:59:00.000Z","should_email":True}
      r = requests.put(params.baseUrl + params.reserveUrl + '/1', data = json.dumps(update), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'description': u'none', u'title': u'none', u'user': {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}, u'complete': True, u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'resources': [{u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': [], u'name': u'reserveaccess'}], u'end_time': u'2011-08-06T10:59:00.000Z'}
      assert decoded['error_msg'] == 'Reservation successfully updated'

  def test_PutReservationWithValidIDUpdateAllFieldsWithReservationManagementPermissionsWithoutReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      update = {"title": "none", "description": "none", "user_id":2,"resource_ids": [2],"begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T10:59:00.000Z","should_email":True}
      r = requests.put(params.baseUrl + params.reserveUrl + '/1', data = json.dumps(update), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Cannot add new resources to reservation' 

  def test_PutReservationWithInvalidIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      update = {"title": "none", "description": "none", "user_id":2,"resource_ids":[1],"begin_time":"2016-03-02T05:27:00.000Z","end_time":"2016-03-02T05:28:00.000Z","should_email":True}
      r = requests.put(params.baseUrl + params.reserveUrl + '/5', data = json.dumps(update), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Reservation with that ID does not exist'

  def test_PutReservationWithValidIDOfAnotherUserWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "3", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = RSMPHeaders, verify = False)
      update = {"title": "none", "description": "none", "user_id":3,"resource_ids":[1],"begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T10:59:00.000Z","should_email":True}
      r = requests.put(params.baseUrl + params.reserveUrl + '/1', data = json.dumps(update), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'description': u'none', u'title': u'none', u'user': {u'username': u'resource', u'should_email': False, u'user_id': 3, u'reservation_p': False, u'resource_p': True, u'user_p': False, u'email': u'resource@a.com'}, u'complete': True, u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'resources': [{u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': [], u'name': u'reserveaccess'}], u'end_time': u'2011-08-06T10:59:00.000Z'}
      assert decoded['error_msg'] == 'Reservation successfully updated'

  def test_PutReservationWithValidIDOfAnotherUserWithoutReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      update = {"title": "none", "description": "none", "user_id":"2","resource_ids": [1],"begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T10:59:00.000Z","should_email":True}
      r = requests.put(params.baseUrl + params.reserveUrl + '/1', data = json.dumps(update), headers = RSMPHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'You don\'t have permissions to edit this reservation'

  def test_DeleteReservationWithValidIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "3", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = RSMPHeaders, verify = False)
      r = requests.delete(params.baseUrl + params.reserveUrl + "/1", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Successfully deleted reservation'

  def test_DeleteReservationWithValidIDWithoutReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      r = requests.delete(params.baseUrl + params.reserveUrl + "/1", headers = RSMPHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'You don\'t have permissions to delete this reservation'

  def test_DeleteReservationWithInvalidIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"title": "none", "description": "none", "user_id": "3", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = RSMPHeaders, verify = False)
      r = requests.delete(params.baseUrl + params.reserveUrl + "/5", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Reservation with that ID does not exist'

  def test_CreateTouchingReservationsWithReservationManagementPermissionsWithReserveAccess(self):
      reservation1 = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      reservation2 = {"title": "none", "description": "none", "user_id": "2", "resource_ids": [1], "begin_time": "2011-08-06T11:00:00.000Z", "end_time": "2012-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation1), headers = allHeaders, verify = False)
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation2), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'description': u'none', u'title': u'none', u'user': {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}, u'complete': True, u'begin_time': u'2011-08-06T11:00:00.000Z', u'reservation_id': 2, u'resources': [{u'description': u'', u'tags': [], u'resource_id': 1, u'restricted': False, u'shared_count': 1, u'can_view': True, u'can_reserve': True, u'parent_id': 0, u'children': [], u'name': u'reserveaccess'}], u'end_time': u'2012-08-06T11:00:00.000Z'}
      assert decoded['error_msg'] == 'Reservation inserted successfully'
      
if __name__ == '__main__':
    unittest.main()

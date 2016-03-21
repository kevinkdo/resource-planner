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
      reserveAcessResource = {'name':'reserveaccess', 'description':'', 'tags':[], 'restricted': False}
      noReserveAccessResource = {'name':'noaccess', 'description':'', 'tags':[], 'restricted': False}
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
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'user_id': 2, u'resource_id': 1, u'end_time': u'2011-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1}
      assert decoded['error_msg'] == 'Reservation inserted successfully'

  def test_CreateValidReservationWithValidUserIDWithReservationManagementPermissionsWithoutReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "2", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource cannot be reserved by user'

  def test_CreateValidReservationWithValidUserIDWithoutReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "3", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = RSMPHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'user_id': 3, u'resource_id': 1, u'end_time': u'2011-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1}
      assert decoded['error_msg'] == 'Reservation inserted successfully'

  def test_CreateValidReservationWithValidUserIDWithoutReservationManagementPermissionsWithoutReserveAccess(self):
      reservation = {"user_id": "3", "resource_id": "2", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = RSMPHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource cannot be reserved by user'

  def test_CreateValidReservationWithInvalidUserIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "5", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'User does not exist'

  def test_CreateInvalidReservationWithInvalidResourceIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "5", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource does not exist'

  def test_CreateInvalidReservationInvalidTimeRangeWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2012-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Begin time after end time'

  def test_GetReservationWithValidIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      r = requests.get(params.baseUrl + params.reserveUrl + "/1", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'resource': {'restricted': False, u'description': u'', u'tags': [], u'name': u'reserveaccess', u'resource_id': 1}, u'end_time': u'2011-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'user': {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}}
      assert decoded['error_msg'] == 'Reservation with given ID found'

  def test_GetReservationWithValidIDWithoutReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      r = requests.get(params.baseUrl + params.reserveUrl + "/1", headers = RSMPHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'resource': {'restricted': False, u'description': u'', u'tags': [], u'name': u'reserveaccess', u'resource_id': 1}, u'end_time': u'2011-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'user': {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}}
      assert decoded['error_msg'] == 'Reservation with given ID found'

  def test_GetReservationWithInvalidIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      r = requests.get(params.baseUrl + params.reserveUrl + "/10", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Reservation with given ID not found'

  def test_GetReservationWithValidQueryWithResourceAndUserListsWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      queryUrl = '/?start=2008-08-06T10:54:17.000Z&end=2015-08-06T10:54:17.000Z&user_ids=1,2&resource_ids=1'
      r = requests.get(params.baseUrl + params.reserveUrl + queryUrl, headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == [{u'should_email': True, u'resource': {'restricted': False, u'description': u'', u'tags': [], u'name': u'reserveaccess', u'resource_id': 1}, u'end_time': u'2011-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'user': {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}}]
      assert decoded['error_msg'] == 'Matching reservations retrieved'

  def test_GetReservationWithQueryValidTimeRangeWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      queryUrl = '/?start=2008-08-06T10:54:17.000Z&end=2015-08-06T10:54:17.000Z'
      r = requests.get(params.baseUrl + params.reserveUrl + queryUrl, headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == [{u'should_email': True, u'resource': {'restricted': False, u'description': u'', u'tags': [], u'name': u'reserveaccess', u'resource_id': 1}, u'end_time': u'2011-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'user': {u'username': u'all', u'should_email': False, u'user_id': 2, u'reservation_p': True, u'resource_p': True, u'user_p': True, u'email': u'all@a.com'}}]
      assert decoded['error_msg'] == 'Matching reservations retrieved'

  def test_GetReservationWtihQueryInvalidTimeRangeWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      queryUrl = '/?start=2015-08-06T10:54:17.000Z&end=2008-08-06T10:54:17.000Z'
      r = requests.get(params.baseUrl + params.reserveUrl + queryUrl, headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Invalid input parameters (Issue with start and end times)'

  def test_PutReservationWithValidIDUpdateAllFieldsWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      update = {"user_id":2,"resource_id":1,"begin_time":"2016-03-02T05:27:00.000Z","end_time":"2016-03-02T05:28:00.000Z","should_email":True}
      r = requests.put(params.baseUrl + params.reserveUrl + '/1', data = json.dumps(update), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'user_id': 2, u'resource_id': 1, u'end_time': u'2016-03-02T05:28:00.000Z', u'begin_time': u'2016-03-02T05:27:00.000Z', u'reservation_id': 1}
      assert decoded['error_msg'] == 'Successfully updated reservation'

  def test_PutReservationWithValidIDUpdateAllFieldsWithReservationManagementPermissionsWithoutReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      update = {"user_id":2,"resource_id":2,"begin_time":"2016-03-02T05:27:00.000Z","end_time":"2016-03-02T05:28:00.000Z","should_email":True}
      r = requests.put(params.baseUrl + params.reserveUrl + '/1', data = json.dumps(update), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource not reservable by user' 

  def test_PutReservationWithInvalidIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      update = {"user_id":2,"resource_id":1,"begin_time":"2016-03-02T05:27:00.000Z","end_time":"2016-03-02T05:28:00.000Z","should_email":True}
      r = requests.put(params.baseUrl + params.reserveUrl + '/5', data = json.dumps(update), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'No reservation with given ID exists'

  def test_PutReservationWithValidIDOfAnotherUserWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "3", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = RSMPHeaders, verify = False)
      update = {"user_id":3,"resource_id":1,"begin_time":"2016-03-02T05:27:00.000Z","end_time":"2016-03-02T05:28:00.000Z","should_email":True}
      r = requests.put(params.baseUrl + params.reserveUrl + '/1', data = json.dumps(update), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'user_id': 3, u'resource_id': 1, u'end_time': u'2016-03-02T05:28:00.000Z', u'begin_time': u'2016-03-02T05:27:00.000Z', u'reservation_id': 1}
      assert decoded['error_msg'] == 'Successfully updated reservation'

  def test_PutReservationWithValidIDOfAnotherUserWithoutReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      update = {"user_id":2,"resource_id":1,"begin_time":"2016-03-02T05:27:00.000Z","end_time":"2016-03-02T05:28:00.000Z","should_email":True}
      r = requests.put(params.baseUrl + params.reserveUrl + '/1', data = json.dumps(update), headers = RSMPHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Non-admin trying to alter another user\'s reservation'

  def test_DeleteReservationWithValidIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "3", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = RSMPHeaders, verify = False)
      r = requests.delete(params.baseUrl + params.reserveUrl + "/1", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Reservation successfully deleted'

  def test_DeleteReservationWithValidIDWithoutReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = allHeaders, verify = False)
      r = requests.delete(params.baseUrl + params.reserveUrl + "/1", headers = RSMPHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Non-Admin user attempting to delete reservation for another user'

  def test_DeleteReservationWithInvalidIDWithReservationManagementPermissionsWithReserveAccess(self):
      reservation = {"user_id": "3", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = RSMPHeaders, verify = False)
      r = requests.delete(params.baseUrl + params.reserveUrl + "/5", headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'No reservation with given ID exists'

  def test_CreateTouchingReservationsWithReservationManagementPermissionsWithReserveAccess(self):
      reservation1 = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      reservation2 = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T11:00:00.000Z", "end_time": "2012-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation1), headers = allHeaders, verify = False)
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation2), headers = allHeaders, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'user_id': 2, u'resource_id': 1, u'end_time': u'2012-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T11:00:00.000Z', u'reservation_id': 2}
      assert decoded['error_msg'] == 'Reservation inserted successfully'
      
if __name__ == '__main__':
    unittest.main()

import unittest
import requests
import json

class ReservationTestCases(unittest.TestCase):
  def setUp(self):
      self.baseUrl = 'http://localhost:8080/'
      self.headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
      self.reserveUrl = 'api/reservations'
      self.resourceUrl = 'api/resources'
      self.resetUrl = '/admin/init'
      r = requests.get(self.baseUrl + self.resetUrl, headers = self.headers)
      validResource = {'name':'some resource', 'description':'some resource description', 'tags':[]}
      validResource2 = {'name':'some resource2', 'description':'some resource2 description', 'tags':[]}
      r = requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource), headers = self.headers)
      r = requests.post(self.baseUrl + self.resourceUrl, data = json.dumps(validResource2), headers = self.headers)

  def test_CreateValidReservationWithValidUserID(self):
    reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    r = requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == False
    assert decoded['data'] == {u'should_email': True, u'user_id': 1, u'resource_id': 1, u'end_time': u'2011-08-06T11:00:00', u'begin_time': u'2011-08-06T10:54:17', u'reservation_id': 1}
    assert decoded['error_msg'] == 'Reservation inserted successfully'

  def test_CreateValidReservationWithInvalidUserID(self):
    reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    r = requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == True
    assert decoded['data'] == None
    assert decoded['error_msg'] == 'User does not exist'
    
  def test_CreateInvalidReservationWithInvalidResourceID(self):
    reservation = {"user_id": "1", "resource_id": "3", "begin_time": "2011-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    r = requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == True
    assert decoded['data'] == None
    assert decoded['error_msg'] == 'Resource does not exist'

  def test_CreateInvalidReservationInvalidTimeRange(self):
    reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2012-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    r = requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == True
    assert decoded['data'] == None
    assert decoded['error_msg'] == 'Begin time after end time'

  def test_GetReservationWithValidID(self):
    reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    r = requests.get(self.baseUrl + self.reserveUrl + '/1', headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == False
    assert decoded['data'] == {u'should_email': True, u'resource': {u'description': u'some resource description', u'tags': [], u'name': u'some resource', u'resource_id': 1}, u'end_time': u'2011-08-06T11:00:00', u'begin_time': u'2011-08-06T10:54:17', u'reservation_id': 1, u'user': {u'username': u'admin', u'should_email': False, u'email': u'admin@admin.com'}}
    assert decoded['error_msg'] == 'Reservation with given ID found'
    
  def test_GetReservationWithInvalidID(self):
    reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    r = requests.get(self.baseUrl + self.reserveUrl + '/2', headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == True
    assert decoded['data'] == None
    assert decoded['error_msg'] == 'Reservation with given ID not found'

  def test_GetReservationWithValidQueryWithResourceAndUserLists(self):
    reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    queryUrl = '/?start=2008-08-06T10:54:17&end=2015-08-06T10:54:17&user_ids=1,2&resource_ids=1'
    r = requests.get(self.baseUrl + self.reserveUrl + queryUrl, headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == False
    assert decoded['data'] == [{u'should_email': True, u'resource': {u'description': u'some resource description', u'tags': [], u'name': u'some resource', u'resource_id': 1}, u'end_time': u'2011-08-06T11:00:00', u'begin_time': u'2011-08-06T10:54:17', u'reservation_id': 1, u'user': {u'username': u'admin', u'should_email': False, u'email': u'admin@admin.com'}}]
    assert decoded['error_msg'] == 'Matching reservations retrieved'

  def test_GetReservationWtihQueryValidTimeRange(self):
    reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    queryUrl = '/?start=2008-08-06T10:54:17&end=2015-08-06T10:54:17'
    r = requests.get(self.baseUrl + self.reserveUrl + queryUrl, headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == False
    assert decoded['data'] == [{u'should_email': True, u'resource': {u'description': u'some resource description', u'tags': [], u'name': u'some resource', u'resource_id': 1}, u'end_time': u'2011-08-06T11:00:00', u'begin_time': u'2011-08-06T10:54:17', u'reservation_id': 1, u'user': {u'username': u'admin', u'should_email': False, u'email': u'admin@admin.com'}}]
    assert decoded['error_msg'] == 'Matching reservations retrieved'

  def test_GetReservationWtihQueryInvalidTimeRange(self):
    reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    queryUrl = '/?start=2015-08-06T10:54:17&end=2008-08-06T10:54:17'
    r = requests.get(self.baseUrl + self.reserveUrl + queryUrl, headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == True
    assert decoded['data'] == None
    assert decoded['error_msg'] == 'Invalid input parameters (Issue with start and end times)'

  def test_PutReservationWithValidIDUpdateAllFields(self):
    reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    update = {'resource_id':'2', 'should_email':'false'}
    r = requests.put(self.baseUrl + self.reserveUrl + '/1', data = json.dumps(update), headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == False
    assert decoded['data'] == {u'should_email': False, u'user_id': 1, u'resource_id': 2, u'end_time': u'2011-08-06T11:00:00', u'begin_time': u'2011-08-06T10:54:17', u'reservation_id': 1}
    assert decoded['error_msg'] == 'Successfully updated reservation'

  def test_PutReservationWithValidIDUpdateNoFields(self):
    reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    update = {}
    r = requests.put(self.baseUrl + self.reserveUrl + '/1', data = json.dumps(update), headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == False
    assert decoded['data'] == {u'should_email': True, u'user_id': 1, u'resource_id': 1, u'end_time': u'2011-08-06T11:00:00', u'begin_time': u'2011-08-06T10:54:17', u'reservation_id': 1}
    assert decoded['error_msg'] == 'Successfully updated reservation'

  def test_PutReservationWithInvalidID(self):
    reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    update = {}
    r = requests.put(self.baseUrl + self.reserveUrl + '/2', data = json.dumps(update), headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == True
    assert decoded['data'] == None
    assert decoded['error_msg'] == 'No reservation with given ID exists'

  def test_DeleteReservationWithValidID(self):
    reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    r = requests.delete(self.baseUrl + self.reserveUrl + '/1', headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == False
    assert decoded['data'] == None
    assert decoded['error_msg'] == 'Reservation successfully deleted'

  def test_DeleteReservationWithInvalidID(self):
    reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:17", "end_time": "2011-08-06T11:00:00", "should_email": "true"}
    requests.post(self.baseUrl + self.reserveUrl, data = json.dumps(reservation), headers = self.headers)
    r = requests.delete(self.baseUrl + self.reserveUrl + '/2', headers = self.headers)
    decoded = r.json()
    assert decoded['is_error'] == True
    assert decoded['data'] == None
    assert decoded['error_msg'] == 'No reservation with given ID exists'
      
if __name__ == '__main__':
    unittest.main()

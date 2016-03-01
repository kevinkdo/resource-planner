import unittest
import requests
import json
import params

class ReservationTestCases(unittest.TestCase):
  def setUp(self):
      r = requests.get(params.baseUrl + params.resetUrl, headers = params.headers, verify = False)
      validResource = {'name':'some resource', 'description':'some resource description', 'tags':[]}
      validResource2 = {'name':'some resource2', 'description':'some resource2 description', 'tags':[]}
      r = requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(validResource), headers = params.headers, verify = False)
      r = requests.post(params.baseUrl + params.resourceUrl, data = json.dumps(validResource2), headers = params.headers, verify = False)

  def test_CreateValidReservationWithValidUserID(self):
      reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'user_id': 1, u'resource_id': 1, u'end_time': u'2011-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1}
      assert decoded['error_msg'] == 'Reservation inserted successfully'

  def test_CreateValidReservationWithInvalidUserID(self):
      reservation = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'User does not exist'

  def test_CreateTouchingReservations(self):
      reservation1 = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      reservation2 = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T11:00:00.000Z", "end_time": "2012-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation1), headers = params.headers, verify = False)
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation2), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'user_id': 1, u'resource_id': 1, u'end_time': u'2012-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T11:00:00.000Z', u'reservation_id': 2}
      assert decoded['error_msg'] == 'Reservation inserted successfully'
    
  def test_CreateInvalidReservationWithInvalidResourceID(self):
      reservation = {"user_id": "1", "resource_id": "3", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Resource does not exist'

  def test_CreateInvalidReservationInvalidTimeRange(self):
      reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2012-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      r = requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Begin time after end time'

  def test_GetReservationWithValidID(self):
      reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      r = requests.get(params.baseUrl + params.reserveUrl + '/1', headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == {u'should_email': True, u'resource': {u'description': u'some resource description', u'tags': [], u'name': u'some resource', u'resource_id': 1}, u'end_time': u'2011-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'user': {u'user_id': 1, u'resource_p': True, u'reservation_p': True, u'user_p': True, u'username': u'admin', u'should_email': False, u'email': u'admin@admin.com'}}
      assert decoded['error_msg'] == 'Reservation with given ID found'
    
  def test_GetReservationWithInvalidID(self):
      reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      r = requests.get(params.baseUrl + params.reserveUrl + '/2', headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Reservation with given ID not found'

  def test_GetReservationWithValidQueryWithResourceAndUserLists(self):
      reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      queryUrl = '/?start=2008-08-06T10:54:17.000Z&end=2015-08-06T10:54:17.000Z&user_ids=1,2&resource_ids=1'
      r = requests.get(params.baseUrl + params.reserveUrl + queryUrl, headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == [{u'should_email': True, u'resource': {u'description': u'some resource description', u'tags': [], u'name': u'some resource', u'resource_id': 1}, u'end_time': u'2011-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'user': {u'user_id': 1, u'username': u'admin', u'resource_p': True, u'reservation_p': True, u'user_p': True, u'should_email': False, u'email': u'admin@admin.com'}}]
      assert decoded['error_msg'] == 'Matching reservations retrieved'

  def test_GetReservationWithQueryValidTimeRange(self):
      reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      queryUrl = '/?start=2008-08-06T10:54:17.000Z&end=2015-08-06T10:54:17.000Z'
      r = requests.get(params.baseUrl + params.reserveUrl + queryUrl, headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == [{u'should_email': True, u'resource': {u'description': u'some resource description', u'tags': [], u'name': u'some resource', u'resource_id': 1}, u'end_time': u'2011-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1, u'user': {u'username': u'admin', u'resource_p': True, u'reservation_p': True, u'user_p': True, u'should_email': False, u'user_id': 1, u'email': u'admin@admin.com'}}]
      assert decoded['error_msg'] == 'Matching reservations retrieved'

  def test_GetReservationWithQueryInvalidTimeRange(self):
      reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      queryUrl = '/?start=2015-08-06T10:54:17.000Z&end=2008-08-06T10:54:17.000Z'
      r = requests.get(params.baseUrl + params.reserveUrl + queryUrl, headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Invalid input parameters (Issue with start and end times)'

  # def test_PutReservationWithValidIDUpdateAllFields(self):
  #     reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
  #     requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
  #     update = {'resource_id':'2', 'should_email':'false'}
  #     r = requests.put(params.baseUrl + params.reserveUrl + '/1', data = json.dumps(update), headers = params.headers, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == False
  #     assert decoded['data'] == {u'should_email': False, u'user_id': 1, u'resource_id': 2, u'end_time': u'2011-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1}
  #     assert decoded['error_msg'] == 'Successfully updated reservation'

  # def test_PutReservationWithValidIDUpdateNoFields(self):
  #     reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
  #     requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
  #     update = {}
  #     r = requests.put(params.baseUrl + params.reserveUrl + '/1', data = json.dumps(update), headers = params.headers, verify = False)
  #     decoded = r.json()
  #     assert decoded['is_error'] == False
  #     assert decoded['data'] == {u'should_email': True, u'user_id': 1, u'resource_id': 1, u'end_time': u'2011-08-06T11:00:00.000Z', u'begin_time': u'2011-08-06T10:54:00.000Z', u'reservation_id': 1}
  #     assert decoded['error_msg'] == 'Successfully updated reservation'

  def test_PutReservationWithInvalidID(self):
      reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      update = {}
      r = requests.put(params.baseUrl + params.reserveUrl + '/2', data = json.dumps(update), headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'No reservation with given ID exists'

  def test_DeleteReservationWithValidID(self):
      reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      r = requests.delete(params.baseUrl + params.reserveUrl + '/1', headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == False
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'Reservation successfully deleted'

  def test_DeleteReservationWithInvalidID(self):
      reservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
      requests.post(params.baseUrl + params.reserveUrl, data = json.dumps(reservation), headers = params.headers, verify = False)
      r = requests.delete(params.baseUrl + params.reserveUrl + '/2', headers = params.headers, verify = False)
      decoded = r.json()
      assert decoded['is_error'] == True
      assert decoded['data'] == None
      assert decoded['error_msg'] == 'No reservation with given ID exists'
      
if __name__ == '__main__':
    unittest.main()

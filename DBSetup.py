import unittest
import requests
import json


def addUsers():
	baseUrl = 'https://localhost:8443/'
	headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
	userUrl = 'api/users'
	resourceUrl = 'api/resources'
	reserveUrl = 'api/reservations'

	newUser1 = {"email":"davis.treybig@gmail.com", "password":"password", "should_email":'true', "username":"davis"}
	newUser2 = {"email":"fakeuser@admin.com", "password":"password", "should_email":'false', "username":"fakeuser"}
	newUser3 = {"email":"coolguy@awesome.com", "password":"cool", "should_email":'false', "username":"cool"}

	r1 = requests.post(baseUrl + userUrl, data = json.dumps(newUser1), headers = headers)

	r2 = requests.post(baseUrl + userUrl, data = json.dumps(newUser2), headers = headers)

	r3 = requests.post(baseUrl + userUrl, data = json.dumps(newUser3), headers = headers)
	

def addResources():
	baseUrl = 'https://localhost:8443/'
	headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
	userUrl = 'api/users'
	resourceUrl = 'api/resources'
	reserveUrl = 'api/reservations'

	validResource1 = {'name':'chair', 'description':'Great chair.', 'tags':[]}
	validResource2 = {'name':'desk', 'description':'4 Legged Desk', 'tags':['wood']}
	validResource3 = {'name':'kitty', 'description':'meow', 'tags':['animal', 'cat']}
	validResource4 = {'name':'computer', 'description':'Windows 98', 'tags':['machine', 'expensive', 'rare']}
	validResource5 = {'name':'tree', 'description':'Huge tree', 'tags':['wood', 'expensive']}
	
	r1 = requests.post(baseUrl + resourceUrl, data = json.dumps(validResource1), headers = headers)
	r2 = requests.post(baseUrl + resourceUrl, data = json.dumps(validResource2), headers = headers)
	r3 = requests.post(baseUrl + resourceUrl, data = json.dumps(validResource3), headers = headers)
	r4 = requests.post(baseUrl + resourceUrl, data = json.dumps(validResource4), headers = headers)
	r5 = requests.post(baseUrl + resourceUrl, data = json.dumps(validResource5), headers = headers)

def addReservations():
	baseUrl = 'https://localhost:8443/'
	headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
	userUrl = 'api/users'
	resourceUrl = 'api/resources'
	reserveUrl = 'api/reservations'

	reservation1 = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:17.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
	reservation2 = {"user_id": "2", "resource_id": "2", "begin_time": "2010-08-06T10:54:17.000Z", "end_time": "2010-08-06T11:54:17.000Z", "should_email": "false"}
	reservation3 = {"user_id": "3", "resource_id": "3", "begin_time": "2009-08-06T10:54:17.000Z", "end_time": "2009-08-06T11:54:17.000Z", "should_email": "false"}
	reservation4 = {"user_id": "1", "resource_id": "4", "begin_time": "2008-08-06T10:54:17.000Z", "end_time": "2008-08-06T11:54:17.000Z", "should_email": "false"}
	reservation5 = {"user_id": "2", "resource_id": "5", "begin_time": "2000-08-06T10:54:17.000Z", "end_time": "2005-08-06T11:54:17.000Z", "should_email": "true"}

	r = requests.post(baseUrl + reserveUrl, data = json.dumps(reservation1), headers = headers)
	r = requests.post(baseUrl + reserveUrl, data = json.dumps(reservation2), headers = headers)
	r = requests.post(baseUrl + reserveUrl, data = json.dumps(reservation3), headers = headers)
	r = requests.post(baseUrl + reserveUrl, data = json.dumps(reservation4), headers = headers)
	r = requests.post(baseUrl + reserveUrl, data = json.dumps(reservation5), headers = headers)

if __name__ == '__main__':
	baseUrl = 'https://localhost:8443/'
	headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
	resetUrl = '/admin/init'
	requests.get(baseUrl + resetUrl, headers = headers)

	addUsers()
	addResources()
	addReservations()

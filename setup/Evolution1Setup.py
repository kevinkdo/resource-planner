import unittest
import requests
import json


def addUsers():
	baseUrl = 'http://localhost/'
	headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
	userUrl = 'api/users'
	resourceUrl = 'api/resources'
	reserveUrl = 'api/reservations'

	newUser1 = {"email":"jiaweizhang95@gmail.com", "password":"password", "should_email":'true', "username":"jiaweizhang95"}
	newUser2 = {"email":"randomguy@gmail.com", "password":"password", "should_email":'false', "username":"randomguy"}

	r1 = requests.post(baseUrl + userUrl, data = json.dumps(newUser1), headers = headers)
	r2 = requests.post(baseUrl + userUrl, data = json.dumps(newUser2), headers = headers)

def addResources():
	baseUrl = 'http://localhost/'
	headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
	userUrl = 'api/users'
	resourceUrl = 'api/resources'
	reserveUrl = 'api/reservations'

	validResource1 = {'name':'Brown Cat', 'description':'A pretty brown cat', 'tags':['animal', 'cute']}
	validResource2 = {'name':'White Dog', 'description':'My dog', 'tags':['animal', 'ferocious']}
	validResource3 = {'name':'Black Kitty', 'description':'Meow, said the kat', 'tags':['animal', 'cute', 'baby']}
	validResource4 = {'name':'Kat Toy', 'description':'Mhmm', 'tags':[]}
	validResource5 = {'name':'Dog Toy', 'description':'Woof-woof', 'tags':['expensive']}

	r1 = requests.post(baseUrl + resourceUrl, data = json.dumps(validResource1), headers = headers)
	r2 = requests.post(baseUrl + resourceUrl, data = json.dumps(validResource2), headers = headers)
	r3 = requests.post(baseUrl + resourceUrl, data = json.dumps(validResource3), headers = headers)
	r4 = requests.post(baseUrl + resourceUrl, data = json.dumps(validResource4), headers = headers)
	r5 = requests.post(baseUrl + resourceUrl, data = json.dumps(validResource5), headers = headers)

def addReservations():
	baseUrl = 'http://localhost/'
	headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
	userUrl = 'api/users'
	resourceUrl = 'api/resources'
	reserveUrl = 'api/reservations'

	reservation1 = {"user_id": "2", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2012-08-06T11:00:00.000Z", "should_email": "true"}
	reservation2 = {"user_id": "2", "resource_id": "1", "begin_time": "2015-08-06T05:54:00.000Z", "end_time": "2017-08-06T11:54:00.000Z", "should_email": "false"}
	reservation3 = {"user_id": "2", "resource_id": "3", "begin_time": "2016-08-06T06:54:00.000Z", "end_time": "2018-08-06T11:34:00.000Z", "should_email": "false"}
	reservation4 = {"user_id": "2", "resource_id": "4", "begin_time": "2016-08-06T07:54:00.000Z", "end_time": "2016-08-06T11:44:00.000Z", "should_email": "false"}
	reservation5 = {"user_id": "1", "resource_id": "5", "begin_time": "2016-08-06T02:54:00.000Z", "end_time": "2016-08-06T11:54:00.000Z", "should_email": "true"}

	r = requests.post(baseUrl + reserveUrl, data = json.dumps(reservation1), headers = headers)
	r = requests.post(baseUrl + reserveUrl, data = json.dumps(reservation2), headers = headers)
	r = requests.post(baseUrl + reserveUrl, data = json.dumps(reservation3), headers = headers)
	r = requests.post(baseUrl + reserveUrl, data = json.dumps(reservation4), headers = headers)
	r = requests.post(baseUrl + reserveUrl, data = json.dumps(reservation5), headers = headers)

if __name__ == '__main__':
	baseUrl = 'http://localhost/'
	headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
	resetUrl = '/admin/init'
	requests.get(baseUrl + resetUrl, headers = headers)

	addUsers()
	addResources()
	addReservations()

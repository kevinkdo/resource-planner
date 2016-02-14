protocol = 'https' # change to http for non SSL
host = 'localhost' 
#host = 'easywebapi.com'
baseUrl = protocol + '://' + host
resetUrl = '/admin/init' 
tagUrl = '/api/tags'
resourceUrl = '/api/resources'
registerUrl = '/auth/register'
loginUrl = '/auth/login'
reserveUrl = '/api/reservations'
userUrl = '/api/users'
headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }


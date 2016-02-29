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
permissionsUrl = '/editablePermissions'
headers = {
  'Accept': 'application/json',
  "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOjEsInN1cGVyX3AiOmZhbHNlLCJyZXNvdXJjZV9wIjp0cnVlLCJyZXNlcnZhdGlvbl9wIjpmYWxzZSwidXNlcl9wIjp0cnVlfQ.186D0TeuA1Nw14W9f4QrYTFAKdU7epPURmQjegj89jQ",
  "Content-Type": "application/json" 
}

#user creation json
userWithAll = {
  "email": "all@a.com",
  "password": "password",
  "should_email": "false",
  "username": "all"
}
userWithUser = {
  "email": "user@a.com",
  "password": "password",
  "should_email": "false",
  "username": "user"
}
userWithResource = {
  "email": "resource@a.com",
  "password": "password",
  "should_email": "false",
  "username": "resource"
}
userWithReserve = {
  "email": "reserve@a.com",
  "password": "password",
  "should_email": "false",
  "username": "reserve"
}

#user login json
loginWithAdmin = {
  "username": "admin",
  "password": "admin",
}
loginWithAll = {
  "username": "all",
  "password": "password",
  "email": "all@a.com"
}
loginWithUser = {
  "username": "user",
  "password": "password",
  "email": "user@a.com"
}
loginWithResource = {
  "username": "resource",
  "password": "password",
  "email": "resource@a.com"
}
loginWithReserve = {
  "username": "reserve",
  "password": "password",
  "email": "reserve@a.com"
}

#user permissions json
allPermissions = {
	"users":[{"user_id":2,"username":"all"}],
	"groups":[],
	"resources":[],
	"system_permissions":{
		"user_permissions":[{"resource_p":True,"reservation_p":True,"user_p":True,"user_id":2}],
		"group_permissions":[]
	},
	"resource_permissions":{
		"user_permissions":[],
		"group_permissions":[]
	}
}
userPermissions = {
	"users":[{"user_id":2,"username":"all"}],
	"groups":[],
	"resources":[],
	"system_permissions":{
		"user_permissions":[{"resource_p":False,"reservation_p":False,"user_p":True,"user_id":2}],
		"group_permissions":[]
	},
	"resource_permissions":{
		"user_permissions":[],
		"group_permissions":[]
	}
}
resourcePermissions = {
	"users":[{"user_id":3,"username":"all"}],
	"groups":[],
	"resources":[],
	"system_permissions":{
		"user_permissionx":[{"resource_p":True,"reservation_p":False,"user_p":False,"user_id":3}],
		"group_permissions":[]
	},
	"resource_permissions":{
		"user_permissions":[],
		"group_permissions":[]
	}
}
reservePermissions = {
	"users":[{"user_id":2,"username":"all"}],
	"groups":[],
	"resources":[],
	"system_permissions":{
		"user_permissions":[{"resource_p":False,"reservation_p":True,"user_p":False,"user_id":2}],
		"group_permissions":[]
	},
	"resource_permissions":{
		"user_permissions":[],
		"group_permissions":[]
	}
}






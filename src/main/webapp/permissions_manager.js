const PermissionsManager = React.createClass({
  getInitialState() {
    return {
      initial_load: true,
      error_msg: "",
      is_error: true
    }
  },

  getResourceUserPermission(resource_id, user_id) {
    var answer = 0;
    this.state.data.resource_permissions.user_permissions.forEach(function(x) {
      if (x.user_id == user_id && x.resource_id == resource_id) {
        answer = x.permission_level;
      }
    });
    return answer;
  },

  cycleResourceUserPermission(resource_id, user_id) {
    this.state.data.resource_permissions.user_permissions.forEach(function(x) {
      if (x.user_id == user_id && x.resource_id == resource_id) {
        x.permission_level += 1;
        if (x.permission_level > 3)
          x.permission_level = 0;
      }
    });
    this.setState(this.state);
  },

  getResourceGroupPermission(resource_id, group_id) {
    var answer = 0;
    this.state.data.resource_permissions.group_permissions.forEach(function(x) {
      if (x.group_id == group_id && x.resource_id == resource_id) {
        answer = x.permission_level;
      }
    });
    return answer;
  },

  cycleResourceGroupPermission(resource_id, group_id) {
    this.state.data.resource_permissions.group_permissions.forEach(function(x) {
      if (x.group_id == group_id && x.resource_id == resource_id) {
        x.permission_level += 1;
        if (x.permission_level > 3)
          x.permission_level = 0;
      }
    });
    this.setState(this.state);
  },

  getSystemUserPermission(field, user_id) {
    var answer = true;
    this.state.data.system_permissions.user_permissions.forEach(function(x) {
      if (x.user_id == user_id) {
        answer = x[field];
      }
    });
    return answer.toString();
  },

  cycleSystemUserPermission(field, user_id) {
    this.state.data.system_permissions.user_permissions.forEach(function(x) {
      if (x.user_id == user_id) {
        x[field] = !x[field];
      }
    });
    this.setState(this.state);
  },

  getSystemGroupPermission(field, group_id) {
    var answer = true;
    this.state.data.system_permissions.group_permissions.forEach(function(x) {
      if (x.group_id == group_id) {
        answer = x[field];
      }
    });
    return answer.toString();
  },

  cycleSystemGroupPermission(field, group_id) {
    this.state.data.system_permissions.group_permissions.forEach(function(x) {
      if (x.group_id == group_id) {
        x[field] = !x[field];
      }
    });
    this.setState(this.state);
  },

  getPermissionText(x) {
    if (x == 0) 
      return "None";
    if (x == 1)
      return "View";
    if (x == 2)
      return "Reserve";
    if (x == 3)
      return "Manage";
  },

  getBackgroundColor(x) {
    if (x === "true")
      return "success";
    if (x === "false")
      return "danger";
    if (x == 0)
      return "danger";
    if (x == 1)
      return "warning";
    if (x == 2)
      return "info";
    if (x == 3)
      return "success";
    return "";
  },

  save() {
    var me = this;
    me.setState({sending: true});
    send_xhr("PUT", "/api/users/" + userId() + "/editablePermissions", localStorage.getItem("session"),
      JSON.stringify(me.state.data),
      function(obj) {
        me.setState({sending: false, error_msg: "Last saved " + new Date().toLocaleString(), is_error: false});
      },
      function(obj) {
        me.setState({
          sending: false,
          is_error: true,
          error_msg: obj.error_msg
        });
      }
    );
  },

  componentDidMount() {
    var me = this;
    send_xhr("GET", "/api/users/" + userId() + "/editablePermissions", localStorage.getItem("session"), null,
      function(obj) {
        me.setState({
          data: obj.data,
          initial_load: false
        });
      },
      function(obj) {
        me.setState({
          initial_load: false,
          is_error: true,
          error_msg: obj.error_msg
        });
      }
    );
  },

  makeTable() {
    var me = this;
    var user_ids = me.state.data.users.map(x => x.user_id);
    var group_ids = me.state.data.groups.map(x => x.group_id);
    var resource_ids = me.state.data.resources.map(x => x.resource_id);
    var resource_names = me.state.data.resources.map(x => x.resource_name);
    var user_management = me.state.data.system_permissions.user_permissions.length > 0;
    return (
      <table className="table table-hover">
        <thead>
          <tr>
            <th>User/Group</th>
            {user_management ? <th>Manage Resources</th> : null}
            {user_management ? <th>Manage Reservations</th> : null}
            {user_management ? <th>Manage Users</th> : null}
            {me.state.data.resources.map(resource => <th key={"res" + resource.resource_id}>{resource.resource_name}</th>)}
          </tr>
        </thead>
        <tbody>
          {me.state.data.users.map(function(user) {
            var user_id = user.user_id;
            return (
              <tr key={"tr user" + user_id}>
                <td>{"User: " + user.username}</td>
                {user_management ? <td className={me.getBackgroundColor(me.getSystemUserPermission("resource_p", user_id)) + " pointer noselect"} onClick={() => me.cycleSystemUserPermission("resource_p", user_id)}>{me.getSystemUserPermission("resource_p", user_id)}</td> : null}
                {user_management ? <td className={me.getBackgroundColor(me.getSystemUserPermission("reservation_p", user_id)) + " pointer noselect"} onClick={() => me.cycleSystemUserPermission("reservation_p", user_id)}>{me.getSystemUserPermission("reservation_p", user_id)}</td> : null}
                {user_management ? <td className={me.getBackgroundColor(me.getSystemUserPermission("user_p", user_id)) + " pointer noselect"} onClick={() => me.cycleSystemUserPermission("user_p", user_id)}>{me.getSystemUserPermission("user_p", user_id)}</td> : null}
                {
                  resource_ids.map(resource_id => <td className={me.getBackgroundColor(me.getResourceUserPermission(resource_id, user_id)) + " pointer noselect"} onClick={() => me.cycleResourceUserPermission(resource_id, user_id)} key={"permuser" + resource_id + " " + user_id}>{
                    me.getPermissionText(me.getResourceUserPermission(resource_id, user_id))}</td>)
                }
              </tr>
            );
          })}
          {me.state.data.groups.map(function(group) {
            var group_id = group.group_id;
            return (
              <tr key={"tr group" + group_id}>
                <td>{"Group: " + group.group_name}</td>
                {user_management ? <td className={me.getBackgroundColor(me.getSystemGroupPermission("resource_p", group_id)) + " pointer noselect"} onClick={() => me.cycleSystemGroupPermission("resource_p", group_id)}>{me.getSystemGroupPermission("resource_p", group_id)}</td> : null}
                {user_management ? <td className={me.getBackgroundColor(me.getSystemGroupPermission("reservation_p", group_id)) + " pointer noselect"} onClick={() => me.cycleSystemGroupPermission("reservation_p", group_id)}>{me.getSystemGroupPermission("reservation_p", group_id)}</td> : null}
                {user_management ? <td className={me.getBackgroundColor(me.getSystemGroupPermission("user_p", group_id)) + " pointer noselect"} onClick={() => me.cycleSystemGroupPermission("user_p", group_id)}>{me.getSystemGroupPermission("user_p", group_id)}</td> : null}
                {
                  resource_ids.map(resource_id => <td className={me.getBackgroundColor(me.getResourceGroupPermission(resource_id, group_id)) + " pointer noselect"} onClick={() => me.cycleResourceGroupPermission(resource_id, group_id)} key={"permgroup" + resource_id + " " + group_id}>{me.getPermissionText(me.getResourceGroupPermission(resource_id, group_id))}</td>)
                }
              </tr>
            );
          })}
        </tbody>
      </table>
    );
  },

  render() {
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <h3>Permissions Manager
          <span className="padleft"><button type="button" className="btn btn-primary" onClick={() => this.save()}><span className="glyphicon glyphicon-ok" aria-hidden="true"></span> Save</button></span>
        </h3>
        {!this.state.error_msg ? <div></div> :
          <div className={"alert " + (this.state.is_error ? "alert-danger" : "alert-success")}>
            <strong>{this.state.error_msg}</strong>
          </div>
        }
        {this.state.initial_load ? <Loader /> : this.makeTable()}
      </div>
    );
  }
});

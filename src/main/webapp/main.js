var send_xhr = function(verb, endpoint, token, data, success_callback, error_callback) {
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) {
      if (xhr.status == 200) {
        var responseObject = JSON.parse(xhr.responseText);
        if (responseObject.is_error) {
          error_callback(responseObject);
        } else {
          success_callback(responseObject);
        }
      }
    }
  };
  xhr.open(verb, endpoint, true);
  if (verb != "GET") {
    xhr.setRequestHeader("Content-Type", "application/json");
  }
  xhr.setRequestHeader("Accept", "application/json");
  if (token) {
    xhr.setRequestHeader("Authorization", "Bearer " + token);
  }
  xhr.send(data);
};

//Convert Date object to y-m-d string for date input
var formatDate = function(d) {
  var month = '' + (d.getMonth() + 1);
  var day = '' + d.getDate();
  var year = '' + d.getFullYear();

  while (year.length < 4) { year = '0' + year; }
  if (month.length < 2) month = '0' + month;
  if (day.length < 2) day = '0' + day;

  return [year, month, day].join('-');
};

//Converts Date object to h:m string for time input
var formatTime = function(d) {
  var h = '' + d.getHours();
  var m = '' + d.getMinutes();

  if (h.length < 2) h = '0' + h;
  if (m.length < 2) m = '0' + m;

  return h + ":" + m;
};

//Rounds a date down to the nearest minute
var round = function(d) {
  d.setMilliseconds(0);
  d.setSeconds(0);
  return d;
}

var userId = function() {
  return jwt_decode(localStorage.getItem("session")).user_id;
};

const Router = React.createClass({
  getInitialState() {
    var session = localStorage.getItem("session");
    return {
      route: session ? "reservation_list" : "login",
      view_id: 0
    };
  },

  render() {
    switch (this.state.route) {
      case "login":
        return <Login setPstate={this.setState.bind(this)} pstate={this.state} />
      case "group_manager":
        return <GroupManager setPstate={this.setState.bind(this)} pstate={this.state} />
      case "group_editor":
        return <GroupEditor setPstate={this.setState.bind(this)} pstate={this.state} id={this.state.view_id} />
      case "settings":
        return <Settings setPstate={this.setState.bind(this)} pstate={this.state} />
      case "reservation_list":
        return <ReservationList setPstate={this.setState.bind(this)} pstate={this.state} />
      case "reservation_creator":
        return <ReservationCreator setPstate={this.setState.bind(this)} pstate={this.state} resource_id={this.state.view_id} />
      case "reservation_editor":
        return <ReservationEditor setPstate={this.setState.bind(this)} pstate={this.state} id={this.state.view_id}/>
      case "resource_list":
        return <ResourceList setPstate={this.setState.bind(this)} pstate={this.state} />
      case "resource_creator":
        return <ResourceCreator setPstate={this.setState.bind(this)} pstate={this.state} />
      case "resource_editor":
        return <ResourceEditor setPstate={this.setState.bind(this)} pstate={this.state} id={this.state.view_id}/>
    }
    return <div>ERROR</div>;
  }
});

const Loader = React.createClass({
  render() {
    return (
      <div className="spinner">
        <div className="bounce1"></div>
        <div className="bounce2"></div>
        <div className="bounce3"></div>
      </div>
    )
  }
});

const Navbar = React.createClass({
  logout() {
    localStorage.setItem("session", "");
    this.props.setPstate({ route: "login" });
  },

  getInitialState() {
    return {
      username: ""
    };
  },

  componentDidMount() {
    var me = this;
    send_xhr("GET", "/api/users/" + userId(), localStorage.getItem("session"), null,
      function(obj) {
        obj.data.initial_load = false;
        me.setState({username: obj.data.username});
      },
      function(obj) {
        me.setState({initial_load: false, error_msg: obj.error_msg});
      }
    );
  },

  render() {
    return (
      <nav className="navbar navbar-default">
        <div className="container">
          <div className="navbar-header">
            <button type="button" className="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
              <span className="sr-only">Toggle navigation</span>
              <span className="icon-bar"></span>
              <span className="icon-bar"></span>
              <span className="icon-bar"></span>
            </button>
            <a className="navbar-brand" href="#"><span className="glyphicon glyphicon-pawn" aria-hidden="true"></span></a>
          </div>

          <div className="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            <ul className="nav navbar-nav navbar-left">
              <li className={this.props.pstate.route.indexOf("resource") > -1 ? "active" : ""}><a href="#" onClick={() => this.props.setPstate({route: "resource_list"})}>Resources</a></li>
              <li className={this.props.pstate.route.indexOf("reservation") > -1 ? "active" : ""}><a href="#" onClick={() => this.props.setPstate({route: "reservation_list"})}>Reservations</a></li>
            </ul>
            <ul className="nav navbar-nav navbar-right">
              <li><p className="navbar-text">{this.state.username}</p></li>
              <li className="dropdown">
                <a href="#" className="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"><span className="glyphicon glyphicon-cog" aria-hidden="true"></span></a>
                <ul className="dropdown-menu">
                  <li><a href="#" onClick={() => this.props.setPstate({route: "group_manager"})}>Group Manager</a></li>
                  <li><a href="#" onClick={() => this.props.setPstate({route: "settings"})}>Settings</a></li>
                  <li role="separator" className="divider"></li>
                  <li><a href="#" onClick={this.logout}>Log Out</a></li>
                </ul>
              </li>
            </ul>
          </div>
        </div>
      </nav>
    );
  }
});

const GroupManager = React.createClass({
  getInitialState() {
    return {
      initial_load: false,
      new_group_name: "",
      groups: {}
    };
  },

  newGroup() {
    var new_group_name = prompt();
    var me = this;
    if (new_group_name != null) {
      console.log(new_group_name);
      this.setState({sending: true});
      send_xhr("POST", "/api/groups", localStorage.getItem("session"),
      JSON.stringify({group_name:new_group_name, user_ids: [], resource_p: false, reservation_p: false, user_p: false}),
      function(obj) {
        me.refresh();
      },
      function(obj) {
        me.setState({sending: false, error_msg: obj.error_msg});
      }
    );
    }
  },

  editGroup(id) {
    this.props.setPstate({
      route: "group_editor",
      view_id: id
    });
  },

  refresh() {
    var me = this;
    send_xhr("GET", "/api/groups", localStorage.getItem("session"), null,
      function(obj) {
        var new_groups = {};
        obj.data.forEach(function(x) {
          new_groups[x.group_id] = x;
        });
        me.setState({
          groups: new_groups,
          loading_table: false,
        });
      },
      function(obj) {
        me.setState({
          error_msg: obj.error_msg
        })
      }
    );
  },

  deleteGroup(id) {
    var me = this;
    send_xhr("DELETE", "/api/groups/" + id, localStorage.getItem("session"), null,
      function(obj) {
        me.refresh();
        me.setState({error_msg: ""});       
      },
      function(obj) {
        me.refresh();
        me.setState({error_msg: obj.error_msg});
      }
    );
  },

  componentDidMount() {
    this.refresh();
  },

  render() {
    var me = this;    
    var table = this.state.initial_load ? <Loader /> : (
      <table className="table table-hover">
        <thead>
          <tr>
            <th>Group ID</th>
            <th>Group Name</th>
            <th></th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {Object.keys(me.state.groups).map(id => {
            var x = me.state.groups[id];
            return <tr key={"group " + x.group_id}>
              <td>{x.group_id}</td>
              <td>{x.name}</td>
              <td><a role="button" onClick={() => this.editGroup(x.group_id)}>Edit</a></td>
              <td><a role="button" onClick={() => this.deleteGroup(x.group_id)}>Delete</a></td>
            </tr>
          })}
          {Object.keys(me.state.groups).length > 0 ? null :
            <tr><td className="lead text-center" colSpan="7">No groups to display</td></tr>}
        </tbody>
      </table>
    );
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <h3>Reservations
              <button type="button" className="btn btn-success pull-right" onClick={this.newGroup}><span className="glyphicon glyphicon-plus-sign" aria-hidden="true"></span> New group</button>
          </h3>
          {table}
        </div>
      </div>
    );
  }
});

const GroupEditor = React.createClass({
  set(field, value) {
    this.state[field] = value;
    this.setState(this.state);
  },

  editGroup(evt) {
    var me = this;
    this.setState({sending: true});
    send_xhr("PUT", "/api/groups/" + this.props.id, localStorage.getItem("session"),
      JSON.stringify({group_name: this.state.group_name, user_ids:this.state.user_ids}),
      function(obj) {
        me.props.setPstate({route: "group_manager"});
      },
      function(obj) {
        me.setState({sending: false, error_msg: obj.error_msg});
      }
    );
  },

  cancel() {
    this.props.setPstate({ route: "group_manager" });
  },

  addMember() {
    this.state.user_ids.push("");
    this.setState({user_ids: this.state.user_ids});
  },

  setMember(evt, i) {
    this.state.user_ids[i] = evt.target.value;
    this.setState({user_ids: this.state.user_ids});
  },

  getInitialState() {
    return {
      group_name: "",
      initial_load: false,
      error_msg: "",
      user_ids: [""]
    };
  },

  render() {
    var last_member = this.state.user_ids[this.state.user_ids.length-1];
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="col-md-6 col-md-offset-3">
              {this.state.initial_load ? <Loader /> :
                <form>
                  <legend>Edit group {this.props.id}</legend>
                  {!this.state.error_msg ? <div></div> :
                    <div className="alert alert-danger">
                      <strong>{this.state.error_msg}</strong>
                    </div>
                  }
                  <div className="form-group">
                    <label>Name</label>
                    <input type="text" className="form-control" placeholder="Name" value={this.state.group_name} onChange={(evt) => this.set("group_name", evt.target.value)}/>
                  </div>
                  <div className="form-group">
                  <label>Members</label>
                  <div className="row">
                    <div className="col-md-4">
                      {this.state.user_ids.slice(0, -1).map((x,i) =>
                        <ListInput key={i} addFunc={this.addMember} value={x} index={i} editFunc={this.setMember} placeholder="Optional member" hasAddon={false}/>
                      )}
                      <ListInput addFunc={this.addMember} value={last_member} index={this.state.user_ids.length-1} placeholder="Optional member" editFunc={this.setMember} hasAddon={true}/>
                    </div>
                  </div>
                </div>
                  <div className="btn-toolbar">
                    <button type="submit" className="btn btn-primary" onClick={this.editGroup} disabled={this.state.sending}>Edit group</button>
                    <button type="button" className="btn btn-default" onClick={this.cancel}>Cancel</button>
                  </div>
                </form>
              }
            </div>
          </div>
        </div>
      </div>
    )
  }
});

const Settings = React.createClass({
  set(field, value) {
    this.state[field] = value;
    this.setState(this.state);
  },

  editSettings(evt) {
    evt.preventDefault();
    var me = this;
    this.setState({sending: true});
    send_xhr("PUT", "/api/users/" + userId(), localStorage.getItem("session"),
      JSON.stringify({/*email:this.state.email, username:this.state.username, password:this.state.password, */should_email: this.state.should_email}),
      function(obj) {
        me.props.setPstate({ route: "reservation_list" });
      },
      function(obj) {
        me.setState({sending: false, error_msg: obj.error_msg});
      }
    );
  },

  cancel() {
    this.props.setPstate({
      route: "reservation_list"
    });
  },

  getInitialState() {
    return {
      initial_load: true,
      sending: false,
      email: "",
      username: "",
      password: "",
      should_email: "",
      error_msg: ""
    };
  },

  componentDidMount() {
    var me = this;
    send_xhr("GET", "/api/users/" + userId(), localStorage.getItem("session"), null,
      function(obj) {
        obj.data.initial_load = false;
        me.setState(obj.data);
      },
      function(obj) {
        me.setState({initial_load: false, error_msg: obj.error_msg});
      }
    );
  },

  render() {
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="col-md-6 col-md-offset-3">
              {this.state.initial_load ? <Loader /> :
                <form>
                  <legend>User Settings</legend>
                  {!this.state.error_msg ? <div></div> :
                    <div className="alert alert-danger">
                      <strong>{this.state.error_msg}</strong>
                    </div>
                  }
                  <div className="form-group">
                    <label htmlFor="settings_user_id">User ID: {userId()}</label>
                  </div>
                  <div className="form-group">
                    <label htmlFor="settings_email">Email</label>
                    <input type="email" className="form-control" id="settings_email" placeholder="Email" value={this.state.email} onChange={(evt)=>this.set("email", evt.target.value)} disabled/>
                  </div>
                  <div className="form-group">
                    <label htmlFor="settings_username">Username</label>
                    <input type="text" className="form-control" id="settings_username" placeholder="Username" value={this.state.username} onChange={(evt)=>this.set("username", evt.target.value)} disabled/>
                  </div>
                  {/*<div className="form-group">
                    <label htmlFor="settings_password">Password</label>
                    <input type="password" className="form-control" id="settings_password" placeholder="Password" value={this.state.password} onChange={(evt)=>this.set("password", evt.target.value)} disabled/>
                  </div>*/}
                  <div className="checkbox">
                    <label htmlFor="settings_should_email"><input type="checkbox" id="settings_should_email" checked={this.state.should_email} onChange={(evt)=>this.set("should_email", evt.target.checked)}/> Enable email reminders</label>
                  </div>
                  <div className="btn-toolbar">
                    <button type="submit" className="btn btn-primary" onClick={this.editSettings} disabled={this.state.sending}>Edit user</button>
                    <button type="button" className="btn btn-default" onClick={this.cancel}>Cancel</button>
                  </div>
                </form>
              }
            </div>
          </div>
        </div>
      </div>
    );
  }
});

const ReservationList = React.createClass({
  getInitialState() {
    var now = new Date();
    var start = new Date();
    var end = new Date();
    start.setMonth(now.getMonth() - 1);
    end.setMonth(now.getMonth() + 1);
    var start_date = formatDate(start);
    var start_time = formatTime(start);
    var end_date = formatDate(end);
    var end_time = formatTime(end);    
    return {
      loading_tags: true,
      loading_table: true,
      tags: [],
      start_date: start_date,
      start_time: start_time,
      end_date: end_date,
      end_time: end_time,
      reservations: {},
      error_msg: "",
      resource_id: ""
    };
  },

  cycleState(tag_name) {
    var tags = this.state.tags;
    tags.forEach(function(x) {
      if (x.name == tag_name) {
        if (x.state == "Required") x.state = "Excluded";
        else if (x.state == "Excluded") x.state = "";
        else x.state = "Required";
      }
    });
    this.setState({tags: tags});
  },

  set(field, value) {
    this.state[field] = value;
    this.setState(this.state);
  },

  getDateObject(dateStr, timeStr) {
    var date = new Date();
    var dateParts = dateStr.split('-');
    date.setFullYear(dateParts[0]);
    date.setMonth(dateParts[1]-1);
    date.setDate(dateParts[2]);
    
    var timeParts = timeStr.split(':');
    date.setHours(timeParts[0]);
    date.setMinutes(timeParts[1]);
    
    return date;
  },

  //http://stackoverflow.com/questions/1353684/detecting-an-invalid-date-date-instance-in-javascript
  isValidDate(date) {
  if ( Object.prototype.toString.call(date) !== "[object Date]" )
    return false;
  return !isNaN(date.getTime());
  },

  editReservation(id) {
    this.props.setPstate({
      route: "reservation_editor",
      view_id: id
    });
  },

  deleteReservation(id) {
    var me = this;
    send_xhr("DELETE", "/api/reservations/" + id, localStorage.getItem("session"), null,
      function(obj) {
        me.refresh();
        me.setState({error_msg: ""});
      },
      function(obj) {
        me.refresh();
        me.setState({error_msg: obj.error_msg});
      }
    );
  },

  refresh() {
    var me = this;
    var required_tags_str = this.state.tags.filter(x => x.state=="Required").map(x => x.name).join(",");
    var excluded_tags_str = this.state.tags.filter(x => x.state=="Excluded").map(x => x.name).join(",");
    send_xhr("GET", "/api/reservations/?start=" + round(this.getDateObject(this.state.start_date, this.state.start_time)).toISOString() + "&end=" + round(this.getDateObject(this.state.end_date, this.state.end_time)).toISOString() + "&required_tags=" + required_tags_str + "&excluded_tags=" + excluded_tags_str + "&resource_ids=" + this.state.resource_id, localStorage.getItem("session"), null,
      function(obj) {
        var new_reservations = {};
          obj.data.forEach(function(x) {
            new_reservations[x.reservation_id] = x;
          });
        me.setState({
          reservations: new_reservations,
          loading_table: false,
          //error_msg: ""
        });
      },
      function(obj) {
        me.setState({
          loading_table: false,
          error_msg: obj.error_msg
        });
      }
    );
  },

  componentDidMount() {
    var me = this;
    this.refresh();
    send_xhr("GET", "/api/tags", localStorage.getItem("session"), null,
      function(obj) {
        me.setState({
          tags: obj.data.tags.map(x => ({name: x, state: ""})),
          loading_tags: false
        });
      },
      function(obj) {
        me.setState({
          loading_tags: false,
          error_msg: obj.error_msg
        });
      }
    );
  },

  render() {
    var me = this;
    var leftpane =
      <div>
        <h3></h3>
        <div className="panel panel-primary">
          <div className="panel-heading">
            <h3 className="panel-title">Display settings</h3>
          </div>
          <div className="panel-body">
            <button type="button" className="btn btn-primary" onClick={this.refresh}>Load reservations</button>
            <h4>Start</h4>
              <input type="date" className="form-control" id="reservation_list_start_date" value={this.state.start_date} onChange={(evt) => this.set('start_date', evt.target.value)}/>
              <input type="time" className="form-control" id="reservation_list_start_time" value={this.state.start_time} onChange={(evt) => this.set('start_time', evt.target.value)}/>
            <h4>End</h4>
              <input type="date" className="form-control" id="reservation_list_end_date" value={this.state.end_date} onChange={(evt) => this.set('end_date', evt.target.value)}/>
              <input type="time" className="form-control" id="reservation_list_end_time" value={this.state.end_time} onChange={(evt) => this.set('end_time', evt.target.value)}/>
            <h4>Resource ID</h4>
              <input type="number" className="form-control" id="reservation_list_resource_id" value={this.state.resource_id} onChange={(evt) => this.set("resource_id", evt.target.value)}/>
            <h4>Tags</h4>
            {this.state.loading_tags ? <Loader /> : <div>
              <ul className="list-group">
                {this.state.tags.map(x =>
                  <a key={"reservationtag" + x.name} href="#" className="list-group-item" onClick={(evt) => {evt.preventDefault(); me.cycleState(x.name)}}>{x.name}<span className="badge">{x.state}</span></a>
                )}
              </ul>
              {Object.keys(me.state.tags).length > 0 ? null :
                  <div className="lead text-center">No tags to display</div>}
              </div>
            }
          </div>
        </div>
      </div>
    var rightpane = this.state.loading_table ? <Loader /> : (
      <table className="table table-hover">
        <thead>
          <tr>
            <th>ID</th>
            <th>Resource</th>
            <th>User</th>
            <th>Start</th>
            <th>End</th>
            <th></th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {Object.keys(me.state.reservations).map(id => {
            var x = me.state.reservations[id];
            return <tr key={"reservation " + x.reservation_id}>
              <td>{x.reservation_id}</td>
              <td>{x.resource.name}</td>
              <td>{x.user.username}</td>
              <td>{new Date(x.begin_time).toLocaleString()}</td>
              <td>{new Date(x.end_time).toLocaleString()}</td>
              <td><a role="button" onClick={() => this.editReservation(x.reservation_id)}>Edit</a></td>
              <td><a role="button" onClick={() => this.deleteReservation(x.reservation_id)}>Delete</a></td>
            </tr>
          })}
          {Object.keys(me.state.reservations).length > 0 ? null :
            <tr><td className="lead text-center" colSpan="7">No reservations in this timespan</td></tr>}
        </tbody>
      </table>
    );
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="col-md-3">
              {leftpane}
            </div>
            <div className="col-md-9">
              <h3>Reservations</h3>
              {!this.state.error_msg ? <div></div> :
                <div className="alert alert-danger">
                  <strong>{this.state.error_msg}</strong>
                </div>
              }
              {rightpane}
            </div>
          </div>
        </div>
      </div>
    )
  }
});

const ResourceList = React.createClass({
  getInitialState() {
    return {
      loading_tags: true,
      loading_table: true,
      tags: {},
      resources: {},
      error_msg: ""
    };
  },

  cycleState(tag_name) {
    var tags = this.state.tags;
    Object.keys(tags).forEach(function(tag) {
      if (tag == tag_name) {
        if (tags[tag] == "Required") tags[tag] = "Excluded";
        else if (tags[tag] == "Excluded") tags[tag] = "";
        else tags[tag] = "Required";
      }
    });
    this.setState({tags: tags});
  },

  editResource(id) {
    this.props.setPstate({
      route: "resource_editor",
      view_id: id
    });
  },

  deleteResource(id) {
    var me = this;
    send_xhr("GET", "/api/resources/" + id + "/candelete", localStorage.getItem("session"), null,
      function(obj) {
        var confirmed_delete = obj.data.canDelete;

        if (!confirmed_delete) {
          confirmed_delete = confirm("Resource " + id + " has current and/or future reservations. Really delete?");
        }

        if (confirmed_delete) {
          send_xhr("DELETE", "/api/resources/" + id, localStorage.getItem("session"), null,
            function(obj) {
              me.refresh();
              me.setState({error_msg: ""});
            },
            function(obj) {
              me.refresh();
              me.setState({error_msg: obj.error_msg});
            }
          );
        }
      },
      function(obj) {
        me.refresh();
        me.setState({error_msg: obj.error_msg});
      }
    );
  },

  refresh() {
    var me = this;
    var required_tags_str = Object.keys(this.state.tags).filter(x => this.state.tags[x] == "Required").join(",");
    var excluded_tags_str = Object.keys(this.state.tags).filter(x => this.state.tags[x] == "Excluded").join(",");
    send_xhr("GET", "/api/resources/?required_tags=" + required_tags_str + "&excluded_tags=" + excluded_tags_str, localStorage.getItem("session"), null,
      function(obj) {
        var new_resources = {};
        obj.data.resources.forEach(function(x) {
          new_resources[x.resource_id] = x;
        });
        me.setState({
          resources: new_resources,
          loading_table: false,
          //error_msg: ""
        });
      },
      function(obj) {
        me.setState({
          error_msg: obj.error_msg
        });
      }
    );
    send_xhr("GET", "/api/tags", localStorage.getItem("session"), null,
      function(obj) {
        var new_tags = {};
        obj.data.tags.forEach(function(x) {
          new_tags[x] = me.state.tags[x] ? me.state.tags[x] : ""
        });
        me.setState({
          tags: new_tags,
          loading_tags: false
        });
      },
      function(obj) {
        me.setState({
          loading_tags: false,
          error_msg: error_msg
        });
      }
    );
  },

  componentDidMount() {
    this.refresh();
  },

  render() {
    var me = this;
    var leftpane =
      <div>
        <h3></h3>
        <div className="panel panel-primary">
          <div className="panel-heading">
            <h3 className="panel-title">Display settings</h3>
          </div>
          <div className="panel-body">
            <button type="button" className="btn btn-primary" onClick={this.refresh}>Load resources</button>
            <h4>Tags</h4>
            {this.state.loading_tags ? <Loader /> :
              <div>
                <ul className="list-group">
                  {Object.keys(this.state.tags).map(tag =>
                    <a key={"resourcetag" + tag} href="#" className="list-group-item" onClick={(evt) => {evt.preventDefault(); me.cycleState(tag)}}>{tag}<span className="badge">{this.state.tags[tag]}</span></a>
                  )}
                </ul>
                {Object.keys(me.state.tags).length > 0 ? null :
                  <div className="lead text-center">No tags to display</div>}
              </div>
            }
          </div>
        </div>
      </div>
    var rightpane = this.state.loading_table ? <Loader /> : (
      <table className="table table-hover">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Description</th>
            <th>Tags</th>
            <th></th>
            <th></th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {Object.keys(me.state.resources).map(id => {
            var x = me.state.resources[id];
            return <tr key={"resource " + id}>
              <td>{id}</td>
              <td>{x.name}</td>
              <td>{x.description}</td>
              <td>{x.tags.join(",")}</td>
              <td><a role="button" onClick={() => this.editResource(id)}>Edit</a></td>
              <td><a role="button" onClick={() => this.deleteResource(id)}>Delete</a></td>
              <td><a role="button" onClick={() => this.props.setPstate({route: "reservation_creator", view_id: id})}>Reserve</a></td>
            </tr>
          })}
          {Object.keys(me.state.resources).length > 0 ? null :
            <tr><td className="lead text-center" colSpan="7">No resources to display</td></tr>}
        </tbody>
      </table>
    );
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="col-md-3">
              {leftpane}
            </div>
            <div className="col-md-9">
              <h3>Resources <button type="button" className="btn btn-success pull-right" onClick={() => this.props.setPstate({route: "resource_creator"})}><span className="glyphicon glyphicon-time" aria-hidden="true"></span> New resource</button></h3>
              {!this.state.error_msg ? <div></div> :
                <div className="alert alert-danger">
                  <strong>{this.state.error_msg}</strong>
                </div>
              }
              {rightpane}
            </div>
          </div>
        </div>
      </div>
    )
  }
});

/* Generic input for lists. Props are addFunc (called when user clicks the plus sign), value (string or number), index (int), editFunc (called onChange), placeholder (string), hasAddon (boolean)
*/
const ListInput = React.createClass({
  render() {
    var add_on = !this.props.hasAddon ? null :
      <span className="input-group-btn">
        <button className="btn btn-default" type="button" onClick={this.props.addFunc}><span className="glyphicon glyphicon-plus-sign" aria-hidden="true"></span></button>
      </span>;

    return (
      <div className="input-group">
        <input type="text" className="form-control" placeholder={this.props.placeholder} onChange={(evt) => this.props.editFunc(evt, this.props.index)} value={this.props.value}/>
        {add_on}
      </div>
    );
  }
});

const ResourceCreator = React.createClass({
  createResource(evt) {
    evt.preventDefault();
    var me = this;
    this.setState({sending: true});
    send_xhr("POST", "/api/resources", localStorage.getItem("session"),
      JSON.stringify({name:this.state.name, description:this.state.description, tags: this.state.tags.filter(x => x.length > 0)}),
      function(obj) {
        me.props.setPstate({ route: "resource_list" });
      },
      function(obj) {
        me.setState({sending: false, error_msg: obj.error_msg});
      }
    );
  },

  cancel() {
    this.props.setPstate({ route: "resource_list" });
  },

  addTag() {
    this.state.tags.push("");
    this.setState({tags: this.state.tags});
  },

  setName(evt) {
    this.setState({name: evt.target.value});
  },

  setDescription(evt) {
    this.setState({description: evt.target.value});
  },

  setTag(evt, i) {
    this.state.tags[i] = evt.target.value;
    this.setState({tags: this.state.tags});
  },

  getInitialState() {
    return {
      name: "",
      description: "",
      tags: [""],
      error_msg: ""
    };
  },

  render() {
    var last_tag = this.state.tags[this.state.tags.length-1];
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="col-md-6 col-md-offset-3">
              <form>
                <legend>New resource</legend>
                {!this.state.error_msg ? <div></div> :
                  <div className="alert alert-danger">
                    <strong>{this.state.error_msg}</strong>
                  </div>
                }
                <div className="form-group">
                  <label htmlFor="resource_creator_name">Name</label>
                  <input type="text" className="form-control" id="resource_creator_name" placeholder="Name" value={this.state.name} onChange={this.setName}/>
                </div>
                <div className="form-group">
                  <label htmlFor="resource_creator_description">Description</label>
                  <input type="text" className="form-control" id="resource_creator_description" placeholder="Description" value={this.state.description} onChange={this.setDescription}/>
                </div>
                <div className="form-group">
                  <label htmlFor="resource_creator_tags">Tags</label>
                  <div className="row">
                    <div className="col-md-4">
                      {this.state.tags.slice(0, -1).map((x,i) =>
                        <ListInput key={i} addFunc={this.addTag} value={x} index={i} editFunc={this.setTag} placeholder="Optional tag"hasAddon={false}/>
                      )}
                      <ListInput addFunc={this.addTag} value={last_tag} index={this.state.tags.length-1} placeholder="Optional tag" editFunc={this.setTag} hasAddon={true}/>
                    </div>
                  </div>
                </div>
                <div className="btn-toolbar">
                  <button type="submit" className="btn btn-primary" onClick={this.createResource} disabled={this.state.sending}>Create resource</button>
                  <button type="button" className="btn btn-default" onClick={this.cancel}>Cancel</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    )
  }
});

const ReservationCreator = React.createClass({
  createReservation(evt) {
    evt.preventDefault();
    var me = this;
    this.setState({sending: true});
    send_xhr("POST", "/api/reservations", localStorage.getItem("session"),
      JSON.stringify({user_id: this.state.user_id, resource_id:this.state.resource_id, begin_time: round(this.getDateObject(this.state.start_date, this.state.start_time)).toISOString(), end_time: round(this.getDateObject(this.state.end_date, this.state.end_time)).toISOString(), should_email:this.state.should_email}),
      function(obj) {
        me.props.setPstate({ route: "reservation_list" });
      },
      function(obj) {
        me.setState({sending: false, error_msg: obj.error_msg});
      }
    );
  },

  cancel() {
    this.props.setPstate({ route: "resource_list" });
  },

  set(field, value) {
    this.state[field] = value;
    this.setState(this.state);
  },

  getDateObject(dateStr, timeStr) {
    var date = new Date();
    var dateParts = dateStr.split('-');
    date.setFullYear(dateParts[0]);
    date.setMonth(dateParts[1]-1);
    date.setDate(dateParts[2]);
    
    var timeParts = timeStr.split(':');
    date.setHours(timeParts[0]);
    date.setMinutes(timeParts[1]);
    
    return date;
  },

  getInitialState() {
    var now = new Date();
    var start_date = formatDate(now)
    var start_time = formatTime(now)
    var end_date = formatDate(now)
    var end_time = formatTime(now)
    return {
      sending: false,
      resource_id: this.props.resource_id ? this.props.resource_id : 0,
      user_id: userId(),
      start_date: start_date,
      start_time: start_time,
      end_date: end_date,
      end_time: end_time,
      should_email: false,
      error_msg: ""
    };
  },

  render() {
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="col-md-6 col-md-offset-3">
              <form>
                <legend>New reservation</legend>
                {!this.state.error_msg ? <div></div> :
                  <div className="alert alert-danger">
                    <strong>{this.state.error_msg}</strong>
                  </div>
                }
                <div className="form-group">
                  <label htmlFor="reservation_creator_resource">Resource ID</label>
                  <input type="number" className="form-control" id="reservation_creator_resource_id" placeholder="Resource ID" value={this.state.resource_id} onChange={(evt)=>this.set("resource_id", evt.target.value)}/>
                </div>
                <div className="form-group">
                  <label htmlFor="reservation_creator_user_id">User ID (yours by default)</label>
                  <input type="number" className="form-control" id="reservation_creator_user_id" placeholder="User ID" value={this.state.user_id} onChange={(evt)=>this.set("user_id", evt.target.value)}/>
                </div>
                <div className="form-group">
                  <label htmlFor="reservation_creator_start_date">Start Date</label>
                  <input type="date" className="form-control" id="reservation_creator_start_date" value={this.state.start_date} onChange={(evt)=>this.set("start_date", evt.target.value)} />
                </div>
                <div className="form-group">
                  <label htmlFor="reservation_creator_start_time">Start Time</label>
                  <input type="time" className="form-control" id="reservation_creator_start_time" value={this.state.start_time} onChange={(evt)=>this.set("start_time", evt.target.value)}/>
                </div>
                <div className="form-group">
                  <label htmlFor="reservation_creator_end_date">End Date</label>
                  <input type="date" className="form-control" id="reservation_creator_end_date" value={this.state.end_date} onChange={(evt)=>this.set("end_date", evt.target.value)} />
                </div>
                <div className="form-group">
                  <label htmlFor="reservation_creator_end_time">End Time</label>
                  <input type="time" className="form-control" id="reservation_creator_end_time" value={this.state.end_time} onChange={(evt)=>this.set("end_time", evt.target.value)}/>
                </div>
                <div className="checkbox">
                  <label htmlFor="reservation_creator_should_email"><input type="checkbox" id="reservation_creator_should_email" checked={this.state.should_email} onChange={(evt)=>this.set("should_email", evt.target.checked)}/> Email reminder</label>
                </div>
                <div className="btn-toolbar">
                  <button type="submit" className="btn btn-primary" onClick={this.createReservation} disabled={this.state.sending}>Reserve</button>
                  <button type="button" className="btn btn-default" onClick={this.cancel}>Cancel</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    )
  }
});

const ReservationEditor = React.createClass({
  editReservation(evt) {
    evt.preventDefault();
    var me = this;
    this.setState({sending: true});
    send_xhr("PUT", "/api/reservations/" + this.props.id, localStorage.getItem("session"),
      JSON.stringify({user_id: this.state.user_id, resource_id:this.state.resource_id, begin_time: round(this.getDateObject(this.state.start_date, this.state.start_time)).toISOString(), end_time: round(this.getDateObject(this.state.end_date, this.state.end_time)).toISOString(), should_email:this.state.should_email}),
      function(obj) {
        me.props.setPstate({ route: "reservation_list" });
      },
      function(obj) {
        me.setState({sending: false, error_msg: obj.error_msg});
      }
    );
  },

  cancel() {
    this.props.setPstate({ route: "reservation_list" });
  },

  set(field, value) {
    this.state[field] = value;
    this.setState(this.state);
  },

  getDateObject(dateStr, timeStr) {
    var date = new Date();
    var dateParts = dateStr.split('-');
    date.setFullYear(dateParts[0]);
    date.setMonth(dateParts[1]-1);
    date.setDate(dateParts[2]);
    
    var timeParts = timeStr.split(':');
    date.setHours(timeParts[0]);
    date.setMinutes(timeParts[1]);
    
    return date;
  },

  getInitialState() {
    var now = new Date();
    var start_date = formatDate(now)
    var start_time = formatTime(now)
    var end_date = formatDate(now)
    var end_time = formatTime(now)
    return {
      initial_load: true,
      sending: false,
      resource_id: 0,
      user_id: userId(),
      start_date: start_date,
      start_time: start_time,
      end_date: end_date,
      end_time: end_time,
      should_email: false,
      error_msg: ""
    };
  },

  componentDidMount() {
    var me = this;
    send_xhr("GET", "/api/reservations/" + this.props.id, localStorage.getItem("session"), null,
      function(obj) {
        me.setState({
          resource_id: obj.data.resource.resource_id,
          user_id: obj.data.user.user_id,
          start_date: formatDate(new Date(obj.data.begin_time)),
          start_time: formatTime(new Date(obj.data.begin_time)),
          end_date: formatDate(new Date(obj.data.end_time)),
          end_time: formatTime(new Date(obj.data.end_time)),
          should_email: obj.data.should_email,
          error_msg: "",
          initial_load: false
        });
      },
      function(obj) {
        me.setState({initial_load: false, error_msg: obj.error_msg});
      }
    );
  },

  render() {
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="col-md-6 col-md-offset-3">
              {this.state.initial_load ? <Loader /> :
                <form>
                  <legend>Edit reservation {this.props.id}</legend>
                  {!this.state.error_msg ? <div></div> :
                    <div className="alert alert-danger">
                      <strong>{this.state.error_msg}</strong>
                    </div>
                  }
                  <div className="form-group">
                    <label htmlFor="reservation_creator_resource">Resource ID</label>
                    <input type="number" className="form-control" id="reservation_creator_resource_id" placeholder="Resource ID" value={this.state.resource_id} onChange={(evt)=>this.set("resource_id", evt.target.value)}/>
                  </div>
                  <div className="form-group">
                    <label htmlFor="reservation_creator_user_id">User ID</label>
                    <input type="number" className="form-control" id="reservation_creator_user_id" placeholder="User ID" value={this.state.user_id} onChange={(evt)=>this.set("user_id", evt.target.value)}/>
                  </div>
                  <div className="form-group">
                    <label htmlFor="reservation_creator_start_date">Start Date</label>
                    <input type="date" className="form-control" id="reservation_creator_start_date" value={this.state.start_date} onChange={(evt)=>this.set("start_date", evt.target.value)} />
                  </div>
                  <div className="form-group">
                    <label htmlFor="reservation_creator_start_time">Start Time</label>
                    <input type="time" className="form-control" id="reservation_creator_start_time" value={this.state.start_time} onChange={(evt)=>this.set("start_time", evt.target.value)}/>
                  </div>
                  <div className="form-group">
                    <label htmlFor="reservation_creator_end_date">End Date</label>
                    <input type="date" className="form-control" id="reservation_creator_end_date" value={this.state.end_date} onChange={(evt)=>this.set("end_date", evt.target.value)} />
                  </div>
                  <div className="form-group">
                    <label htmlFor="reservation_creator_end_time">End Time</label>
                    <input type="time" className="form-control" id="reservation_creator_end_time" value={this.state.end_time} onChange={(evt)=>this.set("end_time", evt.target.value)}/>
                  </div>
                  <div className="checkbox">
                    <label htmlFor="reservation_creator_should_email"><input type="checkbox" id="reservation_creator_should_email" checked={this.state.should_email} onChange={(evt)=>this.set("should_email", evt.target.checked)}/> Email reminder</label>
                  </div>
                  <div className="btn-toolbar">
                    <button type="submit" className="btn btn-primary" onClick={this.editReservation} disabled={this.state.sending}>Edit reservation</button>
                    <button type="button" className="btn btn-default" onClick={this.cancel}>Cancel</button>
                  </div>
                </form>
              }
            </div>
          </div>
        </div>
      </div>
    )
  }
});

const ResourceEditor = React.createClass({
  editResource(evt) {
    evt.preventDefault();
    var me = this;
    this.setState({sending: true});
    send_xhr("PUT", "/api/resources/" + this.props.id, localStorage.getItem("session"),
      JSON.stringify({name:this.state.name, description:this.state.description || "", tags: this.state.tags.filter(x => x.length > 0)}),
      function(obj) {
        me.props.setPstate({ route: "resource_list" });
      },
      function(obj) {
        me.setState({sending: false, error_msg: obj.error_msg});
      }
    );
  },

  cancel() {
    this.props.setPstate({ route: "resource_list" });
  },

  addTag() {
    this.state.tags.push("");
    this.setState({tags: this.state.tags});
  },

  setName(evt) {
    this.setState({name: evt.target.value});
  },

  setDescription(evt) {
    this.setState({description: evt.target.value});
  },

  setTag(evt, i) {
    this.state.tags[i] = evt.target.value;
    this.setState({tags: this.state.tags});
  },

  getInitialState() {
    return {
      initial_load: true,
      sending: false,
      name: "",
      description: "",
      tags: [""],
      error_msg: ""
    };
  },

  componentDidMount() {
    var me = this;
    send_xhr("GET", "/api/resources/" + this.props.id, localStorage.getItem("session"), null,
      function(obj) {
        obj.data.initial_load = false;
        obj.data.tags.push("");
        me.setState(obj.data);
      },
      function(obj) {
        me.setState({initial_load: false, error_msg: obj.error_msg});
      }
    );
  },

  render() {
    var last_tag = this.state.tags[this.state.tags.length-1];
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="col-md-6 col-md-offset-3">
              {this.state.initial_load ? <Loader /> :
                <form>
                  <legend>Edit resource {this.props.id}</legend>
                  {!this.state.error_msg ? <div></div> :
                    <div className="alert alert-danger">
                      <strong>{this.state.error_msg}</strong>
                    </div>
                  }
                  <div className="form-group">
                    <label htmlFor="resource_editor_name">Name</label>
                    <input type="text" className="form-control" id="resource_editor_name" placeholder="Name" value={this.state.name} onChange={this.setName}/>
                  </div>
                  <div className="form-group">
                    <label htmlFor="resource_editor_description">Description</label>
                    <input type="text" className="form-control" id="resource_editor_description" placeholder="Description" value={this.state.description} onChange={this.setDescription}/>
                  </div>
                  <div className="form-group">
                    <label htmlFor="resource_editor_tags">Tags</label>
                    <div className="row">
                      <div className="col-md-4">
                        {this.state.tags.slice(0, -1).map((x,i) =>
                          <ListInput key={i} addFunc={this.addTag} value={x} index={i} editFunc={this.setTag} hasAddon={false}/>
                        )}
                        <ListInput addFunc={this.addTag} value={last_tag} index={this.state.tags.length-1} editFunc={this.setTag} hasAddon={true}/>
                      </div>
                    </div>
                  </div>
                  <div className="btn-toolbar">
                    <button type="submit" className="btn btn-primary" onClick={this.editResource} disabled={this.state.sending}>Edit resource</button>
                    <button type="button" className="btn btn-default" onClick={this.cancel}>Cancel</button>
                  </div>
                </form>
              }
            </div>
          </div>
        </div>
      </div>
    )
  }
});

const Login = React.createClass({
  set(field, value) {
    this.state[field] = value;
    this.setState(this.state);
  },

  handleSubmit(evt) {
    evt.preventDefault();
    var me = this;
    send_xhr("POST", "/auth/login", "o",
      JSON.stringify({username:this.state.username, password:this.state.password}),
      function(obj) {
        localStorage.setItem("session", obj.data.token);
        me.props.setPstate({ route: "reservation_list" });
      },
      function(obj) {
        me.setState({error_msg: obj.error_msg});
      }
    );
  },

  getInitialState() {
    return {
      username: "",
      password: "",
      error_msg: ""
    }
  },

  render() {
    return (
      <div className="vertical-center">
        <div className="container">
          <div className="row">
            <div className="col-md-4 col-md-offset-4">
              <h1 className="text-center">Resource Manager</h1>
              <br/>
              <form onSubmit={this.handleSubmit}>
                {!this.state.error_msg ? <div></div> :
                  <div className="alert alert-danger">
                    <strong>{this.state.error_msg}</strong>
                  </div>
                }
                <div className="form-group">
                  <label htmlFor="login_username">Username</label>
                  <input type="text" className="form-control" id="login_username" placeholder="Username" onChange={(evt)=>this.set("username", evt.target.value)} value={this.state.username}/>
                </div>
                <div className="form-group">
                  <label htmlFor="login_password">Password</label>
                  <input type="password" className="form-control" id="login_password" placeholder="Password" onChange={(evt)=>this.set("password", evt.target.value)} value={this.state.password}/>
                </div>
                <button type="submit" className="btn btn-primary">Log In</button>
              </form>
            </div>
          </div>
        </div>
      </div>
    )
  }
});

ReactDOM.render(<Router />, document.getElementById("main"));

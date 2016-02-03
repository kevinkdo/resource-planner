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

const Router = React.createClass({
  getInitialState() {
    var session = localStorage.getItem("session");
    return {
      route: session ? "reservation_list" : "login",
      username: "kevinkdo",
      user_id: 0,
      view_id: 0
    };
  },

  render() {
    switch (this.state.route) {
      case "login":
        return <Login setPstate={this.setState.bind(this)} pstate={this.state} />
      case "admin_console":
        return <AdminConsole setPstate={this.setState.bind(this)} pstate={this.state} />
      case "reservation_list":
        return <ReservationList setPstate={this.setState.bind(this)} pstate={this.state} />
      case "reservation_creator":
        return <ReservationCreator setPstate={this.setState.bind(this)} pstate={this.state} />
      case "reservation_editor":
        return <ReservationEditor setPstate={this.setState.bind(this)} pstate={this.state} />
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

const Navbar = React.createClass({
  logout() {
    localStorage.setItem("session", "");
    this.props.setPstate({ route: "login" });
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
              <li><p className="navbar-text">{this.props.pstate.username}</p></li>
              <li className="dropdown">
                <a href="#" className="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"><span className="glyphicon glyphicon-cog" aria-hidden="true"></span></a>
                <ul className="dropdown-menu">
                  <li><a href="#" onClick={() => this.props.setPstate({route: "admin_console"})}>Admin Console</a></li>
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

const AdminConsole = React.createClass({
  set(field, value) {
    this.state[field] = value;
    this.setState(this.state);
  },

  createUser() {
    console.log("creating user");
  },

  cancel() {
    this.props.setPstate({
      route: "reservation_list"
    });
  },

  getInitialState() {
    return {
      email: "",
      username: "",
      password: ""
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
                <legend>New user</legend>
                <div className="form-group">
                  <label htmlFor="user_creator_email">Email</label>
                  <input type="text" className="form-control" id="user_creator_email" placeholder="Email" value={this.state.email} onChange={(evt)=>this.set("email", evt.target.value)}/>
                </div>
                <div className="form-group">
                  <label htmlFor="user_creator_username">Username</label>
                  <input type="text" className="form-control" id="user_creator_username" placeholder="Username" value={this.state.username} onChange={(evt)=>this.set("username", evt.target.value)}/>
                </div>
                <div className="form-group">
                  <label htmlFor="user_creator_password">Password</label>
                  <input type="text" className="form-control" id="user_creator_username" placeholder="Password" value={this.state.password} onChange={(evt)=>this.set("password", evt.target.value)}/>
                </div>
                <div className="btn-toolbar">
                  <button type="submit" className="btn btn-primary" onClick={this.createUser}>Create user</button>
                  <button type="submit" className="btn btn-default" onClick={this.cancel}>Cancel</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    );
  }
});

const ReservationList = React.createClass({
  getInitialState() {
    return {
      loading_tags: true,
      loading_table: false,
      tags: [],
      start: new Date(),
      end: new Date(),
      reservations: {
        0: {id: 0, resource_id: 0, user_id: 1, start_timestamp: new Date(), end_timestamp: new Date()},
        1: {id: 1, resource_id: 1, user_id: 1, start_timestamp: new Date(), end_timestamp: new Date()},
        2: {id: 2, resource_id: 2, user_id: 1, start_timestamp: new Date(), end_timestamp: new Date()},
        3: {id: 3, resource_id: 2, user_id: 1, start_timestamp: new Date(), end_timestamp: new Date()},
        4: {id: 4, resource_id: 2, user_id: 1, start_timestamp: new Date(), end_timestamp: new Date()}
      },
      resources: {
        0: {name: "laptop classroom", description: "description1", tags: ["laptop", "classroom"]},
        1: {name: "classroom server", description: "description2", tags: ["classroom", "server"]},
        2: {id: 2, name: "projector", description: "description3", tags: ["projector"]}
      },
      users: {
        1: {email: "kevin.kydat.do@gmail.com", username: "kevinkdo", should_email: true}
      }
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

  setDate(field, str) {
    var parts = str.split('-');
    this.state[field].setFullYear(parts[0]);
    this.state[field].setMonth(parts[1]-1);
    this.state[field].setDate(parts[2]);
    this.setState(this.state);
  },

  setTime(field, str) {
    var parts = str.split(':');
    this.state[field].setHours(parts[0]);
    this.state[field].setMinutes(parts[1]);
    this.setState(this.state);
  },

  editReservation(id) {
    this.props.setPstate({
      route: "reservation_editor",
      view_id: id
    });
  },

  deleteReservation(id) {
    console.log("reservation deleted: " + id);
  },

  refresh() {
    console.log("refreshing reservations");
  },

  componentDidMount() {
    var me = this;
    send_xhr("GET", "/api/tags", localStorage.getItem("session"), null,
      function(obj) {
        me.setState({
          tags: obj.data.tags.map(x => ({name: x, state: ""})),
          loading_tags: false
        });
      },
      function(obj) {
        console.log("todo");
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
              <input type="date" className="form-control" id="reservation_list_start_date" value={formatDate(this.state.start)} onChange={(evt) => this.setDate("start", evt.target.value)}/>
              <input type="time" className="form-control" id="reservation_list_start_time" value={formatTime(this.state.start)} onChange={(evt) => this.setTime("start", evt.target.value)}/>
            <h4>End</h4>
              <input type="date" className="form-control" id="reservation_list_end_date" value={formatDate(this.state.end)} value={formatDate(this.state.end)} onChange={(evt) => this.setDate("end", evt.target.value)}/>
              <input type="time" className="form-control" id="reservation_list_end_time" value={formatTime(this.state.end)} value={formatTime(this.state.end)} onChange={(evt) => this.setTime("end", evt.target.value)}/>
            <h4>Tags</h4>
            {this.state.loading_tags ? <div className="loader">Loading...</div> : (
              <ul className="list-group">
                {this.state.tags.map(x =>
                  <a key={"reservationtag" + x.name} href="#" className="list-group-item" onClick={function() {me.cycleState(x.name)}}>{x.name}<span className="badge">{x.state}</span></a>
                )}
              </ul>
            )}
          </div>
        </div>
      </div>
    var rightpane = this.state.loading_table ? <div className="loader">Loading...</div> : (
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
            return <tr key={"reservation " + x.id}>
              <td>{x.id}</td>
              <td>{this.state.resources[x.resource_id].name}</td>
              <td>{this.state.users[x.user_id].username}</td>
              <td>{x.start_timestamp.toLocaleString()}</td>
              <td>{x.end_timestamp.toLocaleString()}</td>
              <td><a role="button" onClick={() => this.editReservation(x.id)}>Edit</a></td>
              <td><a role="button" onClick={() => this.deleteReservation(x.id)}>Delete</a></td>
            </tr>
          })}
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
              <h3>Reservations
              <button type="button" className="btn btn-success pull-right" onClick={() => this.props.setPstate({route: "reservation_creator"})}><span className="glyphicon glyphicon-time" aria-hidden="true"></span> New reservation</button></h3>
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
      tags: [],
      resources: {}
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

  editResource(id) {
    this.props.setPstate({
      route: "resource_editor",
      view_id: id
    });
  },

  deleteResource(id) {
    var me = this;
    send_xhr("DELETE", "/api/resources/" + id, localStorage.getItem("session"), null,
      function(obj) {
        me.loadResources();
      },
      function(obj) {
        console.log("todo");
      }
    );
  },

  loadResources() {
    var me = this;
    var required_tags_str = this.state.tags.filter(x => x.state=="Required").map(x => x.name).join(",");
    var excluded_tags_str = this.state.tags.filter(x => x.state=="Excluded").map(x => x.name).join(",");
    send_xhr("GET", "/api/resources/?required_tags=" + required_tags_str + "&excluded_tags=" + excluded_tags_str, localStorage.getItem("session"), null,
      function(obj) {
        var new_resources = {};
        obj.data.resources.forEach(function(x) {
          new_resources[x.resource_id] = x;
        });
        me.setState({
          resources: new_resources,
          loading_table: false
        });
      },
      function(obj) {
        console.log("todo");
      }
    );
  },

  componentDidMount() {
    var me = this;
    send_xhr("GET", "/api/tags", localStorage.getItem("session"), null,
      function(obj) {
        me.setState({
          tags: obj.data.tags.map(x => ({name: x, state: ""})),
          loading_tags: false
        });
      },
      function(obj) {
        console.log("todo");
      }
    );
    this.loadResources();
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
            <button type="button" className="btn btn-primary" onClick={this.loadResources}>Load resources</button>
            <h4>Tags</h4>
            {this.state.loading_tags ? <div className="loader">Loading...</div> :
              <ul className="list-group">
                {this.state.tags.map(x =>
                  <a key={"resourcetag" + x.name} href="#" className="list-group-item" onClick={function() {me.cycleState(x.name)}}>{x.name}<span className="badge">{x.state}</span></a>
                )}
              </ul>
            }
          </div>
        </div>
      </div>
    var rightpane = this.state.loading_table ? <div className="loader">Loading...</div> : (
      <table className="table table-hover">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Description</th>
            <th>Tags</th>
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
            </tr>
          })}
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
              {rightpane}
            </div>
          </div>
        </div>
      </div>
    )
  }
});

const TagInput = React.createClass({
  render() {
    var add_on = !this.props.hasAddon ? null :
      <span className="input-group-btn">
        <button className="btn btn-default" type="button" onClick={this.props.addTag}><span className="glyphicon glyphicon-plus-sign" aria-hidden="true"></span></button>
      </span>;

    return (
      <div className="input-group">
        <input type="text" className="form-control" id="resource_creator_tags" placeholder="Optional tag" onChange={(evt) => this.props.setTag(evt, this.props.index)} value={this.props.value}/>
        {add_on}
      </div>
    );
  }
});

const ResourceCreator = React.createClass({
  createResource() {
    var me = this;
    this.setState({loading: true});
    send_xhr("POST", "/api/resources", localStorage.getItem("session"),
      JSON.stringify({name:this.state.name, description:this.state.description, tags: this.state.tags.filter(x => x.length > 0)}),
      function(obj) {
        me.props.setPstate({ route: "resource_list" });
      },
      function(obj) {
        me.setState({loading: false});
        console.log("todo");
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
      tags: [""]
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
                        <TagInput key={i} addTag={this.addTag} value={x} index={i} setTag={this.setTag} hasAddon={false}/>
                      )}
                      <TagInput addTag={this.addTag} value={last_tag} index={this.state.tags.length-1} setTag={this.setTag} hasAddon={true}/>
                    </div>
                  </div>
                </div>
                <div className="btn-toolbar">
                  <button type="submit" className={"btn btn-primary" + (this.state.loading ? " disabled" : "")} onClick={this.createResource}>Create resource</button>
                  <button type="submit" className="btn btn-default" onClick={this.cancel}>Cancel</button>
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
  createReservation() {
    var me = this;
    this.setState({loading: true});
    send_xhr("POST", "/api/reservations", localStorage.getItem("session"),
      JSON.stringify({user_id: this.state.user_id, resource_id:this.state.resource_id, begin_time: this.state.start.toISOString(), end_time: this.state.end.toISOString(), should_email:this.state.should_email}),
      function(obj) {
        me.props.setPstate({ route: "reservation_list" });
      },
      function(obj) {
        me.setState({loading: false});
        console.log("todo");
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

  setDate(field, str) {
    var parts = str.split('-');
    this.state[field].setFullYear(parts[0]);
    this.state[field].setMonth(parts[1]-1);
    this.state[field].setDate(parts[2]);
    this.setState(this.state);
  },

  setTime(field, str) {
    var parts = str.split(':');
    this.state[field].setHours(parts[0]);
    this.state[field].setMinutes(parts[1]);
    this.setState(this.state);
  },

  getInitialState() {
    return {
      resource_id: 0,
      user_id: this.props.pstate.user_id,
      start: new Date(),
      end: new Date(),
      should_email: false
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
                <div className="form-group">
                  <label htmlFor="reservation_creator_resource">Resource ID</label>
                  <input type="number" className="form-control" id="reservation_creator_resource_id" placeholder="Resource ID" value={this.state.resourcue_id} onChange={(evt)=>this.set("resource_id", evt.target.value)}/>
                </div>
                <div className="form-group">
                  <label htmlFor="reservation_creator_user_id">User ID (yours by default)</label>
                  <input type="number" className="form-control" id="reservation_creator_user_id" placeholder="User ID" value={this.state.user_id} onChange={(evt)=>this.set("user_id", evt.target.value)}/>
                </div>
                <div className="form-group">
                  <label htmlFor="reservation_creator_start_date">Start Date</label>
                  <input type="date" className="form-control" id="reservation_creator_start_date" value={formatDate(this.state.start)} onChange={(evt)=>this.setDate("start", evt.target.value)} />
                </div>
                <div className="form-group">
                  <label htmlFor="reservation_creator_start_time">Start Time</label>
                  <input type="time" className="form-control" id="reservation_creator_start_time" value={formatTime(this.state.start)} onChange={(evt)=>this.setTime("start", evt.target.value)}/>
                </div>
                <div className="form-group">
                  <label htmlFor="reservation_creator_end_date">End Date</label>
                  <input type="date" className="form-control" id="reservation_creator_end_date" value={formatDate(this.state.end)} onChange={(evt)=>this.setDate("end", evt.target.value)} />
                </div>
                <div className="form-group">
                  <label htmlFor="reservation_creator_end_time">End Time</label>
                  <input type="time" className="form-control" id="reservation_creator_end_time" value={formatTime(this.state.end)} onChange={(evt)=>this.setTime("end", evt.target.value)}/>
                </div>
                <div className="checkbox">
                  <label htmlFor="reservation_creator_should_email"><input type="checkbox" id="reservation_creator_should_email" value={this.state.should_email} onChange={(evt)=>this.set("should_email", evt.target.value)}/> Email reminder</label>
                </div>
                <div className="btn-toolbar">
                  <button type="submit" className="btn btn-primary" onClick={this.createReservation}>Reserve</button>
                  <button type="submit" className="btn btn-default" onClick={this.cancel}>Cancel</button>
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
  render() {
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
        Reservation editor
        </div>
      </div>
    )
  }
});

const ResourceEditor = React.createClass({
  editResource() {
    var me = this;
    this.setState({loading: true});
    send_xhr("PUT", "/api/resources/" + this.props.id, localStorage.getItem("session"),
      JSON.stringify({name:this.state.name, description:this.state.description || "", tags: this.state.tags.filter(x => x.length > 0)}),
      function(obj) {
        me.props.setPstate({ route: "resource_list" });
      },
      function(obj) {
        me.setState({loading: false});
        console.log("todo");
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
      loading: false,
      name: "",
      description: "",
      tags: [""]
    };
  },

  componentDidMount() {
    var me = this;
    send_xhr("GET", "/api/resources/" + this.props.id, localStorage.getItem("session"), null,
      function(obj) {
        obj.data.initial_load = false;
        me.setState(obj.data);
      },
      function(obj) {
        me.setState({initial_load: false});
        console.log("todo");
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
              <form>
                <legend>Edit resource {this.props.id}</legend>
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
                        <TagInput key={i} addTag={this.addTag} value={x} index={i} setTag={this.setTag} hasAddon={false}/>
                      )}
                      <TagInput addTag={this.addTag} value={last_tag} index={this.state.tags.length-1} setTag={this.setTag} hasAddon={true}/>
                    </div>
                  </div>
                </div>
                <div className="btn-toolbar">
                  <button type="submit" className={"btn btn-primary" + (this.state.loading ? " disabled" : "")} onClick={this.editResource}>Create resource</button>
                  <button type="submit" className="btn btn-default" onClick={this.cancel}>Cancel</button>
                </div>
              </form>
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

  handleSubmit() {
    var me = this;
    send_xhr("POST", "/auth/login", "o",
      JSON.stringify({email:this.state.email, password:this.state.password}),
      function(obj) {
        localStorage.setItem("session", obj.data.token);
        me.props.setPstate({ route: "reservation_list" });
      },
      function(obj) {
        console.log("todo");
      }
    );
  },

  getInitialState() {
    return {
      email: "",
      password: ""
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
              <form onSubmit={this.handleSubmit} >
                <div className="form-group">
                  <label htmlFor="login_email">Email address</label>
                  <input type="email" className="form-control" id="login_email" placeholder="Email" onChange={(evt)=>this.set("email", evt.target.value)} value={this.state.email}/>
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

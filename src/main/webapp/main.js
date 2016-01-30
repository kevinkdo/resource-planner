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
  xhr.setRequestHeader("Authorization", "Bearer " + token);
  xhr.send(data);
};

const Router = React.createClass({
  getInitialState() {
    var session = localStorage.getItem("session");
    return {
      route: session ? "reservation_list" : "login",
      username: "kevinkdo",
      user_id: 0
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
        return <ResourceEditor setPstate={this.setState.bind(this)} pstate={this.state} />
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
                  <li><a href="#" onClick={this.logout} >Log Out</a></li>
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
      loading_tags: false,
      loading_table: false,
      tags: [
        {name: "laptop", state: "Required"},
        {name: "classroom", state: "Required"},
        {name: "server", state: "Excluded"},
        {name: "projector", state: ""},
        {name: "other", state: ""}
      ],
      reservations: [
        {id: 0, resource_id: 0, user_id: 1, start_timestamp: new Date(), end_timestamp: new Date()},
        {id: 1, resource_id: 1, user_id: 1, start_timestamp: new Date(), end_timestamp: new Date()},
        {id: 2, resource_id: 2, user_id: 1, start_timestamp: new Date(), end_timestamp: new Date()},
        {id: 3, resource_id: 2, user_id: 1, start_timestamp: new Date(), end_timestamp: new Date()},
        {id: 4, resource_id: 2, user_id: 1, start_timestamp: new Date(), end_timestamp: new Date()}
      ],
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

  render() {
    var me = this;
    var leftpane = this.state.loading_tags ? <div className="loader">Loading...</div> : (
      <div>
        <h3></h3>
        <div className="panel panel-primary">
          <div className="panel-heading">
            <h3 className="panel-title">Display settings</h3>
          </div>
          <div className="panel-body">
            <button type="button" className="btn btn-primary" onClick={this.refresh}>Load reservations</button>
            <h4>Start</h4>
              <input type="date" className="form-control" id="reservation_list_start_date"/>
              <input type="time" className="form-control" id="reservation_list_start_time"/>
            <h4>End</h4>
              <input type="date" className="form-control" id="reservation_list_end_date"/>
              <input type="time" className="form-control" id="reservation_list_end_time"/>
            <h4>Tags</h4>
            <ul className="list-group">
              {this.state.tags.map(x =>
                <a key={x.name} href="#" className="list-group-item" onClick={function() {me.cycleState(x.name)}}>{x.name}<span className="badge">{x.state}</span></a>
              )}
            </ul>
          </div>
        </div>
      </div>
    );
    var rightpane = this.state.loading_table ? <div className="loader">Loading...</div> : (
      <table className="table table-hover">
        <thead>
          <tr>
            <th>Resource</th>
            <th>User</th>
            <th>Start</th>
            <th>End</th>
            <th></th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {this.state.reservations.map(x =>
            <tr key={"reservation " + x.id}>
              <td>{this.state.resources[x.resource_id].name}</td>
              <td>{this.state.users[x.user_id].username}</td>
              <td>{x.start_timestamp.toLocaleString()}</td>
              <td>{x.end_timestamp.toLocaleString()}</td>
              <td><a role="button" onClick={() => this.editReservation(x.id)}>Edit</a></td>
              <td><a role="button" onClick={() => this.deleteReservation(x.id)}>Delete</a></td>
            </tr>
          )}
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
      tags: [
        {name: "laptop", state: "Required"},
        {name: "classroom", state: "Required"},
        {name: "server", state: "Excluded"},
        {name: "projector", state: ""},
        {name: "other", state: ""}
      ],
      resources: {
        0: {name: "laptop classroom", description: "description1", tags: ["laptop", "classroom"]},
        1: {name: "classroom server", description: "description2", tags: ["classroom", "server"]},
        2: {name: "projector", description: "description3", tags: ["projector"]}
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

  editResource(id) {
    this.props.setPstate({
      route: "resource_editor",
      view_id: id
    });
  },

  deleteResource(id) {
    console.log("resource deleted: " + id);
  },

  refresh() {
    console.log("refreshing resources");
  },

  render() {
    var me = this;
    var leftpane = this.state.loading_tags ? <div className="loader">Loading...</div> : (
      <div>
        <h3></h3>
        <div className="panel panel-primary">
          <div className="panel-heading">
            <h3 className="panel-title">Display settings</h3>
          </div>
          <div className="panel-body">
            <button type="button" className="btn btn-primary" onClick={this.refresh}>Load resources</button>
            <h4>Tags</h4>
            <ul className="list-group">
              {this.state.tags.map(x =>
                <a key={x.name} href="#" className="list-group-item" onClick={function() {me.cycleState(x.name)}}>{x.name}<span className="badge">{x.state}</span></a>
              )}
            </ul>
          </div>
        </div>
      </div>
    );
    var rightpane = this.state.loading_table ? <div className="loader">Loading...</div> : (
      <table className="table table-hover">
        <thead>
          <tr>
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
              <td>{x.name}</td>
              <td>{x.description}</td>
              <td>{x.description}</td>
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
    console.log("resource created!");
    this.props.setPstate({ route: "resource_list" });
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
                  <button type="submit" className="btn btn-primary" onClick={this.createResource}>Create resource</button>
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
    console.log("reservation created!");
    this.props.setPstate({ route: "reservation_list" });
  },

  cancel() {
    this.props.setPstate({ route: "reservation_list" });
  },

  setResource(evt) {
    this.setState({resource: evt.target.value});
  },

  setUserId(evt) {
    this.setState({user_id: evt.target.value});
  },

  setStartDate(evt) {
    this.setState({start_date: evt.target.value});
  },

  setStartTime(evt) {
    this.setState({start_time: evt.target.value});
  },

  setEndDate(evt) {
    this.setState({end_date: evt.target.value});
  },

  setEndTime(evt) {
    this.setState({end_time: evt.target.value});
  },

  getInitialState() {
    return {
      resources: ["laptop", "resource 2"],
      resource: "",
      user_id: this.props.pstate.user_id,
      start_date: null,
      start_time: null,
      end_date: null,
      end_time: null
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
                  <label htmlFor="reservation_creator_resource">Resource</label>
                  <select className="form-control" id="reservation_creator_resource" value={this.state.resource} onChange={this.setResource}>
                  {this.state.resources.map(x =>
                    <option key={"resource" + x} value={x}>{x}</option>
                  )}
                  </select>
                </div>
                <div className="form-group">
                  <label htmlFor="resource_creator_user_id">User ID (yours by default)</label>
                  <input type="number" className="form-control" id="resource_creator_user_id" placeholder="User ID" value={this.state.user_id} onChange={this.setUserId}/>
                </div>
                <div className="form-group">
                  <label htmlFor="resource_creator_start_date">Start Date</label>
                  <input type="date" className="form-control" id="resource_creator_start_date" value={this.state.start_date} onChange={this.setStartDate} />
                </div>
                <div className="form-group">
                  <label htmlFor="resource_creator_start_time">Start Time</label>
                  <input type="time" className="form-control" id="resource_creator_start_time" value={this.state.start_time} onChange={this.setStartTime} />
                </div>
                <div className="form-group">
                  <label htmlFor="resource_creator_end_date">End Date</label>
                  <input type="date" className="form-control" id="resource_creator_end_date" value={this.state.end_date} onChange={this.setEndDate} />
                </div>
                <div className="form-group">
                  <label htmlFor="resource_creator_end_time">Start Time</label>
                  <input type="time" className="form-control" id="resource_creator_end_time" value={this.state.end_time} onChange={this.setEndTime} />
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
  render() {
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
        Resource editor
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
    var xhr = send_xhr("POST", "/auth/login", "o",
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

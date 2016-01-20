const Router = React.createClass({
  getInitialState() {
    var session = localStorage.getItem("session");
    return {
      route: session ? "overview" : "login",
      username: "kevinkdo"
    };
  },

  render() {
    switch (this.state.route) {
      case "login":
        return <Login setPstate={this.setState.bind(this)} pstate={this.state} />
      case "overview":
        return <Overview setPstate={this.setState.bind(this)} pstate={this.state} />
      case "resource_creator":
        return <ResourceCreator setPstate={this.setState.bind(this)} pstate={this.state} />
    }
  }
});

const Navbar = React.createClass({
  logout() {
    localStorage.setItem("session", "");
    this.props.setPstate({
      route: "login"
    });
  },

  render() {
    return (
      <nav className="navbar navbar-default">
        <div className="container-fluid">
          <div className="navbar-header">
            <button type="button" className="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
              <span className="sr-only">Toggle navigation</span>
              <span className="icon-bar"></span>
              <span className="icon-bar"></span>
              <span className="icon-bar"></span>
            </button>
            <a className="navbar-brand" href="#">Resource Manager</a>
          </div>

          <div className="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            <ul className="nav navbar-nav navbar-right">
              <li><p className="navbar-text">{this.props.pstate.username}</p></li>
              <li className="dropdown">
                <a href="#" className="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"><span className="glyphicon glyphicon-cog" aria-hidden="true"></span></a>
                <ul className="dropdown-menu">
                  <li><a href="#">Settings</a></li>
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

const Overview = React.createClass({
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

  render() {
    var me = this;
    var leftpane = this.state.loading_tags ? <div className="loader">Loading...</div> : (
      <ul className="list-group">
        {this.state.tags.map(x =>
          <a key={x.name} href="#" className="list-group-item" onClick={function() {me.cycleState(x.name)}}>{x.name}<span className="badge">{x.state}</span></a>
        )}
      </ul>
    );
    var rightpane = this.state.loading_table ? <div className="loader">Loading...</div> : (
      <table className="table table-hover">
        <thead>
          <tr>
            <th>Resource</th>
            <th>User</th>
            <th>Start</th>
            <th>End</th>
          </tr>
        </thead>
        <tbody>
          {this.state.reservations.map(x =>
            <tr key={"reservation " + x.id}><td>{this.state.resources[x.resource_id].name}</td><td>{this.state.users[x.user_id].username}</td><td>{x.start_timestamp.toLocaleString()}</td><td>{x.end_timestamp.toLocaleString()}</td></tr>
          )}
        </tbody>
      </table>
    );
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="btn-group" role="group" aria-label="...">
              <button type="button" className="btn btn-default" onClick={function() {me.props.setPstate({route: "resource_creator"})}}><span className="glyphicon glyphicon-plus-sign" aria-hidden="true"></span> New resource</button>
              <button type="button" className="btn btn-default"><span className="glyphicon glyphicon-time" aria-hidden="true"></span> New reservation</button>
            </div>
          </div>
          
          <div className="row">
            <div className="col-md-3">
              <h3>Tags</h3>
              {leftpane}
            </div>
            <div className="col-md-9">
              <h3>Reservations</h3>
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
        <input type="text" className="form-control" id="resource_creator_tags" placeholder="" onChange={(evt) => this.props.setTag(evt, this.props.index)} value={this.props.value}/>
        {add_on}
      </div>
    );
  }
});

const ResourceCreator = React.createClass({
  createResource() {
    console.log("resource created!");
    this.props.setPstate({
      route: "overview"
    });
  },

  cancel() {
    this.props.setPstate({
      route: "overview"
    });
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

const Login = React.createClass({
  handleSubmit() {
    localStorage.setItem("session", "my_session_id");
    this.props.setPstate({
      route: "overview"
    });
  },

  setEmail(evt) {
    this.setState({email: evt.target.value});
  },

  setPassword(evt) {
    this.setState({password: evt.target.value});
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
                  <label htmlFor="exampleInputEmail1">Email address</label>
                  <input type="email" className="form-control" id="exampleInputEmail1" placeholder="Email" onChange={this.setEmail} value={this.state.email}/>
                </div>
                <div className="form-group">
                  <label htmlFor="exampleInputPassword1">Password</label>
                  <input type="password" className="form-control" id="exampleInputPassword1" placeholder="Password" onChange={this.setPassword} value={this.state.password}/>
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

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
    this.req = send_xhr("GET", "/api/users/" + userId(), localStorage.getItem("session"), null,
      function(obj) {
        obj.data.initial_load = false;
        me.setState({username: obj.data.username});
      },
      function(obj) {
        me.setState({initial_load: false, error_msg: obj.error_msg});
      }
    );
  },

  componentWillUnmount() {
    this.req.abort();
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
            <a className="navbar-brand"><span className="glyphicon glyphicon-pawn" aria-hidden="true"></span></a>
          </div>

          <div className="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            <ul className="nav navbar-nav navbar-left">
              <li className={this.props.pstate.route.indexOf("resource") > -1 ? "active" : ""}><a href="#resource_list/0">Resources</a></li>
              <li className={this.props.pstate.route.indexOf("reservation") > -1 ? "active" : ""}><a href="#reservation_list/0">Reservations</a></li>
              <li className={this.props.pstate.route.indexOf("group") > -1 ? "active" : ""}><a href="#group_manager/0">Groups</a></li>
              <li className={this.props.pstate.route.indexOf("permissions") > -1 ? "active" : ""}><a href="#permissions_manager/0">Permissions Manager</a></li>
            </ul>
            <ul className="nav navbar-nav navbar-right">
              <li><p className="navbar-text">{this.state.username}</p></li>
              <li className="dropdown">
                <a href="#" className="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"><span className="glyphicon glyphicon-cog" aria-hidden="true"></span></a>
                <ul className="dropdown-menu">
                  <li><a href="#admin_console/0" onClick={() => this.props.setPstate({route: "admin_console"})}>Admin Console</a></li>
                  <li><a href="#settings/0" onClick={() => this.props.setPstate({route: "settings"})}>Settings</a></li>
                  <li role="separator" className="divider"></li>
                  <li><a href="#login/0" onClick={this.logout}>Log Out</a></li>
                </ul>
              </li>
            </ul>
          </div>
        </div>
      </nav>
    );
  }
});

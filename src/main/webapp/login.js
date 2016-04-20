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
        sessionStorage.setItem("session", obj.data.token);
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
                  <a type="button" className="center-block btn btn-primary" href="https://oauth.oit.duke.edu/oauth/authorize.php?client_id=ECE458_Resource_manager2&state=0.6590120431501418&response_type=token&redirect_uri=https://colab-sbx-304.oit.duke.edu/oauth">Login with Duke NetID</a>
                </div>
                <hr />
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

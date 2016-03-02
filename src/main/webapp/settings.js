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

const GroupEditor = React.createClass({
  set(field, value) {
    this.state[field] = value;
    this.setState(this.state);
  },

  editGroup(evt) {
    var me = this;
    this.setState({sending: true});
    send_xhr("PUT", "/api/groups/" + this.props.pstate.view_id, localStorage.getItem("session"),
      JSON.stringify({group_name: this.state.group_name, user_ids: this.state.user_ids.filter(x => x.length > 0)}),
      function(obj) {
        me.props.setPstate({route: "group_manager", is_error: false, error_msg: "Successfully edited group!"});
      },
      function(obj) {
        me.setState({sending: false, error_msg: obj.error_msg, is_error: true});
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

  removeMember(evt, i) {
    this.state.user_ids.splice(i, 1);
    this.setState({user_ids: this.state.user_ids});
  },

  getInitialState() {
    return {
      group_name: "",
      initial_load_group: true,
      initial_load_users: true,
      error_msg: "",
      is_error: false,
      user_ids: [""],
      all_users: []
    };
  },

  componentDidMount() {
    var me = this;
    send_xhr("GET", "/api/groups/" + this.props.pstate.view_id, localStorage.getItem("session"), null,
      function(obj) {
        obj.data.user_ids = obj.data.user_ids.map(x => x.toString());
        obj.data.user_ids.push("");
        me.setState({
          group_name: obj.data.group_name,
          user_ids: obj.data.user_ids,
          error_msg: "",
          initial_load_group: false
        });
      },
      function(obj) {
        me.setState({initial_load_group: false, error_msg: obj.error_msg, is_error: true});
      }
    );
    send_xhr("GET", "/api/users/", localStorage.getItem("session"), null,
      function(obj) {
        me.setState({
          all_users: obj.data,
          initial_load_users: false
        });
      },
      function(obj) {
        me.setState({initial_load_users: false, error_msg: obj.error_msg, is_error: true});
      }
    );
  },

  render() {
    var me = this;
    var last_member = this.state.user_ids[this.state.user_ids.length-1];
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="col-md-6 col-md-offset-3">
              {this.state.initial_load_group || this.state.initial_load_users ? <Loader /> :
                <form>
                  <legend>Edit group {this.props.pstate.view_id}</legend>
                  {!this.state.error_msg ? <div></div> :
                    <div className={"alert " + (this.state.is_error ? "alert-danger" : "alert-success")}>
                      <strong>{this.state.error_msg}</strong>
                    </div>
                  }
                  <div className="form-group">
                    <label>Name</label>
                    <input type="text" className="form-control" placeholder="Name" value={this.state.group_name} onChange={(evt) => this.set("group_name", evt.target.value)}/>
                  </div>
                  <div className="form-group">
                  
                  <div className="row">
                    <div className="col-xs-4 col-md-4">
                      <label>Members</label>
                      {this.state.user_ids.slice(0, -1).map((x,i) =>
                        <ListMinusInput key={i} minusFunc={this.removeMember} value={x} index={i} editFunc={this.setMember} placeholder="Optional member ID" hasAddon={false}/>
                      )}
                      <ListPlusInput addFunc={this.addMember} value={last_member} index={this.state.user_ids.length-1} placeholder="Optional member ID" editFunc={this.setMember} hasAddon={true}/>
                    </div>
                    <div className="col-xs-4 col-xs-offset-4 col-md-4 col-md-offset-4">
                      <label>All users</label>
                      <table className="table table-hover">
                        <thead>
                          <tr>
                            <th>User ID</th>
                            <th>Username</th>
                          </tr>
                        </thead>
                        <tbody>
                          {me.state.all_users.map(x => <tr><td>{x.user_id}</td><td>{x.username}</td></tr>)}
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
                  <div className="btn-toolbar">
                    <button type="submit" className="btn btn-primary" onClick={this.editGroup} disabled={this.state.sending}>Save changes</button>
                    <button type="button" className="btn btn-default" onClick={this.cancel}>Go back</button>
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

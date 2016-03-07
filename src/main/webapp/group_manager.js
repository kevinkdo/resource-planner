const GroupManager = React.createClass({
  getInitialState() {  
    var error_msg = this.props.pstate.error_msg;
    var is_error = this.props.pstate.is_error;
    this.props.setPstate({error_msg: ""});
    return {
      initial_load: true,
      new_group_name: "",
      groups: {},
      error_msg: error_msg,
      is_error: is_error
    };
  },

  newGroup() {
    var new_group_name = prompt("New group name:");
    var me = this;
    if (new_group_name != null && new_group_name.length > 0) {
      this.setState({sending: true});
      send_xhr("POST", "/api/groups", localStorage.getItem("session"),
      JSON.stringify({group_name:new_group_name, user_ids: []}),
      function(obj) {
        me.refresh();
      },
      function(obj) {
        me.setState({sending: false, error_msg: obj.error_msg, is_error: true});
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
          initial_load: false
        });
      },
      function(obj) {
        me.setState({
          error_msg: obj.error_msg,
          is_error: true,
          initial_load: false
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
        me.setState({error_msg: obj.error_msg, is_error: true});
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
              <td>{x.group_name}</td>
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
          <h3>Groups
              <button type="button" className="btn btn-success pull-right" onClick={this.newGroup}><span className="glyphicon glyphicon-plus-sign" aria-hidden="true"></span> New group</button>
          </h3>
          {!this.state.error_msg ? <div></div> :
            <div className={"alert " + (this.state.is_error ? "alert-danger" : "alert-success")}>
              <strong>{this.state.error_msg}</strong>
            </div>
          }
          {table}
        </div>
      </div>
    );
  }
});

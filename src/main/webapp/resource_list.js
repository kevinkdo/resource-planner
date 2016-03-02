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
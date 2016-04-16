const ResourceList = React.createClass({
  getInitialState() {
    var error_msg = this.props.pstate.error_msg;
    var is_error = this.props.pstate.is_error;
    return {
      loading_tags: true,
      loading_table: true,
      tags: {},
      subroute: "list",
      resources: {},
      error_msg: error_msg,
      is_error: is_error, 
      selected_id: 0,
      selected_link: {
        source_id: 0,
        target_id: 0
      }
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
              me.setState({error_msg: obj.error_msg, is_error: true});
            }
          );
        }
      },
      function(obj) {
        me.refresh();
        me.setState({error_msg: obj.error_msg, is_error: true});
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
          subroute: "list"
        });
      },
      function(obj) {
        me.setState({error_msg: obj.error_msg, is_error: true});
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
          loading_tags: false,
          subroute: "list"
        });
      },
      function(obj) {
        me.setState({
          loading_tags: false,
          error_msg: error_msg,
          is_error: true
        });
      }
    );
  },

  componentDidMount() {
    this.props.setPstate({error_msg: ""});
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
                    <a key={"resourcetag" + tag} className="list-group-item pointer" onClick={(evt) => {evt.preventDefault(); me.cycleState(tag)}}>{tag}<span className="badge">{this.state.tags[tag]}</span></a>
                  )}
                </ul>
                {Object.keys(me.state.tags).length > 0 ? null :
                  <div className="lead text-center">No tags to display</div>}
              </div>
            }
          </div>
        </div>
      </div>
    var rightpane_list = this.state.loading_table ? <Loader /> : (
      <div>
        <ul className="nav nav-tabs">
          <li className={this.state.subroute == 'list' ? "active" : ""}><a href="#resource_list/0" onClick={(evt) => this.setState({subroute: "list"})}>Resource List</a></li>
          <li className={this.state.subroute == 'hierarchy' ? "active" : ""}><a href="#resource_list/0" onClick={(evt) => this.setState({subroute: "hierarchy"})}>Resource Hierarchy</a></li>
        </ul>
        <table className="table table-hover">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Restricted</th>
              <th>Tags</th>
              <th>Parent ID</th>
              <th>Shared Count</th>
              <th></th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {Object.keys(me.state.resources).map(id => {
              var x = me.state.resources[id];
              return <tr key={"resource " + id} className={x.restricted ? "warning" : ""}>
                <td>{id}</td>
                <td>{x.name}</td>
                <td>{x.restricted ? "Yes" : "No"}</td>
                <td>{x.tags.join(",")}</td>
                <td>TODO</td>
                <td>TODO</td>
                <td><a role="button" onClick={() => this.editResource(id)}>View/Edit</a></td>
                <td><a role="button" onClick={() => this.deleteResource(id)}>Delete</a></td>
              </tr>
            })}
            {Object.keys(me.state.resources).length > 0 ? null :
              <tr><td className="lead text-center" colSpan="7">No resources to display</td></tr>}
          </tbody>
        </table>
      </div>
    );
  
    var rightpane_hierarchy = this.state.loading_table ? <Loader /> : (
      <div>
        <ul className="nav nav-tabs" id="rightpane_hierarchy">
          <li className={this.state.subroute == 'list' ? "active" : ""}><a href="#resource_list/0" onClick={(evt) => this.setState({subroute: "list"})}>Resource List</a></li>
          <li className={this.state.subroute == 'hierarchy' ? "active" : ""}><a href="#resource_list/0" onClick={(evt) => this.setState({subroute: "hierarchy"})}>Resource Hierarchy</a></li>
        </ul>
        <br/>
        <br/>
        <ResourceTree setPstate={this.props.setPstate} pstate={this.props.pstate} setSelectedId={(new_id)=> me.setState({selected_id: new_id})} selected_id={me.state.selected_id} setSelectedLink={(link_ids)=> me.setState({selected_link: link_ids})} selected_link={me.state.selected_link} clearClick={() => me.setState({selected_id: 0, selected_link: {source_id: 0, target_id: 0}})}/>
      </div>
    );
    return (
      <div>
        <Navbar setPstate={this.props.setPstate} pstate={this.props.pstate}/>

        <div className="container">
          <div className="row">
            <div className="col-xs-4 col-md-3">
              {leftpane}
            </div>
            <div className="col-xs-8 col-md-9" onClick={me.clickOut}>
              <h3>Resources <button type="button" className="btn btn-success pull-right" onClick={() => this.props.setPstate({route: "resource_creator"})}><span className="glyphicon glyphicon-time" aria-hidden="true"></span> New resource</button></h3>
              {!this.state.error_msg ? <div></div> :
                <div className={"alert " + (this.state.is_error ? "alert-danger" : "alert-success")}>
                  <strong>{this.state.error_msg}</strong>
                </div>
              }
              {me.state.subroute == 'list' ? rightpane_list : rightpane_hierarchy}
            </div>
          </div>
        </div>
      </div>
    )
  }
});

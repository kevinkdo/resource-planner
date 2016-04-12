const ResourceList = React.createClass({
  getInitialState() {
    var error_msg = this.props.pstate.error_msg;
    var is_error = this.props.pstate.is_error;
    this.props.setPstate({error_msg: ""});
    return {
      loading_tags: true,
      loading_table: true,
      tags: {},
      subroute: "list",
      resources: {},
      error_msg: error_msg,
      is_error: is_error,
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

    // ----- TreeNode -----
    var TreeNode = React.createClass({
      getInitialState() {
        return {
          clicked: false
        };
      },

      handleClick(evt) {
        this.setState({clicked: !this.state.clicked});
        if (!this.state.clicked) {
          evt.target.setAttribute('fill', '#5cb85c');  
        } else {
          evt.target.setAttribute('fill', this.isRestricted() ? "#d9534f": "#5bc0de");  
        }        
      },

      handleXClick(evt) {
        this.props.deleteNode(this.props.resource_id);
        this.props.refresh();
      },

      render() {
        var marker = <circle r="8" fill={this.props.restricted ? "#d9534f": "#5bc0de"} cx={this.props.x+8} cy={this.props.y+8} onClick={this.handleClick}/>;
        var text = <text className="nodelabel" x={this.props.x + 20} y={this.props.y + 13}>{this.props.name}</text>;
        var x = <text className="deleteButton" x={this.props.x - 15} y={this.props.y + 13} onClick={this.handleXClick}>{this.state.clicked ? "X" : ""}</text>
        return <g>{marker}{x}{text}</g>;
      }
    });

    // ----- TreeLink -----
    var TreeLink = React.createClass({
      getInitialState() {
        return {
          clicked: false
        };
      },

      handleClick(evt) {
        this.setState({clicked: !this.state.clicked});
        if (!this.state.clicked) {
          evt.target.setAttribute('stroke', '#5cb85c');  
        } else {
          evt.target.setAttribute('stroke', '#999');  
        }        
      },   

      handleXClick(evt) {
        this.props.deleteLink(this.props.target);
        this.props.refresh();
      },

      render() {
        var line = <line className="link" stroke='#999' x1={this.props.source.x+8} y1={this.props.source.y+16} x2={this.props.target.x+8} y2={this.props.target.y} onClick={this.handleClick}></line>;
        var x = <text className="deleteButton" x={(this.props.source.x + this.props.target.x)/2 - 5} y={(this.props.source.y + this.props.target.y)/2 - 5} onClick={this.handleXClick}>{this.state.clicked ? "X" : ""}</text>
        return <g>{line}{x}</g>;
      }
    });

    // ----- ResourceTree -----
    var ResourceTree = React.createClass({ 
      getInitialState() {
        return {
          nodeId: 1,
          targetId: 0,
          sourceId: 0,
          xOffset: 0,
          yOffset: 0,
          selecting: false,
          tree: {
            name: "Dummy",
            children: [],
            ignore: true
          }
        };
      },

      setTargetId(targetId) {
        this.setState({
          targetId: targetId
        });
      },

      startDrag(dragState) {
        this.setState({
          sourceId: dragState.sourceId,
          xOffset: dragState.xOffset,
          yOffset: dragState.yOffset
        });
      },

      deleteLink(node) {
        var me = this;
        send_xhr("PUT", "/api/resources/" + node.resource_id.toString(), localStorage.getItem("session"),
          JSON.stringify({restricted: node.restricted, name: node.name, description: node.description || "", tags: node.tags, parent_id: 0, shared_count: node.shared_count}), 
          function(obj) {
            me.refresh(); 
          },
          function(obj) {
            me.setState({error_msg: obj.error_msg, is_error: true});
          }
        );
      },

      deleteNode(deleted_node_id) {
        //get deletedNodes's parent ID
        //find all children of deletedNode
        //replace all of the children's parent ID's with the deleted Node's parent ID
        //send the update in 
        //delete the node and send in that resource call 
        //refresh and re render 
        var deleted_node_parent_id = 0;
        var deleted_node_children = {};
        for (var i = 0; i < this.state.tree.children.length; i++) {
          if (this.state.tree.children[i].resource_id == deleted_node_id) { 
            deleted_node_children = this.state.tree.children[i].children;
            deleted_node_parent_id = this.state.tree.children[i].parent_id;
          } 
        }

        for (var i = 0; i < deleted_node_children.length; i++) {
          console.log(deleted_node_children[i].resource_id.toString());
          console.log(deleted_node_children[i].parent_id);
          console.log(deleted_node_parent_id);
          // send_xhr("PUT", "/api/resources/" + deleted_node_children[i].resource_id.toString(), localStorage.getItem("session"),
          //   JSON.stringify({restricted: deleted_node_children[i].restricted, name: deleted_node_children[i].name, description: deleted_node_children[i].description || "", tags: deleted_node_children[i].tags, parent_id: deleted_node_parent_id, shared_count: deleted_node_children[i].shared_count}), 
          //   function(obj) {
          //     console.log("much success");
          //   },
          //   function(obj) {
          //     me.setState({error_msg: obj.error_msg, is_error: true});
          //   }
          // );
        }
        
        send_xhr("DELETE", "/api/resources/" + deleted_node_id, localStorage.getItem("session"), null,
          function(obj) {
            me.refresh();
            me.render();
          },
          function(obj) {
            me.refresh();
            me.setState({error_msg: obj.error_msg, is_error: true});
          }
        );
      },

      refresh() {
        var me = this;
        send_xhr("GET", "/api/resources/forest", localStorage.getItem("session"), null,
          function(obj) {
            var forest = {};
            me.state.tree.children = obj.data.resources
            me.setState({
              tree: me.state.tree
            });
          },
          function(obj) {
            me.setState({error_msg: obj.error_msg, is_error: true});
          }
        );
      },

      componentDidMount() {
        this.refresh();

      },

      render() {
        var me = this;
        var width = window.innerWidth / 2;
        var height = window.innerHeight / 2;
        var tree = d3.layout.tree().size([width*5/6, height*5/6]);
        var nodes = tree.nodes(this.state.tree);
        var links = tree.links(nodes);
        
        var renderedNodes = nodes.map(function(node) {      
          if (node.ignore) {
            return null;
          }
          return <TreeNode key={node.id} id={node.id} x={node.x} y={node.y} name={node.name} resource_id={node.resource_id} setTargetId={me.setTargetId} dragging={me.state.sourceId != 0 ? true : false} selecting={me.state.selecting} subscript={node.subscript} restricted={node.restricted} refresh={me.refresh} deleteNode={me.deleteNode}/>;
        });

        var renderedLinks = links.map(function(link) {
          if (link.source.ignore) {
            return null;
          }
          return <TreeLink key={nodeId++} source={link.source} target={link.target} makeSubtreeRoot={me.makeSubtreeRoot} refresh={me.refresh}/>;
        });

        var svg = <svg id="mysvg" width={width} height={height}>{renderedNodes}{renderedLinks}</svg>;

        return <div>{svg}</div>;        
      }
    });

    var rightpane_hierarchy = this.state.loading_table ? <Loader /> : (
      <div>
        <ul className="nav nav-tabs" id="rightpane_hierarchy">
          <li className={this.state.subroute == 'list' ? "active" : ""}><a href="#resource_list/0" onClick={(evt) => this.setState({subroute: "list"})}>Resource List</a></li>
          <li className={this.state.subroute == 'hierarchy' ? "active" : ""}><a href="#resource_list/0" onClick={(evt) => this.setState({subroute: "hierarchy"})}>Resource Hierarchy</a></li>
        </ul>
        <br/>
        <br/>
        <ResourceTree/>
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
            <div className="col-xs-8 col-md-9">
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

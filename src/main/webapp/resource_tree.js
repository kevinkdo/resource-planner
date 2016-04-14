const ResourceTree = React.createClass({ 
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

  //click on one node and then click on another node to make it its parent 
  //perhaps color the other nodes so you know what's available 
  updateParent(resourceId, newParent) {
    var me = this;
    send_xhr("PUT", "/api/resources/" + resourceId.toString(), localStorage.getItem("session"),
      JSON.stringify({restricted: node.restricted, name: node.name, description: node.description || "", tags: node.tags, parent_id: newParent, shared_count: node.shared_count}), 
      function(obj) {
        me.refresh(); 
      },
      function(obj) {
        me.setState({error_msg: obj.error_msg, is_error: true});
      }
    );
  },

  setSelectedResource(id) {
    this.setState({selected_id: id});
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
      return <TreeNode key={node.id} id={node.id} x={node.x} y={node.y} name={node.name} resource_id={node.resource_id} setTargetId={me.setTargetId} dragging={me.state.sourceId != 0 ? true : false} selecting={me.state.selecting} subscript={node.subscript} restricted={node.restricted} refresh={me.refresh} deleteNode={me.deleteNode} setPstate={me.props.setPstate} setSelectedId={me.props.setSelectedId} is_selected={me.props.selected_id == node.resource_id}/>;
    });

    var renderedLinks = links.map(function(link) {
      if (link.source.ignore) {
        return null;
      }
      return <TreeLink key={nodeId++} source={link.source} target={link.target} deleteLink={me.deleteLink} refresh={me.refresh} setSelectedLink={me.props.setSelectedLink} is_selected={me.props.selected_link.source_id == link.source.resource_id && me.props.selected_link.target_id == link.target.resource_id}/>;
    });

    var helpText = <text className="helpText" x={0} y={0}>Click on a node to access node options. Click 'edit' to go to the resource edit page for that resource. Click the 'X' to delete the resource and make it's children children of the deleted node's parent. Click on a link to access link options. Click the 'X' to make the node and it's children it's own tree with the child node of the link becoming the new root.</text>;

    var svg = <svg id="mysvg" width={width} height={height}>{renderedNodes}{renderedLinks}</svg>;
    var clearButton = <button type="button" onClick={me.props.clearClick}>Clear</button>;

    return <div>{helpText}<br/>{clearButton}<br/>{svg}</div>;        
  }
});

const ResourceTree = React.createClass({ 
  getInitialState() {
    return {
      targetId: 0,
      sourceId: 0,
      xOffset: 0,
      yOffset: 0,
      selected_id: 0,
      selected_link: {
        source_id: 0,
        target_id: 0
      },
      tree: {
        name: "Dummy",
        children: [],
        ignore: true
      }
    };
  },

  setSelectedId(new_id) {
    this.setState({selected_id: new_id});
  },

  setSelectedLink(new_link) {
    this.setState({selected_link: new_link})
  },

  clearClick() {
    this.setState({selected_id: 0, selected_link: {source_id: 0, target_id: 0}});
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
    var me = this;
    send_xhr("DELETE", "/api/resources/" + deleted_node_id, localStorage.getItem("session"), null,
      function(obj) {
        me.refresh();
      },
      function(obj) {
        me.refresh();
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
        me.state.tree.children = obj.data.resources;
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
      return <TreeNode key={node.id} id={node.id} x={node.x} y={node.y} name={node.name} resource_id={node.resource_id} setTargetId={me.setTargetId} dragging={me.state.sourceId != 0 ? true : false} subscript={node.subscript} restricted={node.restricted} refresh={me.refresh} deleteNode={me.deleteNode} setPstate={me.props.setPstate} setSelectedId={me.setSelectedId} is_selected={me.state.selected_id == node.resource_id}/>;
    });

    var renderedLinks = links.map(function(link) {
      if (link.source.ignore) {
        return null;
      }
      return <TreeLink key={nodeId++} source={link.source} target={link.target} deleteLink={me.deleteLink} refresh={me.refresh} setSelectedLink={me.setSelectedLink} is_selected={me.state.selected_link.source_id == link.source.resource_id && me.state.selected_link.target_id == link.target.resource_id}/>;
    });

    var helpText = <text className="helpText" x={0} y={0}>Click on a node to access node options. Click 'edit' to go to the resource edit page for that resource. Click the 'X' to delete the resource and make it's children children of the deleted node's parent. Click on a link to access link options. Click the 'X' to make the node and it's children it's own tree with the child node of the link becoming the new root. Click the clear button to unselect any selected nodes or links.</text>;

    var svg = <svg id="mysvg" width={width} height={height} onClick={me.clearClick}>{renderedNodes}{renderedLinks}</svg>;

    return <div>{helpText}<br/>{svg}</div>;
  }
});

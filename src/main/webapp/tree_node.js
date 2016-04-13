const TreeNode = React.createClass({
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

  handleDeleteClick(evt) {
    this.props.deleteNode(this.props.resource_id);
    this.props.refresh();
  },

  handleEditClick(evt) {
    this.props.setPstate({
      route: "resource_editor",
      view_id: this.props.resource_id
    });
    this.props.refresh();
  },

  render() {
    var marker = <circle r="8" fill={this.props.restricted ? "#d9534f": "#5bc0de"} cx={this.props.x+8} cy={this.props.y+8} onClick={this.handleClick}/>;
    var text = <text className="nodelabel" x={this.props.x + 20} y={this.props.y + 13}>{this.props.name}</text>;
    var deleteButton = <text className="deleteButton" x={this.props.x - 15} y={this.props.y + 13} onClick={this.handleDeleteClick}>{this.state.clicked ? "X" : ""}</text>;
    var editButton = <text className="editButton" x={this.props.x - 45} y={this.props.y + 13} onClick={this.handleEditClick}>{this.state.clicked ? "edit" : ""}</text>;
    return <g>{marker}{deleteButton}{editButton}{text}</g>;
  }
});
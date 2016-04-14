const TreeNode = React.createClass({
  getFill() {
    if (this.props.is_selected) {
      return '#5cb85c';
    } else {
      return this.props.restricted ? "#d9534f": "#5bc0de"
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
    var marker = <circle r="8" fill={this.getFill()} cx={this.props.x+8} cy={this.props.y+8} onClick={() => this.props.setSelectedId(this.props.resource_id)}/>;
    var text = <text className="nodelabel" x={this.props.x + 20} y={this.props.y + 13}>{this.props.name}</text>;
    var deleteButton = <text className="deleteButton" x={this.props.x - 15} y={this.props.y + 13} onClick={this.handleDeleteClick}>{this.props.is_selected ? "X" : ""}</text>;
    var editButton = <text className="editButton" x={this.props.x - 45} y={this.props.y + 13} onClick={this.handleEditClick}>{this.props.is_selected ? "edit" : ""}</text>;
    return <g>{marker}{deleteButton}{editButton}{text}</g>;
  }
});